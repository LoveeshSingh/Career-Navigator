# API Specification

*(To be populated with exact REST endpoints and JSON payloads during the backend development phase)*

## Expected Endpoints
### 1. `POST /api/v1/analyze`
**Purpose**: Starts the core skill gap analysis flow.
**Request Body**:
- `role` (Optional String)
- `job_description` (Optional String)
- `resume` (String, base64 or raw text)
- `level` (String)
- `hours_per_week` (Integer)
- `top_k` (Integer)

**Response**:
- Final Learning Roadmap (from LLM) OR Fallback video list.
