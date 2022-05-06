package com.bloxmove.marketmaker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EmailPropertiesTest {

    @Autowired
    private EmailProperties emailProperties;

    @Test
    void shouldTestPropertiesValidation() {
        assertThat(emailProperties.getHost()).isNotBlank();
        assertThat(emailProperties.getPort()).isNotNull().isPositive();
        assertThat(emailProperties.getUsername()).isNotBlank();
        assertThat(emailProperties.getPassword()).isNotBlank();
        assertThat(emailProperties.getAddressee()).isNotBlank();
        assertThat(emailProperties.getProperties()).isNotNull();
    }

}