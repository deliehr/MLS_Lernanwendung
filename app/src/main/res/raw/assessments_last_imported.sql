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
                   	t_import) AS q1