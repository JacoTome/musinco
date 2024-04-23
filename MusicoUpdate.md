# Musico Update

## TODO

correggere prefissi: OK

- Come mettere assiomi in OWL
- Impara come fare gli assiomi in Protege

- Sistema documento con le cose da rimuovere

- Trovare nuove regole/assiomi

<!-- Descrivere assiomi su un file separato
Journal of WebSemantics cerca articoli su ontologie per cercare assiomi -->

## Appunti prototipo

Link ad altri social/pagine

Spezzoni di canzoni caricati dall'utente

Follower(?)

icone strumenti

prenotazioni eventi, mappe,

Eventi privati

Annunci per eventi

suggerimenti per eventi

Descrizione utente

## To ADD

- Something to express real time mood of HumanMusician

- Segnare se 2 artisti hanno già suonato insieme -> gets_recommendation_user

- recognized_mood domains: MusicalWork, Genre, MusicianParticipation

- Integrare DBPedia Generi, lavori, artisti tutto //OK
    - Matchare concetti Equivalenza
- Add recommendation for a venue

- Inferire indirizzo per la performance

- accessibilità per disabili alla music venue -> se un musicista con disabilità suona in una venue, allora la venue è accessibile per disabili

- livello della professionalità della venue -> se un artista suona in una venue, allora la venue è di un certo livello di professionalità

- AGGIUNGERE STORICO

- Altri assiomi SWRL

- SCADENZA 10 LUGLIO !! 

## General Description

<!-- Con l'ottica di creare informazioni da suggerire all'utente finale, le entità che sono state aggiunte e modificate sull'ontologia Musico mirano, principalmente, ad aumentare le informazioni tra i concetti che ruotano attorno alla figura del musicista. Sono state create quindi delle relazioni che collegano concetti non connessi direttamente al musicista, ma le cui informazioni dedotte con ragionamento automatico possono essere suggerite al musicista per migliorare la sua esperienza. -->

In order to creating information to be suggested to the end user, the entities that have been added and modified on the Musico ontology aim, primarily, to augment the information between concepts revolving around the figure of the musician.
The basic idea is to increase the amount of data that can later be related to the musico:Artist class.
In this way, relationships have been created that relate previously disconnected concepts in a way that further enriches the knowledge graph. With these new relationships, it is possible to do automatic reasoning to infer new information that can be suggested to the musician to improve his experience. This information can be used to suggest new music genres or instrument to play, new people to meet, or new venues to perform in.

## Object Properties

- **ADDED** - _played_genre_:

  - Used to indicate what genre is played in a particular MusicVenue
    - _Domain_: schema:MusicVenue
    - _Range_: mo:Genre

- **ADDED** - _gets_recommendation_:

  - Used to indicate the a musician gets a recommendation for any kind of thing
    <!-- :Artist :Artist -->
    - _Domain_: foaf:Agent
    - _Range_: owl:Thing

- **ADDED** - _gets_recommendation_user_:

  - Used to indicate the a musician gets a recommendation from another musician
  - _Domain_: foaf:Agent
  - _Range_: foaf:Agent

- **ADDED** - _gets_recommendation_genre_:

  - Used to indicate the a musician gets a recommendation for a genre of music
  - _Domain_: musico:HumanMusician
  - _Range_: mo:Genre

- **ADDED** - _gets_recommendation_instrument_:

  - Used to indicate the a musician gets a recommendation for an instrument to play
  - _Domain_: musico:HumanMusician
  - _Range_: mo:Instrument or iomust:SmartInstrument

- **MODIFIED** - _musico:plays_genre_:

  - Changed the domain with the addition of the Artist class
    <!-- musico:Musician :Artist mo:Genre -->
    - _Domain_: musico:Musician or musico:MusiciansGroup
    - _Range_: mo:Genre

- **MODIFIED** - _recognized_mood_:
  - Changed the domain with the addition of the Artist class
  <!-- musico:MusicalWork musico:Genre musico:MusicianParticipation -->
  - _Domain_: musico:MusicalWork musico:Genre musico:MusicianParticipation
  - _Range_: obo:MFOEM_000193

## Rules

<!-- Di seguito sono elencate delle regole costruite per inferire nuove informazioni con le nuove relazioni per popolare il database di GraphDB by Ontotext. -->

Listed below are rules constructed to infer new information using the new relationships. These rules are written using SWRL (Semantic Web Rule Language).

```swrl

genreMood: Genre(?gen) ^ MusicalWork(?work) ^ genre(?work, ?gen) ^ expressed_mood(?work, ?mood) -> recognized_mood(?gen, ?mood)

workMood: MusicalWork(?work) ^ MusicianParticipation(?part) ^ played_musical_work(?part, ?work) ^ expressed_mood(?part, ?mood) -> recognized_mood(?work, ?mood)

venueGenre: MusicianParticipation(?part) ^ MusicalEvent(?ev) ^ MusicVenue(?pl) ^ Genre(?gen) ^ involved_event(?part, ?ev) ^ location(?ev, ?pl) ^ played_musical_work(?part, ?work) ^ genre(?work, ?gen) -> played_genre(?pl, ?gen)

userRecommendation: HumanMusician(?u) ^ HumanMusician(?o) ^ swrlb:notEqual(?u, ?o) ^ foaf:based_near(?u, ?city) ^ foaf:based_near(?o, ?city) ^ plays_genre(?u, ?gen) ^ similar_to(?gen, ?ggen) ^ plays_genre(?o, ?ggen) -> gets_recommended_user(?u, ?o)

genreRecommendation: HumanMusician(?u) ^ plays_genre(?u, ?gen) ^ similar_to(?gen,?ggen) ^ swrlb:notEqual(?gen, ?ggen)-> gets_recommended_genre(?u, ?ggen)


```

<!--
```pie
Id: workMood: AXIOM
		p <rdf:type> <musico:MusicianParticipation>
		p <musico:played_musical_work> w
		p <musico:expressed_mood> m
		-----------------------------------
		w <musico:recognized_mood> m

	Id: genreMood: AXIOM
		p <rdf:type> <mo:MusicalWork>
		w <mo:genre> g
		w <musico:recognized_mood> m
		-----------------------------------
		g <musico:recognized_mood> m

	Id: instrGenre
		p <rdf:type> <musico:MusicianParticipation>
		p <musico:played_instrument> i
		p <musico:played_musical_work> w
		w <mo:genre> g
		-----------------------------------
		i <musico:used_to_play> g

	Id: musicianGroupGenre
		a <musico:has_group> mg
		a <musico:plays_genre> g
		--------------------------
		mg <musico:plays_genre> g

	Id: venueGenre // AGGIORNARE AXIOM
		p <rdf:type> <musico:MusicianParticipation>
		p <musico:involved_event> e
		s <owl:sameAs> e
		s <schema:address> v
		p <musico:played_musical_work> w
		w <mo:genre> g
		-----------------------------------
		v <musico:played_genre> g

	Id: suggestedUser AXIOM
		u <rdf:type> <musico:HumanMusician>
		o <rdf:type> <musico:HumanMusician>
		u <foaf:based_near> c
		o <foaf:based_near> c
		u <musico:plays_genre> g
		o <musico:plays_genre> gg
		gg <mo:similar_to> g [Constraint g != gg]
		u p o [Constraint p != <owl:sameAs>,
                p != <foaf:knows>,
                p !=< musico:suggested_user>]
		-----------------------------------
		u <musico:suggested_user> o [Constraint u != o]
``` -->
