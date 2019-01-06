package com.gwidgets.mongotest;


import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataMongoTest(excludeAutoConfiguration= {EmbeddedMongoAutoConfiguration.class})
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private final static List<String> USER_ID_LIST = Arrays.asList("b2b1f340-cba2-11e8-ad5d-873445c542a2", "bd5dd3a4-cba2-11e8-9594-3356a2e7ef10");

    private static final Random RANDOM = new Random();


    @BeforeEach
    public void dataSetup() {

        Transaction transaction;

        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            if (i % 2 == 0) {
                transaction = new Transaction(requestId, true, USER_ID_LIST.get(RANDOM.nextInt(2)), System.currentTimeMillis());
            } else {
                transaction = new Transaction(requestId, false, USER_ID_LIST.get(RANDOM.nextInt(2)), System.currentTimeMillis());
            }

            transactionRepository.save(transaction);
        }

    }


    @Test
    public void findSuccessfullOperationsForUserWithCreatedDateLessThanNowTest() {
        long now = System.currentTimeMillis();
        String userId = USER_ID_LIST.get(RANDOM.nextInt(2));
        List<Transaction> resultsPage =  transactionRepository.findBySuccessIsTrueAndCreatedLessThanEqualAndUserIdOrderByCreatedDesc(now, userId, PageRequest.of(0, 5)).getContent();

        assertThat(resultsPage).isNotEmpty();
        assertThat(resultsPage).extracting("userId").allMatch(id -> Objects.equals(id, userId));
        assertThat(resultsPage).extracting("created").isSortedAccordingTo(Collections.reverseOrder());
        assertThat(resultsPage).extracting("created").first().matches(createdTimeStamp -> (Long)createdTimeStamp <= now);
        assertThat(resultsPage).extracting("success").allMatch(sucessfull -> (Boolean)sucessfull == true);
    }

    @Configuration
    static class MongoConfiguration implements InitializingBean, DisposableBean {

         MongodExecutable executable;

        @Override
        public void afterPropertiesSet() throws Exception {
            String host = "localhost";
            int port = 27019;

            IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                    .net(new Net(host, port, Network.localhostIsIPv6()))
                    .build();

            MongodStarter starter = MongodStarter.getDefaultInstance();
            executable = starter.prepare(mongodConfig);
            executable.start();
        }


        @Bean
        public MongoDbFactory factory() {
            MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(new MongoClientURI("mongodb://localhost:27019/test_db"));
            return mongoDbFactory;
        }


        @Bean
        public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory) {
            MongoTemplate template = new MongoTemplate(mongoDbFactory);
            template.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            return template;
        }

        @Bean
        public MongoRepositoryFactoryBean mongoFactoryRepositoryBean(MongoTemplate template) {
            MongoRepositoryFactoryBean mongoDbFactoryBean = new MongoRepositoryFactoryBean(TransactionRepository.class);
            mongoDbFactoryBean.setMongoOperations(template);

            return mongoDbFactoryBean;
        }

        @Override
        public void destroy() throws Exception {
            executable.stop();
        }
    }
}