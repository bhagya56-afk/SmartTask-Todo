/**
 * SmartTask Login Script
 * Handles user authentication
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', handleLogin);
});

/**
 * Handle login form submission
 */
async function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    // Basic validation
    if (!email || !password) {
        showError('Please enter both email and password');
        return;
    }

    showLoading('Signing in...');

    try {
        const { data, error } = await supabase.auth.signInWithPassword({
            email: email,
            password: password
        });

        if (error) {
            hideLoading();
            showError(`Login failed: ${error.message}`);
            console.error('Login error:', error);
            return;
        }

        // Get user with metadata
        const { data: { user } } = await supabase.auth.getUser();

        hideLoading();

        if (user) {
            showSuccess('Login successful! Redirecting...');
            console.log('User logged in:', user);

            // Redirect to dashboard after short delay
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1000);
        }
    } catch (err) {
        hideLoading();
        showError('An unexpected error occurred. Please try again.');
        console.error('Login error:', err);
    }
}