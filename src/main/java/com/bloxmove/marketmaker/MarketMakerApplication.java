package com.bloxmove.marketmaker;

import com.bloxmove.marketmaker.config.EmailProperties;
import com.bloxmove.marketmaker.config.ExchangeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ExchangeProperties.class, EmailProperties.class})
public class MarketMakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketMakerApplication.class, args);
	}

}
