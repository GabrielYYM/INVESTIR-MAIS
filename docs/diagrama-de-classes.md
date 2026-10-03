# Diagrama de classes

Diagrama das entidades e serviços centrais identificados em `src/main/java`, com atributos e operações.

```mermaid
classDiagram
    direction LR

    class User {
        +UUID id
        +String name
        +int age
        +UserSecurity userSecurity
        +LocalDateTime createdAt
        +LocalDateTime lastModifiedAt
        +getId() UUID
        +setId(UUID) void
        +getName() String
        +setName(String) void
        +getAge() int
        +setAge(int) void
        +getUserSecurity() UserSecurity
        +setUserSecurity(UserSecurity) void
    }

    class UserSecurity {
        <<Embeddable>>
        +String email
        +String password
        +UserRole role
        +boolean emailVerified
        +getEmail() String
        +setEmail(String) void
        +getPassword() String
        +setPassword(String) void
        +getRole() UserRole
        +setRole(UserRole) void
        +isEmailVerified() boolean
        +setEmailVerified(boolean) void
    }

    class UserRole {
        <<enumeration>>
        ADMIN
        TEACHER
        STUDENT
    }

    class Course {
        +UUID id
        +String name
        +String description
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
        +getId() UUID
        +setId(UUID) void
        +getName() String
        +setName(String) void
        +getDescription() String
        +setDescription(String) void
        +getProfessor() User
        +setProfessor(User) void
    }

    class Lesson {
        +UUID id
        +String title
        +String description
        +String mediaUrl
        +String thumbnailUrl
        +getId() UUID
        +setId(UUID) void
        +getTitle() String
        +setTitle(String) void
        +getDescription() String
        +setDescription(String) void
        +getCourse() Course
        +setCourse(Course) void
    }

    class Portfolio {
        +UUID id
        +User userId
        +List~Asset~ assets
        +getId() UUID
        +setId(UUID) void
        +getUserId() User
        +setUserId(User) void
        +getAssets() List~Asset~
        +setAssets(List~Asset~) void
    }

    class AssetRole {
        <<enumeration>>
        AÇÕES
        RENDA_FIXA
        CRIPTOMOEDAS
        FUNDOS_IMOBILIARIOS
        INTERNACIONAL
    }

    class Asset {
        +UUID id
        +String ticker
        +BigDecimal currentPositionValue
        +BigDecimal quantity
        +BigDecimal averagePrice
        +int rawScore
        +boolean isPositive
        +AssetRole role
        +Portfolio portfolio
        +getId() UUID
        +setId(UUID) void
        +getTicker() String
        +setTicker(String) void
        +getQuantity() BigDecimal
        +setQuantity(BigDecimal) void
        +getRole() AssetRole
        +setRole(AssetRole) void
        +getPortfolio() Portfolio
        +setPortfolio(Portfolio) void
    }

    class Question {
        +UUID id
        +String text
        +AssetRole role
        +getId() UUID
        +setId(UUID) void
        +getText() String
        +setText(String) void
        +getRole() AssetRole
        +setRole(AssetRole) void
    }

    User *-- UserSecurity : dados incorporados
    User "1" <-- "0..*" Course : professor
    Course "1" <-- "0..*" Lesson : curso
    Portfolio "1" <-- "0..*" Asset : portfolio
    UserRole .. UserSecurity : role
    AssetRole .. Asset : role
    AssetRole .. Question : role
```

## Serviços e operações

```mermaid
classDiagram
    direction LR

    class UserService {
        -UserRepository userRepository
        -PasswordEncoder passwordEncoder
        -UserMapper userMapper
        -OneTimeTokenService oneTimeTokenService
        -EmailOttHandler emailOttHandler
        +findByEmail(String) User
        +registerUser(UserRequestDTO) UserResponseDTO
        +updatePassword(String, UpdatePasswordDTO) void
        +forgotPassword(ForgotPasswordRequestDTO) void
        +resetPassword(ResetPasswordRequestDTO) void
    }
    class CourseService {
        -CourseRepository courseRepository
        -CourseMapper courseMapper
        -UserService userService
        +createCourse(CourseRequestDTO, String) CourseResponseDTO
        +getCourseById(UUID) CourseResponseDTO
        +getAllCourses(Pageable) Page~CourseResponseDTO~
        +getCoursesByProfessor(UUID, Pageable) Page~CourseResponseDTO~
        +updateCourse(UUID, CourseRequestDTO, String) CourseResponseDTO
        +deleteCourse(UUID, String) void
        -findCourseOwnedBy(UUID, String) Course
    }
    class LessonService {
        -LessonRepository lessonRepository
        -CourseRepository courseRepository
        -LessonMapper lessonMapper
        -UserService userService
        +createLesson(UUID, LessonRequestDTO, String) LessonResponseDTO
        +getLessonById(UUID) LessonResponseDTO
        +getLessonsByCourseId(UUID, Pageable) Page~LessonResponseDTO~
        +updateLesson(UUID, LessonRequestDTO, String) LessonResponseDTO
        +deleteLesson(UUID, String) void
        +findLessonOwnedBy(UUID, String) Lesson
    }
    class PortfolioService {
        -PortfolioRepository portfolioRepository
        -UserRepository userRepository
        -PortfolioMapper portfolioMapper
        +findPortfolioById(UUID) PortfolioResponseDTO
        +findByUserId(UUID) PortfolioResponseDTO
        +createPortfolio(UUID) PortfolioResponseDTO
        +findById(UUID) Portfolio
        +createPortfolioForUser(UUID) PortfolioResponseDTO
    }
    class AssetService {
        -RestClient brapiClient
        -AssetRepository assetRepository
        -AssetMapper assetMapper
        +listAllAssets() List~AssetResponseDTO~
        +findAssetById(UUID) AssetResponseDTO
        +createAsset(AssetRequestDTO) AssetResponseDTO
        +updateAsset(UUID, AssetRequestDTO) AssetResponseDTO
        +deleteAsset(UUID) void
        +getAssetQuote(String) String
        +getQuotesForAllAssets() String
        +listAssetsByRole(AssetRole) List~AssetResponseDTO~
        +findById(UUID) Asset
    }
    class QuestionService {
        -QuestionRepository questionRepository
        -QuestionMapper questionMapper
        +getQuestionsByRole(AssetRole, Pageable) Page~QuestionResponseDTO~
        +getQuestionById(UUID) QuestionResponseDTO
        +createQuestion(UUID, QuestionRequestDTO) QuestionResponseDTO
        +updateQuestion(UUID, QuestionRequestDTO) QuestionResponseDTO
        +deleteQuestion(UUID) void
    }
    UserService ..> UserRepository
    CourseService ..> CourseRepository
    CourseService ..> UserService
    LessonService ..> LessonRepository
    LessonService ..> CourseRepository
    LessonService ..> UserService
    PortfolioService ..> PortfolioRepository
    PortfolioService ..> UserRepository
    AssetService ..> AssetRepository
    QuestionService ..> QuestionRepository
```

## Observações

- `UserSecurity` é `@Embeddable` e faz parte de `User`, não sendo uma entidade independente.
- As relações `Course`–`User` (professor), `Lesson`–`Course`, `Portfolio`–`User` e `Portfolio`–`Asset` têm anotações JPA no código. `AssetRole` classifica ativos e perguntas como enum.
- O enum `UserRole` está representado com os valores definidos atualmente: `ADMIN`, `TEACHER` e `STUDENT`.
- Os getters e setters mostrados nos modelos são gerados por Lombok (`@Data`). Lombok também gera `equals`, `hashCode`, `toString` e, conforme as anotações, construtores sem argumentos e completos.
- O segundo diagrama lista os atributos de dependência e métodos públicos/de apoio declarados nos serviços. Os métodos HTTP dos controllers e os métodos derivados dos repositórios não foram repetidos aqui.
