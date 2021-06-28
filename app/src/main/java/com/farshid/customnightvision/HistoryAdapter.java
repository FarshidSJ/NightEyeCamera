package com.farshid.customnightvision;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private final List<PictureItem> pictureItems;
    private static final String TAG = "HistoryAdapter";

    public HistoryAdapter(Context context, List<PictureItem> pictureItems) {
        this.context = context;
        this.pictureItems = pictureItems;
    }


    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_layout, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {

        PictureItem p = pictureItems.get(position);
        holder.name.setText(p.getName());
        Glide.with(context).load(p.getUri()).placeholder(R.drawable.ic_launcher).into(holder.thumbnail);
        String extension = p.getUri().toString().substring(p.getUri().toString().lastIndexOf("."));
        Log.i(TAG, "onBindViewHolder: " + extension);

        if (extension.equals(".mp4")) {
            holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, holder.buttonViewOption);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.history_item_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_item:
                                    //handle menu1 click
                                    ContentResolver contentResolver = context.getContentResolver();
                                    contentResolver.delete(p.getUri(), null, null);
                                    pictureItems.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, pictureItems.size());
                                    break;
                                case R.id.share_item://handle sharing the item
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    String title = context.getResources().getString(R.string.app_name);
                                    Intent chooser = Intent.createChooser(intent, title);
                                    intent.setDataAndType(p.getUri(), "video/*");
                                    intent.putExtra(Intent.EXTRA_STREAM, p.getUri());
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                                        context.startActivity(chooser);
                                    }
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();

                }
            });
            holder.thumbnailForeground.setImageResource(R.drawable.ic_play_circle_outline);
            holder.thumbnailForeground.setVisibility(View.VISIBLE);
            holder.thumbnailForeground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.i(TAG, "onClick: " + p.getUri());
                    Intent intent = new Intent(Intent.ACTION_VIEW, p.getUri());
                    String title = context.getResources().getString(R.string.app_name);
                    Intent chooser = Intent.createChooser(intent, title);
                    intent.setDataAndType(p.getUri(), "video/mp4");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(chooser);
                    }
                }
            });
        } else if (extension.equals(".png")) {

            holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, holder.buttonViewOption);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.history_item_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_item:
                                    //handle menu1 click
                                    ContentResolver contentResolver = context.getContentResolver();
                                    contentResolver.delete(p.getUri(), null, null);
                                    pictureItems.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, pictureItems.size());
                                    break;
                                case R.id.share_item: //handle sharing the item
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    String title = context.getResources().getString(R.string.app_name);
                                    Intent chooser = Intent.createChooser(intent, title);
                                    intent.setDataAndType(p.getUri(), "image/*");
                                    intent.putExtra(Intent.EXTRA_STREAM, p.getUri());
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                                        context.startActivity(chooser);
                                    }
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();

                }
            });
            holder.thumbnailForeground.setVisibility(View.INVISIBLE);
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.i(TAG, "onClick: " + p.getUri());
                    Intent intent = new Intent(Intent.ACTION_VIEW, p.getUri());
                    String title = context.getResources().getString(R.string.app_name);
                    Intent chooser = Intent.createChooser(intent, title);
                    intent.setDataAndType(p.getUri(), "image/png");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(chooser);
                    }
                }
            });

        }


    }

    @Override
    public int getItemCount() {
        return pictureItems.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView thumbnail, thumbnailForeground;
        public TextView buttonViewOption;

        public HistoryViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            thumbnail = view.findViewById(R.id.thumbnail);
            buttonViewOption = view.findViewById(R.id.textViewOptions);
            thumbnailForeground = view.findViewById(R.id.thumbnail_foreground);
        }
    }


}
