@ID011
Feature: Reset Password

As a registered Huddle user who has forgotten my password
I want to reset my password using a link sent to my McGill email address
So that I can regain access to my account

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role                   |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student                |
    | Frank Ho   | frank.ho@mcgill.ca        | Admin!2026  | Facility Administrator |
  And the current date and time is "2026-11-09 17:00"
  And the following password reset links exist:
    | link | email                     | expires          | status |
    | L1   | alice.chen@mail.mcgill.ca | 2026-11-09 18:00 | Unused |
    | L2   | alice.chen@mail.mcgill.ca | 2026-11-09 16:00 | Unused |
    | L3   | frank.ho@mcgill.ca        | 2026-11-09 18:00 | Used   |

Scenario: ID011_Request a password reset link (Normal Flow)
  When a password reset is requested for email "alice.chen@mail.mcgill.ca"
  Then a password reset link is sent to "alice.chen@mail.mcgill.ca"
  And the confirmation message "If an account exists for this email address, a password reset link has been sent" is issued

Scenario: ID011_Set a new password with a valid password reset link (Alternate Flow)
  When "Alice Chen" resets her password using link "L1" with new password "Shuttle!2026"
  Then "Alice Chen" can log in with email "alice.chen@mail.mcgill.ca" and password "Shuttle!2026"
  And "Alice Chen" cannot log in with email "alice.chen@mail.mcgill.ca" and password "Racket!2026"
  And password reset link "L1" has status "Used"
  And the confirmation message "Your password has been reset" is issued

Scenario: ID011_Request a password reset link for an email address with no account (Alternate Flow)
  When a password reset is requested for email "dan.lee@mail.mcgill.ca"
  Then no password reset link is sent to "dan.lee@mail.mcgill.ca"
  And the confirmation message "If an account exists for this email address, a password reset link has been sent" is issued

Scenario Outline: ID011_Set a new password with an expired or already used password reset link (Error Flow)
  When "<name>" resets their password using link "<link>" with new password "Shuttle!2026"
  Then "<name>" can log in with email "<email>" and password "<password>"
  And the error message "This password reset link is invalid or has expired" is issued

  Examples:
    | name       | link | email                     | password    |
    | Alice Chen | L2   | alice.chen@mail.mcgill.ca | Racket!2026 |
    | Frank Ho   | L3   | frank.ho@mcgill.ca        | Admin!2026  |
