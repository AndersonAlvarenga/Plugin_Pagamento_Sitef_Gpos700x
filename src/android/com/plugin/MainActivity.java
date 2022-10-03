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

//Imports Clisitef
//---------------------------------------------------------------

import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.CliSiTefI;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import android.os.Handler;
import android.util.Log;

//---------------------------------------------------------------
public class MainActivity extends CordovaPlugin{

    public static final String G700 = "GPOS700";
    public static final String G800 = "Smart G800";
    private static final String version = "RC03";
    public static String Model = Build.MODEL;
    private String resultado_Leitor;
    private IntentIntegrator qrScan;
    private IntentIntegrator qrScanv2;
    private String titulo;
    private String tipo;
    private String status;
    private String mensagem;
    private ArrayList<String> arrayListTipo;
    private CallbackContext callbackContext;
    private CallbackContext scancallbackContext;
    private Intent intent;
    private int pulaLinha;

    //Variavais Clisitef
    //-----------------------------------------------------------------
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
    //-----------------------------------------------------------------


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.webView = webView;
        gertecPrinter = new GertecPrinter(cordova.getActivity().getApplicationContext());
        gertecPrinter.setConfigImpressao(configPrint);

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
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    intent = null;
                    try {
                        intent = new Intent(context, Pagamento.class);
                        cordova.getActivity().startActivity(intent);
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

    private void startCamera() {
        cordova.setActivityResultCallback(this);
        qrScan = new IntentIntegrator(cordova.getActivity());
        qrScan.setPrompt("Digitalizar o código " + titulo);
        qrScan.setBeepEnabled(true);
        qrScan.setBarcodeImageEnabled(true);
        qrScan.setTimeout(30000); // 30 * 1000 => 3 minuto
        qrScan.addExtra("FLASH_MODE_ON", FLASH_MODE_ON);
        qrScan.initiateScan(Collections.singleton(this.tipo));
    }

    private void ImprimeTodasAsFucoes() {

        configPrint.setItalico(false);
        configPrint.setNegrito(true);
        configPrint.setTamanho(20);
        configPrint.setFonte("MONOSPACE");
        gertecPrinter.setConfigImpressao(configPrint);
        try {
            gertecPrinter.getStatusImpressora();
            // Imprimindo Imagem
            configPrint.setiWidth(300);
            configPrint.setiHeight(130);
            configPrint.setAlinhamento("CENTER");
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("==[Iniciando Impressao Imagem]==");
            gertecPrinter.imprimeImagem("gertec_2");
            gertecPrinter.avancaLinha(10);
            gertecPrinter.imprimeTexto("====[Fim Impressão Imagem]====");
            gertecPrinter.avancaLinha(10);
            // Fim Imagem

            // Impressão Centralizada
            configPrint.setAlinhamento("CENTER");
            configPrint.setTamanho(30);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("CENTRALIZADO");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Centralizada

            // Impressão Esquerda
            configPrint.setAlinhamento("LEFT");
            configPrint.setTamanho(40);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("ESQUERDA");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Esquerda

            // Impressão Direita
            configPrint.setAlinhamento("RIGHT");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("DIREITA");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Direita

            // Impressão Negrito
            configPrint.setNegrito(true);
            configPrint.setAlinhamento("LEFT");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("=======[Escrita Netrigo]=======");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Negrito

            // Impressão Italico
            configPrint.setNegrito(false);
            configPrint.setItalico(true);
            configPrint.setAlinhamento("LEFT");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("=======[Escrita Italico]=======");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Italico

            // Impressão Italico
            configPrint.setNegrito(false);
            configPrint.setItalico(false);
            configPrint.setSublinhado(true);
            configPrint.setAlinhamento("LEFT");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("======[Escrita Sublinhado]=====");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Italico

            // Impressão BarCode 128
            configPrint.setNegrito(false);
            configPrint.setItalico(false);
            configPrint.setSublinhado(false);
            configPrint.setAlinhamento("CENTER");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("====[Codigo Barras CODE 128]====");
            gertecPrinter.imprimeBarCode("12345678901234567890", 120, 120, "CODE_128");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão BarCode 128

            // Impressão Normal
            configPrint.setNegrito(false);
            configPrint.setItalico(false);
            configPrint.setSublinhado(true);
            configPrint.setAlinhamento("LEFT");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("=======[Escrita Normal]=======");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Normal

            // Impressão Normal
            configPrint.setNegrito(false);
            configPrint.setItalico(false);
            configPrint.setSublinhado(true);
            configPrint.setAlinhamento("LEFT");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("=========[BlankLine 50]=========");
            gertecPrinter.avancaLinha(50);
            gertecPrinter.imprimeTexto("=======[Fim BlankLine 50]=======");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão Normal

            // Impressão BarCode 13
            configPrint.setNegrito(false);
            configPrint.setItalico(false);
            configPrint.setSublinhado(false);
            configPrint.setAlinhamento("CENTER");
            configPrint.setTamanho(20);
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("=====[Codigo Barras EAN13]=====");
            gertecPrinter.imprimeBarCode("7891234567895", 120, 120, "EAN_13");
            gertecPrinter.avancaLinha(10);
            // Fim Impressão BarCode 128

            // Impressão BarCode 13
            gertecPrinter.setConfigImpressao(configPrint);
            gertecPrinter.imprimeTexto("===[Codigo QrCode Gertec LIB]==");
            gertecPrinter.avancaLinha(10);
            gertecPrinter.imprimeBarCode("Gertec Developer Partner LIB", 240, 240, "QR_CODE");

            configPrint.setNegrito(false);
            configPrint.setItalico(false);
            configPrint.setSublinhado(false);
            configPrint.setAlinhamento("CENTER");
            configPrint.setTamanho(20);
            gertecPrinter.imprimeTexto("===[Codigo QrCode Gertec IMG]==");
            gertecPrinter.imprimeBarCodeIMG("Gertec Developer Partner IMG", 240, 240, "QR_CODE");

            gertecPrinter.avancaLinha(100);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
