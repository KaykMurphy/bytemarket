












2. Ingredientes (Camada de Domínio / Entities)
   Pacote: com.seuprojeto.api.domain Instrução Geral: Todas as classes devem ter anotações @Entity, @Table, @Id (PK) e Lombok (@Data ou @Getter/@Setter).

2.1. Entidade: User
Objetivo: Representar o comprador.

Atributos: id (Long/UUID), name (String), email (String), password (String).

Relacionamento: Um User tem muitos Order (@OneToMany).

2.2. Enum: ProductType
Valores: AUTOMATIC_DELIVERY (para contas/chaves enviadas na hora), SERVICE (para serviços manuais).

2.3. Entidade: Product
Objetivo: A vitrine (o que o usuário vê).

Atributos: id, title, description, price (BigDecimal), imageUrl (String - apenas a URL), type (Enum ProductType).

Anotação Especial: @OneToMany para StockItem.

2.4. Entidade: StockItem
Objetivo: O item digital real (segredo/login).

Atributos: id, content (String - ex: "login:x|senha:y"), sold (Boolean - indica se já foi vendido).

Controle de Concorrência: Adicionar campo version com anotação @Version (Optimistic Locking) para evitar venda dupla.

Relacionamento: @ManyToOne para Product.

2.5. Entidade: Order
Objetivo: O cabeçalho da compra.

Atributos: id, moment (Instant/LocalDateTime), total (BigDecimal), status (Enum: WAITING_PAYMENT, PAID).

Relacionamento: @ManyToOne para User, @OneToMany para OrderItem.

2.6. Entidade: OrderItem
Objetivo: Itens dentro do pedido (tabela pivo).

Atributos: id, quantity (Integer), price (BigDecimal - preço fixo no momento da compra).

Relacionamento: @ManyToOne para Order, @ManyToOne para Product.

3. Utensílios (Camada Repository)
   Pacote: com.seuprojeto.api.repositories Instrução: Interfaces que estendem JpaRepository.

StockItemRepository:

Método Customizado Necessário: findFirstByProductAndSoldFalse(Product product) -> Deve retornar Optional<StockItem>. Isso serve para pegar o próximo item disponível daquele produto para entrega.

4. Preparação dos Dados (DTOs)
   Pacote: com.seuprojeto.api.dto Objetivo: Blindar a API e não expor as entidades diretamente.

ProductDTO (Response): Retorna id, título, preço, imagem e tipo.

OrderDTO (Request): Recebe o ID do usuário (mockado por enquanto) e uma lista de itens.

OrderItemDTO (Request): Contém productId e quantity.

OrderReceiptDTO (Response): Retorna dados do pedido confirmado e, crucialmente, uma lista de Credenciais/Conteúdo (se for entrega automática).

5. Modo de Preparo (Camada Service - Regras de Negócio)
   Pacote: com.seuprojeto.api.services

5.1. Interface: PaymentStrategy
Método: boolean processPayment(BigDecimal amount).

Implementação Inicial: MockPaymentService (sempre retorna true).

5.2. Classe: OrderService
Dependências: Injetar Repositories, PaymentStrategy.

Método Principal: placeOrder(OrderDTO dto)

Passo 1: Recuperar o Usuário.

Passo 2: Iterar sobre os itens do DTO.

Passo 3 (Validação de Estoque):

Se ProductType for SERVICE: Apenas criar o pedido.

Se ProductType for AUTOMATIC: Buscar no StockItemRepository se existem itens disponíveis (sold = false) suficientes para a quantidade pedida. Se não houver, lançar OutOfStockException.

Passo 4: Calcular o total.

Passo 5: Chamar paymentStrategy.processPayment().

Passo 6 (Finalização):

Se pago: Salvar Order e OrderItems.

Crucial: Para cada item vendido do tipo AUTOMATIC, atualizar o StockItem correspondente para sold = true e associar ao pedido.

Retorno: OrderReceiptDTO.

5.3. Classe: ProductService
Método: findAll(Pageable pageable) -> Retorna página de produtos.

Método: findById(Long id) -> Detalhes.

6. Empratamento (Camada Controller)
   Pacote: com.seuprojeto.api.controllers

6.1. ProductController
Endpoint: GET /products

Recebe: Parâmetros de paginação (?page=0&size=10).

Retorna: Page<ProductDTO>.

6.2. OrderController
Endpoint: POST /orders (ou /checkout)

Recebe: OrderDTO (JSON).

Retorna: 201 Created com OrderReceiptDTO no corpo.

Endpoint: GET /orders/my-orders

Objetivo: Histórico onde o usuário pode rever as senhas/contas compradas.

7. Tratamento de Erros (Exceptions)
   Pacote: com.seuprojeto.api.exceptions

Classe: ResourceExceptionHandler (@ControllerAdvice).

Captura:

OutOfStockException -> Retornar 422 Unprocessable Entity ou 409 Conflict.

EntityNotFoundException -> Retornar 404 Not Found.

OptimisticLockingFailureException -> Retornar 409 Conflict (pedir para o usuário tentar de novo).