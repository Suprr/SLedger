package com.SLedger;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Keys {
//    public PrivateKey loadPrivateKey(String key64){
//        byte[] clear = Base64.decodeBase64(key64);
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
//        KeyFactory fact = KeyFactory.getInstance("RSA");
//        PrivateKey priv = fact.generatePrivate(keySpec);
//        Arrays.fill(clear, (byte) 0);
//        return priv;
//    }
//
//
//    public PublicKey loadPublicKey(String stored){
//        byte[] data = Base64.decodeBase64(stored);
//        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
//        KeyFactory fact = KeyFactory.getInstance("RSA");
//        return fact.generatePublic(spec);
//    }

    public static String savePrivateKey(PrivateKey priv){
        String key64 = Base64.getEncoder().encodeToString(priv.getEncoded());
        return key64;
    }


    public static String savePublicKey(PublicKey publ){
        String key64 = Base64.getEncoder().encodeToString(publ.getEncoded());
        return 	key64;
    }

}
