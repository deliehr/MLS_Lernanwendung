SELECT DISTINCT
    t_assessment_item.id,
    t_assessment_item.uuid,
    t_assessment_item.title,
    t_assessment_item.creation_timestamp,
    t_assessment_item.identifier
FROM
	t_assessment_item,
	t_import
WHERE
	t_assessment_item.id = t_import.fk_t_assessment_item_id AND
	(t_import.import_timestamp = [max_timestamp] OR t_import.update_timestamp = [max_timestamp])