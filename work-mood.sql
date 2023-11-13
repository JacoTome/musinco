select  mw.musical_work_id , pi2.displayed_mood , count(pi2.displayed_mood ) as  dmc
from participating_in pi2 
join musical_work_played mwp  on mwp.session_id = pi2.participation_key 
join musical_work mw on mw.musical_work_id = mwp.musical_work_id 
GROUP by pi2.displayed_mood , mw.musical_work_id 
order by dmc desc 