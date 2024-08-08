## Data Clumps

**Description:** There is a data clump in the assembleResults method where all fields of UserDto are being set from an array of Objects

**Suggested Refactoring:** Refactor the code to create a separate method for mapping the Object array to UserDto

## Shotgun Surgery

**Description:** The findAll method is responsible for multiple tasks such as building query, executing query, and assembling results

**Suggested Refactoring:** Refactor the code to separate concerns and create individual methods for each task

## Feature Envy

**Description:** The assembleResults method has a high dependency on the Object array structure returned by the query

**Suggested Refactoring:** Refactor the code to encapsulate the mapping logic within the UserDto class or a separate mapper class

