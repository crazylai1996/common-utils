package gdou.laixiaoming.commonutils.distributed.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ZK分布式锁
 */
public class ZooKeeperDistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperDistributedLock.class);

    private ZooKeeper zk;

    private String root;
    private String lockKey;
    private String path;

    private static final byte[] data = new byte[0];
    private ThreadLocal<String> nodeHolder = new ThreadLocal<>();

    public ZooKeeperDistributedLock(ZooKeeper zk, String root, String lockKey) {
        this.zk = zk;
        this.root = root;
        this.lockKey = lockKey;
        this.path = root + "/" + lockKey;
        try{
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws KeeperException, InterruptedException {
        Stat stat = zk.exists(root, false);
        if (stat == null) {
            // 创建根节点
            zk.create(root, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    /**
     * 尝试获取锁
     * @return
     */
    public boolean tryLock() {
        String currentNode = null;
        try {
            currentNode = zk.create(path, data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            TreeSet<String> sortedNodeSet = getRootChildren();
            if (sortedNodeSet.first().equals(currentNode)) {
                nodeHolder.set(currentNode);
                return true;
            }
            deleteNode(currentNode);
        } catch (Exception e) {
            deleteNode(currentNode);
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 等待获取锁
     */
    public void waitForLock() {
        try {
            String currentNode = zk.create(path, data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            TreeSet<String> sortedNodeSet = getRootChildren();
            if (sortedNodeSet.first().equals(currentNode)) {
                nodeHolder.set(currentNode);
                logger.info("获取{}", currentNode);
                return ;
            }

            CountDownLatch latch = new CountDownLatch(1);
            Stat stat = null;
            String preNode = currentNode;
            do {
                preNode = sortedNodeSet.lower(preNode);
                if(preNode == null) {
                    nodeHolder.set(currentNode);
                    return ;
                }
                stat = zk.exists(preNode, new UnLockWatcher(latch, currentNode));
            }while (stat == null);

            latch.await();
            nodeHolder.set(currentNode);
            latch = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 等待获取锁, 接受超时时间
     * @param timeout
     * @param timeUnit
     * @return
     */
    public boolean waitForLock(int timeout, TimeUnit timeUnit) {
        String currentNode = null;
        try {
            currentNode = zk.create(path, data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            TreeSet<String> sortedNodeSet = getRootChildren();
            if (sortedNodeSet.first().equals(currentNode)) {
                nodeHolder.set(currentNode);
                return true;
            }

            CountDownLatch latch = new CountDownLatch(1);
            Stat stat = null;
            String preNode = currentNode;
            do {
                preNode = sortedNodeSet.lower(preNode);
                if(preNode == null) {
                    nodeHolder.set(currentNode);
                    return true;
                }
                stat = zk.exists(preNode, new UnLockWatcher(latch, currentNode));
            }while (stat == null);

            boolean result = latch.await(timeout, timeUnit);
            if(!result) {
                deleteNode(currentNode);
                return false;
            }
            nodeHolder.set(currentNode);
            latch = null;
            return true;
        } catch (Exception e) {
            deleteNode(currentNode);
            throw new RuntimeException(e);
        }
    }

    /**
     * 锁释放
     */
    public void unlock() {
        try {
            if(nodeHolder.get() == null) {
                return ;
            }
            deleteNode(nodeHolder.get());
            nodeHolder.remove();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TreeSet<String> getRootChildren() {
        try {
            List<String> rootChildren = zk.getChildren(root, false);
            TreeSet<String> sortedNodeSet = new TreeSet<>();
            for (String node : rootChildren) {
                sortedNodeSet.add(root + "/" + node);
            }
            return sortedNodeSet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteNode(String path) {
        try{
            zk.delete(path, -1);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 锁等待监听
     */
    private class UnLockWatcher implements Watcher {

        private CountDownLatch countDownLatch;
        private String currentNode;

        public UnLockWatcher(CountDownLatch countDownLatch, String currentNode) {
            this.countDownLatch = countDownLatch;
            this.currentNode = currentNode;
        }

        @Override
        public void process(WatchedEvent event) {
            if(event.getType() != Event.EventType.NodeDeleted) {
                return ;
            }
            TreeSet<String> sortedNodeSet = getRootChildren();
            //节点已被删除
            if(!sortedNodeSet.contains(currentNode)) {
                return ;
            }
            String firstNode = sortedNodeSet.first();
            //当前节点是第一个节点, 即认为获取到了锁, 否则继续监听前一节点
            if(currentNode.equals(firstNode)){
                countDownLatch.countDown();
                return ;
            }
            try{
                Stat stat = null;
                String preNode = currentNode;
                do {
                    preNode = sortedNodeSet.lower(preNode);
                    if(preNode == null) {
                        countDownLatch.countDown();
                        return ;
                    }
                    //继续监听前一个节点
                    stat = zk.exists(preNode, this);
                }while (stat == null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
