# world-of-toys

This is a backend API for a toy store web application built using Spring Boot 3, Spring Security 6, and Java 17. This API is responsible for handling all requests made to the server and returning the appropriate data to the front-end.

## Table of Contents

- [Introduction](#introduction)
- [Technologies](#technologies)
- [Features](#features)
- [Controllers and Endpoints](#controllers-and-endpoints)
- [API Documentation](#api-documentation)
- [Installation and Usage](#installation-and-usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

This web application is designed to allow users to view and purchase toys from a toy store. Users can browse through shop goods, search them flexibly, add products to a cart, make purchases, and proceeding payment with their card credentials.

## Technologies

This application was built using the following technologies:

### Backend:
- Spring Boot 3
- Spring Security 6
- Java 17

### Database:
- Java ORM (JPA 3 + Hibernate 6)
- MySQL 8: A database used for development purposes
- H2: An embedded database used for testing purposes

### Mail Servers:
- Maildev 2: A local mail server for development and testing environments
- GreenMail 2: A mail server designed for integration testing

### Testing Frameworks:
- JUnit 5: A Java framework for writing unit tests
- Mockito 5: A Java framework for writing integration tests

### Documentation:
- Swagger 3: A tool for writing API documentation

## Features

### Features for Unauthenticated Users:
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

### Features for Authenticated Users:
- Add the product to the cart
- Retrieve the contents of the cart for the user
- Update cart item quantity
- Delete the product from the cart
- Create order
- Retrieve all user's orders

### Features for Admin Users:
- Fetch filtered products
- Fetch product by id
- Create product
- Update product
- Get all categories
- Create product category
- Update product category
- Delete product category
- Get order filtering options
- Fetch filtered orders
- Get order statuses
- Update order status

### Features for Payment Integration:
- Create a payment for the order
- Fulfill orders after successful payment

## Controllers and Endpoints

### AuthenticationController:
- POST /api/v1/auth/register - User registration
- GET /api/v1/auth/confirm - User account activation using email
- POST /api/v1/auth/resend-verification-email - Resend the account activation email letter 
- POST /api/v1/auth/forgot-password - Send reset password email
- GET /api/v1/auth/reset-password - Change user password
- POST /api/v1/auth/login - Login
- POST /api/v1/auth/refresh-token - Refresh access token
- GET /api/v1/auth/logout - Logout

### ShopController:
- GET /api/v1/products - Fetch filtered products
- GET /api/v1/products/categories - Fetch filtering product categories
- GET /api/v1/products/{productSlug} - Fetch product by slug

### CartController:
- POST /api/v1/cart/add-product - Add the product to the cart
- GET /api/v1/cart - Retrieve the contents of the shopping cart for the user
- PATCH /api/v1/cart - Update cart item quantity
- DELETE /api/v1/cart - Delete product from the cart

### OrderController:
- POST /api/v1/order - Create order
- GET /api/v1/order - Retrieve all user orders

### PaymentController:
- POST /api/v1/payment/{orderId} - Create a payment for the order
- POST /api/v1/payment/webhook - Fulfill orders after successful payment

### AdminPanelController:
- GET /api/v1/admin/products - Fetch filtered products
- GET /api/v1/admin/products/{productId} - Fetch product by id
- POST /api/v1/admin/products/add - Create product
- PUT /api/v1/admin/products/{productId} - Update product
- GET /api/v1/admin/categories/{categoryType} - Get all categories
- POST /api/v1/admin/categories/{categoryType}/add - Create product category
- PUT /api/v1/admin/categories/{categoryType}/{categoryId} - Update product category
- DELETE /api/v1/admin/categories/{categoryType}/{categoryId} - Delete product category
- GET /api/v1/admin/orders/filtering-options - Get order filtering options
- GET /api/v1/admin/orders - Fetch filtered orders
- GET /api/v1/admin/orders/statuses - Get order statuses
- PATCH /api/v1/admin/orders/{orderId} - Update order status

## API Documentation

To view the API documentation, you can use Swagger. Swagger provides a user-friendly interface for exploring and testing the API endpoints.

[Swagger API Documentation](http://localhost:8080/swagger-ui/index.html)

## Installation and Usage

To run this application, please follow the steps below:

1. Clone the repository to your local machine
2. Import the project into your IDE
3. Update the application.yml file with your data and settings.
4. Set up a MySQL database and update the application.yml file with your database details
5. Run the application using the command `mvn spring-boot:run` or by running the main method in the `WorldOfToysApplication` class
6. Use a tool such as Postman or Swagger to make requests to the API endpoints.
7. In order to locally test POST /api/v1/payment/webhook API endpoint you can use [Stripe CLI](https://stripe.com/docs/payments/checkout/fulfill-orders#install-stripe-cli).

> **_NOTE:_** The application is independent of a specific relational database, so if you want to use a different database, you only need to remove the MySQL dependency in the pom.xml file and add the dependency for your database.

## Contributing

If you would like to contribute to this project, feel free to fork the repository and submit a pull request.

## License

This project is not licensed and is not intended for use or distribution.