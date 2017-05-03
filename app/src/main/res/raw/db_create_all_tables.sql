CREATE TABLE IF NOT EXISTS 't_assessment_item' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'uuid' TEXT NULL,
  'creation_timestamp' TEXT NULL,
  'identifier' TEXT NULL,
  'title' TEXT NULL,
  'adaptive' INTEGER NULL DEFAULT 0,
  'time_dependent' INTEGER NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS 't_related' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'uuid' TEXT NULL,
  'creation_timestamp' INTEGER NULL,
  'title' TEXT NULL,
  'shuffle' INTEGER NULL);
CREATE TABLE IF NOT EXISTS 't_group_item' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_related_id' INTEGER NULL,
  'assessment_uuid' TEXT NULL,
  CONSTRAINT 'fk_t_related_id'
    FOREIGN KEY ('fk_t_related_id')
    REFERENCES 't_related' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_category_tags' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_assessment_item_id' INTEGER NULL,
  'fk_t_related_id' INTEGER NULL,
  'tag_name' INTEGER NULL,
  CONSTRAINT 'fk_t_assessment_item_id'
    FOREIGN KEY ('fk_t_assessment_item_id')
    REFERENCES 't_assessment_item' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT 'fk_t_related_id'
    FOREIGN KEY ('fk_t_related_id')
    REFERENCES 't_related' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_statistic' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_assessment_item_id' INTEGER NOT NULL,
  'started_timestamp' INTEGER NULL,
  'processed_timestamp' INTEGER NULL,
  'how_solved' INTEGER NULL,
  CONSTRAINT 'fk_t_assessment_item_id'
    FOREIGN KEY ('fk_t_assessment_item_id')
    REFERENCES 't_assessment_item' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_import' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_assessment_item_id' INTEGER NOT NULL,
  'import_timestamp' INTEGER NULL,
  'update_timestamp' INTEGER NULL,
  'used_url' TEXT NULL,
  CONSTRAINT 'fk_t_assessment_item_id'
     FOREIGN KEY ('fk_t_assessment_item_id')
     REFERENCES 't_assessment_item' ('id')
     ON DELETE CASCADE
     ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_response_declaration' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_assessment_item_id' INTEGER NOT NULL,
  'identifier' TEXT NULL DEFAULT 'response',
  'cardinality' TEXT NULL DEFAULT 'multiple',
  'base_type' TEXT NULL DEFAULT 'identifier',
  CONSTRAINT 'fk_t_assessment_item_id'
    FOREIGN KEY ('fk_t_assessment_item_id')
    REFERENCES 't_assessment_item' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_item_body' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_assessment_item_id' INTEGER NOT NULL,
  CONSTRAINT 'fk_t_assessment_item_id'
    FOREIGN KEY ('fk_t_assessment_item_id')
    REFERENCES 't_assessment_item' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_paragraph' (
    'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    'fk_t_item_body_id' INTEGER NOT NULL,
    'paragraph' TEXT,
    CONSTRAINT 'fk_t_item_body_id'
        FOREIGN KEY ('fk_t_item_body_id')
        REFERENCES 't_item_body' ('id')
        ON DELETE CASCADE
        ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_correct_response' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_response_declaration_id' INTEGER NOT NULL,
  CONSTRAINT 'fk_t_response_declaration_id'
    FOREIGN KEY ('fk_t_response_declaration_id')
    REFERENCES 't_response_declaration' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_mapping' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_response_declaration_id' INTEGER NOT NULL,
  'lower_bound' INTEGER NULL,
  'upper_bound' INTEGER NULL,
  'default_value' INTEGER NULL,
  CONSTRAINT 'fk_t_response_declaration_id'
    FOREIGN KEY ('fk_t_response_declaration_id')
    REFERENCES 't_response_declaration' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_area_mapping' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_response_declaration_id' INTEGER NOT NULL,
  'default_value' INTEGER NULL DEFAULT 0,
  CONSTRAINT 'fk_t_response_declaration_id'
    FOREIGN KEY ('fk_t_response_declaration_id')
    REFERENCES 't_response_declaration' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_choice_interaction' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_item_body_id' INTEGER NOT NULL,
  'response_identifier' TEXT NULL,
  'shuffle' INTEGER NULL DEFAULT 0,
  'max_choices' INTEGER NULL DEFAULT 1,
  'prompt' TEXT NULL,
  CONSTRAINT 'fk_t_item_body_id'
    FOREIGN KEY ('fk_t_item_body_id')
    REFERENCES 't_item_body' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_hotspot_interaction' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_item_body_id' INTEGER NOT NULL,
  'fk_outer_object_id' INTEGER NOT NULL,
  'fk_inner_object_id' INTEGER NOT NULL,
  'response_identifier' TEXT NULL,
  'max_choices' INTEGER NULL DEFAULT 1,
  'prompt' TEXT NULL,
  CONSTRAINT 'fk_t_item_body_id'
    FOREIGN KEY ('fk_t_item_body_id')
    REFERENCES 't_item_body' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT 'fk_outer_object_id'
    FOREIGN KEY ('fk_outer_object_id')
    REFERENCES 't_object' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT 'fk_inner_object_id'
    FOREIGN KEY ('fk_inner_object_id')
    REFERENCES 't_object' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_table_interaction' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_item_body_id' INTEGER NOT NULL,
  'response_identifier' TEXT NULL,
  'prompt' TEXT NULL,
  CONSTRAINT 'fk_t_item_body_id'
    FOREIGN KEY ('fk_t_item_body_id')
    REFERENCES 't_item_body' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_drag_interaction' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_item_body_id' INTEGER NOT NULL,
  'prompt' TEXT NULL,
  'mode' TEXT NULL,
  CONSTRAINT 'fk_t_item_body_id'
    FOREIGN KEY ('fk_t_item_body_id')
    REFERENCES 't_item_body' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_value' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_correct_response_id' INTEGER NOT NULL,
  'value' TEXT NULL,
  'value2' TEXT NULL,
  'cell_identifier' TEXT NULL,
    CONSTRAINT 'fk_t_correct_response_id'
    FOREIGN KEY ('fk_t_correct_response_id')
    REFERENCES 't_correct_response' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_map_entry' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_mapping_id' INTEGER NOT NULL,
  'map_key' TEXT NULL,
  'mapped_value' TEXT NULL DEFAULT '1',
  CONSTRAINT 'fk_t_mapping_id'
    FOREIGN KEY ('fk_t_mapping_id')
    REFERENCES 't_mapping' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_area_map_entry' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_area_mapping_id' INTEGER NOT NULL,
  'shape' TEXT NULL DEFAULT 'circle',
  'coords' TEXT NULL,
  'mapped_value' INTEGER NULL DEFAULT 1,
  CONSTRAINT 'fk_t_area_mapping_id'
    FOREIGN KEY ('fk_t_area_mapping_id')
    REFERENCES 't_area_mapping' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_simple_choice' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_choice_interaction_id' INTEGER NOT NULL,
  'identifier' TEXT NULL,
  'caption' INTEGER NULL,
  'img_src' TEXT NULL,
  CONSTRAINT 'fk_t_choice_interaction_id'
    FOREIGN KEY ('fk_t_choice_interaction_id')
    REFERENCES 't_choice_interaction' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_object' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'type' TEXT NULL,
  'data' TEXT NULL,
  'width' INTEGER NULL,
  'height' INTEGER NULL);
CREATE TABLE IF NOT EXISTS 't_table' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'response_identifier' TEXT NULL,
  'fk_t_table_interaction_id' INTEGER NULL,
  'fk_t_drag_interaction_id' INTEGER NULL,
  'fk_t_support_table_id' INTEGER NULL,
  CONSTRAINT 'fk_t_table_interaction_id'
    FOREIGN KEY ('fk_t_table_interaction_id')
    REFERENCES 't_table_interaction' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT 'fk_t_drag_interaction_id'
    FOREIGN KEY ('fk_t_drag_interaction_id')
    REFERENCES 't_drag_interaction' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT 'fk_t_support_table_id'
    FOREIGN KEY ('fk_t_support_table_id')
    REFERENCES 't_support_table' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_drag_item' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_drag_interaction_id' INTEGER NOT NULL,
  'identifier' TEXT NOT NULL,
  'value' TEXT NULL,
  CONSTRAINT 'fk_t_drag_interaction_id'
    FOREIGN KEY ('fk_t_drag_interaction_id')
    REFERENCES 't_drag_interaction' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_row' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_table_id' INTEGER NOT NULL,
  CONSTRAINT 'fk_t_table_id'
    FOREIGN KEY ('fk_t_table_id')
    REFERENCES 't_table' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_cell' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_row_id' INTEGER NOT NULL,
  'cell_identifier' TEXT NULL,
  'value' TEXT NULL,
  'head' INTEGER NULL DEFAULT 0,
  'colspan' INTEGER NULL DEFAULT 1,
  'writeable' INTEGER NULL DEFAULT 1,
  'drag_identifier' TEXT NULL,
  CONSTRAINT 'fk_t_row_id'
    FOREIGN KEY ('fk_t_row_id')
    REFERENCES 't_row' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_support' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'uuid' TEXT NULL,
  'assessment_uuid' TEXT NULL,
  'creation_timestamp' INTEGER NULL,
  'identifier' TEXT NULL);
CREATE TABLE IF NOT EXISTS 't_support_media' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_support_id' INTEGER NOT NULL,
  'media_type' TEXT NULL ,
  'media_source' TEXT NULL,
  'prompt' TEXT NULL,
  CONSTRAINT 'fk_t_support_id'
    FOREIGN KEY ('fk_t_support_id')
    REFERENCES 't_support' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_support_selection' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_support_id' INTEGER NOT NULL,
  'prompt' TEXT NULL,
  CONSTRAINT 'fk_t_support_id'
    FOREIGN KEY ('fk_t_support_id')
    REFERENCES 't_support' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_selection_item' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_support_selection_id' INTEGER NULL,
  'select_value' TEXT NULL,
  CONSTRAINT 'fk_t_support_selection_id'
    FOREIGN KEY ('fk_t_support_selection_id')
    REFERENCES 't_support_selection' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_support_textbox' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_support_id' INTEGER NOT NULL,
  'textbox_content' TEXT NULL,
  CONSTRAINT 'fk_t_support_id'
    FOREIGN KEY ('fk_t_support_id')
    REFERENCES 't_support' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);
CREATE TABLE IF NOT EXISTS 't_support_table' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_t_support_id' INTEGER NOT NULL,
   'prompt' TEXT NULL,
  CONSTRAINT 'fk_t_support_id'
    FOREIGN KEY ('fk_t_support_id')
    REFERENCES 't_support' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);