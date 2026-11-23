@ID013
Feature: Edit Profile

As a registered Huddle user
I want to change the name and password of my account
So that other players see my correct name and my account stays secure

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role                   |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student                |
    | Frank Ho   | frank.ho@mcgill.ca        | Admin!2026  | Facility Administrator |
  And "Alice Chen" is logged in to Huddle

Scenario: Change the name of the account (Normal Flow)
  When "Alice Chen" changes her name to "Alice Chen-Roy"
  Then the account for "alice.chen@mail.mcgill.ca" has name "Alice Chen-Roy"
  And the confirmation message "Profile updated" is issued

Scenario: Change the password of the account (Alternate Flow)
  When "Alice Chen" changes her password from "Racket!2026" to "Shuttle!2026"
  Then "Alice Chen" can log in with email "alice.chen@mail.mcgill.ca" and password "Shuttle!2026"
  And "Alice Chen" cannot log in with email "alice.chen@mail.mcgill.ca" and password "Racket!2026"
  And the confirmation message "Profile updated" is issued

Scenario: Change the password with an incorrect current password (Error Flow)
  When "Alice Chen" changes her password from "WrongPass1" to "Shuttle!2026"
  Then "Alice Chen" can log in with email "alice.chen@mail.mcgill.ca" and password "Racket!2026"
  And the error message "Current password is incorrect" is issued

Scenario: Change the name to an empty name (Error Flow)
  When "Alice Chen" changes her name to ""
  Then the account for "alice.chen@mail.mcgill.ca" has name "Alice Chen"
  And the error message "Name cannot be empty" is issued
