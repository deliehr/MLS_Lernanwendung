package Components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Comprehensive.Application;
import Comprehensive.DatabaseHelper;
import Comprehensive.ExtendedOnTouchListener;
import Comprehensive.UsersAssessmentResponse;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;

/**
 * Class for assessment type HotspotInteraction (Hotspot).
 * Contains ?????????
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class HotspotAssessment extends Assessment implements AssessmentInterface  {
    //region object variables
    private PositionObjectInteraction positionObjectInteraction;
    private List<String> correctValueList = new ArrayList<String>();    // list with coordinates, which are correct
    private List<AreaMapEntry> areaMapEntryList = new ArrayList<AreaMapEntry>();
    private int areaMappingDefaultValue = 0;
    private int maxChoices = 1;    // max selectable choices
    private long returnIdInsertStatistic = -1;
    private int touchCount = 0;
    private List<Point> touchedPoints = new ArrayList<Point>();
    private int relativeLayoutId = -1;
    private int tvRemainingId = -1;
    // endregion

    // region listener
    ExtendedOnTouchListener touchListener;
    // endregion

    private void showPoint(float clickX, float clickY, View view) {
        // target layout
        RelativeLayout targetRelativeLayout = (RelativeLayout) ((Activity) this.getContext()).findViewById(this.relativeLayoutId);

        // layout params
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageParams.leftMargin = Math.round(clickX) - (this.positionObjectInteraction.getInnerObject().getWidth() / 2);
        imageParams.topMargin = Math.round(clickY) - this.positionObjectInteraction.getInnerObject().getHeight() / 2;

        // click image
        ImageView imageView = new ImageView(this.getContext());
        File imageFile = new File(((Activity) this.getContext()).getFilesDir() + Application.relativeWorkingDataDirectory + "media/assessments/" + this.getUuid() + "/" + this.positionObjectInteraction.getInnerObject().getData());
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView.setImageBitmap(imageBitmap);

        imageView.setLayoutParams(imageParams);

        // add click image to layout
        targetRelativeLayout.addView(imageView);
    }

    private void removeOnTouchListener(View view) {
        // after clicked all possible elements, remove on touch listener
        view.setOnTouchListener(null);
    }

    // region interface methods
    @Override
    public void displayAssessment(Context context, LinearLayout targetLayout) {
        // start statistic
        this.startStatisticEntry();

        // remove views
        targetLayout.removeAllViews();

        // reset touch count
        this.touchCount = 0;

        // define touchlistener
        this.touchListener = new ExtendedOnTouchListener(context, new java.lang.Object[]{this}) {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                HotspotAssessment assessment = (HotspotAssessment) this.getObjects()[0];

                if(assessment.touchCount < assessment.maxChoices) {
                    // increase counter
                    assessment.touchCount++;

                    // store coordinates
                    touchedPoints.add(new Point(Math.round(event.getX()), Math.round(event.getY())));

                    // show point
                    assessment.showPoint(event.getX(), event.getY(), view);

                    // decrease remaining
                    TextView tvRemaining = (TextView) ((Activity) this.getContext()).findViewById(assessment.tvRemainingId);
                    tvRemaining.setText(String.valueOf(assessment.maxChoices - assessment.touchCount));
                }

                if(assessment.touchCount == assessment.maxChoices) {
                    assessment.handleUserResponse(view);
                    assessment.removeOnTouchListener(view);
                }

                return false;
            }
        };

        // create assessment
        TextView tvTitle = new TextView(context);
        tvTitle.setText(this.getTitle());
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        tvTitle.setId(View.generateViewId());
        targetLayout.addView(tvTitle);

        // paragraphs
        this.displayItemBodyParagraphs(targetLayout);

        // prompt
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText(this.getPrompt());
        tvPrompt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        tvPrompt.setId(View.generateViewId());
        targetLayout.addView(tvPrompt);

        // user info, remaining clicks
        LinearLayout userInfoLayout = new LinearLayout(this.getContext());
        userInfoLayout.setId(View.generateViewId());
        userInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        userInfoLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvUserInfo = new TextView(this.getContext());
        tvUserInfo.setId(View.generateViewId());
        tvUserInfo.setText("Noch zu identifizierende Objekte: ");
        userInfoLayout.addView(tvUserInfo);

        TextView tvCountRemaining = new TextView(this.getContext());
        tvCountRemaining.setId(View.generateViewId());
        this.tvRemainingId = tvCountRemaining.getId();
        tvCountRemaining.setText(String.valueOf(this.getMaxChoices()));
        userInfoLayout.addView(tvCountRemaining);

        targetLayout.addView(userInfoLayout);

        // image border
        GradientDrawable imageBackground = new GradientDrawable();
        imageBackground.setShape(GradientDrawable.RECTANGLE);
        imageBackground.setStroke(1, Color.rgb(0, 0, 0));
        imageBackground.setColor(Color.rgb(255, 255, 255));

        // show large image
        RelativeLayout relativeLayout = new RelativeLayout(this.getContext());
        relativeLayout.setId(View.generateViewId());
        this.relativeLayoutId = relativeLayout.getId();
        relativeLayout.setClickable(true);
        relativeLayout.setFocusable(true);
        relativeLayout.setFocusableInTouchMode(true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeLayout.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imageView = new ImageView(this.getContext());
        File imageFile = new File(((Activity) this.getContext()).getFilesDir() + Application.relativeWorkingDataDirectory + "media/assessments/" + this.getUuid() + "/" + this.positionObjectInteraction.getOuterObject().getData());
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView.setImageBitmap(imageBitmap);
        imageView.setLayoutParams(imageParams);
        imageView.setBackground(imageBackground);

        // add large image to relative layout
        relativeLayout.addView(imageView);

        // add complete relative layout to target layout
        targetLayout.addView(relativeLayout);

        // add listener to image
        imageView.setOnTouchListener(this.touchListener);

        // show support
        Application.addSupportToLayout(this.getContext(), this);
    }

    @Override
    public void handleUserResponse(View view) {
        // compare touched points
        // iterate area mapping values
        UsersAssessmentResponse response = UsersAssessmentResponse.Wrong;

        for(int i=0;i < this.areaMapEntryList.size();i++) {
            AreaMapEntry entry = this.areaMapEntryList.get(i);

            // get coords
            int x = Integer.parseInt(entry.getCoords().split(",")[0]);
            int y = Integer.parseInt(entry.getCoords().split(",")[1]);
            int dev = Integer.parseInt(entry.getCoords().split(",")[2]);

            // distinguish shape
            // // TODO: 06.04.2017 shape implementation hotspot assessment

            if(i == 0) {
                // first round
                // search for clicked points
                for(Point p:this.touchedPoints) {
                    // x within p.x + deviation radius?
                    if(x >= p.x - dev && x <= p.x + dev) {
                        if(y >= p.y - dev && y < p.y + dev) {
                            // point found
                            response = UsersAssessmentResponse.Correct;
                            this.touchedPoints.remove(p);
                            break;
                        }
                    }
                }
            } else {
                // following round
                // search for clicked points
                for(Point p:this.touchedPoints) {
                    // x within p.x + deviation radius?
                    if(x >= p.x - dev && x <= p.x + dev) {
                        if(y >= p.y - dev && y < p.y + dev) {
                            // point found
                            if(response.equals(UsersAssessmentResponse.Wrong)) {
                                response = UsersAssessmentResponse.Partly_Correct;
                                this.touchedPoints.remove(p);
                                break;
                            }
                        } else {
                            // not found
                            if(response.equals(UsersAssessmentResponse.Correct)) {
                                response = UsersAssessmentResponse.Partly_Correct;
                            }
                        }
                    } else {
                        if(response.equals(UsersAssessmentResponse.Correct)) {
                            response = UsersAssessmentResponse.Partly_Correct;
                        }
                    }
                }
            }
        }

        // assessments to process
        if(!this.isAlreadySolved()) {
            ActivityLearn.assessmentsToProcess--;
        }

        // statistic
        this.completeStatisticEntry(response);

        // user response
        Application.showUserResponse(this.getContext(), (LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling), response);

        // mark solved
        this.setAlreadySolved(true);
        this.setHowSolved(response);

        // buttons
        if(ActivityLearn.assessmentsToProcess == 0) {
            // disable buttons
            Application.disableLearnButtons(this.getContext());

            // show summary button (only #assessments > 1)
            if(((ActivityLearn) this.getContext()).assessments.size() > 1) {
                Button b = new Button(this.getContext());
                b.setText(this.getContext().getString(R.string.activity_learn_message_all_assessments_solved));
                b.setOnClickListener(Application.getNewSummaryOnClickListener(this.getContext()));
                ((LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling)).addView(b);
            }
        }
    }

    @Override
    public void handleUserResponse(UsersAssessmentResponse response, View clickedView) {

    }

    // endregion

    // region constructors
    public HotspotAssessment()  {
        this.setIdentifier("positionObjects");
        this.setPositionObjectInteraction(new HotspotAssessment.PositionObjectInteraction());
    }

    public HotspotAssessment(PositionObjectInteraction poi, int amdv) {
        this.setIdentifier("positionObjects");
        this.setPositionObjectInteraction(poi);
        this.setAreaMappingDefaultValue(amdv);
    }
    // endregion

    // region getter & setter
    public int getMaxChoices() {
        return maxChoices;
    }

    public void setMaxChoices(byte maxChoices) {
        this.maxChoices = maxChoices;
    }

    public PositionObjectInteraction getPositionObjectInteraction() {
        return positionObjectInteraction;
    }

    public void setPositionObjectInteraction(PositionObjectInteraction positionObjectInteraction) {
        this.positionObjectInteraction = positionObjectInteraction;
    }

    public List<String> getCorrectValueList() {
        return correctValueList;
    }

    public void setCorrectValueList(List<String> correctValueList) {
        this.correctValueList = correctValueList;
    }

    public int getAreaMappingDefaultValue() {
        return areaMappingDefaultValue;
    }

    public void setAreaMappingDefaultValue(int areaMappingDefaultValue) {
        this.areaMappingDefaultValue = areaMappingDefaultValue;
    }

    public List<AreaMapEntry> getAreaMapEntryList() {
        return areaMapEntryList;
    }

    public void setAreaMapEntryList(List<AreaMapEntry> areaMapEntryList) {
        this.areaMapEntryList = areaMapEntryList;
    }
    // endregion

    public static class AreaMapEntry {
        //region object variables
        private String shape = "";
        private String coords = "";
        private int mappedValue = 0;
        // endregion

        // region constructors
        public AreaMapEntry() {
        }

        public AreaMapEntry(String shape, String coords, int mappedValue) {
            this.setShape(shape);
            this.setCoords(coords);
            this.setMappedValue(mappedValue);
        }
        // endregion

        // region getter & setter

        public String getShape() {
            return shape;
        }

        public void setShape(String shape) {
            this.shape = shape;
        }

        public String getCoords() {
            return coords;
        }

        public void setCoords(String coords) {
            this.coords = coords;
        }

        public int getMappedValue() {
            return mappedValue;
        }

        public void setMappedValue(int mappedValue) {
            this.mappedValue = mappedValue;
        }

        // endregion

        // region object methods

        // endregion
    }

    public static class Object {
        //region object variables
        private String type = "";
        private String data = "";
        private int width = -1;
        private int height = -1;
        // endregion

        // region constructors
        public Object()  {
        }

        public Object(String t, String d, int w, int h) {
            this.setType(t);
            this.setData(d);
            this.setWidth(w);
            this.setHeight(h);
        }
        // endregion

        // region getter & setter
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
        // endregion

        // region object methods

        // endregion
    }

    public static class PositionObjectInteraction {
        //region object variables
        private HotspotAssessment.Object outerObject;
        private HotspotAssessment.Object innerObject;
        // endregion

        // region constructors
        public PositionObjectInteraction()  {
            this.setOuterObject(new HotspotAssessment.Object());
            this.setInnerObject(new HotspotAssessment.Object());
        }

        public PositionObjectInteraction(HotspotAssessment.Object outerObject, HotspotAssessment.Object innerObject) {
            this.setOuterObject(outerObject);
            this.setInnerObject(innerObject);
        }
        // endregion

        // region getter & setter

        public Object getOuterObject() {
            return outerObject;
        }

        public void setOuterObject(Object outerObject) {
            this.outerObject = outerObject;
        }

        public Object getInnerObject() {
            return innerObject;
        }

        public void setInnerObject(Object innerObject) {
            this.innerObject = innerObject;
        }

        // endregion

        // region object methods
        // endregion
    }
}