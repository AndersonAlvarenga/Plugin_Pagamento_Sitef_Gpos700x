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

import static android.hardware.Camera.Parameters.FLASH_MODE_ON;

import com.plugin.Pagamento;

//imports Clisitef
import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.CliSiTefI;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import android.os.Handler;


//---------------------------------------------------------------
public class MainActivity extends CordovaPlugin implements ICliSiTefListener{

    private CallbackContext callbackContext;
    private Intent intent;
    private Pagamento pag;
    private String status;


    //Implementação Clisitef passando os dados como Paramentro

    private CliSiTef cliSiTef = null;
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


    //Variaveis parametros entrada pagamento

    //Variaveis configuração servidor
    //-------------------------------
    private String confIpSitef;
    private String confCodigoLoja;
    private String confNumeroTerminal;
    //-------------------------------
    //Variaveis startTransaction
    //-------------------------------
    private String startValor;
    private String startCupomFiscal;
    private String startDataFiscal;
    private String startHorario;
    private String startOperador;
    //-------------------------------
    //Variaveis ContinueTransaction
    //-------------------------------
    private String contFormaPagamento;





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

        if (action.equals("pagamento")) {
            //Seta valores recebidos as variaveis de configuração
            JSONObject params = args.getJSONObject(0);
            this.confIpSitef = params.getString("ipSitef");
            this.confCodigoLoja = params.getString("codigoLoja");
            this.confNumeroTerminal = params.getString("numeroTerminal");
            this.startValor = params.getString("valor");
            this.startCupomFiscal = params.getString("cupomFiscal");
            this.startDataFiscal = params.getString("dataFiscal");
            this.startHorario = params.getString("horario");
            this.startOperador = params.getString("operador");
            this.contFormaPagamento = params.getString("formaPagamento");

            //Inicia biblioteca Clisitef
            try{
                if(this.cliSiTef == null){
                    this.cliSiTef = new CliSiTef(cordova.getActivity().getApplicationContext());
                    this.cliSiTef.setMessageHandler(hndMessage);
                    this.cliSiTef.setDebug(true);
                    int idConfig = this.cliSiTef.configure(
                            this.confIpSitef,
                            this.confCodigoLoja,
                            this.confNumeroTerminal,
                            "TipoPinPad=Android_AUTO");

                }
                this.cliSiTef.setActivity(cordova.getActivity());
                int i = this.cliSiTef.startTransaction(
                        this,
                        0,
                        this.startValor,
                        this.startCupomFiscal,
                        this.startDataFiscal,
                        this.startHorario,
                        this.startOperador,
                        "");

            }catch (Exception e){
                callbackContext.error("Erro " + e.getMessage());
            }
            callbackContext.success("PagamentoFinalizado");
            return true;
        }

        return false; // Returning false results in a "MethodNotFound" error.
    }

    private static Handler hndMessage = new Handler() {
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case CliSiTefI.EVT_INICIA_ATIVACAO_BT:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"Ativando BT", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case CliSiTefI.EVT_FIM_ATIVACAO_BT:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"PinPad", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case CliSiTefI.EVT_INICIA_AGUARDA_CONEXAO_PP:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"Aguardando pinpad", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case CliSiTefI.EVT_FIM_AGUARDA_CONEXAO_PP:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURANDO:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"Configurando pinpad", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURADO:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"Pinpad configurado", android.widget.Toast.LENGTH_SHORT).show();
                    break;
                case CliSiTefI.EVT_PP_BT_DESCONECTADO:
                    //android.widget.Toast.makeText(cordova.getActivity().getApplicationContext(),"Pinpad desconectado", android.widget.Toast.LENGTH_SHORT).show();
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
                String ret = this.cliSiTef.getBuffer();
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
                String ret = this.cliSiTef.getBuffer();
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
                String ret = this.cliSiTef.getBuffer();
                switch (this.title){
                    case "Selecione a forma de pagamento":
                        this.cliSiTef.continueTransaction(this.contFormaPagamento);
                        break;
                    case  "":
                        break;
                }

                //seleciona o opção escolhida


               /* Intent i = new Intent(this, Itens.class);
                i.putExtra("title", title);
                i.putExtra("message", this.cliSiTef.getBuffer());
                i.putExtra("request",RequestCode.GET_DATA);
                cordova.getActivity().startActivityForResult(i);
                System.out.println(this.cliSiTef.getBuffer());*/
                return;
            }
            case CliSiTef.CMD_PRESS_ANY_KEY: {
                Log.i("OnData","CMD_PRESS_ANY_KEY");
                String ret = this.cliSiTef.getBuffer();
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
                //finish();
            } else {

            }
        }
    }



}
