package com.trade.test;


public class SimpleEncoder implements Encoder {

    private final String publicKey;

    private final String privateKey;

    private final String algorithm;

    private final String keyId;

    public SimpleEncoder(String publicKey, String privateKey, String algorithm, String keyId) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.algorithm = algorithm;
        this.keyId = keyId;
    }

    public static EncoderBuilder withKeyPair(String publicKey, String privateKey) {
        EncoderBuilder builder = new EncoderBuilder(publicKey, privateKey);
        return builder;
    }

    @Override
    public void printConfig() {
        System.out.println("公钥：" + publicKey);
        System.out.println("已配置私钥：" + (privateKey != null));
        System.out.println("算法：" + algorithm);
        System.out.println("密钥标识：" + keyId);
    }
}
