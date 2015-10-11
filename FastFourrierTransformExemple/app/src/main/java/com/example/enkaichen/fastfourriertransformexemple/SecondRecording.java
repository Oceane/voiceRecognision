package com.example.enkaichen.fastfourriertransformexemple;

/**
 * Created by Enkaichen on 8/23/15.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.sf.javaml.distance.dtw.DTWSimilarity;
import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.AbstractInstance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.distance.fastdtw.dtw.FastDTW;

import org.oc.ocvolume.dsp.featureExtraction;
import jAudioFeatureExtractor.jAudioTools.AudioSamples;
import cern.colt.matrix.doublealgo.Statistic;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.apache.commons.math3.stat.correlation.*;


/*import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.*;
import com.musicg.graphic.GraphicRender;
import com.musicg.wave.extension.Spectrogram;*/


/**
 * Created by Enkaichen on 5/17/15.
 */
public class SecondRecording extends Activity{
    public static RecordAudio recordTask;
    public static PlayAudio playTask;
    public static Button startRecordingButton, stopRecordingButton, startPlaybackButton,stopPlaybackButton, okButton;
    public static TextView statusText, percent, SentenceSaidByUser;
    public static File recordingFile;
    public static ImageView imageView;
    public static Bitmap bitmap;
    public static Canvas canvas;
    public static Paint paint;
    public static File path;
    //public static RealDoubleFFT transformer;
    public double[] sizeDD;
    public static String ACTION_PLAY_BACK = "Play back";
    public static String ACTION_RECORD = "record";
    public short[] buffer;
    double sum_sq = 0;
    double[]Array1;
    double[]Array2;
    public static double[] toTransform2;
    public static double[] toTransformMelArrayInitial;
    public static double[] toTransformMelSecondArray;
    double sma2llest = 0;
    double size2 = 0;
    public static boolean isRecording = false,isPlaying = false;

    public static int frequency = 11025,channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    public static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static final String ACTION_DEMAND = "com.androidweardocs.ACTION_DEMAND";
    public static final String EXTRA_MESSAGE = "com.androidweardocs.EXTRA_MESSAGE";
    public static final String EXTRA_VOICE_REPLY = "com.androidweardocs.EXTRA_VOICE_REPLY";
    public static double[] toTransform;
    public static int percentage = 0;
    public double MSEInt = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_recording);
        statusText = (TextView) this.findViewById(R.id.StatusTextView);
        SentenceSaidByUser = (TextView) this.findViewById(R.id.textViewWhatHasBeenSaid);
        SentenceSaidByUser.setText(MainActivity.Finalsentence);
        percent = (TextView) this.findViewById(R.id.textViewPercentage);
        percent.setText("% of similarity: -");

        startRecordingButton = (Button) this
                .findViewById(R.id.StartRecordingButton2);
        stopRecordingButton = (Button) this
                .findViewById(R.id.StopRecordingButton2);
        startPlaybackButton = (Button) this
                .findViewById(R.id.StartPlaybackButton2);
        stopPlaybackButton = (Button) this
                .findViewById(R.id.StopPlaybackButton2);
        okButton = (Button)this.findViewById(R.id.OKbutton2);
        okButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                compare();
            }
        });
        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record();
            }
        });
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                //compare();
            }
        });
        startPlaybackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        stopPlaybackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying();
            }
        });

        path = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/");
        path.mkdirs();

        if(path.getUsableSpace() == 0){
            startPlaybackButton.setEnabled(false);
            try {
                recordingFile = File.createTempFile("recordingSecond", ".pcm", path);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create file on SD card", e);
            }
        }
        else{
            recordingFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/recordingSecond.pcm");
            startPlaybackButton.setEnabled(true);
        }
        //transformer = new RealDoubleFFT(1000);//256);

        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int) 256, (int) 100,
                Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);
        System.out.println("IIIIIINNNNN");
    }

  /*  @Override
    public void onClick(View v) {
        if (v == startRecordingButton) {
            record();
        } else if (v == stopRecordingButton) {
            stopRecording();
        } else if (v == startPlaybackButton) {
            play();
        } else if (v == stopPlaybackButton) {
            stopPlaying();
        }
    }*/


    public void play() {
        startPlaybackButton.setEnabled(false);
        startRecordingButton.setEnabled(false);
        playTask = new PlayAudio();
        playTask.execute();

        stopPlaybackButton.setEnabled(true);
        //compare();

    }

    public void stopPlaying() {
        isPlaying = false;
        playTask.cancel(true);
        stopPlaybackButton.setEnabled(false);
        startRecordingButton.setEnabled(true);
        startPlaybackButton.setEnabled(true);
    }

    public void record() {
        //percentage = 0;
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
        startPlaybackButton.setEnabled(false);
        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void stopRecording() {
        recordTask.cancel(true);
        //compare();
        startRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);
        startPlaybackButton.setEnabled(true);
        isRecording = false;
    }

    public void compare() {
        double totalPercentage = 0;
        double percentageCurrent = 0;
        double initial = MainActivity.toTransform2[0] - toTransform2[0];
        double r = 0;
        double a = MainActivity.toTransform2[0];
        double b = toTransform2[0];

        Array1 = new double[MainActivity.toTransform2.length]; //Normalize the first recording and the second recording by ai/max ai
        Array2 = new double[toTransform2.length];


        for (int i = 0; i < MainActivity.toTransform2.length; i++) {

            if (MainActivity.toTransform2[i] >= a) {
                a = MainActivity.toTransform2[i];
            }
        }
        for (int i = 0; i < MainActivity.toTransform2.length; i++) {
            if (a != 0) {
                MainActivity.toTransform2[i] = MainActivity.toTransform2[i] / a;
            }
        }
        for (int i = 0; i < toTransform2.length; i++) {
            if (toTransform2[i] >= b) {
                b = toTransform2[i];
            }
        }

        for (int i = 0; i < toTransform2.length; i++) {
            if (b != 0) {
                toTransform2[i] = toTransform2[i] / b;
            }
        }
        //adding this
        toTransformMelArrayInitial = MEL(MainActivity.toTransform2, MainActivity.buffer);
        toTransformMelSecondArray = MEL(toTransform2, buffer);
        //end

        //changing all the MainActivity.toTransform2 by toTransformMelArrayInitial and toTransform2 by toTransformMelSecondArray
        //starting from here

        double MESBLAH = MSE();
        // commented this out
        /*if(MESBLAH >= MainActivity.MSE()-0.02 && MESBLAH <= MainActivity.MSE()+0.02){//100){
            MESBLAH = MESBLAH*5;
        }*/
        //end
        /*else if(MESBLAH>0.9){
            MESBLAH = MESBLAH/5;
        }*/

       /* for(int i = 0; i < MainActivity.toTransform.length-1; i++){


            double LowerLim = MainActivity.toTransform[i] - MESBLAH;
            double upperLim = MainActivity.toTransform[i] + MESBLAH;

            if(LowerLim<=toTransform[i] && upperLim>=toTransform[i]){
                percentageCurrent++;
            }

            r++;
        }*/
        for (int i = 0; i < toTransformMelArrayInitial.length; i++) {


            double LowerLim = toTransformMelArrayInitial[i] - MESBLAH;//1
            double upperLim = toTransformMelArrayInitial[i] + MESBLAH;//1

            if (LowerLim <= toTransformMelSecondArray[i] && upperLim >= toTransformMelSecondArray[i]) {
                percentageCurrent++;
            }

            r++;
        }

       /* for(int i = 0; i < Array1.length && i < Array2.length; i++){


            double LowerLim = Array1[i] - MESBLAH;
            double upperLim = Array1[i] + MESBLAH;

            if(LowerLim<=Array2[i] && upperLim>=Array2[i]){
                percentageCurrent++;
            }

            r++;
        }*/


        for (int i = 0; i < toTransformMelArrayInitial.length; i++) {
            //if(MainActivity.toTransform[i] != 0) {
            System.out.println("Main activity recording " + toTransformMelArrayInitial[i]);
            // writeToFile(Double.toString(MainActivity.toTransform[i]));
            //}
        }
        // writeToFile("/////////////////////////////////////////////////////////////////////");
        for (int i = 0; i < toTransformMelSecondArray.length; i++) {


            // if(toTransform[i] != 0) {
            System.out.println("Second activity recording " + toTransformMelSecondArray[i]);
            //writeToFile(Double.toString(toTransform[i]));
            //}
        }
        sizeDD = getDistance(toTransformMelArrayInitial, toTransformMelSecondArray);
        for (int i = 0; i < sizeDD.length; i++) {
            System.out.println("This is what is sizeDD: " + sizeDD[i]);
        }

        double[] blah = get2SmallestNumber(sizeDD);

        if (sma2llest != 0) {
            sma2llest = 0;
        }

        for (int i = 0; i < blah.length; i++) {
            sma2llest += blah[i];
        }
        double dist = (sma2llest / sizeDD.length);//*Math.pow(10.0,9.0);

        System.out.println("The distance between the two audio files is: " + dist);
        double correlation = crossCorrelation(toTransformMelArrayInitial, toTransformMelSecondArray);
        //crossCorrelation( Array1,Array2);
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double corr = pc.correlation(MainActivity.toTransform2, toTransform2);
        double corr2 = pc.correlation(toTransformMelArrayInitial, toTransformMelSecondArray);
        double average = (corr + corr2) / 2;
        System.out.println("corr = " + average);
        System.out.println("sum_sq = " + sum_sq);
        System.out.println("size = " + toTransformMelArrayInitial.length);
        System.out.println("MSE = " + MSE());
        System.out.println("MESBLAH = " + MESBLAH);
        System.out.println("percentage = " + percentageCurrent);
        System.out.println("r = " + r);
        int percentTemp = (int) (percentageCurrent / r * 100);
        int Onehalf = percentTemp / 2;
        int OneThird = percentTemp / 3;
        //MainActivity.toTransform2, toTransform2);
        //if(average <= 0.33){
        //    percentTemp = percentTemp-percentTemp;
        //}

        if (average <= 0.45 && dist > 2 * Math.pow(10, -17)) {
            percentTemp = -OneThird;
        } else if (average >= 0.6 && dist < 1 * Math.pow(10, -17) && percentTemp <= 80 && percentTemp >= 68) {
            percentTemp = percentTemp + 20;
        }
        if (size2 > 0.33) {
            percentTemp = percentTemp - Onehalf;
            //add this else if
        }else if(size2 >0.25 && percentTemp<80){
            percentTemp = percentTemp - 20;
            //end
        }else if (size2<0.14 && percentTemp <= 80 && percentTemp>=68){// added && percentTemp>=68
            percentTemp = percentTemp+20;
        } else if (size2<0.14 && percentTemp > 80){
            percentTemp = 100;
        }

        if(average <=0.5985 && dist > 1.282*Math.pow(10,-17) && size2>0.32){//change from 0.33 to 0.32
            percentTemp = percentTemp-percentTemp;
        }
        if (percentTemp < 68) {
            percentTemp = percentTemp - 20;
        } else if (percentTemp >= 80) {
            percentTemp = 100;
        }
        if(percentTemp <0){
            percentTemp = 0;
        }
        /*else if(crossCorrelation( toTransform, MainActivity.toTransform)>0.3 && percentTemp<=90){
            percentTemp = percentTemp+10;
        }*/
        System.out.println(percentTemp + "% of similarity");
        // System.out.println(correlation);
        percent.setText("% of similarity: "+percentTemp+ "%");//totalPercentage
        //end
    }

    public double MSE(){
        sum_sq = 0;
        double mse = 0.0;

       /* for (int i = 0;  i < toTransform2.length; ++i)
        {
            double p1 = toTransform2[i];
            double err = p1;
            sum_sq += (err* err);

        }*/

        for (int i = 0;  i < toTransformMelArrayInitial.length; ++i)
        {
            double p1 = toTransformMelArrayInitial[i];
            double err = p1;
            sum_sq += (err* err);

        }
        //toTransformMelSecondArray


       /* for (int i = 0; i < Array2.length && i < Array1.length; ++i)
        {
            double p1 = Array1[i];
            double p2 = Array2[i];
            double err = p2 - p1;
            sum_sq += (err* err);

        }*/
        //sum_sq = sum_sq/500;
        mse = Math.sqrt((sum_sq)) / (toTransformMelArrayInitial.length*10);

        return mse;
    }

    /*
    MFCCs are commonly derived as follows:

    1) Take the Fourier transform of (a windowed excerpt of) a signal.
    2) Map the powers of the spectrum obtained above onto the mel scale, using triangular overlapping windows.
    3) Take the logs of the powers at each of the mel frequencies.
    4) Take the discrete cosine transform of the list of mel log powers, as if it were a signal.
    5) The MFCCs are the amplitudes of the resulting spectrum.
     */

    public double[] MEL(double[]xM, short[] ym){
        double [][] TempArray = new double[512][13];
        /*double[] MelTransformedArray = new double[xM.length];

        //frequency to MFC
        for(int i = 0; i< xM.length; i++){
            MelTransformedArray[i] = 2595 * Math.log10(1 + (xM[i] / 700));
            //MelTransformedArray[i] = Math.log( MelTransformedArray[i]);
        }
        //MFC to log(MFC)
        for(int i = 0; i < xM.length; i++){
            if(!(MelTransformedArray[i] == 0.0)) {
                MelTransformedArray[i] = Math.log(Math.abs(MelTransformedArray[i]));
            }

        }*/
        int counter = 0;
        featureExtraction fe = new featureExtraction();
        // using the non linear version of the array
        //double[] initialTransform = fe.nonLinearTransformation(xM);
        //taking the ceptrum

        //for(int i = 0; i < ceptrum.length;i++){
            //System.out.println("coefficient "+ i + " is equal to " + ceptrum[i]);
        //}
        double[][]ceptrum2 = fe.process(ym, 2048);
        double[] ceptrum = new double[ceptrum2.length * 13];//fe.cepCoefficients(xM);
        for (int i = 0; i < ceptrum2.length; i++){
            for(int k = 0; k < 13; k++){
                ceptrum[counter] = ceptrum2[i][k];
                counter ++;
               // System.out.println("Counter = "+ counter );
            }
        }

        //log(MFC) to MFCC
        /*for(int i = 0; i < xM.length; i++){
            if(!(MelTransformedArray[i] == 0.0)) {
                for (int j = 0; j < xM.length; j++) {
                    MelTransformedArray[i] += MelTransformedArray[j] * Math.cos((Math.PI / xM.length) * (j + 1 / 2) * i);
                }
            }
        }*/
        return ceptrum;
        //return MelTransformedArray;
    }

  /*  public void vqlbg(double[] [] d,int k) {

        double e = .01;
        double[] r = mean(d, 2);
        int dpr = 10000;

        for (int i = 0; i<Math.log(k); i++) {
            r =[r * (1 + e), r * (1 - e)];
        }

        while (1 == 1) {
            int z = disteu(d, r);
            [m, ind]=min(z,[],2);
            t = 0;
            for j = 1:2 ^ i
            r(:,j)=mean(d(:,find(ind == j)),2);
            x = disteu(d(:,find(ind == j)),r(:,j));
            for q = 1:length(x)
            t = t + x(q);
            end
                    end
            if (((dpr - t) / t) < e) {
                break;
            }
            else {
                dpr = t;
            }
        }

    }*/

    private double[] getDistance(double[] d1, double[] d2)
    {
       // return Math.sqrt((d1-d2)* (d1-d2));



        double[] pesi = new double[13];
        //double[][]ddsize = new double[512][13];
        pesi[0] = 0.20;
        pesi[1] = 0.90;
        pesi[2] = 0.95;
        pesi[3] = 0.90;
        pesi[4] = 0.70;
        pesi[5] = 0.90;
        pesi[6] = 1.00;
        pesi[7] = 1.00;
        pesi[8] = 1.00;
        pesi[9] = 0.95;
        pesi[10] = 0.30;
        pesi[11] = 0.30;
        pesi[12] = 0.30;
        int location = 0;
        double maxSize = d1.length;
        if(maxSize<d2.length){
            maxSize = d2.length;
        }


        double[] size = new double[(int)maxSize];
        //double size2 = 0;
        DTWSimilarity dtwe = new DTWSimilarity();
        Instance tmpInstance1 = new SparseInstance();
        Instance tmpInstance2 = new SparseInstance();
        for(int i = 0; i < d1.length; i++){
            tmpInstance1.put(i,d1[i]);
        }
        for(int i = 0; i < d2.length; i++){
            tmpInstance2.put(i,d2[i]);
        }
        size2 = dtwe.measure(tmpInstance1,tmpInstance2);
        System.out.println("This is what size2 is equal to: " + size2);
        //FastDTW fdtwe = new FastDTW();

        //tmpInstance.put(4,1.0);
        //System.out.println("This is what is in the tmpInstance: "+tmpInstance.value(4));


        for(int i = 0; i < maxSize; i++){
            //System.out.println("Pesi at "+ i+ " is "+pesi[location]);
            if((d1[i] - d2[i]) > 0) {
                size[i] = Math.sqrt(Math.pow((d1[i] - d2[i]),2));//pesi[location]));
            }else if((d1[i] - d2[i]) < 0){
                size[i] = Math.sqrt(Math.pow((d2[i] - d1[i]),2));// pesi[location]));
            }
            else{
                size[i] = 0.0;
            }
            location ++;
            if(location == 13){
                location = 0;
            }
        }
        return size;
    }

    private double[] get2SmallestNumber(double[]v){
        double[] lowestValues = new double[2];
        Arrays.fill(lowestValues, Integer.MAX_VALUE);

        for(double n : v) {
            if(n < lowestValues[1] && n >0) {
                lowestValues[1] = n;
                Arrays.sort(lowestValues);
            }
        }
        return lowestValues;
    }

    public double crossCorrelation(double[] x, double[] y){

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;
        double x_min_mean = 0.0;
        double y_min_mean = 0.0;
        double sum = 0.0;


        int n = x.length;

        if(n<y.length){
            n = y.length;
        }

        for(int i = 0; i < n; ++i) {
            double xs = x[i];
            double ys = y[i];

            sx += xs;
            sy += ys;
            sxx += xs * xs;
            syy += ys * ys;
            sxy += xs * ys;
        }

        //meanX
        double meanX = sx/n;
        //meanY
        double meanY = sy/n;
        for(int k = 0; k<n; k++){
            x_min_mean = x[k]-meanX;
            y_min_mean = y[k]-meanY;
            double inter = x_min_mean*y_min_mean;// change it back to addition if it does not work
            sum += inter;
        }
        double a = 0;
        double b = 0;
        for(int l = 0; l<n;l++){
            a+=x_min_mean*x_min_mean;
            b+=y_min_mean*y_min_mean;
        }

        //sigmax
        //double sigmax = Math.sqrt(sxx/n);
        double sigmax = Math.sqrt(a/(n-1));
        //sigmay
        //double sigmay = Math.sqrt(syy/n);
        double sigmay = Math.sqrt(b/(n-1));
        //covariance
        double cov = sum/(n-1);
        //correlation
        //double r = cov/(sigmax*sigmay);//
        //double r = ((n*sxy)-(sx*sy))/(Math.sqrt(((n*(sx*sx))-(sy*sy))*((n*(sy*sy))-(sx*sx))));
        double r = ((n*sxy)-(sx*sy))/(Math.pow(((n*sxx)-(sx*sx))*((n*syy)-(sy*sy)),1/2));
        //double r = cov/(sigmax*sigmay);
        //double r = sum/(Math.sqrt((x_min_mean))*Math.sqrt((y_min_mean)));
        System.out.println("Total Numbers:"+n+"\nCorrelation Coefficient:"+r+ "\nThe covariance: "+ cov+ "\nSigmaX: "+sigmax+"\nSigmaY: "+sigmay
        );
        // correlation is just a normalized covariation
        return r*10;//cov / sigmax / sigmay;
       // return 0.0;
       // double corr = new PearsonsCorrelation().correlation(x, y);
       // System.out.println("Correlation: " + corr);
       // return corr;
    }




    public static class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;

            int bufferSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
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
    public class RecordAudio extends AsyncTask<Void, double[], Void> {
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
                audioRecord.startRecording();
                int r = 0;//this is a comment for the first commit
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i]/500.0;// /500.0;
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

       // double[] toTransform2;
       DoubleFFT_1D fftDo;
        void settoTransform2(double[] toTransform3){
            toTransform2 = new double[toTransform3.length * 2];
            System.arraycopy(toTransform3, 0, toTransform2, 0, toTransform3.length);
            fftDo = new DoubleFFT_1D(toTransform2.length);
        }
        @Override
        protected Void doInBackground(Void... params) {
           // transformer.ft(toTransform2);
            return null;

        }
    }
    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}

