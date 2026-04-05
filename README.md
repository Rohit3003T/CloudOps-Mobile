# CloudOps

A full-stack cloud monitoring platform that provides **AWS infrastructure management and monitoring through a REST API and Android application**.
The system integrates **Node.js backend, AWS SDK, GitHub CI/CD monitoring, and a Kotlin Android app** to deliver a production-ready cloud monitoring solution.

---

## 🚀 Overview

Cloud Monitor helps users:

* Monitor AWS infrastructure (EC2, S3, Lambda, EBS, VPC)
* Track CloudWatch metrics
* View GitHub CI/CD workflow runs
* Monitor AWS cost usage
* Manage resources through a secure REST API
* Access everything via a modern Android app


---

## 🏗️ Architecture

```
CloudMonitor/
│
├── backend/        → Node.js + Express AWS API
├── android-app/    → Kotlin Jetpack Compose Android App
└── README.md
```

### System Flow

Android App → REST API → AWS SDK → AWS Services
Android App → REST API → GitHub API → CI/CD Data

---

## ⚙️ Tech Stack

### Backend

* Node.js
* Express.js
* AWS SDK v3
* GitHub REST API
* JWT Authentication
* Rate Limiting
* CloudWatch Integration

### Frontend

* Kotlin
* Jetpack Compose
* MVVM Architecture
* Retrofit
* Material 3
* DataStore
* Coroutines & Flow

### Cloud & DevOps

* AWS EC2, S3, Lambda, EBS, VPC
* CloudWatch
* Cost Explorer
* GitHub Actions
* REST API
* IAM

---

## 📦 Features

### AWS Monitoring

* EC2 instance management
* S3 bucket and file management
* Lambda monitoring
* EBS volume management
* VPC and security group listing
* CloudWatch metrics
* AWS cost tracking

### CI/CD Monitoring

* GitHub workflow runs
* Jobs and steps tracking
* Repository-based monitoring

### Security

* JWT authentication
* IAM-based AWS access
* Rate limiting
* Secure token storage
* Environment-based configuration

### Android App

* Dashboard with live AWS status
* EC2, S3, Lambda, EBS, VPC screens
* CloudWatch monitoring screen
* Cost dashboard
* CI/CD monitoring screen
* Secure login and token management

---

## 🚀 Running the Project

### 1️⃣ Start Backend

```bash
cd backend
npm install
cp .env.example .env
npm start
```

Server runs on:

```
http://localhost:3000
```

---

### 2️⃣ Run Android App

Open Android Studio

```
CloudMonitor/android-app
```

Set base URL:

```kotlin
BASE_URL = "http://10.0.2.2:3000/"
```

Run the app and login:

```
username: admin
password: password123
```

---

## 🔐 Environment Variables

```
PORT=3000
JWT_SECRET=your_secret
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret
GITHUB_TOKEN=your_token
```

---

## 📱 Screens

* Login
* Dashboard
* EC2
* S3
* Lambda
* EBS
* VPC
* Monitoring
* Cost
* CI/CD

---

## 🛡️ Security

* JWT authentication
* IAM least privilege
* Rate limiting
* AWS credentials via environment variables
* Secure Android token storage

---

## 🎯 Use Cases

* DevOps Engineers
* Cloud Engineers
* AWS Monitoring Projects
* Mobile Cloud Dashboard
* Infrastructure Monitoring System
* Portfolio Project

---
