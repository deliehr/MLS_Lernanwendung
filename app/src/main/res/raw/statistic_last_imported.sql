SELECT
    COUNT(id) AS count,
    date(started_timestamp, 'unixepoch', 'localtime') AS started,
    date(processed_timestamp, 'unixepoch', 'localtime') AS processed,
    how_solved
FROM
    t_statistic
WHERE
    started NOT NULL AND
    processed NOT NULL AND
    how_solved NOT NULL AND
    processed = '[date]' AND
    fk_t_assessment_item_id IN
    (
    SELECT DISTINCT
        t_assessment_item.id
    FROM
        t_assessment_item,
        t_import,
        (
        SELECT
            MAX(
                CASE WHEN max_imp IS NULL THEN 0 ELSE MAX(max_imp) END,
                CASE WHEN max_upd IS NULL THEN 0 ELSE MAX(max_upd) END
            ) AS max_timestamp
        FROM
            (SELECT
                MAX(t_import.import_timestamp) AS max_imp,
                MAX(t_import.update_timestamp) AS max_upd
            FROM
                t_import)
            ) AS q1
    WHERE
        t_assessment_item.id = t_import.fk_t_assessment_item_id AND
        (t_import.import_timestamp = q1.max_timestamp OR t_import.update_timestamp = q1.max_timestamp)
    )
GROUP BY
    how_solved
ORDER BY
    how_solved ASC;