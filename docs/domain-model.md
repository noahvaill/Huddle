# Huddle Domain Model

This is the domain model for the Sprint A stories (ID004, ID005, ID006, ID007, ID008 and ID010). It is based on the scenarios in our feature files and will be updated during the sprint if needed.

The diagram is written in [Mermaid](https://mermaid.js.org/syntax/classDiagram.html): GitHub renders it in this file, and it can be edited or exported as an image at <https://mermaid.live>.

```mermaid
classDiagram
    class Sport {
        name : String
        maxPlayers : Integer
    }
    class Court {
        name : String
    }
    class TimeSlot {
        date : Date
        startTime : Time
        endTime : Time
        status : TimeSlotStatus
    }
    class FacilityAdministrator {
        name : String
    }
    class User {
        name : String
    }
    class Notification {
        message : String
    }
    class GameRequest {
        groupSize : Integer
        spotsRemaining : Integer
        status : GameRequestStatus
    }
    class TimeSlotStatus {
        <<enumeration>>
        Available
        Booked
    }
    class GameRequestStatus {
        <<enumeration>>
        Open
        Confirmed
        Cancelled
    }

    Sport "1" -- "0..*" Court : is for
    Court "1" -- "0..*" TimeSlot : has
    FacilityAdministrator "0..1" -- "0..*" TimeSlot : books
    TimeSlot "1" -- "0..*" GameRequest : is for
    User "1 recipient" -- "0..* notifications" Notification
    User "1 creator" -- "0..* createdRequests" GameRequest
    User "1..* participants" -- "0..* joinedRequests" GameRequest

    note for GameRequest "The creator is always a participant\nand cannot withdraw (ID008)"
```

## Classes

- **Sport**: a sport offered in Huddle. maxPlayers is the max number of players in one game (Squash 2, Badminton 4, Basketball 10).
- **Court**: a court for one sport (e.g. Squash Court 1).
- **TimeSlot**: a period on a court. Its status is Available or Booked.
- **GameRequest**: a game posted by a user for a time slot. groupSize is the number of players the creator brings and spotsRemaining is how many more players are needed.
- **User**: a Huddle user. A user can create game requests and join other users' game requests, and receives notifications.
- **Notification**: a message sent to a user, e.g. when their game is cancelled because its time slot was booked.
- **FacilityAdministrator**: a McGill facility administrator, who can book a time slot directly.
- **TimeSlotStatus**: Available, or Booked once a game is confirmed or an administrator books the slot.
- **GameRequestStatus**: Open while spots remain, Confirmed when the last spot is filled, Cancelled when the time slot is no longer available. A Confirmed game goes back to Open when a participant withdraws.

## Withdrawing from a game (ID008)

ID008 adds no new class, attribute or association. Withdrawing changes the existing links and values:

- The withdrawing user is removed from the game request's participants (and the game request from their joinedRequests), and spotsRemaining goes up by 1.
- If the game request was Confirmed, its status goes back to Open and its time slot goes from Booked back to Available, so the game can fill up and be confirmed again.
- Only a participant can withdraw ("You are not a participant of this game").
- The creator cannot withdraw and must cancel the game request instead. Because the creator is always one of the participants, a game request always keeps at least one participant, which is what the 1..* participants multiplicity requires.
