/**
 * SmartTask Profile Script
 * Manages user profile data and statistics
 */

const API_BASE_URL = 'http://localhost:8080/api';

// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    await loadProfileData();
    await loadTaskStatistics();
    initEventListeners();
});

/**
 * Check authentication
 */
async function checkAuth() {
    try {
        const { data: { user } } = await supabase.auth.getUser();

        if (!user) {
            showError('Please login first');
            window.location.href = 'login.html';
            return false;
        }

        return true;
    } catch (error) {
        console.error('Auth check error:', error);
        window.location.href = 'login.html';
        return false;
    }
}

/**
 * Load profile data from Supabase
 */
async function loadProfileData() {
    try {
        const { data: { user } } = await supabase.auth.getUser();

        if (!user) return;

        const { email, user_metadata } = user;
        const firstName = user_metadata?.first_name || '';
        const lastName = user_metadata?.last_name || '';
        const studentId = user_metadata?.student_id || '';
        const major = user_metadata?.major || '';

        // Update profile fields
        document.getElementById('studentEmail').textContent = email;
        document.getElementById('email').value = email;
        document.getElementById('firstName').value = firstName;
        document.getElementById('lastName').value = lastName;
        document.getElementById('studentId').value = studentId;
        document.getElementById('studentIdDisplay').textContent = studentId || '-';
        document.getElementById('major').value = major;

        // Update display name
        const fullName = `${firstName} ${lastName}`.trim() || 'User';
        document.getElementById('studentName').textContent = fullName;

        // Generate initials
        let initials = '';
        if (firstName) initials += firstName.charAt(0).toUpperCase();
        if (lastName) initials += lastName.charAt(0).toUpperCase();
        if (!initials) initials = email.charAt(0).toUpperCase();

        document.getElementById('profileInitials').textContent = initials;
        document.getElementById('sidebarInitials').textContent = initials;
    } catch (error) {
        console.error('Error loading profile:', error);
        showError('Error loading profile data');
    }
}

/**
 * Load task statistics
 */
async function loadTaskStatistics() {
    try {
        const { data: { user } } = await supabase.auth.getUser();
        if (!user) return;

        const response = await fetch(`${API_BASE_URL}/tasks?email=${encodeURIComponent(user.email)}`);

        if (!response.ok) {
            throw new Error('Failed to load tasks');
        }

        const tasks = await response.json();

        // Calculate statistics
        const total = tasks.length;
        const completed = tasks.filter(t => t.completed).length;
        const pending = total - completed;
        const rate = total > 0 ? Math.round((completed / total) * 100) : 0;

        // Update stats display
        document.getElementById('profileStatTotal').textContent = total;
        document.getElementById('profileStatCompleted').textContent = completed;
        document.getElementById('profileStatPending').textContent = pending;
        document.getElementById('profileStatRate').textContent = rate + '%';

        // Count by category
        const categories = {};
        tasks.forEach(task => {
            const cat = task.category || 'Other';
            categories[cat] = (categories[cat] || 0) + 1;
        });

        // Display categories
        renderCategories(categories, total);
    } catch (error) {
        console.error('Error loading statistics:', error);
        document.getElementById('categoriesList').innerHTML =
            '<p style="text-align: center; color: #ef4444;">Error loading data. Make sure your backend is running.</p>';
    }
}

/**
 * Render category statistics
 */
function renderCategories(categories, total) {
    const categoryList = document.getElementById('categoriesList');

    if (Object.keys(categories).length === 0) {
        categoryList.innerHTML = '<p style="text-align: center; color: #64748b;">No tasks yet. Add some tasks to see categories!</p>';
        return;
    }

    const emojis = {
        'study/Academics': '📚',
        'personal': '👤',
        'lab/Practical work': '🧪',
        'Projects': '🗃️',
        'Exams': '📃',
        'Events': '🔖',
        'Other': '📝'
    };

    categoryList.innerHTML = Object.entries(categories)
        .sort((a, b) => b[1] - a[1])
        .map(([cat, count]) => {
            const percent = Math.round((count / total) * 100);
            const emoji = emojis[cat] || '📝';
            return `
                <div class="category-item">
                    <span class="category-name">${emoji} ${cat}</span>
                    <div class="category-bar">
                        <div class="category-progress" style="width: ${percent}%"></div>
                    </div>
                    <span class="category-count">${count}</span>
                </div>
            `;
        }).join('');
}

/**
 * Initialize event listeners
 */
function initEventListeners() {
    // Section navigation
    document.querySelectorAll('.nav-item').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const section = e.target.getAttribute('data-section');
            showSection(section);
        });
    });

    // Edit buttons
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const formType = e.target.getAttribute('data-form');
            toggleEdit(formType, e.target);
        });
    });

    // Save buttons
    document.querySelectorAll('.save-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const formType = e.target.getAttribute('data-form');
            if (formType === 'personal') savePersonalInfo();
            if (formType === 'academic') saveAcademicInfo();
        });
    });

    // Cancel buttons
    document.querySelectorAll('.cancel-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const formType = e.target.getAttribute('data-form');
            cancelEdit(formType);
        });
    });

    // Password change modal
    document.getElementById('changePasswordBtn').addEventListener('click', showChangePasswordModal);
    document.getElementById('closePasswordModal').addEventListener('click', hideChangePasswordModal);
    document.getElementById('cancelPasswordBtn').addEventListener('click', hideChangePasswordModal);
    document.getElementById('passwordForm').addEventListener('submit', changePassword);
}

/**
 * Show section
 */
function showSection(sectionId) {
    document.querySelectorAll('.profile-section').forEach(section => {
        section.classList.remove('active');
    });

    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    document.getElementById(sectionId).classList.add('active');
    document.querySelector(`[data-section="${sectionId}"]`).classList.add('active');

    if (sectionId === 'task-stats') {
        loadTaskStatistics();
    }
}

/**
 * Toggle edit mode
 */
function toggleEdit(formType, editBtn) {
    const form = document.getElementById(formType + 'Form');
    const inputs = form.querySelectorAll('input, select, textarea');
    const actions = document.getElementById(formType + 'Actions');

    if (editBtn.textContent === 'Edit') {
        inputs.forEach(input => {
            if (input.id !== 'email') {
                input.readOnly = false;
                input.disabled = false;
            }
        });
        actions.classList.remove('hidden');
        editBtn.textContent = 'Cancel';
        editBtn.classList.add('cancel-mode');
    } else {
        cancelEdit(formType);
    }
}

/**
 * Cancel edit
 */
function cancelEdit(formType) {
    const form = document.getElementById(formType + 'Form');
    const inputs = form.querySelectorAll('input, select, textarea');
    const actions = document.getElementById(formType + 'Actions');
    const editBtn = form.previousElementSibling.querySelector('.edit-btn');

    inputs.forEach(input => {
        input.readOnly = true;
        input.disabled = true;
    });
    actions.classList.add('hidden');
    editBtn.textContent = 'Edit';
    editBtn.classList.remove('cancel-mode');

    loadProfileData();
}

/**
 * Save personal info
 */
async function savePersonalInfo() {
    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const bio = document.getElementById('bio').value.trim();

    try {
        const { error } = await supabase.auth.updateUser({
            data: {
                first_name: firstName,
                last_name: lastName,
                phone: phone,
                bio: bio
            }
        });

        if (error) throw error;

        showSuccess('Personal information updated successfully!');
        loadProfileData();
        cancelEdit('personal');
    } catch (error) {
        console.error('Error updating profile:', error);
        showError('Failed to update profile');
    }
}

/**
 * Save academic info
 */
async function saveAcademicInfo() {
    const studentId = document.getElementById('studentId').value.trim();
    const major = document.getElementById('major').value;
    const yearOfStudy = document.getElementById('yearOfStudy').value;
    const gpa = document.getElementById('gpa').value;
    const institution = document.getElementById('institution').value.trim();

    try {
        const { error } = await supabase.auth.updateUser({
            data: {
                student_id: studentId,
                major: major,
                year_of_study: yearOfStudy,
                gpa: gpa,
                institution: institution
            }
        });

        if (error) throw error;

        showSuccess('Academic information updated successfully!');
        loadProfileData();
        cancelEdit('academic');
    } catch (error) {
        console.error('Error updating academic info:', error);
        showError('Failed to update academic information');
    }
}

/**
 * Change password modal functions
 */
function showChangePasswordModal() {
    document.getElementById('changePasswordModal').style.display = 'flex';
}

function hideChangePasswordModal() {
    document.getElementById('changePasswordModal').style.display = 'none';
    document.getElementById('passwordForm').reset();
}

/**
 * Change password
 */
async function changePassword(event) {
    event.preventDefault();

    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showError('New passwords do not match!');
        return;
    }

    if (newPassword.length < 6) {
        showError('Password must be at least 6 characters long!');
        return;
    }

    try {
        const { error } = await supabase.auth.updateUser({
            password: newPassword
        });

        if (error) throw error;

        showSuccess('Password changed successfully!');
        hideChangePasswordModal();
    } catch (error) {
        console.error('Error changing password:', error);
        showError('Failed to change password: ' + error.message);
    }
}