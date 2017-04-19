package Components;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Comprehensive.Application;
import Comprehensive.DatabaseHelper;
import Comprehensive.ExtendedButton;
import Comprehensive.ExtendedOnClickListener;
import Comprehensive.UsersAssessmentResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * Main abstract class Assessment.
 * Other assessment classes, like SingleChoiceAssessment, MultipleChoiceAssessment or HotspotAssessment inherit from this class.
 *
 * @author Dominik Liehr
 * @version 0.05
 */
public class Assessment {
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
                        File imageFile = new File(((Activity) this.getContext()).getFilesDir() + Application.relativeWorkingDataDirectory + "media/assessments/" + this.getUuid() + "/" + imageSource);

                        if(imageFile.exists()) {
                            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                            imageView.setImageBitmap(imageBitmap);
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

    public void displayAssessmentSummary() {

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