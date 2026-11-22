# TID030: ID011 password-reset review

Reviewed merged PRs #40 (reset password), #46 (request link), and #49 (Cucumber scenarios), against the ID011 feature on main at 0c17fdfc65c7d5528625f5a952708ddddcc29135.

## Findings

1. **Reset links are not actually delivered.** The requestResetLink method generates and returns a link, and the Cucumber step records it in an in-memory list; neither sends an email. PR #46 explicitly says the link “would be emailed.” The current tests verify a simulated handoff, not email delivery. If email delivery is expected in this sprint, add a delivery interface and integration test; otherwise track it as future work.

2. **Previously issued links remain valid after a password reset.** Each request creates an independent token, and resetPassword marks only the submitted token as used. If two links exist, using the first does not invalidate the second before it expires. Confirm the intended policy and, if only the latest reset should remain valid, revoke the other outstanding links and test that case.

3. **Single-use behavior is not protected against concurrent requests.** resetPassword checks link.isUsed(), then changes the password, then marks the link used; these operations are not atomic. Concurrent requests could both pass the check. This is a risk if AccountService is accessed concurrently; make token consumption atomic in the persistence layer and test concurrent redemption if that usage is in scope.

## Verification

The ID011 Cucumber scenarios passed within the manual Java/JUnit test run against the snapshot used for the logout branch. Full Gradle execution was blocked by the environment's socket restriction. No changes or review comments were posted to the original PRs.
