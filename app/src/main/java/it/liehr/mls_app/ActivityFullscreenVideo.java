package it.liehr.mls_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class ActivityFullscreenVideo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get extra
        Intent intent = this.getIntent();
        String videoPath = intent.getStringExtra("it.liehr.mls_app.VIDEOPATH");

        // set video view
        VideoView videoView = (VideoView) this.findViewById(R.id.videoView);
        videoView.setVideoPath(videoPath);

        // controller
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);

        // start video
        videoView.start();
    }
}
