package Comprehensive;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import android.widget.VideoView;

/**
 * Extension of the video view with auto resize functionality
 */
public class ExtendedVideoView extends VideoView {
    private int videoWidth = 0;
    private int videoHeight = 0;

    public ExtendedVideoView(Context context) {
        super(context);
    }

    public ExtendedVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int width, int height) {
        this.setVideoWidth(width);
        this.setVideoHeight(height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(videoWidth, videoHeight);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // region getter & setter
    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }
    // endregion
}
