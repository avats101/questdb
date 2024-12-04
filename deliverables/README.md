# Description of the submitted code
- Group members: Aryan Vats	(aryanvat@usc.edu), Anirudh Bhattacharya(ab62477@usc.edu), Han Kyul Kim	(hankyulk@usc.edu)

## Building for testing & deployment
### Prerequisites
- Java 11 64-bit
- Maven 3
### Steps:
1. Download the original master repository of QuestDB (https://github.com/questdb/questdb)
2. Copy and paste our submitted codes into the downloaded original master repository. Our submitted codes preserve the directory structure of the original repository.
3. Build our modified version of using Maven
```bash
mvn clean package -DskipTests -P build-web-console
```
4. Create a directory for the database and launch QuestDB
```bash
mkdir <root_directory>
java -jar core/target/questdb-<software_version>.jar -d <root_directory>
```

## Codes descriptions:
### Newly added
- ``core/src/main/java/io/questdb/CallTablesMemory.java``: Implements Copy-On-Write and Incremental Snapshot strategies from scratch, including the algorithms themselves, the interface with CairoEngine (query processing engine of QuestDB) to retrieve the information of the updated tuples during the query execution, data structure to hold the updated tuples in memory, snapshot creation from the DB initialization capability and serializing & saving functionalities for the snapshots.
- ``core/src/main/java/io/questdb/TableTokenTimeKey.java``: Creates a new hash key that combines TableToken (QuestDB's table identifier) with timestamp index to be used for storing updated tuples.
- ``scripts/*`: Contains scripts that can be used for testing our implementation
### Revised
- ``core/src/main/java/io/questdb/ServerMain.java``: Modified to ensure that our snapshot strategies are loaded during the DB initialization
- ``core/src/main/java/io/questdb/BinarySerializer.java`: Modified to handle serializing our generated snapshots
- ``core/src/main/java/io/questdb/griffin/UpdateOperatorImpl.java``: Modified to extract tuples being updated during a query execution

## Testing:
1. Start QuestDB
2. Run ``scripts/populating_table_executor.py`` to generate sample tables
3. Run ``scripts/update_query_executor.py`` to run our experiments and test our implementation
4. Snapshots will be located in ``data/``
