package com.farshid.customnightvision;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.farshid.customnightvision.GlCameraClasses.PortraitActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public ArrayList<PictureItem> pictureItems = new ArrayList<>();
    HistoryAdapter historyAdapter = new HistoryAdapter(this, getData());


    @Override
    protected void onResume() {
        super.onResume();
        pictureItems.clear();
        getData();
        historyAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RecyclerView recyclerView = findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(historyAdapter);
    }


    private ArrayList<PictureItem> getData(){

        File customCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/CustomCamera");
        PictureItem pictureItem;

        if(customCameraFolder.exists()){
            File[] files = customCameraFolder.listFiles();
            for (int i = 0; i < files.length ; i++) {

                File file = files[i];
                pictureItem = new PictureItem();
                pictureItem.setName(file.getName());
                pictureItem.setUri(FileProvider.getUriForFile(MainActivity.this, "com.farshid.customnightvision.provider", file));
                pictureItem.setExtension(Uri.fromFile(file).toString().substring(Uri.fromFile(file).toString().lastIndexOf(".")));
                pictureItems.add(pictureItem);
                Log.i(TAG, "getData: " + Uri.fromFile(file));
            }

        }

        Collections.sort(pictureItems, new Comparator<PictureItem>(){
            public int compare(PictureItem obj1, PictureItem obj2) {
                // ## Ascending order
//                        return obj1.getName().compareToIgnoreCase(obj2.getName()); // To compare string values
                // return Integer.valueOf(obj1.getId()).compareTo(obj2.getId()); // To compare integer values

                // ## Descending order
                return obj2.getName().compareToIgnoreCase(obj1.getName()); // To compare string values
                // return Integer.valueOf(obj2.getId()).compareTo(obj1.getId()); // To compare integer values
            }
        });



        return pictureItems;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent (MainActivity.this , PortraitActivity.class);
        startActivity(intent);
    }
 }



