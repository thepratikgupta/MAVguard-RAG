# 🤖 Autonomous AI Coding Agent — v4.0

This is **Version 4.0** of the project. It is a complete backend release that can be run and tested through **REST APIs using Postman**.

> **Note:** The project is still under active development, and I am currently integrating additional services. Therefore, the repository may not always contain the latest changes from the ongoing development. However, **v4.0 represents a complete and functional backend milestone.**

---

## 🚀 Some Features:

- Built a **Spring Boot-based autonomous AI coding agent** that interprets natural-language tasks and autonomously executes controlled backend tools.

- Engineered a **headless REST API** using **Java, Spring Boot, and MongoDB** to support autonomous agent execution.

- Integrated the **OpenAI SDK** to implement a dynamic **LLM tool-calling loop**, enabling the agent to select and orchestrate custom backend functions.

- Implemented tools for **web research, web scraping, filesystem manipulation, codebase analysis, project navigation, and controlled code modifications**, and many more.

- Implemented **AST-based code parsing, project indexing, class discovery, and method discovery** to enable context-aware interaction with codebases.

- Designed a **secure sandboxed execution environment** for filesystem and shell operations.

- Implemented **command-risk classification and validation** to restrict potentially dangerous shell commands.

- Built a custom **Role-Based Access Control (RBAC)** workflow where high-risk operations are placed into an approval queue for **manual administrator approval**.

- Implemented **hybrid authentication** using **JWT and OAuth2**, with **Google and GitHub login support**.

- Implemented **role-based authorization** using **Spring Security**.

- Used **Redis** for:
  - Distributed session management
  - IP-based rate limiting
  - User-based rate limiting
  - Daily LLM token-usage tracking

- Implemented **MongoDB-backed**:
  - Conversation history
  - Agent execution history
  - Tool execution records
  - Approval workflows

- Built a **controlled multi-step agent execution workflow** for planning and executing tasks using multiple backend tools.

- Implemented **code intelligence capabilities** to support class and method discovery across indexed codebases.

- Added **sandboxed file operations and rollback capabilities** for safer automated code modifications.

- Documented the REST API using **SpringDoc OpenAPI (Swagger)** for easier API testing and integration.

- The backend can currently be accessed and tested through **Postman**.

---

## 🛠️ Tech Stack:

| Category | Technologies |
|---|---|
| **Language** | Java |
| **Backend Framework** | Spring Boot |
| **Security** | Spring Security |
| **Database** | MongoDB |
| **Caching & Distributed State** | Redis |
| **Authentication** | JWT, OAuth2 |
| **AI Integration** | OpenAI SDK |
| **API** | REST APIs |
| **Code Intelligence** | AST-based Code Parsing |
| **API Documentation** | SpringDoc OpenAPI / Swagger |
| **Containerization** | Docker |
| **API Testing** | Postman |

---

## 🎯 Project Goal:

Built an **agentic system** that can:

- Understand a user's request
- Select appropriate tools
- Execute controlled actions
- Interact with a codebase
- Enforce security boundaries
- Maintain state
- Verify operations

Maintain State
      │
      ▼
Verify Operations
