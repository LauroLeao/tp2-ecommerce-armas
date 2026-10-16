# E-commerce de armas — TP2

Projeto com dois CRUDs completos, desenvolvido com Quarkus, Angular e PostgreSQL.

## Estrutura

- `armas-api`: API REST em Quarkus.
- `ecommerce-armas`: aplicação web em Angular.
- `docker-compose.yml`: banco PostgreSQL usado durante o desenvolvimento.

## Como executar

### 1. Banco de dados

Na pasta raiz do projeto, execute:

```powershell
docker compose up -d
```

O PostgreSQL ficará disponível na porta `5433`. O banco, o usuário e as permissões são criados automaticamente.

### 2. Back-end

Abra outro terminal:

```powershell
cd armas-api
.\mvnw.cmd quarkus:dev
```

A API ficará disponível em `http://localhost:8080`.

### 3. Front-end

Abra mais um terminal:

```powershell
cd ecommerce-armas
npm install
npm start
```

A aplicação ficará disponível em `http://localhost:4200`.

## Testes

Back-end:

```powershell
cd armas-api
.\mvnw.cmd test
```

Front-end:

```powershell
cd ecommerce-armas
npm test -- --watch=false
```

Os testes do back-end usam um banco temporário em memória. O PostgreSQL não precisa estar iniciado para executá-los.

## Preparação para entrega

Execute na pasta raiz:

```powershell
.\preparar-entrega.ps1
```

O script cria `ecommerce-armas-entrega.zip` fora da pasta do projeto com o código-fonte do back e do front. As pastas geradas, como `node_modules`, `dist`, `target` e `.angular`, não são incluídas.
