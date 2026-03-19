# Database Schema

## Overview
This schema is designed for PostgreSQL to support the deterministic skill matching and learning roadmap fallback systems.

## Tables

### `Skill`
The core dictionary of master skills.
- `id` (UUID) - Primary Key
- `name` (VARCHAR(100)) - Unique, NOT NULL. The canonical, normalized name of the skill (e.g., "reactjs").
- `score` (INT) - NOT NULL. Used for fallback metrics.
- `created_at` (TIMESTAMP) - NOT NULL, default CURRENT_TIMESTAMP.
- *Constraints*: `UNIQUE(name)`

### `SkillAlias`
Handles variations, abbreviations, or common misspellings to ensure robust deterministic matching without NLP.
- `id` (UUID) - Primary Key
- `skill_id` (UUID) - Foreign Key referencing `Skill(id)`, NOT NULL, ON DELETE CASCADE.
- `alias_name` (VARCHAR(100)) - NOT NULL. (e.g., "react", "react.js").
- `created_at` (TIMESTAMP) - NOT NULL, default CURRENT_TIMESTAMP.
- *Constraints*: `UNIQUE(skill_id, alias_name)` to prevent duplicate aliases for the same skill.

### `Role`
Predefined roles used in the Role Flow branch.
- `id` (UUID) - Primary Key
- `title` (VARCHAR(100)) - Unique, NOT NULL.
- `created_at` (TIMESTAMP) - NOT NULL, default CURRENT_TIMESTAMP.
- *Constraints*: `UNIQUE(title)`

### `RoleSkills`
Mapping table that associates skills with specific roles and orders them by priority.
- `role_id` (UUID) - Foreign Key referencing `Role(id)`, NOT NULL, ON DELETE CASCADE.
- `skill_id` (UUID) - Foreign Key referencing `Skill(id)`, NOT NULL, ON DELETE CASCADE.
- `priority` (INT) - NOT NULL. Determines default ordering (e.g., Top K selection).
- `created_at` (TIMESTAMP) - NOT NULL, default CURRENT_TIMESTAMP.
- *Primary Key*: `(role_id, skill_id)`

### `SkillContent`
Fallback learning resources (previously `learning_resources`), linked to skills for specific user levels.
- `id` (UUID) - Primary Key
- `skill_id` (UUID) - Foreign Key referencing `Skill(id)`, NOT NULL, ON DELETE CASCADE.
- `level` (VARCHAR(20)) - NOT NULL. e.g., 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'.
- `resource_type` (VARCHAR(50)) - NOT NULL, default 'YOUTUBE_VIDEO'.
- `url` (TEXT) - NOT NULL.
- `created_at` (TIMESTAMP) - NOT NULL, default CURRENT_TIMESTAMP.
- *Constraints*: `UNIQUE(skill_id, level, url)` to prevent duplicate resource entries.

## Indexing Strategy
1. **`Skill(name)`**: B-Tree index. *Why*: Critical for validation lookups when exact-matching skills extracted from JDs.
2. **`SkillAlias(alias_name)`**: B-Tree index. *Why*: Required for high-performance deterministic resume matching when checking if tokenized resume text exists in known aliases.
3. **`Role(title)`**: B-Tree index. *Why*: Enables fast role lookups in the Role Flow.
4. **`RoleSkills(role_id, priority)`**: Composite index. *Why*: Optimizes fetching the Top K skills for a specific role, ordered by priority.
5. **`SkillContent(skill_id, level)`**: Composite index. *Why*: Allows rapid retrieval of fallback resources when the LLM roadmap generation fails.

## Edge-Case Handling & Normalization
- **Case Sensitivity**: All `name` in `Skill` and `alias_name` in `SkillAlias` must be inserted in fully normalized lowercase. Resume text will be lowercased before matching to ensure case-insensitive deterministic comparisons.
- **Alias Conflicts**: If two different skills share the same alias (e.g., "Java" the language vs "Java" the island), the system relies on the parent JD extraction to contextualize the master `Skill` and keeps the mapping strict. `UNIQUE(skill_id, alias_name)` prevents redundant mappings. 
- **Normalization (Many-to-One Aliases)**: Extracted aliases into a dedicated `SkillAlias` table instead of a JSONB column. *Why*: Relational rows allow for efficient, indexed exact-string searches across millions of resumes, which is significantly faster and more standard in SQL than querying inside JSONB arrays.
- **Normalization (Many-to-Many Roles)**: `RoleSkills` is a strict associative mapping table. *Why*: Roles and skills exist independently; a single skill like "Python" can belong to Data Scientist, Backend Engineer, and DevOps roles with different priorities. 
- **Duplicate Skills**: `UNIQUE(name)` ensures that duplicate canonical skills cannot be inserted, centralizing all variations into the `SkillAlias` table.

## Implementation Details (JPA/Hibernate)
- **Primary Keys**: Configured using `@GeneratedValue(strategy = GenerationType.UUID)` to enable robust distributed setups.
- **Relationships**:
  - `Skill` ↔ `SkillAlias`: `@OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)`
  - `Skill` ↔ `SkillContent`: `@OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)`
  - `Role` ↔ `RoleSkills`: `@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)`
- **Composite Keys**: `RoleSkills` utilizes an `@EmbeddedId` mapped to `RoleSkillsId` handling the many-to-many junction properties properly.
- **Naming Constraints**: Java fields follow `camelCase` which Hibernate transforms into standard PostgreSQL `snake_case`. All entities use exact `@Table(name="...")` and logical column sizing via `@Column(length=X, nullable=false, unique=true)`.

## Sample Data Structure
The application employs a `CommandLineRunner` for initial bootstrapping ensuring strict consistency:
- **Role**: `Backend Developer`
- **Core Skills**: `java`, `spring boot`, `postgresql`, `docker`, `kubernetes`, `rest api`, `microservices`, `git`, `redis`, `aws`.
- **Normalization**: Each skill seeds 2-4 lowercase string aliases directly appended into `SkillAlias`.
- **Fallback Videos**: Automatic generation of `BEGINNER` and `INTERMEDIATE` YouTube query links mapping precisely to each skill in `SkillContent`.
