package com.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.verdemar.pdvmovel.R;

import java.util.StringTokenizer;

public class Itens extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itens);
        String t = getIntent().getStringExtra("mensagem");
        int i = getIntent().getIntExtra("request",0);
        String title = getIntent().getStringExtra("title");
        TextView textViewTitle = (TextView) findViewById(R.id.textView3);
        textViewTitle.setText(title);

        ListView lv = (ListView) findViewById(R.id.lv);
        ArrayAdapter<String> itensMenu = new ArrayAdapter<String>(this, R.layout.menu_item);
        lv.setAdapter(itensMenu);
        lv.setOnItemClickListener(itemMenuClickListener);

        String itens = getIntent().getExtras().getString("message").toString();
        StringTokenizer st = new StringTokenizer(itens, ";", false);
        while (st.hasMoreTokens()) {
            itensMenu.add(st.nextToken());
        }

    }
    public void cancelar(View v){
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }
    private AdapterView.OnItemClickListener itemMenuClickListener = new AdapterView.OnItemClickListener() {
        // Click do item de menu
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            Intent i = new Intent();
            String item = ((TextView) v).getText().toString();
            StringTokenizer st = new StringTokenizer(item, ":", false);

            i.putExtra("input", st.nextToken());
            setResult(RESULT_OK, i);
            finish();
        }
    };
}