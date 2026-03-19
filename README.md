# AI Skill Gap Analyzer & Roadmap Generator

A production-grade system that analyzes a user's resume against a job description or predefined role to generate a targeted learning roadmap.

## Project Structure
- `/docs`: System documentation (system design, database schema, flow logic, etc.).

## Key Principles
1. **Deterministic Matching**: Gap analysis is performed using strict DBalias matching. No NLP guessing on the resume.
2. **Restricted LLM**: The LLM is used *only* for generating study plans based on pre-calculated missing skills.
3. **Database as Source of Truth**: The PostgreSQL DB holds the definitive list of skills and fallback resources.
