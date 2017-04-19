SELECT DISTINCT
    date(processed_timestamp, 'unixepoch', 'localtime') AS processed
FROM
    t_statistic
WHERE
    processed NOT NULL
ORDER BY
    processed ASC;