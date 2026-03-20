# System Design: Career Navigator

## Architecture Overview
The Career Navigator is an AI-powered Skill Gap Analyzer and Roadmap Generator designed to help developers transition between roles or match their skills against specific Job Descriptions. It uses a **Dual-Flow** approach to identify skill gaps and leverages the **Google Gemini 2.5 Flash** model to generate personalized learning paths.

## Tech Stack
- **Backend**: Spring Boot 3 (Java 17), Spring Data JPA, Hibernate.
- **Frontend**: React, Tailwind CSS, Lucide React (Icons), React Markdown.
- **AI**: Google Gemini 2.5 Flash API (REST via `RestTemplate`).
- **Database**: PostgreSQL (Entities: `Role`, `Skill`, `RoleSkills`, `SkillAlias`).

## Core Components

### 1. Dual-Flow Discovery Layer
The system allows two primary input methods to identify target skills:
- **Role-Based Flow**: Fetches a predefined list of skills and priorities for a specific career path (e.g., Backend Developer) from the PostgreSQL registry.
- **Job Description (JD) Flow**: A deterministic matching engine that identifies skills from a raw JD text by comparing it against the internal `Skill` and `SkillAlias` database.

### 2. Deterministic Matching Engine (`ResumeMatchingService`)
Unlike unpredictable NLP APIs, our matching engine is **100% deterministic**:
- **Text Normalization**: Both the resume and skill names/aliases are normalized (lowercased, punctuation removed).
- **Keyword Search**: Performs exact string matching for each target skill (including all its aliases) within the resume text.
- **Accuracy**: This ensures that if a user has "Spring Boot" on their resume, it correctly matches the "Spring" skill in our database via its alias.

### 3. Skill Profile Analytics
After matching, the system computes:
- **Match Percentage**: `(Present Skills / Target Skills) * 100`.
- **Strengths**: A list of skills found in both the target set and the resume.
- **Gaps (Missing Skills)**: Target skills not found in the resume, which form the basis for the roadmap.

### 4. Roadmap Generation (`RoadmapGenerationService`)
The system sends only the **Missing Skills** to the Google Gemini API:
- **Constraint**: Strict 5-week maximum duration.
- **Output**: Returns a structured JSON containing a detailed Markdown roadmap and a list of suggested certifications.
- **Post-Processing**: The backend un-escapes AI-generated literal `\n` characters to ensure perfect Markdown rendering in the frontend.

## Architectural Constraints
- **Zero Hallucination**: The system never "invents" skills. It only analyzes gaps against the verified internal database.
- **LLM Boundary**: The LLM is used exclusively for pedagogical planning (roadmaps), not for skill extraction or scoring.
- **Privacy**: `application.properties` is untracked to prevent sensitive API keys from being exposed.

## Future Enhancements
- **Fallback Video System**: Integration of YouTube API to provide curated video content for each missing skill.
- **User Authentication**: Allowing users to save and track their roadmap progress.
- **Multiple Resumes**: Support for comparing different resume versions against the same role.
