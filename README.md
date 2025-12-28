# Barber Reservation Backend

A Spring Boot REST API backend for the Barber Reservation App, supporting mobile (React Native) and web admin (React) clients.

## Project Overview

This backend handles all business logic, data persistence, and API endpoints for managing barbershops, barbers, customers, appointments, services, and system analytics.

It is designed as a RESTful API and consumed by:

- **React Native mobile application**
- **React web admin panel**

## Technologies

- **Java 21** 
- **Spring Boot 3.5.3** 
- **Spring Data JPA** 
- **Spring Security** 
- **MySQL** - 
- **Maven** - 

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed on your system:

- **Java 21+** - [Download JDK](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Installation Guide](https://maven.apache.org/install.html)
- **MySQL 8.0+** - [Download MySQL](https://dev.mysql.com/downloads/)
- **Git** - [Download Git](https://git-scm.com/downloads)

### Setup Steps

#### 1. Clone the Repository

   ```bash
         git clone https://github.com/sijanthapaa34/Barber-Reservation-Backend.git
         cd Barber-Reservation-Backend
   ```


#### 2. Build the Project

```bash
  mvn clean install
```

#### 3. Run the Application

```bash
  mvn spring-boot:run
```

Or run the generated JAR file:

```bash
  java -jar target/Barber-Reservation-Backend-0.0.1-SNAPSHOT.jar
```


## Contributing

We welcome contributions from the team! Follow these steps to contribute:


1. **Create your feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Make your changes** and ensure code quality
    - Write meaningful commit messages
    - Add tests for new features
    - Update documentation as needed
3. **Commit your changes**
   ```bash
   git commit -m "Add: brief description of your changes"
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Create a Pull Request**
    - Provide a clear description of changes
    - Reference any related issues
    - Request review from team members

## Environment Compatibility

- **Works with both mobile and admin clients**
- **CORS configured for frontend integration**
- **Scalable for future microservices split**

## License
Private project
