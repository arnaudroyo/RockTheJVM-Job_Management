# Rock the JVM Jobs Board

## Overview

This project is a state-of-the-art Scala-based web application, developed as part of the Rock the JVM curriculum. It showcases a full-stack online job platform that features credit card integration, highlighting the application of functional programming principles in real-world web development. The Rock the JVM Jobs Board allows users to manage accounts, post and view jobs, and apply to them, providing a comprehensive platform for job seekers and employers.

## Functionality

The application delivers a rich feature set, including:

- **User Accounts**: Personalized account creation and management.
- **Job Management**: Capabilities for posting, viewing, and applying for jobs.
- **Credit Card Integration**: Direct payment functionalities for job postings.
- **Authentication & Permissions**: Secure login and role-based access control.
- **Email Integration & Image Uploads**: Enhanced communication and personalization options.
- **Test Coverage**: High level of backend test coverage employing test-driven development (TDD).

## Technologies Used

- **Cats**: A functional programming library used to make the code modular and capability-expressed.
- **Cats Effect**: Enables writing of composable, high-performance applications through pure functional programming. Every action in the application is built with Cats Effect.
- **Doobie**: A Typelevel library for type-safe database operations, using PostgreSQL for data storage.
- **Http4s**: Utilized for creating REST APIs with features like JSON payloads, automatic validation, and incremental content loading with pagination. It also handles authentication, authorization with JWTs, and role-based access control.
- **Tyrian**: A lightweight Scala 3 library for building single-page applications in a functional, Elm-style manner. It leverages Cats Effect and FS2 for state management, and supports features like routing, authentication flows, checkout processes, and UI state management.
- **PureConfig**: For application configuration management.
- **FS2**: Used for both incremental loading on the backend and SPA history management on the frontend.
- **TSec**: For JWT authorization and role-based access control.
- **Circe**: For JSON serialization and deserialization.
- **Log4Cats**: For purely-functional logging.
- **ScalaTest with TestContainers**: Wrapped in Cats Effect for robust testing.


The Rock the JVM Jobs Board exemplifies the use of Scala and functional programming to build modern, scalable web applications, offering a rich platform for job management while employing cutting-edge technologies and practices.
