# Architectural Decisions Log

## 1. Resume Matching Modality
- **Decision**: Use 100% deterministic text matching (name and aliases) against a canonical DB of skills.
- **Reason**: Using NLP or LLMs for resume matching introduces hallucinations and non-determinism. Deterministic matching ensures absolute confidence.

## 2. LLM Scope Constraints
- **Decision**: Limit LLM operation strictly to translating a formal list of `missing_skills` into a structured learning roadmap.
- **Reason**: We want the AI to synthesize pedagogical content, NOT perform analytical extraction.

## 3. Rule-Based JD Parsing (Deterministic)
- **Decision**: Replace external NLP API with an internal, rule-based `JDParsingService`.
- **Reason**: External NLP APIs introduce costs, latency, and dependency risks. A deterministic, DB-driven approach ensures 100% reliability and zero cost.

## 4. Database as Absolute Truth for Selection
- **Decision**: The PostgreSQL database strictly determines what skills move forward into the matching engine.
- **Reason**: By matching against the `Skill` or `SkillAlias` tables, we ensure that every skill presented to the user has a canonical learning path and fallback video.

## 5. Normalization and Relational Schema Design
- **Decision**: Break out `SkillAlias` into a dedicated table rather than storing aliases as a JSONB array.
- **Reason**: Search performance for exact string matching is highly optimized in indexed PostgreSQL columns.

## 6. Pre-Seeded Database Necessity
- **Decision**: Deploy initial application state via a dedicated `DataSeeder`.
- **Reason**: The system relies on a "source of truth" to validate dynamic extraction. Seeding core data ensures the app is functional out-of-the-box.

## 7. Absolute Fallback Video Delegation
- **Decision**: Forced fallback into `FallbackService` upon LLM timeout or error.
- **Reason**: Guarantees uptime even if external Generative AI services are unavailable.
