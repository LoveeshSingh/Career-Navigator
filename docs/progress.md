# Progress Log

... [previous contents] ...

## Update: Refactor to Rule-Based JD Parsing (Deterministic)
- **Action**: Completely removed `NlpSkillExtractionService` and associated `ExtractedSkillDto` / `nlp.api.*` configurations.
- **Action**: Implemented `JDParsingService` as the primary JD analysis engine.
- **Action**: Developed a deterministic scoring algorithm matching against the PostgreSQL registry (Skill + Aliases).
- **Rule**: +10 for "required/must", +5 for "preferred", +2 per occurrence.
- **Deduplication**: Centralized text normalization and regex boundary logic in `TextNormalizationUtils`.
- **Status**: Backend transitioned to a cost-free, reliable, and DB-driven architecture.
- **Documentation**: Updated `system-design.md`, `flow.md`, and `decisions.md` to reflect the new architecture.
