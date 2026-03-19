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
