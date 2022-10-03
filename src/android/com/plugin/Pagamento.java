package com.plugin;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

//imports Clisitef
/*
import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.CliSiTefI;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import android.os.Handler;
import android.util.Log;*/

import com.verdemar.bibliotecapagamento.MainLib;


public class Pagamento extends Activity{

    //Variaveis Clisitef
    /*private CliSiTef cliSiTef;
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
    private TextView text;
    private int id;*/
    private MainLib mainLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("pagamento", "layout", package_name));
        id = getResources().getIdentifier("textStatus","id",package_name);
        text = (TextView)findViewById(id);
        mainLib = new MainLib();
        mainLib.InitTransaction();
        /*try{
            this.cliSiTef = new CliSiTef(getApplicationContext());
            this.cliSiTef.setMessageHandler(hndMessage);
            this.cliSiTef.setDebug(true);
            int idConfig = this.cliSiTef.configure("10.0.213.78", "00000000", "pdvrd.90","TipoPinPad=Android_AUTO");
            this.cliSiTef.setActivity(this);
            int i = this.cliSiTef.startTransaction(this,110,"12","123456","20120514","120000","Teste","");

        }catch (Exception e){
            Log.i("Erro",e.getMessage());
        }*/

    }

}
