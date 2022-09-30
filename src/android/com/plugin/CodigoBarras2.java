package com.plugin;

import android.app.Activity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CodigoBarras2 extends Activity implements ZXingScannerView.ResultHandler {
    private static final String ACESSO_NEGADO = "Permissão negada à câmera";
    private boolean flash = false;
    private ViewGroup contentFrame;
    private ZXingScannerView mScannerView;
    private TextView txtLeitura;
    int StartCameraFlag = 0;
    private int id;
    public static final String G700x = "GPOS700";
    public static final String G800x = "Smart G800";
    private String package_name;
    MediaPlayer mp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("codigo_barras2", "layout", this.package_name));

        id = getResources().getIdentifier("textLeitura","id", this.package_name);
        txtLeitura = (TextView)findViewById(id);

        id = getResources().getIdentifier("content_frame","id", this.package_name);
        contentFrame = (ViewGroup) findViewById(id);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);

        this.mp = MediaPlayer.create(getApplicationContext(), getResources().getIdentifier("beep","raw", this.package_name));
        // this.mpBeep = getResources().getIdentifier("beep","raw", this.package_name);

    }

    @Override
    public void handleResult(Result result) {
        this.mp.seekTo(0);
        this.mp.start();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Código " + result.getBarcodeFormat());
        alertDialog.setMessage(result.getBarcodeFormat() + ": " + result.getText());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startCamera();
                    }
                });
        alertDialog.show();

        txtLeitura.setText(result.getBarcodeFormat() + ": " + result.getText());
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mScannerView!=null)
            mScannerView.stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getApplicationContext(), ACESSO_NEGADO, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    protected void startCamera(){
        contentFrame.setVisibility(View.VISIBLE);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);

        if(Build.MODEL.equals(G700x)){
            if(StartCameraFlag == 0) {
                mScannerView.startCamera();
                StartCameraFlag = 1;
            }else {
                mScannerView.resumeCameraPreview(this);
            }
        }else{
            mScannerView.startCamera();
        }
    }

    protected void stopCamera(){
        mScannerView.stopCamera();
        contentFrame.setVisibility(View.INVISIBLE);
    }
}