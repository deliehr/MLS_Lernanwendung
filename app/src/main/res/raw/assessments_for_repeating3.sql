SELECT
    t_assessment_item.id,
    t_assessment_item.uuid,
    t_assessment_item.title,
    t_assessment_item.creation_timestamp,
    t_assessment_item.identifier
FROM
    t_assessment_item,
    t_statistic
WHERE
    t_assessment_item.id = t_statistic.fk_t_assessment_item_id AND
    (t_statistic.how_solved = 1 OR t_statistic.how_solved = 2) AND
     t_assessment_item.id NOT IN [exclude];

