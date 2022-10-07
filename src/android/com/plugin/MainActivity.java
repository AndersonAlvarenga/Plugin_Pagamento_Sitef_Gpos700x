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

//Imports Gertec
import com.plugin.Beep;
import com.plugin.ConfigPrint;
import com.plugin.Led;
import com.plugin.Printer;
//-----------------------------------------------------------


//imports Clisitef
import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.CliSiTefI;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import android.os.Handler;


//---------------------------------------------------------------
public class MainActivity extends CordovaPlugin implements ICliSiTefListener{

    private CallbackContext callbackContext;
    private Intent intent;
    private String status;

    private Beep beep;
    private Led led;
    private Printer print;
    private ConfigPrint configPrint = new ConfigPrint();


    //Implementação Clisitef passando os dados como Paramentro

    private CliSiTef cliSiTef = null;
    private int trnResultCode;
    private static final int CAMPO_COMPROVANTE_CLIENTE = 121;
    private static final int CAMPO_COMPROVANTE_ESTAB = 122;
    private static int REQ_CODE = 4321;

    private static MainActivity instance;
    private class RequestCode {
        private static final int GET_DATA = 1;
        private static final int END_STAGE_1_MSG = 2;
        private static final int END_STAGE_2_MSG = 3;
    }
    private int id;
    private static String title;

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

    //Variaveis retorno Pagamento
    private static String titulo;
    private String statusPagamento="";
    private String impressão="";

    private int pulaLinha;
    private String mensagem;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.webView = webView;
        beep = new Beep(cordova.getActivity().getApplicationContext());
        led = new Led(cordova.getActivity().getApplicationContext());
        print = new Printer(cordova.getActivity().getApplicationContext());
    }

    public MainActivity() {
        super();
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        this.callbackContext = callbackContext;
        intent = null;
        //Metodos Pagamento
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

            new Thread(() -> {

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

                try {
                    new Thread().sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            //Inicia biblioteca Clisitef

            callbackContext.success("PagamentoFinalizado");
            return true;
        }
        if (action.equals("getTitulo")) {
            callbackContext.success(this.statusPagamento);
            return true;
        }
        if(action.equals("GetStringImpressao")) {
            callbackContext.success(this.impressão);
            return true;
        }

        //Impressão
        if (action.equals("checarImpressora")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = print.getStatusImpressora();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("imprimir")) {
            try {
                print.getStatusImpressora();
                if (print.isImpressoraOK()) {
                    JSONObject params = args.getJSONObject(0);
                    String tipoImpressao = params.getString("tipoImpressao");

                    switch (tipoImpressao) {
                        case "Texto":
                            mensagem = params.getString("mensagem");
                            String alinhar = params.getString("alinhar");
                            int size = params.getInt("size");
                            String fontFamily = params.getString("font");
                            Boolean opNegrito = params.getBoolean("opNegrito");
                            Boolean opItalico = params.getBoolean("opItalico");
                            Boolean opSublinhado = params.getBoolean("opSublinhado");

                            print.confgPrint(opItalico,opSublinhado,opNegrito,size,fontFamily,alinhar);
                            print.imprimeTexto(mensagem);
                            print.ImpressoraOutput();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("Erro " + e.getMessage());
            }
            callbackContext.success("Adicionado ao buffer");
            return true;
        }
        if (action.equals("impressoraOutput")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONObject params = args.getJSONObject(0);
                        if (params.has("avancaLinha")) {
                            pulaLinha = params.getInt("avancaLinha");
                            print.avancaLinha(pulaLinha);
                        }
                        print.ImpressoraOutput();
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                    callbackContext.success("Buffer impresso");
                }
            });
            return true;
        }

        //Métodos Led
        if (action.equals("ledOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOn();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOff();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledRedOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledRedOn();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledBlueOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledBlueOn();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledGreenOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledGreenOn();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledOrangeOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOrangeOn();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledRedOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledRedOff();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledBlueOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledBlueOff();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledGreenOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledGreenOff();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledOrangeOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOrangeOff();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }

        //Metodo Beep
        if (action.equals("beep")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = beep.beep();
                        Toast.makeText(cordova.getActivity(), status, Toast.LENGTH_LONG).show();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }

        return false; // Returning false results in a "MethodNotFound" error.
    }

    private static Handler hndMessage = new Handler() {
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case CliSiTefI.EVT_INICIA_ATIVACAO_BT:
                    break;
                case CliSiTefI.EVT_FIM_ATIVACAO_BT:
                    break;
                case CliSiTefI.EVT_INICIA_AGUARDA_CONEXAO_PP:
                    break;
                case CliSiTefI.EVT_FIM_AGUARDA_CONEXAO_PP:
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURANDO:
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURADO:
                    break;
                case CliSiTefI.EVT_PP_BT_DESCONECTADO:
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
                        Log.i("CAMPO_COMPROVANTE_CLIENTE",this.cliSiTef.getBuffer());
                    case CAMPO_COMPROVANTE_ESTAB:
                        Log.i("CAMPO_COMPROVANTE_ESTAB",this.cliSiTef.getBuffer());
                        imprimir(this.cliSiTef.getBuffer());
                }
                break;
            case CliSiTef.CMD_SHOW_MSG_CASHIER:
            case CliSiTef.CMD_SHOW_MSG_CUSTOMER:
            case CliSiTef.CMD_SHOW_MSG_CASHIER_CUSTOMER:
                Log.i("OnData","CMD_SHOW_MSG_CASHIER_CUSTOMER");
                //Conectando Servidor
                //Servidor Conectado
                //Aproxime, insira ou passe o cartão
                //Processando
                //SELECIONADO: Debito Nubank
                //Aguarde, em processamento...
                //Aguarde, em processamento...(35)

                //Transacao OK
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
                break;
            case CliSiTef.CMD_CONFIRM_GO_BACK:
            case CliSiTef.CMD_CONFIRMATION: {
                Log.i("OnData","CMD_CONFIRMATION");
                String ret = this.cliSiTef.getBuffer();
                int i = this.cliSiTef.abortTransaction(-1);
                return;
            }
            case CliSiTef.CMD_GET_FIELD_CURRENCY:
            case CliSiTef.CMD_GET_FIELD_BARCODE:
            case CliSiTef.CMD_GET_FIELD: {
                Log.i("OnData","CMD_GET_FIELD");
                String ret = this.cliSiTef.getBuffer();
                this.cliSiTef.continueTransaction("");
                return;
            }
            case CliSiTef.CMD_GET_MENU_OPTION: {
                //Segunda entrada
                Log.i("CMD_GET_MENU_OPTION","CMD_GET_MENU_OPTION");
                String ret = this.cliSiTef.getBuffer();
                switch (ret){
                    case "1:Cheque;2:Cartao de Debito;3:Cartao de Credito;4:Cartao Private Label;5:Confirmacao de Pre-autorizacao;":
                        data = this.contFormaPagamento;
                        this.cliSiTef.continueTransaction(data);
                        break;
                    default:
                        data = "1";
                        this.cliSiTef.continueTransaction(data);
                        break;
                }
                return;
            }
            case CliSiTef.CMD_PRESS_ANY_KEY: {
                Log.i("OnData","CMD_PRESS_ANY_KEY");
                String ret = this.cliSiTef.getBuffer();
                int i = this.cliSiTef.abortTransaction(-1);
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
    public void setStatus(String s){
        this.statusPagamento = s;
    }
    private void alert(String message) {
        String mensagem = message;
    }
    @Override
    public void onTransactionResult(int stage, int resultCode) {
        trnResultCode = resultCode;
        //alert ("Fim do estágio " + stage + ", retorno " + resultCode);
        if (stage == 1 && resultCode == 0) { // Confirm the transaction
            try {
                this.cliSiTef.finishTransaction(1);
            } catch (Exception e) {
                Log.e("onTransactionResult",e.getMessage());
            }
        } else {
            if (resultCode == 0) {
                //Transação ok e pode exibir comprovante
                String t = this.cliSiTef.getBuffer();
                t=t;
            } else {
                //Finaliza aplicação
            }
        }
    }
    private void imprimir(String texto){
        this.impressão = texto;
    }

}
