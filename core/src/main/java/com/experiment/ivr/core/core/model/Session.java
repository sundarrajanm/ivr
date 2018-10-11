package com.experiment.ivr.core.core.model;

import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@ToString
public class Session {

    public static enum KEYS {
        CURRENT_NODE_ID ("CURRENT_NODE_ID");

        // getter method
        private String value;
        public String getValue()
        {
            return this.value;
        }
        KEYS(String value)
        {
            this.value = value;
        }
    };

    private String callId;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public void putData(String key, Object val) {
        this.data.put(key, val);
    }

    public Object getData(String key) {
        return this.data.get(key);
    }
}
