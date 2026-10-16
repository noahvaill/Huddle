# How to run the tests

Huddle uses Gradle with JUnit 5 for unit tests and Cucumber for acceptance tests.
Everything runs through the Gradle wrapper, so you do not need to install Gradle.

## Requirements

- JDK 21 or newer (`java -version` to check)
- On Windows, use `gradlew.bat` instead of `./gradlew`

## Commands

| What | Command |
|---|---|
| Full build (compile + all tests), required before every PR | `./gradlew build` |
| All tests only | `./gradlew test` |
| One story's Cucumber scenarios | `./gradlew test -Dcucumber.filter.tags=@ID004` |
| Several stories | `./gradlew test -Dcucumber.filter.tags="@ID004 or @ID005"` |
| One JUnit test class | `./gradlew test --tests ca.mcgill.ecse428.huddle.HuddleTest` |
| Force tests to rerun when nothing changed | `./gradlew test --rerun` |

Scenarios that don't match the tag filter are reported as `SKIPPED`. JUnit tests always run.

## Reports

After a run, open these in a browser:

- JUnit and overall results: `build/reports/tests/test/index.html`
- Cucumber scenarios: `build/reports/cucumber/cucumber.html`

## Continuous integration

GitHub Actions runs `./gradlew build` on JDK 21 for every push and every pull request
(workflow: `.github/workflows/build.yml`). The result shows as a check on the pull request,
and a pull request can only be merged when the check is green.

- **See why a run failed**: open the pull request, click **Details** next to the *Build and test* check,
  and read the log of the *Build and run all tests* step.
- **Test reports**: each run uploads the JUnit and Cucumber HTML reports as the `test-reports`
  artifact, at the bottom of the run's summary page.
- **Re-run**: from the run page, **Re-run jobs**, or from the *Actions* tab, *Build* → **Run workflow**.

If CI fails on `main`, fixing it comes before anything else.

## Where things go

| File | Location |
|---|---|
| Production code | `src/main/java/ca/mcgill/ecse428/huddle/` |
| JUnit tests | `src/test/java/ca/mcgill/ecse428/huddle/` (same package as the class under test) |
| Feature files | `src/test/resources/features/` (e.g. `ID004_View_Available_Time_Slots.feature`) |
| Step definitions | `src/test/java/ca/mcgill/ecse428/huddle/features/` |

Tag each feature file with its story ID on the line above `Feature:` (e.g. `@ID004`).

Cucumber fails the build when a step has no step definition. Add a feature file in the same
PR as its step definitions, or `./gradlew build` will fail on `main`.

## Troubleshooting

- **`Permission denied: ./gradlew`**: run `chmod +x gradlew`.
- **`Unsupported class file major version` or a compile error about `release 21`**: your JDK is older than 21.
- **Step reported as undefined**: the step definition class must be in the
  `ca.mcgill.ecse428.huddle.features` package (or a subpackage), and the step text must match exactly.
