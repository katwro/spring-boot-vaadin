# Spring Vaadin MVC: UserGear Application

This is an example web application for managing user gear and creating user requests using Spring Boot, Vaadin, Hibernate/JPA, and Microsoft SQL.

## Prerequisites

Before you proceed, ensure you have the following prerequisites:

- Java JDK (version 22.0)
- MSSQL Express

## Installation

To install the application, follow these steps:

1. Clone the repository.
2. Run the `UGA.sql` script to create the database.
3. Configure the database connection in the `application.properties` file with appropriate credentials.
4. Grant user privileges to the UGA database.

## Running the application

To start the application, run the following command:

    mvnw spring-boot:run

## Usage

The application will be available at: http://localhost:8080/

To log in, use the username and password from the `UGA.sql` script.