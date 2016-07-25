package com.ssms.securesms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class TelephoneNumberActivity extends AppCompatActivity {

    private String action, nonceA, nonceB, myPhone;
    public final static String EXTRA_MESSAGE = "com.ssms.securesms.Action";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telephone_number);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TelephoneNumberFragment()).commit();
        }

        // Retrieve the operation
        Intent intent = getIntent();
        action = intent.getStringExtra(EXTRA_MESSAGE);

        // Retrieve nonces
        if(action.equals("SEND"))
            nonceA = intent.getStringExtra("nonceA");
        else
            nonceB = intent.getStringExtra("nonceB");

    }

    //Send the first message of the session key exchange protocol (if Action=SEND)
    private void sendMsg0(String destPhone) throws Exception
    {
        AsymmetricCipher ac = new AsymmetricCipher("RSA/ECB/PKCS1Padding");
        KeyStorage myAsymStorage;
        PublicKey bPublicKey;
        smsHandler hdl;

        // retrieve B's public key
        File path = Environment.getExternalStorageDirectory();
        String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
        myAsymStorage = new KeyStorage(keysPath, "", destPhone + ".key", "");
        bPublicKey = myAsymStorage.loadPublicKey();

        // retrieve my phone number
        Scanner s = new Scanner(new FileReader(keysPath + "myPhone.txt"));
        myPhone = s.next();
        s.close();

        // prepare SMS text
        String plainText = nonceA + "|" + myPhone;

        // encrypt plaintext
        String cipherText = ac.encrypt(plainText, bPublicKey);

        // send message
        hdl = new smsHandler(this,destPhone);
        hdl.smsSend(cipherText);

    }

    //Send the second message of the session key exchange protocol (if Action=RECEIVE)
    private int sendMsg1(String destPhone) throws Exception
    {
        AsymmetricCipher ac = new AsymmetricCipher("RSA/ECB/PKCS1Padding");
        KeyStorage myAsymStorage, mySymStorage;
        PrivateKey myKey;
        PublicKey aPublicKey;
        String cipherText;
        smsHandler hdl;

        // retrieve A's public key
        File path = Environment.getExternalStorageDirectory();
        String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
        myAsymStorage = new KeyStorage(keysPath, keysPath, destPhone + ".key", "private.key");
        aPublicKey = myAsymStorage.loadPublicKey();
        // retrieve my private key
        myKey = myAsymStorage.loadPrivateKey();

        // retrieve cipherText
        hdl = new smsHandler(this, destPhone);
        cipherText = hdl.smsReceive();
        if(cipherText.equals("Error1")) {
            return -1;
        }
        if(cipherText.equals("Error2")) {
            return -2;
        }

        // decrypt first and split the message; msgFields[0] contains A's nonce, msgFields[1] contains A's phone number
        String plainText = ac.decrypt(cipherText, myKey);
        String[] msgFields = plainText.split("[\\x7C]"); // 'x7C' ASCII code for vertical bar '|'
        // check for sender equality; if destPhone is not equal to the number contained in the message, it returns -3
        if(!destPhone.equals(msgFields[1]))
            return -3;

        // prepare SMS text
        // retrieve my phone number
        Scanner s = new Scanner(new FileReader(keysPath + "myPhone.txt"));
        myPhone = s.next();
        s.close();

        // generate the session key
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey key = kg.generateKey();
        // save the session key
        mySymStorage = new KeyStorage(keysPath, myPhone + "_" + destPhone + ".key", "AES");
        mySymStorage.saveSharedKey(key);
        // encode key into a DEC string
        byte [] encodedKey = key.getEncoded();
        String encodedKeyString = "";
        for(int i=0; i<encodedKey.length; i++)
        {
            encodedKeyString += encodedKey[i];
            if(i == (encodedKey.length - 1))
                continue;
            encodedKeyString += " ";
        }
        // prepare text to encrypt
        plainText = msgFields[0] + "|" + nonceB + "|" + myPhone + "|" + encodedKeyString;
        // encrypt plaintext
        cipherText = ac.encrypt(plainText, aPublicKey);

        // send message
        hdl.smsSend(cipherText);
        return 0;
    }

    /*
         Gestione button
    */
    public void buttonAction(View arg0)
    {
        TextView telTextView = (TextView) findViewById(R.id.textViewActivity);
        String telText = telTextView.getText().toString();
        Intent nextActivityIntent = null;

        switch(arg0.getId())
        {
            case R.id.ButtonKeypadBackActivity:
                if(telText.length() > 0)
                    telText = telText.substring(0, telText.length() - 1);
                break;

            case R.id.ButtonKeypadOkActivity:
                if(telText.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Phone number not valid", Toast.LENGTH_LONG).show();
                    break;
                }
                else
                {
                    if(action.equals("SEND"))
                    {
                        try
                        {
                            sendMsg0(telText);
                        }
                        catch(Exception e)
                        {
                            // if an exception occurs during the protocol execution, the app returns on the main activity
                            Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
                            nextActivityIntent = new Intent(this, MainActivity.class);
                            startActivity(nextActivityIntent);
                        }
                        nextActivityIntent  = new Intent(this, SendTextActivity.class);
                        nextActivityIntent.putExtra("nonceA", nonceA);
                        nextActivityIntent.putExtra("destPhone", telText);
                    }
                    else
                    {
                        try
                        {
                            String errorString = "";
                            int tmp = sendMsg1(telText);

                            if(tmp != 0)
                            {
                                switch(tmp)
                                {
                                    case -1:
                                        errorString = "Error! No SMS found.";
                                        break;
                                    case -2:
                                        errorString = "Errore! No SMS from " + telText + " found.";
                                        break;
                                    case -3:
                                        errorString = "Error on the sender!";
                                        break;
                                }
                                Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
                                nextActivityIntent = new Intent(this, MainActivity.class);
                                startActivity(nextActivityIntent);
                            }
                            nextActivityIntent = new Intent(this, ReceiveTextActivity.class);
                            nextActivityIntent.putExtra("nonceB", nonceB);
                            nextActivityIntent.putExtra("destPhone", telText);
                            nextActivityIntent.putExtra("myPhone", myPhone);
                        }
                        catch(Exception e)
                        {
                            // if an exception occurs during the protocol execution, the app returns on the main activity
                            Toast.makeText(getApplicationContext(), "Sorry, an error occurs!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                startActivity(nextActivityIntent);

                break;

            default:
                if(telText.length() < 15)
                {
                    Button b = (Button)arg0;
                    telText += b.getText().toString();
                }
                else
                    Toast.makeText(getApplicationContext(), "Click ok OK to continue", Toast.LENGTH_LONG).show();
                break;

        }

        telTextView.setText(telText);
    }

    /*
         Creazione menu
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.MENU_1) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
         Fragment creation
    */
    public static class TelephoneNumberFragment extends Fragment {

        public TelephoneNumberFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_telephone_number, container, false);
        }
    }

}