package com.example.androidmediaplayer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AllSongsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    RecyclerView SongsList;
    public static final int REQUEST_CODE = 1;
    static ArrayList<SongsModel> modelSongs;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static SongsCustomAdapter songsCustomAdapter;
    private String MY_SORT_PREFERENCE = "SortOrder";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_songs_layout);

        SongsList = findViewById(R.id.recyclerSongs);
        Permission();
        SongsList.setHasFixedSize(true);

        if(!(modelSongs.size() < 1)) {
            songsCustomAdapter = new SongsCustomAdapter(this, this, modelSongs);
            SongsList.setAdapter(songsCustomAdapter);
            SongsList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }


    private void Permission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        else{
            modelSongs = getSongs(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                modelSongs = getSongs(this);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    public ArrayList<SongsModel> getSongs(Context context){
        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREFERENCE, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sortByName");
        ArrayList<SongsModel> songList = new ArrayList<>();
        String order = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        switch(sortOrder){
            case "sortByName":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;

            case "sortByDate":
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;

            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);
        if(cursor != null){
            while(cursor.moveToNext()){
                String title = cursor.getString(0);
                String duration = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                String id = cursor.getString(4);

                SongsModel songsModel = new SongsModel(path, title, artist, duration, id);

                Log.e("Path : " + path, "Title : " + title);
                songList.add(songsModel);
            }
            cursor.close();
        }
        return songList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_songs, menu);
        MenuItem menuItem = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREFERENCE, MODE_PRIVATE).edit();
        switch(item.getItemId()){
            case R.id.subMenuByName:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                Toast.makeText(this, "Sorted by name", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.subMenuByDate:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                Toast.makeText(this, "Sorted by date", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.subMenuBySize:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                Toast.makeText(this, "Sorted by size", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String userInput = s.toLowerCase();
        ArrayList<SongsModel> myMusic = new ArrayList<>();
        for(SongsModel songs : modelSongs){
            if(songs.getTitle().toLowerCase().contains(userInput)){
                myMusic.add(songs);
            }
        }
        songsCustomAdapter.updateList(myMusic);
        return true;
    }
}
