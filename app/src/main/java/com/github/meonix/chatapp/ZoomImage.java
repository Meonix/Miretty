package com.github.meonix.chatapp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

public class ZoomImage extends AppCompatActivity {
    private String linkImage;
    private PhotoView ZoomImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide Status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_zoom_image);
        ZoomImageView = findViewById(R.id.pvZoomImage);
        linkImage = getIntent().getExtras().get("imageURL").toString();
        Picasso.get().load(linkImage).into(ZoomImageView);


        ZoomImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                registerForContextMenu(ZoomImageView);
                ZoomImageView.showContextMenu();
                unregisterForContextMenu(ZoomImageView);
                return true;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.zoom_image_options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.downloadImage:
                Picasso.get()
                        .load(linkImage)
                        .into(new Target() {
                                  @Override
                                  public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                      try {
                                          Toast.makeText(ZoomImage.this, "Image has been Downloaded....", Toast.LENGTH_SHORT).show();
                                          // Get the directory for the user's public pictures directory.
                                          File file = new File(Environment.getExternalStoragePublicDirectory(
                                                  Environment.DIRECTORY_PICTURES), "Miretty");
                                          file.mkdir();
                                          final File externalFile = new File(file, System.currentTimeMillis() + ".jpg");

                                          FileOutputStream out = new FileOutputStream(externalFile);
                                          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                                          out.flush();
                                          out.close();
                                      } catch (Exception e) {
                                          // some action
                                          Toast.makeText(ZoomImage.this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
                                      }
                                  }

                                  @Override
                                  public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                      Toast.makeText(ZoomImage.this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
                                  }

                                  @Override
                                  public void onPrepareLoad(Drawable placeHolderDrawable) {

                                  }
                              }
                        );
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}

