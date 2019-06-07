package com.example.bsk69.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ListMenuItemView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    ListMenuItemView a_replay,a_end;
    private ListView songView;
    private TextView seekTitleTxt;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused= false, playbackPaused=false;
    private boolean clicked=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       while(true) {
           if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
               Toast.makeText(MainActivity.this, "Zezwoliłeś na dostęp do pamięci ! ", Toast.LENGTH_SHORT).show();
           } else {
               requestPermission();
           }
           break;
       }
        setContentView(R.layout.activity_main);
        a_replay = (ListMenuItemView)findViewById(R.id.action_replay);
        a_end = (ListMenuItemView)findViewById(R.id.action_end);
        songView=(ListView)findViewById(R.id.song_list);
        songList=new ArrayList<Song>();
        seekTitleTxt=(TextView)findViewById(R.id.seek_song_title);
        SongAdapter songAdapter = new SongAdapter(this,songList);
        getSongList();
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        songView.setAdapter(songAdapter);
        setController();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this).setTitle("Potrzebna zgoda").setMessage("Ta zgoda jest potrzebna do działania aplikacji ").setPositiveButton("Przyjęto", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
            }).setNegativeButton("Poniechaj", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Uprawnienie przyjęte",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Uprawnienie odrzucone",Toast.LENGTH_SHORT).show();
                }
            case 2:

                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Uprawnienie przyjęte",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Uprawnienie odrzucone",Toast.LENGTH_SHORT).show();
                }
            case 3:

                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Uprawnienie przyjęte",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Uprawnienie odrzucone",Toast.LENGTH_SHORT).show();
                }
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder )service;
            musicService = binder.getService();
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
   /* public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {
                    Toast.makeText(MainActivity.this,"Nie masz dostepu do sieci",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {
                    Toast.makeText(MainActivity.this,"Nie masz dostepu do blokady wybudzenia",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 3:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {
                    Toast.makeText(MainActivity.this,"Nie masz dostepu do sieci",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }*/

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }


    protected void onStart(){
        super.onStart();
        if(playIntent==null){
            playIntent=new Intent(this,MusicService.class);
            bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);

        }
    }

    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
        //seekTitleTxt.setText("Teraz grane:" );
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_replay:
                musicService.setShuffle();
                if(clicked==false) {
                    Toast.makeText(MainActivity.this, "Ustawiono zapętlenie utworu !", Toast.LENGTH_SHORT).show();
                    clicked = true;
                }
                else {
                    Toast.makeText(MainActivity.this, "Wyłączono zapętlenie utworu !", Toast.LENGTH_SHORT).show();
                    clicked = false;
                }
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService=null;
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy(){
        stopService(playIntent);
        musicService=null;
        super.onDestroy();
    }

    public void setController(){
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void playNext(){
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPng()){
            return musicService.getDur();
        }
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService!=null && musicBound && musicService.isPng()){
            return musicService.getPosn();
        }
        else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService!=null && musicBound){
            musicService.isPng();
        }
        return false;
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop(){
        controller.hide();
        super.onStop();
    }



    @Override
    public int getBufferPercentage() {
        return 2;
    }

    @Override
    public boolean canPause() {
        super.onPause();
        paused=true;
        return paused;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
