SELECT DISTINCT
	t_assessment_item.id,
	t_assessment_item.uuid,
	t_assessment_item.creation_timestamp,
	t_assessment_item.identifier,
	t_assessment_item.title,
	t_assessment_item.adaptive,
	t_assessment_item.time_dependent,
	t_item_body.id,
	t_drag_interaction.id,
	t_drag_interaction.prompt,
	t_drag_interaction.mode
FROM
	t_assessment_item,
	t_item_body,
	t_drag_interaction
WHERE
	t_assessment_item.id = t_item_body.fk_t_assessment_item_id
	AND t_item_body.id = t_drag_interaction.fk_t_item_body_id
	AND t_assessment_item.id =