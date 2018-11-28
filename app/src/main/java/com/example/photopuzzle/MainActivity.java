package com.example.photopuzzle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    static final int STORAGE_PERMISSION_CODE = 1;
    static final int IMAGE_PATH = 402;

    int maxSize;
    ImageButton[] buttons = new ImageButton[9];
    float initX, initY, deltaX, deltaY;
    ImageButton collision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttons[0] = findViewById(R.id.button1);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);
        buttons[4] = findViewById(R.id.button5);
        buttons[5] = findViewById(R.id.button6);
        buttons[6] = findViewById(R.id.button7);
        buttons[7] = findViewById(R.id.button8);
        buttons[8] = findViewById(R.id.button9);



        for(ImageButton btn : buttons) {
            btn.setOnTouchListener(this);
            btn.setBackgroundColor(Color.GRAY);
        }

        boolean grantedAll = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!grantedAll)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mix) {
            this.mixButtons();
            return true;
        } else if(item.getItemId() == R.id.photo) {
            this.takePicture();
            return false;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                initX = v.getX();
                initY = v.getY();
                deltaX = event.getRawX() - initX;
                deltaY = event.getRawY() - initY;
                v.setZ(100);
                break;

            case MotionEvent.ACTION_MOVE:
                v.setX(event.getRawX() - deltaX);
                v.setY(event.getRawY() - deltaY);
                collision = null;

                for(ImageButton b : buttons) {
                    if(b.getId() != v.getId()) {
                        if (detectCollision(b, v.getX() + event.getX(), v.getY() + event.getY())) {
                            b.setBackgroundColor(Color.RED);
                            collision = b;
                        } else {
                            b.setBackgroundColor(Color.GRAY);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

                for(ImageButton b : buttons) {
                    b.setBackgroundColor(Color.GRAY);
                }

                if(collision != null){
                    collision.setZ(50);
                    collision.animate()
                            .x(initX)
                            .y(initY)
                            .z(1)
                            .setDuration(500)
                            .start();

                    v.animate()
                            .x(collision.getX())
                            .y(collision.getY())
                            .z(1)
                            .setDuration(500)
                            .start();


                } else {
                    v.animate()
                            .x(initX)
                            .y(initY)
                            .z(1)
                            .setDuration(500)
                            .start();
                }


                break;
        }

        return false;
    }

    public boolean detectCollision(ImageButton b, float x, float y) {
        Rect hitRect = new Rect();
        b.getHitRect(hitRect);

        return hitRect.left <= x && hitRect.right >= x &&
                hitRect.top <= y && hitRect.bottom >= y;
    }

    public void mixButtons() {
        for(ImageButton b : buttons){
            Random r = new Random();
            int random = r.nextInt(8);
            float tempX = b.getX();
            float tempY = b.getY();
            b.setX(buttons[random].getX());
            b.setY(buttons[random].getY());
            buttons[random].setX(tempX);
            buttons[random].setY(tempY);
        }
    }

    public Uri prepareFile() {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File file = null;
            try {
                file = File.createTempFile("PuzzleImage",".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jgp");
            return FileProvider.getUriForFile(this, "com.example.photopuzzle", file);
        }
        return null;
    }

    public void takePicture() {
        Uri imageFile = prepareFile();
        Log.w("URI", imageFile.toString());

        if(imageFile != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);
            startActivityForResult(intent, IMAGE_PATH);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("RESULT", "onActivityResult: " + resultCode + " requ: " + requestCode);
        if (resultCode != Activity.RESULT_CANCELED && requestCode == IMAGE_PATH) {
           // Toast.makeText(this, resultCode, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show();
            this.setPhoto();
        }
    }

    public File getFile(){
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            if(files != null) {
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    if(file.getName().contains("PuzzleImage")) {
                        Toast.makeText(this, file.getName(), Toast.LENGTH_SHORT).show();
                        return file;
                    }
                }
            }
        }
        return null;
    }

    public void setPhoto(){
        File imageFile = getFile();
        Bitmap imgBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imgBitmap = shapeIntoSqare(imgBitmap);

        Bitmap cutBitmap = null;
        int id = 0;
        for(int y = 0; y < 3; y++){
            for(int x = 0; x < 3; x++){
                cutBitmap = cutBitmap(imgBitmap, x, y);
                buttons[id].setImageBitmap(cutBitmap);
                id++;
            }
        }

        //.setImageBitmap(imgBitmap);
    }

    public Bitmap shapeIntoSqare(Bitmap src) {
        this.maxSize = buttons[0].getWidth()*3;
        Log.d("SIZE", "maxSize: " + buttons[7].getWidth());

        int w = src.getWidth();
        int h = src.getHeight();
        Log.d("SHAPEINTO", "w: " + w + " h: " + h);
        Bitmap target = null;
        if(w > h) {
            target = Bitmap.createBitmap(src, (w-h)/2, 0, h, h);
        } else {
            target = Bitmap.createBitmap(src, 0, (h-w)/2, w, w);
        }

        return Bitmap.createScaledBitmap(target, maxSize, maxSize, false);
    }

    public Bitmap cutBitmap(Bitmap src,int row, int col){
        int length = src.getWidth()/3;
        int x = length*row;
        int y = length*col;

        Bitmap target = null;
        target = Bitmap.createBitmap(src, x, y, length, length);

        return target;
    }

}
