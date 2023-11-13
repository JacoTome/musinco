# Select instrument used to play some genres

SELECT pi2.instrument_id , pg.genre_name, COUNT(pi2.instrument_id) as conta
FROM part_genre pg inner join part_instrument pi2  on pg.participation_key = pi2.participation_key 
GROUP by pg.genre_name , pi2.instrument_id 
ORDER by conta desc