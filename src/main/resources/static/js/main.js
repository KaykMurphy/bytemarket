const API_URL = window.location.origin;

const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(value);
};

const createProductCard = (product) => {
    const imageSrc = product.imageUrl && product.imageUrl.startsWith('http')
        ? product.imageUrl
        : 'https://placehold.co/600x400/202024/8257e5?text=Sem+Imagem';

    const hasStock = product.availableStock > 0;
    const stockHtml = hasStock
        ? `<div class="stock-badge"><i class="fa-solid fa-circle" style="color: var(--green)"></i> ${product.availableStock} un</div>`
        : `<div class="stock-badge out"><i class="fa-solid fa-circle"></i> Esgotado</div>`;

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
        let url = `${API_URL}/products`;

        if (searchTerm && searchTerm.trim() !== '') {
            url = `${API_URL}/products/search?q=${encodeURIComponent(searchTerm)}`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error('Erro ao conectar com a API');

        const data = await response.json();
        const products = searchTerm ? data : data.content; // Ajuste para endpoint de busca

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
                <p>Erro ao carregar produtos. Verifique se a API está rodando na porta 8080.</p>
            </div>
        `;
    }
}

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
        const response = await fetch(`${API_URL}/auth/me`, {
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
            // Token inválido ou expirado
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



function setupSearchBar() {
    const searchInput = document.querySelector('.search-bar input');
    const searchButton = document.querySelector('.search-bar button');

    if (!searchInput || !searchButton) return;

    let searchTimeout;

    const performSearch = async () => {
        const searchTerm = searchInput.value.trim();

        const container = document.getElementById('products-grid');
        if (container) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 40px;">
                    <i class="fa-solid fa-circle-notch fa-spin" style="font-size: 2rem; color: var(--primary);"></i>
                    <p>Buscando produtos...</p>
                </div>
            `;
        }

        await fetchProducts(searchTerm);
    };

    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(performSearch, 500);
    });

    searchButton.addEventListener('click', performSearch);

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            clearTimeout(searchTimeout);
            performSearch();
        }
    });

    const clearButton = document.querySelector('.search-bar .fa-times');
    if (clearButton) {
        clearButton.addEventListener('click', () => {
            searchInput.value = '';
            performSearch();
        });
    }
}


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

    // Mostrar dropdown
    const showDropdown = () => {
        clearTimeout(dropdownTimeout);
        dropdown.classList.add('visible');
    };

    const hideDropdown = () => {
        dropdownTimeout = setTimeout(() => {
            if (!isDropdownHovered && !isProfileHovered) {
                dropdown.classList.remove('visible');
            }
        }, 300); // 300ms de delay
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

    dropdown.querySelectorAll('a, button').forEach(item => {
        item.addEventListener('click', () => {
            setTimeout(() => {
                dropdown.classList.remove('visible');
                isDropdownHovered = false;
                isProfileHovered = false;
            }, 100);
        });
    });
}

function setupMutationObserver() {
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === 'childList') {
                const userProfile = document.querySelector('.user-profile');
                const dropdown = document.querySelector('.user-dropdown');

                if (userProfile && dropdown && !dropdown.classList.contains('dropdown-initialized')) {
                    dropdown.classList.add('dropdown-initialized');
                    setupDropdownBehavior();
                }
            }
        });
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}


document.addEventListener('DOMContentLoaded', () => {
    fetchProducts();
    checkAuth();
    setTimeout(() => {
        setupDropdownBehavior();
        setupMutationObserver();
    }, 1000);

    setupSearchBar();

    document.body.classList.add('js-loaded');
});


window.showLoading = (show) => {
    const loadingEl = document.getElementById('loading-spinner');
    if (loadingEl) {
        loadingEl.style.display = show ? 'block' : 'none';
    }
};

// Função para exibir mensagens de erro
window.showError = (message, elementId = 'error-message') => {
    const errorEl = document.getElementById(elementId);
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.classList.remove('hidden');
        setTimeout(() => {
            errorEl.classList.add('hidden');
        }, 5000);
    } else {
        console.error(message);
    }
};

// Função para validar email
window.validateEmail = (email) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
};

// Função para formatar data
window.formatDate = (dateString) => {
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString('pt-BR', options);
};