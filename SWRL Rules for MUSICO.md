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

3) userRecommendation:
    HumanMusician(?u) ^ HumanMusician(?o) ^
    swrlb:notEqual(?u, ?o) ^
    foaf:based_near(?u, ?city) ^ foaf:based_near(?o, ?city) ^
    plays_genre(?u, ?gen) ^ similar_to(?gen, ?ggen) ^
    plays_genre(?o, ?ggen)
        ->
    gets_recommended_user(?u, ?o)

4) genreRecommendation:
    HumanMusician(?u) ^
    plays_genre(?u, ?gen) ^
    similar_to(?gen,?ggen) ^ swrlb:notEqual(?gen, ?ggen)
        ->
    gets_recommended_genre(?u, ?ggen)

5) venueRecommendation:
    HumanMusician(?u) ^  MusicVenue(?pl) ^
    plays_genre(?u, ?gen) ^ played_genre(?pl,?gen)
        ->
    gets_recommended_venue(?u, ?pl)

```

These rules, written according to the syntax of SWRL, an extension of the OWL language, were used to implement the inferential logic of the MUSICO ontology. These rules were written primarily to materialize new data that could be suggested to the end user.

- Rule 1) allows the inference of the mood of a musical genre and MusicalWork from the mood expressed during the performance of that song during a musician's participation in an event.

- Rule 2) allows the inference of the genre of music played at a particular venue by inferring it from the appearances of musicians held there.

- Rule 3) allows inference of a user's recommendation to another user by analyzing whether they live in the same city and play similar genres.

- Rule 4) allows inferring the recommendation of a music genre to a user based on the similarity of the genres they play.

- Similarly, rule 5) allows inferring the recommendation of a venue to a user based on the genres it plays and that are played at that venue.

<!-- Queste regole, scritte secondo la sintassi di SWRL, estensione del linguaggio OWL, sono state utilizzate per implementare la logica inferenziale del sistema MUSICO.
La regola 1) permette di inferire il mood di un genere musicale e di un MusicalWork a partire dal mood espresso durante l'esecuzione di tale brano durante una partecipazione di un musicista ad un evento. La regola 2) permette di inferire il genere musicale suonato in un determinato locale deducendolo dalle partecipazioni dei musicisti tenutesi in quel luogo. La regola 3) permette di inferire la raccomandazione di un utente ad un altro utente analizzando qualora vivano nella stessa città e suonino generi simili. La regola 4) permette di inferire la raccomandazione di un genere musicale ad un utente basati sulla similitudine dei generi che suona. Analogamente la regola 5) permette di inferire la raccomandazione di un locale ad un utente basandosi sui generi che suona e che vengono suonati in quel locale. -->
