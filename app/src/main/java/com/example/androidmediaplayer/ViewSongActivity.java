package com.example.androidmediaplayer;

import static com.example.androidmediaplayer.AllSongsActivity.repeatBoolean;
import static com.example.androidmediaplayer.AllSongsActivity.shuffleBoolean;
import static com.example.androidmediaplayer.SongsCustomAdapter.songsModels;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class ViewSongActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    ImageView SongImage, ShuffleButton, PreviousButton, NextButton, RepeatButton;
    FloatingActionButton PlayPauseButton;
    TextView SongName, SongArtist, DurationPlayed, DurationTotal;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<SongsModel> songsModelArrayList = new ArrayList<>();
    static Uri uri;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    MusicService musicService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.view_song_layout);
        getSupportActionBar().hide();
        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(musicService != null && b){
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ViewSongActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    int currentPos = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPos);
                    DurationPlayed.setText(formattedTime(currentPos));
                }
                handler.postDelayed(this, 1000);
            }
        });

        ShuffleButton.setOnClickListener(view -> {
            if(shuffleBoolean){
                shuffleBoolean = false;
                ShuffleButton.setImageResource(R.drawable.ic_shuffle_off);
            }
            else{
                shuffleBoolean = true;
                ShuffleButton.setImageResource(R.drawable.ic_shuffle_on);
            }
        });

        RepeatButton.setOnClickListener(view -> {
            if(repeatBoolean){
                repeatBoolean = false;
                RepeatButton.setImageResource(R.drawable.ic_repeat_off);
            }
            else{
                repeatBoolean = true;
                RepeatButton.setImageResource(R.drawable.ic_repeat_on);
            }
        });
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void initViews(){
        SongImage = findViewById(R.id.imgImage);
        ShuffleButton = findViewById(R.id.imgShuffleSong);
        PreviousButton = findViewById(R.id.imgPrevious);
        NextButton = findViewById(R.id.imgNext);
        RepeatButton = findViewById(R.id.imgRepeatSong);
        PlayPauseButton = findViewById(R.id.btnPlayPauseSong);
        SongName = findViewById(R.id.txtSong);
        SongArtist = findViewById(R.id.txtArtist);
        DurationPlayed = findViewById(R.id.txtDurationPlayed);
        DurationTotal = findViewById(R.id.txtDurationTotal);
        seekBar = findViewById(R.id.seekBar);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                PreviousButton.setOnClickListener(view -> {
                    prevButtonClicked();
                });
            }
        };
        prevThread.start();
    }

    public void prevButtonClicked() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(songsModelArrayList.size() - 1);
            }
            else if(!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? songsModelArrayList.size() - 1 : (position - 1));
            }
            uri = Uri.parse(songsModelArrayList.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri.toString());
            SongName.setText(songsModelArrayList.get(position).getTitle());
            SongArtist.setText(songsModelArrayList.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            ViewSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                        DurationPlayed.setText(formattedTime(currentPos));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
            PlayPauseButton.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        }
        else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(songsModelArrayList.size() - 1);
            }
            else if(!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? songsModelArrayList.size() - 1 : (position - 1));
            }
            uri = Uri.parse(songsModelArrayList.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri.toString());
            SongName.setText(songsModelArrayList.get(position).getTitle());
            SongArtist.setText(songsModelArrayList.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            ViewSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                        DurationPlayed.setText(formattedTime(currentPos));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
            PlayPauseButton.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                NextButton.setOnClickListener(view -> {
                    nextButtonClicked();
                });
            }
        };
        nextThread.start();
    }

    public void nextButtonClicked() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(songsModelArrayList.size() - 1);
            }
            else if(!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % songsModelArrayList.size());
            }
            uri = Uri.parse(songsModelArrayList.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri.toString());
            SongName.setText(songsModelArrayList.get(position).getTitle());
            SongArtist.setText(songsModelArrayList.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            ViewSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                        DurationPlayed.setText(formattedTime(currentPos));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
            PlayPauseButton.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        }
        else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(songsModelArrayList.size() - 1);
            }
            else if(!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % songsModelArrayList.size());
            }
            uri = Uri.parse(songsModelArrayList.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri.toString());
            SongName.setText(songsModelArrayList.get(position).getTitle());
            SongArtist.setText(songsModelArrayList.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            ViewSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                        DurationPlayed.setText(formattedTime(currentPos));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
            PlayPauseButton.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                PlayPauseButton.setOnClickListener(view -> {
                    playPauseButtonClicked();
                });
            }
        };
        playThread.start();
    }

    public void playPauseButtonClicked() {
        if(musicService.isPlaying()){
            PlayPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            ViewSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                        DurationPlayed.setText(formattedTime(currentPos));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
        else{
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
            PlayPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            ViewSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                        DurationPlayed.setText(formattedTime(currentPos));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    private String formattedTime(int currentPos) {
        String totalOut, totalNew;
        String seconds = String.valueOf(currentPos % 60);
        String minutes = String.valueOf(currentPos / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1){
            return totalNew;
        }
        else{
            return totalOut;
        }
    }

    public void getIntentMethod(){
        position = getIntent().getIntExtra("position", -1);
        songsModelArrayList = songsModels;
        if(songsModelArrayList != null){
            PlayPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
            uri = Uri.parse(songsModelArrayList.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    private void metaData(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        int durationTotal = Integer.parseInt(songsModelArrayList.get(position).getDuration()) / 1000;
        DurationTotal.setText(formattedTime(durationTotal));
        byte[] image = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (image != null) {
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            ImageAnimation(this, SongImage, bitmap);
            Palette.from(bitmap).generate(palette -> {
                assert palette != null;
                Palette.Swatch swatch = palette.getDominantSwatch();

                if (swatch != null) {
                    ImageView gradient = findViewById(R.id.imgImage);
                    ConstraintLayout ViewSongLY = findViewById(R.id.constraintLYViewSong);
                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                    ViewSongLY.setBackgroundResource(R.drawable.view_song_bg);
                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), 0x00000000});
                    gradient.setBackground(gradientDrawable);
                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), swatch.getRgb()});
                    ViewSongLY.setBackground(gradientDrawableBg);
                    SongName.setTextColor(swatch.getTitleTextColor());
                    SongArtist.setTextColor(swatch.getBodyTextColor());
                } else {
                    ImageView gradient = findViewById(R.id.imgImage);
                    ConstraintLayout ViewSongLY = findViewById(R.id.constraintLYViewSong);
                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                    ViewSongLY.setBackgroundResource(R.drawable.view_song_bg);
                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0x00000000});
                    gradient.setBackground(gradientDrawable);
                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0xff000000});
                    ViewSongLY.setBackground(gradientDrawableBg);
                    SongName.setTextColor(Color.WHITE);
                    SongArtist.setTextColor(Color.DKGRAY);
                }
            });
        } else {
            Glide.with(this).asBitmap().load(R.drawable.song_logo).into(SongImage);
            ImageView gradient = findViewById(R.id.imgImage);
            ConstraintLayout ViewSongLY = findViewById(R.id.constraintLYViewSong);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            ViewSongLY.setBackgroundResource(R.drawable.view_song_bg);
            SongName.setTextColor(Color.WHITE);
            SongArtist.setTextColor(Color.DKGRAY);
        }
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){

        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri.toString());
        SongName.setText(songsModelArrayList.get(position).getTitle());
        SongArtist.setText(songsModelArrayList.get(position).getArtist());
        musicService.OnCompleted();
        musicService.showNotification(R.drawable.ic_baseline_pause_24);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, AllSongsActivity.class));
    }
}
