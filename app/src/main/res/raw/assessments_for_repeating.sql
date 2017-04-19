SELECT t_assessment_item.id, COUNT(*) AS [count_correct]
FROM t_assessment_item, t_statistic
WHERE t_assessment_item.id = t_statistic.fk_t_assessment_item_id
AND t_statistic.how_solved=0 AND  t_assessment_item.id IN (

SELECT DISTINCT
    t_assessment_item.id
FROM
    t_assessment_item,
    t_statistic
WHERE
    t_assessment_item.id = t_statistic.fk_t_assessment_item_id AND
	(how_solved = 1 OR how_solved = 2)
)
AND how_solved NOT NULL
GROUP BY t_assessment_item.id;

