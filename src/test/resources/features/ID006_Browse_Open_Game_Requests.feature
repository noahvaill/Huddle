@ID006
Feature: Browse Open Game Requests

As a Huddle user looking for a game
I want to see the open game requests for a sport
So that I can find a game to join

Background:
  Given "Bob Tremblay" is logged in to Huddle
  And the following game requests exist:
    | request | creator      | sport     | court             | date       | start | end   | spots remaining | status    |
    | R1      | Alice Chen   | Squash    | Squash Court 1    | 2026-10-15 | 18:00 | 18:45 | 1               | Open      |
    | R2      | Carla Dubois | Squash    | Squash Court 2    | 2026-10-16 | 19:30 | 20:15 | 1               | Open      |
    | R3      | Alice Chen   | Squash    | Squash Court 1    | 2026-10-17 | 18:00 | 18:45 | 0               | Confirmed |
    | R4      | Carla Dubois | Badminton | Badminton Court 2 | 2026-10-15 | 17:00 | 18:00 | 2               | Open      |
    | R5      | Bob Tremblay | Squash    | Squash Court 2    | 2026-10-18 | 18:00 | 18:45 | 1               | Open      |

Scenario: Browse the open game requests for a sport (Normal Flow)
  When "Bob Tremblay" requests the open game requests for "Squash"
  Then the following game requests are listed:
    | request | creator      | court          | date       | start | end   | spots remaining |
    | R1      | Alice Chen   | Squash Court 1 | 2026-10-15 | 18:00 | 18:45 | 1               |
    | R2      | Carla Dubois | Squash Court 2 | 2026-10-16 | 19:30 | 20:15 | 1               |

Scenario: Browse the open game requests for a sport with no open requests (Alternate Flow)
  When "Bob Tremblay" requests the open game requests for "Tennis"
  Then no game requests are listed
  And the message "No open game requests" is issued

Scenario: Browse the open game requests for a sport that is not offered (Error Flow)
  When "Bob Tremblay" requests the open game requests for "Curling"
  Then no game requests are listed
  And the error message "Sport not offered" is issued
