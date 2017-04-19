SELECT DISTINCT
    date(processed_timestamp, 'unixepoch', 'localtime') AS processed
FROM
    t_statistic
WHERE
    processed NOT NULL AND
    processed >= '[period_start]' AND
    processed <= '[period_end]'
ORDER BY
    processed ASC;