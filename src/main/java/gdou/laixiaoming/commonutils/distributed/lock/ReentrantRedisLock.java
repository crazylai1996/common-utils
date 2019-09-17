package gdou.laixiaoming.commonutils.distributed.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis锁-可重入
 */
@Component
public class ReentrantRedisLock implements DistributedLock{

    private ThreadLocal<Map<String, AtomicInteger>> locksHolder = new ThreadLocal<>();

    private DistributedLock redisLock;

    @Autowired
    public ReentrantRedisLock(JedisPool jedisPool) {
        this.redisLock = new RedisLock(jedisPool);
    }

    protected Map<String, AtomicInteger> getLocks() {
        Map<String, AtomicInteger> locks = locksHolder.get();
        if (locks != null) {
            return locks;
        }
        locksHolder.set(new HashMap<>());
        return locksHolder.get();
    }

    @Override
    public boolean tryLock(String lockKey, String val, int lockTime) {
        Map<String, AtomicInteger> locks = getLocks();
        AtomicInteger lockCount = locks.get(lockKey);
        if(lockCount != null) {
            lockCount.incrementAndGet();
            return true;
        }
        if(doTryLock(lockKey, val, lockTime)) {
            locks.put(lockKey, new AtomicInteger(1));
            return true;
        }
        return false;
    }

    @Override
    public void lock(String lockKey, String val, int lockTime) throws InterruptedException {
        Map<String, AtomicInteger> locks = getLocks();
        AtomicInteger lockCount = locks.get(lockKey);
        if(lockCount != null) {
            lockCount.incrementAndGet();
            return ;
        }
        doLock(lockKey, val, lockTime);
        locks.put(lockKey, new AtomicInteger(1));
    }

    @Override
    public boolean lock(String lockKey, String val, int lockTime, int timeout) throws InterruptedException {
        Map<String, AtomicInteger> locks = getLocks();
        AtomicInteger lockCount = locks.get(lockKey);
        if(lockCount != null) {
            lockCount.incrementAndGet();
            return true;
        }
        if(doLock(lockKey, val, lockTime, timeout)) {
            locks.put(lockKey, new AtomicInteger(1));
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock(String lockKey) {
        Map<String, AtomicInteger> locks = getLocks();
        AtomicInteger lockCount = locks.get(lockKey);
        if(lockCount == null) {
            return false;
        }
        int newLockCount = lockCount.decrementAndGet();
        if(newLockCount > 0) {
            return true;
        }
        locks.remove(lockKey);
        return doUnLock(lockKey);
    }

    private boolean doTryLock(String lockKey, String val, int lockTime) {
        return redisLock.tryLock(lockKey, val, lockTime);
    }

    private void doLock(String lockKey, String val, int lockTime) throws InterruptedException {
        redisLock.lock(lockKey, val, lockTime);
    }

    private boolean doLock(String lockKey, String val, int lockTime, int timeout) throws InterruptedException {
        return redisLock.lock(lockKey, val, lockTime, timeout);
    }

    private boolean doUnLock(String lockKey) {
        return redisLock.unlock(lockKey);
    }
}
