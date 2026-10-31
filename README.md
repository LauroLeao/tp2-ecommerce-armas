# E-commerce de armas — TP2

Projeto desenvolvido com Quarkus, Angular e PostgreSQL. O painel administrativo reúne catálogo (produtos, categorias, marcas e modelos), vendas (pedidos e clientes) e fornecedores. Há também login e painel de cliente.

Marcas e modelos são gerenciados na mesma tela: selecione uma marca para ver e cadastrar seus modelos. No produto, escolha a marca e o modelo correspondente; o fornecedor é opcional. Produtos antigos permanecem preservados.

Pedidos são cadastrados pelo administrativo com cliente e itens. Ao confirmar um pedido, o estoque é descontado; ao cancelar um pedido confirmado, ele é devolvido. A tela de pedidos permite filtrar por cliente e consultar o histórico de itens e preços registrados. O cliente pode criar sua conta, entrar e consultar seus próprios pedidos. Ainda não há carrinho nem pagamento: o painel não registra compras automaticamente.

As rotas administrativas exigem o perfil `ADMIN` no Quarkus. O perfil `CLIENTE` acessa apenas seu cadastro e seus pedidos. A interface também separa os menus por perfil. As senhas são armazenadas como hash bcrypt e a sessão usa cookie HttpOnly.

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

Antes de iniciar pela primeira vez, copie `armas-api/.env.example` para `armas-api/.env` e substitua a chave e a senha por valores próprios. O arquivo `.env` é ignorado pelo Git. A conta administrativa é criada na primeira inicialização; clientes cadastrados anteriormente não recebem senha automaticamente.

```powershell
cd armas-api
.\mvnw.cmd quarkus:dev
```

A API ficará disponível em `http://localhost:8080`.

### 3. Front-end

Abra mais um terminal:

```powershell
cd ecommerce-armas
npm.cmd install
npm.cmd start
```

A aplicação ficará disponível em `http://localhost:4200/login`. Após entrar, o painel inicial será diferente para administrador e cliente. É possível alternar entre os modos claro e escuro.

## Testes

Back-end:

```powershell
cd armas-api
.\mvnw.cmd test
```

Front-end:

```powershell
cd ecommerce-armas
npm.cmd test -- --watch=false
```

Os testes do back-end usam um banco temporário em memória. O PostgreSQL não precisa estar iniciado para executá-los.
