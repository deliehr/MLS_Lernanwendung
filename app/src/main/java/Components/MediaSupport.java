package Components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import Comprehensive.Application;
import Comprehensive.ExtendedButton;
import Comprehensive.ExtendedOnClickListener;
import it.liehr.mls_app.ActivityFullscreenVideo;
import it.liehr.mls_app.R;

/**
 * Class for media support
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class MediaSupport extends Support implements SupportInterface  {
    // region object variables
    private String mediaSource;
    private String prompt;
    // endregion

    // region interface

    @Override
    public void displaySupport(Context context, LinearLayout targetLinearLayout) {
        // build source path
        String sourcePath = ((Activity) context).getFilesDir() + Application.relativeWorkingDataDirectory + "media/support/" + this.getUuid() + "/" + this.getMediaSource();
        File mediaFile = new File(sourcePath);

        if(mediaFile.exists()) {
            // switch type
            switch (this.getIdentifier()) {
                case "image":
                    try {
                        // show image
                        ImageView imageView = new ImageView(context);

                        ScrollView.LayoutParams imageParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        Bitmap imageBitmap = BitmapFactory.decodeFile(mediaFile.getAbsolutePath());
                        imageView.setImageBitmap(imageBitmap);
                        imageView.setLayoutParams(imageParams);
                        //imageView.setBackground(imageBackground);

                        targetLinearLayout.addView(imageView);
                    } catch (Exception e) {
                        Log.e("Error", "Media support method displaySupport: " + e.getMessage());
                    }

                    break;
                case "video":
                    // show video
                    try {
                        GradientDrawable bg = new GradientDrawable();
                        bg.setStroke(1, Color.rgb(0, 0, 0));

                        VideoView videoView = new VideoView(context);
                        //videoView.setBackground(bg);
                        videoView.setId(View.generateViewId());
                        videoView.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
                        videoView.setVideoPath(mediaFile.getAbsolutePath());
                        //videoView.requestFocus();
                        //videoView.start();
                        targetLinearLayout.addView(videoView);

                        MediaController controller = new MediaController(context);
                        controller.setAnchorView(videoView);
                        videoView.setMediaController(controller);

                        // buttons
                        Button startVideoButton = new Button(context);
                        startVideoButton.setId(View.generateViewId());
                        startVideoButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        startVideoButton.setText(R.string.activity_learn_support_button_start_video);
                        startVideoButton.setOnClickListener(new ExtendedOnClickListener(context, new Object[] {videoView}) {
                            @Override
                            public void onClick(View view) {
                                super.onClick(view);

                                // get video view
                                VideoView videoView = (VideoView) this.getObjects()[0];
                                videoView.start();
                            }
                        });
                        targetLinearLayout.addView(startVideoButton);

                        Button fullscreenVideoButton = new Button(context);
                        fullscreenVideoButton.setId(View.generateViewId());
                        fullscreenVideoButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        fullscreenVideoButton.setText(R.string.activity_learn_support_button_video_fullscreen);
                        fullscreenVideoButton.setOnClickListener(new ExtendedOnClickListener(context, new Object[] {context, mediaFile.getAbsolutePath()}) {
                            @Override
                            public void onClick(View view) {
                                super.onClick(view);

                                // get media file path
                                Intent intent = new Intent((Activity) ((Context) this.getObjects()[0]), ActivityFullscreenVideo.class);
                                intent.putExtra("it.liehr.mls_app.VIDEOPATH", (String) this.getObjects()[1]);
                                ((Activity) ((Context) this.getObjects()[0])).startActivity(intent);
                            }
                        });
                        targetLinearLayout.addView(fullscreenVideoButton);
                    } catch (Exception e) {
                        Log.e("Error", "Media support method displaySupport: " + e.getMessage());
                    }

                    break;
            }
        } else {
            Log.e("Error", "Media Support display support: media file not exist");
        }
    }

    // endregion

    // region object methods
    public MediaSupport(String uuid, String identifier) {
        this.setPrompt("");
        this.setMediaSource("");
        this.setIdentifier(identifier);
        this.setUuid(uuid);
    }

    public MediaSupport(String uuid, String mediaSource, String prompt, String identifier) {
        this.setPrompt(prompt);
        this.setMediaSource(mediaSource);
        this.setIdentifier(identifier);
        this.setUuid(uuid);
    }
    // endregion

    // region getter & setter
    public String getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    // endregion
}
