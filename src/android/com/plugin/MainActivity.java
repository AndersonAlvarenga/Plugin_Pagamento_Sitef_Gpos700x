package com.plugin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import static android.app.Activity.RESULT_OK;
import static android.app.Activity.RESULT_CANCELED;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import com.plugin.Pagamento;

//Import Clisitef
import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.CliSiTefI;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import android.os.Handler;
import android.widget.Toast;
import android.widget.TextView;

//---------------------------------------------------------------
public class MainActivity extends CordovaPlugin implements ICliSiTefListener{

    private CallbackContext callbackContext;
    private Intent intent;
    private Pagamento pag;
    private String status;

    //Variaveis Clisitef
    private CliSiTef cliSiTef;
    private int trnResultCode;
    private static final int CAMPO_COMPROVANTE_CLIENTE = 121;
    private static final int CAMPO_COMPROVANTE_ESTAB = 122;
    private static int REQ_CODE = 4321;
    private static String title;
    private static MainActivity instance;
    private class RequestCode {
        private static final int GET_DATA = 1;
        private static final int END_STAGE_1_MSG = 2;
        private static final int END_STAGE_2_MSG = 3;
    }
    private int id;
    private static TextView text;



    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.webView = webView;
        this.pag = new Pagamento();
    }

    public MainActivity() {
        super();
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        this.callbackContext = callbackContext;
        intent = null;

        if (action.equals("testePagamento")) {
            try{
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        try {
                            this.cliSiTef = new CliSiTef(context);
                            this.cliSiTef.setMessageHandler(hndMessage);
                            this.cliSiTef.setDebug(true);
                            int idConfig = this.cliSiTef.configure("10.0.213.78", "00000000", "pdvrd.90","TipoPinPad=Android_AUTO");
                            this.cliSiTef.setActivity(cordova.getActivity());
                            int i = this.cliSiTef.startTransaction(this,110,"12","123456","20120514","120000","Teste","");
                            callbackContext.success("OK");
                        } catch (Exception e) {
                            e.printStackTrace();
                            callbackContext.error("Erro " + e.getMessage());
                        }
                    }
                });

            }catch (Exception e){
                Log.i("Erro",e.getMessage());
                e.printStackTrace();
                callbackContext.error("Erro " + e.getMessage());
            }

            return true;
        }
        if (action.equals("testeIntent")) {
            try{
                intent = new Intent(context, Pagamento.class);
                intent.putExtra("input", "TestePutExtra");
                cordova.getActivity().startActivity(intent);
            }catch (Exception e){
                Log.i("Erro",e.getMessage());
                e.printStackTrace();
                callbackContext.error("Erro " + e.getMessage());
            }

            return true;
        }



        return false; // Returning false results in a "MethodNotFound" error.
    }

    //Implementação Clisitef

    private static Handler hndMessage = new Handler() {
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case CliSiTefI.EVT_INICIA_ATIVACAO_BT:
                    instance.setStatus("Ativando BT");
                    break;
                case CliSiTefI.EVT_FIM_ATIVACAO_BT:
                    //instance.setProgressBarIndeterminateVisibility(false);
                    instance.setStatus("PinPad");
                    break;
                case CliSiTefI.EVT_INICIA_AGUARDA_CONEXAO_PP:
                    //instance.setProgressBarIndeterminateVisibility(true);
                    instance.setStatus("Aguardando pinpad");
                    break;
                case CliSiTefI.EVT_FIM_AGUARDA_CONEXAO_PP:
                    //instance.setProgressBarIndeterminateVisibility(false);
                    instance.setStatus("");
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURANDO:
                    //instance.setProgressBarIndeterminateVisibility(true);
                    instance.setStatus("Configurando pinpad");
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURADO:
                    //instance.setProgressBarIndeterminateVisibility(false);
                    instance.setStatus("Pinpad configurado");
                    break;
                case CliSiTefI.EVT_PP_BT_DESCONECTADO:
                    //instance.setProgressBarIndeterminateVisibility(false);
                    instance.setStatus("Pinpad desconectado");
                    break;
            }
        }
    };

    @Override
    public void onData(int stage, int command, int fieldId, int minLength, int maxLength, byte[] input) {
        String data = "";
        if (stage == 1) {
            // Evento onData recebido em uma startTransaction
        } else if (stage == 2) {
            // Evento onData recebido em uma finishTransaction
        }
        switch (command) {
            case CliSiTef.CMD_RESULT_DATA:
                switch (fieldId) {
                    case CAMPO_COMPROVANTE_CLIENTE:
                        Log.i("CAMPO_COMPROVANTE_CLIENTE","CAMPO_COMPROVANTE_CLIENTE");
                    case CAMPO_COMPROVANTE_ESTAB:
                        Log.i("CAMPO_COMPROVANTE_ESTAB","CAMPO_COMPROVANTE_ESTAB");
                        alert(this.cliSiTef.getBuffer());
                }
                break;
            case CliSiTef.CMD_SHOW_MSG_CASHIER:
            case CliSiTef.CMD_SHOW_MSG_CUSTOMER:
            case CliSiTef.CMD_SHOW_MSG_CASHIER_CUSTOMER:
                Log.i("OnData","CMD_SHOW_MSG_CASHIER_CUSTOMER");
                setStatus(this.cliSiTef.getBuffer());
                break;
            case CliSiTef.CMD_SHOW_MENU_TITLE:
            case CliSiTef.CMD_SHOW_HEADER:
                //Primiro Entrada
                title = this.cliSiTef.getBuffer();
                Log.i("OnData","CMD_SHOW_HEADER");
                break;
            case CliSiTef.CMD_CLEAR_MSG_CASHIER:
            case CliSiTef.CMD_CLEAR_MSG_CUSTOMER:
            case CliSiTef.CMD_CLEAR_MSG_CASHIER_CUSTOMER:
            case CliSiTef.CMD_CLEAR_MENU_TITLE:
            case CliSiTef.CMD_CLEAR_HEADER:
                Log.i("OnData","CMD_CLEAR_HEADER");
                this.setStatus("");
                title = "";
                break;
            case CliSiTef.CMD_CONFIRM_GO_BACK:
            case CliSiTef.CMD_CONFIRMATION: {
                Log.i("OnData","CMD_CONFIRMATION");
                /*Intent i = new Intent(this, yesno.class);
                i.putExtra("title", title);
                i.putExtra("message", this.cliSiTef.getBuffer());
                starActivityForResult.launch(i);*/

                return;
            }
            case CliSiTef.CMD_GET_FIELD_CURRENCY:
            case CliSiTef.CMD_GET_FIELD_BARCODE:
            case CliSiTef.CMD_GET_FIELD: {
                Log.i("OnData","CMD_GET_FIELD");
                /*Intent i = new Intent(this, Dialog.class);
                i.putExtra("title", title);
                i.putExtra("message", this.cliSiTef.getBuffer());
                i.putExtra("request",RequestCode.GET_DATA);
                starActivityForResult.launch(i);*/
                return;
            }
            case CliSiTef.CMD_GET_MENU_OPTION: {
                //Segunda entrada
                Log.i("CMD_GET_MENU_OPTION","CMD_GET_MENU_OPTION");

                /*Intent i = new Intent(this, Itens.class);
                i.putExtra("title", title);
                i.putExtra("message", this.cliSiTef.getBuffer());
                i.putExtra("request",RequestCode.GET_DATA);
                cordova.getActivity().startActivityForResult(i);
                System.out.println(this.cliSiTef.getBuffer());*/
                return;
            }
            case CliSiTef.CMD_PRESS_ANY_KEY: {
                Log.i("OnData","CMD_PRESS_ANY_KEY");
               /* Intent i = new Intent(this, mensagem.class);
                i.putExtra("message", this.cliSiTef.getBuffer());
                starActivityForResult.launch(i);*/
                return;
            }
            case CliSiTef.CMD_ABORT_REQUEST:
                Log.i("OnData","CMD_ABORT_REQUEST");
                break;
            default:
                Log.i("default","default");
                break;
        }


        this.cliSiTef.continueTransaction(data);
    }
    public static void setStatus(String s){
        String t = s;
        t = t;
    }
    private void alert(String message) {
        String mensagem = message;
        //Toast.makeText(this,mensagem,Toast.LENGTH_LONG).show();
    }
    @Override
    public void onTransactionResult(int stage, int resultCode) {
        trnResultCode = resultCode;
        //alert ("Fim do estágio " + stage + ", retorno " + resultCode);
        if (stage == 1 && resultCode == 0) { // Confirm the transaction
            try {
                this.cliSiTef.finishTransaction(1);
            } catch (Exception e) {
                //alert(e.getMessage());
            }
        } else {

            if (resultCode == 0) {

            } else {

            }
        }
    }

}
