package com.bloxmove.marketmaker.config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.bitmart.api.Call;
import com.bitmart.api.CloudContext;
import com.bitmart.api.key.CloudKey;
import com.bloxmove.marketmaker.actor.ExchangeManagerActor;
import com.bloxmove.marketmaker.actor.Guardian;
import com.bloxmove.marketmaker.model.ExchangeName;
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
    public Call bitMartClient(ExchangeProperties exchangeProperties) {
        return new Call(new CloudContext(new CloudKey(
                exchangeProperties.getApiKey(ExchangeName.BITMART.toString()),
                exchangeProperties.getApiSecret(ExchangeName.BITMART.toString()),
                exchangeProperties.getMemo(ExchangeName.BITMART.toString()))));
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
}
