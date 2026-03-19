# Career Navigator API Specifications

## Endpoints Overview

### POST `/api/v1/roadmap/generate`
Generates a structured learning roadmap intelligently parsing user constraints against a canonical database and leveraging AI for weekly distribution structuring.

#### Request Target:
- Evaluates Top-K required skills against native DB schemas (extracted intelligently via NLP for Job Descriptions or queried directly natively for Hard-coded Roles).
- Cross references Top-K skills sequentially against an explicit Regex Word Boundary Resume matcher.
- Pushes missing gaps into standard Generative AI (LLM) mappings.
- Re-routes system mapping to explicit Video Fallbacks automatically upon generative network failure.

#### Request Body
```json
{
  "roleId": "UUID" // Optional: Required if jdText is empty
  "jdText": "string" // Optional: Required if roleId is empty
  "resumeText": "string", // Required
  "level": "beginner | intermediate", // Optional: defaults to beginner
  "hoursPerWeek": 10, // Optional: defaults to 10
  "topK": 10 // Optional: defaults to 10, strictly capped at maximum 20
}
```

#### Responses 

**200 OK (AI Mode - Success)**
```json
{
  "mode": "ai",
  "data": {
    "week_1": ["java", "spring_boot"],
    "week_2": ["aws"]
  }
}
```

**200 OK (AI Mode - Perfect Resume Match)**
```json
{
  "mode": "ai",
  "message": "You are already aligned with required skills"
}
```

**200 OK (Fallback Mode - External LLM Crash / Timeout)**
```json
{
  "mode": "fallback",
  "message": "AI generation failed. Degrading to predefined standard video paths.",
  "data": [
    {
      "skill": "java",
      "video": "https://youtube.com/..."
    }
  ]
}
```

**400 Bad Request (Validation Failure)**
```json
{
  "mode": "error",
  "message": "Either roleId or jdText must be provided."
}
```
