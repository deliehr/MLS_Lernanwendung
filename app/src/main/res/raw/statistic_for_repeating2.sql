SELECT
	fk_t_assessment_item_id,
	processed_timestamp,
	how_solved
FROM
	t_statistic
WHERE
	fk_t_assessment_item_id = [id]
ORDER BY
	fk_t_assessment_item_id ASC,
	processed_timestamp DESC
LIMIT [limit]