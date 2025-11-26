document.addEventListener('DOMContentLoaded', (event) => {
    const password_input = document.getElementById('password');
    const submit_button = document.getElementById('registerButton');
    const password_error_div = document.getElementById('passwordError');
    const confirm_input = document.getElementById('confirmPassword');

    if (password_input && submit_button && password_error_div) {
        submit_button.disabled = true;

        const min_length = 5;

        function validatePasswords() {
            const password = password_input.value;
            const confirm = confirm_input ? confirm_input.value : '';

            const lengthOk = password.length >= min_length;
            const matchOk = confirm_input ? (password === confirm) : true;

            if (!lengthOk) {
                password_error_div.textContent = `La contraseña debe tener al menos ${min_length} caracteres.`;
            } else if (confirm_input && !matchOk) {
                password_error_div.textContent = 'Las contraseñas no coinciden.';
            } else {
                password_error_div.textContent = '';
            }

            submit_button.disabled = !(lengthOk && matchOk);
        }

        password_input.addEventListener('input', validatePasswords);
        if (confirm_input) {
            confirm_input.addEventListener('input', validatePasswords);
        }

        validatePasswords();
    }
});