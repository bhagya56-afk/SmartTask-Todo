// Authentication Script for SmartTask Login System

class AuthManager {
    constructor() {
        this.initializeEventListeners();
        this.checkExistingSession();
    }

    initializeEventListeners() {
        // Login form submission
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Register form submission
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // Remember me checkbox
        const rememberMe = document.getElementById('rememberMe');
        if (rememberMe) {
            // Load saved email if remember me was checked
            const savedEmail = localStorage.getItem('savedEmail');
            if (savedEmail) {
                const emailInput = document.getElementById('email') || document.querySelector('input[type="email"]');
                if (emailInput) {
                    emailInput.value = savedEmail;
                    rememberMe.checked = true;
                }
            }
        }

        // Show/hide password functionality
        this.setupPasswordToggle();
    }

    async handleLogin(event) {
        event.preventDefault();

        const formData = this.getLoginFormData();

        if (!this.validateLoginForm(formData)) {
            return;
        }

        this.showLoadingState('Signing in...');

        try {
            // For demo purposes - replace with actual API call
            const result = await this.loginUser(formData);

            if (result.success) {
                this.handleLoginSuccess(formData);
            } else {
                this.showError(result.message || 'Login failed. Please try again.');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showError('An error occurred during login. Please try again.');
        } finally {
            this.hideLoadingState();
        }
    }

    async handleRegister(event) {
        event.preventDefault();

        const formData = this.getRegisterFormData();

        if (!this.validateRegisterForm(formData)) {
            return;
        }

        this.showLoadingState('Creating account...');

        try {
            const result = await this.registerUser(formData);

            if (result.success) {
                this.showSuccess('Account created successfully! Please sign in.');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else {
                this.showError(result.message || 'Registration failed. Please try again.');
            }
        } catch (error) {
            console.error('Registration error:', error);
            this.showError('An error occurred during registration. Please try again.');
        } finally {
            this.hideLoadingState();
        }
    }

    getLoginFormData() {
        const emailInput = document.getElementById('email') || document.querySelector('input[type="email"]');
        const passwordInput = document.getElementById('password') || document.querySelector('input[type="password"]');
        const rememberMe = document.getElementById('rememberMe');

        return {
            email: emailInput ? emailInput.value.trim() : '',
            password: passwordInput ? passwordInput.value : '',
            rememberMe: rememberMe ? rememberMe.checked : false
        };
    }

    getRegisterFormData() {
        return {
            email: document.getElementById('email')?.value.trim() || '',
            password: document.getElementById('password')?.value || '',
            confirmPassword: document.getElementById('confirmPassword')?.value || '',
            firstName: document.getElementById('firstName')?.value.trim() || '',
            lastName: document.getElementById('lastName')?.value.trim() || ''
        };
    }

    validateLoginForm(data) {
        if (!data.email) {
            this.showError('Please enter your email address.');
            this.focusField('email');
            return false;
        }

        if (!this.isValidEmail(data.email)) {
            this.showError('Please enter a valid email address.');
            this.focusField('email');
            return false;
        }

        if (!data.password) {
            this.showError('Please enter your password.');
            this.focusField('password');
            return false;
        }

        return true;
    }

    validateRegisterForm(data) {
        if (!data.firstName) {
            this.showError('Please enter your first name.');
            this.focusField('firstName');
            return false;
        }

        if (!data.lastName) {
            this.showError('Please enter your last name.');
            this.focusField('lastName');
            return false;
        }

        if (!data.email) {
            this.showError('Please enter your email address.');
            this.focusField('email');
            return false;
        }

        if (!this.isValidEmail(data.email)) {
            this.showError('Please enter a valid email address.');
            this.focusField('email');
            return false;
        }

        if (!data.password) {
            this.showError('Please enter a password.');
            this.focusField('password');
            return false;
        }

        if (data.password.length < 6) {
            this.showError('Password must be at least 6 characters long.');
            this.focusField('password');
            return false;
        }

        if (data.password !== data.confirmPassword) {
            this.showError('Passwords do not match.');
            this.focusField('confirmPassword');
            return false;
        }

        return true;
    }

    async loginUser(formData) {
        // Demo authentication - replace with actual API call
        return new Promise((resolve) => {
            setTimeout(() => {
                // Demo credentials for testing
                const demoCredentials = [
                    { email: 'student@example.com', password: 'password' },
                    { email: 'bhagya@gmail.com', password: 'password' },
                    { email: 'demo@smarttask.com', password: 'demo123' }
                ];

                const isValid = demoCredentials.some(cred =>
                    cred.email.toLowerCase() === formData.email.toLowerCase() &&
                    cred.password === formData.password
                );

                if (isValid) {
                    resolve({
                        success: true,
                        user: {
                            email: formData.email,
                            name: 'Student User',
                            id: 'demo-user-id'
                        },
                        token: 'demo-jwt-token'
                    });
                } else {
                    resolve({
                        success: false,
                        message: 'Invalid email or password. Try: student@example.com / password'
                    });
                }
            }, 1000); // Simulate network delay
        });
    }

    async registerUser(formData) {
        // Demo registration - replace with actual API call
        return new Promise((resolve) => {
            setTimeout(() => {
                // For demo, always succeed
                resolve({
                    success: true,
                    message: 'Account created successfully!'
                });
            }, 1000);
        });
    }

    handleLoginSuccess(formData) {
        // Save email if remember me is checked
        if (formData.rememberMe) {
            localStorage.setItem('savedEmail', formData.email);
        } else {
            localStorage.removeItem('savedEmail');
        }

        // Save session info (replace with proper token handling)
        sessionStorage.setItem('isLoggedIn', 'true');
        sessionStorage.setItem('userEmail', formData.email);

        this.showSuccess('Login successful! Redirecting...');

        // Redirect to dashboard or main app
        setTimeout(() => {
            window.location.href = 'dashboard.html'; // Change this to your main app page
        }, 1500);
    }

    checkExistingSession() {
        // Check if user is already logged in
        const isLoggedIn = sessionStorage.getItem('isLoggedIn');
        const currentPage = window.location.pathname;

        if (isLoggedIn === 'true' && (currentPage.includes('login.html') || currentPage.includes('register.html'))) {
            // Already logged in, redirect to dashboard
            window.location.href = 'dashboard.html';
        }
    }

    setupPasswordToggle() {
        const passwordInputs = document.querySelectorAll('input[type="password"]');

        passwordInputs.forEach(input => {
            const container = input.parentElement;

            // Create toggle button
            const toggleBtn = document.createElement('button');
            toggleBtn.type = 'button';
            toggleBtn.className = 'password-toggle';
            toggleBtn.innerHTML = '👁️';
            toggleBtn.title = 'Show/Hide Password';

            // Add styles
            toggleBtn.style.cssText = `
                position: absolute;
                right: 12px;
                top: 50%;
                transform: translateY(-50%);
                background: none;
                border: none;
                cursor: pointer;
                font-size: 16px;
                opacity: 0.6;
                transition: opacity 0.2s;
            `;

            toggleBtn.addEventListener('mouseenter', () => toggleBtn.style.opacity = '1');
            toggleBtn.addEventListener('mouseleave', () => toggleBtn.style.opacity = '0.6');

            // Make container relative if not already
            if (getComputedStyle(container).position === 'static') {
                container.style.position = 'relative';
            }

            container.appendChild(toggleBtn);

            toggleBtn.addEventListener('click', () => {
                if (input.type === 'password') {
                    input.type = 'text';
                    toggleBtn.innerHTML = '🙈';
                } else {
                    input.type = 'password';
                    toggleBtn.innerHTML = '👁️';
                }
            });
        });
    }

    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    focusField(fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            field.focus();
            field.select();
        }
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showNotification(message, type = 'info') {
        // Remove any existing notifications
        const existingNotifications = document.querySelectorAll('.auth-notification');
        existingNotifications.forEach(notification => notification.remove());

        // Create notification element
        const notification = document.createElement('div');
        notification.className = `auth-notification ${type}`;
        notification.textContent = message;

        // Add styles
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 16px 24px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            max-width: 400px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            transform: translateX(400px);
            transition: transform 0.3s ease;
        `;

        // Set colors based on type
        switch (type) {
            case 'error':
                notification.style.background = 'linear-gradient(135deg, #ef4444, #dc2626)';
                break;
            case 'success':
                notification.style.background = 'linear-gradient(135deg, #10b981, #059669)';
                break;
            default:
                notification.style.background = 'linear-gradient(135deg, #6366f1, #4f46e5)';
        }

        document.body.appendChild(notification);

        // Animate in
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 100);

        // Auto hide after 5 seconds
        setTimeout(() => {
            notification.style.transform = 'translateX(400px)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 5000);

        // Click to dismiss
        notification.addEventListener('click', () => {
            notification.style.transform = 'translateX(400px)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        });
    }

    showLoadingState(message = 'Loading...') {
        const submitBtn = document.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `
                <span style="display: inline-flex; align-items: center; gap: 8px;">
                    <span class="loading-spinner"></span>
                    ${message}
                </span>
            `;
        }

        // Add spinner CSS if not already present
        if (!document.getElementById('spinner-styles')) {
            const style = document.createElement('style');
            style.id = 'spinner-styles';
            style.textContent = `
                .loading-spinner {
                    width: 16px;
                    height: 16px;
                    border: 2px solid rgba(255,255,255,0.3);
                    border-top: 2px solid white;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `;
            document.head.appendChild(style);
        }
    }

    hideLoadingState() {
        const submitBtn = document.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            // Restore original button text based on page
            const isLoginPage = window.location.pathname.includes('login');
            submitBtn.innerHTML = isLoginPage ? 'Sign In' : 'Create Account';
        }
    }

    // Logout function
    logout() {
        sessionStorage.clear();
        localStorage.removeItem('savedEmail'); // Optional: keep saved email
        window.location.href = 'login.html';
    }
}

// Utility functions for other pages
function checkAuthStatus() {
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    if (isLoggedIn !== 'true') {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function getUserInfo() {
    return {
        email: sessionStorage.getItem('userEmail'),
        isLoggedIn: sessionStorage.getItem('isLoggedIn') === 'true'
    };
}

function logout() {
    if (window.authManager) {
        window.authManager.logout();
    } else {
        sessionStorage.clear();
        window.location.href = 'login.html';
    }
}

// Initialize auth manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.authManager = new AuthManager();
});

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { AuthManager, checkAuthStatus, getUserInfo, logout };
}