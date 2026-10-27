@ID010
Feature: Confirm Game and Book Court

As the creator of a game request
I want the court to be booked automatically once all needed players have joined
So that I do not have to book the court separately

Background:
  Given "Bob Tremblay" is logged in to Huddle
  And the following time slots exist:
    | slot | court             | sport      | date       | start | end   | status    |
    | S1   | Squash Court 1    | Squash     | 2026-10-15 | 18:00 | 18:45 | Available |
    | S3   | Basketball Court  | Basketball | 2026-10-15 | 19:00 | 20:00 | Available |
    | S4   | Badminton Court 2 | Badminton  | 2026-10-16 | 17:00 | 18:00 | Available |
  And the following game requests exist:
    | request | creator      | slot | spots remaining | status | participants |
    | R1      | Alice Chen   | S1   | 1               | Open   | 1            |
    | R2      | Carla Dubois | S3   | 1               | Open   | 9            |
    | R4      | Carla Dubois | S4   | 1               | Open   | 1            |

Scenario: The last needed player joins a two-player game (Normal Flow)
  When "Bob Tremblay" joins game request "R1"
  Then game request "R1" has status "Confirmed"
  And time slot "S1" has status "Booked"
  And game request "R1" has 2 participants
  And the confirmation message "Game confirmed on Squash Court 1 on 2026-10-15 at 18:00" is issued

Scenario: The last needed player joins a group game (Alternate Flow)
  When "Bob Tremblay" joins game request "R2"
  Then game request "R2" has status "Confirmed"
  And time slot "S3" has status "Booked"
  And game request "R2" has 10 participants
  And the confirmation message "Game confirmed on Basketball Court on 2026-10-15 at 19:00" is issued

Scenario: The last needed player joins but the time slot is no longer available (Error Flow)
  Given the time slot "S4" is booked by a facility administrator before the join is completed
  When "Bob Tremblay" joins game request "R4"
  Then "Bob Tremblay" is not a participant of game request "R4"
  And "Carla Dubois" receives the notification "The Badminton game on 2026-10-16 at 17:00 has been cancelled: time slot no longer available"
  And the error message "Time slot is no longer available" is issued
