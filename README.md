# world-of-toys

This is a backend API for a toy store web application built using Spring Boot 3, Spring Security 6 and Java 17. This API is responsible for handling all requests made to the server and returning the appropriate data to the front-end.

## Table of Contents

- [Introduction](#introduction)
- [Technologies](#technologies)
- [Features](#features)
- [API Documentation](#api-documentation)
- [Installation and Usage](#installation-and-usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

This web application is designed to allow users to view and purchase toys from a toy store. Users can browse through the available toys, add them to their cart, and checkout to purchase them.

## Technologies

This application was built using the following technologies:

### Backend:
- Spring Boot 3
- Spring Security 6
- Java 17

### Database:
- Java ORM (JPA 3 + Hibernate 6)
- MySQL 8: An database used for development purposes
- H2: An embedded database used for testing purposes

### Mail Servers:
- Maildev: A local mail server for development and testing environments
- GreenMail: A mail server designed for integration testing

### Testing Frameworks:
- JUnit 5: A Java framework for writing unit tests
- Mockito: A Java framework for writing integration tests

### Documentation:
- Swagger: A tool for writing API documentation

## Features

The following features are currently available in the application:
- User registration 
- Account activation using email letter
- Resend activation account email letter
- Send change password email letter
- Change password 
- Login 
- Refresh access token
- Logout
- Fetch filtered products
- Fetch filtering product categories
- Fetch product by slug
- Add the product to the cart
- Retrieve the contents of the shopping cart for the user
- Update cart item quantity
- Delete product from the cart

## Endpoints
The following endpoints are currently available in the API:
- POST /api/v1/auth/register - User registration
- GET /api/v1/auth/confirm - User account activation using email
- POST /api/v1/auth/resend-verification-email - Resend the account activation email letter 
- POST /api/v1/auth/forgot-password - Send reset password email
- GET /api/v1/auth/reset-password - Change user password
- POST /api/v1/auth/login - Login
- POST /api/v1/auth/refresh-token - Refresh access token
- GET /api/v1/auth/logout - Logout
- GET /api/v1/products - Fetch filtered products
- GET /api/v1/products/categories - Fetch filtering product categories
- GET /api/v1/products/{productSlug} - Fetch product by slug
- POST /api/v1/cart/add-product - Add the product to the cart
- GET /api/v1/cart - Retrieve the contents of the shopping cart for the user
- PATCH /api/v1/cart - Update cart item quantity
- DELETE /api/v1/cart - Delete product from the cart

## API Documentation

To view the API documentation, you can use Swagger. Swagger provides a user-friendly interface for exploring and testing the API endpoints.

[Swagger API Documentation](http://localhost:8080/swagger-ui/index.html)

## Installation and Usage

To run this application, please follow the steps below:

1. Clone the repository to your local machine
2. Import the project into your IDE
3. Set up a MySQL database and update the application.yml file with your database details
4. Run the application using the command `mvn spring-boot:run` or by running the main method in the `WorldOfToysApplication` class
5.  Use a tool such as Postman or Swagger to make requests to the API endpoints.

## Contributing

If you would like to contribute to this project, feel free to fork the repository and submit a pull request.

## License

This project is not licensed and is not intended for use or distribution.