---
marp: true
size: 16:9
---

# User story and use case description

---

## User Stories

The following user stories are the one that the use cases are built around.

1. As a musician, who just moved in town, i want to play my smart guitar with someone, so that i can have fun and meet new people.

2. As a beginner guitarist, who can't play at home because of the neighbors, i want to find a place where i can play my guitar, so that i can do my exercise and improve myself.

3. As a singer, i want to find some events, so that i can start to play in public and give my career a boost.

4. As a bar owner, i want to find musicians to play in my bar, so that i can attract more customers.

---

## Actors

The system will be accessible for different kind of actors

- Unlogged users
- Logged users
- Moderators users (Administrators of the system)
- Smart instruments (?)

---

### Logged Users Use Cases

Possible use cases for the users that are logged in the system.
The main idea is that the application will help the user to find other musicians or places where he can play  

|![width: 100%](./images/LoggedMusiUML.png)|
|:--:|
|*Complete schema of the logged user use cases*|
---

### Use Case "Play with someone"

**Abstract**:
It lets the user to discover and meet someone to play with.

**Description**:

1. Based on the user profile and the actual context the system will provide
other profiles whose match the user's one.[Exception 1]
2. The user can contact other musicians.
    2.1 The user can send a message to the other musician.
    2.2 The user can add as a friend the other musician.
3. The user can also refine some research criteria. [Exception 2]

**Exceptions**:

- Exception 1: It may be possible for the user to not find other musicians to play with. The system will report such error
- Exception 2: The criteria provided are incompatible.

---

#### Use Case "Play Alone"

**Abstract**:
The user can play along with the system.

**Description**:

1. The system will detect the actual musical context. [Exception 1]
2. The user can press a button to start the backing track provided by the system. [Exception 2]
3. The user can play along with the system.

**Exception**:

- Exception 1: The smart instrument can't detect or update the musical context of the user, or it may be wrong
- Exception 2: The system can't provide a backing track for the actual musical context.

---

#### Use Case "Update context"

**Abstract**:
The user can set or update his actual musical context.

**Description**:

1. The user lets the system to detect the actual musical context. [Exception 1]
2. The user can provide the context manually, adding more details.

**Exception**:

- Exception 1: The smart instrument can't detect or update the musical context of the user, or it may be wrong

---

#### Use Case "Check for event nearby"

**Abstract**:
The user can check if there are events around him where he can play.

**Description**:

1. The system will provide the user with a list of events corresponding to the user's interest. [Exception 1] [Exception 2]
2. The user can click on an event and the system will provide the user with the details of that event.
    2.1 The user can book himself at the event. [Exception 3]
        2.1.1 The user can choose to participate as a musician or as a spectator.
    2.2 The user can send that event to his friends.

**Exceptions**:

- Exception 1: There are no events near the user.
- Exception 2: The events are not matching the user's interest.
- Eccezione 3: The event is already full.

---

#### Use Case "Search for a place to play in"

**Abstract**:
The user can search available places where is possible to play with instruments, either for training or for playing in public.

**Description**:

1. The system will provide the user with a list of places where the user can go play his instrument and meet other musicians.
    1.1 The user can provide some other criteria to refine the search. [Exception 1]
2. The user clicks on that place and the system will provide the user with the details of that place.
   2.1 The user can add that place to his favorites.
3. The user can book a reservation for that place. [Exception 2]

**Exceptions**:

- Exception 1: There are no places near the user where he can play.
- Exception 2: There are no available spaces to reserve.

---

#### Use Case "Make yourself available"

**Abstract**:
The user can make himself available to play with other musicians.

**Description**:

1. Using the profile page, the user can set his availability to play with other musicians.
    1.1 The user can add some details about his availability (e.g. where he is, when he will be available etc.).

---

#### Use Case "Chat with friends"

**Abstract**:
The user can chat with his friends.

**Description**:

1. The user can open the chat page.
2. The user can select a friend to chat with. [Exception 1]
3. The user can send a message to the selected friend.

**Exceptions**:

- Exception 1: The user has no friends.

---

#### Use Case "Add a place"

**Abstract**:
The user can add a place open to the public where is possible to play.

**Description**:

1. The user can open the page to add a place.
2. The user can fill the form to add a place.
    2.1 The user adds the information about the place's availability.
    2.2 The user adds the information about the place's location.
3. The user can add an event that will be held in that place.
    3.1 The user can add the information about the event. [Exception 3]
4. The user can publish the place. [Exception 1-2]

**Exceptions**:

- Exception 1: The place is already present in the system.
- Exception 2: The place has some missing or wrong information.
- Exception 3: The event has some missing or wrong information.

---

#### Use Case "Create event"

**Abstract**:
The user can create an event.

**Description**:

1. The user can open the page to create an event.
2. The user fills the form to create an event.
    2.1 The user can add the information about the event. [Exception 1]
3. The user can publish the event. [Exception 2]

**Exceptions**:

- Exception 1: The event has some missing or wrong information.
- Exception 2: The event is already present in the system.

---

## Comments/Todo

- Gli attori sono ancora da definire.
- Mancano i casi d'uso "standard" per gestione profilo, login, registrazione, ecc.
- Non sono sicuro se gli smart instrument siano da inserire come attori. Non se se considerarli un tutt'uno col sistema o un attore a parte.
- Si potrebbe aggiungere un sistema per lasciare delle recensioni degli eventi o luoghi, volendo anche sui musicisti che si sono esibiti.
