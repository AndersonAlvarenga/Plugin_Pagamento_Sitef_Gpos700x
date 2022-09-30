package com.plugin;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class NFCId extends Activity {
    private static final String TAG = NFCId.class.getSimpleName();
    private NfcAdapter nfcAdapter;
    private MifareClassic mifareClassic;
    private IsoDep isoDep;
    private Tag tag;
    private TextView text;
    private static int Contagem = 0;
    private int id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("nfcid", "layout", package_name));
        id = getResources().getIdentifier("textIdCartao","id",package_name);
        text = (TextView)findViewById(id);
    }

    /*
    * Trecho principal pra ligar o NFC
    * Caso não exista esse código dentro do onStart, a NFC não vai funcionar.
    * */
    @Override
    protected void onStart() {
        super.onStart();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    /*
     * Filtrar todas as tags que queremos define as tags de cartao, passa pro ident filter
     *
     * */
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter idDetected = new IntentFilter((NfcAdapter.EXTRA_ID));
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected, idDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Deixa o sensor da NFC em alerta.
        if(nfcAdapter!= null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    /*
     * Todo os retornos da NFC devem ser tratados nesse método.
     * Aqui deve ser feito todos os tratamentos para converter do id do cartão de Byte para Int ou String
     *
     * */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag == null){
                Toast.makeText(getApplicationContext(), "Não foi possível ler o cartão.", Toast.LENGTH_SHORT).show();
            }else{
                LerCartaoNfc();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Faz a leitura do ID do cartão
    protected void LerCartaoNfc(){
        mifareClassic = MifareClassic.get(tag);
        if(mifareClassic == null){
            isoDep = IsoDep.get(tag);
        }
        Contagem += 1;
        text.setText("Leitura: " + Contagem + "\nId do Cartão: " + idCartao());
    }

    // Converte o ID do cartão que esta em Bytes para String
    public String idCartao() {

        byte[] idCartao = null;
        long result = 0;

        if(mifareClassic != null){
            idCartao = mifareClassic.getTag().getId();
        }else if (isoDep != null){
            idCartao = isoDep.getTag().getId();
        }

        if (idCartao == null) return "";

        for (int i = idCartao.length - 1; i >= 0; --i) {
            result <<= 8;
            result |= idCartao[i] & 0x0FF;
        }

        Log.d(TAG, "ID Cartão INT: " + Long.toString(result) + " (é o result convertido)");
        Log.d(TAG, "ID Cartão HEX: " + bytesToHex(idCartao) + " (é o idCartao convertido)");

        return Long.toString(result);
        
    }

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
