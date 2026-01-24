# 🛒 ByteMarket API

> API REST completa para marketplace de produtos digitais com pagamento PIX via Mercado Pago

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Executando](#-executando)
- [Documentação da API](#-documentação-da-api)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Testes](#-testes)
- [Deploy](#-deploy)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)

---

## 🎯 Sobre o Projeto

ByteMarket é uma API REST robusta para criação de marketplaces de produtos digitais (contas de streaming, softwares, etc.) com:

- ✅ Autenticação JWT segura
- 💳 Pagamento PIX real via Mercado Pago
- 📧 Envio automático de produtos por email
- 🔔 Webhooks para confirmação de pagamento
- 📦 Gestão automática de estoque
- 🔐 Sistema de roles (USER/ADMIN)
- 📚 Documentação Swagger/OpenAPI

---

## ✨ Funcionalidades

### 👤 Para Usuários
- Registro e login com JWT
- Listagem de produtos com paginação
- Criação de pedidos
- Pagamento via PIX (QR Code)
- Recebimento automático de produtos por email
- Histórico de pedidos

### 👨‍💼 Para Administradores
- CRUD completo de produtos
- Gestão de estoque (adicionar contas digitais)
- Visualização de status de estoque
- Controle de vendas

### 🔄 Automações
- Envio automático de contas após pagamento
- Atualização de estoque em tempo real
- Webhooks do Mercado Pago
- Validação HMAC de webhooks

---

## 🛠 Tecnologias

### Backend
- **Java 17**
- **Spring Boot 3.4.2**
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - Spring Mail
- **PostgreSQL** (Produção)
- **H2** (Desenvolvimento)

### Pagamento & Comunicação
- **Mercado Pago SDK 2.1.28**
- **JWT (JJWT 0.12.5)**
- **Thymeleaf** (Templates de email)

### Documentação
- **Springdoc OpenAPI 2.7.0** (Swagger)

### Utilitários
- **Lombok**
- **Maven**

---

## 📋 Pré-requisitos

```bash
# Java 17+
java -version

# Maven 3.9+
mvn -version

# PostgreSQL (opcional, pode usar H2)
psql --version

# Git
git --version
```

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/KaykMurphy/bytemarket-api.git
cd bytemarket-api
```

### 2. Instale as dependências

```bash
mvn clean install
```

---

## ⚙️ Configuração

### 1. Crie o arquivo `application.properties`

Copie o arquivo exemplo e configure:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 2. Configure as variáveis obrigatórias

```properties
# JWT Secret (gere uma chave base64)
jwt.secret=dGhpc2lzYXZlcnlzZWNyZXRrZXlmb3Jqd3RhdXRoZW50aWNhdGlvbg==
jwt.expiration=86400000

# Admin (será criado automaticamente)
admin.email=admin@bytemarket.com
admin.password=suaSenhaSegura123

# Banco de Dados (H2 em memória para desenvolvimento)
spring.datasource.url=jdbc:h2:mem:bytemarket
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
```

### 3. Configure Email (Opcional)

Para envio de emails, configure o Gmail:

```properties
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-app

# Como obter senha de app:
# https://myaccount.google.com/apppasswords
```

### 4. Configure Mercado Pago (Opcional)

Para pagamentos PIX reais:

```properties
payment.mercadopago.access-token=TEST-seu-token-aqui
payment.mercadopago.public-key=TEST-sua-public-key
payment.mercadopago.webhook-secret=seu-webhook-secret

# Obtenha em: https://www.mercadopago.com.br/developers/panel/credentials
```

---

## ▶️ Executando

### Desenvolvimento

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: **http://localhost:8080**

### Produção

```bash
# Compilar
mvn clean package -DskipTests

# Executar
java -jar target/bytemarket-api-0.0.1-SNAPSHOT.jar
```

---

## 📚 Documentação da API

### Swagger UI

Acesse a documentação interativa:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principais

#### Autenticação
```http
POST /auth/register - Registrar usuário
POST /auth/login    - Login
```

#### Produtos (Público)
```http
GET  /products      - Listar produtos
GET  /products/{id} - Detalhes do produto
```

#### Pedidos (Autenticado)
```http
POST /orders        - Criar pedido
GET  /users/{userId}/orders - Histórico de pedidos
```

#### Pagamentos (Autenticado)
```http
POST /payments/pix/orders/{orderId} - Gerar PIX
GET  /payments/{paymentId}          - Status do pagamento
```

#### Admin (Requer ROLE_ADMIN)
```http
POST   /admin/products              - Criar produto
PUT    /admin/products/{id}         - Atualizar produto
DELETE /admin/products/{id}         - Deletar produto
POST   /admin/products/{id}/stock   - Adicionar estoque
GET    /admin/products/{id}/stock/status - Status do estoque
```

---

## 📂 Estrutura do Projeto

```
bytemarket-api/
│
├── src/main/java/com/bytemarket/bytemarket_api/
│   ├── config/              # Configurações (Security, Swagger, Email, MP)
│   ├── controllers/         # Controllers REST
│   ├── domain/              # Entidades JPA
│   ├── dto/                 # DTOs (Request/Response)
│   ├── exceptions/          # Tratamento de exceções
│   ├── repository/          # Repositories JPA
│   ├── security/            # JWT, UserDetails, Filters
│   ├── service/             # Lógica de negócio
│   └── validation/          # Validadores customizados
│
├── src/main/resources/
│   ├── templates/email/     # Templates Thymeleaf
│   └── application.properties
│
└── src/test/                # Testes unitários/integração
```

---

## 🧪 Testes

### Executar todos os testes

```bash
mvn test
```

### Testar manualmente com cURL

#### 1. Registrar usuário
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "password": "senha123"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "senha123"
  }'
```

#### 3. Listar produtos
```bash
curl http://localhost:8080/products
```

---

## 🚢 Deploy

### Heroku

```bash
# Login
heroku login.html

# Criar app
heroku create bytemarket-api

# Adicionar PostgreSQL
heroku addons:create heroku-postgresql:mini

# Configurar variáveis
heroku config:set JWT_SECRET=sua-chave-aqui
heroku config:set ADMIN_PASSWORD=senha-admin

# Deploy
git push heroku main
```

### Docker

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# Build
docker build -t bytemarket-api .

# Run
docker run -p 8080:8080 \
  -e JWT_SECRET=sua-chave \
  -e ADMIN_PASSWORD=senha \
  bytemarket-api
```

---

## 🔐 Segurança

- ✅ Senhas criptografadas com BCrypt
- ✅ Autenticação JWT com expiração
- ✅ Validação HMAC de webhooks
- ✅ Validação de entrada com Bean Validation
- ✅ CORS configurado
- ✅ Rate limiting (recomendado para produção)

---

## 🤝 Contribuindo

Contribuições são bem-vindas!

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👨‍💻 Autor

Desenvolvido com ❤️ por **Kayk Edmar**

- GitHub: [@KaykMurphy](https://github.com/KaykMurphy)
- LinkedIn: [Kayk Edmar](https://www.linkedin.com/in/kayk-edmar/)
- Portfolio: [github.com/KaykMurphy](https://github.com/KaykMurphy)

---

## 🙏 Agradecimentos

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Mercado Pago Developers](https://www.mercadopago.com.br/developers)
- [Swagger](https://swagger.io/)
- Comunidade open-source

---

## 📞 Suporte

Encontrou um bug? Tem uma sugestão?

- 🐛 [Reportar Bug](https://github.com/KaykMurphy/bytemarket-api/issues)
- 💡 [Sugerir Feature](https://github.com/KaykMurphy/bytemarket-api/issues)
- 💬 [Discussões](https://github.com/KaykMurphy/bytemarket-api/discussions)

---

## 📊 Status do Projeto

✅ **Em Desenvolvimento Ativo**

### Roadmap

- [x] Autenticação JWT
- [x] CRUD de produtos
- [x] Pagamento PIX
- [x] Webhooks
- [x] Envio de emails
- [x] Documentação Swagger
- [ ] Dashboard administrativo
- [ ] Notificações em tempo real
- [ ] Sistema de cupons
- [ ] Avaliações de produtos

---

<div align="center">

**⭐ Se este projeto te ajudou, deixe uma estrela!**

</div>
