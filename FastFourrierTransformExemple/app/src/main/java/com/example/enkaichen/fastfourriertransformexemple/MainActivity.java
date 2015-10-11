package com.example.enkaichen.fastfourriertransformexemple;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.app.RemoteInput;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import org.jtransforms.fft.DoubleFFT_1D;
//import org.jtransforms.*;
//import org.jtransforms.fft.DoubleFFT_1D;


public class MainActivity extends Activity implements View.OnClickListener {

    public static RecordAudio recordTask;
    public static PlayAudio playTask;
    public static Button startRecordingButton, stopRecordingButton, startPlaybackButton,stopPlaybackButton, sendButton, DoneButton;
    public static TextView statusText, Sentence;
    public static File recordingFile;
    public static ImageView imageView;
    public static Bitmap bitmap;
    public static Canvas canvas;
    public static Paint paint;
    public static File path;
    public static String ACTION_PLAY_BACK = "Play back";
    public static String ACTION_RECORD = "record";
    public static int numberOfTimePressedRecording = 1;
    public static int numberOfTimePressedPlayBack = 1;
    public static short[] buffer;
    public static String[] Final;
    public static String Finalsentence = "";

    public static double[] toTransform2;

    public static boolean isRecording = false,isPlaying = false;

    public static int frequency = 11025,channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    public static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static final String ACTION_DEMAND = "com.androidweardocs.ACTION_DEMAND";
    public static final String EXTRA_MESSAGE = "com.androidweardocs.EXTRA_MESSAGE";
    public static final String EXTRA_VOICE_REPLY = "com.androidweardocs.EXTRA_VOICE_REPLY";
    public static double[] toTransform;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) this.findViewById(R.id.StatusTextView);

        Sentence = (TextView) this.findViewById(R.id.textViewWhatHasBeenSaidInitial);

        startRecordingButton = (Button) this
                .findViewById(R.id.StartRecordingButton);
        stopRecordingButton = (Button) this
                .findViewById(R.id.StopRecordingButton);
        startPlaybackButton = (Button) this
                .findViewById(R.id.StartPlaybackButton);
        stopPlaybackButton = (Button) this
                .findViewById(R.id.StopPlaybackButton);
        sendButton = (Button)this.findViewById(R.id.SendNotification);

        DoneButton = (Button)this.findViewById(R.id.Donebutton);

        startRecordingButton.setOnClickListener(this);
        stopRecordingButton.setOnClickListener(this);
        startPlaybackButton.setOnClickListener(this);
        stopPlaybackButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        DoneButton.setOnClickListener(this);

        stopRecordingButton.setEnabled(false);

        stopPlaybackButton.setEnabled(false);

        path = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/");
        path.mkdirs();

        if(path.getUsableSpace() == 0){
            startPlaybackButton.setEnabled(false);
            try {
                recordingFile = File.createTempFile("recording", ".pcm", path);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create file on SD card", e);
            }
        }
        else{
            recordingFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/recording.pcm");//recording.pcm
            startPlaybackButton.setEnabled(true);
        }



        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int) 256, (int) 100,
                Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);

        Random r = new Random();
        int Low = 3;
        int High = 4;
        int R = r.nextInt(High-Low) + Low;
        Final = generateRandomWords(R);
         for(int i = 0; i < R; i++) {
            Finalsentence += Final[i] + " ";//Final[0] + " " + Final[1] + " "+ Final[2]+ " "+ Final[3];
        }
        Sentence.setText(Finalsentence);



    }

    public static String[] generateRandomWords(int numberOfWords)// changed this to String it was String[]
    {

        char[] vowels = {'a','e','i','o','u'};
        char[] consonants = {'q','w','r','t','p','s','d','f','g','h','j','k','l','z','x','c','v','b','n','m'};
       String[] randomwords = {"Peter Piper picked a peck of pickled peppers.", "thimble blumble carmel. Candycorn unicorn corn", "Red lorry, yellow lorry, red lorry, yellow lorry.", "I love to go shopping and drink soda. Robots are my life.", "coding is fun espacially when you are creating great things. Pecked a pickled pepper.", "horses cowboys and cows what goes better together? Well sun and light, roses and love.", "doy dewy loy coy poy. lala la lala la ladi da ladi da.", "She sells sea-shells on the sea-shore. The shells she sells are sea-shells"};
        int previousR = -1;
        String[] randomStrings = new String[numberOfWords];
        Random random = new Random();
        int low = 0;
        int high = randomwords.length;
        for(int i = 0; i < numberOfWords; i++) {
            int R = random.nextInt(high - low) + low;
            if(R == previousR){
                R = random.nextInt(high - low) + low;
            }
            randomStrings[i] = randomwords[R];
            previousR = R;
        }

       /* Random random = new Random();
        for(int i = 0; i < numberOfWords; i++)
        {
            char[] word = new char[random.nextInt(6)+4]; // words of length 3 through 10. (1 and 2 letter words are boring.)
            for(int j = 1; j < word.length; j+=2) {

                Random r = new Random();
                int Low = 0;
                int High = vowels.length;
                int R = r.nextInt(High - Low) + Low;

                word[j] = vowels[R];//(char)('a' + random.nextInt(26));
            }
            for(int j = 0; j < word.length; j+=2) {
                Random rc = new Random();
                int Lowc = 0;
                int Highc = consonants.length;
                int Rc = rc.nextInt(Highc - Lowc) + Lowc;

                word[j] = consonants[Rc];
            }


            randomStrings[i] = new String(word);
        }*/
        return randomStrings;

    }



    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say something");
        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech to text recognition is not supported on your phone",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                }
                break;
            }

        }
    }

    public void onClick(View v) {
        if (v == startRecordingButton) {
            record();
        } else if (v == stopRecordingButton) {
            stopRecording();
        } else if (v == startPlaybackButton) {
            play();
        } else if (v == stopPlaybackButton) {
            stopPlaying();
        } else if(v == sendButton){
            // if(sendButton.getText()=="Send Notification") {
            SendNotification();
            // sendButton.setText("Use phone");
            //  }
            // if(sendButton.getText() == "Use phone"){
            //SendNotificationPhone();
            //     sendButton.setText("Send Notification");
            //  }
        } else if(v == DoneButton){
            Intent intent = new Intent(this, SecondRecording.class);
            startActivity(intent);
            finish();
        }
    }

    public void SendNotificationPhone() {
    /*    final Intent removeIntent = new Intent( ACTION_PLAY_BACK, null, this, WearablePlayBackAudio.class );
        final PendingIntent pendingRemoveIntent = PendingIntent.getActivity(this, 0, removeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Intent removeIntent2 = new Intent( ACTION_RECORD, null, this, returnToPhone.class );
        final PendingIntent pendingRemoveIntent2 = PendingIntent.getActivity( this, 0, removeIntent2, PendingIntent.FLAG_UPDATE_CURRENT );
        Intent demandIntent = new Intent(this, WearableAudio.class)
                .putExtra(EXTRA_MESSAGE, "Reply selected.")
                .setAction(ACTION_DEMAND);
        final PendingIntent demandPendingIntent =
                PendingIntent.getActivity(this, 0, demandIntent, 0);
        String replyLabel = "How many carrots?";
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();
        final NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                        getString(R.string.reply_label), demandPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        removeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( MainActivity.this )
                .setSmallIcon( R.drawable.ic_launcher )
                .setContentTitle("Your Wearable Recorder")
                .setContentText("Use the first button to record and the second to play back")
                .setContentIntent(pendingRemoveIntent)
                .setVibrate(new long[]{0, 200, 100, 200})
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .addAction(R.drawable.ic_launcher, "Press to record",pendingRemoveIntent2);
        //.addAction(R.drawable.ic_launcher, "Press to play back", pendingRemoveIntent)
        //.addAction(replyAction);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(001, notificationBuilder.build());
        finish();*/
    }

    public void play() {
        startPlaybackButton.setEnabled(false);
        startRecordingButton.setEnabled(false);
        playTask = new PlayAudio();
        playTask.execute();

        stopPlaybackButton.setEnabled(true);

    }

    public void stopPlaying() {
        isPlaying = false;
        playTask.cancel(true);
        stopPlaybackButton.setEnabled(false);
        startRecordingButton.setEnabled(true);
        startPlaybackButton.setEnabled(true);
    }

    public void record() {
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
        startPlaybackButton.setEnabled(false);
        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void stopRecording() {
        recordTask.cancel(true);
        startRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);
        startPlaybackButton.setEnabled(true);
        isRecording = false;
    }

    public void SendNotification(){

        notification();
    }


    public void notification(){
    /*    final Intent removeIntent = new Intent( ACTION_PLAY_BACK, null, this, WearablePlayBackAudio.class );
        final PendingIntent pendingRemoveIntent = PendingIntent.getActivity(this, 0, removeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Intent removeIntent2 = new Intent( ACTION_RECORD, null, this, WearableAudio.class );
        final PendingIntent pendingRemoveIntent2 = PendingIntent.getActivity( this, 0, removeIntent2, PendingIntent.FLAG_UPDATE_CURRENT );
        Intent demandIntent = new Intent(this, WearableAudio.class)
                .putExtra(EXTRA_MESSAGE, "Reply selected.")
                .setAction(ACTION_DEMAND);
        final PendingIntent demandPendingIntent =
                PendingIntent.getActivity(this, 0, demandIntent, 0);
        String replyLabel = "How many carrots?";
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();
        final NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                        getString(R.string.reply_label), demandPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        removeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( MainActivity.this )
                .setSmallIcon( R.drawable.ic_launcher )
                .setContentTitle("Your Wearable Recorder")
                .setContentText("Use the first button to record and the second to play back")
                .setContentIntent(pendingRemoveIntent)
                .setVibrate(new long[]{0, 200, 100, 200})
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .addAction(R.drawable.ic_launcher, "Press to record",pendingRemoveIntent2)
                .addAction(R.drawable.ic_launcher, "Press to play back", pendingRemoveIntent)
                .addAction(replyAction);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(001, notificationBuilder.build());
        finish();*/



    }

    public static double MSE() {
        double sum_sq = 0;
        double mse = 0.0;

      /*  for (int i = 0; i < toTransform.length && i < MainActivity.toTransform.length; ++i)
        {
             double p1 = MainActivity.toTransform[i];
             double p2 = toTransform[i];
             double err = p2 - p1;
             sum_sq += (err* err);

        }*/

        for (int i = 0; i < toTransform.length; ++i) {
            double p1 = toTransform[i];
            //double p2 = toTransform[i];
            double err = p1;
            sum_sq += (err * err);

        }
        mse = Math.sqrt((sum_sq) / (MainActivity.toTransform.length));
        return mse;
    }

    public static class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;

            int bufferSize = AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);
            short[] audiodata = new short[bufferSize / 4];

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < audiodata.length) {
                        audiodata[i] = dis.readShort();
                        i++;
                    }
                    audioTrack.write(audiodata, 0, audiodata.length);
                }
                dis.close();
                startPlaybackButton.setEnabled(false);
                stopPlaybackButton.setEnabled(true);
            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }
            return null;
        }
    }


    public static class RecordAudio extends AsyncTask<Void, double[], Void> {
        //double[] toTransform;
        doFFT FFT;// = new doFFT();
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                recordingFile)));
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.VOICE_RECOGNITION, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                buffer = new short[bufferSize];
                toTransform = new double[bufferSize];
                double[] a = new double[bufferSize];
                audioRecord.startRecording();
                int r = 0;//this is a comment for the first commit
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i]/500.0;// /500.0;
                        a[i] = toTransform[i];
                        dos.writeShort(buffer[i]);
                    }
                    //publishProgress(new Integer(r));

                    FFT = new doFFT();
                    FFT.settoTransform2(toTransform);
                    FFT.execute();
                    publishProgress(toTransform);
                    FFT.cancel(true);
                    r++;
                }
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(double[]... toTransform) {

            canvas.drawColor(Color.BLACK);

            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;
                int downy = (int) (100 - (toTransform[0][i] * 10));
                int upy = 100;

                canvas.drawLine(x, downy, x, upy, paint);
            }

            imageView.invalidate();

            // TODO Auto-generated method stub
            // super.onProgressUpdate(values);
        }
        protected void onPostExecute(Void result) {
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
            startPlaybackButton.setEnabled(true);
        }
    }

    public static class doFFT extends AsyncTask<Void, double[], Void>{


        DoubleFFT_1D fftDo;// = new DoubleFFT_1D(toTransform2.length);
        void settoTransform2(double[] toTransform3){
            //this.toTransform2 = toTransform3;
            toTransform2 = new double[toTransform3.length * 2];
            System.arraycopy(toTransform3, 0, toTransform2, 0, toTransform3.length);
            fftDo = new DoubleFFT_1D(toTransform2.length);
        }

        @Override
        protected Void doInBackground(Void... params) {
        //    transformer.ft(toTransform2);
            fftDo.realForwardFull(toTransform2);
            return null;
        }
    }
}
