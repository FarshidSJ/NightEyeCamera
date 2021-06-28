/*MIT License

        Copyright (c) 2018 Masayuki Suda

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.*/
package com.farshid.customnightvision.GlCameraClasses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.farshid.customnightvision.MainActivity;
import com.farshid.customnightvision.PictureItem;
import com.farshid.customnightvision.R;

import java.io.File;

public class PortraitActivity extends BaseCamera {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait);
        onCreateActivity();
        videoWidth = 720;
        videoHeight = 1280;
        cameraWidth = 1280;
        cameraHeight = 720;

        ImageButton btnHistory = findViewById(R.id.btn_history);

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File customCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/CustomCamera");
                File[] files;

                if(customCameraFolder.exists()) {
                    files = customCameraFolder.listFiles();
                    if(files.length != 0){
                        Intent intent = new Intent(PortraitActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(PortraitActivity.this, "Take an image first !", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(PortraitActivity.this, "Take an image first !", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImageButton btnHistory = findViewById(R.id.btn_history);

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File customCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/CustomCamera");
                PictureItem pictureItem;
                File[] files;

                if(customCameraFolder.exists()) {
                    files = customCameraFolder.listFiles();
                    if(files.length != 0){
                        Intent intent = new Intent(PortraitActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(PortraitActivity.this, "Take an image first !", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(PortraitActivity.this, "Take an image first !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press Back one more time to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
