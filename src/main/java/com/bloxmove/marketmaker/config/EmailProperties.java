package com.bloxmove.marketmaker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "spring.mail")
@ConstructorBinding
public class EmailProperties {
    @NotBlank
    private final String host;
    @NotNull
    @Positive
    private final Integer port;
    @NotBlank
    private final String username;
    @NotBlank
    private final String password;
    @NotBlank
    private final String addressee;
    @NotNull
    private final Map<String, String> properties;

    public EmailProperties(String host, Integer port, String username, String password, String addressee,
                           Map<String, String> properties) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.addressee = addressee;
        this.properties = properties;
    }

    public String getHost() {
        return this.host;
    }

    public Integer getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAddressee() {
        return this.addressee;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }
}
