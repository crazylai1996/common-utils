package gdou.laixiaoming.commonutils.config;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ZooKeeperConfig {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperConfig.class);

    @Bean
    public ZooKeeper zooKeeper() throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 3000, event -> {
            // 建立连接
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                logger.info("ZooKeeper连接成功");
            }
        });
        return zooKeeper;
    }

}
