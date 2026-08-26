# REVIEW.md — Project Review Profile

Project review profile for this repository (skills collection with pipeline-generated
demo deliverables).

## Scope of this profile
- Applies to pipeline-created demo modules under `src/main/java`/`src/test/java`
  (e.g. the HelloWorld example).
- Business modules live under `src/account-login` (Python) and `src/file-service`;
  those follow their own language-specific review gates.

## Java module gates
- Public methods must handle `null`/blank inputs with a documented default.
- No shared mutable state; prefer stateless or effectively-immutable classes.
- JUnit 5 (`org.junit.jupiter`) + AssertJ for assertions.
- Every public method and the `main` entry point must have test coverage.
- Keep the class surface minimal; do not add methods/fields the task does not ask for.

## Evidence expectations
- Tests must exercise the real method through the public API, not via reflection or
  mocks, for this trivial stateless code.
- Edge cases (null, whitespace, trimmed input) should be covered across the variants.

## Non-goals
- Do not apply the skills-repo SLI (skill anatomy) gates to plain demo Java modules;
  those apply only to `skills/<name>/SKILL.md`.