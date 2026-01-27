// ===== CONFIGURAÇÃO DA API =====
window.API_URL =
    window.location.origin === 'null'
        ? 'http://localhost:8080'
        : window.location.origin;

let cart = JSON.parse(localStorage.getItem('cart')) || [];
const currentTheme = localStorage.getItem('theme') || 'dark';

// Aplicar tema inicial
if (currentTheme === 'light') {
    document.documentElement.setAttribute('data-theme', 'light');
}

// Toggle Theme
function toggleTheme() {
    const html = document.documentElement;
    const currentTheme = html.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    html.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const icon = document.querySelector('#theme-toggle i');
    const text = document.querySelector('#theme-toggle span');
    if (icon) {
        if (theme === 'light') {
            icon.className = 'fa-solid fa-moon';
            if (text) text.textContent = 'Escuro';
        } else {
            icon.className = 'fa-solid fa-sun';
            if (text) text.textContent = 'Claro';
        }
    }
}

// ===== DISCORD LINK =====
async function loadDiscordLink() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;
        const response = await fetch(`${window.API_URL}/admin/settings/discord`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const discordLink = await response.text();
            const discordBtn = document.getElementById('discord-btn');
            if (discordBtn && discordLink) {
                discordBtn.href = discordLink;
                discordBtn.style.display = 'flex';
            }
        }
    } catch (error) {
        console.log('Discord link não configurado');
    }
}

// ===== LIVE SEARCH =====
let searchTimeout;
const searchInput = document.getElementById('search-input');
const searchDropdown = document.querySelector('.search-dropdown');

if (searchInput && searchDropdown) {
    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        const query = searchInput.value.trim();
        if (query.length < 2) {
            searchDropdown.classList.remove('active');
            return;
        }
        searchTimeout = setTimeout(() => performLiveSearch(query), 300);
    });

    // Fechar dropdown ao clicar fora
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.search-bar')) {
            searchDropdown.classList.remove('active');
        }
    });
}

async function performLiveSearch(query) {
    try {
        const response = await fetch(`${window.API_URL}/api/products/search?q=${encodeURIComponent(query)}`);
        const products = await response.json();
        displaySearchResults(products);
    } catch (error) {
        console.error('Erro na busca:', error);
    }
}

function displaySearchResults(products) {
    const searchDropdown = document.querySelector('.search-dropdown');
    if (!products || products.length === 0) {
        searchDropdown.innerHTML = `
            <div class="search-no-results">
                <i class="fa-solid fa-search" style="font-size: 2rem; color: var(--text-secondary); margin-bottom: 10px;"></i>
                <p>Nenhum produto encontrado</p>
            </div>
        `;
        searchDropdown.classList.add('active');
        return;
    }

    searchDropdown.innerHTML = products.slice(0, 5).map(product => `
        <div class="search-result-item" onclick="window.location.href='/product.html?id=${product.id}'">
            <img src="${product.imageUrl || 'https://placehold.co/50'}" 
                 alt="${product.title}" 
                 class="search-result-img"
                 onerror="this.src='https://placehold.co/50'">
            <div class="search-result-info">
                <div class="search-result-title">${product.title}</div>
                <div class="search-result-price">${formatCurrency(product.price)}</div>
            </div>
        </div>
    `).join('');
    searchDropdown.classList.add('active');
}

// ===== CARRINHO DE COMPRAS =====
function updateCartUI() {
    const cartCount = document.querySelector('.cart-count');
    const cartItems = document.querySelector('.cart-items');
    const cartTotal = document.querySelector('.cart-total span:last-child');
    const cartEmpty = document.querySelector('.cart-empty');
    const cartFooter = document.querySelector('.cart-footer');

    if (cartCount) {
        cartCount.textContent = cart.reduce((sum, item) => sum + item.quantity, 0);
    }

    if (cartItems) {
        if (cart.length === 0) {
            if (cartEmpty) cartEmpty.style.display = 'block';
            if (cartFooter) cartFooter.style.display = 'none';
            cartItems.innerHTML = '';
        } else {
            if (cartEmpty) cartEmpty.style.display = 'none';
            if (cartFooter) cartFooter.style.display = 'block';
            cartItems.innerHTML = cart.map(item => `
                <div class="cart-item">
                    <img src="${item.imageUrl}" alt="${item.title}" class="cart-item-img">
                    <div class="cart-item-info">
                        <div class="cart-item-title">${item.title}</div>
                        <div class="cart-item-price">${formatCurrency(item.price)} x ${item.quantity}</div>
                    </div>
                    <button class="cart-item-remove" onclick="removeFromCart(${item.id})">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </div>
            `).join('');
            const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            if (cartTotal) {
                cartTotal.textContent = formatCurrency(total);
            }
        }
    }
}

function addToCart(product, quantity = 1) {
    const existingItem = cart.find(item => item.id === product.id);
    if (existingItem) {
        existingItem.quantity += quantity;
    } else {
        cart.push({
            id: product.id,
            title: product.title,
            price: product.price,
            imageUrl: product.imageUrl,
            quantity: quantity
        });
    }
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartUI();
    showNotification('Produto adicionado ao carrinho!', 'success');
}

function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartUI();
    showNotification('Produto removido do carrinho', 'info');
}

function clearCart() {
    cart = [];
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartUI();
}

function goToCheckout() {
    if (cart.length === 0) {
        showNotification('Seu carrinho está vazio!', 'warning');
        return;
    }
    const firstItem = cart[0];
    window.location.href = `/checkout.html?productId=${firstItem.id}&qty=${firstItem.quantity}`;
}

// Toggle Cart Dropdown
const cartBtn = document.querySelector('.cart-btn');
const cartDropdown = document.querySelector('.cart-dropdown');
if (cartBtn && cartDropdown) {
    cartBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        cartDropdown.classList.toggle('active');
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.cart-btn') && !e.target.closest('.cart-dropdown')) {
            cartDropdown.classList.remove('active');
        }
    });
}

// ===== FUNÇÕES AUXILIARES =====
const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(value);
};

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fa-solid fa-${type === 'success' ? 'check-circle' : type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        background: var(--surface);
        border: 1px solid var(--border);
        border-left: 4px solid ${type === 'success' ? 'var(--green)' : type === 'warning' ? '#fbbf24' : 'var(--primary)'};
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
    }, 3000);
}

const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { transform: translateX(400px); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(400px); opacity: 0; }
    }
`;
document.head.appendChild(style);

// ===== PRODUTOS =====
const createProductCard = (product) => {
    const imageSrc = product.imageUrl && product.imageUrl.startsWith('http')
        ? product.imageUrl
        : 'https://placehold.co/600x400/202024/8257e5?text=Sem+Imagem';
    const hasStock = product.availableStock > 0;
    const stockHtml = hasStock
        ? `<div class="stock-badge"><i class="fa-solid fa-circle" style="color: var(--green)"></i> <span>${product.availableStock} un</span></div>`
        : `<div class="stock-badge out"><i class="fa-solid fa-circle"></i> <span>Esgotado</span></div>`;

    return `
        <div class="product-card" onclick="window.location.href='/product.html?id=${product.id}'">
            <img src="${imageSrc}" alt="${product.title}" class="card-img" onerror="this.src='https://placehold.co/600x400/202024/8257e5?text=Erro+Imagem'">
            <div class="card-body">
                <span class="card-tag">${product.type}</span>
                <h3 class="card-title">${product.title}</h3>
                <div class="card-footer">
                    <span class="price">${formatCurrency(product.price)}</span>
                    ${stockHtml}
                </div>
            </div>
        </div>
    `;
};

async function fetchProducts(searchTerm = '') {
    const container = document.getElementById('products-grid');
    if (!container) return;

    try {
        let url = `${window.API_URL}/api/products`;
        if (searchTerm && searchTerm.trim() !== '') {
            url = `${window.API_URL}/api/products/search?q=${encodeURIComponent(searchTerm)}`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error('Erro ao conectar com a API');

        const data = await response.json();
        const products = searchTerm ? data : data.content;

        container.innerHTML = '';

        if (!products || products.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 40px;">
                    <i class="fa-solid fa-search" style="font-size: 3rem; color: var(--text-secondary); margin-bottom: 15px;"></i>
                    <h3>${searchTerm ? 'Nenhum produto encontrado' : 'Nenhum produto disponível'}</h3>
                    ${searchTerm ? `<p>Tente outro termo de busca.</p>` : ''}
                </div>
            `;
            return;
        }

        products.forEach(product => {
            container.innerHTML += createProductCard(product);
        });

    } catch (error) {
        console.error('Erro:', error);
        container.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; color: var(--red);">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <p>Erro ao carregar produtos. Verifique se a API está rodando.</p>
            </div>
        `;
    }
}

// ===== AUTENTICAÇÃO =====
async function checkAuth() {
    const token = localStorage.getItem('token');
    const guestActions = document.getElementById('guest-actions');
    const userActions = document.getElementById('user-actions');
    const nameDisplay = document.getElementById('user-name-display');
    const adminLink = document.getElementById('admin-link');

    if (!token) {
        if (guestActions) guestActions.classList.remove('hidden');
        if (userActions) userActions.classList.add('hidden');
        return;
    }

    try {
        const response = await fetch(`${window.API_URL}/auth/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const user = await response.json();
            if (guestActions) guestActions.classList.add('hidden');
            if (userActions) userActions.classList.remove('hidden');
            if (nameDisplay) {
                nameDisplay.innerText = user.name.split(' ')[0];
            }
            if (user.role === 'ADMIN' && adminLink) {
                adminLink.classList.remove('hidden');
            }
        } else {
            localStorage.removeItem('token');
            if (guestActions) guestActions.classList.remove('hidden');
            if (userActions) userActions.classList.add('hidden');
        }
    } catch (error) {
        console.error("Erro na verificação de autenticação:", error);
    }
}

window.logout = () => {
    localStorage.removeItem('token');
    window.location.reload();
};

// ===== DROPDOWN DO USUÁRIO =====
function setupDropdownBehavior() {
    const userProfile = document.querySelector('.user-profile');
    const dropdown = document.querySelector('.user-dropdown');

    if (!userProfile || !dropdown) {
        setTimeout(setupDropdownBehavior, 500);
        return;
    }

    let dropdownTimeout;
    let isDropdownHovered = false;
    let isProfileHovered = false;

    const showDropdown = () => {
        clearTimeout(dropdownTimeout);
        dropdown.classList.add('visible');
    };

    const hideDropdown = () => {
        dropdownTimeout = setTimeout(() => {
            if (!isDropdownHovered && !isProfileHovered) {
                dropdown.classList.remove('visible');
            }
        }, 300);
    };

    userProfile.addEventListener('mouseenter', () => {
        isProfileHovered = true;
        showDropdown();
    });

    userProfile.addEventListener('mouseleave', () => {
        isProfileHovered = false;
        hideDropdown();
    });

    dropdown.addEventListener('mouseenter', () => {
        isDropdownHovered = true;
        clearTimeout(dropdownTimeout);
        dropdown.classList.add('visible');
    });

    dropdown.addEventListener('mouseleave', () => {
        isDropdownHovered = false;
        hideDropdown();
    });

    document.addEventListener('click', (e) => {
        if (!userProfile.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('visible');
            isDropdownHovered = false;
            isProfileHovered = false;
        }
    });
}

// ===== INICIALIZAÇÃO =====
document.addEventListener('DOMContentLoaded', () => {
    fetchProducts();
    checkAuth();
    updateCartUI();
    loadDiscordLink();
    updateThemeIcon(currentTheme);
    setTimeout(() => {
        setupDropdownBehavior();
    }, 500);
    document.body.classList.add('js-loaded');
});

window.addToCart = addToCart;
window.removeFromCart = removeFromCart;
window.goToCheckout = goToCheckout;
window.toggleTheme = toggleTheme;