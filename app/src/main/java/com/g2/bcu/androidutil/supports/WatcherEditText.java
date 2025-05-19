package com.g2.bcu.androidutil.supports;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class WatcherEditText extends AppCompatEditText {

    private TextWatcher curWatcher = null;

    public WatcherEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public WatcherEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WatcherEditText(Context context) {
        super(context);
    }

    public void setWatcher(TextWatcher watch) {
        if (curWatcher != null)
            removeTextChangedListener(curWatcher);
        addTextChangedListener(curWatcher = watch);
    }

    public void setWatcher(Function0<Unit> watch) {
        setWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                watch.invoke();
            }
        });
    }
}
