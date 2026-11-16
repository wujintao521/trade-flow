package com.trade.test;

import java.util.function.Consumer;

public class EncoderBuilder {

    private final String publicKey;

    private final String privateKey;

    private String algorithm;

    private final KeyBuilder keyBuilder = new KeyBuilder();

    public EncoderBuilder(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void algorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void jwkPostProcessor(Consumer<KeyBuilder> processor) {
        processor.accept(keyBuilder);
    }

    public SimpleEncoder build() {
        SimpleEncoder encoder = new SimpleEncoder(publicKey, privateKey, algorithm, keyBuilder.getKeyId());
        return encoder;
    }
}