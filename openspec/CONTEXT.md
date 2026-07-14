# OpenSpec Context

## Repository
- **Type**: Skills repository for Claude Code
- **Primary Language**: JavaScript, Python
- **Structure**: skills/, scripts/, agents/, docs/

## Standards
- Code: PEP 8 for Python, ESLint for JavaScript
- Testing: Jest for JS, pytest for Python
- Documentation: Markdown with frontmatter

## Naming Conventions
- Change IDs: `{feature}-{sequence}` (e.g., heap-sort-001)
- Files: snake_case for Python, camelCase for JS
- Directories: kebab-case

## Workflow
1. Create proposal in `openspec/changes/{id}/`
2. Review and approve
3. Implement with tests
4. Verify and close