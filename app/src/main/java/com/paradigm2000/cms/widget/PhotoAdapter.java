package com.paradigm2000.cms.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.app.Image;
import com.paradigm2000.cms.app.MyPref_;
import com.paradigm2000.cms.app.PhotoGroup;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.gallery.GalleryUtils;
import com.paradigm2000.core.gallery.event.CheckSelectedEvent;
import com.paradigm2000.core.gallery.event.ModeChangeEvent;
import com.paradigm2000.core.io.FileCompat;
import com.paradigm2000.core.io.Folder;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.util.ArrayList;

@EBean
public class PhotoAdapter extends RecyclerViewAdapterBase<Image, FrameLayout>
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "PhotoAdapter";
    static final int SIZE = 640;
    static final int TYPE_BASE = 0;
    static final int TYPE_PHOTO = 1;
    static final int SPAN_COUNT = 2;

    @RootContext
    Context context;
    @Bean
    EventBus bus;
    @Pref
    MyPref_ pref;

    AddPhotoListener addPhotoListener;
    PhotoGroup photoGroup;
    boolean modeSelect;
    boolean enabled = true;

    public void setAddPhotoListener(AddPhotoListener listener)
    {
        addPhotoListener = listener;
    }

    public void setEnabled(boolean flag)
    {
        enabled = flag;
        notifyDataSetChanged();
    }

    public void setPhotoGroup(PhotoGroup photoGroup)
    {
        if (items.size() > 0)
        {
            notifyItemRangeRemoved(0, items.size());
            items.clear();
        }
        this.photoGroup = photoGroup;
        Folder folder = photoGroup.getFolder(context);
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;
        for (File file: files)
        {
            Uri uri = Uri.fromFile(file);
            Image image = new Image(uri);
            image.setLoaded();
            items.add(image);
            notifyItemInserted(items.size() - 1);
        }
    }

    public boolean isSelectMode()
    {
        return modeSelect;
    }

    public void setSelectMode(boolean modeSelect)
    {
        if (this.modeSelect == modeSelect) return;
        if (this.modeSelect = modeSelect)
        {
            for (Image image: items) image.setChecked(false);
            notifyItemRemoved(getItemCount());
        }
        else
        {
            notifyItemInserted(items.size());
        }
        notifyItemRangeChanged(0, getItemCount());
        bus.post(new ModeChangeEvent(modeSelect));

    }

    @Override
    public int getItemCount()
    {
        return items.size() + (!enabled || modeSelect? 0: 1);
    }

    @Override
    public int getItemViewType(int position)
    {
        return items.size() == position? TYPE_BASE: TYPE_PHOTO;
    }

    @Override
    protected FrameLayout onCreateItemView(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case TYPE_BASE:
            {
                return PhotoBaseView_.build(context);
            }
            default:
            {
                return PhotoView_.build(context);
            }
        }
    }

    @Override
    protected void onViewItemCreated(View itemView)
    {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        int minValue = Math.min(display.widthPixels, display.heightPixels);
        params.height = minValue / SPAN_COUNT;
        itemView.setLayoutParams(params);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<FrameLayout> holder, FrameLayout view, int position, Image item)
    {
        if (item == null) return;
        PhotoView photoView = (PhotoView) view;
        photoView.setOnCheckedChangeListener(null);
        File file = new FileCompat(item.getUri());
        int index = file.getName().lastIndexOf(".");
        item.setTitle(photoGroup.getName(position, BuildConfig.DEBUG? file.getName().substring(0, index): ""));
        photoView.bind(item);
        photoView.setCheckable(modeSelect);
        if (enabled)
        {
            photoView.setOnLongClickListener(new OnLongClickListener(position));
            photoView.setOnCheckedChangeListener(new OnCheckedChangeListener(position));
        }
    }

    @Override
    protected boolean onItemClick(FrameLayout container, View view, int position, Image item)
    {
        if (item == null)
        {
            if (addPhotoListener != null) addPhotoListener.addPhoto();
            return addPhotoListener != null;
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new FileCompat(item.getUri())) : item.getUri(), "image/*");
            context.startActivity(intent);
            return true;
        }
    }

    public void addImage(Uri uri)
    {
        Image image = new Image(uri);
        items.add(image);
        notifyItemInserted(items.size() - 1);
        notifyItemChanged(items.size());
        saveImage(image);
    }

    /****************************************/
    // TODO Check selected images
    /****************************************/

    @Background
    void checkSelected()
    {
        boolean result = false;
        for (Image image: items)
        {
            if (!image.isChecked()) continue;
            result = true;
            break;
        }
        afterCheck(result);
    }

    @UiThread
    void afterCheck(boolean hasSelected)
    {
        bus.post(new CheckSelectedEvent(hasSelected));
    }

    /****************************************/
    // TODO Delete selected images
    /****************************************/

    @Background
    public void commitDeletion()
    {
        int min = items.size();
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = min - 1; i >= 0; i -= 1)
        {
            Image image = items.get(i);
            if (!image.isChecked()) continue;
            File file = new FileCompat(image.getUri());
            if (!file.delete() && DEBUG) Log.w(TAG, "Fail to delete @" + file);
            items.remove(i);
            indices.add(i);
            min = i;
        }
        afterDeletion(min, indices);
    }

    @UiThread
    void afterDeletion(int min, ArrayList<Integer> indices)
    {
        for (Integer index: indices) notifyItemRemoved(index);
        if (getItemCount() > 0) bus.post(new CheckSelectedEvent(false));
        if (min < getItemCount()) notifyItemRangeChanged(min, getItemCount() - min);
        if (getItemCount() == 0) setSelectMode(false);
    }

    /****************************************/
    // TODO Save photo from camera
    /****************************************/

    @Background(serial="saveImage")
    void saveImage(Image image)
    {
        GalleryUtils.resize(context, image, new GalleryUtils.Options(640).exactSize().fixOrientation().quality(Integer.parseInt(pref.PhotoQuality().getOr("100"))));
        image.setLoaded();
        afterSave(image);
    }

    @UiThread
    void afterSave(Image image)
    {
        int index = items.indexOf(image);
        if (index > -1) notifyItemChanged(index);
    }

    /****************************************/
    // TODO Public interfaces
    /****************************************/

    public interface AddPhotoListener
    {
        void addPhoto();
    }

    /****************************************/
    // TODO Public classes
    /****************************************/

    class OnLongClickListener implements View.OnLongClickListener
    {
        int position;

        OnLongClickListener(int position)
        {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View view)
        {
            if (modeSelect) return false;
            setSelectMode(true);
            items.get(position).setChecked(true);
            bus.post(new CheckSelectedEvent(true));
            return true;
        }
    }

    class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        int position;

        OnCheckedChangeListener(int position)
        {
            this.position = position;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
        {
            items.get(position).setChecked(checked);
            if (!checked) checkSelected();
            else bus.post(new CheckSelectedEvent(true));
        }
    }
}
