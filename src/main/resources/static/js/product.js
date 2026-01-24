const API_URL = window.location.origin;

const getProductIdFromUrl = () => {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
};

const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(value);
};

window.updateQty = (change) => {
    const input = document.getElementById('qty');
    let newValue = parseInt(input.value) + change;
    if (newValue < 1) newValue = 1;
    input.value = newValue;
};

async function loadProductDetails() {
    const id = getProductIdFromUrl();

    if (!id) {
        alert('Produto não especificado.');
        window.location.href = '/';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/products/${id}`);

        if (!response.ok) throw new Error('Produto não encontrado');

        const product = await response.json();

        const img = document.getElementById('product-img');
        img.src = product.imageUrl && product.imageUrl.startsWith('http')
            ? product.imageUrl
            : 'https://placehold.co/600x400/202024/8257e5?text=Sem+Imagem';

        // Preencher Textos
        document.getElementById('product-title').innerText = product.title;
        document.getElementById('product-price').innerText = formatCurrency(product.price).replace('R$', '').trim();
        document.getElementById('product-type').innerText = product.type;
        document.getElementById('product-desc').innerHTML = product.description;

        // Lógica de Estoque
        const stockEl = document.getElementById('stock-status');
        const btnBuy = document.getElementById('btn-buy');

        if (product.availableStock > 0) {
            stockEl.innerHTML = `<i class="fa-solid fa-check-circle"></i> Disponível (${product.availableStock} un)`;
            stockEl.className = 'stock-status in-stock';
        } else {
            stockEl.innerHTML = `<i class="fa-solid fa-times-circle"></i> Esgotado`;
            stockEl.className = 'stock-status out-stock';
            btnBuy.disabled = true;
            btnBuy.style.opacity = '0.5';
            btnBuy.innerHTML = 'Indisponível';
        }

        window.currentProduct = product;

    } catch (error) {
        console.error(error);
        document.querySelector('.product-container').innerHTML =
            '<h2 style="text-align:center; color: var(--red)">Erro ao carregar produto.</h2>';
    }
}

window.buyProduct = () => {
    const qty = document.getElementById('qty').value;
    const id = getProductIdFromUrl();
    window.location.href = `/checkout.html?productId=${id}&qty=${qty}`;
};

document.addEventListener('DOMContentLoaded', loadProductDetails);