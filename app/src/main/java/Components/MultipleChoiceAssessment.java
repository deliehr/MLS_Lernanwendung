package Components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import java.util.Collections;
import java.util.List;

import Comprehensive.App;
import Comprehensive.ExtendedCheckBox;
import Comprehensive.ExtendedOnClickListener;
import Comprehensive.UsersAssessmentResponse;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;

/**
 * Class for assessment type ChoiceInteraction (Multiplechoice).
 * Contains a correct response group, a mapping key-value group and one choiceInteraction item.
 * Inherit from class SingleChoiceAssessment.
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class MultipleChoiceAssessment extends SingleChoiceAssessment {
    // region object variables
    private List<String> correctValueList = new ArrayList<String>();    // list with identifiers, which are correct
    private KeyValueMappingGroup keyValueMappingGroup;
    private List<ExtendedCheckBox> checkBoxes = new ArrayList<ExtendedCheckBox>();
    private int buttonsClickedCount = 0;
    private List<ExtendedCheckBox> clickedCheckBoxes = new ArrayList<ExtendedCheckBox>();
    // endregion

    // region constructors
    public MultipleChoiceAssessment() {
        this.setIdentifier("choiceMultiple");
    }
    // endregion

    // region getter & setter

    public KeyValueMappingGroup getKeyValueMappingGroup() {
        return keyValueMappingGroup;
    }

    public void setKeyValueMappingGroup(KeyValueMappingGroup keyValueMappingGroup) {
        this.keyValueMappingGroup = keyValueMappingGroup;
    }

    public List<String> getCorrectValueList() {
        return this.correctValueList;
    }

    // endregion

    // region button onclick listener
    private ExtendedOnClickListener simpleChoiceOnClickListener;
    private ExtendedOnClickListener checkUserResponseOnClickListener;
    // endregion

    // handle user response
    @Override
    public void handleUserResponse(View view) {
        if(this.buttonsClickedCount == 0) {
            // only check user input if min one checkbox clicked
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.activity_learn_multiple_choice_no_checkboxes_clicked), Toast.LENGTH_SHORT).show();
            return;
        }

        // user response
        UsersAssessmentResponse response = UsersAssessmentResponse.Wrong;

        // get min points to have
        int lowerBound = this.getKeyValueMappingGroup().getLowerBound();
        int upperBound = this.getKeyValueMappingGroup().getUpperBound();
        int startValue = this.getKeyValueMappingGroup().getDefaultValue();

        // first, check directly correct value list
        for(ExtendedCheckBox ccb:this.clickedCheckBoxes) {
            // current checkbox = ccb
            // check if checkbox identifier is in correct value list
            boolean found = false;
            for(String identifier:this.getCorrectValueList()) {
                if(ccb.getIdentifier().equals(identifier)) {
                    found = true;

                    if(ccb.getParent() instanceof LinearLayout) {
                        ((LinearLayout) ccb.getParent()).setBackgroundResource(R.drawable.mc_solved_correct);
                    }

                    break;
                }
            }
            // found?
            if(found) {
                // identifier found in correct value list
                // search for mapped value
                for(int i=0;i < this.getKeyValueMappingGroup().getKeyValueMappingList().size();i++) {
                    if(ccb.getIdentifier().equals(this.getKeyValueMappingGroup().getKeyValueMappingList().get(i).getMapKey())) {
                        // found
                        startValue += this.getKeyValueMappingGroup().getKeyValueMappingList().get(i).getMappedValue();
                        break;
                    }
                }
            } else {
                // not found
                ((LinearLayout) ccb.getParent()).setBackgroundResource(R.drawable.mc_solved_falsely);

                // search for mapped value
                for(int i=0;i < this.getKeyValueMappingGroup().getKeyValueMappingList().size();i++) {
                    if(ccb.getIdentifier().equals(this.getKeyValueMappingGroup().getKeyValueMappingList().get(i).getMapKey())) {
                        // found
                        int mappedValue = Math.abs(this.getKeyValueMappingGroup().getKeyValueMappingList().get(i).getMappedValue());
                        startValue -= (mappedValue);
                        break;
                    }
                }
            }
        }

        // calc values
        if(startValue >= lowerBound && startValue <= upperBound) {
            response = UsersAssessmentResponse.Correct;
        }

        if(startValue > this.getKeyValueMappingGroup().getDefaultValue() && startValue < lowerBound) {
            response = UsersAssessmentResponse.Partly_Correct;
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.activity_learn_mc_required_points), Toast.LENGTH_SHORT).show();
        }

        this.handleUserResponseEnd(response);

        // disable check button
        this.checkButton.setEnabled(false);
    }

    @Override
    public void handleUserResponse(UsersAssessmentResponse response, View clickedView) {

    }

    // region object methods
    @Override
    public void displayAssessment(Context context, LinearLayout targetLayout) {
        // display start
        this.displayAssessmentStart(context, targetLayout);

        // reset clicked textboxed
        this.clickedCheckBoxes.clear();

        // click listener
        this.simpleChoiceOnClickListener = new ExtendedOnClickListener(context, new Object[]{this}) {
            @Override
            public void onClick(View view) {
                MultipleChoiceAssessment relatedAssessment = (MultipleChoiceAssessment) this.getObjects()[0];
                relatedAssessment.buttonsClickedCount++;
                relatedAssessment.clickedCheckBoxes.add((ExtendedCheckBox) view);

                if(relatedAssessment.buttonsClickedCount == relatedAssessment.getMaxChoices()) {
                    // all checkboxes pressed
                    relatedAssessment.handleUserResponse(view);
                }
            }
        };

        this.checkUserResponseOnClickListener = new ExtendedOnClickListener(context, new Object[]{this}) {
            @Override
            public void onClick(View view) {
                // user wants to check his response
                MultipleChoiceAssessment relatedAssessment = (MultipleChoiceAssessment) this.getObjects()[0];
                relatedAssessment.handleUserResponse(view);
            }
        };

        // selections
        LinearLayout.LayoutParams paramsSelections = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsSelections.setMargins(0, 10, 0, 10);

        // linear layout with all choices
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            // own linear layout
            LinearLayout choiceLayout = new LinearLayout(context);
            choiceLayout.setId(View.generateViewId());
            choiceLayout.setLayoutParams(params);
            choiceLayout.setOrientation(LinearLayout.HORIZONTAL);
            choiceLayout.setBackground(background);
            choiceLayout.setPadding(10, 10, 10, 10);


            // button with text and identifer
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            checkBoxParams.setMargins(0, 0, 10, 0);
            checkBoxParams.gravity = Gravity.CENTER_VERTICAL;

            // caption
            TextView tvCaption = new TextView(this.getContext());
            tvCaption.setText(currentChoice.getCaption());
            tvCaption.setPadding(5,5,5,5);

            // button
            ExtendedCheckBox simpleChoiceCheckBox = new ExtendedCheckBox(context);
            simpleChoiceCheckBox.setId(View.generateViewId());
            simpleChoiceCheckBox.setIdentifier(currentChoice.getIdentifier());
            simpleChoiceCheckBox.setLayoutParams(checkBoxParams);

            if(currentChoice.getCaption().equals("")) {
                simpleChoiceCheckBox.setText(this.getContext().getString(R.string.activity_learn_mc_label_answer) + " " + String.valueOf((i+1)));
            }

            simpleChoiceCheckBox.setAssessmentObject(this);
            simpleChoiceCheckBox.setOnClickListener(this.simpleChoiceOnClickListener);
            choiceLayout.addView(simpleChoiceCheckBox);

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

            // add text
            choiceLayout.addView(tvCaption);

            // add buttons to list
            this.checkBoxes.add(simpleChoiceCheckBox);

            // add choice layout to selection layout
            linearLayoutSelections.addView(choiceLayout);
        }

        // add check button
        this.checkButton = App.getNewCheckButton(linearLayoutSelections, this, this.checkUserResponseOnClickListener);

        targetLayout.addView(linearLayoutSelections);

        // show support
        App.addSupportToLayout(this.getContext(), this);
    }
    // endregion

    /**
     * Inner class for storing a complete mapping item.
     * Contains a lowerBound, an upperBound and a default value.
     */
    public static class KeyValueMappingGroup {
        // region object variables
        private int lowerBound;
        private int upperBound;
        private int defaultValue;
        private List<KeyValueMapping> keyValueMappingList = new ArrayList<KeyValueMapping>();
        // endregion

        // region constructors

        /**
         * Instanciating a new key-value mapping group. Require parameters lowerBound, upperBound and defaultValue.
         * Makes a check at first: if lowerBound <= upperBound, everything will be fine.
         *
         * @param lowerBound Lower bound of mapping group.
         * @param upperBound Upper bound of mapping group.
         * @param defaultValue Default value of mapping group.
         *
         * @throws Exception If lowerBound > upperBound
         */
        public KeyValueMappingGroup(int lowerBound, int upperBound, int defaultValue) throws Exception {
            if(lowerBound <= upperBound) {
                this.setLowerBound(lowerBound);
                this.setUpperBound(upperBound);
                this.setDefaultValue(defaultValue);
            } else {
                throw new Exception("Lower bound must be <= than upper bound!");
            }
        }
        // endregion

        // region object methods
        public void addKeyValueMapping(KeyValueMapping kvm) {
            this.keyValueMappingList.add(kvm);
        }
        // endregion

        // region getter & setter

        public List<KeyValueMapping> getKeyValueMappingList() {
            return keyValueMappingList;
        }

        public void setKeyValueMappingList(List<KeyValueMapping> keyValueMappingList) {
            this.keyValueMappingList = keyValueMappingList;
        }

        public int getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(int lowerBound) {
            this.lowerBound = lowerBound;
        }

        public int getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(int upperBound) {
            this.upperBound = upperBound;
        }

        public int getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
        }

        // endregion

        /**
         * Inner class for representating a single mapping item.
         * Contains a key (identifier) and corresponding int value.
         */
        public static class KeyValueMapping {
            // region object variables
            private String mapKey; // mapped identifier, belongs to a value in correct value list
            private int mappedValue;    // mapped int value
            // endregion

            // region contructors
            public KeyValueMapping(String mapKey, int mappedValue) {
                this.setMapKey(mapKey);
                this.setMappedValue(mappedValue);
            }
            // endregion

            // region getter & setter
            public String getMapKey() {
                return mapKey;
            }

            public int getMappedValue() {
                return mappedValue;
            }

            public void setMapKey(String mapKey) {
                this.mapKey = mapKey;
            }

            public void setMappedValue(int mappedValue) {
                this.mappedValue = mappedValue;
            }
            // endregion
        }
    }
}
