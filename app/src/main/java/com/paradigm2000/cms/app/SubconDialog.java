package com.paradigm2000.cms.app;

import android.content.Context;
import android.text.InputFilter;
import android.widget.FrameLayout;

import com.paradigm2000.cms.R;
import com.paradigm2000.cms.widget.AutoCompleteView;
import com.paradigm2000.core.dialog.Dialog2;

public class SubconDialog extends Dialog2 {
    OnSelectListener mListener;
    String[] mSubcons;

    public SubconDialog(Context context, String[] subcons) {
        super(context, Dialog2.CUSTOM_TYPE);
        mSubcons = subcons;
        restore();
    }

    private void restore() {
        setTitleRes(R.string.app_name);
        setContentRes(R.string.subc);
        setConfirm(R.string.submit);
        setCancel(android.R.string.cancel);
        setCloseOnClick(false);
        showCancel(true);
        setView(R.layout.dialog_subc, new Dialog2.CustomListener() {
            AutoCompleteView _textview;

            @Override
            public void onLayout(Dialog2 dialog, FrameLayout parent) {
                _textview = (AutoCompleteView) parent.findViewById(R.id.textview);
                _textview.setFilters(new InputFilter[] { new InputFilter.AllCaps() });
                _textview.setItems(mSubcons);
            }

            @Override
            public void onConfirm(Dialog2 dialog, FrameLayout parent) {
                if (_textview.length() == 0) return;
                if (mListener != null) mListener.onSelect(dialog, _textview.getText().toString());
            }

            @Override
            public void onCancel(Dialog2 dialog, FrameLayout parent) {
                dialog.dismiss();
            }
        });
    }

    public SubconDialog setListener(OnSelectListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnSelectListener {
        void onSelect(Dialog2 dialog, String subcon);
    }
}
