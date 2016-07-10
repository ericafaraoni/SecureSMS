package com.ssms.securesms;

import android.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;


public class SymmetricCipher {

    private Cipher cipher;
    private IvParameterSpec ips;
    private byte[] IV;
    private Key key;

    public SymmetricCipher(Key key, String xform, byte[] IV) throws Exception
    {
        this.cipher = Cipher.getInstance(xform);
        this.IV = IV;
        this.key = key;

        if(this.IV != null)
            this.ips = new IvParameterSpec(IV);
    }

    public void setKey(Key key)
    {
        this.key = key;
    }

    public String encrypt(String plainText) throws Exception
    {
        String cipherText = "";
        // encrypt
        if(IV != null)
            cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        else
            cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes());
        cipherText = Base64.encodeToString(cipherTextBytes, Base64.DEFAULT);
        return cipherText;
    }

    public String decrypt(String cipherText) throws Exception
    {
        String plainText = "";
        byte[] cipherTextBytes = Base64.decode(cipherText, Base64.DEFAULT);

        if(IV != null)
            cipher.init(Cipher.DECRYPT_MODE, key, ips);
        else
            cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] plainTextBytes =  cipher.doFinal(cipherTextBytes);
        for(int i=0; i<plainTextBytes.length; i++)
            plainText += (char)plainTextBytes[i];
        return plainText;
    }
}
