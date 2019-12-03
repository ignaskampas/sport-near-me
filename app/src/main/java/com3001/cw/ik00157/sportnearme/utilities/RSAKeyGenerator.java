package com3001.cw.ik00157.sportnearme.utilities;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RSAKeyGenerator {

    RSAPublicKeySpec publicKeySpec;
    RSAPrivateKeySpec privateKeySpec;

    public RSAKeyGenerator() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2024);
        KeyPair kp = kpg.genKeyPair();
        KeyFactory fact = KeyFactory.getInstance("RSA");
        publicKeySpec = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
        privateKeySpec = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
    }

    public BigInteger getPublicKeyModulus(){
        return publicKeySpec.getModulus();
    }

    public BigInteger getPublicKeyExponent(){
        return publicKeySpec.getPublicExponent();
    }

    public BigInteger getPrivateKeyExponent(){
        return privateKeySpec.getPrivateExponent();
    }

    public BigInteger getPrivateKeyModulus(){
        return privateKeySpec.getModulus();
    }
}
