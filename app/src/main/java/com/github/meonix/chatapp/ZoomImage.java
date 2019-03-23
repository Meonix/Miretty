package com.github.meonix.chatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ZoomImage extends AppCompatActivity {
    private String linkImage;
    private PhotoView ZoomImageView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);
        progressDialog = new ProgressDialog(this);
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
                progressDialog.setTitle("Image is Downloading...");
                progressDialog.show();



                progressDialog.dismiss();
                Toast.makeText(this, "Image is Downloading...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}

