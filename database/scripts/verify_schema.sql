SELECT VERSION();
SELECT DATABASE();
SHOW TABLES;
SELECT installed_rank, version, description, script, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
