# 🛒 ShopSphere — E-Commerce Backend

ShopSphere is a RESTful e-commerce backend built with **Java and Spring Boot**.

The project focuses on building a production-style backend with secure authentication, user management, address management, product and category management, cart operations, order processing, validation, exception handling, and Redis caching.

The goal of this project is to understand how a real-world e-commerce backend is designed rather than simply building CRUD APIs.

---

## 🚀 Features

### 🔐 Authentication & Authorization

- User registration
- User login
- JWT-based authentication
- Role-based authorization
- Account activation/deactivation
- Secure password handling
- Protected endpoints using Spring Security

### 👤 User Management

- Create and manage user accounts
- Retrieve user profile
- Update user information
- Activate/deactivate users
- User-specific resource access

### 📍 Address Management

Users can manage multiple delivery addresses.

- Add address
- Get all user addresses
- Get address by ID
- Update address
- Delete address
- Set default address
- Address type support
- Maximum address limit per user
- Prevent deletion of default address

### 📦 Product Management

- Create products
- Update products
- Delete products
- Retrieve products
- Retrieve product by ID
- Product search
- Category-based filtering
- Pagination and sorting
- Product availability management

### 🗂️ Category Management

- Create categories
- Update categories
- Delete categories
- Retrieve categories
- Associate products with categories

### 🛒 Cart Management

- Add products to cart
- Update product quantity
- Remove products from cart
- View cart
- Calculate cart totals
- User-specific carts

### 📋 Order Management

- Create orders from cart
- View order history
- Retrieve order details
- Manage order status
- Validate product availability before placing orders
- Calculate order totals

### ⚡ Redis Caching

Redis is used to improve performance for frequently accessed data.

Caching is implemented for suitable read-heavy operations to reduce unnecessary database queries.

### ✅ Validation & Error Handling

- Request validation using Jakarta Bean Validation
- Custom validation messages
- Global exception handling
- Custom application exceptions
- Meaningful HTTP status codes
- Consistent error responses
