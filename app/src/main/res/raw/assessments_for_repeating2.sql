SELECT how_solved
FROM t_assessment_item, t_statistic
WHERE t_assessment_item.id = t_statistic.fk_t_assessment_item_id
AND t_assessment_item.id = [id]
ORDER BY processed_timestamp DESC;


