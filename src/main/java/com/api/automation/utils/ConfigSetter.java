package com.api.automation.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigSetter {
    private final static Map<String, Object> CONFIG_VALUES;

    static {
        final InputStream inputStream = ConfigSetter.class.getClassLoader().getResourceAsStream("application.yml");
        CONFIG_VALUES = new Yaml().load(inputStream);
    }

    public static Object getConfigValue(String configName) {
        return CONFIG_VALUES.get(configName);
    }
}
