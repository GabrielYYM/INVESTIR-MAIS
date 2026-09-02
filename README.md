# INVESTIR MAIS

[![Java Version](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Seguran%C3%A7a-Refor%C3%A7ada-red?style=for-the-badge&logo=springsecurity)](https://spring.io/projects/spring-security)
[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://vercel.com/)
[![Railway](https://img.shields.io/badge/Railway-131415?style=for-the-badge&logo=railway&logoColor=white)](https://railway.app/)



O **INVESTE+** é um backend robusto e de alta performance para gestão de investimentos, construído com **Spring Boot 3.5** e **Java 25**. Desenvolvido com mentalidade *security-first*, o projeto implementa padrões avançados de autenticação, filtros de infraestrutura defensivos e segue rigorosamente os princípios **SOLID** e **Domain-Driven Design (DDD)** para garantir escalabilidade e manutenibilidade.

---

## Tecnologias

- **Core**: Java 25 LTS & Spring Boot 3.5.x
- **Persistência**: PostgreSQL (Produção) / H2 (Desenvolvimento & Testes)
- **Segurança**: 
  - Spring Security com JWT (java-jwt)
  - Integração OAuth2 Client
  - Hashing de senhas com Argon2
  - Criptografia AES-256 no Banco de Dados para campos sensíveis
- **Infraestrutura**:
  - **Rate Limiting**: Implementação usando Bucket4j & Caffeine
  - **Validação**: Bean Validation (Hibernate Validator)
  - **Mapeadores**: MapStruct para conversão limpa entre Entidades e DTOs
- **Documentação**: OpenAPI 3 / Swagger (SpringDoc UI)
- **Utilitários**: Lombok, dotenv-java, BouncyCastle

---
## Arquitetura e Princípios

Este projeto foi construído para transcender os padrões básicos de um MVP, adotando as melhores práticas de engenharia de software moderna:

- **Domain-Driven Design (DDD)**: Lógica organizada por limites de domínio (`auth`, `user`, `asset`, `admin`).
- **Princípios SOLID**: Foco no desacoplamento, responsabilidade única e design orientado a interfaces.
- **Programação Defensiva**: Validação extensiva de entradas e tratamento padronizado de erros.
- **Endurecimento de Segurança (Hardening)**:
  - Tokens JWT de curta duração com lista de bloqueio (*blacklist*) no servidor (hasheada).
  - Implementação de 2FA (Autenticação de Dois Fatores) em múltiplas camadas.

---
