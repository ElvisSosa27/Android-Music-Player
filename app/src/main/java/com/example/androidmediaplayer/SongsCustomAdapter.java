package com.example.androidmediaplayer;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class SongsCustomAdapter extends RecyclerView.Adapter<SongsCustomAdapter.MyViewHolder> {

    private Activity activity;
    private Context context;
    static ArrayList<SongsModel> songsModels;

    public SongsCustomAdapter(Activity activity, Context context, ArrayList<SongsModel> songsModels) {
        this.activity = activity;
        this.context = context;
        this.songsModels = songsModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_list_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.Title.setText(songsModels.get(position).getTitle());
        holder.Artist.setText(songsModels.get(position).getArtist());
        byte[] image = getSongImage(songsModels.get(position).getPath());
        if(image != null){
            Glide.with(context).asBitmap().load(image).into(holder.SongImage);
        }
        else{
            Glide.with(context).load(R.drawable.song_logo).into(holder.SongImage);
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewSongActivity.class);
            intent.putExtra("position", position);
            context.startActivity(intent);
            activity.finish();
        });
        holder.MenuMore.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_more, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch(menuItem.getItemId()){
                    case R.id.menuDelete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete song")
                                .setMessage("Are you sure you want to delete this song?")
                                .setPositiveButton("Yes", (dialogInterface, i) -> deleteFile(position, view))
                                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                                .create().show();
                        break;
                }
                return true;
            });
        });
    }

    private void deleteFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(songsModels.get(position).getId()));
        File file = new File(songsModels.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted) {
            context.getContentResolver().delete(contentUri, null, null);
            songsModels.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, songsModels.size());
            Snackbar.make(view, "Song deleted: ", Snackbar.LENGTH_LONG).show();
        }
        else{
            Snackbar.make(view, "Cannot delete song: ", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return songsModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView SongImage, MenuMore;
        TextView Title, Artist;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            SongImage = itemView.findViewById(R.id.imgSongIMG);
            MenuMore = itemView.findViewById(R.id.imgMore);
            Title = itemView.findViewById(R.id.txtSongTitle);
            Artist = itemView.findViewById(R.id.txtSongArtist);
        }
    }

    private byte[] getSongImage(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] img = retriever.getEmbeddedPicture();
        retriever.release();
        return img;
    }

    void updateList(ArrayList<SongsModel> models){
        songsModels = new ArrayList<>();
        songsModels.addAll(models);
        notifyDataSetChanged();
    }
}
