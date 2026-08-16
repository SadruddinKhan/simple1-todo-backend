/**
 * TaskFlow - Plain JavaScript Todo Frontend Client
 * Interacts with Spring Boot REST API (/api/todos)
 */

(function () {
    'use strict';

    // State
    const state = {
        todos: [],
        currentFilter: 'all', // 'all', 'pending', 'completed'
        searchQuery: '',
        pendingDelete: null, // { type: 'single'|'all', id: null }
        isLoading: false
    };

    // DOM Elements
    const elements = {
        // Theme
        themeToggleBtn: document.getElementById('themeToggleBtn'),
        sunIcon: document.getElementById('sunIcon'),
        moonIcon: document.getElementById('moonIcon'),

        // Stats
        statTotal: document.getElementById('statTotal'),
        statCompleted: document.getElementById('statCompleted'),
        statPending: document.getElementById('statPending'),
        progressText: document.getElementById('progressText'),
        progressBar: document.getElementById('progressBar'),
        tabCountAll: document.getElementById('tabCountAll'),
        tabCountPending: document.getElementById('tabCountPending'),
        tabCountCompleted: document.getElementById('tabCountCompleted'),

        // Quick Add Form
        quickAddForm: document.getElementById('quickAddForm'),
        quickTitle: document.getElementById('quickTitle'),
        quickDescription: document.getElementById('quickDescription'),
        quickDescriptionContainer: document.getElementById('quickDescriptionContainer'),
        toggleDescriptionBtn: document.getElementById('toggleDescriptionBtn'),

        // Search & Filters
        searchInput: document.getElementById('searchInput'),
        clearSearchBtn: document.getElementById('clearSearchBtn'),
        filterTabs: document.querySelectorAll('.filter-tab'),
        clearAllBtn: document.getElementById('clearAllBtn'),

        // Todo List
        todoListContainer: document.getElementById('todoListContainer'),
        emptyStateContainer: document.getElementById('emptyStateContainer'),
        emptyStateAddBtn: document.getElementById('emptyStateAddBtn'),

        // Create / Edit Modal
        taskModal: document.getElementById('taskModal'),
        modalDialog: document.getElementById('modalDialog'),
        modalTitle: document.getElementById('modalTitle'),
        taskForm: document.getElementById('taskForm'),
        modalTaskId: document.getElementById('modalTaskId'),
        modalTitleInput: document.getElementById('modalTitleInput'),
        modalDescriptionInput: document.getElementById('modalDescriptionInput'),
        modalCompletedContainer: document.getElementById('modalCompletedContainer'),
        modalCompletedInput: document.getElementById('modalCompletedInput'),
        openCreateModalBtn: document.getElementById('openCreateModalBtn'),
        closeModalBtn: document.getElementById('closeModalBtn'),
        cancelModalBtn: document.getElementById('cancelModalBtn'),

        // Delete Modal
        deleteModal: document.getElementById('deleteModal'),
        deleteModalDialog: document.getElementById('deleteModalDialog'),
        deleteModalTitle: document.getElementById('deleteModalTitle'),
        deleteModalMessage: document.getElementById('deleteModalMessage'),
        cancelDeleteBtn: document.getElementById('cancelDeleteBtn'),
        confirmDeleteBtn: document.getElementById('confirmDeleteBtn'),

        // Toast Container
        toastContainer: document.getElementById('toastContainer')
    };

    // --- HTML Sanitization & Date Helpers ---
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    function formatDate(dateStr) {
        if (!dateStr) return '';
        try {
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) return '';
            return date.toLocaleDateString(undefined, {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return '';
        }
    }

    // --- Toast Notifications ---
    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = 'pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg border text-sm font-medium transition-all toast-in';

        let iconSvg = '';
        if (type === 'success') {
            toast.className += ' bg-white dark:bg-slate-900 border-emerald-200 dark:border-emerald-800/80 text-emerald-800 dark:text-emerald-300';
            iconSvg = `<svg class="w-5 h-5 text-emerald-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`;
        } else if (type === 'error') {
            toast.className += ' bg-white dark:bg-slate-900 border-rose-200 dark:border-rose-800/80 text-rose-800 dark:text-rose-300';
            iconSvg = `<svg class="w-5 h-5 text-rose-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/></svg>`;
        } else {
            toast.className += ' bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200';
            iconSvg = `<svg class="w-5 h-5 text-brand-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2"><path stroke-linecap="round" stroke-linejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z"/></svg>`;
        }

        toast.innerHTML = `
            ${iconSvg}
            <span class="flex-1">${escapeHtml(message)}</span>
            <button type="button" class="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-1 rounded-lg transition" aria-label="Close">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
        `;

        const closeBtn = toast.querySelector('button');
        const dismiss = () => {
            toast.classList.remove('toast-in');
            toast.classList.add('toast-out');
            setTimeout(() => toast.remove(), 200);
        };

        closeBtn.addEventListener('click', dismiss);
        elements.toastContainer.appendChild(toast);
        setTimeout(dismiss, 3500);
    }

    // --- Theme Management ---
    function initTheme() {
        const savedTheme = localStorage.getItem('taskflow_theme');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        const isDark = savedTheme === 'dark' || (!savedTheme && prefersDark);

        if (isDark) {
            document.documentElement.classList.add('dark');
            elements.sunIcon.classList.remove('hidden');
            elements.moonIcon.classList.add('hidden');
        } else {
            document.documentElement.classList.remove('dark');
            elements.moonIcon.classList.remove('hidden');
            elements.sunIcon.classList.add('hidden');
        }
    }

    function toggleTheme() {
        const isDark = document.documentElement.classList.toggle('dark');
        localStorage.setItem('taskflow_theme', isDark ? 'dark' : 'light');
        if (isDark) {
            elements.sunIcon.classList.remove('hidden');
            elements.moonIcon.classList.add('hidden');
        } else {
            elements.moonIcon.classList.remove('hidden');
            elements.sunIcon.classList.add('hidden');
        }
    }

    // --- API Calls ---
    async function apiRequest(endpoint, options = {}) {
        try {
            const config = {
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                ...options
            };
            const response = await fetch(endpoint, config);

            if (response.status === 204) {
                return null;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || errorData.error || `HTTP error ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`API Error on ${endpoint}:`, error);
            throw error;
        }
    }

    async function loadTodos() {
        try {
            state.isLoading = true;
            const todos = await apiRequest('/api/todos');
            state.todos = todos || [];
            renderUI();
        } catch (error) {
            showToast(`Failed to load tasks: ${error.message}`, 'error');
        } finally {
            state.isLoading = false;
        }
    }

    async function refreshStats() {
        try {
            const stats = await apiRequest('/api/todos/stats');
            if (stats) {
                updateStatsUI(stats.total, stats.completed, stats.pending);
            }
        } catch (error) {
            // Recalculate locally if stats endpoint fails
            updateStatsLocally();
        }
    }

    // --- UI Stats Update ---
    function updateStatsUI(total, completed, pending) {
        const progress = total > 0 ? Math.round((completed / total) * 100) : 0;

        if (elements.statTotal) elements.statTotal.textContent = total;
        if (elements.statCompleted) elements.statCompleted.textContent = completed;
        if (elements.statPending) elements.statPending.textContent = pending;
        if (elements.progressText) elements.progressText.textContent = `${progress}%`;
        if (elements.progressBar) elements.progressBar.style.width = `${progress}%`;

        if (elements.tabCountAll) elements.tabCountAll.textContent = `(${total})`;
        if (elements.tabCountPending) elements.tabCountPending.textContent = `(${pending})`;
        if (elements.tabCountCompleted) elements.tabCountCompleted.textContent = `(${completed})`;
    }

    function updateStatsLocally() {
        const total = state.todos.length;
        const completed = state.todos.filter(t => t.completed).length;
        const pending = total - completed;
        updateStatsUI(total, completed, pending);
    }

    // --- Filtering & Searching ---
    function getFilteredTodos() {
        return state.todos.filter(todo => {
            // Status filter
            if (state.currentFilter === 'pending' && todo.completed) return false;
            if (state.currentFilter === 'completed' && !todo.completed) return false;

            // Search query
            if (state.searchQuery) {
                const query = state.searchQuery.toLowerCase();
                const matchTitle = todo.title && todo.title.toLowerCase().includes(query);
                const matchDesc = todo.description && todo.description.toLowerCase().includes(query);
                return matchTitle || matchDesc;
            }

            return true;
        });
    }

    // --- Render List of Todos ---
    function renderUI() {
        const filtered = getFilteredTodos();
        updateStatsLocally();

        if (filtered.length === 0) {
            elements.todoListContainer.innerHTML = '';
            elements.emptyStateContainer.classList.remove('hidden');
            return;
        }

        elements.emptyStateContainer.classList.add('hidden');
        elements.todoListContainer.innerHTML = filtered.map(todo => renderTodoCard(todo)).join('');
    }

    function renderTodoCard(todo) {
        const isCompleted = todo.completed === true;
        const completedClass = isCompleted ? 'line-through text-slate-400 dark:text-slate-500' : '';
        const descCompletedClass = isCompleted ? 'line-through opacity-70' : '';
        const checkboxClass = isCompleted
            ? 'bg-emerald-500 border-emerald-500 text-white'
            : 'border-slate-300 dark:border-slate-600 hover:border-brand-500 dark:hover:border-brand-400';
        const checkIconScale = isCompleted ? 'scale-100' : 'scale-0';

        const badgeHtml = isCompleted
            ? `<span class="badge-status inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-emerald-50 text-emerald-700 dark:bg-emerald-950/70 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800/60">Completed</span>`
            : `<span class="badge-status inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-amber-50 text-amber-700 dark:bg-amber-950/70 dark:text-amber-300 border border-amber-200 dark:border-amber-800/60">Pending</span>`;

        const dateFormatted = formatDate(todo.createdAt);
        const dateHtml = dateFormatted
            ? `<span class="text-[11px] text-slate-400 dark:text-slate-500">${dateFormatted}</span>`
            : '';

        const descHtml = todo.description
            ? `<p class="todo-description mt-1 text-xs sm:text-sm text-slate-500 dark:text-slate-400 break-words line-clamp-2 ${descCompletedClass}">${escapeHtml(todo.description)}</p>`
            : '';

        return `
            <div id="todo-item-${todo.id}"
                 data-id="${todo.id}"
                 data-completed="${isCompleted}"
                 data-title="${escapeHtml(todo.title)}"
                 data-description="${escapeHtml(todo.description || '')}"
                 class="todo-card group relative bg-white dark:bg-slate-900 rounded-2xl p-4 sm:p-5 border border-slate-200/80 dark:border-slate-800/80 shadow-sm hover:shadow-md transition-all flex items-start justify-between gap-4">
                
                <div class="flex items-start gap-3.5 flex-1 min-w-0">
                    <button type="button" 
                            data-action="toggle" 
                            data-id="${todo.id}" 
                            aria-checked="${isCompleted}"
                            class="todo-toggle-btn mt-0.5 w-5 h-5 rounded-lg border-2 flex items-center justify-center transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2 dark:focus:ring-offset-slate-900 ${checkboxClass}">
                        <svg class="w-3.5 h-3.5 transition-transform ${checkIconScale}" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                        </svg>
                    </button>

                    <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 flex-wrap">
                            <h3 class="todo-title text-sm sm:text-base font-semibold text-slate-900 dark:text-slate-100 transition-all break-words ${completedClass}">
                                ${escapeHtml(todo.title)}
                            </h3>
                            ${badgeHtml}
                            ${dateHtml}
                        </div>
                        ${descHtml}
                    </div>
                </div>

                <div class="flex items-center gap-1.5 shrink-0 opacity-90 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                    <button type="button" 
                            data-action="edit" 
                            data-id="${todo.id}" 
                            title="Edit task"
                            class="p-1.5 rounded-lg text-slate-500 hover:text-brand-600 dark:text-slate-400 dark:hover:text-brand-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                        </svg>
                    </button>
                    <button type="button" 
                            data-action="delete" 
                            data-id="${todo.id}" 
                            title="Delete task"
                            class="p-1.5 rounded-lg text-slate-500 hover:text-rose-600 dark:text-slate-400 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                        </svg>
                    </button>
                </div>
            </div>
        `;
    }

    // --- Action Handlers ---
    async function handleToggle(id) {
        try {
            const updated = await apiRequest(`/api/todos/${id}/toggle`, { method: 'PATCH' });
            const index = state.todos.findIndex(t => t.id === id);
            if (index !== -1) {
                state.todos[index] = updated;
                renderUI();
                showToast(
                    updated.completed ? 'Task marked as completed' : 'Task marked as pending',
                    'success'
                );
            }
        } catch (error) {
            showToast(`Could not update task: ${error.message}`, 'error');
        }
    }

    async function handleCreate(title, description, completed = false) {
        if (!title || !title.trim()) {
            showToast('Please enter a task title', 'error');
            return;
        }

        try {
            const newTodo = await apiRequest('/api/todos', {
                method: 'POST',
                body: JSON.stringify({
                    title: title.trim(),
                    description: description ? description.trim() : '',
                    completed: completed
                })
            });

            state.todos.push(newTodo);
            renderUI();
            showToast('Task created successfully', 'success');
            return newTodo;
        } catch (error) {
            showToast(`Could not create task: ${error.message}`, 'error');
        }
    }

    async function handleUpdate(id, title, description, completed) {
        if (!title || !title.trim()) {
            showToast('Task title cannot be empty', 'error');
            return;
        }

        try {
            const updated = await apiRequest(`/api/todos/${id}`, {
                method: 'PUT',
                body: JSON.stringify({
                    title: title.trim(),
                    description: description ? description.trim() : '',
                    completed: completed
                })
            });

            const index = state.todos.findIndex(t => t.id === id);
            if (index !== -1) {
                state.todos[index] = updated;
                renderUI();
                showToast('Task updated successfully', 'success');
            }
            return updated;
        } catch (error) {
            showToast(`Could not update task: ${error.message}`, 'error');
        }
    }

    async function handleDelete(id) {
        try {
            await apiRequest(`/api/todos/${id}`, { method: 'DELETE' });
            state.todos = state.todos.filter(t => t.id !== id);
            renderUI();
            showToast('Task deleted', 'info');
        } catch (error) {
            showToast(`Could not delete task: ${error.message}`, 'error');
        }
    }

    async function handleDeleteAll() {
        try {
            await apiRequest('/api/todos', { method: 'DELETE' });
            state.todos = [];
            renderUI();
            showToast('All tasks have been cleared', 'info');
        } catch (error) {
            showToast(`Could not clear tasks: ${error.message}`, 'error');
        }
    }

    // --- Modal Management ---
    function openModal(mode = 'create', todo = null) {
        if (mode === 'create') {
            elements.modalTitle.textContent = 'Create New Task';
            elements.modalTaskId.value = '';
            elements.modalTitleInput.value = '';
            elements.modalDescriptionInput.value = '';
            elements.modalCompletedInput.checked = false;
            elements.modalCompletedContainer.classList.add('hidden');
        } else if (mode === 'edit' && todo) {
            elements.modalTitle.textContent = 'Edit Task';
            elements.modalTaskId.value = todo.id;
            elements.modalTitleInput.value = todo.title;
            elements.modalDescriptionInput.value = todo.description || '';
            elements.modalCompletedInput.checked = todo.completed;
            elements.modalCompletedContainer.classList.remove('hidden');
        }

        elements.taskModal.classList.remove('hidden');
        document.body.classList.add('modal-open');
        setTimeout(() => {
            elements.taskModal.classList.remove('opacity-0');
            elements.modalDialog.classList.remove('scale-95');
            elements.modalDialog.classList.add('scale-100');
            elements.modalTitleInput.focus();
        }, 10);
    }

    function closeModal() {
        elements.taskModal.classList.add('opacity-0');
        elements.modalDialog.classList.remove('scale-100');
        elements.modalDialog.classList.add('scale-95');
        setTimeout(() => {
            elements.taskModal.classList.add('hidden');
            document.body.classList.remove('modal-open');
        }, 200);
    }

    function openDeleteModal(type, id = null, title = '') {
        state.pendingDelete = { type, id };
        if (type === 'all') {
            elements.deleteModalTitle.textContent = 'Clear All Tasks';
            elements.deleteModalMessage.textContent = 'Are you sure you want to delete all tasks? This cannot be undone.';
        } else {
            elements.deleteModalTitle.textContent = 'Delete Task';
            elements.deleteModalMessage.textContent = title
                ? `Are you sure you want to delete "${title}"?`
                : 'Are you sure you want to delete this task?';
        }

        elements.deleteModal.classList.remove('hidden');
        document.body.classList.add('modal-open');
        setTimeout(() => {
            elements.deleteModal.classList.remove('opacity-0');
            elements.deleteModalDialog.classList.remove('scale-95');
            elements.deleteModalDialog.classList.add('scale-100');
        }, 10);
    }

    function closeDeleteModal() {
        elements.deleteModal.classList.add('opacity-0');
        elements.deleteModalDialog.classList.remove('scale-100');
        elements.deleteModalDialog.classList.add('scale-95');
        setTimeout(() => {
            elements.deleteModal.classList.add('hidden');
            document.body.classList.remove('modal-open');
            state.pendingDelete = null;
        }, 200);
    }

    // --- Event Listeners Setup ---
    function setupEventListeners() {
        // Theme Toggle
        elements.themeToggleBtn.addEventListener('click', toggleTheme);

        // Quick Add Form
        elements.quickAddForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const title = elements.quickTitle.value;
            const desc = elements.quickDescription.value;
            const created = await handleCreate(title, desc, false);
            if (created) {
                elements.quickTitle.value = '';
                elements.quickDescription.value = '';
                elements.quickDescriptionContainer.classList.add('hidden');
                elements.quickTitle.focus();
            }
        });

        // Toggle Details inside Quick Add
        elements.toggleDescriptionBtn.addEventListener('click', () => {
            elements.quickDescriptionContainer.classList.toggle('hidden');
            if (!elements.quickDescriptionContainer.classList.contains('hidden')) {
                elements.quickDescription.focus();
            }
        });

        // Search Input
        let debounceTimer;
        elements.searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            const val = e.target.value;
            elements.clearSearchBtn.classList.toggle('hidden', !val);
            debounceTimer = setTimeout(() => {
                state.searchQuery = val;
                renderUI();
            }, 180);
        });

        elements.clearSearchBtn.addEventListener('click', () => {
            elements.searchInput.value = '';
            elements.clearSearchBtn.classList.add('hidden');
            state.searchQuery = '';
            renderUI();
            elements.searchInput.focus();
        });

        // Filter Tabs
        elements.filterTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                elements.filterTabs.forEach(t => {
                    t.classList.remove('text-brand-700', 'dark:text-brand-300', 'bg-white', 'dark:bg-slate-900', 'shadow-sm', 'font-semibold');
                    t.classList.add('text-slate-600', 'dark:text-slate-400', 'font-medium');
                    t.setAttribute('aria-selected', 'false');
                });
                tab.classList.remove('text-slate-600', 'dark:text-slate-400', 'font-medium');
                tab.classList.add('text-brand-700', 'dark:text-brand-300', 'bg-white', 'dark:bg-slate-900', 'shadow-sm', 'font-semibold');
                tab.setAttribute('aria-selected', 'true');

                state.currentFilter = tab.getAttribute('data-filter') || 'all';
                renderUI();
            });
        });

        // Todo List Item Click Delegation
        elements.todoListContainer.addEventListener('click', (e) => {
            const toggleBtn = e.target.closest('[data-action="toggle"]');
            if (toggleBtn) {
                const id = Number(toggleBtn.getAttribute('data-id'));
                handleToggle(id);
                return;
            }

            const editBtn = e.target.closest('[data-action="edit"]');
            if (editBtn) {
                const id = Number(editBtn.getAttribute('data-id'));
                const todo = state.todos.find(t => t.id === id);
                if (todo) openModal('edit', todo);
                return;
            }

            const deleteBtn = e.target.closest('[data-action="delete"]');
            if (deleteBtn) {
                const id = Number(deleteBtn.getAttribute('data-id'));
                const todo = state.todos.find(t => t.id === id);
                openDeleteModal('single', id, todo ? todo.title : '');
                return;
            }
        });

        // Modal Form Submit
        elements.taskForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = elements.modalTaskId.value ? Number(elements.modalTaskId.value) : null;
            const title = elements.modalTitleInput.value;
            const desc = elements.modalDescriptionInput.value;
            const completed = elements.modalCompletedInput.checked;

            if (id) {
                await handleUpdate(id, title, desc, completed);
            } else {
                await handleCreate(title, desc, false);
            }
            closeModal();
        });

        // Modal Triggers
        elements.openCreateModalBtn.addEventListener('click', () => openModal('create'));
        elements.emptyStateAddBtn.addEventListener('click', () => openModal('create'));
        elements.closeModalBtn.addEventListener('click', closeModal);
        elements.cancelModalBtn.addEventListener('click', closeModal);

        // Close on Backdrop Click
        elements.taskModal.addEventListener('click', (e) => {
            if (e.target === elements.taskModal) closeModal();
        });

        // Delete Modal Triggers
        elements.clearAllBtn.addEventListener('click', () => openDeleteModal('all'));
        elements.cancelDeleteBtn.addEventListener('click', closeDeleteModal);
        elements.confirmDeleteBtn.addEventListener('click', async () => {
            if (state.pendingDelete) {
                if (state.pendingDelete.type === 'all') {
                    await handleDeleteAll();
                } else if (state.pendingDelete.type === 'single' && state.pendingDelete.id) {
                    await handleDelete(state.pendingDelete.id);
                }
            }
            closeDeleteModal();
        });

        elements.deleteModal.addEventListener('click', (e) => {
            if (e.target === elements.deleteModal) closeDeleteModal();
        });

        // Keyboard Shortcuts
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                if (!elements.taskModal.classList.contains('hidden')) closeModal();
                if (!elements.deleteModal.classList.contains('hidden')) closeDeleteModal();
            }

            if ((e.key === '/' || ((e.metaKey || e.ctrlKey) && e.key === 'k')) &&
                document.activeElement !== elements.searchInput &&
                document.activeElement !== elements.quickTitle &&
                document.activeElement !== elements.quickDescription &&
                document.activeElement !== elements.modalTitleInput &&
                document.activeElement !== elements.modalDescriptionInput) {
                e.preventDefault();
                elements.searchInput.focus();
                elements.searchInput.select();
            }
        });
    }

    // --- Initialization ---
    async function init() {
        initTheme();
        setupEventListeners();
        await loadTodos();
    }

    // Run on DOM Ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
