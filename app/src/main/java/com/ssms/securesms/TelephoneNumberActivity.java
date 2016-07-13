package com.ssms.securesms;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
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
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class TelephoneNumberActivity extends AppCompatActivity {

    private String action, nonceA, nonceB, myPhone;
    public final static String EXTRA_MESSAGE = "com.ssms.securesms.MESSAGE";

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
        action = intent.getStringExtra("action");
        // Retrieve nonces
        if(action.equals("SEND"))
            nonceA = intent.getStringExtra("nonceA");
        else
            nonceB = intent.getStringExtra("nonceB");

        // Keyboard buttons
        Button keyboard[] = new Button[12];
        keyboard[0] = (Button) findViewById(R.id.ButtonKeypad1Activity);
        keyboard[1] = (Button) findViewById(R.id.ButtonKeypad2Activity);
        keyboard[2] = (Button) findViewById(R.id.ButtonKeypad3Activity);
        keyboard[3] = (Button) findViewById(R.id.ButtonKeypad4Activity);
        keyboard[4] = (Button) findViewById(R.id.ButtonKeypad5Activity);
        keyboard[5] = (Button) findViewById(R.id.ButtonKeypad6Activity);
        keyboard[6] = (Button) findViewById(R.id.ButtonKeypad7Activity);
        keyboard[7] = (Button) findViewById(R.id.ButtonKeypad8Activity);
        keyboard[8] = (Button) findViewById(R.id.ButtonKeypad9Activity);
        keyboard[9] = (Button) findViewById(R.id.ButtonKeypadBackActivity);
        keyboard[10] = (Button) findViewById(R.id.ButtonKeypad0Activity);
        keyboard[11] = (Button) findViewById(R.id.ButtonKeypadOkActivity);

    }

    private void sendMsg0(String destPhone) throws Exception
    {
        AsymmetricCipher ac = new AsymmetricCipher("RSA/ECB/PKCS1Padding");
        KeyStorage myAsymStorage;
        PublicKey bPublicKey;

        // retrieve B's public key
        File path = Environment.getExternalStorageDirectory();
        String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
        myAsymStorage = new KeyStorage(keysPath, "", destPhone + ".key", "");
        bPublicKey = myAsymStorage.loadPublicKey();
        // retrieve my phone number
        Scanner s = new Scanner(new FileReader(keysPath + "myPhone"));
        myPhone = s.next();
        s.close();
        // prepare SMS text
        String plainText = nonceA + "|" + myPhone;
        // encrypt plaintext
        String cipherText = ac.encrypt(plainText, bPublicKey);
        // send message
        SmsManager smanager = SmsManager.getDefault();
        ArrayList<String> parts = smanager.divideMessage(cipherText);
        smanager.sendMultipartTextMessage(destPhone, null, parts, null, null);
    }


    private int sendMsg1(String destPhone) throws Exception
    {
        AsymmetricCipher ac = new AsymmetricCipher("RSA/ECB/PKCS1Padding");
        KeyStorage myAsymStorage, mySymStorage;
        PrivateKey myKey;
        PublicKey aPublicKey;
        String cipherText;

        // retrieve A's public key
        File path = Environment.getExternalStorageDirectory();
        String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
        myAsymStorage = new KeyStorage(keysPath, keysPath, destPhone + ".key", "private.key");
        aPublicKey = myAsymStorage.loadPublicKey();
        // retrieve my private key
        myKey = myAsymStorage.loadPrivateKey();

        // retrieve cipher text SMS: it is the last received message from the sender whose phone number is equal to 'destPhone'
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        // check if there messages; if no messages are found, it returns -1
        if(cursor==null || cursor.getCount()==0) {
            return -1;
        }
        // scroll all messages and find the last sent by 'destPhone'; if no message from destPhone is sent, it returns -2
        for(;;)
        {
            String tmpSender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            if(tmpSender.equals(destPhone))
            {
                // message found! it retrieves the cipher text and breaks the loop
                cipherText = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                break;
            }
            // try the next message; if no more messages are available, it returns -2
            if(!cursor.moveToNext())
                return -2;
        }
        cursor.close();
        // decrypt first and split the message; msgFields[0] contains A's nonce, msgFields[1] contains A's phone number
        String plainText = ac.decrypt(cipherText, myKey);
        String[] msgFields = plainText.split("[\\x7C]"); // 'x7C' ASCII code for vertical bar '|'
        // check for sender equality; if destPhone is not equal to the number contained in the message, it returns -3
        if(!destPhone.equals(msgFields[1]))
            return -3;

        // prepare SMS text
        // retrieve my phone number
        Scanner s = new Scanner(new FileReader(keysPath + "myPhone"));
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
        SmsManager smanager = SmsManager.getDefault();
        ArrayList<String> parts = smanager.divideMessage(cipherText);
        smanager.sendMultipartTextMessage(destPhone, null, parts, null, null);
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
                    Toast.makeText(getApplicationContext(), "Numero di telefono non valido", Toast.LENGTH_LONG).show();
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
                            // if an exception occours during the protocol execution, the app returns on the main activity
                            nextActivityIntent = new Intent(this, MainActivity.class);
                            Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
                        }
                        nextActivityIntent = new Intent(this, SendTextActivity.class);
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
                                        errorString = "Errore! Nessun messaggio presente.";
                                        break;

                                    case -2:
                                        errorString = "Errore! Nessun messaggio inviato da " + telText + ".";
                                        break;
                                    case -3:
                                        errorString = "Errore sul mittente!";
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
                            // if an exception occours during the protocol execution, the app returns on the main activity
                            Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Premi OK per continuare", Toast.LENGTH_LONG).show();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
         Creazione fragment
    */
    public static class TelephoneNumberFragment extends Fragment {

        public TelephoneNumberFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_insert_number, container, false);
        }
    }

}
