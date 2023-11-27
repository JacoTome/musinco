# Displayed mood

WITH RankedData AS (
  SELECT
    HOUR(sdpe.`date`) AS hour,
    sdpe.displayed_mood,
    COUNT(sdpe.displayed_mood) AS mood_count,
    DENSE_RANK () OVER (PARTITION BY HOUR(sdpe.`date`) ORDER BY COUNT(sdpe.displayed_mood) DESC) AS row_num
  FROM
    slsDate_partEmotion AS sdpe
  WHERE
    HOUR(sdpe.`date`) BETWEEN 0 AND 24
  GROUP BY
    HOUR(sdpe.`date`), sdpe.displayed_mood
)
SELECT
  hour,
  displayed_mood,
  mood_count
FROM
  RankedData
WHERE
  row_num = 1

# Felt emotion
-- select 
-- sdpe.felt_emotion  ,
-- count(sdpe.felt_emotion) as felt_emotion_count
-- from slsDate_partEmotion sdpe 
-- WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
-- group by sdpe.felt_emotion  
-- # Displayed emotion
-- SELECT sdpe.displayed_emotion  ,
-- count(sdpe.displayed_emotion)  as disp_emotion
-- FROM slsDate_partEmotion as sdpe 
-- WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
-- group by sdpe.displayed_emotion 
-- # Felt mood
-- select 
-- sdpe.felt_mood ,
-- count(sdpe.felt_mood) as felt_mood_count
-- from slsDate_partEmotion sdpe 
-- WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
-- group by sdpe.felt_mood   