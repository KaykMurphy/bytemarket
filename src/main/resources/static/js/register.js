const API_URL = window.location.origin;

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const btn = document.getElementById('btn-register');
    const errorMsg = document.getElementById('error-msg');
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Criando conta...';
    errorMsg.innerText = '';

    try {
        const regRes = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password })
        });

        if (!regRes.ok) {
            if (regRes.status === 409) throw new Error('Este e-mail já está em uso.');
            throw new Error('Erro ao criar conta. Tente novamente.');
        }

        // Login Automático após registro
        const loginRes = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await loginRes.json();
        localStorage.setItem('token', data.token);

        window.location.href = '/';
    } catch (error) {
        errorMsg.innerText = error.message;
        btn.disabled = false;
        btn.innerText = 'Finalizar cadastro';
    }
});