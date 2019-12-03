package com3001.cw.ik00157.sportnearme.utilities;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSADecryptionCipher {

    private RSAPrivateKeySpec privateKeySpec;
    private PrivateKey privateKey;
    private Cipher privCipher;

    public RSADecryptionCipher(String strPrivateKeyModulus,
                               String strPrivateKeyExponent) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {
        BigInteger privateKeyModulus = new BigInteger(strPrivateKeyModulus);
        BigInteger privateKeyExponent = new BigInteger(strPrivateKeyExponent);
        privateKeySpec = new RSAPrivateKeySpec(privateKeyModulus, privateKeyExponent);
        KeyFactory kFactory = KeyFactory.getInstance("RSA");
        privateKey = kFactory.generatePrivate(privateKeySpec);
        createCipher();

    }

    public void createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        privCipher = Cipher.getInstance("RSA");
        privCipher.init(Cipher.DECRYPT_MODE, privateKey);
    }

    public String decrypt(String hexRepresentationEncryption) throws BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedDataBytes = hexStrToBytes(hexRepresentationEncryption);
        byte[] decryptedDataBytes = privCipher.doFinal(encryptedDataBytes);
        String plaintext = new String(decryptedDataBytes);
        return plaintext;
    }

    public byte[] hexStrToBytes(String hexStr){
        byte[] bytes = new byte[hexStr.length()/2];
        for(int i = 0; i < bytes.length; i++){
            bytes[i] = (byte) Integer.parseInt(hexStr.substring(i*2,  i*2+2), 16);
        }
        return bytes;
    }
}
