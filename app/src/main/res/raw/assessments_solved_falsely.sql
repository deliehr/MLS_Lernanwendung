SELECT DISTINCT
    t_assessment_item.id,
    t_assessment_item.uuid,
    t_assessment_item.title,
    t_assessment_item.creation_timestamp,
    t_assessment_item.identifier,
	t_statistic.how_solved
FROM
    t_assessment_item,
    t_statistic
WHERE
    t_assessment_item.id = t_statistic.fk_t_assessment_item_id AND
	(how_solved = 1 OR how_solved = 2)