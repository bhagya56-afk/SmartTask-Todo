/**
 * SmartTask Authentication Configuration
 * Shared Supabase configuration for login and register pages
 *
 * SECURITY NOTE: Move these credentials to environment variables in production!
 */

// Initialize Supabase client (only once)
const SUPABASE_URL = "https://hfujdljobwilnfwpcaea.supabase.co";
const SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhmdWpkbGpvYndpbG5md3BjYWVhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTg4NTc5ODYsImV4cCI6MjA3NDQzMzk4Nn0.LEpFZBWsnKHR5lZ0l7yPuWUodLhhUpZlthQVqi_eFBk";

const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_KEY);

/**
 * Show notification message
 */
function showNotification(message, type = 'info') {
    // Remove existing notification
    const existing = document.querySelector('.notification');
    if (existing) existing.remove();

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    const colors = {
        error: '#ef4444',
        success: '#10b981',
        info: '#3b82f6'
    };

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${colors[type]};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        z-index: 10000;
        font-weight: 600;
        max-width: 300px;
        animation: slideIn 0.3s ease;
    `;

    // Add keyframe animation
    if (!document.getElementById('notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 4000);
}

/**
 * Show loading overlay
 */
function showLoading(message = 'Processing...') {
    const loading = document.createElement('div');
    loading.id = 'loading-overlay';
    loading.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.9);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
    `;

    loading.innerHTML = `
        <div style="text-align: center;">
            <div style="
                width: 40px;
                height: 40px;
                border: 4px solid #e2e8f0;
                border-top: 4px solid #7c3aed;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin: 0 auto 1rem;
            "></div>
            <p style="color: #64748b; font-weight: 600;">${message}</p>
        </div>
    `;

    // Add spin animation
    if (!document.getElementById('loading-styles')) {
        const style = document.createElement('style');
        style.id = 'loading-styles';
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(loading);
}

/**
 * Hide loading overlay
 */
function hideLoading() {
    const loading = document.getElementById('loading-overlay');
    if (loading) loading.remove();
}

/**
 * Shorthand notification functions
 */
const showError = (message) => showNotification(message, 'error');
const showSuccess = (message) => showNotification(message, 'success');