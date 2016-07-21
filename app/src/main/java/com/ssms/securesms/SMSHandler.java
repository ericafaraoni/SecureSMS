package com.ssms.securesms;

import android.telephony.SmsManager;

import java.util.ArrayList;

public class smsHandler {

    private String smsText;
    private String destPhone;

    public smsHandler(String smsText, String destPhone){

        this.smsText =  smsText;
        this.destPhone = destPhone;
    }

    public void smsSend(){

        SmsManager smanager = SmsManager.getDefault();
        ArrayList<String> parts = smanager.divideMessage(smsText);
        smanager.sendMultipartTextMessage(destPhone, null, parts, null, null);

    }


}
