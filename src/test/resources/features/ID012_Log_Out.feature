@ID012
Feature: Log Out

As a Huddle user who is logged in
I want to log out of Huddle
So that no one else can access my account from the device I was using

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role                   |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student                |
    | Frank Ho   | frank.ho@mcgill.ca        | Admin!2026  | Facility Administrator |

Scenario: Log out as a student (Normal Flow)
  Given "Alice Chen" is logged in to Huddle
  When "Alice Chen" logs out
  Then "Alice Chen" does not have an active session in Huddle
  And the confirmation message "You have been logged out" is issued

Scenario: Log out as a facility administrator (Alternate Flow)
  Given "Frank Ho" is logged in to Huddle
  When "Frank Ho" logs out
  Then "Frank Ho" does not have an active session in Huddle
  And the confirmation message "You have been logged out" is issued

Scenario: Log out on one device while logged in on another device (Alternate Flow)
  Given "Alice Chen" is logged in to Huddle on device "Laptop"
  And "Alice Chen" is logged in to Huddle on device "Phone"
  When "Alice Chen" logs out on device "Laptop"
  Then "Alice Chen" does not have an active session in Huddle on device "Laptop"
  And "Alice Chen" has an active session in Huddle on device "Phone"
  And the confirmation message "You have been logged out" is issued

Scenario: Attempt to log out without an active session (Error Flow)
  Given no active session exists in Huddle
  When a user attempts to log out
  Then no active session exists in Huddle
  And the error message "You are not logged in" is issued
