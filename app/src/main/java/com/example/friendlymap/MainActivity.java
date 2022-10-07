package com.example.friendlymap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "TAG";
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //MAIN - vibrate and voice command saying opening Friendly Maps application

        tts = new TextToSpeech(this, this);
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) vibrator.vibrate(new long[]{0, 500, 200, 500}, -1);
        //

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnectedToInternet()) {
                    startActivity(new Intent(MainActivity.this, recommend.class));
                    finish();
                } else {
                    speakOut("Looks like you're offline, please Connect to the internet!");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Looks like you're offline");
                    builder.setCancelable(false);
                    builder.setMessage("No internet connection");
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isConnectedToInternet()) {
                                startActivity(new Intent(MainActivity.this, recommend.class));
                                finish();
                            } else {
                                //no internet
                                if (vibrator.hasVibrator())
                                    vibrator.vibrate(new long[]{500}, -1);
                                speakOut("closing Friendly Maps!");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 3_000);
                            }

                        }
                    });
                    builder.show();

                }
            }
        }, 5_000); //TESTING
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return mgr.getActiveNetworkInfo() != null && mgr.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onInit(int status) {
        //takes 3s
        if (status == TextToSpeech.SUCCESS) {
            int resultLang = tts.setLanguage(Locale.getDefault());
            if (resultLang == TextToSpeech.LANG_MISSING_DATA ||
                    resultLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                // missing data, install it
                Intent install = new Intent();
                install.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            } else {
                speakOut("opening Friendly Map Application");
            }
        }

    }

    private void speakOut(String s) {
        Log.i(TAG, "speakOut: isSpeaking " + tts.isSpeaking());
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
