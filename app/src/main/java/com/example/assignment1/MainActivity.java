package com.example.assignment1;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        displayRecentImg();
    }

    private void displayRecentImg() {
        File directory = new File(getFilesDir(), "");
        File[] files = directory.listFiles(file -> {
            String fileName = file.getName().toLowerCase();
            return file.isFile() && !fileName.equals("profileinstalled");
        });

        ImageView im = findViewById(R.id.imv);

        if (files != null && files.length > 0) {
            Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

            displayImage(files, files.length - 1, im);
            displayPrevThreeImages(files, files.length - 1);
        } else {
            setPlaceholderImage(im);
            setPlaceholderImage(findViewById(R.id.smallImage1));
            setPlaceholderImage(findViewById(R.id.smallImage2));
            setPlaceholderImage(findViewById(R.id.smallImage3));
        }
    }

    private void displayPrevThreeImages(File[] files, int i) {
        int[] imageViewIds = {R.id.smallImage1, R.id.smallImage2, R.id.smallImage3};
        for (int j = 1; j <= 3; j++) {
            ImageView im = findViewById(imageViewIds[j - 1]);
            int fileIndex = i - j;
            if (fileIndex >= 0) {
                displayImage(files, fileIndex, im);
                im.setTag(fileIndex);
            } else {
                setPlaceholderImage(im);
                im.setTag(null);
            }
        }
    }

    private void displayImage(File[] files, int i, ImageView im) {
        Bitmap bitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());
        im.setImageBitmap(bitmap);
    }

    public void replaceBigImage(View view) {
        ImageView clickedImg = (ImageView) view;
        Object tag = clickedImg.getTag();

        if (tag == null) {
            return;
        }
        int clickedImgIndex = (int) tag;

        File directory = new File(getFilesDir(), "");
        File[] files = directory.listFiles(file -> {
            String fileName = file.getName().toLowerCase();
            return file.isFile() && !fileName.equals("profileinstalled");
        });
        Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

        ImageView im = findViewById(R.id.imv);
        displayImage(files, clickedImgIndex, im);
        displayPrevThreeImages(files, clickedImgIndex);
    }

    private void setPlaceholderImage(ImageView im) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);

        int cropStartX = 0;
        int cropStartY = 0;
        int cropWidth = 120;
        int cropHeight = 160;

        Bitmap croppedPlaceholder = Bitmap.createBitmap(b, cropStartX, cropStartY, cropWidth, cropHeight);
        im.setImageBitmap(croppedPlaceholder);
    }

    public void takePic(View view) {
        Intent x = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(x, 212);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap b = (Bitmap) data.getExtras().get("data");
        String imgName = "IMG_" + System.currentTimeMillis() + ".png";

        FileOutputStream fos = null;
        try {
            fos = openFileOutput(imgName, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        b.compress(Bitmap.CompressFormat.PNG, 95, fos);
        try {
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        displayRecentImg();
    }
}