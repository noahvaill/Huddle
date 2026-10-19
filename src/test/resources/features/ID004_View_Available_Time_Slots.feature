@ID004
Feature: View Available Time Slots

As a Huddle user
I want to see the available time slots for a sport on a given date
So that I can choose when to play

Background:
  Given "Alice Chen" is logged in to Huddle
  And the following time slots exist:
    | court             | sport     | date       | start | end   | status    |
    | Squash Court 1    | Squash    | 2026-10-15 | 18:00 | 18:45 | Available |
    | Squash Court 1    | Squash    | 2026-10-15 | 18:45 | 19:30 | Booked    |
    | Squash Court 2    | Squash    | 2026-10-15 | 19:30 | 20:15 | Available |
    | Badminton Court 2 | Badminton | 2026-10-15 | 17:00 | 18:00 | Available |
    | Squash Court 1    | Squash    | 2026-10-16 | 18:00 | 18:45 | Available |

Scenario: View the available time slots for a sport on a date (Normal Flow)
  When "Alice Chen" requests the available time slots for "Squash" on "2026-10-15"
  Then the following time slots are listed:
    | court          | date       | start | end   |
    | Squash Court 1 | 2026-10-15 | 18:00 | 18:45 |
    | Squash Court 2 | 2026-10-15 | 19:30 | 20:15 |

Scenario: View the available time slots when none are available (Alternate Flow)
  When "Alice Chen" requests the available time slots for "Tennis" on "2026-10-15"
  Then no time slots are listed
  And the message "No available time slots" is issued

Scenario: Request the available time slots for a sport that is not offered (Error Flow)
  When "Alice Chen" requests the available time slots for "Curling" on "2026-10-15"
  Then no time slots are listed
  And the error message "Sport not offered" is issued
