package com.plugin;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

//imports Clisitef
import android.os.Handler;
import android.util.Log;
import android.support.annotation.Nullable;




public class Pagamento extends AppCompatActivity{

    //Variaveis Clisitef
    private CliSiTef cliSiTef;
    private int trnResultCode;
    private static final int CAMPO_COMPROVANTE_CLIENTE = 121;
    private static final int CAMPO_COMPROVANTE_ESTAB = 122;
    private static int REQ_CODE = 4321;
    private static String title;
    private static Pagamento instance;
    private class RequestCode {
        private static final int GET_DATA = 1;
        private static final int END_STAGE_1_MSG = 2;
        private static final int END_STAGE_2_MSG = 3;
    }
    private static TextView text;
    private int id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("pagamento", "layout", package_name));
        id = getResources().getIdentifier("textStatus","id",package_name);
        text = (TextView)findViewById(id);
        text.setText(getIntent().getExtras().getString("message"));
    }
}
