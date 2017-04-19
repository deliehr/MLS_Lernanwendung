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
	t_hotspot_interaction.response_identifier,
	t_hotspot_interaction.max_choices,
	t_hotspot_interaction.prompt,
	t_hotspot_interaction.fk_outer_object_id,
	t_hotspot_interaction.fk_inner_object_id,
	t_area_mapping.id,
	t_correct_response.id

FROM
	t_assessment_item,
	t_response_declaration,
	t_correct_response,
	t_value,
	t_area_mapping,
	t_area_map_entry,
	t_item_body,
	t_hotspot_interaction
WHERE
	t_assessment_item.id = t_response_declaration.fk_t_assessment_item_id
	AND t_response_declaration.id = t_correct_response.fk_t_response_declaration_id
	AND t_correct_response.id = t_value.fk_t_correct_response_id
	AND t_assessment_item.id = t_item_body.fk_t_assessment_item_id
	AND t_item_body.id = t_hotspot_interaction.fk_t_item_body_id
	AND t_assessment_item.id =