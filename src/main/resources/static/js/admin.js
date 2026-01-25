const API_URL = window.location.origin;
const token = localStorage.getItem('token');

if (!token) window.location.href = '/login.html';

let currentProductId = null;
let confirmAction = null;

function openProductModal(productId = null) {
    const modal = document.getElementById('product-modal');
    const title = document.getElementById('modal-title');
    const form = document.getElementById('product-form');
    const submitBtn = document.getElementById('submit-btn');

    if (productId) {
        title.textContent = 'Editar Produto';
        submitBtn.textContent = 'Atualizar Produto';
        loadProductForEdit(productId);
    } else {
        title.textContent = 'Novo Produto';
        submitBtn.textContent = 'Criar Produto';
        form.reset();
        document.getElementById('product-id').value = '';
    }

    modal.classList.remove('hidden');
}

function closeProductModal() {
    document.getElementById('product-modal').classList.add('hidden');
    document.getElementById('product-form').reset();
}

function openStockModal(productId, productName) {
    const modal = document.getElementById('stock-modal');
    const title = document.getElementById('stock-modal-title');

    document.getElementById('stock-product-id').value = productId;
    document.getElementById('stock-product-name').textContent = productName;
    title.textContent = `Estoque: ${productName}`;

    loadStockStatus(productId);
    modal.classList.remove('hidden');
}

function closeStockModal() {
    document.getElementById('stock-modal').classList.add('hidden');
    document.getElementById('stock-items').value = '';
}

function showConfirmModal(title, message, action) {
    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-message').textContent = message;
    document.getElementById('confirm-modal').classList.remove('hidden');
    confirmAction = action;
}

function closeConfirmModal() {
    document.getElementById('confirm-modal').classList.add('hidden');
    confirmAction = null;
}

// Carregar produtos
async function fetchAdminProducts(searchTerm = '') {
    try {
        let url = `${API_URL}/products?size=100`;
        if (searchTerm) {
            url = `${API_URL}/products/search?q=${encodeURIComponent(searchTerm)}`;
        }

        const res = await fetch(url);
        const data = await res.json();
        const tbody = document.getElementById('admin-product-list');

        if (data.content && data.content.length > 0) {
            tbody.innerHTML = data.content.map(p => `
                <tr data-id="${p.id}">
                    <td>
                        <div class="table-prod-info">
                            <img src="${p.imageUrl || 'https://placehold.co/40'}" width="40" height="40">
                            <span>${p.title}</span>
                        </div>
                    </td>
                    <td>R$ ${p.price.toFixed(2)}</td>
                    <td><span class="stock-count">${p.availableStock || 0}</span></td>
                    <td><span class="type-tag">${p.type || 'N/A'}</span></td>
                    <td>
                        <button class="btn-stock" onclick="openStockModal(${p.id}, '${p.title.replace(/'/g, "\\'")}')" 
                                title="Gerenciar Estoque">
                            <i class="fa-solid fa-boxes-stacked"></i>
                        </button>
                        <button class="btn-edit" onclick="openProductModal(${p.id})" title="Editar">
                            <i class="fa-solid fa-pen"></i>
                        </button>
                        <button class="btn-delete" onclick="deleteProduct(${p.id})" title="Excluir">
                            <i class="fa-solid fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 40px;">
                        <i class="fa-solid fa-box-open" style="font-size: 3rem; color: var(--text-secondary); margin-bottom: 15px;"></i>
                        <p>Nenhum produto encontrado</p>
                    </td>
                </tr>
            `;
        }
    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        document.getElementById('admin-product-list').innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; color: var(--red);">
                    Erro ao carregar produtos
                </td>
            </tr>
        `;
    }
}

function searchProducts() {
    const searchTerm = document.getElementById('search-products').value;
    fetchAdminProducts(searchTerm);
}

async function loadProductForEdit(productId) {
    try {
        const res = await fetch(`${API_URL}/products/${productId}`);
        const product = await res.json();

        document.getElementById('product-id').value = product.id;
        document.getElementById('prod-title').value = product.title;
        document.getElementById('prod-desc').value = product.description;
        document.getElementById('prod-price').value = product.price;
        document.getElementById('prod-image').value = product.imageUrl;
        document.getElementById('prod-type').value = product.type;

        if (product.status) {
            document.getElementById('prod-status').value = product.status;
        }
    } catch (error) {
        console.error('Erro ao carregar produto:', error);
        alert('Erro ao carregar produto para edição');
    }
}

async function loadStockStatus(productId) {
    try {
        const res = await fetch(`${API_URL}/admin/products/${productId}/stock/status`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (res.ok) {
            const status = await res.json();
            document.getElementById('stock-available').textContent = status.availableStock;
            document.getElementById('stock-sold').textContent = status.soldStock;
            document.getElementById('stock-total').textContent = status.totalStock;
        }
    } catch (error) {
        console.error('Erro ao carregar status do estoque:', error);
    }
}

async function addStockItems() {
    const productId = document.getElementById('stock-product-id').value;
    const itemsText = document.getElementById('stock-items').value.trim();

    if (!itemsText) {
        alert('Por favor, insira pelo menos um item no formato email:senha');
        return;
    }

    const items = itemsText.split('\n')
        .map(line => line.trim())
        .filter(line => line.length > 0)
        .map(content => ({ content }));

    try {
        const res = await fetch(`${API_URL}/admin/products/${productId}/stock`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(items)
        });

        if (res.ok) {
            alert(`${items.length} itens adicionados ao estoque com sucesso!`);
            document.getElementById('stock-items').value = '';
            loadStockStatus(productId);
            fetchAdminProducts(); // Atualiza a lista de produtos
        } else {
            const error = await res.text();
            alert(`Erro ao adicionar itens: ${error}`);
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao adicionar itens ao estoque');
    }
}

async function deleteProduct(productId) {
    showConfirmModal(
        'Confirmar Exclusão',
        'Tem certeza que deseja excluir este produto? Esta ação não pode ser desfeita.',
        async () => {
            try {
                const res = await fetch(`${API_URL}/admin/products/${productId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (res.ok) {
                    alert('Produto excluído com sucesso!');
                    fetchAdminProducts();
                } else {
                    const error = await res.text();
                    alert(`Erro ao excluir produto: ${error}`);
                }
            } catch (error) {
                console.error('Erro:', error);
                alert('Erro ao excluir produto');
            }
            closeConfirmModal();
        }
    );
}

document.getElementById('product-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const productId = document.getElementById('product-id').value;
    const isEdit = !!productId;

    const payload = {
        title: document.getElementById('prod-title').value,
        description: document.getElementById('prod-desc').value,
        price: parseFloat(document.getElementById('prod-price').value),
        imageUrl: document.getElementById('prod-image').value,
        type: document.getElementById('prod-type').value,

        active: document.getElementById('prod-status') ? document.getElementById('prod-status').value === 'true' : true
    };

    if (isEdit) {
        Object.keys(payload).forEach(key => {
            if (!payload[key] && payload[key] !== 0) {
                delete payload[key];
            }
        });
    }

    try {
        const url = isEdit
            ? `${API_URL}/admin/products/${productId}`
            : `${API_URL}/admin/products`;

        const method = isEdit ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const result = await res.json();
            alert(isEdit ? 'Produto atualizado com sucesso!' : 'Produto criado com sucesso!');
            closeProductModal();
            fetchAdminProducts();
        } else {
            const error = await res.text();
            alert(`Erro ao salvar produto: ${error}`);
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao salvar produto');
    }
});

document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.admin-content').forEach(c => c.classList.add('hidden'));

        btn.classList.add('active');
        const tabId = btn.getAttribute('data-tab');
        document.getElementById(`${tabId}-section`).classList.remove('hidden');

        if (tabId === 'stock') {
            loadStockContent();
        } else if (tabId === 'orders') {
            loadOrdersContent();
        }
    });
});

async function loadStockContent() {
    const content = document.getElementById('stock-content');
    content.innerHTML = `
        <div class="loading-spinner">
            <i class="fa-solid fa-circle-notch fa-spin"></i> Carregando estoque...
        </div>
    `;

    setTimeout(() => {
        content.innerHTML = `
            <div class="card">
                <h3>Visão Geral do Estoque</h3>
                <p>Utilize os botões na tabela de produtos para gerenciar o estoque individualmente.</p>
                <p>Para adicionar itens em massa, utilize o botão "Adicionar em Massa".</p>
            </div>
        `;
    }, 500);
}

async function loadOrdersContent() {
    const content = document.getElementById('orders-content');
    content.innerHTML = `
        <div class="loading-spinner">
            <i class="fa-solid fa-circle-notch fa-spin"></i> Carregando pedidos...
        </div>
    `;

    setTimeout(() => {
        content.innerHTML = `
            <div class="card">
                <h3>Histórico de Pedidos</h3>
                <p>Funcionalidade em desenvolvimento.</p>
                <p>Em breve você poderá visualizar e gerenciar todos os pedidos aqui.</p>
            </div>
        `;
    }, 500);
}

document.getElementById('confirm-action-btn').addEventListener('click', () => {
    if (confirmAction) {
        confirmAction();
    }
});

async function checkAdminRole() {
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/auth/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const user = await response.json();

            if (user.role !== 'ADMIN') {
                alert('Acesso restrito a administradores!');
                window.location.href = '/';
                return;
            }

            console.log('Usuário admin autenticado:', user.name);
        } else {
            localStorage.removeItem('token');
            window.location.href = '/login.html';
        }
    } catch (error) {
        console.error("Erro na verificação de admin:", error);
        window.location.href = '/login.html';
    }
}

window.logout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
};

// Inicialização
document.addEventListener('DOMContentLoaded', async () => {
    await checkAdminRole();

    await fetchAdminProducts();

    document.getElementById('search-products').addEventListener('input', (e) => {
        searchProducts();
    });
});