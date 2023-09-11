# CourseManagementSystem
Promotion project

Work breakdown structure:

```mermaid
stateDiagram
    ProjectStart --> FunctionaltityImplementation
    FunctionaltityImplementation --> DatabaseSetup
    FunctionaltityImplementation --> Security
        Security --> Registration
        Security --> AuthenticationAndAuthorization
        Security --> JWT
    FunctionaltityImplementation --> RoleManagement
    FunctionaltityImplementation --> CourseManagement
    FunctionaltityImplementation --> APIDesign
    ProjectStart --> Deployment
        Deployment --> CI/CD
        Deployment --> Docker
        Deployment --> AWS
    ProjectStart --> Testing
        Testing --> Unit
        Testing --> APIUnit
        Testing --> e2e
        Testing --> IntegrationTests
        Testing --> Manual
    ProjectStart --> Unplanned

```
