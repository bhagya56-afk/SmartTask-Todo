/**
 * SmartTask Dashboard Script
 * Manages task display, CRUD operations, and UI interactions
 */

// Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Task Manager Class
class TaskManager {
    constructor() {
        this.tasks = [];
        this.currentFilter = 'all';
        this.currentUser = null;
    }

    setTasks(tasks) {
        this.tasks = tasks;
    }

    getTasks() {
        return this.tasks;
    }

    getFilteredTasks(filter = 'all') {
        if (filter === 'all') return this.tasks;
        return this.tasks.filter(t => t.priority === filter);
    }

    getCategoryTasks(category) {
        return this.tasks.filter(t =>
            t.category.toLowerCase().includes(category.toLowerCase())
        );
    }

    getCompletedTasks() {
        return this.tasks.filter(t => t.completed);
    }
}

const taskManager = new TaskManager();

// ===========================
// Authentication & Initialization
// ===========================

async function checkAuth() {
    try {
        const { data: { user }, error } = await supabase.auth.getUser();

        console.log('Auth check:', user ? 'User found' : 'No user');

        if (error || !user) {
            console.error('No user found:', error);
            showError('Please login first');
            setTimeout(() => window.location.href = 'login.html', 2000);
            return false;
        }

        taskManager.currentUser = user;
        displayUserInfo(user);
        return true;
    } catch (error) {
        console.error('Auth check error:', error);
        showError('Authentication error. Please login again.');
        setTimeout(() => window.location.href = 'login.html', 2000);
        return false;
    }
}

function displayUserInfo(user) {
    const { email, user_metadata } = user;
    const firstName = user_metadata?.first_name || email.split('@')[0];
    const lastName = user_metadata?.last_name || '';

    document.getElementById('userName').textContent = firstName;

    const initials = firstName && lastName
        ? firstName[0] + lastName[0]
        : firstName[0] + (firstName[1] || '');
    document.getElementById('userAvatar').textContent = initials.toUpperCase();
}

// ===========================
// Task API Operations
// ===========================

async function loadTasks() {
    if (!taskManager.currentUser) {
        console.warn('No current user');
        return;
    }

    const email = taskManager.currentUser.email;
    console.log('Loading tasks for:', email);

    try {
        const response = await fetch(`${API_BASE_URL}/tasks?email=${encodeURIComponent(email)}`);

        console.log('Response status:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const tasks = await response.json();
        console.log('Tasks loaded:', tasks.length);

        taskManager.setTasks(tasks);

        updateStats();
        renderOverviewTasks();
        renderAllTasks();
    } catch (error) {
        console.error('Error loading tasks:', error);

        if (error.message.includes('Failed to fetch')) {
            showError('Cannot connect to backend. Is your Java server running on port 8080?');
        } else {
            showError('Error loading tasks: ' + error.message);
        }

        document.getElementById('recentTasksList').innerHTML =
            '<div class="no-tasks"><p>Unable to load tasks. Check console for details.</p></div>';
    }
}

async function addTask(taskData) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                ...taskData,
                studentEmail: taskManager.currentUser.email
            })
        });

        const result = await response.json();

        if (result.success) {
            await loadTasks();
            showSuccess('Task added successfully!');
            return true;
        } else {
            showError('Failed to add task');
            return false;
        }
    } catch (error) {
        console.error('Error adding task:', error);
        showError('Error connecting to server');
        return false;
    }
}

async function toggleTask(taskId) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/complete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ taskId })
        });

        const result = await response.json();

        if (result.success) {
            await loadTasks();
            showSuccess('Task updated!');
        } else {
            showError('Failed to update task');
        }
    } catch (error) {
        console.error('Error toggling task:', error);
        showError('Error updating task');
    }
}

async function deleteTask(taskId) {


    try {
        const response = await fetch(`${API_BASE_URL}/tasks/delete?id=${taskId}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            await loadTasks();
            showSuccess('Task deleted!');
        } else {
            showError('Failed to delete task');
        }
    } catch (error) {
        console.error('Error deleting task:', error);
        showError('Error deleting task');
    }
}

// ===========================
// UI Rendering
// ===========================

function updateStats() {
    const today = new Date().toISOString().split('T')[0];
    const tasks = taskManager.getTasks();

    const stats = {
        total: tasks.length,
        completed: tasks.filter(t => t.completed).length,
        pending: tasks.filter(t => !t.completed).length,
        dueToday: tasks.filter(t => {
            const taskDate = t.dueDate.split('T')[0];
            return taskDate === today && !t.completed;
        }).length
    };

    document.getElementById('stat-total').textContent = stats.total;
    document.getElementById('stat-completed').textContent = stats.completed;
    document.getElementById('stat-pending').textContent = stats.pending;
    document.getElementById('stat-duetoday').textContent = stats.dueToday;
}

function renderOverviewTasks() {
    const taskList = document.getElementById('recentTasksList');
    if (!taskList) return;

    const recentTasks = [...taskManager.getTasks()]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5);

    if (recentTasks.length === 0) {
        taskList.innerHTML = '<div class="no-tasks"><p>No tasks yet. Click "Add New Task" to get started!</p></div>';
        return;
    }

    taskList.innerHTML = recentTasks.map(task => createTaskHTML(task)).join('');
}

function renderAllTasks() {
    const taskGrid = document.getElementById('allTasksGrid');
    if (!taskGrid) return;

    const filteredTasks = taskManager.getFilteredTasks(taskManager.currentFilter);

    if (filteredTasks.length === 0) {
        taskGrid.innerHTML = '<div class="no-tasks"><p>No tasks found.</p></div>';
        return;
    }

    taskGrid.innerHTML = filteredTasks.map(task => createTaskHTML(task)).join('');
}

function createTaskHTML(task) {
    const priorityClass = `${task.priority}-priority`;
    const priorityEmoji = task.priority === 'high' ? '🔴' : task.priority === 'medium' ? '🟡' : '🟢';

    const categoryEmojis = {
        'study/Academics': '📚',
        'Assignments': '🖋️',
        'lab/Practical work': '🧪',
        'Projects': '🗃️',
        'Exams': '📃',
        'Events': '🔖',
        'Other': '📝'
    };

    const dueDate = new Date(task.dueDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    let dueDateText = dueDate.toLocaleDateString();
    if (dueDate.toDateString() === today.toDateString()) {
        dueDateText = 'Today';
    } else if (dueDate.toDateString() === tomorrow.toDateString()) {
        dueDateText = 'Tomorrow';
    }

    return `
        <div class="task-item ${priorityClass} ${task.completed ? 'completed' : ''}" data-task-id="${task.id}">
            <div class="task-checkbox">
                <input type="checkbox" id="task${task.id}" ${task.completed ? 'checked' : ''} 
                       data-task-id="${task.id}" class="task-toggle">
                <label for="task${task.id}"></label>
            </div>
            <div class="task-content">
                <h4${task.completed ? ' class="completed-task"' : ''}>${escapeHTML(task.title)}</h4>
                <p>Due: ${dueDateText} • Category: ${categoryEmojis[task.category] || '📝'} ${escapeHTML(task.category)} • Priority: ${priorityEmoji} ${task.priority}</p>
                ${task.description ? `<p class="task-description">${escapeHTML(task.description)}</p>` : ''}
            </div>
            <div class="task-actions">
                <button class="btn-icon delete" data-task-id="${task.id}" data-action="delete" title="Delete Task">🗑️</button>
            </div>
        </div>
    `;
}

function escapeHTML(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ===========================
// Event Handlers
// ===========================

function handleTaskFormSubmit(e) {
    e.preventDefault();

    const taskData = {
        title: document.getElementById('taskTitle').value.trim(),
        description: document.getElementById('taskDescription').value.trim(),
        category: document.getElementById('taskCategory').value,
        priority: document.getElementById('taskPriority').value,
        dueDate: document.getElementById('taskDueDate').value,
        dueTime: document.getElementById('taskDueTime').value || ''
    };

    if (!taskData.title || !taskData.category || !taskData.priority || !taskData.dueDate) {
        showError('Please fill in all required fields');
        return;
    }

    addTask(taskData).then(success => {
        if (success) {
            closeModal();
            e.target.reset();
        }
    });
}

function handleTaskAction(e) {
    const target = e.target;

    if (target.classList.contains('task-toggle')) {
        const taskId = parseInt(target.dataset.taskId);
        toggleTask(taskId);
    }

    if (target.dataset.action === 'delete') {
        const taskId = parseInt(target.dataset.taskId);
        deleteTask(taskId);
    }
}

function handleTabSwitch(e) {
    e.preventDefault();
    const tabName = e.target.getAttribute('data-tab');

    if (['study', 'Assignments', 'lab', 'project'].includes(tabName)) {
        showCategoryTasks(tabName);
    } else if (tabName === 'completed') {
        showCompletedTasks();
    } else if (tabName === 'archived') {
        showArchivedTasks();
    } else {
        switchTab(tabName);
        if (tabName === 'all-tasks') {
            document.querySelector('#all-tasks .page-header h1').textContent = 'All Tasks';
            renderAllTasks();
        }
    }
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));

    const tab = document.getElementById(tabName);
    if (tab) tab.classList.add('active');

    const link = document.querySelector(`[data-tab="${tabName}"]`);
    if (link) link.classList.add('active');
}

function showCategoryTasks(categoryName) {
    switchTab('all-tasks');

    const taskGrid = document.getElementById('allTasksGrid');
    const pageHeader = document.querySelector('#all-tasks .page-header h1');

    if (pageHeader) {
        pageHeader.textContent = `${categoryName.charAt(0).toUpperCase() + categoryName.slice(1)} Tasks`;
    }

    const filteredTasks = taskManager.getCategoryTasks(categoryName);

    if (filteredTasks.length === 0) {
        taskGrid.innerHTML = `<div class="no-tasks"><p>No ${categoryName} tasks found. Click "Add Task" to create one!</p></div>`;
        return;
    }

    taskGrid.innerHTML = filteredTasks.map(task => createTaskHTML(task)).join('');
}

function showCompletedTasks() {
    switchTab('all-tasks');

    const taskGrid = document.getElementById('allTasksGrid');
    const pageHeader = document.querySelector('#all-tasks .page-header h1');

    if (pageHeader) pageHeader.textContent = 'Completed Tasks';

    const completedTasks = taskManager.getCompletedTasks();

    if (completedTasks.length === 0) {
        taskGrid.innerHTML = '<div class="no-tasks"><p>No completed tasks yet!</p></div>';
        return;
    }

    taskGrid.innerHTML = completedTasks.map(task => createTaskHTML(task)).join('');
}

function showArchivedTasks() {
    switchTab('all-tasks');

    const taskGrid = document.getElementById('allTasksGrid');
    const pageHeader = document.querySelector('#all-tasks .page-header h1');

    if (pageHeader) pageHeader.textContent = 'Archived Tasks';

    taskGrid.innerHTML = '<div class="no-tasks"><p>Archived tasks feature coming soon!</p></div>';
}

// ===========================
// Modal Management
// ===========================

function openModal() {
    document.getElementById('addTaskModal').classList.add('active');
    document.getElementById('taskDueDate').setAttribute('min', new Date().toISOString().split('T')[0]);
}

function closeModal() {
    document.getElementById('addTaskModal').classList.remove('active');
    document.getElementById('taskForm').reset();
}

// ===========================
// Logout
// ===========================

async function logout() {
    try {
        await supabase.auth.signOut();
        showSuccess('Logged out successfully!');
        setTimeout(() => window.location.href = 'index.html', 1000);
    } catch (error) {
        console.error('Logout error:', error);
        showError('Error logging out');
    }
}


// ===========================
// Initialize
// ===========================

document.addEventListener('DOMContentLoaded', async () => {
    console.log('Dashboard initializing...');

    const isAuthenticated = await checkAuth();
    if (!isAuthenticated) return;

    await loadTasks();

    // Event listeners
    document.getElementById('taskForm').addEventListener('submit', handleTaskFormSubmit);
    document.getElementById('addTaskBtn').addEventListener('click', openModal);
    document.getElementById('addTaskBtn2')?.addEventListener('click', openModal);
    document.getElementById('closeModalBtn').addEventListener('click', closeModal);
    document.getElementById('cancelBtn').addEventListener('click', closeModal);
    document.getElementById('logoutBtn').addEventListener('click', logout);

    document.getElementById('priorityFilter')?.addEventListener('change', (e) => {
        taskManager.currentFilter = e.target.value;
        renderAllTasks();
    });

    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', handleTabSwitch);
    });

    document.addEventListener('click', handleTaskAction);

    document.getElementById('addTaskModal').addEventListener('click', (e) => {
        if (e.target.id === 'addTaskModal') closeModal();
    });

    const style = document.createElement('style');
    style.textContent = `
        .no-tasks { text-align: center; padding: 40px; color: #64748b; }
        .completed-task { text-decoration: line-through; opacity: 0.7; }
        .task-description { font-size: 0.85rem; margin-top: 0.25rem; }
    `;
    document.head.appendChild(style);

    console.log('Dashboard initialized successfully!');
});