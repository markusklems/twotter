### twotter
Twitter clone back-end implementation with Cassandra and Astyanax.

### prerequisites
download and install eclipse for java ee (e.g., https://eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/keplersr2)

### getting started

1.	Download or clone the git repository: ```git clone https://github.com/markusklems/twotter.git```
2.	Open the project in Eclipse
3.	Build the project with Maven:
    * (if not recognized by Eclipse as Maven project: right click on twotter project > Configure > Convert to Maven project)
    * Right click on the twotter project > Run As > Maven build
4.	Install and start a local Cassandra server
5. Run Client.java as java program
    * Right click on twotter project > Run As Java Application
   
Optional: 5.b	Run Client.java with VM arguments

    * Run As > Run Configurations ... >
    * Add this line to (x) = Arguments: -Dlog4j.configuration=de/twotter/log4j.properties

Start the Client.java application and create a userline table using the commandline prompt (execute create-tables).

Next, switch to the Cassandra CQL shell (cqlsh) and create a secondary index on the userline table for user_name:

```cqlsh> CREATE INDEX <index_name> ON userline(user_name);```
