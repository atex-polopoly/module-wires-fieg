package com.atex.custom.util;

import java.util.Properties;

public class PrefixedProperty extends Properties {
    public String getProperty(String group, String key) {
        return getProperty(group + '.' + key);
    }
}
