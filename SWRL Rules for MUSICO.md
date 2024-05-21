# SWRL Rules for MUSICO

<!--
1) genreMood: Genre(?gen) ^ MusicalWork(?work) ^ genre(?work, ?gen) ^ recognized_mood(?work, ?mood) -> recognized_mood(?gen, ?mood) -->

```swrl

1) workMood:
    MusicalWork(?work) ^ MusicianParticipation(?part) ^ Genre(?gen) ^
    genre(?work,?gen) ^
    played_musical_work(?part, ?work) ^ expressed_mood(?part, ?mood)
        ->
    recognized_mood(?work, ?mood) ^ recognized_mood(?gen, ?mood)

2) venueGenre:
    MusicianParticipation(?part) ^ MusicalEvent(?ev) ^
    MusicVenue(?pl) ^ Genre(?gen) ^
    involved_event(?part, ?ev) ^ played_musical_work(?part, ?work) ^
    location(?ev, ?pl) ^  genre(?work, ?gen)
        ->
    played_genre(?pl, ?gen)

<!-- Recommendation of a user based on the similarity of the genre played by the user and the genre played by others in the same city -->

3) userRecommendation:
    HumanMusician(?u) ^ HumanMusician(?o) ^
    swrlb:notEqual(?u, ?o) ^
    foaf:based_near(?u, ?city) ^ foaf:based_near(?o, ?city) ^
    plays_genre(?u, ?gen) ^ similar_to(?gen, ?ggen) ^
    plays_genre(?o, ?ggen)
        ->
    gets_recommended_user(?u, ?o)

<!-- Recommendation of a user based on the similarity of the genre played by others in near events-->

3.1) userRecommendation2:
    HumanMusician(?u) ^ HumanMusician(?o) ^
    swrlb:notEqual(?u, ?o) ^ plays_genre(?u, ?gen)
    in_participation(?o, ?part) ^ involved_event(?part, ?ev) ^ place(?ev, ?pl) ^ foaf:based_near(?u, ?pl) ^
    event_genre(?ev, ?gen)
        ->
    gets_recommended_user(?u, ?o)

3.2) userRecommandation3: in base al mood
3.3) userRecommandation:


4) genreRecommendation:
    HumanMusician(?u) ^
    plays_genre(?u, ?gen) ^
    similar_to(?gen,?ggen) ^ swrlb:notEqual(?gen, ?ggen)
        ->
    gets_recommended_genre(?u, ?ggen)

<!-- Recommendation of a genre based on the similarity of the genres played by the user and the genre of the event near the user's location -->

4.1) genreRecommendation2:
    HumanMusician(?u) ^ MusicalEvent(?ev) ^
    place(?ev, ?pl) ^ foaf:based_near(?u, ?pl) ^
    event_genre(?ev, ?gen) ^ similar_to(?gen,?ggen) ^ swrlb:notEqual(?gen, ?ggen)
        ->
    gets_recommended_genre(?u, ?ggen) ^ gets_recommended_genre(?u, ?gen)

<!-- Venue recommendation based on music genre played by the user and the one played in the venue  -->

5) venueRecommendation:
    HumanMusician(?u) ^  MusicVenue(?pl) ^
    plays_genre(?u, ?gen) ^ played_genre(?pl,?gen)
        ->
    gets_recommended_venue(?u, ?pl)

<!-- Venue Recommendation based on event held in a venue near the user's location -->

5.1) venueRecommendation2:
    HumanMusician(?u) ^ MusicalEvent(?ev) ^
    event_genre(?ev, ?gen) ^ place(?ev,?pl) ^ foaf:based_near(?u, ?pl) ^
    plays_genre(?u, ?gen)
        ->
    gets_recommended_venue(?u, ?pl)

<!--
    Event Recommendation based on the genre played by the user and the event's genre
    and the event is not ended yet and the user is near the event's location
-->
6) eventRecommendation:
    HumanMusician(?u) ^ MusicalEvent(?ev) ^
    event_genre(?ev, ?gen) ^ plays_genre(?u, ?gen)
    alredy_ended(?ev,false) ^place(?ev,?pl) ^ foaf:based_near(?u, ?pl)
        ->
    gets_recommended_event(?u, ?ev)

<!-- Event Recommendation based on the user's friends attending the event and the event is not ended yet -->

6.1) eventRecommendation2:
    HumanMusician(?u) ^ MusicalEvent(?ev) ^
    foaf:knows(?u, ?o) ^  going_to_attend(?o, ?ev) ^
    alredy_ended(?ev,false)
        ->
    gets_recommended_event(?u, ?ev)

7) multiInstrumentalism:
    2 o più strumenti
```

These rules, written according to the syntax of SWRL, an extension of the OWL language, were used to implement the inferential logic of the MUSICO ontology. These rules were written primarily to materialize new data that could be suggested to the end user.

- Rule 1) allows the inference of the mood of a musical genre and MusicalWork from the mood expressed during the performance of that song during a musician's participation in an event.

- Rule 2) allows the inference of the genre of music played at a particular venue by inferring it from the appearances of musicians held there.

- Rule 3) allows the inference of a user's recommendation to another user by analyzing whether they live in the same city and play similar genres.

- **Rule 3.1**) allows inference of a user's recommendation to another user by analyzing whether they live in the same city and checking what genres have been played by others in events near the user's location.

- Rule 4) allows inferring the recommendation of a music genre to a user based on the similarity of the genres they play.

- **Rule 4.1**) allows inferring the recommendation of a music genre to a user based on the similarity of the genres they play and the genre of the event near the user's location.

- Rule 5) allows inferring the recommendation of a venue to a user based on the genres they play and the genres played at that venue.

- **Rule 5.1**) allows inferring the recommendation of a venue to a user based on the genres played at events held in a venue near the user's location.

- **Rule 6**) Infers the recommendation of an event to a user based on the genre played by the user and the genre of the event,if it is already ended, and the event's location.

- **Rule 6.1**) allows inferring the recommendation of an event to a user based on the user's friends attending the event, and the event is not yet ended.

<!-- Queste regole, scritte secondo la sintassi di SWRL, estensione del linguaggio OWL, sono state utilizzate per implementare la logica inferenziale del sistema MUSICO.
La regola 1) permette di inferire il mood di un genere musicale e di un MusicalWork a partire dal mood espresso durante l'esecuzione di tale brano durante una partecipazione di un musicista ad un evento. La regola 2) permette di inferire il genere musicale suonato in un determinato locale deducendolo dalle partecipazioni dei musicisti tenutesi in quel luogo. La regola 3) permette di inferire la raccomandazione di un utente ad un altro utente analizzando qualora vivano nella stessa città e suonino generi simili. La regola 4) permette di inferire la raccomandazione di un genere musicale ad un utente basati sulla similitudine dei generi che suona. Analogamente la regola 5) permette di inferire la raccomandazione di un locale ad un utente basandosi sui generi che suona e che vengono suonati in quel locale. -->
