Feature: Publish Court Time Slots

As a facility administrator
I want to publish the time slots during which a court can be booked
So that students can see when the court is available to play

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role                   |
    | Frank Ho   | frank.ho@mcgill.ca        | Admin!2026  | Facility Administrator |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student                |
  And the following courts exist at "McGill Sports Complex":
    | court             | sport     |
    | Squash Court 1    | Squash    |
    | Badminton Court 2 | Badminton |
    | Tennis Court 3    | Tennis    |
  And the following time slots exist:
    | court          | sport  | date       | start | end   | status    |
    | Squash Court 1 | Squash | 2026-10-15 | 18:00 | 18:45 | Available |

Scenario: Publish a time slot for a court (Normal Flow)
  Given "Frank Ho" is logged in to Huddle
  When "Frank Ho" publishes a time slot for "Tennis Court 3" on "2026-10-15" from "18:00" to "19:00"
  Then an available time slot exists for "Tennis Court 3" on "2026-10-15" from "18:00" to "19:00"
  And the confirmation message "1 time slot published" is issued

Scenario: Publish several consecutive time slots at once (Alternate Flow)
  Given "Frank Ho" is logged in to Huddle
  When "Frank Ho" publishes time slots for "Badminton Court 2" on "2026-10-15" every 60 minutes from "17:00" to "20:00"
  Then the following available time slots exist for "Badminton Court 2" on "2026-10-15":
    | start | end   |
    | 17:00 | 18:00 |
    | 18:00 | 19:00 |
    | 19:00 | 20:00 |
  And the confirmation message "3 time slots published" is issued

Scenario: Publish a time slot that overlaps an existing time slot (Error Flow)
  Given "Frank Ho" is logged in to Huddle
  When "Frank Ho" publishes a time slot for "Squash Court 1" on "2026-10-15" from "18:30" to "19:15"
  Then no time slot exists for "Squash Court 1" on "2026-10-15" from "18:30" to "19:15"
  And the error message "Time slot overlaps an existing time slot" is issued

Scenario: A student attempts to publish a time slot (Error Flow)
  Given "Alice Chen" is logged in to Huddle
  When "Alice Chen" publishes a time slot for "Tennis Court 3" on "2026-10-15" from "18:00" to "19:00"
  Then no time slot exists for "Tennis Court 3" on "2026-10-15" from "18:00" to "19:00"
  And the error message "Only a facility administrator can publish time slots" is issued
