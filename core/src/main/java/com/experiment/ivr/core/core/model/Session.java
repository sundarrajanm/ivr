package com.experiment.ivr.core.core.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Session {

    public static enum KEYS {
        CURRENT_NODE_ID
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
