#Select instrument used to play some genres
create view as strumenti-genere
SELECT ui.instrument_id, pg.genre_id 
FROM (
	SELECT pi3.participation_key , mw.genre_id  FROM participating_in pi3
	join musical_work_played mwp on pi3.participation_key  = mwp.session_id 
	join musical_work mw on mwp.musical_work_id = mw.musical_work_id 
	) as pg
         inner join used_instrument ui on ui.session_id = pg.participation_key

