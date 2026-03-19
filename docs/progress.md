# Progress Log

## Update: Documentation Project Setup
- **Action**: Created core documentation directory (`docs`).
- **Action**: Initialized core documentation to enshrine architecture constraints.
- **Files Created**:
  - `system-design.md`: Documented architecture, core modules, and constraints.
  - `flow.md`: Detailed the Execution Flow enforcing deterministic operations.
  - `db-schema.md`: Drafted PostgreSQL source-of-truth tables.
  - `api-spec.md`: Prepared endpoint spec placeholder.
  - `decisions.md`: Recorded initial architectural constraints.
  - `README.md`: Basic project landing page.
- **Status**: Documentation configured.

## Update: GitHub Repository Scope Correction
- **Action**: Resolved accidental home directory push by isolating the git repository to the `career-navigator` folder.
- **Action**: Force-pushed the correct documentation files to overwrite the remote state.
- **Status**: Repository scope correctly isolated. Sync complete.

## Update: Database Schema Finalization
- **Action**: Converted draft schema into production-ready `db-schema.md`.
- **Action**: Defined strict fields, types, constraints, indexing strategies, and normalization for `Skill`, `SkillAlias`, `Role`, `RoleSkills`, and `SkillContent`.
- **Action**: Updated `decisions.md` and `flow.md` to reflect the transition from JSONB aliases to a dedicated 3NF `SkillAlias` table.
- **Status**: Database documentation finalized.

## Update: Backend Project Structure Initialization
- **Action**: Re-organized root Spring Boot boilerplate into isolated `/backend` directory.
- **Action**: Wired PostgreSQL driver and configured `application.properties`.
- **Action**: Implemented structural JPA Entities (`Skill`, `SkillAlias`, `Role`, `RoleSkills`, `SkillContent`) mapping the robust schema and enforced table keys/constraints. Setup base Spring Data REST Repository interfaces.
- **Status**: Backend Data/JPA layer initialized.

## Update: Initial Data Seeding
- **Action**: Added `score` column to `Skill` table for fallback prioritization.
- **Action**: Created `DataSeeder.java` (CommandLineRunner) to bootstrap the `Backend Developer` role.
- **Action**: Seeded 10 core skills (Java, Spring Boot, etc.), 30+ normalized aliases, and matched fallback YouTube videos into `SkillContent`. Linked everything relationally via `RoleSkills`.
- **Status**: Database seeded properly.

## Update: NLP Integration Setup
- **Action**: Populated `application.properties` with remote NLP endpoint properties (`nlp.api.url`/`nlp.api.key`).
- **Action**: Built the Java backend wrapper (`NlpSkillExtractionService`) relying on `RestTemplate` to ping external NLP providers natively.
- **Action**: Enforced data mapping boundaries via `ExtractedSkillDto` and bounded HTTP error tracking using `NlpExtractionException`.
- **Status**: NLP Extraction scaffolding complete.

## Update: Skill Validation Layer Implementation
- **Action**: Built `SkillValidationService` to intercept raw NLP outputs.
- **Action**: Enforced strong consistency by querying the Database. Skills are resolved explicitly via `Skill.name` first, checking `SkillAlias.alias_name` dynamically upon failure.
- **Action**: Handled normalization (lowercase/alphanumeric formatting) and deduplication (preventing dual aliases from producing dual master skills).
- **Status**: Backend validation bridging complete. NLP terms can now safely integrate with the PostgreSQL dictionary.

## Update: Database Mapping Refactor & Skill Selection Bounds
- **Action**: Trashed legacy composite mappings natively decoupling `Skill` away from bidirectional reference loops internally. `Role` entity owns mapping to `RoleSkills`.
- **Action**: Wrote `SkillSelectionService` to branch application flows efficiently: JD parsing logic sorts NLP values explicitly, while predefined DB role pathways skip math logic to parse native `priority` identifiers.
- **Status**: Core Skill Selection Logic finalized.
