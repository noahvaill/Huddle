@ID007
Feature: Join an Open Game Request

As a Huddle user
I want to join an open game request posted by another user
So that I have people to play with at that time

Background:
  Given "Bob Tremblay" is logged in to Huddle
  And the following game requests exist:
    | request | creator      | sport      | date       | start | end   | spots remaining | status    | participants           |
    | R1      | Alice Chen   | Squash     | 2026-10-15 | 18:00 | 18:45 | 1               | Open      | Alice Chen             |
    | R2      | Carla Dubois | Basketball | 2026-10-15 | 19:00 | 20:00 | 5               | Open      | Carla Dubois           |
    | R3      | Alice Chen   | Squash     | 2026-10-17 | 18:00 | 18:45 | 0               | Confirmed | Alice Chen, Dan Lee    |
    | R4      | Bob Tremblay | Squash     | 2026-10-18 | 18:00 | 18:45 | 1               | Open      | Bob Tremblay           |
    | R5      | Dan Lee      | Badminton  | 2026-10-19 | 17:00 | 18:00 | 2               | Open      | Dan Lee, Bob Tremblay  |
    | R6      | Eva Roy      | Tennis     | 2026-10-19 | 17:30 | 18:30 | 1               | Open      | Eva Roy                |

Scenario: Join a game request with one spot remaining (Normal Flow)
  When "Bob Tremblay" joins game request "R1"
  Then "Bob Tremblay" is a participant of game request "R1"
  And game request "R1" has 0 spots remaining
  And the confirmation message "You have joined the game" is issued

Scenario: Join a group game request with several spots remaining (Alternate Flow)
  When "Bob Tremblay" joins game request "R2"
  Then "Bob Tremblay" is a participant of game request "R2"
  And game request "R2" has 4 spots remaining
  And game request "R2" has status "Open"
  And the confirmation message "You have joined the game" is issued

Scenario: Join a game request that is already full (Error Flow)
  When "Bob Tremblay" joins game request "R3"
  Then "Bob Tremblay" is not a participant of game request "R3"
  And the error message "This game is full" is issued

Scenario: Join your own game request (Error Flow)
  When "Bob Tremblay" joins game request "R4"
  Then game request "R4" has 1 spot remaining
  And the error message "You cannot join your own game request" is issued

Scenario: Join a game request that you have already joined (Error Flow)
  When "Bob Tremblay" joins game request "R5"
  Then game request "R5" has 2 spots remaining
  And the error message "You have already joined this game" is issued

Scenario: Join a game request that overlaps another game of the same user (Error Flow)
  When "Bob Tremblay" joins game request "R6"
  Then "Bob Tremblay" is not a participant of game request "R6"
  And game request "R6" has 1 spot remaining
  And the error message "You already have a game at this time" is issued
