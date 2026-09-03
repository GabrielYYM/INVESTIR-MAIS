# INVESTIR MAIS

[![Java Version](https://img.shields.io/badge/Java-25_LTS-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Node.js](https://img.shields.io/badge/Node.js-22-green?style=flat-square&logo=node.js)](https://nodejs.org/)
[![React](https://img.shields.io/badge/React-19-blue?style=flat-square&logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-purple?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?style=flat-square&logo=tailwindcss)](https://tailwindcss.com/)

[![java-jwt](https://img.shields.io/badge/Java--JWT-red?style=flat-square)](https://github.com/auth0/java-jwt)
[![MapStruct](https://img.shields.io/badge/MapStruct-yellow?style=flat-square)](https://mapstruct.org/)
[![Bucket4j](https://img.shields.io/badge/Bucket4j-lightblue?style=flat-square)](https://bucket4j.com/)
[![SpringDoc OpenAPI](https://img.shields.io/badge/SpringDoc_OpenAPI-darkgreen?style=flat-square)](https://springdoc.org/)
[![Lombok](https://img.shields.io/badge/Lombok-pink?style=flat-square&logo=lombok)](https://projectlombok.org/)

## Escopo

O **INVESTIR MAIS** é um sistema educacional que conecta docentes, com conhecimentos na área financeira, com crianças e adolescentes, a fim de fomentar a educação financeira infantil, além de oferecer um sistema de gestão patrimonial.

## Tecnologias

| Tecnologia | Versão |
|-----------|--------|
| Java | 25 |
| Spring Boot | 3.5.14 |
| Node.js | 22.13.1 |
| React | 19.2.5 |
| Vite | 8.0.10 |
| Tailwind CSS | 4.2.4 |
| java-jwt | 4.5.1 |
| MapStruct | 1.6.3 |
| Bucket4j | 8.10.1 |
| SpringDoc OpenAPI | 2.8.16 |
| Lombok | 1.18.44 |

## Estrutura do Projeto

```text
INVEST_MAIS/
├── .github/
├── .gitignore
├── .env.example
├── pom.xml
├── README.md
├── basedata.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/repositorio/mvp/
│   │   │       ├── MvpApplication.java
│   │   │       ├── common/
│   │   │       │   ├── DTO/
│   │   │       │   ├── constants/
│   │   │       │   ├── model/
│   │   │       │   ├── result/
│   │   │       │   ├── security/
│   │   │       │   └── validation/
│   │   │       ├── domain/
│   │   │       │   ├── admin/
│   │   │       │   ├── asset/
│   │   │       │   ├── auth/
│   │   │       │   ├── portfolio/
│   │   │       │   ├── question/
│   │   │       │   └── user/
│   │   │       └── infrastructure/
│   │   │           ├── config/
│   │   │           ├── exception/
│   │   │           ├── security/
│   │   │           ├── util/
│   │   │           └── web/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── src/
    │   ├── App.jsx
    │   ├── components/
    │   ├── pages/
    │   ├── services/
    │   └── styles/
    ├── public/
    ├── index.html
    └── dist/
```
## Materiais Complementares
[Acessar Documentos](./documentos/)