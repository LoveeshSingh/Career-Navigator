# System Design

## Architecture Overview
The system is an AI Skill Gap Analyzer & Roadmap Generator, architected to be a production-grade backend-driven application. It enforces deterministic resume matching and limits LLM usage purely to learning roadmap generation.

## Components
- **Backend (Spring Boot Java)**: Handles business logic, NLP orchestration, and matching engine. Includes a JPA Entity Layer modeling the entire PostgreSQL database natively with full constraints and cascading behaviors.
- **Frontend**: React + Tailwind - User interface for inputting profiles and displaying the final roadmap/video fallbacks.
- **Database**: PostgreSQL - The absolute source of truth for all predefined skills, roles, and fallback learning resources.

## Core Modules To Implement
1. **Skill Registry (PostgreSQL)**: Stores valid skills, aliases, roles, and fallback videos.
2. **NLP API Integration (JD only)**: Uses `NlpSkillExtractionService` to hit an external endpoint. It purely handles the HTTP connection, payload parsing, sorting `ExtractedSkillDto` objects by score, and bubbling up network exceptions. It performs zero business validation against the registry.
3. **Top K Selection Layer** (`SkillSelectionService`): Truncates the valid skill arrays into focused learning bounds.
   - For JD Flow: Dynamically sorts mapped API scores descending, plucking the top `K`.
   - For Role Flow: Defers entirely to the native predefined `RoleSkills.priority` sequence stored in the PostgreSQL registry up to `K`.
5. **Resume Matching Engine**: A 100% deterministic text matching component. It normalizes resume text and checks for the exact presence of `skill.name` or `skill.aliases` for the Top K skills.
6. **Missing Skill Detector**: Computes the set difference: `missing_skills = topK - present_skills`.
7. **LLM Roadmap Generator**: Sends strictly the `missing_skills` to the LLM (OpenAI API) to create a structured learning roadmap. Does not infer, add, or extract skills.
8. **Fallback Video System**: Engages only if the LLM roadmap generation fails, returning predefined YouTube videos from the DB based on the missing skills and user's level.

## Architectural Constraints
- **Parsing**: Strictly internal and rule-based. No external NLP APIs used for extraction.
- **Resume Processing**: ALWAYS deterministic string matching.
- **LLM**: Never used for skill extraction or gap analysis. Only generates learning material roadmaps.
- **Internal Scoring**: Dynamically computed during JD parsing to prioritize learning paths.
- **Skill Addition**: LLM must not invent new skills or infer unlisted skills from the missing skills.
