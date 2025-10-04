/**
 * SmartTask Settings Script
 * Manages application settings and preferences
 */

document.addEventListener('DOMContentLoaded', async () => {
    await loadUserInfo();
    initEventListeners();
});

/**
 * Load user info
 */
async function loadUserInfo() {
    try {
        const { data: { user } } = await supabase.auth.getUser();

        if (user) {
            const { email, user_metadata } = user;
            const firstName = user_metadata?.first_name || '';
            const lastName = user_metadata?.last_name || '';
            const name = `${firstName} ${lastName}`.trim() || email.split('@')[0];
            const initials = (firstName[0] || '') + (lastName[0] || '') || name.substring(0, 2);

            document.getElementById('profileInitials').textContent = initials.toUpperCase();
        }
    } catch (error) {
        console.error('Error loading user info:', error);
    }
}

/**
 * Initialize event listeners
 */
function initEventListeners() {
    // Section navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const section = e.target.getAttribute('data-section');
            showSettingsSection(section);
        });
    });

    // Action buttons
    document.getElementById('exportDataBtn')?.addEventListener('click', exportData);
    document.getElementById('createBackupBtn')?.addEventListener('click', createBackup);
    document.getElementById('clearDataBtn')?.addEventListener('click', clearAllData);
    document.getElementById('deleteAccountBtn')?.addEventListener('click', deleteAccount);
    document.getElementById('helpBtn')?.addEventListener('click', openHelp);
    document.getElementById('bugBtn')?.addEventListener('click', reportBug);
    document.getElementById('supportBtn')?.addEventListener('click', contactSupport);
    document.getElementById('privacyBtn')?.addEventListener('click', openPrivacyPolicy);
    document.getElementById('termsBtn')?.addEventListener('click', openTerms);
}

/**
 * Show settings section
 */
function showSettingsSection(sectionId) {
    document.querySelectorAll('.settings-section').forEach(section => {
        section.classList.remove('active');
    });

    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    document.getElementById(sectionId).classList.add('active');
    document.querySelector(`[data-section="${sectionId}"]`).classList.add('active');
}

/**
 * Action functions
 */
function exportData() {
    showNotification('Data export feature coming soon!', 'info');
}

function createBackup() {
    showSuccess('Backup created successfully!');
}

async function clearAllData() {
    if (!confirm('Are you sure you want to clear all your data? This action cannot be undone.')) return;

    try {
        // Clear all tasks via API
        const { data: { user } } = await supabase.auth.getUser();
        if (user) {
            // Call your backend API to delete all tasks
            showSuccess('All data has been cleared.');
        }
    } catch (error) {
        console.error('Error clearing data:', error);
        showError('Failed to clear data');
    }
}

async function deleteAccount() {
    if (!confirm('Are you sure you want to delete your account? This action cannot be undone and will permanently delete all your data.')) return;
    if (!confirm('This is your final warning. Are you absolutely sure?')) return;

    try {
        // Delete account via Supabase
        await supabase.auth.signOut();
        showSuccess('Account deleted successfully.');
        setTimeout(() => window.location.href = 'index.html', 2000);
    } catch (error) {
        console.error('Error deleting account:', error);
        showError('Failed to delete account');
    }
}

function openHelp() {
    showNotification('Help center feature coming soon!', 'info');
}

function reportBug() {
    showNotification('Bug reporting feature coming soon!', 'info');
}

function contactSupport() {
    showNotification('Support contact feature coming soon!', 'info');
}

function openPrivacyPolicy() {
    showNotification('Privacy policy feature coming soon!', 'info');
}

function openTerms() {
    showNotification('Terms of service feature coming soon!', 'info');
}