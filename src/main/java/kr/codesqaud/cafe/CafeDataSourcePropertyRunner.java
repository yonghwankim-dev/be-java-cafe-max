package kr.codesqaud.cafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CafeDataSourcePropertyRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CafeDataSourcePropertyRunner.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${my.name}")
    private String myName;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("url : {}", url);
        logger.info("username : {}", username);
        logger.info("password : {}", password);
        logger.info("myName : {}", myName);
    }
}
