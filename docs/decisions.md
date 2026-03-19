# Architectural Decisions Log

## 1. Resume Matching Modality
- **Decision**: Use 100% deterministic text matching (name and aliases) against a canonical DB of skills.
- **Current Status**: Adopting immediately.
- **Reason**: Using NLP or LLMs for resume matching introduces hallucinations, unpredictable dropped keywords, and non-determinism. Deterministic matching ensures absolute confidence that a skill identified as "missing" is genuinely not written in the resume context.

## 2. LLM Scope Constraints
- **Decision**: Limit LLM operation strictly to translating a formal list of `missing_skills` into a structured learning roadmap.
- **Current Status**: Adopting immediately.
- **Reason**: We want the AI to synthesize pedagogical content, NOT perform analytical extraction. This isolates mapping logic within the reliable Backend/DB layer.

## 3. Separation of Primary and Fallback Logic
- **Decision**: Fallback resource extraction (YouTube links from DB) is completely divorced from the main Roadmap calculation flow.
- **Current Status**: Adopting immediately.
- **Reason**: Internal scoring/resource fetching is a worst-case scenario. It should never pollute the Top K selection or Gap Analysis, preserving single-responsibility workflows.

## 4. Normalization and Relational Schema Design
- **Decision**: Break out `SkillAlias` into a dedicated table rather than storing aliases as a JSONB array.
- **Current Status**: Adopting immediately.
- **Reason**: We require maximum read performance for deterministic matching. Searching for an exact string within a massive list of normalized resume tokens against a B-Tree indexed `alias_name` column is highly optimized in PostgreSQL. JSONB array containment queries are less efficient for this specific high-volume intersection math.
- **Decision**: Use `RoleSkills` as a junction table.
- **Reason**: Standard 3NF mapping to support many-to-many relationships, ensuring "Python" can be reused across multiple roles while maintaining role-specific priority mapping.

## 5. Pre-Seeded Database Necessity
- **Decision**: Deploy initial application state via a dedicated `DataSeeder` (CommandLineRunner).
- **Current Status**: Adopting immediately.
- **Reason**: The system is completely deterministic and relies entirely on having a "source of truth" to validate dynamic job extraction and fallback logic. Bootstrapping raw data (e.g., `Backend Developer` role, associated dictionary of 10 skills with aliases, and YouTube fallback videos) is required so the application is functional directly out-of-the-box for core path execution and error fallbacks.

## 6. External NLP Usage for Extraction
- **Decision**: Delegate Job Description text-to-skill extraction entirely to an external NLP SDK/API.
- **Current Status**: Adopting immediately.
- **Reason**: Unstructured text parsing from JDs requires massive domain context. Specialized external NLP engines are better equipped to extract raw context safely, allowing the backend to remain laser-focused on deterministic mapping.
- **Constraint**: The NLP service (`NlpSkillExtractionService`) acts as a pure translator. It parses JSON HTTP responses into `ExtractedSkillDto`, applies basic string normalization (trim/lowercase), and throws `NlpExtractionException` on failure. It intentionally lacks database validation logic to enforce single-responsibility boundaries.

## 7. Database as Absolute Truth for Validation
- **Decision**: The PostgreSQL database strictly determines what skills move forward into the matching engine.
- **Current Status**: Adopting immediately.
- **Reason**: NLP APIs hallucinate strings or extract highly-niche, un-teachable jargon (e.g. "team player", "proactive"). By funneling the raw API outputs into a `SkillValidationService` that demands an exact match in the `Skill` or `SkillAlias` tables, we ensure that every skill presented to the user has a canonical learning path and fallback video associated with it. Any extraction failing this check is silently discarded.

## 8. RoleSkills Mapping Simplification
- **Decision**: Eradicate Hibernate Composite Keys in favor of standard UUID identifiers dynamically pointing arrays back to standard Join Columns. Unidirectional `@OneToMany(role_id)` mappings applied on the parent `Role` table.
- **Reason**: Decouples entities logically preventing bidirectional cyclical `StackOverflow` serialization crashes. Unidirectional designs with explicit `priority` integer tracking make queries lighter.

## 9. Contextual Top-K Pruning Mechanics (`SkillSelectionService`)
- **Decision**: Cap skills shown to users via the `topK` parameter uniformly on both branch streams.
- **Reason**: Overloading a user with 50 unorganized skills generates horrible roadmaps. Confining to `Math.min(top_k, max(20))` curates focus.
- **Secondary Decision (Role Flow Constraints)**: The explicit Predefined Role Flow strictly skips NLP dynamic ranking and relies on our seeded `RoleSkills.priority` integer ordering. This proves stability and human-approved hierarchy over algorithmic guesses for static paths.

## 10. Pure Regex Deterministic Resume Matching (Anti-NLP bounds)
- **Decision**: Avoid NLP frameworks completely during the Resume cross-referencing phase in favor of basic memory boundary RegExp iterations `(\b...\b)`.
- **Current Status**: Implemented strictly via `ResumeMatchingService`.
- **Reason**: NLP Models are notoriously "fuzzy." Utilizing an NLP layer on resumes introduces variable results depending on sentence structure (e.g. false positives like "I want to learn java"). By enforcing direct word boundary matching on known database aliases in O(1) loop evaluations, we retain perfect mathematical intersection bounds for sets. Every "missing" skill generated is objectively unlocated within the text string.

## 11. Bounding the LLM explicitly to Structured JSON Generation
- **Decision**: Restrict OpenAI endpoints solely to mapping validated skill gaps (`missingSkills`) into structural roadmaps cleanly. Use heavy prompts and exception fallbacks to forbid inference.
- **Reason**: The risk of a generic LLM adding non-canonical skills or hallucinating training requirements creates unbound roadmaps useless to our DB's `SkillContent` linking standard. Keeping temperatures at `0.1` and throwing `RoadmapGenerationException` when an unknown String slips through the JSON parsing enforces deterministic integrity alongside creative roadmapping.

## 12. Absolute Fallback Video Delegation
- **Decision**: Eradicate total pipeline application failures explicitly by forcing custom `RoadmapGenerationException` triggers to immediately route requests into a predefined static PostgreSQL mechanism utilizing `FallbackService`.
- **Reason**: We cannot guarantee 100% uptime on the generative LLM wrapper. Rather than rendering the entire Top-K matching execution path effectively useless upon an OpenAI 503 timeout, the backend must instantly pivot and serve highly correlated deterministic YouTube hyperlinks directly sourced from our database. Ensuring hierarchical fall-through constraints natively on the Java layer `Map->targetLevel || Map->BEGINNER` guarantees the user never gets an empty response.
