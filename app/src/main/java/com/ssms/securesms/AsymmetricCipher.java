package com.ssms.securesms;

import javax.crypto.Cipher;
import java.security.Key;
import android.util.Base64;


class AsymmetricCipher
{
    private Cipher cipher;

    public AsymmetricCipher(String xform) throws Exception
    {
        this.cipher = Cipher.getInstance(xform);
    }

    public String encrypt(String plainText, Key key) throws Exception
    {
        String cipherText;
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes());
        cipherText = Base64.encodeToString(cipherTextBytes, Base64.DEFAULT);
        return cipherText;
    }

    public String decrypt(String cipherText, Key key) throws Exception
    {
        String plainText = "";
        byte[] cipherTextBytes = Base64.decode(cipherText, Base64.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
        for(int i=0; i<plainTextBytes.length; i++)
            plainText += (char)plainTextBytes[i];
        return plainText;
    }
}