package com.paradigm2000.cms;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.paradigm2000.cms.app.MyPref_;
import com.paradigm2000.cms.event.PhotosCheckEvent;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Fragment;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.gallery.GalleryActivity_;
import com.paradigm2000.core.io.Folder;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements NavigationView.OnNavigationItemSelectedListener
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "Main_";
    static final int REQUEST = 0x0091bc;

    @ViewById(R.id.drawerlayout)
    DrawerLayout _drawerlayout;
    @ViewById(R.id.toolbar)
    Toolbar _toolbar;
    @ViewById(R.id.drawer)
    NavigationView _drawer;
    @Pref
    MyPref_ pref;
    @Bean
    EventBus bus;

    ActionBarDrawerToggle toggle;
    InspectionFragment inspection = new InspectionFragment_();
    ContainerFragment container = new ContainerFragment_();
    RepairFragment repair = new RepairFragment_();
    LocationFragment location = new LocationFragment_();
    ContainerOutFragment containero = new ContainerOutFragment_();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @AfterViews
    void afterViews()
    {
        toggle = new ActionBarDrawerToggle(this, _drawerlayout, _toolbar, R.string.open, R.string.close);
        _drawerlayout.addDrawerListener(toggle);
        setSupportActionBar(_toolbar);
        setDisplayHomeAsUpEnabled(true);
        toggle.syncState();
        _drawer.setNavigationItemSelectedListener(this);
        SimpleDraweeView view = _drawer.getHeaderView(0).findViewById(R.id.header);
        // TODO retrieve from preference
        int resId = /* pref.ServerURL().exists()? R.drawable.engkong: */ R.drawable.header;
        Uri uri = new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(resId)).build();
        view.setImageURI(uri);
        if (!pref.surveyer().get()) _drawer.getMenu().removeItem(R.id.inspection);
        onNavigationItemSelected(_drawer.getMenu().getItem(0));
        TextView _welcome = _drawer.getHeaderView(0).findViewById(R.id.welcome);
        String username = pref.username().get();
        username = Character.toUpperCase(username.charAt(0)) + username.substring(1);
        _welcome.setText(getString(R.string.welcome, username));
        bus.post(new PhotosCheckEvent(this));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Folder folder = Folder.data(this, true);
        if (folder != null)
        {
            folder = folder.folder(Environment.DIRECTORY_PICTURES);
            if (!folder.delete() && DEBUG) Log.v(TAG, "Fail to delete @" + folder);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @OnActivityResult(REQUEST)
    void afterGallery()
    {
        onCheckPhotos(new PhotosCheckEvent(this));
    }

    @Override
    public void onBackPressed()
    {
        if (_drawerlayout.isDrawerOpen(GravityCompat.START))
        {
            _drawerlayout.closeDrawer(GravityCompat.START);
        }
        else if (!Fragment.onBackPressed(getSupportFragmentManager(), R.id.fragments) &&
                getSupportFragmentManager().getBackStackEntryCount() > 1)
        {
            getSupportFragmentManager().popBackStack();
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bus.unregister(this);
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        try
        {
            switch (item.getItemId())
            {
                case R.id.inspection:
                {
                    Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragments);
                    if (fragment instanceof InspectionFragment) return true;
                    String tag = inspection.getClass().getName();
                    Log.w(TAG, "selected : "+ tag);
                    if (getSupportFragmentManager().popBackStackImmediate(tag, 0)) return true;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragments, inspection, tag)
                            .addToBackStack(tag)
                            .commit();
                    return true;
                }
                case R.id.enquiry:
                {
                    Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragments);
                    if (fragment instanceof ContainerFragment) return true;
                    String tag = container.getClass().getName();
                    Log.w(TAG, "selected : "+ tag);
                    if (getSupportFragmentManager().popBackStackImmediate(tag, 0)) return true;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragments, container, tag)
                            .addToBackStack(tag)
                            .commit();
                    return true;
                }
                case R.id.repair:
                {
                    Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragments);
                    if (fragment instanceof RepairFragment) return true;
                    String tag = repair.getClass().getName();
                    Log.w(TAG, "selected : "+ tag);
                    if (getSupportFragmentManager().popBackStackImmediate(tag, 0)) return true;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragments, repair, tag)
                            .addToBackStack(tag)
                            .commit();
                    return true;
                }
                case R.id.containerout:
                {
                    Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragments);
                    if (fragment instanceof ContainerOutFragment) return true;
                    String tag = containero.getClass().getName();
                    Log.w(TAG, "selected : "+ tag);
                    if (getSupportFragmentManager().popBackStackImmediate(tag, 0)) return true;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragments, containero, tag)
                            .addToBackStack(tag)
                            .commit();
                    return true;
                }
                case R.id.location:
                {
                    Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragments);
                    if (fragment instanceof LocationFragment) return true;
                    String tag = location.getClass().getName();
                    Log.w(TAG, "selected : "+ tag);
                    if (getSupportFragmentManager().popBackStackImmediate(tag, 0)) return true;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragments, location, tag)
                            .addToBackStack(tag)
                            .commit();
                    return true;
                }
                case R.id.files:
                {
                    Folder folder = Folder.data(this, true);
                    if (folder == null)
                    {
                        item.setEnabled(false);
                    }
                    else
                    {
                        GalleryActivity_.intent(this)
                                .root(folder.toUri())
                                .startForResult(REQUEST);
                    }
                    return true;
                }
                case R.id.preference:
                {
                    PreferenceActivity_.intent(this)
                            .logined(true)
                            .start();
                    return true;
                }
                case R.id.logout:
                {
                    new Dialog(this, Dialog.PROMPT_TYPE)
                            .setTitleRes(R.string.logout)
                            .setContentRes(R.string.confirm, getString(R.string.logout).toLowerCase())
                            .setConfirm(R.string.logout)
                            .setConfirmListener(new Dialog.OnClickListener()
                            {
                                @Override
                                public void onClick(Dialog dialog)
                                {
                                    pref.edit().username().remove().apply();
                                    finish();
                                }
                            })
                            .show();
                    return true;
                }
            }
            return false;
        }
        finally
        {
            _drawerlayout.closeDrawer(GravityCompat.START);
        }
    }

    /****************************************/
    // TODO Event subscription
    /****************************************/

    @Subscribe
    public void onCheckPhotos(PhotosCheckEvent event)
    {
        _drawer.getMenu().findItem(R.id.files).setEnabled(event.hasPhotos());
    }
}
