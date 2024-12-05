package ro.orange.smartoff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

    /*@Bean
    public JavaMailSender emailSender() {
        return new JavaMailSenderImpl();
    }*/

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}