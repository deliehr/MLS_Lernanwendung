package Comprehensive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import Components.Assessment;
import Components.Cell;
import Components.DragAssessment;
import Components.DragCell;
import Components.HotspotAssessment;
import Components.MultipleChoiceAssessment;
import Components.Row;
import Components.SimpleChoice;
import Components.SingleChoiceAssessment;
import Components.StandardCell;
import Components.Table;
import Components.TableAssessment;
import it.liehr.mls_app.R;

/**
 * Class for Database helper
 *
 * @author Dominik Liehr
 * @version 0.02
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // region object finals (database name)
    public static final String DATABASE_NAME = "database_debug.db";
    // endregion

    // region object variables
    private Context context;
    // endregion

    // region constructors
    public DatabaseHelper(Context context) {
        //super(Context context, String database name, CursorFactory factory, int version)
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }
    // endregion

    // region override methods
    public void onCreate(SQLiteDatabase db) {
        String sqlCreateAllTables = App.getStringContentFromRawFile(this.context, R.raw.db_create_all_tables);

        String[] singleQueries = App.getMultipleQueries(sqlCreateAllTables);

        int c = 0;
        for(String query:singleQueries) {
            try {
                db.execSQL(query);
                c++;
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }

        Log.i("Info", String.valueOf(c) + " tables created.");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop all tables
        try {
            String sqlDropAllTables = App.getStringContentFromRawFile(this.context, R.raw.db_delete_all_tables);
            db.execSQL(sqlDropAllTables);
        } catch (Exception e) {
            Log.e("Error", "DB. Error droping all tables: " + e.getMessage());
        }

        // create new tables
        this.onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if(!db.isReadOnly()) {
            // enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // endregion

    // region insert methods
    private long insert(String table, ContentValues values) throws Exception {
        // insert or throw
        return this.getReadableDatabase().insertOrThrow(table, null, values);
    }

    public long insertAssessmentItem(Assessment assessment) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put("uuid", assessment.getUuid());
        values.put("creation_timestamp", assessment.getCreationTimestamp());
        values.put("identifier", assessment.getIdentifier());
        values.put("title", assessment.getTitle());
        values.put("adaptive", assessment.isAdaptive());
        values.put("time_dependent", assessment.isTimeDependent());

        // category

        // import

        // return insert id
        return this.insert(TableNames.ASSESSMENT_ITEM, values);
    }

    public long insertResponseDeclaration(long assessmentItemId, String identifier, String cardinality, String baseType) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ASSESSMENT_ITEM, assessmentItemId);
        values.put("identifier", identifier);
        values.put("cardinality", cardinality);
        values.put("base_type", baseType);

        // return insert id
        return this.insert(TableNames.RESPONSE_DECLARATION, values);
    }

    public long insertCorrectResponse(long responseDeclarationId) throws Exception {
               // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.RESPONSE_DECLARATION, responseDeclarationId);

        // return insert id
        return this.insert(TableNames.CORRECT_RESPONSE, values);
    }

    public long insertValue(long correctResponseId, String value, String value2, String cellIdentifier) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.CORRECT_RESPONSE, correctResponseId);
        values.put("value", value);
        values.put("value2", value2);
        values.put("cell_identifier", cellIdentifier);

        // return insert id
        return this.insert(TableNames.VALUE, values);
    }

    public long insertMapping(long responseDeclarationId, MultipleChoiceAssessment.KeyValueMappingGroup group) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.RESPONSE_DECLARATION, responseDeclarationId);
        values.put("lower_bound", group.getLowerBound());
        values.put("upper_bound", group.getUpperBound());
        values.put("default_value", group.getDefaultValue());

        // return insert id
        return this.insert(TableNames.MAPPING, values);
    }

    public long insertMapEntry(long mappingId, MultipleChoiceAssessment.KeyValueMappingGroup.KeyValueMapping mapEntry) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.MAPPING, mappingId);
        values.put("map_key", mapEntry.getMapKey());
        values.put("mapped_value", mapEntry.getMappedValue());

        // return insert id
        return this.insert(TableNames.MAP_ENTRY, values);
    }

    public long insertItemBody(long assessmentId) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ASSESSMENT_ITEM, assessmentId);

        // return insert id
        return this.insert(TableNames.ITEM_BODY, values);
    }

    public long insertItemBodyParagraph(long itemBodyId, String paragraph) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.PARAGRAPH, itemBodyId);
        values.put("paragraph", paragraph);

        // return insert id
        return this.insert(TableNames.PARAGRAPH, values);
    }

    public long insertChoiceInteraction(long itemBodyId, String responseIdentifier, boolean shuffle, int maxChoices, String prompt) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ITEM_BODY, itemBodyId);
        values.put("response_identifier", responseIdentifier);
        values.put("shuffle", shuffle);
        values.put("max_choices", maxChoices);
        values.put("prompt", prompt);

        // return insert id
        return this.insert(TableNames.CHOICE_INTERACTION, values);
    }

    public long insertSimpleChoice(long choiceInteractionId, SimpleChoice choice) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.CHOICE_INTERACTION, choiceInteractionId);
        values.put("identifier", choice.getIdentifier());
        values.put("caption", choice.getCaption());
        values.put("img_src", choice.getImageSource());

        // return insert id
        return this.insert(TableNames.SIMPLE_CHOICE, values);
    }

    public long insertHotspotInteraction(long itemBodyId, long innerObjectId, long outerObjectId, int maxChoices, String prompt) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ITEM_BODY, itemBodyId);
        values.put(ForeignKeyNames.OUTER_OBJECT, outerObjectId);
        values.put(ForeignKeyNames.INNER_OBJECT, innerObjectId);
        values.put("max_choices", maxChoices);
        values.put("prompt", prompt);

        // return insert id
        return this.insert(TableNames.HOTSPOT_INTERACTION, values);
    }

    public long insertObject(HotspotAssessment.Object object) throws Exception {
          // content values
        ContentValues values = new ContentValues();
        values.put("type", object.getType());
        values.put("data", object.getData());
        values.put("width", object.getWidth());
        values.put("height", object.getHeight());

        // return insert id
        return this.insert(TableNames.OBJECT, values);
    }

    public long insertAreaMapping(long responseDeclarationId, int defaultValue) throws Exception {
              // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.RESPONSE_DECLARATION, responseDeclarationId);
        values.put("default_value", defaultValue);

        // return insert id
        return this.insert(TableNames.AREA_MAPPING, values);
    }

    public long insertAreaMapEntry(long areaMappingId, String shape, String coords, int mapped_value) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.AREA_MAPPING, areaMappingId);
        values.put("shape", shape);
        values.put("coords", coords);
        values.put("mapped_value", mapped_value);

        // return insert id
        return this.insert(TableNames.AREA_MAP_ENTRY, values);
    }

    public long insertTableInteraction(long itemBodyId, String responseIdentifier, String prompt) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ITEM_BODY, itemBodyId);
        values.put("response_identifier", responseIdentifier);
        values.put("prompt", prompt);

        // return insert id
        return this.insert(TableNames.TABLE_INTERACTION, values);
    }

    public void insertTable(String responseIdentifier, long fk_t_table_interaction, long fk_t_drag_interaction, long fk_t_support_table, Table table) throws Exception {
        // insert table
        ContentValues values = new ContentValues();
        values.put("response_identifier", responseIdentifier);

        if(fk_t_table_interaction != -1) {
            values.put(ForeignKeyNames.TABLE_INTERACTION, fk_t_table_interaction);
        } else if(fk_t_drag_interaction != -1) {
            values.put(ForeignKeyNames.DRAG_INTERACTION, fk_t_drag_interaction);
        } else {
            values.put(ForeignKeyNames.SUPPORT_TABLE, fk_t_support_table);
        }

        // table insert id
        long tableInsertId = this.insert(TableNames.TABLE, values);

        // insert rows
        for(Row row:table.getRowList()) {
            long rowInsertId = this.insertRow(tableInsertId);

            // insert cells
            for(Cell cell:row.getCellList()) {
                // distinguish between table interaction, drag interaction, support table
                if(fk_t_table_interaction != -1) {
                    // table interaction
                    // standard cell
                    // writeable (true|false), header (true|false), cellIdentifier
                    StandardCell standardCell = (StandardCell) cell;
                    long cellInsertId = this.insertCell(rowInsertId, standardCell.getIdentifier(), standardCell.getCellValue(), standardCell.isHead(), standardCell.getColspan(), standardCell.isWriteable(), "");
                } else if(fk_t_drag_interaction != -1) {
                    // drag interaction => drag cell => writeable (only false), header (true|false), drag identifier (column / row identifier)
                    DragCell dragCell = (DragCell) cell;
                    long cellInsertId = this.insertCell(rowInsertId, dragCell.getIdentifier(), dragCell.getCellValue(), dragCell.isHead(), dragCell.getColspan(), false, dragCell.getDragIdentifier());
                } else {
                    // support table
                    // standard cell
                    // writeable (only false), header (true|false)
                    StandardCell standardCell = (StandardCell) cell;
                    long cellInsertId = this.insertCell(rowInsertId, null, standardCell.getCellValue(), standardCell.isHead(), standardCell.getColspan(), false, "");
                }
            }
        }
    }

    public long insertRow(long tableId) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.TABLE, tableId);

        // return insert id
        return this.insert(TableNames.ROW, values);
    }

    public long insertCell(long rowId, String cell_identifier, String value, boolean head, int colspan, boolean writeable, String dragIdentifier) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ROW, rowId);
        values.put("cell_identifier", cell_identifier);
        values.put("value", value);
        values.put("head", head);
        values.put("colspan", colspan);
        values.put("writeable", writeable);
        values.put("drag_identifier", dragIdentifier);

        // return insert id
        return this.insert(TableNames.CELL, values);
    }

    public long insertDragInteraction(long itemBodyid, String prompt, String mode) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ITEM_BODY, itemBodyid);
        values.put("prompt", prompt);
        values.put("mode", mode);

        // return insert id
        return this.insert(TableNames.DRAG_INTERACTION, values);
    }

    public long insertRowIdentifier(long dragInteractionId, String id2, String value) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.DRAG_INTERACTION, dragInteractionId);
        values.put("id2", id2);
        values.put("value", value);

        // return insert id
        return this.insert(TableNames.ROW_IDENTIFIER, values);
    }

    public long insertDragItem(long dragInteractionId, String identifier, String value) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.DRAG_INTERACTION, dragInteractionId);
        values.put("identifier", identifier);
        values.put("value", value);

        // return insert id
        return this.insert(TableNames.DRAG_ITEM, values);
    }

    public long insertCategoryTagWithAssessmenId(long assessmentId, String tag_name) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ASSESSMENT_ITEM, assessmentId);
        values.put("tag_name", tag_name);

        // return insert id
        return this.insert(TableNames.CATEGORY_TAGS, values);
    }

    public long insertCategoryTagWithRelatedId(long relatedId, String tag_name) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.RELATED, relatedId);
        values.put("tag_name", tag_name);

        // return insert id
        return this.insert(TableNames.CATEGORY_TAGS, values);
    }

    public long insertImportTimestamp(long assessmentId, int timestamp, String used_url) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ASSESSMENT_ITEM, assessmentId);
        values.put("import_timestamp", timestamp);
        values.put("used_url", used_url);

        // return insert id
        return this.insert(TableNames.IMPORT, values);
    }

    public long insertUpdateTimestamp(long assessmentId, int timestamp, String used_url) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.ASSESSMENT_ITEM, assessmentId);
        values.put("update_timestamp", timestamp);
        values.put("used_url", used_url);

        // return insert id
        return this.insert(TableNames.IMPORT, values);
    }

    public long insertRelatedGroup(String uuid, int creationTimestamp, String title, boolean shuffle) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put("uuid", uuid);
        values.put("creation_timestamp", creationTimestamp);
        values.put("title", title);
        values.put("shuffle", shuffle);

        // return insert id
        return this.insert(TableNames.RElATED, values);
    }

    public long insertRelatedGroupItem(long relatedGroupId, String assessmentUuid) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.RELATED, relatedGroupId);
        values.put("assessment_uuid", assessmentUuid);

        // return insert id
        return this.insert(TableNames.RELATED_ITEM, values);
    }

    public long insertSupport(String uuid, String assessmentUuid, int creationTimestamp, String identifier) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put("uuid", uuid);
        values.put("assessment_uuid", assessmentUuid);
        values.put("creation_timestamp", creationTimestamp);
        values.put("identifier", identifier);

        // return insert id
        return this.insert(TableNames.SUPPORT, values);
    }

    public long insertSupportTextbox(long supportId, String content) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.SUPPORT, supportId);
        values.put("textbox_content", content);

        // return insert id
        return this.insert(TableNames.SUPPORT_TEXTBOX, values);
    }

    public long insertSupportMedia(long supportId, String mediaType, String mediaSource, String prompt) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.SUPPORT, supportId);
        values.put("media_type", mediaType);
        values.put("media_source", mediaSource);
        values.put("prompt", prompt);

        // return insert id
        return this.insert(TableNames.SUPPORT_MEDIA, values);
    }

    public long insertSupportSelection(long supportId, String prompt) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.SUPPORT, supportId);
        values.put("prompt", prompt);

        // return insert id
        return this.insert(TableNames.SUPPORT_SELECTION, values);
    }

    public long insertSupportSelectionItem(long selectionId, String selectValue) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.SUPPORT_SELECTION, selectionId);
        values.put("select_value", selectValue);

        // return insert id
        return this.insert(TableNames.SELECTION_ITEM, values);
    }

    public long insertSupportTable(long supportId, String prompt) throws Exception {
        // content values
        ContentValues values = new ContentValues();
        values.put(ForeignKeyNames.SUPPORT, supportId);
        values.put("prompt", prompt);

        // return insert id
        return this.insert(TableNames.SUPPORT_TABLE, values);
    }
    // endregion

    // region get assessments
    public String getAssessmentIdentifierById(long id) throws Exception {
        String identifier = "";

        // boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit
        Cursor cursor = this.getReadableDatabase().query(false, "t_assessment_item", new String[] {"identifier"}, "id = ?", new String[] {String.valueOf(id)}, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0) {
            identifier = cursor.getString(0);
        }

        return identifier;
    }

    public SingleChoiceAssessment getSingleChoiceAssessment(long id) throws Exception {
        // region main assessment data
        // get query
        String query = App.getStringContentFromRawFile(context, R.raw.single_choice_assessment);
        query += " " + String.valueOf(id) + ";";

        // result
        Cursor result = this.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        // create assessment
        SingleChoiceAssessment assessment = new SingleChoiceAssessment();
        assessment.setId(result.getInt(0));
        assessment.setUuid(result.getString(1));
        assessment.setCreationTimestamp(result.getInt(2));
        assessment.setIdentifier(result.getString(3));
        assessment.setTitle(result.getString(4));
        assessment.setAdaptive(App.parseSQLLiteBoolean(result.getInt(5)));
        assessment.setTimeDependent(App.parseSQLLiteBoolean(result.getInt(6)));
        // response declaration identifier 7
        // response declaration cardinality 8
        // response declaration base type 9
        assessment.setResponseIdentifier(result.getString(10));
        assessment.setShuffleChoices(App.parseSQLLiteBoolean(result.getInt(11)));
        assessment.setMaxChoices(Byte.valueOf(String.valueOf(result.getInt(12))));
        assessment.setPrompt(result.getString(13));
        String itemBodyId = result.getString(14);
        String choiceInteractionId = result.getString(15);
        String correctResponse = result.getString(16);
        // endregion

        // region correct values
        // query
        result = this.getReadableDatabase().query(false, TableNames.VALUE, new String[] {"value"}, "fk_t_correct_response_id = ?", new String[] {correctResponse}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.setCorrectValueIdentifier(result.getString(0));
        } while(result.moveToNext());
        // endregion

        // region simple choices
        result = this.getReadableDatabase().query(false, TableNames.SIMPLE_CHOICE, new String[] {"identifier", "caption", "img_src"}, "fk_t_choice_interaction_id = ?", new String[] {choiceInteractionId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getSimpleChoiceList().add(new SimpleChoice(result.getString(0), result.getString(1), result.getString(2)));
        } while(result.moveToNext());
        // endregion

        // region paragraphs
        result = this.getReadableDatabase().query(false, TableNames.PARAGRAPH, new String[] {"paragraph"}, ForeignKeyNames.ITEM_BODY + " = ?", new String[] {itemBodyId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getItemBodyParagraphList().add(result.getString(0));
        } while(result.moveToNext());
        // endregion

        return assessment;
    }

    public MultipleChoiceAssessment getMultipleChoiceAssessment(long id) throws Exception {
        // region main assessment data
        // get query
        String query = App.getStringContentFromRawFile(context, R.raw.multiple_choice_assessment);
        query += " " + String.valueOf(id) + ";";

        // result
        Cursor result = this.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        // create assessment
        MultipleChoiceAssessment assessment = new MultipleChoiceAssessment();
        assessment.setId(result.getInt(0));
        assessment.setUuid(result.getString(1));
        assessment.setCreationTimestamp(result.getInt(2));
        assessment.setIdentifier(result.getString(3));
        assessment.setTitle(result.getString(4));
        assessment.setAdaptive(App.parseSQLLiteBoolean(result.getInt(5)));
        assessment.setTimeDependent(App.parseSQLLiteBoolean(result.getInt(6)));
        // response declaration identifier 7
        // response declaration cardinality 8
        // response declaration base type 9
        assessment.setResponseIdentifier(result.getString(10));
        assessment.setShuffleChoices(App.parseSQLLiteBoolean(result.getInt(11)));
        assessment.setMaxChoices(Byte.valueOf(String.valueOf(result.getInt(12))));
        assessment.setPrompt(result.getString(13));
        String itemBodyId = result.getString(14);
        assessment.setKeyValueMappingGroup(new MultipleChoiceAssessment.KeyValueMappingGroup(Byte.valueOf(String.valueOf(result.getInt(15))), Byte.valueOf(String.valueOf(result.getInt(16))), Byte.valueOf(String.valueOf(result.getInt(17)))));
        String correctResponseId = result.getString(18);
        String choiceInteractionId = result.getString(19);
        // endregion

        // region correct values
        result = this.getReadableDatabase().query(false, TableNames.VALUE, new String[] {"value"}, "fk_t_correct_response_id = ?", new String[] {correctResponseId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getCorrectValueList().add(result.getString(0));
        } while(result.moveToNext());
        // endregion

        // region simple choices
        result = this.getReadableDatabase().query(false, TableNames.SIMPLE_CHOICE, new String[] {"identifier", "caption", "img_src"}, "fk_t_choice_interaction_id = ?", new String[] {choiceInteractionId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getSimpleChoiceList().add(new SimpleChoice(result.getString(0), result.getString(1), result.getString(2)));
        } while(result.moveToNext());
        // endregion

        // region paragraphs
        result = this.getReadableDatabase().query(false, TableNames.PARAGRAPH, new String[] {"paragraph"}, ForeignKeyNames.ITEM_BODY + " = ?", new String[] {itemBodyId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getItemBodyParagraphList().add(result.getString(0));
        } while(result.moveToNext());
        // endregion

        // region mapping
        query = App.getStringContentFromRawFile(context, R.raw.multiple_choice_ids);
        query += " " + String.valueOf(id) + ";";
        result = this.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        // mapping id, lower bound, upper bound, default value
        int mappingId = result.getInt(3);
        int lowerBound = result.getInt(6);
        int upperBound = result.getInt(7);
        int defaultValue = result.getInt(8);

        // get mapping keys and values
        result = this.getReadableDatabase().query(false, TableNames.MAP_ENTRY, new String[] {"map_key", "mapped_value"}, "fk_t_mapping_id = ?", new String[] {String.valueOf(mappingId)}, null, null, null, null);
        result.moveToFirst();

        MultipleChoiceAssessment.KeyValueMappingGroup group = new MultipleChoiceAssessment.KeyValueMappingGroup(lowerBound, upperBound, defaultValue);
        do {
            MultipleChoiceAssessment.KeyValueMappingGroup.KeyValueMapping newMapping = new MultipleChoiceAssessment.KeyValueMappingGroup.KeyValueMapping(result.getString(0), result.getInt(1));
            group.addKeyValueMapping(newMapping);
        } while (result.moveToNext());
        assessment.setKeyValueMappingGroup(group);
        // endregion

        return assessment;
    }

    public HotspotAssessment getHotspotAssessment(long id) throws Exception {
        // region main assessment data
        // get query
        String query = App.getStringContentFromRawFile(context, R.raw.hotspot_assessment);
        query += " " + String.valueOf(id) + ";";

        // result
        Cursor result = this.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        // create assessment
        HotspotAssessment assessment = new HotspotAssessment();
        assessment.setId(result.getInt(0));
        assessment.setUuid(result.getString(1));
        assessment.setCreationTimestamp(result.getInt(2));
        assessment.setIdentifier(result.getString(3));
        assessment.setTitle(result.getString(4));
        assessment.setAdaptive(App.parseSQLLiteBoolean(result.getInt(5)));
        assessment.setTimeDependent(App.parseSQLLiteBoolean(result.getInt(6)));
        // response declaration identifier 7
        // response declaration cardinality 8
        // response declaration base_type 9
        String itemBodyId = result.getString(10);
        // response identifier 11
        assessment.setMaxChoices(Byte.valueOf(String.valueOf(result.getInt(12))));
        assessment.setPrompt(result.getString(13));
        String key_out = String.valueOf(result.getInt(14));
        String key_inn = String.valueOf(result.getInt(15));
        String area_mapping_id = String.valueOf(result.getInt(16));
        String correct_response_id = String.valueOf(result.getInt(17));
        // endregion

        // region correct value lust
        // boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit
        result = this.getReadableDatabase().query(false, TableNames.VALUE, new String[] {"value, value2"}, "fk_t_correct_response_id = ?", new String[] {correct_response_id}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getCorrectValueList().add(result.getString(0) + " " + result.getString(1));
        } while(result.moveToNext());
        // endregion

        // region position object interaction (and objects)
        // primary keys objects
        // query
        result = this.getReadableDatabase().query(false, TableNames.OBJECT, new String[] {"type", "data", "width", "height"}, "id = ?", new String[]{key_out}, null, null, null, null);
        result.moveToFirst();

        HotspotAssessment.Object outerObject = new HotspotAssessment.Object(result.getString(0), result.getString(1), result.getInt(2), result.getInt(3));

        result = this.getReadableDatabase().query(false, TableNames.OBJECT, new String[] {"type", "data", "width", "height"}, "id = ?", new String[]{key_inn}, null, null, null, null);
        result.moveToFirst();

        HotspotAssessment.Object innerObject = new HotspotAssessment.Object(result.getString(0), result.getString(1), result.getInt(2), result.getInt(3));

        HotspotAssessment.PositionObjectInteraction poi = new HotspotAssessment.PositionObjectInteraction(outerObject, innerObject);
        assessment.setPositionObjectInteraction(poi);
        // endregion

        // region area mapping
        // area map entries
        result = this.getReadableDatabase().query(false, TableNames.AREA_MAP_ENTRY, new String[] {"shape", "coords", "mapped_value"}, "fk_t_area_mapping_id = ?", new String[]{area_mapping_id}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getAreaMapEntryList().add(new HotspotAssessment.AreaMapEntry(result.getString(0), result.getString(1), result.getInt(2)));
        } while(result.moveToNext());

        // default value
        result = this.getReadableDatabase().query(false, TableNames.AREA_MAPPING, new String[] {"default_value"}, "id = ?", new String[] {area_mapping_id}, null, null, null, null);
        result.moveToFirst();

        assessment.setAreaMappingDefaultValue(result.getInt(0));
        // endregion

        // region paragraphs
        result = this.getReadableDatabase().query(false, TableNames.PARAGRAPH, new String[] {"paragraph"}, ForeignKeyNames.ITEM_BODY + " = ?", new String[] {itemBodyId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getItemBodyParagraphList().add(result.getString(0));
        } while(result.moveToNext());
        // endregion

        return assessment;
    }

    public TableAssessment getTableAssessment(long id) throws Exception {
        // region main assessment data
        // get query
        String query = App.getStringContentFromRawFile(context, R.raw.table_interaction);
        query = query.replace("[id]", String.valueOf(id));

        // result
        Cursor result = this.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        // create assessment
        TableAssessment assessment = new TableAssessment();
        assessment.setId(result.getInt(0));
        assessment.setUuid(result.getString(1));
        assessment.setCreationTimestamp(result.getInt(2));
        assessment.setIdentifier(result.getString(3));
        assessment.setTitle(result.getString(4));
        assessment.setAdaptive(App.parseSQLLiteBoolean(result.getInt(5)));
        assessment.setTimeDependent(App.parseSQLLiteBoolean(result.getInt(6)));
        // response declaration identifier 7
        // response declaration cardinality 8
        // response declaration base_type 9
        String itemBodyId = result.getString(10);
        String correct_response_id = String.valueOf(result.getInt(11));
        // table interaction response_identifier 12
        assessment.setPrompt(result.getString(13));
        String tableInteractionId = result.getString(14);
        // endregion

        // region correct value list
        result = this.getReadableDatabase().query(false, TableNames.VALUE, new String[] {"value", "cell_identifier"}, "fk_t_correct_response_id = ?", new String[] {correct_response_id}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getValueList().add(new TableAssessment.Value(result.getString(1), result.getString(0)));
        } while(result.moveToNext());
        // endregion

        // region get tables
        result = this.getReadableDatabase().query(false, TableNames.TABLE, new String[] {"response_identifier", "id"}, "fk_t_table_interaction_id = ?", new String[] {tableInteractionId}, null, null, null, null);
        result.moveToFirst();

        do {
            // new table
            Table table = new Table();

            // table data
            // table response identifier 0
            String tableId = result.getString(1);

            // get rows
            Cursor resultRows = this.getReadableDatabase().query(false, TableNames.ROW, new String[] {"id"}, "fk_t_table_id = ?", new String[] {tableId}, null, null, null, null);
            resultRows.moveToFirst();

            // iterate rows
            do {
                // new row
                Row row = new Row();

                // row data
                String rowId = resultRows.getString(0);

                // get cells
                Cursor resultCells = this.getReadableDatabase().query(false, TableNames.CELL, new String[] {"cell_identifier", "value", "head", "colspan", "writeable"}, "fk_t_row_id = ?", new String[] {rowId}, null, null, null, null);
                resultCells.moveToFirst();

                // iterate cells
                do {
                    // new cell
                    StandardCell cell = new StandardCell();

                    // cell data
                    cell.setIdentifier(resultCells.getString(0));
                    cell.setCellValue(resultCells.getString(1));
                    cell.setHead(App.parseSQLLiteBoolean(resultCells.getInt(2)));
                    cell.setColspan(resultCells.getInt(3));
                    cell.setWriteable(App.parseSQLLiteBoolean(resultCells.getInt(4)));

                    // add cell
                    row.addCell(cell);
                } while(resultCells.moveToNext());

                // add row to table
                table.getRowList().add(row);
            } while(resultRows.moveToNext());

            // add table to assessment
            assessment.getTableList().add(table);
        } while (result.moveToNext());
        // endregion

        // region paragraphs
        result = this.getReadableDatabase().query(false, TableNames.PARAGRAPH, new String[] {"paragraph"}, ForeignKeyNames.ITEM_BODY + " = ?", new String[] {itemBodyId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getItemBodyParagraphList().add(result.getString(0));
        } while(result.moveToNext());
        // endregion

        return assessment;
    }

    public DragAssessment getDragAssessment(long id) throws Exception {
        // region main assessment data
        // get query
        String query = App.getStringContentFromRawFile(context, R.raw.drag_interaction_ids);
        query += " " + String.valueOf(id) + ";";

        // result
        Cursor result = this.getReadableDatabase().rawQuery(query, null);
        result.moveToFirst();

        // create assessment
        String dragMode = result.getString(10);
        DragAssessment assessment;

        if(dragMode.equals(DragAssessment.DragMode.COL.toString())) {
            assessment = new DragAssessment(DragAssessment.DragMode.COL);
        } else {
            assessment = new DragAssessment(DragAssessment.DragMode.ROW);
        }

        assessment.setId(result.getInt(0));
        assessment.setUuid(result.getString(1));
        assessment.setCreationTimestamp(result.getInt(2));
        assessment.setIdentifier(result.getString(3));
        assessment.setTitle(result.getString(4));
        assessment.setAdaptive(App.parseSQLLiteBoolean(result.getInt(5)));
        assessment.setTimeDependent(App.parseSQLLiteBoolean(result.getInt(6)));
        String itemBodyId = result.getString(7);
        String dragInteractionId = result.getString(8);
        assessment.setPrompt(result.getString(9));
        // endregion

        // region paragraphs
        result = this.getReadableDatabase().query(false, TableNames.PARAGRAPH, new String[] {"paragraph"}, "fk_t_item_body_id = ?", new String[] {itemBodyId}, null, null, null, null);
        result.moveToFirst();

        do {
             assessment.getItemBodyParagraphList().add(result.getString(0));
        } while(result.moveToNext());
        // endregion

        // region drag items
        result = this.getReadableDatabase().query(false, TableNames.DRAG_ITEM, new String[] {"identifier", "value"}, "fk_t_drag_interaction_id = ?", new String[] {dragInteractionId}, null, null, null, null);
        result.moveToFirst();

        do {
            assessment.getDragItemList().add(new DragAssessment.DragItem(result.getString(0), result.getString(1)));
        } while (result.moveToNext());
        // endregion

        // region table
        result = this.getReadableDatabase().query(false, TableNames.TABLE, new String[] {"id", "response_identifier"}, "fk_t_drag_interaction_id = ?", new String[] {dragInteractionId}, null, null, null, null);
        result.moveToFirst();

        do {
            // new table
            Table table = new Table();

            // table data
            // table response identifier 0
            String tableId = result.getString(0);
            // response identifier, getstring 1

            // get rows
            Cursor resultRows = this.getReadableDatabase().query(false, TableNames.ROW, new String[] {"id"}, "fk_t_table_id = ?", new String[] {tableId}, null, null, null, null);
            resultRows.moveToFirst();

            // iterate rows
            do {
                // new row
                Row row = new Row();

                // row data
                String rowId = resultRows.getString(0);

                // get cells
                Cursor resultCells = this.getReadableDatabase().query(false, TableNames.CELL, new String[] {"id", "head", "colspan", "drag_identifier", "value"}, "fk_t_row_id = ?", new String[] {rowId}, null, null, null, null);
                resultCells.moveToFirst();

                // iterate cells
                do {
                    // new cell
                    DragCell cell = new DragCell();

                    // cell data
                    cell.setId(Integer.valueOf(resultCells.getInt(0)));
                    cell.setHead(App.parseSQLLiteBoolean(resultCells.getInt(1)));
                    cell.setColspan(resultCells.getInt(2));
                    cell.setDragIdentifier(resultCells.getString(3));
                    cell.setCellValue(resultCells.getString(4));

                    // add cell
                    row.addCell(cell);
                } while(resultCells.moveToNext());

                // add row to table
                table.getRowList().add(row);
            } while(resultRows.moveToNext());

            // add table to assessment
            assessment.getTableList().add(table);
        } while (result.moveToNext());
        // endregion

        return assessment;
    }
    // endregion

    /**
     * Get all tags from t_category_tags (distinct)
     * @return ArrayList<String>() with tags containing
     */
    public List<String> getTagList() {
        List<String> tags = new ArrayList<String>();

        Cursor result = this.getReadableDatabase().rawQuery("SELECT DISTINCT tag_name FROM " + TableNames.CATEGORY_TAGS + " ORDER BY tag_name ASC;", null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            do {
                tags.add(result.getString(0));
            } while (result.moveToNext());
        }

        return tags;
    }

    public long insertStatisticStartTimestamp(int assessmentId, long timestampStart) {
        long returnRowId = -1;

        ContentValues values = new ContentValues();
        values.put("fk_t_assessment_item_id", assessmentId);
        values.put("started_timestamp", timestampStart);

        try {
            returnRowId = this.getReadableDatabase().insertOrThrow(TableNames.STATISTIC, null, values);
        } catch (Exception e) {
            return -1;
        }

        return returnRowId;
    }

    public Boolean completeStatisticEntry(long insertStartTimestampId, long timestampProcessed, UsersAssessmentResponse response) {
        ContentValues values = new ContentValues();
        values.put("processed_timestamp", timestampProcessed);
        values.put("how_solved", response.ordinal());

        try {
            this.getReadableDatabase().update(TableNames.STATISTIC, values, "id = ?", new String[]{String.valueOf(insertStartTimestampId)});
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public int assessmentAlreadyExistingByUuid(String uuid) throws Exception {
        Cursor result = this.getReadableDatabase().query(false, TableNames.ASSESSMENT_ITEM, new String[]{"id"}, "uuid = ?", new String[]{uuid}, null, null, null, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            return result.getInt(0);
        }

        return -1;
    }

    public int relatedGroupAlreadyExistingByUuid(String uuid) throws Exception {
        Cursor result = this.getReadableDatabase().query(false, TableNames.RElATED, new String[]{"id"}, "uuid = ?", new String[]{uuid}, null, null, null, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            return result.getInt(0);
        }

        return -1;
    }

    public int getCreationTimestampOfAssessmentById(int id) throws Exception {
        Cursor result = this.getReadableDatabase().query(false, TableNames.ASSESSMENT_ITEM, new String[] {"creation_timestamp"}, "id = ?", new String[] {String.valueOf(id)}, null, null, null, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            return result.getInt(0);
        }

        return -1;
    }

    public int getCreationTimestampOfRelatedGroupById(int id) throws Exception {
        Cursor result = this.getReadableDatabase().query(false, TableNames.RElATED, new String[] {"creation_timestamp"}, "id = ?", new String[] {String.valueOf(id)}, null, null, null, null);
        result.moveToFirst();

        if(result.getCount() > 0) {
            return result.getInt(0);
        }

        return -1;
    }

    // region nested class for table names
    public static class TableNames {
        // region finals
        public static final String ASSESSMENT_ITEM = "t_assessment_item";
        public static final String RESPONSE_DECLARATION = "t_response_declaration";
        public static final String CORRECT_RESPONSE = "t_correct_response";
        public static final String VALUE = "t_value";
        public static final String MAPPING = "t_mapping";
        public static final String MAP_ENTRY = "t_map_entry";
        public static final String ITEM_BODY = "t_item_body";
        public static final String CHOICE_INTERACTION = "t_choice_interaction";
        public static final String SIMPLE_CHOICE = "t_simple_choice";
        public static final String HOTSPOT_INTERACTION = "t_hotspot_interaction";
        public static final String OBJECT = "t_object";
        public static final String AREA_MAPPING = "t_area_mapping";
        public static final String AREA_MAP_ENTRY = "t_area_map_entry";
        public static final String TABLE_INTERACTION = "t_table_interaction";
        public static final String TABLE = "t_table";
        public static final String ROW = "t_row";
        public static final String CELL = "t_cell";
        public static final String DRAG_INTERACTION = "t_drag_interaction";
        public static final String COLUMN_IDENTIFIER = "t_column_identifier";
        public static final String ROW_IDENTIFIER = "t_row_identifier";
        public static final String DRAG_ITEM = "t_drag_item";
        public static final String DROP_ZONE = "t_drop_zone";
        public static final String CATEGORY_TAGS = "t_category_tags";
        public static final String IMPORT = "t_import";
        public static final String RElATED = "t_related";
        public static final String RELATED_ITEM = "t_group_item";
        public static final String SUPPORT = "t_support";
        public static final String SUPPORT_TEXTBOX = "t_support_textbox";
        public static final String SUPPORT_MEDIA = "t_support_media";
        public static final String SUPPORT_SELECTION = "t_support_selection";
        public static final String SELECTION_ITEM = "t_selection_item";
        public static final String SUPPORT_TABLE = "t_support_table";
        public static final String STATISTIC = "t_statistic";
        public static final String PARAGRAPH = "t_paragraph";
        // endregion
    }
    // endregion

    // region nested class for foreign key names
    public static class ForeignKeyNames {
        public static final String AREA_MAPPING = "fk_t_area_mapping_id";
        public static final String ASSESSMENT_ITEM = "fk_t_assessment_item_id";
        public static final String CELL = "fk_t_cell_id";
        public static final String CHOICE_INTERACTION = "fk_t_choice_interaction_id";
        public static final String CORRECT_RESPONSE = "fk_t_correct_response_id";
        public static final String DRAG_INTERACTION = "fk_t_drag_interaction_id";
        public static final String INNER_OBJECT = "fk_inner_object_id";
        public static final String ITEM_BODY = "fk_t_item_body_id";
        public static final String OUTER_OBJECT = "fk_outer_object_id";
        public static final String MAPPING = "fk_t_mapping_id";
        public static final String RELATED = "fk_t_related_id";
        public static final String RESPONSE_DECLARATION = "fk_t_response_declaration_id";
        public static final String ROW = "fk_t_row_id";
        public static final String SUPPORT_TABLE = "fk_t_support_table_id";
        public static final String SUPPORT_SELECTION = "fk_t_support_selection_id";
        public static final String SUPPORT = "fk_t_support_id";
        public static final String TABLE = "fk_t_table_id";
        public static final String TABLE_INTERACTION = "fk_t_table_interaction_id";
        public static final String PARAGRAPH = "fk_t_item_body_id";
    }
    // endregion
}