SELECT DISTINCT
	fk_t_assessment_item_id
FROM
	t_statistic
WHERE
	started_timestamp NOT NULL AND
	processed_timestamp NOT NULL AND
	how_solved NOT NULL AND
	fk_t_assessment_item_id IN
	(
		SELECT
			fk_t_assessment_item_id
		FROM
			t_statistic
		WHERE
			date(processed_timestamp, 'unixepoch', 'localtime') = '[date]' AND
			(how_solved = 1 OR how_solved = 2)
	)
ORDER BY
	fk_t_assessment_item_id ASC,
	processed_timestamp DESC