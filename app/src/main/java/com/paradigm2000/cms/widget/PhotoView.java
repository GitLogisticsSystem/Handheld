package com.paradigm2000.cms.widget;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.app.Image;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_photo)
public class PhotoView extends FrameLayout
{
    @ViewById(R.id.imageview)
    SimpleDraweeView _imageview;
    @ViewById(R.id.checkbox)
    CheckBox _checkbox;
    @ViewById(R.id.textview)
    TextView _textview;

    public PhotoView(Context context)
    {
        super(context);
    }

    public void bind(Image image)
    {
        if (image.isFailed())
        {
            _imageview.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
            String resId = String.valueOf(com.paradigm2000.core.R.drawable.ic_error_24dp);
            _imageview.setImageURI(new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(resId).build());
        }
        else if (image.isLoaded())
        {
            _imageview.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(image.getUri())
                    .setResizeOptions(new ResizeOptions(128, 128))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(_imageview.getController())
                    .setImageRequest(request)
                    .build();
            _imageview.setController(controller);
        }
        else
        {
            _imageview.setImageURI((Uri) null);
        }
        _checkbox.setChecked(image.isChecked());
        _textview.setText(image.getTitle());
    }

    public void performCheck()
    {
        _checkbox.setChecked(!_checkbox.isChecked());
    }

    public void setCheckable(boolean checkable)
    {
        _checkbox.setVisibility(checkable? View.VISIBLE: View.GONE);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener)
    {
        _checkbox.setOnCheckedChangeListener(listener);
    }

    @Override
    public void setOnClickListener(OnClickListener listener)
    {
        getChildAt(0).setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener)
    {
        getChildAt(0).setOnLongClickListener(listener);
    }
}
