/**
 * SmartTask Registration Script
 * Handles user registration with validation
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // Real-time password confirmation validation
    confirmPasswordInput.addEventListener('input', () => {
        if (confirmPasswordInput.value && confirmPasswordInput.value !== passwordInput.value) {
            confirmPasswordInput.style.borderColor = '#ef4444';
        } else {
            confirmPasswordInput.style.borderColor = '';
        }
    });

    // Handle form submission
    form.addEventListener('submit', handleRegister);
});

/**
 * Handle registration form submission
 */
async function handleRegister(event) {
    event.preventDefault();

    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();
    const email = document.getElementById('email').value.trim();
    const studentId = document.getElementById('studentId').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const major = document.getElementById('major').value;

    // Validate passwords match
    if (password !== confirmPassword) {
        showError('Passwords do not match');
        return;
    }

    // Validate password length
    if (password.length < 6) {
        showError('Password must be at least 6 characters');
        return;
    }

    showLoading('Creating account...');

    try {
        const { data, error } = await supabase.auth.signUp({
            email: email,
            password: password,
            options: {
                data: {
                    first_name: firstName,
                    last_name: lastName,
                    student_id: studentId,
                    major: major
                }
            }
        });

        hideLoading();

        if (error) {
            showError(`Registration failed: ${error.message}`);
            console.error('Registration error:', error);
        } else {
            showSuccess('Account created! Please check your email to verify.');
            console.log('User created:', data);

            // Redirect to login after 2 seconds
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        }
    } catch (err) {
        hideLoading();
        showError('An unexpected error occurred. Please try again.');
        console.error('Registration error:', err);
    }
}