# Execution Flow

## 1. Input Processing
User provides:
- `role` OR `job description (JD)`
- `resume`
- `level` (Beginner, Intermediate, Advanced)
- `hours_per_week`
- `top_k` (Integer limiting the skill analysis scope)

## 2. Skill Extraction & Selection (Top K)
The system executes one of two primary flows based on input:

### Branch A: JD Flow (Primary)
- Send the raw JD text to the `JDParsingService`.
- **Normalization**: JD text is normalized (lowercase, remove special characters) and split into lines.
- **DB-Driven Matching**: The service iterates through all skills and aliases in the PostgreSQL registry.
- **Scoring Logic**:
  - Matches (name or alias) are identified within each line.
  - **Context Bonus**: +10 if the line contains "required", "must", or "mandatory". +5 if it contains "preferred" or "plus".
  - **Frequency Bonus**: +2 for every occurrence of the skill in the text.
- **Validation**: Only skills already present in the database are extracted.
- **Top K Selection Step**: Defer to `SkillSelectionService.selectSkillsForJd()`, which sorts skills by their computed scores descending and limits to the Top K.

### Branch B: Role Flow
- Query the database via `SkillSelectionService.selectSkillsForRole(roleId)`.
- Extract canonical skills sequentially based entirely on the `RoleSkills.priority` native entity column ordering.
- Deduplicate and hard-cap available baseline bounds to max `20`.
- **Top K Selection Step**: Truncate array against `Math.min(top_k, skills.size())`.

## 3. Resume Matching (Deterministic `ResumeMatchingService`)
- **CRITICAL RESTRICTION**: Machine Learning / NLP models are entirely prohibited here.
- Input payload receives raw `resumeText` and explicitly truncated `topKSkills` arrays.
- Normalize resume text (lowercase, compress spaces, strip special characters).
- Execute a solitary batch query against the `skill_alias` table.
- For each validated Top K skill:
  - Check if `skill.name` OR any mapped alias exists via strict word-boundary Regex string evaluation.
  - Categorize into `presentSkills` or `missingSkills`.

## 4. Gap Analysis
- Calculate missing skills using exact set difference:
  `missing_skills = topK_skills - present_skills`

## 5. Roadmap Generation (LLM `RoadmapGenerationService`)
- Execute `generateRoadmap()` wrapping `missingSkills`, `level`, and `hours_per_week` into a structured prompt schema.
- Request explicit JSON mappings representing a weekly roadmap allocation.
- **LLM Constraints**:
  - Must NOT add new skills beyond the `missingSkills`.
  - Must NOT omit provided skills.

## 6. Fallback Logic (`FallbackService`)
- Engages only if the LLM integration fails.
- For each missing skill, attempt a strict query for the precise user `level`.
- Hierarchical downgrade: If "INTERMEDIATE" lacks a URL, fallback to "BEGINNER".

## 7. Master Endpoint Orchestrator (`RoadmapController`)
- Orchestrates the sequential stacking of execution sequences.
- Returns generalized `RoadmapResponseDto` back to the user.
