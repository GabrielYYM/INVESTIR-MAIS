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

```
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

## Como Executar

### Pré-requisitos

Garante que possui as seguintes ferramentas instaladas em sua máquina:
- **Java 21 ou superior** (JDK)
- **Node.js 20 ou superior** e **npm**
- **Git**

---

### 1. Clonar o Repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd INVESTIR-MAIS/investir_mais
```

---

### 2. Executar o Backend (Spring Boot)

O backend executa por padrão na porta `8080` utilizando um banco de dados H2 em memória.

**No Windows (PowerShell ou Command Prompt):**
```powershell
.\mvnw.cmd spring-boot:run
```

**No Linux / macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

- **Servidor da API:** `http://localhost:8080`
- **Console H2:** `http://localhost:8080/h2-console`
  - **JDBC URL:** `jdbc:h2:mem:investir_mais`
  - **Usuário:** `sa`
  - **Senha:** *(deixe em branco)*

*(Opcional)* Caso deseje utilizar o PostgreSQL, copie o `.env.example` para `.env` na raiz do projeto e configure as credenciais de banco.

---

### 3. Executar o Frontend (React + Vite)

Em um novo terminal, navegue até o diretório do frontend e instale as dependências:

```bash
cd frontend/investirMais
npm install
```

Configure as variáveis de ambiente criando/editando o arquivo `.env` localizado dentro de `frontend/investirMais/`:
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_BRAPI_TOKEN=seu_token_da_brapi_aqui
```

Inicie a aplicação no modo de desenvolvimento:
```bash
npm run dev
```

- **Aplicação Web Frontend:** `http://localhost:5173`

---

## Materiais Complementares
[Acessar Documentos](./documentos/)
