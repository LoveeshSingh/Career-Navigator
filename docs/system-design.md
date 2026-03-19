# System Design

## Architecture Overview
The system is an AI Skill Gap Analyzer & Roadmap Generator, architected to be a production-grade backend-driven application. It enforces deterministic resume matching and limits LLM usage purely to learning roadmap generation.

## Components
- **Backend (Spring Boot Java)**: Handles business logic, NLP orchestration, and matching engine. Includes a JPA Entity Layer modeling the entire PostgreSQL database natively with full constraints and cascading behaviors.
- **Frontend**: React + Tailwind - User interface for inputting profiles and displaying the final roadmap/video fallbacks.
- **Database**: PostgreSQL - The absolute source of truth for all predefined skills, roles, and fallback learning resources.

## Core Modules To Implement
1. **Skill Registry (PostgreSQL)**: Stores valid skills, aliases, roles, and fallback videos.
2. **NLP API Integration (JD only)**: Uses an external NLP API exclusively to extract and rank skills from Job Descriptions.
3. **Top K Selector**: Selects the top `k` most important skills (either from NLP API ranking for JD or DB priority for roles).
4. **Skill Validation Layer**: Validates skills extracted by the NLP API against the Skill Registry. Discards any skill not found in DB.
5. **Resume Matching Engine**: A 100% deterministic text matching component. It normalizes resume text and checks for the exact presence of `skill.name` or `skill.aliases` for the Top K skills.
6. **Missing Skill Detector**: Computes the set difference: `missing_skills = topK - present_skills`.
7. **LLM Roadmap Generator**: Sends strictly the `missing_skills` to the LLM (OpenAI API) to create a structured learning roadmap. Does not infer, add, or extract skills.
8. **Fallback Video System**: Engages only if the LLM roadmap generation fails, returning predefined YouTube videos from the DB based on the missing skills and user's level.

## Architectural Constraints
- **NLP**: Restricted strictly to Job Description skill extraction.
- **Resume Processing**: ALWAYS deterministic string matching. Never NLP.
- **LLM**: Never used for skill extraction, ranking, or gap analysis. Only generates learning material roadmaps.
- **Internal Scoring**: Exists only as a fallback resource index (e.g., retrieving level-appropriate videos), not used in the main logic flow.
- **Skill Addition**: LLM must not invent new skills or infer unlisted skills from the missing skills.
