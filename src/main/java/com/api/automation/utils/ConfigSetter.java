package com.api.automation.utils;

import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

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
