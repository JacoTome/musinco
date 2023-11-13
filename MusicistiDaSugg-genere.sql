# suggerire dei musicisti da inserire in un gruppo musicale che suona un determinato genere;

-- Abbinare genere e gruppo
-- Select mg.group_id , g.genre_id 
-- from musician_group mg
-- join artist a on mg.group_id  = a.artist_id 
-- join participating_in pi2 on pi2.artist_id = a.artist_id 
-- join musical_work_played mwp on mwp.session_id = pi2.participation_key 
-- join musical_work mw on mwp.musical_work_id = mw.musical_work_id 
-- join genre g on mw.genre_id = g.genre_id
-- GROUP by mg.group_id , g.genre_id ;

# trovare i musicisti che suonano quel genere e non fanno parte del gruppo

Select * 
from user_plays_genre upg  
join genre_musicgroup gm on upg.genre_id = gm.genre_id 
where upg.user_id not in (select user_id from group_user gu2)
