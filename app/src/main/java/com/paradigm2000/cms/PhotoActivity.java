package com.paradigm2000.cms;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.paradigm2000.cms.app.PhotoGroup;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.Repair;
import com.paradigm2000.cms.widget.PhotoAdapter;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.gallery.event.CheckSelectedEvent;
import com.paradigm2000.core.gallery.event.ModeChangeEvent;
import com.paradigm2000.core.io.Folder;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.Locale;

@EActivity(R.layout.activity_photo) @OptionsMenu(R.menu.menu_photo)
public class PhotoActivity extends Activity implements PhotoAdapter.AddPhotoListener
{
    static final int REQUEST = 0x00826e;

    @OptionsMenuItem(R.id.delete)
    MenuItem _delete;
    @ViewById(R.id.recyclerview)
    RecyclerView _recyclerview;
    @Bean
    PhotoAdapter photoAdapter;
    @Bean
    EventBus bus;
    @Extra("Photo_header") @InstanceState
    Header header;
    @Extra("Photo_detail") @InstanceState
    Detail detail;
    @Extra("Photo_repair") @InstanceState
    Repair repair;
    @InstanceState
    Uri lastPhoto;
    @SystemService
    Vibrator vibrator;

    PhotoGroup photoGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);
        bus.register(this);
    }

    @AfterInject
    void afterInject()
    {
        if (header != null) photoGroup = header;
        if (detail != null) photoGroup = detail;
        else if (repair != null) photoGroup = repair;
        setTitle(photoGroup.getTitle());
    }

    @AfterViews
    void afterViews()
    {
        if (Common.get().isExternalStorageAvailable())
        {
            GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
            _recyclerview.setLayoutManager(manager);
            _recyclerview.setAdapter(photoAdapter);
            photoAdapter.setAddPhotoListener(this);
            photoAdapter.setPhotoGroup(photoGroup);
            if (header != null && header.isCompleted()) photoAdapter.setEnabled(false);
            else if (photoAdapter.getItemCount() == 1) addPhoto();
        }
        else
        {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    @OnActivityResult(REQUEST)
    void onCaptureImage(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            if (lastPhoto != null) photoAdapter.addImage(lastPhoto);
        }
        else if (photoAdapter.getItemCount() == 1)
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (photoAdapter.isSelectMode())
        {
            photoAdapter.setSelectMode(false);
        }
        else
        {
            setResult(RESULT_OK);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bus.unregister(this);
    }

    void setTitle(PhotoGroup group)
    {
        photoGroup = group;
        setTitle(group.getTitle());
    }

    /****************************************/
    // TODO UI events
    /****************************************/

    @OptionsItem(R.id.delete)
    void delete()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitleRes(R.string.app_name)
                .setContentRes(R.string.confirm, getString(R.string.delete).toLowerCase(Locale.ENGLISH))
                .setConfirm(R.string.delete)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        photoAdapter.commitDeletion();
                    }
                })
                .show();
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public void addPhoto()
    {
        if (Common.get().isExternalStorageAvailable())
        {
            Folder folder = photoGroup.getFolder(this);
            File file = folder.file(System.currentTimeMillis() + ".jpg");
            Uri uri = lastPhoto = Uri.fromFile(file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraIntent, REQUEST);
        }
        else
        {
            onBackPressed();
        }
    }

    /****************************************/
    // TODO Event subscription
    /****************************************/

    @Subscribe
    public void onCheckSelected(CheckSelectedEvent event)
    {
        if (event.hasSelected())
        {
            _delete.setEnabled(true);
            _delete.getIcon().setAlpha(255);
        }
        else
        {
            _delete.setEnabled(false);
            _delete.getIcon().setAlpha(127);
        }
    }

    @Subscribe
    public void onModeChange(ModeChangeEvent event)
    {
        if (event.isSelectMode())
        {
            _delete.setVisible(true);
            _delete.setEnabled(false);
            _delete.getIcon().setAlpha(127);
        }
        else
        {
            _delete.setVisible(false);
        }
        int colorId = event.isSelectMode()? R.color.colorPrimaryDark: R.color.colorPrimary;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, colorId)));
    }
}
