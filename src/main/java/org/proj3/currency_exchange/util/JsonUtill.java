package org.proj3.currency_exchange.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtill {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtill() {

    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
