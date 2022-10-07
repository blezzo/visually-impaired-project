package com.example.friendlymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class recommend extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);


       //check if application was opened for the first time
       SharedPreferences preferences=getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String FirstTime=preferences.getString("FirstTimeInstall", "");

        if(FirstTime.equals("Yes")){
            Intent i=new Intent(recommend.this, Map.class);
            startActivity(i);

        }else {


            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("FirstTimeInstall", "Yes");
            editor.apply();
        }
    }
    public void skipclick (View view){
        Intent i= new Intent(recommend.this, Map.class);
        startActivity(i);

    }

    public void proceedclick(View view){
        Intent k= new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=news.androidtv.launchonboot"));
        startActivity(k);

    }
}
