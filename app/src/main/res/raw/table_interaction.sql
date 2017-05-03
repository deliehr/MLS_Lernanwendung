SELECT DISTINCT
	t_assessment_item.id,
	t_assessment_item.uuid,
	t_assessment_item.creation_timestamp,
	t_assessment_item.identifier,
	t_assessment_item.title,
	t_assessment_item.adaptive,
	t_assessment_item.time_dependent,
	t_response_declaration.identifier,
	t_response_declaration.cardinality,
	t_response_declaration.base_type,
	t_item_body.id,
	t_correct_response.id,
	t_table_interaction.response_identifier,
	t_table_interaction.prompt,
	t_table_interaction.id
FROM
	t_assessment_item,
	t_response_declaration,
	t_correct_response,
	t_item_body,
	t_table_interaction

WHERE
	t_assessment_item.id = t_response_declaration.fk_t_assessment_item_id
	AND t_response_declaration.id = t_correct_response.fk_t_response_declaration_id
	AND t_assessment_item.id = t_item_body.fk_t_assessment_item_id
	AND t_item_body.id = t_table_interaction.fk_t_item_body_id
	AND t_assessment_item.id = [id];