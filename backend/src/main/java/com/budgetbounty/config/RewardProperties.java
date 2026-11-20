package com.budgetbounty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "budgetbounty.reward")
@Getter
@Setter
public class RewardProperties {

    private double redemptionPointValueInr = 0.25;

    private Map<String, Integer> defaultRules = new HashMap<>();
}
