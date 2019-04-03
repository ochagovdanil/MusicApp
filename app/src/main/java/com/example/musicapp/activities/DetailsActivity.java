package com.example.musicapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.musicapp.R;
import com.example.musicapp.adapters.DetailsRecyclerViewAdapter;
import com.example.musicapp.fragments.DeleteSongDialogFragment;
import com.example.musicapp.models.Detail;

import java.text.DecimalFormat;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initToolbar();
        loadImage();
        loadListOfDetails();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_delete) {
            // the request for the permission before the operation
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                deleteSong();
            }
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle(R.string.details);
        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void loadImage() {
        ImageView imageView = findViewById(R.id.image_logo_details);
        Bitmap bitmap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("image"),
                0,
                getIntent().getByteArrayExtra("image").length);
        imageView.setImageBitmap(bitmap);
    }

    private void loadListOfDetails() {
        Intent intent = getIntent();
        DetailsRecyclerViewAdapter adapter =
                new DetailsRecyclerViewAdapter(DetailsActivity.this);

        adapter.addDetail(new Detail(getString(R.string.details_title),
                intent.getStringExtra("title")));
        adapter.addDetail(new Detail(getString(R.string.details_artist),
                intent.getStringExtra("artist")));
        adapter.addDetail(new Detail(getString(R.string.details_album),
                intent.getStringExtra("album")));
        adapter.addDetail(new Detail(getString(R.string.details_first_year),
                String.valueOf(intent.getIntExtra("first_year", 0))));
        adapter.addDetail(new Detail(getString(R.string.details_last_year),
                String.valueOf(intent.getIntExtra("last_year", 0))));
        adapter.addDetail(new Detail(getString(R.string.details_duration),
                convertDuration(intent.getLongExtra("duration", 0))));
        adapter.addDetail(new Detail(getString(R.string.details_size),
                convertSize(intent.getLongExtra("size", 0))));
        adapter.addDetail(new Detail(getString(R.string.details_mim_type),
                intent.getStringExtra("mim_type")));

        RecyclerView recyclerView = findViewById(R.id.recycler_view_details);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    private String convertDuration(long duration) {
        int minutes = (int) (duration % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((duration % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // from bytes to megabytes
    private String convertSize(long size) {
        float mb = (float) (size / Math.pow(2, 20));
        return new DecimalFormat("#.##").format(mb) + " " + getString(R.string.details_mb);
    }

    private void deleteSong() {
        DeleteSongDialogFragment dialog = new DeleteSongDialogFragment();
        dialog.show(getSupportFragmentManager(), "DeleteSongDialogFragment");

        dialog.setOnPositiveButtonClickListener(
                new DeleteSongDialogFragment.OnPositiveButtonClickListener() {
            @Override
            public void setOnPositiveButtonClickListener() {
                // delete a current song
                getContentResolver().delete(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Audio.Media.DATA + " = ?",
                        new String[]{getIntent().getStringExtra("url")});

                Intent intent = new Intent();
                intent.putExtra("deleted", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
