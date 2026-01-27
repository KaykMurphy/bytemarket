const API_URL = window.location.origin;

const params = new URLSearchParams(window.location.search);
const productId = params.get('productId');
const qty = parseInt(params.get('qty')) || 1;

let currentProduct = null;
let authToken = localStorage.getItem('token');
let currentUserId = null;
let paymentCheckInterval;

async function initCheckout() {
    await loadCheckoutSummary();

    if (authToken) {
        try {
            const res = await fetch(`${API_URL}/auth/me`, {
                headers: { 'Authorization': `Bearer ${authToken}` }
            });

            if (res.ok) {
                const user = await res.json();
                currentUserId = user.id;
                showLoggedView(user);
            } else {
                localStorage.removeItem('token');
                authToken = null;
                showGuestView();
            }
        } catch (e) {
            console.error("Erro ao validar login:", e);
            showGuestView();
        }
    } else {
        showGuestView();
    }
}

function showLoggedView(user) {
    document.getElementById('guest-section').classList.add('hidden');
    document.getElementById('logged-section').classList.remove('hidden');
    document.getElementById('display-user-name').innerText = user.name;
    document.getElementById('logged-delivery-email').value = user.email;
}

function showGuestView() {
    document.getElementById('guest-section').classList.remove('hidden');
    document.getElementById('logged-section').classList.add('hidden');
}

async function loadCheckoutSummary() {
    if (!productId) {
        alert('Produto inválido.');
        window.location.href = '/';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/products/${productId}`);
        currentProduct = await response.json();

        document.getElementById('summary-title').innerText = currentProduct.title;
        document.getElementById('summary-qty').innerText = `x${qty}`;
        document.getElementById('summary-img').src = currentProduct.imageUrl || 'https://placehold.co/60';

        const total = currentProduct.price * qty;
        const formattedTotal = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(total);

        document.getElementById('summary-subtotal').innerText = formattedTotal;
        document.getElementById('summary-total').innerText = formattedTotal;

    } catch (e) {
        console.error(e);
        alert('Erro ao carregar produto.');
    }
}

async function handleLoggedCheckout(event) {
    event.preventDefault();
    const deliveryEmail = document.getElementById('logged-delivery-email').value;
    await processFinalPurchase(currentUserId, deliveryEmail, authToken);
}

async function handleGuestCheckout(event) {
    event.preventDefault();
    const btn = event.submitter;
    const errorMsg = document.getElementById('error-msg');

    const name = document.getElementById('guest-name').value;
    const email = document.getElementById('guest-email').value;
    const password = document.getElementById('guest-pass').value;

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Criando conta...';
    errorMsg.innerText = '';

    try {
        await registerOrLogin(name, email, password);
        const newAuthToken = localStorage.getItem('token');
        const userRes = await fetch(`${API_URL}/auth/me`, {
            headers: { 'Authorization': `Bearer ${newAuthToken}` }
        });
        const user = await userRes.json();
        authToken = newAuthToken;
        await processFinalPurchase(user.id, email, newAuthToken);
    } catch (error) {
        errorMsg.innerText = error.message || 'Erro ao criar conta.';
        btn.disabled = false;
        btn.innerHTML = 'Criar Conta e Pagar <i class="fa-solid fa-arrow-right"></i>';
    }
}

async function processFinalPurchase(userId, deliveryEmail, token) {
    const activeBtn = document.querySelector('.btn-primary:not(.hidden)');
    if(activeBtn) {
        activeBtn.disabled = true;
        activeBtn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Processando...';
    }

    try {
        const orderId = await createOrder(userId, deliveryEmail, token);
        const pixData = await generatePix(orderId, token);
        showPixScreen(pixData);

        startPaymentPolling(pixData.paymentId, token);

    } catch (error) {
        alert(error.message);
        location.reload();
    }
}

function startPaymentPolling(paymentId, token) {
    if (paymentCheckInterval) clearInterval(paymentCheckInterval);

    console.log(`Iniciando monitoramento do pagamento: ${paymentId}`);

    paymentCheckInterval = setInterval(async () => {
        try {
            const res = await fetch(`${API_URL}/payments/${paymentId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (res.ok) {
                const data = await res.json();
                console.log("Status atual:", data.status);

                if (data.status === 'APPROVED') {
                    clearInterval(paymentCheckInterval);
                    window.location.href = '/success.html';
                }
            }
        } catch (e) {
            console.error("Erro ao verificar status:", e);
        }
    }, 3000); // Tenta a cada 3 segundos
}

async function registerOrLogin(name, email, password) {
    const regRes = await fetch(`${API_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password })
    });

    // Tratar rate limiting no registro
    if (regRes.status === 429) {
        throw new Error('Limite de requisições excedido. Aguarde e tente novamente.');
    }

    if (!regRes.ok && regRes.status !== 409) throw new Error('Erro ao criar conta.');

    const loginRes = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    // Tratar rate limiting no login
    if (loginRes.status === 429) {
        throw new Error('Limite de tentativas de login excedido. Aguarde alguns segundos.');
    }

    if (!loginRes.ok) throw new Error('E-mail já existe ou senha incorreta.');

    const data = await loginRes.json();
    localStorage.setItem('token', data.token);
}

async function createOrder(userId, deliveryEmail, token) {
    const payload = {
        userId: userId,
        deliveryEmail: deliveryEmail,
        items: [{ productId: parseInt(productId), quantity: qty }]
    };

    const res = await fetch(`${API_URL}/orders`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
    });

    // Tratar rate limiting na criação de pedido
    if (res.status === 429) {
        throw new Error('Muitas tentativas de pedido. Aguarde 1 minuto antes de tentar novamente.');
    }

    if (!res.ok) throw new Error('Falha ao criar pedido.');
    const order = await res.json();
    return order.id;
}

async function generatePix(orderId, token) {
    const res = await fetch(`${API_URL}/payments/pix/orders/${orderId}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });

    // Tratar rate limiting na geração de PIX
    if (res.status === 429) {
        throw new Error('Limite de geração de pagamentos atingido. Tente novamente em 1 minuto.');
    }

    if (!res.ok) throw new Error('Falha ao gerar PIX.');
    return await res.json();
}

function showPixScreen(pixData) {
    document.getElementById('auth-box').classList.add('hidden');
    document.getElementById('pix-box').classList.remove('hidden');
    document.querySelector('.steps-indicator .step.active').classList.remove('active');
    document.getElementById('step-payment-icon').classList.add('active');

    const base64Prefix = "data:image/png;base64,";
    const qrSrc = pixData.pixQrCode.startsWith('data:') ? pixData.pixQrCode : base64Prefix + pixData.pixQrCode;

    document.getElementById('pix-image').src = qrSrc;
    document.getElementById('pix-copypaste').value = pixData.pixQrCodeText;
}

window.copyPix = async () => {
    const copyInput = document.getElementById("pix-copypaste");
    const copyGroup = document.querySelector('.copy-input-group');
    const copyBtnText = document.getElementById("copy-btn-text");
    const copyIcon = copyGroup.querySelector('i');

    if (!copyInput.value || copyInput.value === "Aguardando código...") return;

    try {
        await navigator.clipboard.writeText(copyInput.value);
        copyGroup.classList.add('success');
        copyBtnText.innerText = "Copiado!";
        copyIcon.classList.replace('fa-copy', 'fa-check-circle');

        setTimeout(() => {
            copyGroup.classList.remove('success');
            copyBtnText.innerText = "Copiar";
            copyIcon.classList.replace('fa-check-circle', 'fa-copy');
        }, 2500);

    } catch (err) {
        console.error('Erro ao copiar: ', err);
        copyInput.select();
        document.execCommand('copy');
    }
}

initCheckout();