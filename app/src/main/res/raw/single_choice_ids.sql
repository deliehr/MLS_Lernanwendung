SELECT
	t_assessment_item.id AS [ai_id],
	t_response_declaration.id AS [rd_id],
	t_correct_response.id AS [cr_id],
	t_item_body.id AS [ib_id],
	t_choice_interaction.id AS [ci_id]
FROM
	t_assessment_item,
	t_response_declaration,
	t_correct_response,
	t_item_body,
	t_choice_interaction
WHERE
	t_assessment_item.id = t_response_declaration.fk_t_assessment_item_id
	AND t_response_declaration.id = t_correct_response.fk_t_response_declaration_id
	AND t_assessment_item.id = t_item_body.fk_t_assessment_item_id
	AND t_item_body.id = t_choice_interaction.fk_t_item_body_id
	AND t_assessment_item.id =