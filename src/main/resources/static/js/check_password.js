document.addEventListener('DOMContentLoaded', (event) => {
    const password_input = document.getElementById('password');
    const submit_button = document.getElementById('registerButton');
    const password_error_div = document.getElementById('passwordError');
    const confirm_input = document.getElementById('confirmPassword');

    // require confirm_input as well; if confirm_input is missing we'll just validate password length
    if (password_input && submit_button && password_error_div) {
        submit_button.disabled = true;

        const min_length = 5;

        function validatePasswords() {
            const password = password_input.value;
            const confirm = confirm_input ? confirm_input.value : '';

            const lengthOk = password.length >= min_length;
            const matchOk = confirm_input ? (password === confirm) : true;

            // Determine error message priority: length first, then mismatch
            if (!lengthOk) {
                password_error_div.textContent = `La contraseña debe tener al menos ${min_length} caracteres.`;
            } else if (confirm_input && !matchOk) {
                password_error_div.textContent = 'Las contraseñas no coinciden.';
            } else {
                password_error_div.textContent = '';
            }

            // Enable submit only when both length and match (if confirm present) are OK
            submit_button.disabled = !(lengthOk && matchOk);
        }

        // listen to both inputs if confirm exists, otherwise only password
        password_input.addEventListener('input', validatePasswords);
        if (confirm_input) {
            confirm_input.addEventListener('input', validatePasswords);
        }

        // run once to set initial state
        validatePasswords();
    }
});