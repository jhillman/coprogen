package com.example.model;
 
import com.google.gson.Gson;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import java.lang.SuppressWarnings;
 
public final class ModelAdapterFactory {
    public static TypeAdapterFactory create() {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                Class<T> rawType = (Class<T>) type.getRawType();
 
                if (com.example.model..Place.class.isAssignableFrom(rawType)) {
                    return (TypeAdapter<T>) new com.example.model..Place.GsonTypeAdapter(gson); 
                } else {
                    return null;
                }
            }
        };
    }
}