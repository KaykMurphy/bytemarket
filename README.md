# ByteMarket API

Uma API REST robusta e escalável para uma plataforma de marketplace digital, construída com Spring Boot e práticas modernas de Java.

## 📋 Visão Geral

ByteMarket é uma solução backend projetada para venda de produtos digitais com entrega automática. A plataforma suporta gerenciamento de inventário, processamento de pedidos e entrega instantânea de conteúdo digital como credenciais de login, chaves de licença e códigos de acesso.

## 🏗️ Arquitetura

O projeto segue um padrão de arquitetura em camadas com clara separação de responsabilidades:

```
├── controllers/     # Endpoints REST e manipulação de requisições
├── services/        # Lógica de negócio e orquestração
├── repositories/    # Camada de acesso a dados
├── domain/          # Modelos de entidade e objetos de negócio
└── dto/             # Objetos de Transferência de Dados
```

### Principais Padrões de Projeto

- **Strategy Pattern**: Processamento de pagamento flexível através da interface `PaymentStrategy`
- **Repository Pattern**: Abstração limpa de acesso a dados com Spring Data JPA
- **DTO Pattern**: Contratos de API desacoplados dos modelos de domínio internos

## 🚀 Funcionalidades

### Implementação Atual

- ✅ Gerenciamento de catálogo de produtos com paginação
- ✅ Sistema de controle de inventário digital
- ✅ Processamento automatizado de pedidos
- ✅ Entrega instantânea de conteúdo digital
- ✅ Bloqueio otimista para itens de estoque
- ✅ Suporte a múltiplos tipos de produto (entrega automática e serviços)
- ✅ Geração de recibo de pedido com conteúdo entregue

### Tipos de Produto

- **AUTOMATIC_DELIVERY**: Produtos digitais com entrega instantânea (contas, chaves, códigos)
- **SERVICE**: Produtos baseados em serviço sem inventário físico

## 🛠️ Stack Tecnológico

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Maven**

## 📦 Instalação

### Pré-requisitos

- JDK 17 ou superior
- PostgreSQL 12+
- Maven 3.9+

### Configuração

1. Clone o repositório:
```bash
git clone https://github.com/seuusuario/bytemarket-api.git
cd bytemarket-api
```

2. Configure a conexão com o banco de dados em `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bytemarket
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

3. Compile o projeto:
```bash
mvn clean install
```

4. Execute a aplicação:
```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`

## 📚 Endpoints da API

### Produtos

```http
GET /products?page=0&size=10
```

Retorna uma lista paginada de produtos disponíveis.

**Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Conta Netflix Premium",
      "price": 29.90,
      "imageUrl": "https://example.com/image.jpg",
      "type": "AUTOMATIC_DELIVERY"
    }
  ],
  "pageable": {...},
  "totalElements": 100
}
```

### Pedidos

```http
POST /orders
```

Cria um novo pedido e processa o pagamento.

**Requisição:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

**Resposta:**
```json
{
  "id": 1,
  "moment": "2025-01-15T10:30:00Z",
  "total": 59.80,
  "status": "PAID",
  "items": [
    {
      "title": "Conta Netflix Premium",
      "quantity": 2,
      "price": 29.90,
      "deliveredContent": [
        "login1:password1",
        "login2:password2"
      ]
    }
  ]
}
```

## 🗄️ Esquema do Banco de Dados

### Entidades Principais

- **User**: Informações do cliente e autenticação
- **Product**: Itens da vitrine (catálogo)
- **StockItem**: Conteúdo digital real (segredos/credenciais)
- **Order**: Transações de compra
- **OrderItem**: Itens de linha dentro de um pedido

### Relacionamentos das Entidades

```
User 1---* Order
Order 1---* OrderItem
OrderItem *---1 Product
Product 1---* StockItem
```

## 🔒 Considerações de Segurança

> ⚠️ **Nota**: Este projeto está em desenvolvimento ativo. Recursos de segurança estão sendo implementados.

Lacunas de segurança sendo abordadas:
- Mecanismos de autenticação e autorização
- Criptografia de senhas (BCrypt)
- Limitação de taxa de API
- Criptografia de conteúdo para dados sensíveis
- Validação e sanitização de entrada

## 🧪 Testes

Execute os testes unitários:
```bash
mvn test
```

Execute os testes de integração:
```bash
mvn verify
```

## 📈 Desenvolvimento Futuro

Este projeto está em desenvolvimento ativo. Funcionalidades e melhorias planejadas são rastreadas na seção [Issues](../../issues).

### Destaques do Roadmap

- 🔐 Integração com Spring Security
- 🔑 Autenticação baseada em JWT
- 💳 Integração com gateway de pagamento real
- 📧 Notificações por email
- 📊 Endpoints de painel administrativo
- 🔍 Busca e filtragem avançadas
- 📦 Suporte a webhooks para atualizações de pedidos
- 🌐 Documentação da API com Swagger/OpenAPI
- ⚡ Camada de cache com Redis
- 📈 Monitoramento e observabilidade

Confira a página de [Issues](../../issues) para solicitações de recursos detalhadas e relatórios de bugs.

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para enviar um Pull Request.

1. Faça um fork do projeto
2. Crie sua branch de feature (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

## 👤 Autor

**Kayk Edmar**

- GitHub: [@KaykMurphy](https://github.com/KaykMurphy)
- LinkedIn: [Kayk Edmar](https://www.linkedin.com/in/kayk-edmar/)

## 🙏 Agradecimentos

- Comunidade Spring Boot pela excelente documentação
- Contribuidores que ajudam a melhorar este projeto

---

**Status**: 🚧 Em Desenvolvimento Ativo

Para dúvidas ou suporte, por favor abra uma issue ou entre em contato com os mantenedores.
