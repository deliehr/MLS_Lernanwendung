package Components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Comprehensive.App;
import Comprehensive.ExtendedButton;
import Comprehensive.UsersAssessmentResponse;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;

/**
 * Class for assessment type ChoiceInteraction (Singlechoice).
 * Contains a correct response group and one choiceInteraction item
 *
 * @author Dominik Liehr
 * @version 0.06
 */
public class SingleChoiceAssessment extends Assessment {
    //region object variables
    private String correctValueIdentifier;
    private List<SimpleChoice> simpleChoiceList = new ArrayList<SimpleChoice>();    // list with choice elements
    private boolean shuffleChoices = false; // default value for shuffling
    private int maxChoices = 1;    // max selectable choices
    private String responseIdentifier;  // response identifer
    private List<ExtendedButton> buttons = new ArrayList<ExtendedButton>();
    // endregion

    // region constructors
    public SingleChoiceAssessment()  {
        this.setIdentifier("choice");
    }

    /**
     * Another constructor of SingleChoiceAssessment.
     * Initialises shuffleChoices variable.
     * @param shuffleChoices Option for shuffling the choices.
     */
    public SingleChoiceAssessment(boolean shuffleChoices) {
        this.setIdentifier("choice");
        this.setShuffleChoices(shuffleChoices);
    }
    // endregion

    // region getter & setter

    public int getMaxChoices() {
        return maxChoices;
    }

    public void setMaxChoices(int maxChoices) {
        this.maxChoices = maxChoices;
    }

    public List<SimpleChoice> getSimpleChoiceList() {
        return this.simpleChoiceList;
    }

    public boolean isShuffleChoices() {
        return shuffleChoices;
    }

    public void setShuffleChoices(boolean shuffleChoices) {
        this.shuffleChoices = shuffleChoices;
    }

    public String getResponseIdentifier() {
        return responseIdentifier;
    }

    public void setResponseIdentifier(String responseIdentifier) { this.responseIdentifier = responseIdentifier; }

    public String getCorrectValueIdentifier() {
        return correctValueIdentifier;
    }

    public void setCorrectValueIdentifier(String correctValueIdentifier) {
        this.correctValueIdentifier = correctValueIdentifier;
    }

    // endregion

    // region button onclick listener
    protected View.OnClickListener simpleChoiceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ExtendedButton clickedButton = (ExtendedButton) view;
            SingleChoiceAssessment relatedAssessment = (SingleChoiceAssessment) clickedButton.getAssessmentObject();

            // validate user response
            if(relatedAssessment.getCorrectValueIdentifier().equals(clickedButton.getIdentifier())) {
                // correct
                relatedAssessment.handleUserResponse(UsersAssessmentResponse.Correct, clickedButton);
            } else {
                // wrong
                relatedAssessment.handleUserResponse(UsersAssessmentResponse.Wrong, clickedButton);
            }
        }
    };
    // endregion

    // handle user response
    @Override
    public void handleUserResponse(View view) {

    }

    @Override
    public void handleUserResponse(UsersAssessmentResponse response, View clickedView) {
        // user response type
        String responseString = response.toString();

        // disable others buttons
        for(ExtendedButton button:this.buttons) {
            button.setEnabled(false);
        }

        switch (responseString) {
            case "Correct":
                // mark correct button
                clickedView.setEnabled(false);
                clickedView.setBackgroundResource(R.drawable.correct_answer);

                break;
            case "Wrong":
                // mark false button
                clickedView.setEnabled(false);
                clickedView.setBackgroundResource(R.drawable.wrong_answer);

                break;
        }

        this.handleUserResponseEnd(response);
    }

    // region object methods
    /**
     * Checks, if an specified identifier (key) in the correct value list exists
     * @param context Context
     * @param targetLayout target LinearLayout
     * @return True, if identifier (key) exists, false, if not
     */
    @Override
    public void displayAssessment(Context context, LinearLayout targetLayout) {
        // display start
        this.displayAssessmentStart(context, targetLayout);

        // selections
        LinearLayout.LayoutParams paramsSelections = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsSelections.setMargins(0, 10, 0, 10);

        LinearLayout linearLayoutSelections = new LinearLayout(context);
        linearLayoutSelections.setId(View.generateViewId());
        linearLayoutSelections.setOrientation(LinearLayout.VERTICAL);
        linearLayoutSelections.setLayoutParams(paramsSelections);

        // background
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(255,255,255));
        background.setStroke(2, Color.rgb(200,200,200));
        background.setShape(GradientDrawable.RECTANGLE);

        // assessment ordering
        List<Integer> assessmentOrder = new ArrayList<Integer>();

        // standard order
        for(int i=0;i < this.getSimpleChoiceList().size();i++) {
            assessmentOrder.add(i);
        }

        // check if shuffle is true
        if(this.isShuffleChoices()) {
            Collections.shuffle(assessmentOrder);
        }

        // selection items
        for(int i=0;i < this.getSimpleChoiceList().size();i++) {
            // get index to take
            int indexToTake = assessmentOrder.get(i);

            // current choice
            SimpleChoice currentChoice = this.getSimpleChoiceList().get(indexToTake);

            // layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // own linear layout
            LinearLayout choiceLayout = new LinearLayout(context);
            choiceLayout.setId(View.generateViewId());
            choiceLayout.setLayoutParams(params);
            choiceLayout.setOrientation(LinearLayout.HORIZONTAL);
            choiceLayout.setBackground(background);
            choiceLayout.setPadding(10, 10, 10, 10);

            // button with text and identifer
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //buttonParams.weight = 1;

            ExtendedButton simpleChoiceButton = new ExtendedButton(context);
            simpleChoiceButton.setId(View.generateViewId());
            simpleChoiceButton.setIdentifier(currentChoice.getIdentifier());
            simpleChoiceButton.setLayoutParams(buttonParams);

            // button
            if(currentChoice.getCaption().equals("")) {
                // caption is empty
                simpleChoiceButton.setText("Antwort " + String.valueOf((i+1)));
            } else {
                simpleChoiceButton.setText(currentChoice.getCaption());
            }

            simpleChoiceButton.setAssessmentObject(this);
            simpleChoiceButton.setOnClickListener(this.simpleChoiceOnClickListener);
            choiceLayout.addView(simpleChoiceButton);

            // contains single choice an image?
            if(currentChoice.getImageSource() != null) {
                File imageFile = new File(this.getContext().getFilesDir() + App.relativeWorkingDataDirectory + "media/assessments/" + this.getUuid() + "/" + currentChoice.getImageSource());
                if(imageFile.exists()) {
                    // image exists, show
                    Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    ImageView image = new ImageView(this.getContext());
                    image.setAdjustViewBounds(true);
                    image.setMaxWidth(200);
                    image.setImageBitmap(imageBitmap);
                    choiceLayout.addView(image);
                }
            }

            // add buttons to list
            this.buttons.add(simpleChoiceButton);

            // add choice layout to selection layout
            linearLayoutSelections.addView(choiceLayout);
        }

        targetLayout.addView(linearLayoutSelections);

        // show support
        App.addSupportToLayout(this.getContext(), this);
    }
    // endregion
}