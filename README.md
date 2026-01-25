# 🛒 ByteMarket API

> API REST completa para marketplace de produtos digitais com pagamento PIX via Mercado Pago

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**🌐 Aplicação em Produção:** [bytemarket-15nv.onrender.com](https://bytemarket-15nv.onrender.com/)

---

## 📖 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Arquitetura e Funcionalidades](#-arquitetura-e-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Executando](#-executando)
- [Documentação da API](#-documentação-da-api)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Fluxo de Pagamento PIX](#-fluxo-de-pagamento-pix)
- [Segurança](#-segurança)
- [Deploy](#-deploy)
- [Autor](#-autor)

---

## 🎯 Sobre o Projeto

ByteMarket é uma **API REST robusta e escalável** desenvolvida para marketplaces de produtos digitais, implementando um sistema completo de e-commerce com entrega automática por email.

### Principais Diferenciais

✅ **Autenticação JWT** com suporte a roles (USER/ADMIN)  
💳 **Integração real com Mercado Pago** para pagamentos PIX  
📧 **Entrega automática** de produtos via email após confirmação de pagamento  
🔔 **Sistema de webhooks** com validação HMAC para segurança  
📦 **Gestão inteligente de estoque** com controle de concorrência (Optimistic Locking)  
🔐 **Segurança em múltiplas camadas** (BCrypt, JWT, validação de entrada)  
📚 **Documentação interativa** com Swagger/OpenAPI  

---

## 🏗 Arquitetura e Funcionalidades

### Para Usuários Finais
- ✅ Registro e autenticação com JWT
- 🔍 Busca e listagem de produtos com paginação
- 🛒 Sistema completo de pedidos
- 💰 Pagamento via PIX com QR Code dinâmico
- 📨 Recebimento automático de credenciais por email
- 📜 Histórico completo de pedidos

### Para Administradores
- 🎨 CRUD completo de produtos
- 📦 Gestão de estoque (adicionar itens digitais)
- 📊 Visualização de status de estoque (disponível/vendido)
- 🔧 Controle de vendas e pedidos

### Automações Backend
- 🤖 Processamento automático de webhooks do Mercado Pago
- 🔄 Atualização de estoque em tempo real com Optimistic Locking
- 📧 Envio de emails transacionais com templates Thymeleaf
- ✅ Validação de assinatura HMAC SHA-256 para webhooks

---

## 🛠 Tecnologias

### Core
- **Java 17** - Linguagem de programação
- **Spring Boot 3.4.2** - Framework principal
- **PostgreSQL** - Banco de dados em produção
- **H2 Database** - Banco de dados para desenvolvimento

### Spring Framework
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **Spring Validation** - Validação de entrada
- **Spring Mail** - Envio de emails

### Segurança & Autenticação
- **JJWT 0.12.5** - Geração e validação de tokens JWT
- **BCrypt** - Criptografia de senhas

### Pagamentos
- **Mercado Pago SDK 2.1.28** - Integração com gateway de pagamento
- **Apache Commons Codec** - Validação HMAC de webhooks

### Comunicação
- **Thymeleaf** - Engine de templates para emails HTML

### Documentação
- **Springdoc OpenAPI 2.7.0** - Documentação Swagger/OpenAPI

### Utilitários
- **Lombok** - Redução de código boilerplate
- **Maven** - Gerenciamento de dependências

---

## 📋 Pré-requisitos

```bash
# Java 17+
java -version

# Maven 3.9+
mvn -version

# PostgreSQL (opcional para desenvolvimento)
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

### 1. Variáveis de Ambiente

Configure as seguintes variáveis de ambiente ou crie um arquivo `.env`:

```properties
# Banco de Dados
DB_HOST=localhost
DB_NAME=bytemarket
DB_USER=postgres
DB_PASSWORD=sua_senha

# Segurança JWT
JWT_SECRET=sua_chave_base64_aqui
JWT_EXPIRATION=86400000

# Admin (será criado automaticamente)
ADMIN_EMAIL=admin@bytemarket.com
ADMIN_PASSWORD=senha_segura

# Email (Gmail)
EMAIL_USER=seu-email@gmail.com
EMAIL_PASSWORD=sua_senha_app

# Mercado Pago
MP_ACCESS_TOKEN=TEST-seu-token
MP_PUBLIC_KEY=TEST-sua-public-key
MP_WEBHOOK_SECRET=seu-webhook-secret

# Base URL
BASE_URL=http://localhost:8080
```

### 2. Gerar JWT Secret

```bash
# Gere uma chave base64 segura
echo -n "sua-chave-secreta-muito-longa-e-segura" | base64
```

### 3. Configurar Email Gmail

1. Acesse [Senhas de App do Google](https://myaccount.google.com/apppasswords)
2. Gere uma senha específica para a aplicação
3. Use essa senha na variável `EMAIL_PASSWORD`

### 4. Configurar Mercado Pago

1. Acesse [Painel de Desenvolvedores do Mercado Pago](https://www.mercadopago.com.br/developers/panel/credentials)
2. Obtenha suas credenciais de teste/produção
3. Configure o webhook em `Integrações > Notificações`
4. Use a URL: `https://seu-dominio.com/webhooks/payment`

---

## ▶️ Executando

### Desenvolvimento (H2 em memória)

```bash
# Usando Maven
mvn spring-boot:run

# Ou usando o wrapper
./mvnw spring-boot:run
```

### Produção

```bash
# Compilar
mvn clean package -DskipTests

# Executar
java -jar target/bytemarket-api-0.0.1-SNAPSHOT.jar
```

A API estará disponível em: `http://localhost:8080`

---

## 📚 Documentação da API

### Swagger UI (Documentação Interativa)

```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principais

#### 🔐 Autenticação
```http
POST   /auth/register     # Registrar novo usuário
POST   /auth/login        # Autenticar usuário
GET    /auth/me           # Obter dados do usuário logado
```

#### 📦 Produtos (Público)
```http
GET    /products          # Listar produtos (paginado)
GET    /products/{id}     # Detalhes de um produto
GET    /products/search   # Buscar produtos por título
```

#### 🛒 Pedidos (Autenticado)
```http
POST   /orders                      # Criar novo pedido
GET    /users/{userId}/orders       # Histórico de pedidos
GET    /users/{userId}/orders/{id}  # Detalhes de um pedido
```

#### 💳 Pagamentos (Autenticado)
```http
POST   /payments/pix/orders/{orderId}  # Gerar pagamento PIX
GET    /payments/{paymentId}            # Consultar status do pagamento
```

#### 🔧 Admin (ROLE_ADMIN)
```http
POST   /admin/products                        # Criar produto
PUT    /admin/products/{id}                   # Atualizar produto
DELETE /admin/products/{id}                   # Desativar produto
POST   /admin/products/{id}/stock             # Adicionar itens ao estoque
GET    /admin/products/{id}/stock/status      # Status do estoque
```

#### 🔔 Webhooks (Mercado Pago)
```http
POST   /webhooks/payment  # Receber notificações de pagamento
```

---

## 📂 Estrutura do Projeto

```
bytemarket-api/
│
├── src/main/java/com/bytemarket/bytemarket_api/
│   ├── config/                 # Configurações (Security, Email, MP, Swagger)
│   │   ├── AdminSeeder.java
│   │   ├── EmailConfig.java
│   │   ├── MercadoPagoConfiguration.java
│   │   └── SecurityConfig.java
│   │
│   ├── controllers/            # Controllers REST
│   │   ├── AuthController.java
│   │   ├── ProductController.java
│   │   ├── OrderController.java
│   │   ├── PaymentController.java
│   │   ├── AdminProductController.java
│   │   ├── UserOrderController.java
│   │   └── WebhookController.java
│   │
│   ├── domain/                 # Entidades JPA
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Payment.java
│   │   └── StockItem.java
│   │
│   ├── dto/                    # DTOs (Request/Response)
│   │   ├── request/
│   │   └── response/
│   │
│   ├── exceptions/             # Tratamento de exceções
│   │   ├── ResourceExceptionHandler.java
│   │   ├── OutOfStockException.java
│   │   └── DuplicateEmailException.java
│   │
│   ├── repository/             # Repositories JPA
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── OrderRepository.java
│   │   ├── PaymentRepository.java
│   │   └── StockItemRepository.java
│   │
│   ├── security/               # JWT, Filters, UserDetails
│   │   ├── JwtUtils.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── service/                # Lógica de negócio
│   │   ├── AuthService.java
│   │   ├── ProductService.java
│   │   ├── OrderService.java
│   │   ├── PixPaymentService.java
│   │   ├── EmailService.java
│   │   ├── WebhookService.java
│   │   └── AdminProductService.java
│   │
│   └── validation/             # Validadores customizados
│       ├── EmailValidator.java
│       └── StockValidator.java
│
├── src/main/resources/
│   ├── templates/email/        # Templates Thymeleaf
│   │   └── order-confirmation.html
│   ├── static/                 # Arquivos estáticos do frontend
│   └── application.properties
│
└── src/test/                   # Testes
    └── java/com/bytemarket/bytemarket_api/
```

---

## 💰 Fluxo de Pagamento PIX

### 1. Criação do Pedido
```java
POST /orders
{
  "userId": "uuid-do-usuario",
  "deliveryEmail": "cliente@email.com",
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

**Backend:**
- Valida disponibilidade de estoque
- Reserva itens do estoque (marca como `sold=true`)
- Calcula o total do pedido
- Cria registro com status `WAITING_PAYMENT`

### 2. Geração do PIX
```java
POST /payments/pix/orders/{orderId}
```

**Backend:**
- Chama API do Mercado Pago
- Gera QR Code dinâmico
- Salva pagamento com status `PENDING`
- Retorna QR Code (base64) e código copia-cola

### 3. Webhook de Confirmação
```java
POST /webhooks/payment
```

**Backend:**
- Valida assinatura HMAC SHA-256
- Consulta status na API do Mercado Pago
- Atualiza status do pagamento
- Se `APPROVED`:
  - Marca pedido como `PAID`
  - Envia email com produtos

### 4. Validação HMAC

```java
// Cálculo da assinatura esperada
String manifest = "id:" + dataId + 
                  ";request-id:" + xRequestId + 
                  ";ts:" + timestamp + ";";
String expectedHash = HMAC_SHA256(manifest, webhookSecret);

// Comparação segura
if (expectedHash.equals(receivedHash)) {
    // Webhook válido
}
```

---

## 🔐 Segurança

### Implementações de Segurança

| Camada | Implementação |
|--------|---------------|
| **Senhas** | BCrypt com salt aleatório |
| **Autenticação** | JWT com expiração configurável |
| **Autorização** | Role-Based Access Control (RBAC) |
| **Webhooks** | Validação HMAC SHA-256 |
| **Entrada** | Bean Validation em todos os endpoints |
| **Concorrência** | Optimistic Locking (`@Version`) |
| **SQL Injection** | JPA/Hibernate com queries preparadas |
| **CORS** | Configurado no SecurityConfig |

### Exemplo de Token JWT

```json
{
  "sub": "usuario@email.com",
  "role": "USER",
  "iat": 1706198400,
  "exp": 1706284800
}
```

### Fluxo de Autenticação

```
1. POST /auth/login
   ↓
2. Validação (Spring Security)
   ↓
3. Geração JWT
   ↓
4. Retorno do token
   ↓
5. Cliente armazena token
   ↓
6. Requisições com header:
   Authorization: Bearer {token}
```

---

## 🚢 Deploy

### Docker Compose (Desenvolvimento)

```bash
# Subir banco de dados
docker-compose up -d bytemarket-db

# Compilar e executar API
docker-compose up --build bytemarket-api
```

### Render (Produção)

1. **Criar Web Service:**
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/bytemarket-api-0.0.1-SNAPSHOT.jar`

2. **Configurar Variáveis de Ambiente:**
   ```
   DB_HOST=postgres-host.render.com
   DB_NAME=bytemarket
   DB_USER=usuario
   DB_PASSWORD=senha
   JWT_SECRET=chave-base64
   MP_ACCESS_TOKEN=token-producao
   MP_WEBHOOK_SECRET=webhook-secret
   BASE_URL=https://seu-app.onrender.com
   ```

3. **Criar PostgreSQL Database:**
   - Vincular ao Web Service
   - Render injeta automaticamente `DATABASE_URL`

### Heroku

```bash
# Login
heroku login

# Criar app
heroku create bytemarket-api

# Adicionar PostgreSQL
heroku addons:create heroku-postgresql:mini

# Configurar variáveis
heroku config:set JWT_SECRET=sua-chave
heroku config:set MP_ACCESS_TOKEN=token

# Deploy
git push heroku main
```

---

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Teste específico
mvn test -Dtest=PixPaymentIntegrationTest
```

### Teste Manual com cURL

#### Registrar Usuário
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "password": "senha123"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "senha123"
  }'
```

#### Listar Produtos
```bash
curl http://localhost:8080/products
```

#### Criar Produto (Admin)
```bash
curl -X POST http://localhost:8080/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {seu-token}" \
  -d '{
    "title": "Produto Digital",
    "description": "Descrição detalhada",
    "price": 49.90,
    "imageUrl": "https://exemplo.com/imagem.jpg",
    "type": "AUTOMATIC_DELIVERY"
  }'
```

---

## 🎯 Decisões de Design

### Por que Optimistic Locking?

```java
@Entity
public class StockItem {
    @Version
    private Long version;
    // ...
}
```

**Problema:** Múltiplos usuários comprando o último item em estoque.

**Solução:** JPA incrementa `version` automaticamente. Se duas transações tentarem atualizar o mesmo item, apenas a primeira sucede. A segunda recebe `OptimisticLockingFailureException`.

### Por que Email no Pedido?

Permite que usuários comprem e recebam em emails diferentes (ex: presente para outra pessoa).

### Por que Webhooks em vez de Polling?

Webhooks são:
- ✅ Mais eficientes (push vs pull)
- ✅ Tempo real
- ✅ Menos carga no servidor
- ✅ Padrão recomendado pelo Mercado Pago

---

## 📊 Status do Projeto

✅ **Em Produção** - [bytemarket-15nv.onrender.com](https://bytemarket-15nv.onrender.com/)

### Funcionalidades Implementadas

- [x] Autenticação JWT com refresh
- [x] CRUD completo de produtos
- [x] Sistema de pedidos
- [x] Pagamento PIX com Mercado Pago
- [x] Webhooks com validação HMAC
- [x] Envio de emails transacionais
- [x] Gestão de estoque com Optimistic Locking
- [x] Documentação Swagger
- [x] Deploy em produção
- [x] Interface web funcional

### Roadmap Futuro

- [ ] Implementar Redis para cache
- [ ] Sistema de cupons de desconto
- [ ] Painel de analytics para admin
- [ ] Testes de integração completos
- [ ] CI/CD com GitHub Actions
- [ ] Suporte a múltiplos métodos de pagamento

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

### Padrões de Código

- Seguir convenções do Spring Boot
- Documentar métodos públicos com Javadoc
- Escrever testes para novos endpoints
- Validar entrada com Bean Validation

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👨‍💻 Autor

**Kayk Edmar**

- GitHub: [@KaykMurphy](https://github.com/KaykMurphy)
- LinkedIn: [Kayk Edmar](https://www.linkedin.com/in/kayk-edmar/)

---

## 🙏 Agradecimentos

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework utilizado
- [Mercado Pago Developers](https://www.mercadopago.com.br/developers) - Gateway de pagamento
- [Swagger](https://swagger.io/) - Documentação da API
- Comunidade open-source

---

## 📞 Suporte

Encontrou um bug? Tem uma sugestão?

- 🐛 [Reportar Bug](https://github.com/KaykMurphy/bytemarket-api/issues)
- 💡 [Sugerir Feature](https://github.com/KaykMurphy/bytemarket-api/issues)

---

<div align="center">

**⭐ Se este projeto foi útil para você, considere dar uma estrela!**

Desenvolvido com ❤️ e ☕ por [Kayk Edmar](https://github.com/KaykMurphy)

</div>
