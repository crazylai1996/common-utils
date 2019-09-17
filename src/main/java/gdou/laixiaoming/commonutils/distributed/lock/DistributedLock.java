package gdou.laixiaoming.commonutils.distributed.lock;

public interface DistributedLock {

    /**
     * 非阻塞锁获取
     * @Author laiminghai
     * @Date 16:14 2019/09/16
     * @param lockKey
     * @param val
     * @param lockTime
     * @return boolean
     **/
    boolean tryLock(String lockKey, String val, int lockTime);

    /**
     * 阻塞获取锁，直到获取成功
     * @Author laiminghai
     * @Date 16:15 2019/09/16
     * @param lockKey
     * @param val
     * @param lockTime 锁占用时间，单位S
     * @return void
     **/
    void lock(String lockKey, String val, int lockTime) throws InterruptedException;

    /**
     * 阻塞获取锁，接受超时时间
     * @Author laiminghai
     * @Date 16:15 2019/09/16
     * @param lockKey
     * @param val
     * @param lockTime 锁占用时间，单位S
     * @param timeout 锁获取超时时间，单位S
     * @return boolean
     **/
    boolean lock(String lockKey, String val, int lockTime, int timeout) throws InterruptedException;

    /**
     * 锁释放
     * @Author laiminghai
     * @Date 16:23 2019/09/16
     * @param lockKey
     * @return boolean
     **/
    boolean unlock(String lockKey);
}
