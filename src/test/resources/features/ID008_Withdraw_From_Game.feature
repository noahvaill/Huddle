@ID008 @wip
Feature: Withdraw from a Game

As a Huddle user who has joined a game
I want to withdraw from the game when I can no longer play
So that my spot is freed for someone else

Background:
  Given "Bob Tremblay" is logged in to Huddle
  And the following game requests exist:
    | request | creator      | sport      | date       | start | end   | spots remaining | status    | participants                        |
    | R1      | Alice Chen   | Squash     | 2026-10-15 | 18:00 | 18:45 | 0               | Confirmed | Alice Chen, Bob Tremblay            |
    | R2      | Carla Dubois | Basketball | 2026-10-15 | 19:00 | 20:00 | 3               | Open      | Carla Dubois, Bob Tremblay, Dan Lee |
    | R3      | Carla Dubois | Badminton  | 2026-10-16 | 17:00 | 18:00 | 2               | Open      | Carla Dubois, Dan Lee               |
    | R5      | Bob Tremblay | Squash     | 2026-10-18 | 18:00 | 18:45 | 1               | Open      | Bob Tremblay                        |

Scenario: Withdraw from an open game (Normal Flow)
  When "Bob Tremblay" withdraws from game request "R2"
  Then "Bob Tremblay" is not a participant of game request "R2"
  And game request "R2" has 4 spots remaining
  And the confirmation message "You have withdrawn from the game" is issued

Scenario: Withdraw from a confirmed game (Alternate Flow)
  When "Bob Tremblay" withdraws from game request "R1"
  Then "Bob Tremblay" is not a participant of game request "R1"
  And game request "R1" has 1 spot remaining
  And game request "R1" has status "Open"
  And the confirmation message "You have withdrawn from the game" is issued

Scenario: Withdraw from a game that you have not joined (Error Flow)
  When "Bob Tremblay" withdraws from game request "R3"
  Then game request "R3" has 2 spots remaining
  And the error message "You are not a participant of this game" is issued

Scenario: The creator attempts to withdraw from their own game (Error Flow)
  When "Bob Tremblay" withdraws from game request "R5"
  Then "Bob Tremblay" is a participant of game request "R5"
  And the error message "The creator cannot withdraw and must cancel the game request instead" is issued
