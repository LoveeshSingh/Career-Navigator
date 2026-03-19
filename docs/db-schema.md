# Database Schema (Draft)

## Tables

### `skills`
The absolute source of truth for all skills the system can process.
- `id` (UUID, Primary Key)
- `name` (VARCHAR, Unique, Normalized format)
- `aliases` (JSONB) - List of string aliases (e.g., `["reactjs", "react.js", "react"]`)
- `created_at` (TIMESTAMP)

### `roles`
Predefined user roles for the Role Flow.
- `id` (UUID, Primary Key)
- `title` (VARCHAR, Unique)
- `created_at` (TIMESTAMP)

### `role_skills`
Maps predefined roles to skills with priority ordering.
- `role_id` (UUID, Foreign Key)
- `skill_id` (UUID, Foreign Key)
- `priority` (INT) - Determines the default ordering for Top K selection
- *Primary Key*: `(role_id, skill_id)`

### `learning_resources`
Fallback resources intended to be served if LLM roadmap generation fails.
- `id` (UUID, Primary Key)
- `skill_id` (UUID, Foreign Key)
- `level` (VARCHAR) - Target audience e.g., 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'
- `url` (VARCHAR) - Link to the resource (e.g., YouTube URL)
- `resource_type` (VARCHAR) - Default 'YOUTUBE_VIDEO'
- `created_at` (TIMESTAMP)
