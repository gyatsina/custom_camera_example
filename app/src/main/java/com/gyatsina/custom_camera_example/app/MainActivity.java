package com.gyatsina.custom_camera_example.app;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileNotFoundException;


public class MainActivity extends ActionBarActivity {

    public static int TAKE_IMAGE_CUSTOM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public void onBackPressed(){
        Intent cameraIntent = new Intent(MainActivity.this, ActivityCustomCamera.class);
        startActivity(cameraIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //    public void takePhotoCustom(final View view) {
//        Intent customCamera = new Intent(this, ActivityCustomCamera.class);
//        startActivityForResult(customCamera, TAKE_IMAGE_CUSTOM);
//	}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == TAKE_IMAGE_CUSTOM){
                String photoPath = data.getExtras().getString("photoFilePath");
                File photoFile = new File(photoPath);
                Uri photoUri = Uri.fromFile(photoFile);
//                Picasso.with(this)
//                        .load(photoUri).fit().centerInside()
//                        .into(takenImage, imageLoadedCallback);
//            }
        }
    }
}
