const API_URL = window.location.origin;

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const btn = document.getElementById('btn-login');
    const errorMsg = document.getElementById('error-msg');
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Entrando...';
    errorMsg.innerText = '';

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) throw new Error('E-mail ou senha incorretos.');

        const data = await response.json();
        localStorage.setItem('token', data.token);

        window.location.href = '/';
    } catch (error) {
        errorMsg.innerText = error.message;
        btn.disabled = false;
        btn.innerText = 'Entrar na conta';
    }
});