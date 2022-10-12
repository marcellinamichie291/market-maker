package com.bloxmove.marketmaker.config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.bloxmove.marketmaker.actor.ExchangeManagerActor;
import com.bloxmove.marketmaker.actor.Guardian;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.ExchangeManagerActorWrapper;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import com.bloxmove.marketmaker.service.ValidationService;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import com.bloxmove.marketmaker.util.AskPatternHelper;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MarketMakerConfiguration {

    @Bean
    public ActorSystem<Guardian.Command> actorSystem(ConfigurableApplicationContext springContext,
                                                     ExchangeClientFactory exchangeClientFactory,
                                                     ValidationService validationService,
                                                     MarketMakerRepository marketMakerRepository,
                                                     MailService mailService,
                                                     NotificationRepository notificationRepository) {
        val guardianBehavior = Guardian.create(springContext, exchangeClientFactory,
                validationService, marketMakerRepository, mailService, notificationRepository);
        return ActorSystem.create(guardianBehavior, "guardian");
    }

    @Bean
    public ActorRef<ExchangeManagerActor.Command> exchangeManagerActor(
            ActorSystem<Guardian.Command> actorSystem, AskPatternHelper askPattern) {
        return askPattern.ask(actorSystem, Guardian.GetExchangeManagerActor::new)
                .map(Guardian.GetExchangeMangerActorResponse::getExchangeManagerActor)
                .block();
    }

    @Bean
    public ExchangeManagerActorWrapper exchangeManagerActorWrapper(
            ActorRef<ExchangeManagerActor.Command> managerActor, AskPatternHelper askPatternHelper) {
        return new ExchangeManagerActorWrapper(managerActor, askPatternHelper);
    }

    @Bean
    public AskPatternHelper askPatternHelper(ActorSystem<Guardian.Command> actorSystem) {
        return new AskPatternHelper(actorSystem);
    }

    @Bean
    public JavaMailSender mailSender(EmailProperties emailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());

        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.putAll(emailProperties.getProperties());

        return mailSender;
    }

//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowCredentials(true);
//        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
//        corsConfiguration.setAllowedHeaders(Arrays.asList("Origin", "Access-Control-Allow-Origin", "Content-Type",
//                "Accept", "Authorization", "Origin, Accept", "X-Requested-With",
//                "Access-Control-Request-Method", "Access-Control-Request-Headers"));
//        corsConfiguration.setExposedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization",
//                "Access-Control-Allow-Origin", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
//        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
//        return new CorsWebFilter(urlBasedCorsConfigurationSource);
//    }
}
