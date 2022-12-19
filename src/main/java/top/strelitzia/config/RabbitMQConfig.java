package top.strelitzia.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public CachingConnectionFactory getConnectionFactory() {
        ConnectionFactory rabbitConnectionFactory = new ConnectionFactory();
        rabbitConnectionFactory.setHost("175.24.31.205");
        rabbitConnectionFactory.setPort(5672);
        rabbitConnectionFactory.setUsername("angelina");
        rabbitConnectionFactory.setPassword("123456");
        rabbitConnectionFactory.setAutomaticRecoveryEnabled(true);
        rabbitConnectionFactory.setNetworkRecoveryInterval(5000);
        return new CachingConnectionFactory(rabbitConnectionFactory);
    }
}
