@ID014
Feature: Set Skill Level

As a Huddle user who plays sports
I want to set my skill level for each sport I play
So that other players know my level before they join or accept a game with me

Background:
  Given the following accounts exist in Huddle:
    | name       | email                     | password    | role    |
    | Alice Chen | alice.chen@mail.mcgill.ca | Racket!2026 | Student |
  And the following skill levels are offered: "Beginner", "Intermediate", "Advanced"
  And "Alice Chen" has the following skill levels:
    | sport     | level    |
    | Badminton | Beginner |
  And "Alice Chen" is logged in to Huddle

Scenario: Set a skill level for a new sport (Normal Flow)
  When "Alice Chen" sets her skill level for "Squash" to "Intermediate"
  Then "Alice Chen" has skill level "Intermediate" for "Squash"
  And the confirmation message "Skill level saved" is issued

Scenario: Change the skill level for a sport that already has one (Alternate Flow)
  When "Alice Chen" sets her skill level for "Badminton" to "Advanced"
  Then "Alice Chen" has skill level "Advanced" for "Badminton"
  And the confirmation message "Skill level saved" is issued

Scenario Outline: Set a skill level with an invalid sport or level (Error Flow)
  When "Alice Chen" sets her skill level for "<sport>" to "<level>"
  Then "Alice Chen" has no skill level for "<sport>"
  And the error message "<message>" is issued

  Examples:
    | sport   | level    | message                 |
    | Curling | Beginner | Sport not offered       |
    | Tennis  | Expert   | Skill level not offered |
