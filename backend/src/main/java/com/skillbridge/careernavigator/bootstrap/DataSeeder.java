package com.skillbridge.careernavigator.bootstrap;

import com.skillbridge.careernavigator.entity.*;
import com.skillbridge.careernavigator.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    
    // Memory cache to ensure one skill record per canonical name across all roles
    private final Map<String, Skill> skillRegistry = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            log.info("Database already contains seed data. Skipping DataSeeder initialization.");
            return;
        }

        log.info("Starting Professional Data Seeding (5 Roles)...");

        // 1. Initialize Roles
        Role backend = createRole("Backend Developer");
        Role frontend = createRole("Frontend Developer");
        Role dataEngineer = createRole("Data Engineer");
        Role dataAnalyst = createRole("Data Analyst");
        Role aiEngineer = createRole("AI Engineer");

        // 2. Seed Backend Skills (Role 1)
        seedBackendSkills(backend);
        roleRepository.save(backend);

        // 3. Seed Frontend Skills (Role 2)
        seedFrontendSkills(frontend);
        roleRepository.save(frontend);

        // 4. Seed Data Engineer Skills (Role 3)
        seedDataEngineerSkills(dataEngineer);
        roleRepository.save(dataEngineer);

        // 5. Seed Data Analyst Skills (Role 4)
        seedDataAnalystSkills(dataAnalyst);
        roleRepository.save(dataAnalyst);

        // 6. Seed AI Engineer Skills (Role 5)
        seedAiEngineerSkills(aiEngineer);
        roleRepository.save(aiEngineer);

        log.info("Successfully seeded 5 Roles and {} unique Skills.", skillRegistry.size());
    }

    private Role createRole(String title) {
        Role role = Role.builder().title(title).build();
        return roleRepository.save(role);
    }

    private void seedBackendSkills(Role role) {
        int p = 1;
        add(role, p++, "Java", 95, "java 8", "java 11", "java 17", "core java", "jdk");
        add(role, p++, "Spring Boot", 95, "spring", "springboot", "spring framework", "spring mvc");
        add(role, p++, "REST API", 90, "rest", "restful api", "rest apis", "http api");
        add(role, p++, "Microservices", 90, "microservice", "microservices architecture", "msa");
        add(role, p++, "PostgreSQL", 85, "postgres", "postgresql db", "pgsql");
        add(role, p++, "MongoDB", 80, "mongo", "mongodb atlas", "nosql mongo");
        add(role, p++, "Redis", 75, "redis cache", "in-memory db", "redis server");
        add(role, p++, "Docker", 90, "docker container", "dockerfile", "containerization");
        add(role, p++, "Kubernetes", 85, "k8s", "kubernetes cluster", "kube");
        add(role, p++, "AWS", 85, "amazon web services", "aws cloud", "ec2", "s3");
        add(role, p++, "Hibernate", 80, "jpa", "hibernate orm", "spring data jpa");
        add(role, p++, "JUnit", 70, "junit5", "junit testing", "unit testing java");
        add(role, p++, "Mockito", 65, "mockito framework", "mocking java");
        add(role, p++, "Maven", 70, "maven build", "apache maven");
        add(role, p++, "Kafka", 85, "apache kafka", "kafka streaming", "kafka queue");
        add(role, p++, "RabbitMQ", 80, "rabbit mq", "message queue", "amqp");
        add(role, p++, "System Design", 95, "system architecture", "scalable systems", "hld", "lld");
        add(role, p++, "Git", 85, "git version control", "github", "gitlab");
        add(role, p++, "Linux", 75, "unix", "linux commands", "shell");
        add(role, p++, "CI/CD", 80, "cicd", "continuous integration", "continuous deployment");
    }

    private void seedFrontendSkills(Role role) {
        int p = 1;
        add(role, p++, "HTML", 90, "html5", "markup", "semantic html");
        add(role, p++, "CSS", 90, "css3", "styling", "flexbox", "grid");
        add(role, p++, "JavaScript", 95, "js", "es6", "ecmascript");
        add(role, p++, "TypeScript", 90, "ts", "typed javascript");
        add(role, p++, "React", 95, "reactjs", "react.js");
        add(role, p++, "Angular", 85, "angular2+", "angular framework");
        add(role, p++, "Vue", 80, "vuejs", "vue.js");
        add(role, p++, "Tailwind CSS", 85, "tailwind", "utility css");
        add(role, p++, "Redux", 85, "redux toolkit", "state management");
        add(role, p++, "Next.js", 90, "nextjs", "react framework");
        add(role, p++, "Webpack", 75, "bundler", "webpack config");
        add(role, p++, "Vite", 75, "vitejs", "vite build");
        add(role, p++, "REST API", 85, "api integration", "rest calls"); // Shared
        add(role, p++, "GraphQL", 80, "graphql api", "apollo");
        add(role, p++, "Git", 85, "github", "git version control"); // Shared
        add(role, p++, "Figma", 75, "figma design", "ui design tool");
        add(role, p++, "Responsive Design", 90, "mobile first", "responsive ui");
        add(role, p++, "Browser DevTools", 70, "chrome devtools", "debugging");
        add(role, p++, "Jest", 75, "jest testing", "unit testing js");
        add(role, p++, "Accessibility", 70, "a11y", "web accessibility");
    }

    private void seedDataEngineerSkills(Role role) {
        int p = 1;
        add(role, p++, "Python", 95, "python3", "scripting python");
        add(role, p++, "SQL", 95, "structured query language", "sql queries");
        add(role, p++, "Apache Spark", 95, "spark", "pyspark");
        add(role, p++, "Hadoop", 85, "hdfs", "hadoop ecosystem");
        add(role, p++, "Kafka", 90, "apache kafka", "streaming"); // Shared
        add(role, p++, "Airflow", 85, "apache airflow", "workflow scheduler");
        add(role, p++, "AWS", 90, "aws cloud", "s3", "glue"); // Shared
        add(role, p++, "Azure", 85, "microsoft azure");
        add(role, p++, "GCP", 85, "google cloud platform");
        add(role, p++, "ETL", 95, "extract transform load", "data pipeline");
        add(role, p++, "Data Warehousing", 90, "data warehouse", "dwh");
        add(role, p++, "Snowflake", 85, "snowflake db");
        add(role, p++, "Redshift", 80, "aws redshift");
        add(role, p++, "BigQuery", 80, "google bigquery");
        add(role, p++, "Pandas", 85, "pandas library");
        add(role, p++, "PySpark", 90, "spark python");
        add(role, p++, "Linux", 75, "unix", "shell"); // Shared
        add(role, p++, "Docker", 80, "containerization"); // Shared
        add(role, p++, "Data Modeling", 90, "schema design", "data modeling");
        add(role, p++, "NoSQL", 80, "nosql db", "document db");
    }

    private void seedDataAnalystSkills(Role role) {
        int p = 1;
        add(role, p++, "SQL", 95, "structured query language", "sql queries", "joins", "group by"); // Shared
        add(role, p++, "Excel", 95, "microsoft excel", "spreadsheets", "pivot tables", "vlookup", "xlookup");
        add(role, p++, "Python", 85, "python3", "scripting python"); // Shared
        add(role, p++, "Pandas", 90, "pandas library", "dataframe pandas"); // Shared
        add(role, p++, "NumPy", 80, "numpy library", "numerical python");
        add(role, p++, "Power BI", 90, "powerbi", "microsoft power bi");
        add(role, p++, "Tableau", 90, "tableau dashboard", "tableau software");
        add(role, p++, "Data Visualization", 95, "data viz", "charts", "graphs", "visualization");
        add(role, p++, "Statistics", 95, "statistical analysis", "probability", "stats");
        add(role, p++, "Data Cleaning", 90, "data preprocessing", "data wrangling", "cleaning data");
        add(role, p++, "Data Analysis", 95, "analyzing data", "data analytics");
        add(role, p++, "ETL", 85, "extract transform load", "data pipeline"); // Shared
        add(role, p++, "Business Intelligence", 90, "bi", "business analytics");
        add(role, p++, "Dashboarding", 90, "dashboards", "reporting dashboards");
        add(role, p++, "A/B Testing", 85, "ab testing", "split testing");
        add(role, p++, "Hypothesis Testing", 90, "statistical testing", "significance testing");
        add(role, p++, "Data Modeling", 85, "data model", "schema design"); // Shared
        add(role, p++, "Reporting", 90, "reports", "business reporting");
        add(role, p++, "Google Sheets", 80, "sheets", "google spreadsheet");
        add(role, p++, "Communication", 85, "stakeholder communication", "data storytelling");
    }

    private void seedAiEngineerSkills(Role role) {
        int p = 1;
        add(role, p++, "Python", 100, "python3", "scripting python"); // Shared
        add(role, p++, "Machine Learning", 95, "ml", "supervised learning", "unsupervised learning");
        add(role, p++, "Deep Learning", 90, "dl", "neural networks", "cnn", "rnn");
        add(role, p++, "PyTorch", 90, "pytorch framework", "torch");
        add(role, p++, "TensorFlow", 85, "tf", "keras");
        add(role, p++, "NLP", 85, "natural language processing", "llm", "transformers");
        add(role, p++, "Computer Vision", 80, "cv", "image processing", "opencv");
        add(role, p++, "Generative AI", 90, "genai", "large language models", "diffusion models");
        add(role, p++, "Prompt Engineering", 80, "prompt design", "ai prompting");
        add(role, p++, "Vector Databases", 80, "vector db", "pinecone", "milvus", "weaviate");
        add(role, p++, "LangChain", 85, "langchain framework", "llm orchestration");
        add(role, p++, "MLOps", 85, "ml pipelines", "kubeflow", "mlflow");
        add(role, p++, "Data Science", 85, "ds", "data analytics");
        add(role, p++, "Apache Spark", 80, "spark", "pyspark"); // Shared
        add(role, p++, "SQL", 80, "sql queries", "rdbms"); // Shared
        add(role, p++, "Mathematics", 80, "linear algebra", "calculus", "optimization");
        add(role, p++, "Statistics", 85, "probability", "stats"); // Shared
        add(role, p++, "API Integration", 75, "rest api", "fastapi", "flask");
        add(role, p++, "Cloud AI", 80, "aws sagemaker", "gcp vertex ai", "azure ml");
        add(role, p++, "Git", 80, "github", "version control"); // Shared
    }

    /**
     * Skill deduplication and linking helper.
     * Ensures each skill exists once in the DB but is linked to multiple roles.
     */
    private void add(Role role, int priority, String name, int score, String... aliases) {
        String key = name.toLowerCase().trim();
        Skill skill = skillRegistry.get(key);

        if (skill == null) {
            skill = Skill.builder()
                    .name(name)
                    .score(score)
                    .build();
            
            for (String aliasName : aliases) {
                SkillAlias alias = SkillAlias.builder()
                        .aliasName(aliasName.trim().toLowerCase())
                        .skill(skill)
                        .build();
                skill.getAliases().add(alias);
            }
            
            skill = skillRepository.save(skill);
            skillRegistry.put(key, skill);
        }

        // Link to Role via RoleSkills mapping (cascaded via Role)
        RoleSkills roleSkill = RoleSkills.builder()
                .skill(skill)
                .priority(priority)
                .build();
        
        role.getRoleSkills().add(roleSkill);
    }
}
