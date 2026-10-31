@ID009
Feature: Cancel a Game Request

As the creator of a game request
I want to cancel my game request
So that the players who joined know the game will not take place and the court is freed

Background:
  Given "Alice Chen" is logged in to Huddle
  And the following time slots exist:
    | slot | court             | sport     | date       | start | end   | status    |
    | S1   | Squash Court 1    | Squash    | 2026-10-15 | 18:00 | 18:45 | Booked    |
    | S2   | Badminton Court 2 | Badminton | 2026-10-16 | 17:00 | 18:00 | Available |
    | S3   | Badminton Court 1 | Badminton | 2026-10-18 | 18:00 | 19:00 | Available |
  And the following game requests exist:
    | request | creator      | slot | spots remaining | status    | participants             |
    | R1      | Alice Chen   | S1   | 0               | Confirmed | Alice Chen, Bob Tremblay |
    | R2      | Alice Chen   | S2   | 2               | Open      | Alice Chen, Dan Lee      |
    | R3      | Carla Dubois | S3   | 2               | Open      | Carla Dubois, Alice Chen |

Scenario: Cancel an open game request (Normal Flow)
  When "Alice Chen" cancels game request "R2"
  Then game request "R2" has status "Cancelled"
  And "Dan Lee" receives the notification "The Badminton game on 2026-10-16 at 17:00 has been cancelled by the creator"
  And "Alice Chen" receives no notification
  And time slot "S2" has status "Available"
  And the confirmation message "Game request cancelled" is issued

Scenario: Cancel a confirmed game request (Alternate Flow)
  When "Alice Chen" cancels game request "R1"
  Then game request "R1" has status "Cancelled"
  And time slot "S1" has status "Available"
  And "Bob Tremblay" receives the notification "The Squash game on 2026-10-15 at 18:00 has been cancelled by the creator"
  And "Alice Chen" receives no notification
  And the confirmation message "Game request cancelled" is issued

Scenario: Cancel a game request created by another user (Error Flow)
  When "Alice Chen" cancels game request "R3"
  Then game request "R3" has status "Open"
  And "Carla Dubois" receives no notification
  And the error message "Only the creator can cancel this game request" is issued

Scenario: Cancel a game request that does not exist (Error Flow)
  When "Alice Chen" cancels game request "R9"
  Then the error message "Game request not found" is issued
