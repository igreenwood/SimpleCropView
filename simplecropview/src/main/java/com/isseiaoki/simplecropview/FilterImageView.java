package com.isseiaoki.simplecropview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FilterImageView extends CropImageView {
    private static final String TAG = "FilterImageView";

    private FilterMode mFilterMode = FilterMode.NO_FILTER;

    public FilterImageView(Context context) {
        super(context);
    }

    public FilterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public enum FilterMode{
        NO_FILTER(0), mFilter(1);

        private final int id;

        FilterMode(int id){
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public LoadRequest load(Uri sourceUri) {
        return new LoadRequest(this, sourceUri);
    }
}
