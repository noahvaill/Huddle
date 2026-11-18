@ID001
Feature: Create Account

As a McGill student or staff member
I want to create a Huddle account with my McGill email address
So that I can post and join games

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role    |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student |

Scenario: ID001_Create an account with a McGill student email address (Normal Flow)
  When "Bob Tremblay" requests an account with email "bob.tremblay@mail.mcgill.ca" and password "Racket!2026"
  Then an account exists for "bob.tremblay@mail.mcgill.ca" with name "Bob Tremblay"
  And the confirmation message "Account created" is issued

Scenario: ID001_Create an account with a McGill staff email address (Alternate Flow)
  When "Carla Dubois" requests an account with email "carla.dubois@mcgill.ca" and password "Court#2026"
  Then an account exists for "carla.dubois@mcgill.ca" with name "Carla Dubois"
  And the confirmation message "Account created" is issued

Scenario Outline: ID001_Create an account with an email address that is not a McGill address (Error Flow)
  When "<name>" requests an account with email "<email>" and password "Racket!2026"
  Then no account exists for "<email>"
  And the error message "A McGill email address is required" is issued

  Examples:
    | name    | email                |
    | Dan Lee | dan.lee@gmail.com    |
    | Eva Roy | eva.roy@umontreal.ca |

Scenario: ID001_Create an account with an email address that is already in use (Error Flow)
  When "Alice Chen" requests an account with email "alice.chen@mail.mcgill.ca" and password "Racket!2026"
  Then only one account exists for "alice.chen@mail.mcgill.ca"
  And the error message "An account already exists for this email address" is issued
