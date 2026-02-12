/**
 * Niko AI Chat - JavaScript
 * Main application script for the chat interface
 */

// ============================================================================
// Global State
// ============================================================================

const state = {
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    currentSessionId: null,
    isGenerating: false,
    abortController: null,
    webSearchEnabled: false,

    attachedImages: [],
    models: [],
    selectedModel: null,
    newProfileImage: null,
    profileImage: null
};

// ============================================================================
// DOM Elements
// ============================================================================

const elements = {
    // Sidebar
    sidebar: document.getElementById('sidebar'),
    menuToggle: document.getElementById('menuToggle'),
    newChatBtn: document.getElementById('newChatBtn'),
    historyList: document.getElementById('historyList'),
    clearAllBtn: document.getElementById('clearAllBtn'),
    
    // Chat
    chatMessages: document.getElementById('chatMessages'),
    welcomeMessage: document.getElementById('welcomeMessage'),
    messageInput: document.getElementById('messageInput'),
    sendBtn: document.getElementById('sendBtn'),
    modelSelector: document.getElementById('modelSelector'),
    
    // Search toggles
    webSearchBtn: document.getElementById('webSearchBtn'),

    
    // Images
    imageBtn: document.getElementById('imageBtn'),
    imageInput: document.getElementById('imageInput'),
    imagePreviewContainer: document.getElementById('imagePreviewContainer'),
    
    // Connection status
    connectionStatus: document.getElementById('connectionStatus'),
    
    // User
    userAvatar: document.getElementById('userAvatar'),
    
    // Profile Modal
    profileModal: document.getElementById('profileModal'),
    profileForm: document.getElementById('profileForm'),
    profilePicInput: document.getElementById('profilePicInput'),
    profilePicPreview: document.getElementById('profilePicPreview'),
    profileUsername: document.getElementById('profileUsername'),
    profileEmail: document.getElementById('profileEmail'),
    profileFullName: document.getElementById('profileFullName'),
    profileError: document.getElementById('profileError'),
    profileSuccess: document.getElementById('profileSuccess'),
    closeProfileBtn: document.getElementById('closeProfileBtn'),
    logoutBtn: document.getElementById('logoutBtn'),
    deleteAccBtn: document.getElementById('deleteAccBtn'),
    
    // Confirm Modal
    confirmModal: document.getElementById('confirmModal'),
    confirmIcon: document.getElementById('confirmIcon'),
    confirmTitle: document.getElementById('confirmTitle'),
    confirmDescription: document.getElementById('confirmDescription'),
    confirmCancelBtn: document.getElementById('confirmCancelBtn'),
    confirmOkBtn: document.getElementById('confirmOkBtn'),
    
    // Toast
    toastContainer: document.getElementById('toastContainer')
};

// ============================================================================
// Utility Functions
// ============================================================================

/**
 * Scroll chat messages to bottom
 * @param {boolean} smooth - Use smooth scrolling animation
 */
function scrollToBottom(smooth = true) {
    const chatMessages = elements.chatMessages;
    if (chatMessages) {
        chatMessages.scrollTo({
            top: chatMessages.scrollHeight,
            behavior: smooth ? 'smooth' : 'auto'
        });
    }
}

/**
 * Debounce function to limit function calls
 * @param {Function} func - Function to debounce
 * @param {number} wait - Wait time in milliseconds
 * @returns {Function} Debounced function
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Get authorization headers for API requests
 * @returns {Object} Headers object with Authorization
 */
function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${state.token}`
    };
}

/**
 * Check if user is authenticated
 * @returns {boolean} True if authenticated
 */
function isAuthenticated() {
    return !!state.token;
}

/**
 * Redirect to login page
 */
function redirectToLogin() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    window.location.href = '/login.html';
}

/**
 * Handle API errors
 * @param {Response} response - Fetch response object
 */
async function handleApiError(response) {
    if (response.status === 401) {
        showToast('Oturum süresi doldu. Lütfen tekrar giriş yapın.', 'error');
        setTimeout(redirectToLogin, 2000);
        return;
    }
    
    try {
        const data = await response.json();
        showToast(data.error || 'Bir hata oluştu', 'error');
    } catch {
        showToast('Bir hata oluştu', 'error');
    }
}

// ============================================================================
// Model Functions
// ============================================================================

/**
 * Fetch available models from Ollama
 */
async function fetchModels() {
    if (!isAuthenticated()) return;
    
    try {
        const response = await fetch('/models', {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            await handleApiError(response);
            return;
        }
        
        const data = await response.json();
        state.models = data.models || [];
        
        // Update model selector
        renderModelSelector();
        
        // Update connection status
        updateConnectionStatus(state.models.length > 0);
        
    } catch (error) {
        console.error('Error fetching models:', error);
        updateConnectionStatus(false);
        elements.modelSelector.innerHTML = '<option value="">Bağlantı hatası</option>';
    }
}

/**
 * Render model selector options
 */
function renderModelSelector() {
    const selector = elements.modelSelector;
    
    if (state.models.length === 0) {
        selector.innerHTML = '<option value="">Model bulunamadı</option>';
        return;
    }
    
    selector.innerHTML = state.models.map(model => 
        `<option value="${model}">${model}</option>`
    ).join('');
    
    // Set selected model
    if (state.selectedModel && state.models.includes(state.selectedModel)) {
        selector.value = state.selectedModel;
    } else {
        state.selectedModel = state.models[0];
    }
}

// ============================================================================
// Sidebar Functions
// ============================================================================

/**
 * Toggle sidebar visibility (for mobile)
 */
function toggleSidebar() {
    const sidebar = elements.sidebar;
    
    if (window.innerWidth <= 768) {
        // Mobile behavior
        sidebar.classList.toggle('open');
        
        // Toggle overlay
        let overlay = document.querySelector('.sidebar-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'sidebar-overlay';
            overlay.addEventListener('click', toggleSidebar);
            document.body.appendChild(overlay);
        }
        overlay.classList.toggle('active', sidebar.classList.contains('open'));
    } else {
        // Desktop behavior
        sidebar.classList.toggle('collapsed');
    }
}

/**
 * Start a new chat session
 */
function startNewChat() {
    // Reset current session
    state.currentSessionId = null;
    
    // Clear messages
    elements.chatMessages.innerHTML = '';
    
    // Show welcome message
    elements.welcomeMessage.style.display = 'flex';
    elements.chatMessages.appendChild(elements.welcomeMessage);
    
    // Clear input
    elements.messageInput.value = '';
    clearAttachedImages();
    
    // Update send button state
    updateSendButtonState();
    
    // Clear draft
    localStorage.removeItem('messageDraft');
    
    // Close sidebar on mobile
    if (window.innerWidth <= 768) {
        elements.sidebar.classList.remove('open');
        const overlay = document.querySelector('.sidebar-overlay');
        if (overlay) overlay.classList.remove('active');
    }
    
    // Refresh history to update active state
    fetchHistory();
}

// ============================================================================
// Connection Status
// ============================================================================

/**
 * Update connection status indicator
 * @param {boolean} connected - Whether connected to Ollama
 */
function updateConnectionStatus(connected) {
    const statusDot = elements.connectionStatus.querySelector('.status-dot');
    const statusText = elements.connectionStatus.querySelector('.status-text');
    
    if (connected) {
        statusDot.classList.remove('offline');
        statusText.textContent = 'Bağlı';
    } else {
        statusDot.classList.add('offline');
        statusText.textContent = 'Bağlantı yok';
    }
}

// ============================================================================
// Toast Notifications
// ============================================================================

/**
 * Show a toast notification
 * @param {string} message - Message to display
 * @param {string} type - Toast type: 'success', 'error', 'warning', 'info'
 * @param {number} duration - Duration in milliseconds
 */
function showToast(message, type = 'info', duration = 3000) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    }[type] || 'ℹ';
    
    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    
    elements.toastContainer.appendChild(toast);
    
    // Remove toast after duration
    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// ============================================================================
// Confirm Modal
// ============================================================================

/**
 * Show confirmation modal
 * @param {string} title - Modal title
 * @param {string} description - Modal description
 * @param {string} icon - Optional icon emoji
 * @returns {Promise<boolean>} Resolves to true if confirmed, false if cancelled
 */
function showConfirmModal(title, description, icon = '') {
    return new Promise((resolve) => {
        elements.confirmTitle.textContent = title;
        elements.confirmDescription.textContent = description;
        
        if (icon) {
            elements.confirmIcon.textContent = icon;
            elements.confirmIcon.style.display = 'block';
        } else {
            elements.confirmIcon.style.display = 'none';
        }

        elements.confirmModal.classList.add('active');
        
        const handleConfirm = () => {
            cleanup();
            resolve(true);
        };
        
        const handleCancel = () => {
            cleanup();
            resolve(false);
        };
        
        const cleanup = () => {
            elements.confirmModal.classList.remove('active');
            elements.confirmOkBtn.removeEventListener('click', handleConfirm);
            elements.confirmCancelBtn.removeEventListener('click', handleCancel);
        };
        
        elements.confirmOkBtn.addEventListener('click', handleConfirm);
        elements.confirmCancelBtn.addEventListener('click', handleCancel);
    });
}

// ============================================================================
// Image Handling
// ============================================================================

/**
 * Clear all attached images
 */
function clearAttachedImages() {
    state.attachedImages = [];
    elements.imagePreviewContainer.innerHTML = '';
}

// ============================================================================
// Send Button State
// ============================================================================

/**
 * Update send button enabled/disabled state
 */
function updateSendButtonState() {
    const hasMessage = elements.messageInput.value.trim().length > 0;
    const hasImages = state.attachedImages.length > 0;
    elements.sendBtn.disabled = (!hasMessage && !hasImages) || state.isGenerating;
}

// ============================================================================
// Initialization
// ============================================================================

/**
 * Initialize the application
 */
async function init() {
    // Check authentication
    if (!isAuthenticated()) {
        redirectToLogin();
        return;
    }
    
    // Set up event listeners
    setupEventListeners();
    
    // Load user profile
    loadUserProfile();
    
    // Fetch models
    await fetchModels();
    
    // Fetch history
    await fetchHistory();
    
    // Load draft if exists
    loadDraft();
    
    // Focus input
    elements.messageInput.focus();
}

/**
 * Set up event listeners
 */
function setupEventListeners() {
    // Sidebar toggle
    elements.menuToggle.addEventListener('click', toggleSidebar);
    
    // New chat
    elements.newChatBtn.addEventListener('click', startNewChat);
    
    // Clear all history
    elements.clearAllBtn.addEventListener('click', clearAllHistory);
    
    // Model selector
    elements.modelSelector.addEventListener('change', (e) => {
        state.selectedModel = e.target.value;
    });
    
    // Message input
    elements.messageInput.addEventListener('input', () => {
        updateSendButtonState();
        autoResizeTextarea();
        saveDraft();
    });
    
    elements.messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            if (!elements.sendBtn.disabled) {
                sendMessage();
            }
        }
    });
    
    // Send button
    elements.sendBtn.addEventListener('click', sendMessage);
    
    // Search toggles
    elements.webSearchBtn.addEventListener('click', () => {
        state.webSearchEnabled = !state.webSearchEnabled;
        elements.webSearchBtn.classList.toggle('active', state.webSearchEnabled);
    });
    

    
    // Image upload
    elements.imageBtn.addEventListener('click', () => {
        elements.imageInput.click();
    });
    
    elements.imageInput.addEventListener('change', handleImageSelect);
    
    // Profile
    elements.userAvatar.addEventListener('click', openProfileModal);
    
    // Profile picture upload
    const profilePicWrapper = document.querySelector('.profile-pic-wrapper');
    if (profilePicWrapper) {
        profilePicWrapper.addEventListener('click', () => {
            elements.profilePicInput.click();
        });
    }
    
    elements.profilePicInput.addEventListener('change', handleProfilePicSelect);
    
    elements.closeProfileBtn.addEventListener('click', closeProfileModal);
    elements.profileForm.addEventListener('submit', saveUserProfile);
    elements.logoutBtn.addEventListener('click', logout);
    elements.deleteAccBtn.addEventListener('click', deleteAccount);
    
    // Close modals on overlay click
    elements.profileModal.addEventListener('click', (e) => {
        if (e.target === elements.profileModal) {
            closeProfileModal();
        }
    });
    
    elements.confirmModal.addEventListener('click', (e) => {
        if (e.target === elements.confirmModal) {
            elements.confirmModal.classList.remove('active');
        }
    });
    
    // Drag and drop for images
    setupDragAndDrop();
    
    // Handle Window Resize
    window.addEventListener('resize', () => {
        if (window.innerWidth > 768) {
            elements.sidebar.classList.remove('open');
            const overlay = document.querySelector('.sidebar-overlay');
            if (overlay) overlay.classList.remove('active');
        }
    });
    
    // Event delegation for code copy buttons in chat messages
    elements.chatMessages.addEventListener('click', (e) => {
        if (e.target.classList.contains('code-copy-btn')) {
            copyCodeFromButton(e.target);
        }
    });
}

/**
 * Auto-resize textarea based on content
 */
function autoResizeTextarea() {
    const textarea = elements.messageInput;
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
}

/**
 * Set up drag and drop for image upload
 */
function setupDragAndDrop() {
    const dropZone = document.querySelector('.chat-input-box');
    
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.style.borderColor = 'var(--accent-primary)';
    });
    
    dropZone.addEventListener('dragleave', () => {
        dropZone.style.borderColor = '';
    });
    
    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.style.borderColor = '';
        
        const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'));
        if (files.length > 0) {
            handleFiles(files);
        }
    });
}

/**
 * Handle image file selection
 */
function handleImageSelect(e) {
    const files = Array.from(e.target.files);
    handleFiles(files);
    e.target.value = ''; // Reset input
}

/**
 * Handle profile picture selection
 * @param {Event} e - Change event
 */
async function handleProfilePicSelect(e) {
    const file = e.target.files[0];
    if (!file || !file.type.startsWith('image/')) return;
    
    try {
        const dataUrl = await resizeImage(file, 400); 
        state.newProfileImage = dataUrl;
        elements.profilePicPreview.src = dataUrl;
    } catch (error) {
        console.error('Error processing profile picture:', error);
        showToast('Profil fotoğrafı işlenirken hata oluştu', 'error');
    }
}

/**
 * Handle image files
 * @param {File[]} files - Array of image files
 */
async function handleFiles(files) {
    for (const file of files) {
        if (!file.type.startsWith('image/')) continue;
        
        try {
            const dataUrl = await resizeImage(file);
            const base64 = dataUrl.split(',')[1];
            state.attachedImages.push(base64);
            addImagePreview(base64);
            updateSendButtonState();
        } catch (error) {
            console.error('Error processing image:', error);
            showToast('Görsel işlenirken hata oluştu', 'error');
        }
    }
}

/**
 * Resize image and convert to base64
 * @param {File} file - Image file
 * @returns {Promise<string>} Base64 encoded image
 */
function resizeImage(file, maxSize = 800) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                const canvas = document.createElement('canvas');
                let width = img.width;
                let height = img.height;
                
                if (width > height && width > maxSize) {
                    height = (height * maxSize) / width;
                    width = maxSize;
                } else if (height > maxSize) {
                    width = (width * maxSize) / height;
                    height = maxSize;
                }
                
                canvas.width = width;
                canvas.height = height;
                
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, width, height);
                
                const dataUrl = canvas.toDataURL('image/jpeg', 0.8);
                resolve(dataUrl);
            };
            img.onerror = reject;
            img.src = e.target.result;
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

/**
 * Add image preview to the container
 * @param {string} base64 - Base64 encoded image
 */
function addImagePreview(base64) {
    const preview = document.createElement('div');
    preview.className = 'image-preview';
    preview.innerHTML = `
        <img src="data:image/jpeg;base64,${base64}" alt="Preview">
        <button class="image-preview-remove" title="Kaldır">×</button>
    `;
    
    preview.querySelector('.image-preview-remove').addEventListener('click', () => {
        const index = state.attachedImages.indexOf(base64);
        if (index > -1) {
            state.attachedImages.splice(index, 1);
        }
        preview.remove();
        updateSendButtonState();
    });
    
    elements.imagePreviewContainer.appendChild(preview);
}

/**
 * Save message draft to localStorage
 */
const saveDraft = debounce(() => {
    const message = elements.messageInput.value;
    if (message.trim()) {
        localStorage.setItem('messageDraft', message);
    } else {
        localStorage.removeItem('messageDraft');
    }
}, 500);

/**
 * Load message draft from localStorage
 */
function loadDraft() {
    const draft = localStorage.getItem('messageDraft');
    if (draft) {
        elements.messageInput.value = draft;
        autoResizeTextarea();
        updateSendButtonState();
    }
}

/**
 * Logout user
 */
async function logout() {
    try {
        await fetch('/logout', {
            method: 'POST',
            headers: getAuthHeaders()
        });
    } catch (error) {
        console.error('Logout error:', error);
    }
    
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('messageDraft');
    window.location.href = '/login.html';
}

// ============================================================================
// Service Worker Registration
// ============================================================================

/**
 * Register the service worker for offline caching
 */
async function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        try {
            const registration = await navigator.serviceWorker.register('/sw.js', {
                scope: '/'
            });
            
            console.log('[App] Service Worker registered:', registration.scope);
            
            // Handle updates
            registration.addEventListener('updatefound', () => {
                const newWorker = registration.installing;
                console.log('[App] Service Worker update found');
                
                newWorker.addEventListener('statechange', () => {
                    if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                        // New service worker available, show update notification
                        showToast('Yeni sürüm mevcut. Sayfayı yenileyiniz.', 'info', 5000);
                    }
                });
            });
            
        } catch (error) {
            console.error('[App] Service Worker registration failed:', error);
        }
    } else {
        console.log('[App] Service Workers not supported');
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    init();
    registerServiceWorker();
});


// ============================================================================
// Messaging Functions
// Requirements: 8.3, 8.4
// ============================================================================

/**
 * Append a message to the chat
 * @param {string} role - 'user' or 'bot'
 * @param {string} content - Message content
 * @param {string[]} images - Optional array of base64 images (for user messages)
 * @returns {HTMLElement} The message element
 */
function appendMessage(role, content, images = []) {
    // Hide welcome message
    if (elements.welcomeMessage) {
        elements.welcomeMessage.style.display = 'none';
    }
    
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;
    
    let avatar = role === 'user' ? '👤' : '🤖';
    if (role === 'user' && state.profileImage) {
        avatar = `<img src="${state.profileImage}" alt="${state.username}">`;
    }
    
    messageDiv.innerHTML = `
        <div class="message-avatar">${avatar}</div>
        <div class="message-content">
            <div class="message-text"></div>
            <div class="message-actions">
                <button class="message-action-btn copy-btn" title="Kopyala">📋 Kopyala</button>
            </div>
        </div>
    `;
    
    const messageText = messageDiv.querySelector('.message-text');
    
    // Add images if present (for user messages)
    if (images.length > 0) {
        const imagesHtml = images.map(img => 
            `<img src="data:image/jpeg;base64,${img}" alt="Attached image" style="max-width: 200px; border-radius: var(--radius-md); margin-bottom: var(--spacing-sm);">`
        ).join('');
        messageText.innerHTML = imagesHtml + '<br>';
    }
    
    // Set content
    if (role === 'bot') {
        // For bot messages, we'll update content progressively
        messageText.innerHTML += '';
    } else {
        // For user messages, escape HTML and preserve line breaks
        messageText.innerHTML += escapeHtml(content).replace(/\n/g, '<br>');
    }
    
    // Add copy functionality
    const copyBtn = messageDiv.querySelector('.copy-btn');
    copyBtn.addEventListener('click', () => {
        copyToClipboard(content);
    });
    
    elements.chatMessages.appendChild(messageDiv);
    scrollToBottom();
    
    return messageDiv;
}

/**
 * Escape HTML special characters
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Copy text to clipboard
 * @param {string} text - Text to copy
 */
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        showToast('Kopyalandı!', 'success');
    } catch (error) {
        console.error('Copy failed:', error);
        showToast('Kopyalama başarısız', 'error');
    }
}

/**
 * Parse and render markdown content with syntax highlighting
 * @param {string} content - Markdown content
 * @returns {string} HTML content
 */
function parseMarkdown(content) {
    // Store code blocks temporarily to prevent escaping
    const codeBlocks = [];
    let processedContent = content;
    
    // Extract code blocks first (```code```)
    processedContent = processedContent.replace(/```(\w*)\n([\s\S]*?)```/g, (match, lang, code) => {
        const index = codeBlocks.length;
        const trimmedCode = code.trim();
        const language = lang || 'plaintext';
        codeBlocks.push({ code: trimmedCode, language });
        return `__CODE_BLOCK_${index}__`;
    });
    
    // Escape HTML for the rest
    let html = escapeHtml(processedContent);
    
    // Restore code blocks with syntax highlighting
    codeBlocks.forEach((block, index) => {
        const escapedCode = escapeHtml(block.code);
        const highlightedCode = highlightCode(escapedCode, block.language);
        const codeBlockHtml = `<pre class="code-block" data-language="${block.language}"><code class="language-${block.language}">${highlightedCode}</code><button class="code-copy-btn" data-code="${encodeURIComponent(block.code)}" title="Kodu Kopyala">📋 Kopyala</button></pre>`;
        html = html.replace(`__CODE_BLOCK_${index}__`, codeBlockHtml);
    });
    
    // Inline code (`code`)
    html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');
    
    // Bold (**text**)
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    
    // Italic (*text*)
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    
    // Headers
    html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
    html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
    html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');
    
    // Lists
    html = html.replace(/^\* (.+)$/gm, '<li>$1</li>');
    html = html.replace(/^- (.+)$/gm, '<li>$1</li>');
    html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>');
    
    // Wrap consecutive list items
    html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>');
    
    // Links [text](url)
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');
    
    // Blockquotes
    html = html.replace(/^&gt; (.+)$/gm, '<blockquote>$1</blockquote>');
    
    // Line breaks
    html = html.replace(/\n/g, '<br>');
    
    // Clean up multiple <br> tags
    html = html.replace(/(<br>){3,}/g, '<br><br>');
    
    return html;
}

/**
 * Highlight code using Prism.js if available
 * @param {string} code - Code to highlight
 * @param {string} language - Programming language
 * @returns {string} Highlighted HTML
 */
function highlightCode(code, language) {
    // Map common language aliases
    const languageMap = {
        'js': 'javascript',
        'ts': 'typescript',
        'py': 'python',
        'sh': 'bash',
        'shell': 'bash',
        'html': 'markup',
        'xml': 'markup'
    };
    
    const mappedLang = languageMap[language] || language;
    
    // Use Prism.js if available
    if (typeof Prism !== 'undefined' && Prism.languages[mappedLang]) {
        try {
            return Prism.highlight(code, Prism.languages[mappedLang], mappedLang);
        } catch (e) {
            console.warn('Prism highlighting failed:', e);
            return code;
        }
    }
    
    return code;
}

/**
 * Copy code from code block button
 * @param {HTMLElement} button - The copy button element
 */
function copyCodeFromButton(button) {
    const code = decodeURIComponent(button.dataset.code);
    copyToClipboard(code);
}

/**
 * Send a message to the AI
 */
async function sendMessage() {
    const message = elements.messageInput.value.trim();
    const images = [...state.attachedImages];
    
    if (!message && images.length === 0) return;
    if (state.isGenerating) return;
    
    // Clear input and images
    elements.messageInput.value = '';
    clearAttachedImages();
    autoResizeTextarea();
    updateSendButtonState();
    localStorage.removeItem('messageDraft');
    
    // Add user message to chat
    appendMessage('user', message, images);
    
    // Set generating state
    state.isGenerating = true;
    state.abortController = new AbortController();
    
    // Change send button to stop button
    elements.sendBtn.innerHTML = '⬛';
    elements.sendBtn.classList.add('stop-btn');
    elements.sendBtn.disabled = false;
    elements.sendBtn.onclick = stopGeneration;
    
    // Add bot message placeholder with typing indicator
    const botMessage = appendMessage('bot', '');
    const messageText = botMessage.querySelector('.message-text');
    messageText.innerHTML = '<div class="typing-indicator"><span></span><span></span><span></span></div>';
    
    let fullResponse = '';
    
    try {
        const response = await fetch('/chat', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                message: message,
                session_id: state.currentSessionId,
                model: state.selectedModel,
                web_search: state.webSearchEnabled,

                images: images.length > 0 ? images : null
            }),
            signal: state.abortController.signal
        });
        
        if (!response.ok) {
            await handleApiError(response);
            messageText.innerHTML = '<span style="color: var(--error);">Bir hata oluştu. Lütfen tekrar deneyin.</span>';
            return;
        }
        
        // Handle streaming response
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        
        // Clear typing indicator
        messageText.innerHTML = '';
        
        while (true) {
            const { done, value } = await reader.read();
            
            if (done) break;
            
            const chunk = decoder.decode(value, { stream: true });
            const lines = chunk.split('\n');
            
            for (const line of lines) {
                if (line.startsWith('data: ')) {
                    try {
                        const data = JSON.parse(line.slice(6));
                        
                        if (data.type === 'session_id') {
                            state.currentSessionId = data.session_id;
                        } else if (data.type === 'content') {
                            fullResponse += data.content;
                            messageText.innerHTML = parseMarkdown(fullResponse);
                            scrollToBottom();
                        } else if (data.type === 'done') {
                            // Response complete
                            fetchHistory(); // Refresh history
                        }
                    } catch (e) {
                        // Ignore parse errors for incomplete chunks
                    }
                }
            }
        }
        
    } catch (error) {
        if (error.name === 'AbortError') {
            // User stopped generation
            if (fullResponse) {
                messageText.innerHTML = parseMarkdown(fullResponse) + '<br><span style="color: var(--warning);">[Durduruldu]</span>';
            } else {
                messageText.innerHTML = '<span style="color: var(--warning);">Yanıt durduruldu.</span>';
            }
        } else {
            console.error('Chat error:', error);
            messageText.innerHTML = '<span style="color: var(--error);">Bağlantı hatası. Lütfen tekrar deneyin.</span>';
        }
    } finally {
        // Reset state
        state.isGenerating = false;
        state.abortController = null;
        
        // Reset send button
        elements.sendBtn.innerHTML = '➤';
        elements.sendBtn.classList.remove('stop-btn');
        elements.sendBtn.onclick = sendMessage;
        updateSendButtonState();
        
        scrollToBottom();
    }
}

/**
 * Stop the current generation
 */
function stopGeneration() {
    if (state.abortController) {
        state.abortController.abort();
    }
}


// ============================================================================
// History Functions
// Requirements: 8.2
// ============================================================================

/**
 * Fetch chat history from the server
 */
async function fetchHistory() {
    if (!isAuthenticated()) return;
    
    try {
        const response = await fetch('/history', {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            await handleApiError(response);
            return;
        }
        
        const data = await response.json();
        renderHistory(data.sessions || []);
        
    } catch (error) {
        console.error('Error fetching history:', error);
        showToast('Geçmiş yüklenirken hata oluştu', 'error');
    }
}

/**
 * Render history list in sidebar
 * @param {Array} sessions - Array of session objects
 */
function renderHistory(sessions) {
    const historyList = elements.historyList;
    
    if (sessions.length === 0) {
        historyList.innerHTML = '<div class="empty-history">Henüz sohbet geçmişi yok</div>';
        return;
    }
    
    historyList.innerHTML = sessions.map(session => `
        <div class="history-item ${session.id === state.currentSessionId ? 'active' : ''}" data-session-id="${session.id}">
            <span class="history-item-title" title="${escapeHtml(session.title)}">${escapeHtml(session.title)}</span>
            <div class="history-item-actions">
                <button class="history-action-btn export-btn" title="Dışa Aktar" data-session-id="${session.id}">📥</button>
                <button class="history-action-btn delete-btn" title="Sil" data-session-id="${session.id}">🗑️</button>
            </div>
        </div>
    `).join('');
    
    // Add click handlers
    historyList.querySelectorAll('.history-item').forEach(item => {
        item.addEventListener('click', (e) => {
            // Don't trigger if clicking on action buttons
            if (e.target.closest('.history-item-actions')) return;
            
            const sessionId = item.dataset.sessionId;
            loadHistoryItem(sessionId);
        });
    });
    
    // Add export handlers
    historyList.querySelectorAll('.export-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const sessionId = btn.dataset.sessionId;
            exportHistoryItem(sessionId);
        });
    });
    
    // Add delete handlers
    historyList.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const sessionId = btn.dataset.sessionId;
            deleteHistoryItem(sessionId);
        });
    });
}

/**
 * Load a history item and display its messages
 * @param {string} sessionId - Session ID to load
 */
async function loadHistoryItem(sessionId) {
    if (!isAuthenticated()) return;
    
    try {
        const response = await fetch(`/history/${sessionId}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            await handleApiError(response);
            return;
        }
        
        const session = await response.json();
        
        // Update current session
        state.currentSessionId = sessionId;
        
        // Clear chat messages
        elements.chatMessages.innerHTML = '';
        
        // Hide welcome message
        if (elements.welcomeMessage) {
            elements.welcomeMessage.style.display = 'none';
        }
        
        // Render messages
        for (const msg of session.messages) {
            const messageDiv = appendMessage(msg.role === 'user' ? 'user' : 'bot', msg.content);
            
            // For bot messages, parse markdown
            if (msg.role !== 'user') {
                const messageText = messageDiv.querySelector('.message-text');
                messageText.innerHTML = parseMarkdown(msg.content);
            }
        }
        
        // Update active state in history list
        elements.historyList.querySelectorAll('.history-item').forEach(item => {
            item.classList.toggle('active', item.dataset.sessionId === sessionId);
        });
        
        // Close sidebar on mobile
        if (window.innerWidth <= 768) {
            elements.sidebar.classList.remove('open');
            const overlay = document.querySelector('.sidebar-overlay');
            if (overlay) overlay.classList.remove('active');
        }
        
        scrollToBottom(false);
        
    } catch (error) {
        console.error('Error loading history item:', error);
        showToast('Sohbet yüklenirken hata oluştu', 'error');
    }
}

/**
 * Delete a history item
 * @param {string} sessionId - Session ID to delete
 */
async function deleteHistoryItem(sessionId) {
    const confirmed = await showConfirmModal(
        'Sohbeti Sil',
        'Bu sohbeti silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.'
    );
    
    if (!confirmed) return;
    
    try {
        const response = await fetch(`/history/${sessionId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            await handleApiError(response);
            return;
        }
        
        showToast('Sohbet silindi', 'success');
        
        // If deleted current session, start new chat
        if (sessionId === state.currentSessionId) {
            startNewChat();
        } else {
            fetchHistory();
        }
        
    } catch (error) {
        console.error('Error deleting history item:', error);
        showToast('Sohbet silinirken hata oluştu', 'error');
    }
}

/**
 * Clear all chat history
 */
async function clearAllHistory() {
    const confirmed = await showConfirmModal(
        'Tüm Geçmişi Temizle',
        'Tüm sohbet geçmişinizi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.'
    );
    
    if (!confirmed) return;
    
    try {
        const response = await fetch('/history', {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            await handleApiError(response);
            return;
        }
        
        const data = await response.json();
        showToast(data.message || 'Tüm geçmiş silindi', 'success');
        
        // Start new chat
        startNewChat();
        
    } catch (error) {
        console.error('Error clearing history:', error);
        showToast('Geçmiş temizlenirken hata oluştu', 'error');
    }
}

/**
 * Export a history item as Markdown
 * @param {string} sessionId - Session ID to export
 */
async function exportHistoryItem(sessionId) {
    if (!isAuthenticated()) return;
    
    try {
        const response = await fetch(`/export/${sessionId}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            await handleApiError(response);
            return;
        }
        
        const markdown = await response.text();
        
        // Create download link
        const blob = new Blob([markdown], { type: 'text/markdown' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `chat_${sessionId}.md`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        showToast('Sohbet dışa aktarıldı', 'success');
        
    } catch (error) {
        console.error('Error exporting history item:', error);
        showToast('Dışa aktarma başarısız', 'error');
    }
}


// ============================================================================
// Profile Functions
// Requirements: 2.6, 2.7
// ============================================================================

/**
 * Load user profile from the server
 */
async function loadUserProfile() {
    if (!isAuthenticated()) return;
    
    try {
        const response = await fetch('/me', {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            await handleApiError(response);
            return;
        }
        
        const profile = await response.json();
        
        // Update state
        state.username = profile.username;
        state.profileImage = profile.profile_image;
        localStorage.setItem('username', profile.username);
        
        // Update avatar
        if (profile.profile_image) {
            elements.userAvatar.innerHTML = `<img src="${profile.profile_image}" class="nav-profile-pic" alt="${profile.username}">`;
            elements.profilePicPreview.src = profile.profile_image;
        } else {
            const initial = profile.username.charAt(0).toUpperCase();
            elements.userAvatar.innerHTML = initial;
            elements.userAvatar.title = profile.username;
        }
        
        // Update profile form
        elements.profileUsername.value = profile.username;
        elements.profileEmail.value = profile.email || '';
        elements.profileFullName.value = profile.full_name || '';
        
        // Show admin panel link if user is admin
        const adminPanelSection = document.getElementById('adminPanelSection');
        if (adminPanelSection && profile.is_admin) {
            adminPanelSection.style.display = 'block';
        }
        
    } catch (error) {
        console.error('Error loading profile:', error);
    }
}

/**
 * Save user profile to the server
 * @param {Event} e - Form submit event
 */
async function saveUserProfile(e) {
    e.preventDefault();
    
    // Hide previous messages
    hideProfileMessages();
    
    const email = elements.profileEmail.value.trim();
    const fullName = elements.profileFullName.value.trim();
    
    try {
        const response = await fetch('/me', {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                email: email || null,
                full_name: fullName || null,
                profile_image: state.newProfileImage || null
            })
        });
        
        if (!response.ok) {
            const data = await response.json();
            showProfileError(data.error || data.detail || 'Profil güncellenemedi');
            return;
        }
        
        showProfileSuccess('Profil güncellendi');
        
        // Reload profile to get updated data
        await loadUserProfile();
        
        // Close modal after success
        setTimeout(closeProfileModal, 1500);
        
    } catch (error) {
        console.error('Error saving profile:', error);
        showProfileError('Bağlantı hatası');
    }
}

/**
 * Show profile error message
 * @param {string} message - Error message
 */
function showProfileError(message) {
    elements.profileError.textContent = message;
    elements.profileError.style.display = 'block';
    elements.profileSuccess.style.display = 'none';
}

/**
 * Show profile success message
 * @param {string} message - Success message
 */
function showProfileSuccess(message) {
    elements.profileSuccess.textContent = message;
    elements.profileSuccess.style.display = 'block';
    elements.profileError.style.display = 'none';
    
    // Auto-hide after 3 seconds
    setTimeout(() => {
        elements.profileSuccess.style.display = 'none';
    }, 3000);
}

/**
 * Hide profile messages
 */
function hideProfileMessages() {
    elements.profileError.style.display = 'none';
    elements.profileSuccess.style.display = 'none';
}

/**
 * Delete current user account
 */
async function deleteAccount() {
    const confirmed = await showConfirmModal(
        'Hesabımı Sil',
        'Hesabınızı silmek istediğinizden emin misiniz? Hesabınız 30 gün boyunca askıya alınacak ve bu süre sonunda kalıcı olarak silinecektir. 30 gün içinde tekrar giriş yaparak işlemi iptal edebilirsiniz.',
        '⚠️'
    );
    
    if (!confirmed) return;
    
    try {
        const response = await fetch('/me', {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showToast('Hesabınız silme için işaretlendi. 30 gün vaktiniz var.', 'info', 5000);
            setTimeout(() => {
                localStorage.clear();
                window.location.href = '/login.html';
            }, 3000);
        } else {
            showToast(data.error || 'İşlem başarısız', 'error');
        }
    } catch (error) {
        console.error('Delete account error:', error);
        showToast('Bağlantı hatası', 'error');
    }
}

/**
 * Open profile modal
 */
function openProfileModal() {
    hideProfileMessages();
    elements.profileModal.classList.add('active');
}

/**
 * Close profile modal
 */
function closeProfileModal() {
    elements.profileModal.classList.remove('active');
}

// Social Media FAB Logic
document.addEventListener('DOMContentLoaded', () => {
    const fabToggle = document.getElementById('socialFabToggle');
    const fabContainer = document.getElementById('socialFabContainer');
    
    if (fabToggle && fabContainer) {
        fabToggle.addEventListener('click', () => {
            fabContainer.classList.toggle('active');
            fabToggle.classList.toggle('active');
        });
        
        // Close when clicking outside
        document.addEventListener('click', (e) => {
            if (!fabContainer.contains(e.target) && fabContainer.classList.contains('active')) {
                fabContainer.classList.remove('active');
                fabToggle.classList.remove('active');
            }
        });
    }
});

