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


    private static TextView text;
    private int id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("pagamento", "layout", package_name));
    }
}
