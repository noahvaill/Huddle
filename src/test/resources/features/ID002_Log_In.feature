@ID002
Feature: Log In

As a registered Huddle user
I want to log in with my email address and password
So that I can access my games and game requests

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role                   |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student                |
    | Frank Ho   | frank.ho@mcgill.ca        | Admin!2026  | Facility Administrator |

Scenario: ID002_Log in as a student (Normal Flow)
  When "Alice Chen" logs in with email "alice.chen@mail.mcgill.ca" and password "Racket!2026"
  Then "Alice Chen" has an active session in Huddle as a "Student"
  And the confirmation message "Welcome back, Alice Chen" is issued

Scenario: ID002_Log in as a facility administrator (Alternate Flow)
  When "Frank Ho" logs in with email "frank.ho@mcgill.ca" and password "Admin!2026"
  Then "Frank Ho" has an active session in Huddle as a "Facility Administrator"
  And the confirmation message "Welcome back, Frank Ho" is issued

Scenario Outline: ID002_Log in with an incorrect email address or password (Error Flow)
  When "<name>" logs in with email "<email>" and password "<password>"
  Then no active session exists in Huddle
  And the error message "Invalid email address or password" is issued

  Examples:
    | name       | email                     | password    |
    | Alice Chen | alice.chen@mail.mcgill.ca | WrongPass1  |
    | Dan Lee    | dan.lee@mail.mcgill.ca    | Racket!2026 |