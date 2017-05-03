package Components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Comprehensive.App;
import Comprehensive.DatabaseHelper;
import Comprehensive.ExtendedButton;
import Comprehensive.UsersAssessmentResponse;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;

import org.apache.commons.lang3.StringUtils;

/**
 * Main abstract class Assessment.
 * Other assessment classes, like SingleChoiceAssessment, MultipleChoiceAssessment or HotspotAssessment inherit from this class.
 *
 * @author Dominik Liehr
 * @version 0.05
 */
public class Assessment implements AssessmentInterface {
    // region object variables
    private int id = -1;    // database id
    private String uuid = "";
    private int creationTimestamp = -1;
    private String categoryTags = "";
    private String title = "";
    private boolean adaptive = false;
    private boolean timeDependent = false;
    private String prompt = "";
    private String identifier = "";
    private Context context;
    private List<String> itemBodyParagraphList = new ArrayList<String>();
    private long returnIdInsertStatistic = -1;
    private boolean alreadySolved = false;
    private UsersAssessmentResponse howSolved = UsersAssessmentResponse.Wrong;
    protected ExtendedButton checkButton;
    // endregion

    // region object methods
    public void startStatisticEntry() {
        // statistic
        long startTimestamp = System.currentTimeMillis()/1000;
        this.returnIdInsertStatistic = (new DatabaseHelper(context)).insertStatisticStartTimestamp(this.getId(), startTimestamp);
    }

    protected void displayAssessmentStart(Context context, LinearLayout targetLayout) {
        // scroll up
        this.assessmentScrollUp();

        // start statistic
        this.startStatisticEntry();

        // remove views
        targetLayout.removeAllViews();

        // assessment type
        LinearLayout layoutAssessmentType = new LinearLayout(this.getContext());
        layoutAssessmentType.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutAssessmentType.setBackgroundResource(R.drawable.label_assessment_type);

        String assessmentType = this.getIdentifier();
        switch (assessmentType) {
            case "choice": { assessmentType = "Singlechoice"; break; }
            case "choiceMultiple": { assessmentType = "Multiplechoice"; break; }
            case "positionObjects": { assessmentType = "Hotspot"; break; }
            case "table": { assessmentType = this.getContext().getString(R.string.keyword_table_assessment_type); break; }
            default: { assessmentType = "Drag'n'Drop"; break; }
        }
        assessmentType += "-" + this.getContext().getString(R.string.keyword_assessment);

        SpannableString assessmentTypeSpan = new SpannableString(assessmentType);
        assessmentTypeSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, assessmentType.length(), 0);
        assessmentTypeSpan.setSpan(new RelativeSizeSpan(1.5f), 0, assessmentType.length(), 0);

        TextView tvAssessmentType = new TextView(context);
        tvAssessmentType.setId(View.generateViewId());
        tvAssessmentType.setText(assessmentTypeSpan);
        tvAssessmentType.setPadding(10,10,10,10);

        layoutAssessmentType.addView(tvAssessmentType);

        TextView tvAssessmentTypeDescription = new TextView(this.getContext());
        tvAssessmentTypeDescription.setId(View.generateViewId());
        tvAssessmentTypeDescription.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        tvAssessmentTypeDescription.setGravity(Gravity.RIGHT);
        tvAssessmentTypeDescription.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

        assessmentType = this.getIdentifier();
        switch (assessmentType) {
            case "choice": { tvAssessmentTypeDescription.setText(R.string.assessment_type_description_choice); break; }
            case "choiceMultiple": { tvAssessmentTypeDescription.setText(R.string.assessment_type_description_choiceMultiple); break; }
            case "positionObjects": { tvAssessmentTypeDescription.setText(R.string.assessment_type_description_hotspot); break; }
            case "table": { tvAssessmentTypeDescription.setText(R.string.assessment_type_description_table); break; }
            default: { tvAssessmentTypeDescription.setText(R.string.assessment_type_description_drag); break; }
        }

        layoutAssessmentType.addView(tvAssessmentTypeDescription);

        targetLayout.addView(layoutAssessmentType);

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
    }

    protected void handleUserResponseEnd(UsersAssessmentResponse response) {
        // assessments to process
        if(!this.isAlreadySolved()) {
            ActivityLearn.assessmentsToProcess--;
        }

        // statistic
        this.completeStatisticEntry(response);

        // user response
        App.showUserResponse(this.getContext(), (LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling), response);

        // mark solved
        this.setAlreadySolved(true);
        this.setHowSolved(response);

        // buttons
        if(ActivityLearn.assessmentsToProcess == 0) {
            // disable buttons
            App.disableLearnButtons(this.getContext());

            // show summary button (only #assessments > 1)
            if(((ActivityLearn) this.getContext()).assessments.size() > 1) {
                Button b = new Button(this.getContext());
                b.setText(this.getContext().getString(R.string.activity_learn_message_all_assessments_solved));
                b.setOnClickListener(App.getNewSummaryOnClickListener(this.getContext()));
                ((LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling)).addView(b);
            }
        }
    }

    private void assessmentScrollUp() {
        // scroll up
        final ScrollView scrollView = (ScrollView) ((Activity) context).findViewById(R.id.scrollViewLearnActivity);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
    }

    public void completeStatisticEntry(UsersAssessmentResponse response) {
        long processedTimestamp = System.currentTimeMillis()/1000;

        if(!(new DatabaseHelper(this.getContext())).completeStatisticEntry(this.returnIdInsertStatistic, processedTimestamp, response)) {
            Log.e("Error", "Assessment completeStatisticEntry");
        }
    }

    public void displayItemBodyParagraphs(LinearLayout targetLayout) {
        for(String paragraph:this.getItemBodyParagraphList()) {
            // contains image?
            if(paragraph.contains("[img:")) {
                try {
                    FlexboxLayout imageLayout = new FlexboxLayout(this.getContext());
                    imageLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
                    imageLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
                    imageLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_STRETCH);

                    String completeParagraph = paragraph;

                    for(int i=0;i < StringUtils.countMatches(completeParagraph, "[img:");i++) {
                        int positionStart = paragraph.indexOf("[img:");
                        int positionEnd = paragraph.indexOf("]", positionStart) + 1;

                        if(positionStart != 0) {
                            // there was text before
                            TextView tvTmp = new TextView(this.getContext());
                            tvTmp.setText(paragraph.substring(0, positionStart));
                            imageLayout.addView(tvTmp);
                        }

                        // image
                        String imageString = paragraph.substring(positionStart, positionEnd);
                        imageString = imageString.replace("[", "");
                        imageString = imageString.replace("]", "");
                        String[] imageParts = imageString.split(":");
                        String imageSource = imageParts[1];

                        ImageView imageView = new ImageView(this.getContext());
                        File imageFile = new File(((Activity) this.getContext()).getFilesDir() + App.relativeWorkingDataDirectory + "media/assessments/" + this.getUuid() + "/" + imageSource);

                        if(imageFile.exists()) {
                            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                            imageView.setImageBitmap(imageBitmap);

                            FlexboxLayout.LayoutParams imageParams = new FlexboxLayout.LayoutParams(App.convertDpToPx((Activity) this.getContext(), imageBitmap.getWidth()) / 2, App.convertDpToPx((Activity) this.getContext(), imageBitmap.getHeight()) / 2);

                            imageView.setLayoutParams(imageParams);
                        }

                        imageLayout.addView(imageView);


                        // take remain string
                        paragraph = paragraph.substring(positionEnd, paragraph.length());
                    }

                    if(paragraph.length() > 0) {
                        TextView tvTmp = new TextView(this.getContext());
                        tvTmp.setText(paragraph);
                        imageLayout.addView(tvTmp);
                    }

                    targetLayout.addView(imageLayout);
                } catch (Exception e) {
                    Log.e("Error", "Assessment displayItemBodyParagraphs(): " + e.getMessage());

                    TextView tvParagraph = new TextView(context);
                    tvParagraph.setId(View.generateViewId());
                    tvParagraph.setText(paragraph);
                    tvParagraph.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    targetLayout.addView(tvParagraph);
                }
            } else {
                // direct add textview without linear layout
                TextView tvParagraph = new TextView(context);
                tvParagraph.setId(View.generateViewId());
                tvParagraph.setText(paragraph);
                tvParagraph.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                targetLayout.addView(tvParagraph);
            }
        }
    }
    // endregion

    // region interface

    @Override
    public void displayAssessment(Context context, LinearLayout targetLayout) {

    }

    @Override
    public void handleUserResponse(View view) {

    }

    @Override
    public void handleUserResponse(UsersAssessmentResponse response, View clickedView) {

    }

    // endregion

    // region getter & setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(int creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getCategoryTags() {
        return categoryTags;
    }

    public void setCategoryTags(String categoryTags) {
        this.categoryTags = categoryTags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAdaptive() {
        return adaptive;
    }

    public void setAdaptive(boolean adaptive) {
        this.adaptive = adaptive;
    }

    public boolean isTimeDependent() {
        return timeDependent;
    }

    public void setTimeDependent(boolean timeDependent) {
        this.timeDependent = timeDependent;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<String> getItemBodyParagraphList() {
        return itemBodyParagraphList;
    }

    public void setItemBodyParagraphList(List<String> paragraphList) {
        this.itemBodyParagraphList = paragraphList;
    }

    public boolean isAlreadySolved() {
        return alreadySolved;
    }

    public void setAlreadySolved(boolean alreadySolved) {
        this.alreadySolved = alreadySolved;
    }

    public long getReturnIdInsertStatistic() {
        return returnIdInsertStatistic;
    }

    public void setReturnIdInsertStatistic(long returnIdInsertStatistic) {
        this.returnIdInsertStatistic = returnIdInsertStatistic;
    }

    public UsersAssessmentResponse getHowSolved() {
        return howSolved;
    }

    public void setHowSolved(UsersAssessmentResponse howSolved) {
        this.howSolved = howSolved;
    }

    // endregion
}