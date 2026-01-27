if (typeof window.API_URL === 'undefined') {
    window.API_URL =
        window.location.origin === 'null'
            ? 'http://localhost:8080'
            : window.location.origin;
}

console.log('product.js carregado!');
console.log('API_URL:', window.API_URL);
console.log('URL atual:', window.location.href);

// Função para pegar ID da URL
const getProductIdFromUrl = () => {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');
    console.log('ID do produto na URL:', id);
    return id;
};

// Função para atualizar quantidade
window.updateQty = (change) => {
    console.log('Atualizando quantidade:', change);
    const input = document.getElementById('qty');
    if (!input) {
        console.error('Input de quantidade não encontrado!');
        return;
    }
    let newValue = parseInt(input.value) + change;
    if (newValue < 1) newValue = 1;
    const product = window.currentProduct;
    if (product && newValue > product.availableStock) {
        newValue = product.availableStock;
        console.log('Quantidade limitada ao estoque:', newValue);
    }
    input.value = newValue;
    console.log('Nova quantidade:', newValue);
};

// FUNÇÃO PRINCIPAL - CARREGAR PRODUTO
async function loadProductDetails() {
    console.log('Iniciando carregamento do produto...');
    const id = getProductIdFromUrl();
    if (!id) {
        console.error('ID do produto não encontrado na URL!');
        alert('Produto não especificado.');
        window.location.href = '/';
        return;
    }

    console.log('ID válido:', id);
    console.log('Fazendo requisição para:', `${window.API_URL}/api/products/${id}`);

    try {
        const response = await fetch(`${window.API_URL}/api/products/${id}`); // ✅ CORRIGIDO: /api/products
        console.log('Resposta recebida!');
        console.log('Status:', response.status);
        console.log('OK:', response.ok);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Produto não encontrado`);
        }

        const product = await response.json();
        console.log('PRODUTO CARREGADO COM SUCESSO!');
        console.log('Dados do produto:', product);

        // ATUALIZAR IMAGEM
        console.log('Atualizando imagem...');
        const img = document.getElementById('product-img');
        if (img) {
            const imageUrl = product.imageUrl && product.imageUrl.startsWith('http')
                ? product.imageUrl
                : 'https://placehold.co/600x400/8257e5/ffffff?text=' + encodeURIComponent(product.title || 'Produto');
            console.log('URL da imagem:', imageUrl);
            img.src = imageUrl;
            img.alt = product.title || 'Produto';
            img.onerror = function() {
                console.warn('Erro ao carregar imagem, usando placeholder');
                this.src = 'https://placehold.co/600x400/8257e5/ffffff?text=Sem+Imagem';
            };
            img.onload = function() {
                console.log('Imagem carregada com sucesso!');
            };
        } else {
            console.error('Elemento #product-img não encontrado!');
        }

        // ATUALIZAR TÍTULO
        console.log('Atualizando título...');
        const titleEl = document.getElementById('product-title');
        if (titleEl) {
            titleEl.textContent = product.title || 'Produto sem título';
            document.title = `${product.title} - ByteMarket`;
            console.log('Título atualizado:', product.title);
        } else {
            console.error('Elemento #product-title não encontrado!');
        }

        // ATUALIZAR PREÇO
        console.log('Atualizando preço...');
        const priceEl = document.getElementById('product-price');
        if (priceEl) {
            const formattedPrice = formatCurrency(product.price);
            priceEl.textContent = formattedPrice.replace('R$', '').trim();
            console.log('Preço atualizado:', formattedPrice);
        } else {
            console.error('Elemento #product-price não encontrado!');
        }

        // ATUALIZAR TIPO/CATEGORIA
        console.log('Atualizando tipo...');
        const typeEl = document.getElementById('product-type');
        if (typeEl) {
            typeEl.textContent = product.type || 'PRODUTO';
            console.log('Tipo atualizado:', product.type);
        } else {
            console.error('Elemento #product-type não encontrado!');
        }

        // ATUALIZAR DESCRIÇÃO
        console.log('Atualizando descrição...');
        const descEl = document.getElementById('product-desc');
        if (descEl) {
            if (product.description && product.description.trim() !== '') {
                descEl.innerHTML = product.description;
                console.log('Descrição atualizada!');
            } else {
                descEl.innerHTML = '<p style="color: var(--text-secondary);">Nenhuma descrição disponível para este produto.</p>';
                console.log('Produto sem descrição');
            }
        } else {
            console.error('Elemento #product-desc não encontrado!');
        }

        // ATUALIZAR ESTOQUE E BOTÕES
        console.log('Atualizando status de estoque...');
        console.log('Estoque disponível:', product.availableStock);
        const stockEl = document.getElementById('stock-status');
        const btnBuy = document.getElementById('btn-buy');
        const btnAddCart = document.getElementById('btn-add-cart');
        const qtyInput = document.getElementById('qty');

        if (product.availableStock > 0) {
            console.log('Produto EM ESTOQUE');
            // Atualizar badge de estoque
            if (stockEl) {
                stockEl.innerHTML = `<i class="fa-solid fa-check-circle"></i> Disponível (${product.availableStock} un)`;
                stockEl.className = 'stock-status in-stock';
            }
            // Habilitar input de quantidade
            if (qtyInput) {
                qtyInput.max = product.availableStock;
                qtyInput.disabled = false;
                qtyInput.value = 1;
            }
            // Habilitar botão COMPRAR
            if (btnBuy) {
                btnBuy.disabled = false;
                btnBuy.style.opacity = '1';
                btnBuy.style.cursor = 'pointer';
                btnBuy.innerHTML = '<i class="fa-solid fa-bolt"></i> Comprar Agora';
            }
            // Habilitar botão ADICIONAR AO CARRINHO
            if (btnAddCart) {
                btnAddCart.disabled = false;
                btnAddCart.style.opacity = '1';
                btnAddCart.style.cursor = 'pointer';
            }
        } else {
            console.log('Produto ESGOTADO');
            // Atualizar badge de estoque
            if (stockEl) {
                stockEl.innerHTML = `<i class="fa-solid fa-times-circle"></i> Esgotado`;
                stockEl.className = 'stock-status out-stock';
            }
            // Desabilitar botões
            if (btnBuy) {
                btnBuy.disabled = true;
                btnBuy.style.opacity = '0.5';
                btnBuy.style.cursor = 'not-allowed';
                btnBuy.innerHTML = '<i class="fa-solid fa-ban"></i> Indisponível';
            }
            if (btnAddCart) {
                btnAddCart.disabled = true;
                btnAddCart.style.opacity = '0.5';
                btnAddCart.style.cursor = 'not-allowed';
            }
            if (qtyInput) {
                qtyInput.disabled = true;
            }
        }

        // Salvar produto globalmente
        window.currentProduct = product;
        console.log('Produto salvo em window.currentProduct');
        console.log('PRODUTO CARREGADO COM SUCESSO!');

    } catch (error) {
        console.error('ERRO AO CARREGAR PRODUTO!');
        console.error('Erro completo:', error);
        console.error('Mensagem:', error.message);
        console.error('Stack:', error.stack);

        // Mostrar erro na página
        const container = document.querySelector('.product-container');
        if (container) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px;">
                    <i class="fa-solid fa-triangle-exclamation" style="font-size: 4rem; color: var(--red); margin-bottom: 20px;"></i>
                    <h2 style="color: var(--text-primary); margin-bottom: 10px;">Erro ao carregar produto</h2>
                    <p style="color: var(--text-secondary); margin-bottom: 20px;">
                        ${error.message || 'O produto não foi encontrado ou ocorreu um erro.'}
                    </p>
                    <a href="/" class="btn-primary" style="display: inline-block; padding: 12px 24px; text-decoration: none;">
                        <i class="fa-solid fa-arrow-left"></i> Voltar para a loja
                    </a>
                </div>
            `;
        }
    }
}

// FUNÇÃO: COMPRAR PRODUTO
window.buyProduct = () => {
    console.log('Botão COMPRAR clicado!');
    const product = window.currentProduct;
    if (!product) {
        console.error('Produto não carregado!');
        alert('Erro: produto não carregado. Tente recarregar a página.');
        return;
    }

    const qtyInput = document.getElementById('qty');
    const qty = qtyInput ? qtyInput.value : 1;
    console.log('Quantidade:', qty);
    console.log('Redirecionando para checkout...');
    window.location.href = `/checkout.html?productId=${product.id}&qty=${qty}`;
};

// FUNÇÃO: ADICIONAR AO CARRINHO
window.addProductToCart = () => {
    console.log('Botão ADICIONAR AO CARRINHO clicado!');
    const product = window.currentProduct;
    if (!product) {
        console.error('Produto não carregado!');
        alert('Erro: produto não carregado. Tente recarregar a página.');
        return;
    }

    const qtyInput = document.getElementById('qty');
    const qty = qtyInput ? parseInt(qtyInput.value) : 1;
    console.log('Adicionando ao carrinho:', {
        produto: product.title,
        quantidade: qty,
        preço: product.price
    });

    // Tentar usar função do main.js
    if (typeof window.addToCart === 'function') {
        console.log('Usando função addToCart do main.js');
        window.addToCart(product, qty);
        return;
    }

    console.log('Função addToCart não encontrada, usando fallback');
    // Fallback: adicionar manualmente
    let cart = JSON.parse(localStorage.getItem('cart')) || [];
    console.log('Carrinho atual:', cart);

    const existingItem = cart.find(item => item.id === product.id);
    if (existingItem) {
        existingItem.quantity += qty;
        console.log('Item já existia, quantidade atualizada');
    } else {
        cart.push({
            id: product.id,
            title: product.title,
            price: product.price,
            imageUrl: product.imageUrl,
            quantity: qty
        });
        console.log('Novo item adicionado ao carrinho');
    }

    localStorage.setItem('cart', JSON.stringify(cart));
    console.log('Carrinho salvo no localStorage');

    // Atualizar contador
    const cartCounts = document.querySelectorAll('.cart-count');
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCounts.forEach(el => {
        el.textContent = totalItems;
        console.log('Contador atualizado:', totalItems);
    });

    // Mostrar notificação
    const message = qty > 1
        ? `${qty} produtos adicionados ao carrinho!`
        : 'Produto adicionado ao carrinho!';
    showSimpleNotification(message);
};

// FUNÇÃO: MOSTRAR NOTIFICAÇÃO
function showSimpleNotification(message) {
    console.log('Mostrando notificação:', message);
    // Remover notificação anterior
    const oldNotification = document.querySelector('.simple-notification');
    if (oldNotification) {
        oldNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = 'simple-notification';
    notification.innerHTML = `
        <i class="fa-solid fa-check-circle"></i>
        <span>${message}</span>
    `;
    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        background: var(--surface);
        border: 1px solid var(--border);
        border-left: 4px solid var(--green);
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 12px;
        animation: slideInRight 0.3s ease;
        max-width: 300px;
    `;
    document.body.appendChild(notification);
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 2500);
}

// ESTILOS DE ANIMAÇÃO
if (!document.getElementById('notification-styles')) {
    const style = document.createElement('style');
    style.id = 'notification-styles';
    style.textContent = `
        @keyframes slideInRight {
            from { transform: translateX(400px); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOutRight {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(400px); opacity: 0; }
        }
        .simple-notification i {
            color: var(--green);
            font-size: 1.2rem;
        }
    `;
    document.head.appendChild(style);
}


// INICIALIZAÇÃO
console.log('Aguardando DOM carregar...');
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        console.log('DOM carregado!');
        loadProductDetails();
    });
} else {
    console.log('DOM já estava carregado!');
    loadProductDetails();
}

console.log('product.js configurado e pronto!');