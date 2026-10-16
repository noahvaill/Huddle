@smoke
Feature: Build smoke test
  Checks that Cucumber is wired into the Gradle build.

  Scenario: The application name is Huddle
    When the application name is requested
    Then the name is "Huddle"
