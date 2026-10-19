@ID005
Feature: Post an Open Game Request

As a Huddle user
I want to post an open game request for an available time slot
So that other users can find my game and join it

Background:
  Given "Alice Chen" is logged in to Huddle
  And the following time slots exist:
    | slot | court             | sport      | date       | start | end   | status    |
    | S1   | Squash Court 1    | Squash     | 2026-10-15 | 18:00 | 18:45 | Available |
    | S2   | Squash Court 1    | Squash     | 2026-10-15 | 18:45 | 19:30 | Booked    |
    | S3   | Basketball Court  | Basketball | 2026-10-15 | 19:00 | 20:00 | Available |
    | S4   | Badminton Court 2 | Badminton  | 2026-10-15 | 18:00 | 19:00 | Available |
  And the maximum number of players per sport is:
    | sport      | max players |
    | Squash     | 2           |
    | Badminton  | 4           |
    | Basketball | 10          |

Scenario: Post a request for one partner (Normal Flow)
  When "Alice Chen" posts an open game request for time slot "S1" needing 1 more player
  Then an open game request exists for time slot "S1" created by "Alice Chen" with 1 spot remaining
  And the confirmation message "Game request posted" is issued

Scenario: Post a request on behalf of a group looking for another group (Alternate Flow)
  When "Alice Chen" posts an open game request for time slot "S3" for a group of 5 players needing 5 more players
  Then an open game request exists for time slot "S3" created by "Alice Chen" with 5 spots remaining
  And the confirmation message "Game request posted" is issued

Scenario: Post a request for a time slot that is already booked (Error Flow)
  When "Alice Chen" posts an open game request for time slot "S2" needing 1 more player
  Then no open game request exists for time slot "S2"
  And the error message "Time slot is not available" is issued

Scenario Outline: Post a request with an invalid number of players (Error Flow)
  When "Alice Chen" posts an open game request for time slot "<slot>" needing <players> more players
  Then no open game request exists for time slot "<slot>"
  And the error message "<message>" is issued

  Examples:
    | slot | players | message                                 |
    | S1   | 0       | At least 1 more player is required      |
    | S1   | 3       | Squash allows at most 1 more player     |
    | S4   | 5       | Badminton allows at most 3 more players |

Scenario: Post a request that overlaps another game of the same user (Error Flow)
  Given "Alice Chen" has an open game request for time slot "S4"
  When "Alice Chen" posts an open game request for time slot "S1" needing 1 more player
  Then no open game request exists for time slot "S1"
  And the error message "You already have a game at this time" is issued
