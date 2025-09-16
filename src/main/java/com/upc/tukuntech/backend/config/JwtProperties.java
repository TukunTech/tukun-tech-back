package com.upc.tukuntech.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter @Setter
public class JwtProperties {
    private String secret;

    private String issuer = "tukuntech";

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration accessTokenExpiration  = Duration.ofMinutes(30);

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration refreshTokenExpiration  = Duration.ofDays(14);

    private int maximumSessions = 5;
}
