package com.trade.test;

public class Main {

    public static Encoder createEncoder(String publicKey, String privateKey, String keyId) {
        EncoderBuilder builder = SimpleEncoder.withKeyPair(publicKey, privateKey);
        builder.algorithm("RS256");
        builder.jwkPostProcessor(keyBuilder -> keyBuilder.keyID(keyId));
        return builder.build();
    }

    public static Encoder createEncoder1(String publicKey, String privateKey, String keyId) {
        EncoderBuilder builder1 = new EncoderBuilder(publicKey, privateKey);
        builder1.algorithm("RS256");
        builder1.jwkPostProcessor(keyBuilder -> keyBuilder.keyID(keyId));
        return builder1.build();
    }


    public static void main(String[] args) {
        Encoder encoder = createEncoder("演示公钥", "演示私钥", "key-001");
        //Encoder encoder1 = createEncoder("演示公钥1", "演示私钥1", "key-002");
        //encoder1.printConfig();
        encoder.printConfig();
    }
}
