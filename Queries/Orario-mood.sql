# Displayed mood
SELECT sdpe.displayed_mood ,
count(sdpe.displayed_mood)  as disp_mood
FROM slsDate_partEmotion as sdpe 
WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
group by sdpe.displayed_mood
# Felt emotion
select 
sdpe.felt_emotion  ,
count(sdpe.felt_emotion) as felt_emotion_count
from slsDate_partEmotion sdpe 
WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
group by sdpe.felt_emotion  
# Displayed emotion
SELECT sdpe.displayed_emotion  ,
count(sdpe.displayed_emotion)  as disp_emotion
FROM slsDate_partEmotion as sdpe 
WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
group by sdpe.displayed_emotion 
# Felt mood
select 
sdpe.felt_mood ,
count(sdpe.felt_mood) as felt_mood_count
from slsDate_partEmotion sdpe 
WHERE HOUR(sdpe.`date`)  BETWEEN 12 and 24
group by sdpe.felt_mood   