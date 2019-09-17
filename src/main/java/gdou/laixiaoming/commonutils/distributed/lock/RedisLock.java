package gdou.laixiaoming.commonutils.distributed.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

/**
 * Redis锁
 */
@Component
public class RedisLock implements DistributedLock{

    private static final String LOCK_SUCCESS_FLAG = "OK";
    private static final Long UNLOCK_SUCCESS_FLAG = 1L;
    private static final long SLEEP_TIME = 200;

    private JedisPool jedisPool;

    @Autowired
    public RedisLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    private Jedis getRedisConnection() {
        return jedisPool.getResource();
    }

    @Override
    public boolean tryLock(String lockKey, String val, int lockTime) {
        String result = getRedisConnection().set(lockKey, val, SetParams.setParams().nx().ex(lockTime));
        if(LOCK_SUCCESS_FLAG.equals(result)){
            return true;
        }
        return false;
    }

    @Override
    public void lock(String lockKey, String val, int lockTime) throws InterruptedException {
        for(; ;){
            String result = getRedisConnection().set(lockKey, val, SetParams.setParams().nx().ex(lockTime));
            if(LOCK_SUCCESS_FLAG.equals(result)){
                return ;
            }
            Thread.sleep(SLEEP_TIME);
        }
    }

    @Override
    public boolean lock(String lockKey, String val, int lockTime, int timeout) throws InterruptedException {
        long bolckTime = timeout * 1000;
        while (bolckTime >= 0) {
            String result = getRedisConnection().set(lockKey, val, SetParams.setParams().nx().ex(lockTime));
            if(LOCK_SUCCESS_FLAG.equals(result)){
                return true;
            }
            Thread.sleep(SLEEP_TIME);
            bolckTime -= SLEEP_TIME;
        }
        return false;
    }

    @Override
    public boolean unlock(String lockKey) {
        Long result = getRedisConnection().del(lockKey);
        return UNLOCK_SUCCESS_FLAG.equals(result);
    }

}
