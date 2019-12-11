package com.communityuni.appdemo_tts_asr;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import android.os.IBinder;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.speech.v1.Speech;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener{
    ImageView imgSpeech;
    EditText edtText;
    //private static ArrayList<String> results;
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private static final String STATE_RESULTS = "results";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private SpeechService mSpeechService;
    private VoiceRecoder mVoiceRecorder;


    private ResultAdapter mAdapter;

    private final VoiceRecoder.Callback mVoiceCallback = new VoiceRecoder.Callback() {
        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
                // định cấu hình cho GoogleAPI
            }
        }
        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                // Call the streaming recognition API
                mSpeechService.recognize(data, size);
                /**
                 * nhận dạng âm thanh lời nói, phương thức này được gọi mỗi khi đoạn
                 * đệm byte được sẵn sàng
                 * data là dữ liệu âm thanh
                 * size là số lượng phần tử có liên quan
                 */
            }
        }
        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
                // kết thúc nhận dạng âm thanh giọng nói
            }
        }

    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            edtText.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            outState.putStringArrayList(STATE_RESULTS, mAdapter.getResults());
        }
    }
    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecoder(mVoiceCallback);
        mVoiceRecorder.start();
    }
    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(hearingVoice==true)
                {
                    Toast.makeText(MainActivity.this,"Listening...",Toast.LENGTH_LONG).show();
                }
                else if(hearingVoice==false)
                {
                    Toast.makeText(MainActivity.this,"Stop listening!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    /**
     * được gọi khi một đoạn văn bản mới được Google API nhận ra
     *
     * @param text    văn bản
     * @param isFinal {@code true} khi Google API xử lý xong âm thanh .
     */
    private final SpeechService.Listener mSpeechServiceListener = new SpeechService.Listener() {
        @Override
        public void onSpeechRecognized(final String text, final boolean isFinal) {
            if (isFinal) {
                mVoiceRecorder.dismiss();
            }
            if (edtText != null && !TextUtils.isEmpty(text)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinal) {
                            edtText.setText(null);
                            mAdapter.addResult(text);
                        } else {
                            edtText.setText(text);
                        }
                    }
                });
            }
        }
    };
    // để nguyên lớp này
    private static class ResultAdapter {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }
        void addResult(String result) {
            mResults.add(0, result);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

    }


    public Intent intent= new Intent(MainActivity.this,SpeechService.class);
    private void addEvents() {
        imgSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speak();
            }
        });
    }

    ArrayList<String> results;
    void Speak()
    {

        try {

            Toast.makeText(MainActivity.this,"Listening...",Toast.LENGTH_LONG).show();
            // Prepare Cloud Speech API
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            Toast.makeText(MainActivity.this,"đã kết nối",Toast.LENGTH_SHORT).show();
            // Start listening to voices
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
                Toast.makeText(MainActivity.this,"Bắt đầu nghe",Toast.LENGTH_SHORT).show();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                showPermissionMessageDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            }

            Toast.makeText(MainActivity.this,"Stop listening!",Toast.LENGTH_LONG).show();
            // Stop listening to voice
            stopVoiceRecorder();
            Toast.makeText(MainActivity.this,"dừng nghe",Toast.LENGTH_SHORT).show();
            if (mAdapter != null) {
                intent.putExtra(STATE_RESULTS,mAdapter.getResults());
                intent.putStringArrayListExtra(STATE_RESULTS, mAdapter.getResults());
            }
            else
            {
                results = intent==null ? null: intent.getStringArrayListExtra(STATE_RESULTS);
                mAdapter= new ResultAdapter(results);
                mAdapter.addResult(results.toString());

            }


            Toast.makeText(MainActivity.this,"Lấy được results: "+results,Toast.LENGTH_SHORT).show();



            edtText.setText(mAdapter.getResults().toString());
            // Stop Cloud Speech API
            mSpeechService.removeListener(mSpeechServiceListener);
            unbindService(mServiceConnection);
            mSpeechService = null;
            Toast.makeText(MainActivity.this,"Hủy kết nối",Toast.LENGTH_SHORT).show();

        }
        catch (Exception ex)
        {
            //Toast.makeText(MainActivity.this,"Error!",Toast.LENGTH_LONG).show();
        }



    }
    private void addControls() {
        imgSpeech= findViewById(R.id.imgSpeech);
        edtText= findViewById(R.id.edtText);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        addEvents();
    }
}
