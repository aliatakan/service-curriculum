package com.example.demo.producer;

/**
 * Created by aliatakan on 20/11/17.
 */
public interface EventProducer {

    void emit(String partitionKey, byte[] data);
}
