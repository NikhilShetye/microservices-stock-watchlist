# 📈 Stock Watchlist Backend API

## 🚀 Live Demo

👉 https://watchlist-service-5kha.onrender.com

---

## 🧠 Overview

A production-ready backend system built with Spring Boot that allows users to manage stock watchlists securely with authentication and role-based access control.

---

## 🛠️ Tech Stack

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* PostgreSQL (Neon DB)
* JPA (Hibernate)
* Docker

---

## 🔐 Features

* User Registration & Login
* JWT-based Authentication
* Role-Based Authorization (USER / ADMIN)
* Secure REST APIs
* Watchlist Management
* Drag & Drop Reordering Logic
* DTO-based Architecture
* Cloud Deployment

---

## 📦 API Endpoints

### Auth

* POST /users → Register
* POST /users/login → Login (returns JWT)

### Watchlist (Protected)

* GET /watchlist → Get user watchlist
* POST /watchlist → Add stock
* DELETE /watchlist/{id} → Remove stock
* POST /watchlist/reorder → Reorder stocks

### Admin

* GET /admin/test → Admin-only endpoint

---

## 🔑 Authentication

Pass JWT token in header:

Authorization: Bearer YOUR_TOKEN

---

## 🐳 Run with Docker

```bash
docker build -t watchlist-app .
docker run -p 8080:8080 watchlist-app
```

---

## ▶️ Run Locally

```bash
./mvnw spring-boot:run
```

---

## 💡 Future Improvements

* Redis caching
* API documentation (Swagger)
* Microservices architecture

---

## 👨‍💻 Author

Nikhil Shetye
