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
- Send the raw JD text to the external NLP API.
- The NLP API extracts and ranks skills by importance.
- Select the first `k` skills from the ranking.

### Branch B: Role Flow
- Fetch predefined skills mapped to the `role` from the Database (max 20).
- Order them by stored priority.
- Select the first `k` skills.

## 3. Skill Validation
- For each Top K skill, validate its presence in the DB.
- Use `skill.name` and the `SkillAlias` mapping table for dictionary lookups.
- Keep ONLY skills present in the DB.

## 4. Resume Matching (Deterministic)
- **CRITICAL RESTRICTION**: Do not use NLP.
- Normalize resume text (lowercase, tokenization, remove special characters).
- For each validated Top K skill:
  - Check if `skill.name` OR any alias from the `SkillAlias` table is present in the normalized resume text.
  - Mark the skill as `present` or `missing`.

## 5. Gap Analysis
- Calculate missing skills using exact set difference:
  `missing_skills = topK_skills - present_skills`

## 6. Roadmap Generation (LLM)
- Build a prompt containing strictly the `missing_skills`, user `level`, and `hours_per_week`.
- Send to LLM to generate a learning roadmap.
- **LLM Constraints**:
  - Must NOT add new skills.
  - Must NOT infer implied skills.
  - Must NOT perform parsing/extraction.

## 7. Fallback Logic (Video System - ONLY if LLM fails)
- If the LLM integration encounters an error or timeout:
  - For each missing skill, fetch a corresponding YouTube video from the Database based on user `level`.
  - Return the list of fallback video URLs to the user instead of the LLM roadmap.
