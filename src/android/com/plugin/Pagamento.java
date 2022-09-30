package com.plugin;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class Pagamento extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("pagamento", "layout", package_name));
    }

    /*
     * Trecho principal pra ligar o NFC
     * Caso não exista esse código dentro do onStart, a NFC não vai funcionar.
     * */
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }
}
