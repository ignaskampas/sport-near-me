package com3001.cw.ik00157.sportnearme.utilities;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAEncryptionCipher {

    private RSAPublicKeySpec publicKeySpec;
    private PublicKey publicKey;
    private Cipher pubCipher;
    private BigInteger publicKeyModulus;
    private BigInteger publicKeyExponent;
    private boolean cipherIsSetup = false;

    public RSAEncryptionCipher(String strPublicKeyModulus,
                               String strPublicKeyExponent) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {
        publicKeyModulus = new BigInteger(strPublicKeyModulus);
        publicKeyExponent = new BigInteger(strPublicKeyExponent);
        publicKeySpec = new RSAPublicKeySpec(publicKeyModulus, publicKeyExponent);
        KeyFactory kFactory = KeyFactory.getInstance("RSA");
        publicKey = kFactory.generatePublic(publicKeySpec);
        createCipher();
    }

    public void createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        pubCipher = Cipher.getInstance("RSA");
        pubCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipherIsSetup = true;
    }

    public String encrypt(String plaintext) {
        byte[] plaintextBytes = plaintext.getBytes();
        byte[] encryptionBytes = new byte[0];
        try {
            encryptionBytes = pubCipher.doFinal(plaintextBytes);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        String hexRepresentationEncryption = bytesToHex(encryptionBytes);
        return hexRepresentationEncryption;
    }

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();

    }

    public boolean cipherIsSetup(){
        return cipherIsSetup;
    }
}
