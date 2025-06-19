package com.hfad.veloxmechanics.utility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NoSwipePager extends ViewPager {
    private boolean enabled;

    public NoSwipePager(@NonNull Context context) {
        super(context);
        this.enabled = true;
    }

    public NoSwipePager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.enabled)
            return super.onTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.enabled)
            return super.onInterceptTouchEvent(ev);
        return false;
    }
    public void setPagingEnabled(boolean enabled){
        this.enabled = enabled;
    }
}
