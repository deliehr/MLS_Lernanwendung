package Comprehensive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import java.util.List;

import Components.Assessment;
import Components.DragAssessment;
import Components.HotspotAssessment;
import Components.MediaSupport;
import Components.MultipleChoiceAssessment;
import Components.RelatedGroup;
import Components.SelectionSupport;
import Components.SimpleChoice;
import Components.SingleChoiceAssessment;
import Components.Support;
import Components.Table;
import Components.TableAssessment;
import Components.TableSupport;
import Components.TextboxSupport;
import it.liehr.mls_app.R;
import Comprehensive.DatabaseHelper.TableNames;
import Comprehensive.DatabaseHelper.ForeignKeyNames;
import Components.DragAssessment.DragItem;

/**
 * Class for import assessments, related groups and support into the database
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class Import_Obj2Db {
    // region object variables
    private Context context;
    private String used_url;
    private int insertUpdateTimestamp;
    // endregion

    // region constructors
    public Import_Obj2Db(Context con, String used_url) {
        this.setContext(con);
        this.setUsedUrl(used_url);

        // set current timestamp
        Long tsLong = System.currentTimeMillis() / 1000;
        this.insertUpdateTimestamp = Integer.valueOf(tsLong.toString());
    }
    // endregion

    // region getter & setter

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getUsedUrl() {
        return used_url;
    }

    public void setUsedUrl(String used_url) {
        this.used_url = used_url;
    }

    // endregion

    // region import starting processes
    public boolean importAssessments(List<Assessment> assessmentList, DatabaseHelper helper) {
        // list not empty
        if(assessmentList.size() > 0) {
            // iterate each assessment
            for(int i=0;i < assessmentList.size();i++) {
                try {
                    // check first, if assessment already exists
                    int assessmentId = helper.assessmentAlreadyExistingByUuid(assessmentList.get(i).getUuid());
                    if(assessmentId >= 1) {
                        // compare timestamps
                        int timestamp_assessment_package = assessmentList.get(i).getCreationTimestamp();
                        int timestamp_assessment_database = helper.getCreationTimestampOfAssessmentById(helper.assessmentAlreadyExistingByUuid(assessmentList.get(i).getUuid()));

                        if(timestamp_assessment_package > timestamp_assessment_database) {
                            // assessment in package is newer
                            // set id of assessment
                            assessmentList.get(i).setId(assessmentId);

                            // update assessment
                            helper.getReadableDatabase().beginTransaction();
                            this.updateAssessment(assessmentList.get(i), helper);
                            helper.getReadableDatabase().setTransactionSuccessful();
                            helper.getReadableDatabase().endTransaction();
                        }
                    } else {
                        // assessment does not exist, insert
                        helper.getReadableDatabase().beginTransaction();
                        this.insertAssessment(assessmentList.get(i), helper);
                        helper.getReadableDatabase().setTransactionSuccessful();
                        helper.getReadableDatabase().endTransaction();
                    }
                } catch (SQLException se) {
                    Log.e("Error", "SQL Error in importer obj2db: " + se.getMessage());
                    Log.e("Error", "SQL Error in importer obj2db (File / Class / Method / Linenumber): " + se.getStackTrace()[0].getFileName() + " / " + se.getStackTrace()[0].getClassName() + " / " + se.getStackTrace()[0].getMethodName() + " / " + se.getStackTrace()[0].getLineNumber());
                    helper.getReadableDatabase().endTransaction();
                    return false;
                } catch (Exception e) {
                    Log.e("Error", "Error in importer obj2db: " + e.getMessage());
                    Log.e("Error", "Error in importer obj2db (File / Class / Method / Linenumber): " + e.getStackTrace()[0].getFileName() + " / " + e.getStackTrace()[0].getClassName() + " / " + e.getStackTrace()[0].getMethodName() + " / " + e.getStackTrace()[0].getLineNumber());
                    helper.getReadableDatabase().endTransaction();
                    return false;
                }
            }

            // end reached, return true
            return true;
        }

        return false;
    }

    public boolean importRelated(List<RelatedGroup> groupList, DatabaseHelper helper) {
        // list not empty
        if(groupList.size() > 0) {
            // iterate each group
            for(int i=0;i < groupList.size();i++) {
                // current group
                RelatedGroup currentGroup = groupList.get(i);

                try {
                    // update or insert?
                    int relatedGroupId = helper.relatedGroupAlreadyExistingByUuid(currentGroup.getUuid());
                    currentGroup.setId(relatedGroupId);

                    // related group existing?
                    if(relatedGroupId >= 1) {
                        // existing, update?
                        int timestampRelatedDatabase = helper.getCreationTimestampOfRelatedGroupById(relatedGroupId);
                        int timestampRelatedPackage = currentGroup.getCreationTimestamp();

                        if(timestampRelatedPackage > timestampRelatedDatabase) {
                            // package data is newer
                            // update
                            // delete group items
                            helper.getReadableDatabase().delete(TableNames.RELATED_ITEM, ForeignKeyNames.RELATED + " = ?", new String[] {String.valueOf(relatedGroupId)});

                            // delete category tags
                            helper.getReadableDatabase().delete(TableNames.CATEGORY_TAGS, ForeignKeyNames.RELATED + " = ?", new String[] {String.valueOf(relatedGroupId)});

                            // insert group items
                            for(String uuid:currentGroup.getItemUuids()) {
                                helper.insertRelatedGroupItem(relatedGroupId, uuid);
                            }

                            // insert category tags
                            for(String tag:currentGroup.getCategoryTags().split(",")) {
                                helper.insertCategoryTagWithRelatedId(relatedGroupId, tag);
                            }

                            // update t_related
                            ContentValues values = new ContentValues();
                            values.put("uuid", currentGroup.getUuid());
                            values.put("creation_timestamp", currentGroup.getCreationTimestamp());
                            values.put("title", currentGroup.getTitle());
                            values.put("shuffle", currentGroup.getShuffle());
                            helper.getReadableDatabase().update(TableNames.RElATED, values, "id = ?", new String[] {String.valueOf(relatedGroupId)});
                        }
                    } else {
                        // not existing, insert
                        // insert related group
                        long relatedGroupInsertId = helper.insertRelatedGroup(currentGroup.getUuid(), currentGroup.getCreationTimestamp(), currentGroup.getTitle(), currentGroup.getShuffle());

                        // insert related group items
                        for(String uuid:currentGroup.getItemUuids()) {
                            long relatedItemInsertId = helper.insertRelatedGroupItem(relatedGroupInsertId, uuid);
                        }

                        // category tags
                        for(String tag:currentGroup.getCategoryTags().split(",")) {
                            long tagInsertId = helper.insertCategoryTagWithRelatedId(relatedGroupInsertId, tag);
                        }
                    }
                } catch (SQLException se) {
                    Log.e("Error", "SQL Error in importer obj2db: " + se.getMessage());
                    return false;
                } catch (Exception e) {
                    Log.e("Error", "Error in importer obj2db: " + e.getMessage());
                    return false;
                }
            }

            // end reached, return true
            return true;
        }

        return false;
    }

    public boolean importSupports(List<Support> supportList, DatabaseHelper helper) {
        // list not empty
        if(supportList.size() > 0) {
            // iterate each support element
            for(int i=0;i < supportList.size();i++) {
                // current support element
                Support currentSupport = supportList.get(i);

                try {
                    // insert support element
                    switch (currentSupport.getIdentifier()) {
                        case "textbox": this.importTextboxSupport((TextboxSupport) currentSupport, helper); break;
                        case "image":
                        case "video": this.importMediaSupport((MediaSupport) currentSupport, helper); break;
                        case "selection": this.importSelectionSupport((SelectionSupport) currentSupport, helper); break;
                        case "table": this.importTableSupport((TableSupport) currentSupport, helper); break;
                    }
                } catch (SQLException se) {
                    Log.e("Error", "SQL Error in importer obj2db: " + se.getMessage());
                    return false;
                } catch (Exception e) {
                    Log.e("Error", "Error in importer obj2db: " + e.getMessage());
                    return false;
                }
            }

            // end reached, return true
            return true;
        }

        return false;
    }
    // endregion

    // region structional
    private void insertAssessment(Assessment assessment, DatabaseHelper helper) throws Exception {
        // get assessment type / identifier
        String assessmentIdentifier = assessment.getIdentifier();

        // get last id from inserting assessment
        long assessmentInsertId = helper.insertAssessmentItem(assessment);

        // distinguish between assessment types
        switch (assessmentIdentifier) {
            case "choice":
                this.importSingleChoiceAssessment((SingleChoiceAssessment) assessment, helper, assessmentInsertId);
                break;
            case "choiceMultiple":
                this.importMultipleChoiceAssessment((MultipleChoiceAssessment) assessment, helper, assessmentInsertId);
                break;
            case "positionObjects":
                this.importHotspotAssessment((HotspotAssessment) assessment, helper, assessmentInsertId);
                break;
            case "table":
                this.importTableAssessment((TableAssessment) assessment, helper, assessmentInsertId);
                break;
            case "dragndropTable":
                this.importDragAssessment((DragAssessment) assessment, helper, assessmentInsertId);
                break;
        }

        // save category tags
        for (String tag : assessment.getCategoryTags().split(",")) {
            long tagInsertId = helper.insertCategoryTagWithAssessmenId(assessmentInsertId, tag);
        }

        // save to import table
        long importInsertId = helper.insertImportTimestamp(assessmentInsertId, this.insertUpdateTimestamp, this.used_url);
    }

    private void updateAssessment(Assessment assessment, DatabaseHelper helper) throws Exception {
        // distinguish between type
        String assessmentType = assessment.getIdentifier();
        switch (assessmentType) {
            case "choice":
                this.updateSingleChoiceAssessment((SingleChoiceAssessment) assessment, helper);
                break;
            case "choiceMultiple":
                this.updateMultipleChoiceAssessment((MultipleChoiceAssessment) assessment, helper);
                break;
            case "positionObjects":
                this.updateHotspotAssessment((HotspotAssessment) assessment, helper);
                break;
            case "table":
                this.updateTableAssessment((TableAssessment) assessment, helper);
                break;
            case "dragndropTable":
                this.updateDragAssessment((DragAssessment) assessment, helper);
                break;
        }

        // save to import table
        long updateInsertId = helper.insertUpdateTimestamp(assessment.getId(), this.insertUpdateTimestamp, this.used_url);
    }
    // endregion

    // region import methods per assessment type
    private void importSingleChoiceAssessment(SingleChoiceAssessment assessment, DatabaseHelper helper, long assessmentItemId) throws Exception {
        // insert response declaration
        long responseDeclarationInsertId = helper.insertResponseDeclaration(assessmentItemId, "choice", "single", "identifier");

        // insert correct response
        long correctResponseInsertId = helper.insertCorrectResponse(responseDeclarationInsertId);

        // insert values
        helper.insertValue(correctResponseInsertId, assessment.getCorrectValueIdentifier(), null, null);

        // itembody
        long itemBodyInsertId = helper.insertItemBody(assessmentItemId);

        // itembody paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long paragraphInsertId = helper.insertItemBodyParagraph(itemBodyInsertId, paragraph);
        }

        // choice interaction
        long choiceInteractionInsertId = helper.insertChoiceInteraction(itemBodyInsertId, "RESPONSE", assessment.isShuffleChoices(), assessment.getMaxChoices(), assessment.getPrompt());

        // simple choices
        for(SimpleChoice choice:assessment.getSimpleChoiceList()) {
            helper.insertSimpleChoice(choiceInteractionInsertId, choice);
        }
    }

    private void importMultipleChoiceAssessment(MultipleChoiceAssessment assessment, DatabaseHelper helper, long assessmentItemId) throws Exception {
        // insert response declaration
        long responseDeclarationInsertId = helper.insertResponseDeclaration(assessmentItemId, "choiceMultiple", "multiple", "identifier");

        // insert correct response
        long correctResponseInsertId = helper.insertCorrectResponse(responseDeclarationInsertId);

        // insert values
        for(String value:assessment.getCorrectValueList()) {
            helper.insertValue(correctResponseInsertId, value, null, null);
        }

        // insert mapping
        long mappingInsertId = helper.insertMapping(responseDeclarationInsertId, assessment.getKeyValueMappingGroup());

        // insert mapping entries
        for(MultipleChoiceAssessment.KeyValueMappingGroup.KeyValueMapping mapEntry:assessment.getKeyValueMappingGroup().getKeyValueMappingList()) {
            helper.insertMapEntry(mappingInsertId, mapEntry);
        }

        // itembody
        long itemBodyInsertId = helper.insertItemBody(assessmentItemId);

        // itembody paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long paragraphInsertId = helper.insertItemBodyParagraph(itemBodyInsertId, paragraph);
        }

        // choice interaction
        long choiceInteractionInsertId = helper.insertChoiceInteraction(itemBodyInsertId, "RESPONSE", assessment.isShuffleChoices(), assessment.getMaxChoices(), assessment.getPrompt());

        // simple choices
        for(SimpleChoice choice:assessment.getSimpleChoiceList()) {
            helper.insertSimpleChoice(choiceInteractionInsertId, choice);
        }
    }

    private void importHotspotAssessment(HotspotAssessment assessment, DatabaseHelper helper, long assessmentItemId) throws Exception {
        // insert response declaration
        long responseDeclarationInsertId = helper.insertResponseDeclaration(assessmentItemId, "positionObjects", "multiple", "point");

        // insert correct response
        long correctResponseInsertId = helper.insertCorrectResponse(responseDeclarationInsertId);

        // insert values
        for(String value:assessment.getCorrectValueList()) {
            String[] twoValues = value.split(" ");
            helper.insertValue(correctResponseInsertId, twoValues[0], twoValues[1], null);
        }

        // area mapping
        long areaMappingInsertId = helper.insertAreaMapping(responseDeclarationInsertId, assessment.getAreaMappingDefaultValue());

        // area map entries
        for(HotspotAssessment.AreaMapEntry entry:assessment.getAreaMapEntryList()) {
            helper.insertAreaMapEntry(areaMappingInsertId, entry.getShape(), entry.getCoords(), entry.getMappedValue());
        }

        // itembody
        long itemBodyInsertId = helper.insertItemBody(assessmentItemId);

        // itembody paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long paragraphInsertId = helper.insertItemBodyParagraph(itemBodyInsertId, paragraph);
        }

        // objects
        long innerObjectId = helper.insertObject(assessment.getPositionObjectInteraction().getInnerObject());
        long outerObjectId = helper.insertObject(assessment.getPositionObjectInteraction().getOuterObject());

        // hotspot interaction
        long hotspotInteractionInsertId = helper.insertHotspotInteraction(itemBodyInsertId, innerObjectId, outerObjectId, assessment.getMaxChoices(), assessment.getPrompt());

    }

    private void importTableAssessment(TableAssessment assessment, DatabaseHelper helper, long assessmentItemId) throws Exception {
        // insert response declaration
        long responseDeclarationInsertId = helper.insertResponseDeclaration(assessmentItemId, "RESPONSE", null, "identifier");

        // insert correct response
        long correctResponseInsertId = helper.insertCorrectResponse(responseDeclarationInsertId);

        // insert values
        for(TableAssessment.Value value:assessment.getValueList()) {
            helper.insertValue(correctResponseInsertId, value.getValueContent(), null, value.getCellIdentifier());
        }

        // itembody
        long itemBodyInsertId = helper.insertItemBody(assessmentItemId);

        // itembody paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long paragraphInsertId = helper.insertItemBodyParagraph(itemBodyInsertId, paragraph);
        }

        // insert table interaction
        long tableInteractionInsertId = helper.insertTableInteraction(itemBodyInsertId, "RESPONSE", assessment.getPrompt());

        // insert tables
        for(Table table:assessment.getTableList()) {
            helper.insertTable("RESPONSE", tableInteractionInsertId, -1, -1, table);
        }
    }

    private void importDragAssessment(DragAssessment assessment, DatabaseHelper helper, long assessmentItemId) throws Exception {
        // itembody
        long itemBodyInsertId = helper.insertItemBody(assessmentItemId);

        // itembody paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long paragraphInsertId = helper.insertItemBodyParagraph(itemBodyInsertId, paragraph);
        }

        // insert drag interaction
        long dragInteractionInsertId = helper.insertDragInteraction(itemBodyInsertId, assessment.getPrompt(), assessment.getDragMode().toString());

        // insert tables
        for(Table table:assessment.getTableList()) {
            helper.insertTable("RESPONSE", -1, dragInteractionInsertId, -1, table);
        }

        // drag items
        for(DragAssessment.DragItem item:assessment.getDragItemList()) {
            long dragItemInsertId = helper.insertDragItem(dragInteractionInsertId, item.getIdentifier(), item.getItemValue());
        }
    }
    // endregion

    // region import methods per support type
    private void importTextboxSupport(TextboxSupport support, DatabaseHelper helper) throws Exception {
        // update, if existing
        Cursor result = helper.getReadableDatabase().query(false, TableNames.SUPPORT, new String[] {"id", "uuid", "assessment_uuid", "creation_timestamp", "identifier"}, "uuid = ?", new String[] {support.getUuid()}, null, null, null, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            // support existing, update?
            int supportId = result.getInt(0);
            int timestampSupportDatabase = result.getInt(3);

            if(support.getCreationTimestamp() > timestampSupportDatabase) {
                // support in package is new, update
                ContentValues values = new ContentValues();
                values.put("uuid", support.getUuid());
                values.put("assessment_uuid", support.getAssessmentUuid());
                values.put("creation_timestamp", support.getCreationTimestamp());
                values.put("identifier", support.getIdentifier());

                // update
                int updateSupportCount = helper.getReadableDatabase().update(TableNames.SUPPORT, values, "id = ?", new String[] {String.valueOf(supportId)});

                // update t_support_textbox
                values = new ContentValues();
                values.put("textbox_content", support.getTextBoxContent());

                // update
                int updateSupportTextboxCount = helper.getReadableDatabase().update(TableNames.SUPPORT_TEXTBOX, values, ForeignKeyNames.SUPPORT + " = ?", new String[] {String.valueOf(supportId)});
            }
        } else {
            // not existing, insert
            long supportInsertId = helper.insertSupport(support.getUuid(), support.getAssessmentUuid(), support.getCreationTimestamp(), support.getIdentifier());

            long textboxInsertId = helper.insertSupportTextbox(supportInsertId, support.getTextBoxContent());
        }
    }

    private void importMediaSupport(MediaSupport support, DatabaseHelper helper) throws Exception {
        // update, if existing
        Cursor result = helper.getReadableDatabase().query(false, TableNames.SUPPORT, new String[] {"id", "uuid", "assessment_uuid", "creation_timestamp", "identifier"}, "uuid = ?", new String[] {support.getUuid()}, null, null, null, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            // support existing, update?
            int supportId = result.getInt(0);
            int timestampSupportDatabase = result.getInt(3);

            if(support.getCreationTimestamp() > timestampSupportDatabase) {
                // support in package is new, update
                ContentValues values = new ContentValues();
                values.put("uuid", support.getUuid());
                values.put("assessment_uuid", support.getAssessmentUuid());
                values.put("creation_timestamp", support.getCreationTimestamp());
                values.put("identifier", support.getIdentifier());

                // update
                int updateSupportCount = helper.getReadableDatabase().update(TableNames.SUPPORT, values, "id = ?", new String[] {String.valueOf(supportId)});

                // update t_support_media
                values = new ContentValues();
                values.put("media_type", support.getIdentifier());
                values.put("media_source", support.getMediaSource());
                values.put("prompt", support.getPrompt());

                // update
                int updateSupportMediaCount = helper.getReadableDatabase().update(TableNames.SUPPORT_MEDIA, values, ForeignKeyNames.SUPPORT + " = ?", new String[] {String.valueOf(supportId)});
            }
        } else {
            // not existing, insert
            // insert support
            long supportInsertId = helper.insertSupport(support.getUuid(), support.getAssessmentUuid(), support.getCreationTimestamp(), support.getIdentifier());

            // insert media
            long mediaInsertId = helper.insertSupportMedia(supportInsertId, support.getIdentifier(), support.getMediaSource(), support.getPrompt());
        }
    }

    private void importSelectionSupport(SelectionSupport support, DatabaseHelper helper) throws Exception {
        // update, if existing
        String query = "SELECT t_support.id, t_support.creation_timestamp, t_support_selection.id FROM t_support, t_support_selection WHERE t_support.id = t_support_selection.fk_t_support_id AND t_support.uuid = '[uuid]'";
        query = query.replace("[uuid]", support.getUuid());
        Cursor result = helper.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            // support existing, update?
            int supportId = result.getInt(0);
            int timestampSupportDatabase = result.getInt(1);
            int supportSelectionId = result.getInt(2);

            if(support.getCreationTimestamp() > timestampSupportDatabase) {
                // support in package is new, update
                ContentValues values = new ContentValues();
                values.put("uuid", support.getUuid());
                values.put("assessment_uuid", support.getAssessmentUuid());
                values.put("creation_timestamp", support.getCreationTimestamp());
                values.put("identifier", support.getIdentifier());

                int updateSupportCount = helper.getReadableDatabase().update(TableNames.SUPPORT, values, "id = ?", new String[] {String.valueOf(supportId)});

                // update t_support_selection
                values = new ContentValues();
                values.put("prompt", support.getPrompt());

                int updateSupportSelection = helper.getReadableDatabase().update(TableNames.SUPPORT_SELECTION, values, ForeignKeyNames.SUPPORT + " = ?", new String[] {String.valueOf(supportId)});

                // delete selection items
                int deleteItemCount = helper.getReadableDatabase().delete(TableNames.SELECTION_ITEM, ForeignKeyNames.SUPPORT_SELECTION + " = ?", new String[] {String.valueOf(supportSelectionId)});

                // insert selection items
                for(String selection:support.getSelections()) {
                    long insertSelectionItemId = helper.insertSupportSelectionItem(supportSelectionId, selection);
                }
            }
        } else {
            // not existing, insert
            // insert support
            long supportInsertId = helper.insertSupport(support.getUuid(), support.getAssessmentUuid(), support.getCreationTimestamp(), support.getIdentifier());

            // insert selection
            long supportSelectionInsertId = helper.insertSupportSelection(supportInsertId, support.getPrompt());

            // insert selections
            for(String select: support.getSelections()) {
                long selectItemInsertId = helper.insertSupportSelectionItem(supportSelectionInsertId, select);
            }
        }
    }

    private void importTableSupport(TableSupport support, DatabaseHelper helper) throws Exception {
        // update, if existing
        String query = "SELECT t_support.id AS [s_id], t_support.creation_timestamp AS [s_cr], t_support_table.id AS [t_st_id] FROM t_support, t_support_table WHERE t_support.id = t_support_table.fk_t_support_id AND t_support.uuid = '[uuid]'";
        query = query.replace("[uuid]", support.getUuid());
        Cursor result = helper.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            // support existing, update?
            int supportId = result.getInt(0);
            int timestampSupportDatabase = result.getInt(1);
            int supportTableId = result.getInt(2);

            if(support.getCreationTimestamp() > timestampSupportDatabase) {
                // support in package is new, update
                ContentValues values = new ContentValues();
                values.put("uuid", support.getUuid());
                values.put("assessment_uuid", support.getAssessmentUuid());
                values.put("creation_timestamp", support.getCreationTimestamp());
                values.put("identifier", support.getIdentifier());

                // update
                int updateSupportCount = helper.getReadableDatabase().update(TableNames.SUPPORT, values, "id = ?", new String[] {String.valueOf(supportId)});

                // delete tables
                // rows and cells will be also deleted because of foreign key constraints cascade
                query = "DELETE FROM t_table WHERE t_table.id IN (SELECT t_table.id FROM t_table INNER JOIN t_support_table ON t_table.fk_t_support_table_id = t_support_table.id WHERE t_support_table.id = [id]);";
                query = query.replace("[id]", String.valueOf(supportTableId));
                helper.getReadableDatabase().execSQL(query);

                // insert tables
                helper.insertTable("RESPONSE", -1, -1, supportTableId, support.getTable());
            }
        } else {
            // not existing, insert
            // insert support
            long supportInsertId = helper.insertSupport(support.getUuid(), support.getAssessmentUuid(), support.getCreationTimestamp(), support.getIdentifier());

            // insert selection
            long supportTableInsertId = helper.insertSupportTable(supportInsertId, support.getPrompt());

            // insert table
            helper.insertTable("RESPONSE", -1, -1, supportTableInsertId, support.getTable());
        }
    }
    // endregion

    // region update methods per assessment type

    private void updateSingleChoiceAssessment(SingleChoiceAssessment assessment, DatabaseHelper helper) throws Exception {
        // region preparation
        // get ids query
        String idsQuery = App.getStringContentFromRawFile(this.context, R.raw.single_choice_ids);

        // append query with id
        idsQuery += String.valueOf(assessment.getId() + ";");

        // query
        Cursor result = helper.getReadableDatabase().rawQuery(idsQuery, null);
        result.moveToFirst();

        // get ids
        int assessmentId = result.getInt(0);
        int responseDeclarationId = result.getInt(1);
        int correctResponseId = result.getInt(2);
        int itemBodyId = result.getInt(3);
        int choiceInteractionId = result.getInt(4);
        // endregion

        // region delete
        // delete tags
        int deleteTagsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.CATEGORY_TAGS, DatabaseHelper.ForeignKeyNames.ASSESSMENT_ITEM + " = ?", new String[] {String.valueOf(assessmentId)});

        // delete paragraphs
        int deleteParagraphsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.PARAGRAPH, DatabaseHelper.ForeignKeyNames.ITEM_BODY + " = ?", new String[] {String.valueOf(itemBodyId)});

        // delete simple choices
        int deleteChoicesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.SIMPLE_CHOICE, DatabaseHelper.ForeignKeyNames.CHOICE_INTERACTION + " = ?", new String[] {String.valueOf(choiceInteractionId)});

        // delete values
        int deleteValuesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.VALUE, DatabaseHelper.ForeignKeyNames.CORRECT_RESPONSE + " = ?", new String[] {String.valueOf(correctResponseId)});
        // endregion

        // region insert
        // insert tags
        for(String tag:assessment.getCategoryTags().split(",")) {
            long insertCategoryId = helper.insertCategoryTagWithAssessmenId(assessmentId, tag);
        }

        // insert paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long insertParagraphId = helper.insertItemBodyParagraph(itemBodyId, paragraph);
        }

        // insert simple choices
        for(SimpleChoice choice:assessment.getSimpleChoiceList()) {
            long insertChoiceId = helper.insertSimpleChoice(choiceInteractionId, choice);
        }

        // insert value
        long insertValueId = helper.insertValue(correctResponseId, assessment.getCorrectValueIdentifier(), "", "");
        // endregion

        // region update
        // assessment item
        ContentValues values = new ContentValues();
        values.put("uuid", assessment.getUuid());
        values.put("creation_timestamp", assessment.getCreationTimestamp());
        values.put("identifier", assessment.getIdentifier());
        values.put("title", assessment.getTitle());
        values.put("adaptive", assessment.isAdaptive());
        values.put("time_dependent", assessment.isTimeDependent());

        int updateAssessmentCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.ASSESSMENT_ITEM, values, "id = ?", new String[] {String.valueOf(assessmentId)});

        // response declaration
        values = new ContentValues();
        values.put("identifier", "RESPONSE");
        values.put("cardinality", "multiple");
        values.put("base_type", "identifier");

        int updateResponseDeclarationCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.RESPONSE_DECLARATION, values, "id = ?", new String[] {String.valueOf(responseDeclarationId)});

        // choice interaction
        values = new ContentValues();
        values.put("response_identifier", "RESPONSE");
        values.put("shuffle", assessment.isShuffleChoices());
        values.put("max_choices", assessment.getMaxChoices());
        values.put("prompt", assessment.getPrompt());

        int updateChoiceInteractionCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.CHOICE_INTERACTION, values, "id = ?", new String[] {String.valueOf(choiceInteractionId)});
        // endregion
    }

    private void updateMultipleChoiceAssessment(MultipleChoiceAssessment assessment, DatabaseHelper helper) throws Exception {
        // region preparation
        // get ids query
        String idsQuery = App.getStringContentFromRawFile(this.context, R.raw.multiple_choice_ids);

        // append query with id
        idsQuery += String.valueOf(assessment.getId() + ";");

        // query
        Cursor result = helper.getReadableDatabase().rawQuery(idsQuery, null);
        result.moveToFirst();

        // get ids
        int assessmentId = result.getInt(0);
        int responseDeclarationId = result.getInt(1);
        int correctResponseId = result.getInt(2);
        int mappingId = result.getInt(3);
        int itemBodyId = result.getInt(4);
        int choiceInteractionId = result.getInt(5);
        // endregion

        // region delete
        // delete tags
        int deleteTagsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.CATEGORY_TAGS, DatabaseHelper.ForeignKeyNames.ASSESSMENT_ITEM + " = ?", new String[] {String.valueOf(assessmentId)});

        // delete paragraphs
        int deleteParagraphsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.PARAGRAPH, DatabaseHelper.ForeignKeyNames.ITEM_BODY + " = ?", new String[] {String.valueOf(itemBodyId)});

        // delete simple choices
        int deleteSimpleChoicesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.SIMPLE_CHOICE, DatabaseHelper.ForeignKeyNames.CHOICE_INTERACTION + " = ?", new String[] {String.valueOf(choiceInteractionId)});

        // delete map entries
        int deleteMapEntriesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.MAP_ENTRY, DatabaseHelper.ForeignKeyNames.MAPPING + " = ?", new String[] {String.valueOf(mappingId)});

        // delete values
        int deleteValuesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.VALUE, DatabaseHelper.ForeignKeyNames.CORRECT_RESPONSE + " = ?", new String[] {String.valueOf(correctResponseId)});
        // endregion

        // region insert
        // insert tags
        for(String tag:assessment.getCategoryTags().split(",")) {
            long insertTagId = helper.insertCategoryTagWithAssessmenId(assessmentId, tag);
        }

        // insert paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long insertParagraphId = helper.insertItemBodyParagraph(itemBodyId, paragraph);
        }

        // insert simple choices
        for(SimpleChoice choice:assessment.getSimpleChoiceList()) {
            long insertSimpleChoiceId = helper.insertSimpleChoice(choiceInteractionId, choice);
        }

        // insert map entries
        for(MultipleChoiceAssessment.KeyValueMappingGroup.KeyValueMapping mapping:assessment.getKeyValueMappingGroup().getKeyValueMappingList()) {
            long insertMapEntryId = helper.insertMapEntry(mappingId, mapping);
        }

        // insert values
        for(String value:assessment.getCorrectValueList()) {
            long insertValueId = helper.insertValue(correctResponseId, value, "", "");
        }
        // endregion

        // region update
        // assessment item
        ContentValues values = new ContentValues();
        values.put("uuid", assessment.getUuid());
        values.put("creation_timestamp", assessment.getCreationTimestamp());
        values.put("identifier", assessment.getIdentifier());
        values.put("title", assessment.getTitle());
        values.put("adaptive", assessment.isAdaptive());
        values.put("time_dependent", assessment.isTimeDependent());

        int updateAssessmentCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.ASSESSMENT_ITEM, values, "id = ?", new String[] {String.valueOf(assessmentId)});

        // response declaration
        values = new ContentValues();
        values.put("identifier", "RESPONSE");
        values.put("cardinality", "multiple");
        values.put("base_type", "identifier");

        int updateResponseDeclarationCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.RESPONSE_DECLARATION, values, "id = ?", new String[] {String.valueOf(responseDeclarationId)});

        // mapping
        values = new ContentValues();
        values.put("lower_bound", assessment.getKeyValueMappingGroup().getLowerBound());
        values.put("upper_bound", assessment.getKeyValueMappingGroup().getUpperBound());
        values.put("default_value", assessment.getKeyValueMappingGroup().getDefaultValue());

        int updateMappingCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.MAPPING, values, "id = ?", new String[] {String.valueOf(mappingId)});

        // choice interaction
        values = new ContentValues();
        values.put("response_identifier", "RESPONSE");
        values.put("shuffle", assessment.isShuffleChoices());
        values.put("max_choices", assessment.getMaxChoices());
        values.put("prompt", assessment.getPrompt());

        int updateChoiceInteractionCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.CHOICE_INTERACTION, values, "id = ?", new String[] {String.valueOf(choiceInteractionId)});
        // endregion
    }

    private void updateHotspotAssessment(HotspotAssessment assessment, DatabaseHelper helper) throws Exception {
        // region preparation
        // get ids query
        String idsQuery = App.getStringContentFromRawFile(this.context, R.raw.hotspot_ids);

        // append query with id
        idsQuery += String.valueOf(assessment.getId() + ";");

        // query
        Cursor result = helper.getReadableDatabase().rawQuery(idsQuery, null);
        result.moveToFirst();

        // get ids
        int assessmentId = result.getInt(0);
        int responseDeclarationId = result.getInt(1);
        int correctResponseId = result.getInt(2);
        int areaMappingId = result.getInt(3);
        int itemBodyId = result.getInt(4);
        int hotspotInteractionId = result.getInt(5);
        int hotspotOuterObjectId = result.getInt(6);
        int hotspotInnerObjectId = result.getInt(7);
        // endregion

        // region delete
        // delete tags
        int deleteTagsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.CATEGORY_TAGS, DatabaseHelper.ForeignKeyNames.ASSESSMENT_ITEM + " = ?", new String[] {String.valueOf(assessmentId)});

        // delete paragraphs
        int deleteParagraphsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.PARAGRAPH, DatabaseHelper.ForeignKeyNames.ITEM_BODY + " = ?", new String[] {String.valueOf(itemBodyId)});

        // delete area map entries
        int deleteAreaMapEntriesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.AREA_MAP_ENTRY, DatabaseHelper.ForeignKeyNames.AREA_MAPPING + " = ?", new String[] {String.valueOf(areaMappingId)});

        // delete values
        int deleteValuesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.VALUE, DatabaseHelper.ForeignKeyNames.CORRECT_RESPONSE + " = ?", new String[] {String.valueOf(correctResponseId)});
        // endregion

        // region insert
        // insert tags
        for(String tag:assessment.getCategoryTags().split(",")) {
            long insertTagId = helper.insertCategoryTagWithAssessmenId(assessmentId, tag);
        }

        // insert paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long insertParagraphId = helper.insertItemBodyParagraph(itemBodyId, paragraph);
        }

        // insert area map entries
        for(HotspotAssessment.AreaMapEntry entry:assessment.getAreaMapEntryList()) {
            long insertMapEntryId = helper.insertAreaMapEntry(areaMappingId, entry.getShape(), entry.getCoords(), entry.getMappedValue());
        }

        // insert values
        for(String value:assessment.getCorrectValueList()) {
            long insertValueId = helper.insertValue(correctResponseId, value.split(" ")[0], value.split(" ")[1], "");
        }
        // endregion

        // region update
        // assessment item
        ContentValues values = new ContentValues();
        values.put("uuid", assessment.getUuid());
        values.put("creation_timestamp", assessment.getCreationTimestamp());
        values.put("identifier", assessment.getIdentifier());
        values.put("title", assessment.getTitle());
        values.put("adaptive", assessment.isAdaptive());
        values.put("time_dependent", assessment.isTimeDependent());

        int updateAssessmentCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.ASSESSMENT_ITEM, values, "id = ?", new String[] {String.valueOf(assessmentId)});

        // response declaration
        values = new ContentValues();
        values.put("identifier", "RESPONSE");
        values.put("cardinality", "multiple");
        values.put("base_type", "point");

        int updateResponseDeclarationCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.RESPONSE_DECLARATION, values, "id = ?", new String[] {String.valueOf(responseDeclarationId)});

        // area mapping
        values = new ContentValues();
        values.put("default_value", assessment.getAreaMappingDefaultValue());

        int updateAreaMappingCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.AREA_MAPPING, values, "id = ?", new String[] {String.valueOf(areaMappingId)});

        // hotspot interaction
        values = new ContentValues();
        values.put("response_identifier", "RESPONSE");
        values.put("max_choices", assessment.getMaxChoices());
        values.put("prompt", assessment.getPrompt());

        int updateHotspotCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.HOTSPOT_INTERACTION, values, "id = ?", new String[] {String.valueOf(hotspotInteractionId)});

        // outer object
        values = new ContentValues();
        values.put("type", assessment.getPositionObjectInteraction().getOuterObject().getType());
        values.put("data", assessment.getPositionObjectInteraction().getOuterObject().getData());
        values.put("width", assessment.getPositionObjectInteraction().getOuterObject().getWidth());
        values.put("height", assessment.getPositionObjectInteraction().getOuterObject().getHeight());

        int updateOuterObjectCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.OBJECT, values, "id = ?", new String[] {String.valueOf(hotspotOuterObjectId)});

        // inner object
        values = new ContentValues();
        values.put("type", assessment.getPositionObjectInteraction().getInnerObject().getType());
        values.put("data", assessment.getPositionObjectInteraction().getInnerObject().getData());
        values.put("width", assessment.getPositionObjectInteraction().getInnerObject().getWidth());
        values.put("height", assessment.getPositionObjectInteraction().getInnerObject().getHeight());

        int updateInnerObjectCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.OBJECT, values, "id = ?", new String[] {String.valueOf(hotspotInnerObjectId)});
        // endregion
    }

    private void updateTableAssessment(TableAssessment assessment, DatabaseHelper helper) throws Exception {
        // region preparation
        // get ids query
        String idsQuery = App.getStringContentFromRawFile(this.context, R.raw.table_ids);

        // append query with id
        idsQuery += String.valueOf(assessment.getId() + ";");

        // query
        Cursor result = helper.getReadableDatabase().rawQuery(idsQuery, null);
        result.moveToFirst();

        // get ids
        int assessmentId = result.getInt(0);
        int responseDeclarationId = result.getInt(1);
        int correctResponseId = result.getInt(2);
        int itemBodyId = result.getInt(3);
        int tableInteractionId = result.getInt(4);
        // endregion

        // region delete
        // delete tags
        int deleteTagsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.CATEGORY_TAGS, DatabaseHelper.ForeignKeyNames.ASSESSMENT_ITEM + " = ?", new String[] {String.valueOf(assessmentId)});

        // delete paragraphs
        int deleteParagraphsCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.PARAGRAPH, DatabaseHelper.ForeignKeyNames.ITEM_BODY + " = ?", new String[] {String.valueOf(itemBodyId)});

        // delete values
        int deleteValuesCount = helper.getReadableDatabase().delete(DatabaseHelper.TableNames.VALUE, DatabaseHelper.ForeignKeyNames.CORRECT_RESPONSE + " = ?", new String[] {String.valueOf(correctResponseId)});

        // delete tables
        // rows and cells will be also deleted because of foreign key constraints cascade
        String query = "DELETE FROM t_table WHERE t_table.id IN (SELECT t_table.id FROM t_table INNER JOIN t_table_interaction ON t_table.fk_t_table_interaction_id = t_table_interaction.id WHERE t_table_interaction.id = [id]);";
        query = query.replace("[id]", String.valueOf(tableInteractionId));
        helper.getReadableDatabase().execSQL(query);

        // endregion

        // region insert
        // insert tags
        for(String tag:assessment.getCategoryTags().split(",")) {
            long insertTagId = helper.insertCategoryTagWithAssessmenId(assessmentId, tag);
        }

        // insert paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long insertParagraphId = helper.insertItemBodyParagraph(itemBodyId, paragraph);
        }

        // insert values
        for(TableAssessment.Value value:assessment.getValueList()) {
            long insertValueId = helper.insertValue(correctResponseId, value.getValueContent(), "", value.getCellIdentifier());
        }

        // insert tables
        for(Table table:assessment.getTableList()) {
            helper.insertTable("RESPONSE", tableInteractionId, -1, -1, table);
        }
        // endregion

        // region update
        // assessment item
        ContentValues values = new ContentValues();
        values.put("uuid", assessment.getUuid());
        values.put("creation_timestamp", assessment.getCreationTimestamp());
        values.put("identifier", assessment.getIdentifier());
        values.put("title", assessment.getTitle());
        values.put("adaptive", assessment.isAdaptive());
        values.put("time_dependent", assessment.isTimeDependent());

        int updateAssessmentCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.ASSESSMENT_ITEM, values, "id = ?", new String[] {String.valueOf(assessmentId)});

        // response declaration
        values = new ContentValues();
        values.put("identifier", "RESPONSE");
        values.put("cardinality", "multiple");
        values.put("base_type", "point");

        int updateResponseDeclarationCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.RESPONSE_DECLARATION, values, "id = ?", new String[] {String.valueOf(responseDeclarationId)});

        // table interaction
        values = new ContentValues();
        values.put("response_identifier", "RESPONSE");
        values.put("prompt", assessment.getPrompt());

        int updateTableInteractionCount = helper.getReadableDatabase().update(DatabaseHelper.TableNames.TABLE_INTERACTION, values, "id = ?", new String[] {String.valueOf(tableInteractionId)});
        // endregion
    }

    private void updateDragAssessment(DragAssessment assessment, DatabaseHelper helper) throws Exception {
        // region preparation
        // get ids query
        String idsQuery = App.getStringContentFromRawFile(this.context, R.raw.drag_interaction_ids);

        // append query with id
        idsQuery += String.valueOf(assessment.getId() + ";");

        // query
        Cursor result = helper.getReadableDatabase().rawQuery(idsQuery, null);
        result.moveToFirst();

        // get ids
        int assessmentId = result.getInt(0);
        int itemBodyId = result.getInt(7);
        int dragInteractionId = result.getInt(8);
        // endregion

        // region delete
        // delete tags
        int deleteTagsCount = helper.getReadableDatabase().delete(TableNames.CATEGORY_TAGS, ForeignKeyNames.ASSESSMENT_ITEM + " = ?", new String[] {String.valueOf(assessmentId)});

        // delete paragraphs
        int deleteParagraphsCount = helper.getReadableDatabase().delete(TableNames.PARAGRAPH, ForeignKeyNames.ITEM_BODY + " = ?", new String[] {String.valueOf(itemBodyId)});

        // delete tables: rows and cells will be also deleted because of foreign key constraints cascade
        String query = "DELETE FROM t_table WHERE t_table.id IN (SELECT t_table.id FROM t_table INNER JOIN t_drag_interaction ON t_table.fk_t_drag_interaction_id = t_drag_interaction.id WHERE t_drag_interaction.id = [id]);";
        query = query.replace("[id]", String.valueOf(dragInteractionId));
        helper.getReadableDatabase().execSQL(query);

        // delete drag items
        int deleteDragItemsCount = helper.getReadableDatabase().delete(TableNames.DRAG_ITEM, ForeignKeyNames.DRAG_INTERACTION + " = ?", new String[] {String.valueOf(dragInteractionId)});
        // endregion

        // region insert
        // insert tags
        for(String tag:assessment.getCategoryTags().split(",")) {
            long insertTagId = helper.insertCategoryTagWithAssessmenId(assessmentId, tag);
        }

        // insert paragraphs
        for(String paragraph:assessment.getItemBodyParagraphList()) {
            long insertParagraphId = helper.insertItemBodyParagraph(itemBodyId, paragraph);
        }

        // insert tables
        for(Table table:assessment.getTableList()) {
            helper.insertTable("RESPONSE", -1, dragInteractionId, -1, table);
        }

        // insert drag items
        for(DragItem item:assessment.getDragItemList()) {
            helper.insertDragItem(dragInteractionId, item.getIdentifier(), item.getItemValue());
        }
        // endregion

        // region update
        // assessment item
        ContentValues values = new ContentValues();
        values.put("uuid", assessment.getUuid());
        values.put("creation_timestamp", assessment.getCreationTimestamp());
        values.put("identifier", assessment.getIdentifier());
        values.put("title", assessment.getTitle());
        values.put("adaptive", assessment.isAdaptive());
        values.put("time_dependent", assessment.isTimeDependent());

        int updateAssessmendCount = helper.getReadableDatabase().update(TableNames.ASSESSMENT_ITEM, values, "id = ?", new String[] {String.valueOf(assessmentId)});

        // update drag interaction
        values = new ContentValues();
        values.put("prompt", assessment.getPrompt());
        values.put("mode", assessment.getDragMode().toString());

        int updateDragInteractionCount = helper.getReadableDatabase().update(TableNames.DRAG_INTERACTION, values, "id = ?", new String[] {String.valueOf(dragInteractionId)});
        // endregion
    }

    // endregion
}