# Micro Kernel Architecture

## Main idea
The idea is to have a framework that is a rest api where we can add jar files that includes rest operations which can activate when running the application.\
The application is based on spring, so we only accept spring beans.

The configuration can be with annotation or java config. In the past there was the possibility to configure the application in xml(this part is deprecated).

## Architecture

Kernel architecture is the most suitable architecture for this solution.

Each module should have its own prefix in the application.properties to avoid mixed properties.
File watcher will be used to scan the module directory for new libraries and move it to the classpath. 

## How to build
``mvn clean install``