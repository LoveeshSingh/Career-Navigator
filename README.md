# Career Navigator AI 🚀

### An intelligent skill-gap analysis and career roadmap system.

[![▶️ Watch the Demo](https://img.shields.io/badge/▶️_Watch_Demo-YouTube-red?style=for-the-badge&logo=youtube)](https://www.youtube.com/watch?v=cBe1sxy_4Aw)

---

## 💡 What It Does

Most developers don't know *what exactly they're missing* for their target role. Career Navigator solves this by combining **deterministic backend engineering** with **controlled AI** — using each where they are strongest.

1. **Input** a Job Description or select a predefined Career Role.
2. **Analyze** your resume against a verified skill registry — zero hallucination, fully explainable.
3. **Generate** a personalized 5-week learning roadmap powered by Gemini 2.5 Flash.

---

## 🌟 Key Features

| Feature | Description |
|---|---|
| **Dual-Flow Discovery** | Switch between raw JD parsing and curated Role selection |
| **Weighted Scoring Engine** | Context bonuses, frequency analysis, and alias normalization |
| **Skill Profile Analytics** | Real-time match %, categorized strengths & gaps |
| **AI-Powered Roadmaps** | Structured 5-week plans via Google Gemini 2.5 Flash |
| **Deterministic Matching** | No NLP hallucinations — strict keyword & alias mapping |
| **Glassmorphism UI** | Premium dark-mode design with smooth micro-animations |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | React, Tailwind CSS (CDN), Lucide React |
| **Backend** | Spring Boot 3, Java 17, Spring Data JPA |
| **Database** | PostgreSQL (Relational role/skill mapping) |
| **AI Engine** | Google Gemini 2.5 Flash (`responseMimeType: JSON`) |

---

## 📦 Project Structure

```
career-navigator/
├── backend/          # Spring Boot API & business logic
├── frontend/         # React UI with glassmorphism design
├── docs/             # System design & architecture docs
└── data/             # Synthetic dataset for testing (5 examples)
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ / Node.js 18+ / PostgreSQL
- [Gemini API Key](https://aistudio.google.com/)

### Backend
```bash
# Configure DB credentials & API key in application.properties
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install && npm run dev
```

---

## 📐 Design Decisions & Tradeoffs

### 1. Deterministic vs. Probabilistic Matching
> Used an internal keyword-alias registry instead of NLP extraction.
> **Tradeoff**: More backend code, but 100% accuracy — users never get a roadmap for a skill they already have under an alias.

### 2. LLM Boundaries
> LLM is restricted only to pedagogical planning (the roadmap).
> **Tradeoff**: Lower latency and zero hallucination risk for core gap analysis.

### 3. PostgreSQL Over NoSQL
> Used PostgreSQL for role/skill relationships with priority-based Top K selection.
> **Tradeoff**: Rigid schema, but strong consistency for skill ranking.

---

## 📊 Synthetic Dataset

See [`data/synthetic-dataset.json`](data/synthetic-dataset.json) for 5 realistic test examples with full JDs, resumes, and expected metrics.

---

## 📝 License
MIT
