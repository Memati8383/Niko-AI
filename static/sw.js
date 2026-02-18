/**
 * Niko AI Chat - Service Worker
 * Provides offline caching and improved performance
 */

const CACHE_NAME = 'niko-ai-cache-v1';
const STATIC_CACHE_NAME = 'niko-ai-static-v1';

// Static assets to cache immediately on install
const STATIC_ASSETS = [
    '/',
    '/index.html',
    '/login.html',
    '/signup.html',
    '/style.css',
    '/script.js',
    // External CDN resources for Prism.js
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-javascript.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-python.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-typescript.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-css.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-bash.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-sql.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-markup.min.js'
];

// API endpoints that should never be cached
const NO_CACHE_PATTERNS = [
    '/chat',
    '/login',
    '/logout',
    '/register',
    '/me',
    '/models',
    '/history',
    '/export'
];

/**
 * Install event - cache static assets
 */
self.addEventListener('install', (event) => {
    console.log('[Service Worker] Installing...');
    
    event.waitUntil(
        caches.open(STATIC_CACHE_NAME)
            .then((cache) => {
                console.log('[Service Worker] Caching static assets');
                return cache.addAll(STATIC_ASSETS);
            })
            .then(() => {
                console.log('[Service Worker] Static assets cached');
                // Skip waiting to activate immediately
                return self.skipWaiting();
            })
            .catch((error) => {
                console.error('[Service Worker] Cache failed:', error);
            })
    );
});

/**
 * Activate event - clean up old caches
 */
self.addEventListener('activate', (event) => {
    console.log('[Service Worker] Activating...');
    
    event.waitUntil(
        caches.keys()
            .then((cacheNames) => {
                return Promise.all(
                    cacheNames
                        .filter((cacheName) => {
                            // Delete old cache versions
                            return cacheName !== CACHE_NAME && 
                                   cacheName !== STATIC_CACHE_NAME;
                        })
                        .map((cacheName) => {
                            console.log('[Service Worker] Deleting old cache:', cacheName);
                            return caches.delete(cacheName);
                        })
                );
            })
            .then(() => {
                console.log('[Service Worker] Activated');
                // Take control of all clients immediately
                return self.clients.claim();
            })
    );
});

/**
 * Check if a URL should not be cached
 * @param {string} url - The URL to check
 * @returns {boolean} True if the URL should not be cached
 */
function shouldNotCache(url) {
    const urlObj = new URL(url);
    const pathname = urlObj.pathname;
    
    // Don't cache API endpoints
    return NO_CACHE_PATTERNS.some(pattern => pathname.startsWith(pattern));
}

/**
 * Fetch event - serve from cache or network
 */
self.addEventListener('fetch', (event) => {
    const request = event.request;
    const url = request.url;
    
    // Only handle GET requests
    if (request.method !== 'GET') {
        return;
    }
    
    // Don't cache API endpoints
    if (shouldNotCache(url)) {
        return;
    }
    
    event.respondWith(
        caches.match(request)
            .then((cachedResponse) => {
                // Return cached response if available
                if (cachedResponse) {
                    // Fetch in background to update cache (stale-while-revalidate)
                    fetchAndCache(request);
                    return cachedResponse;
                }
                
                // Otherwise fetch from network
                return fetchAndCache(request);
            })
            .catch((error) => {
                console.error('[Service Worker] Fetch failed:', error);
                
                // Return offline fallback for navigation requests
                if (request.mode === 'navigate') {
                    return caches.match('/index.html');
                }
                
                return new Response('Offline', {
                    status: 503,
                    statusText: 'Service Unavailable'
                });
            })
    );
});

/**
 * Fetch from network and update cache
 * @param {Request} request - The request to fetch
 * @returns {Promise<Response>} The response
 */
async function fetchAndCache(request) {
    try {
        const response = await fetch(request);
        
        // Only cache successful responses
        if (response.ok) {
            const cache = await caches.open(CACHE_NAME);
            // Clone the response since it can only be consumed once
            cache.put(request, response.clone());
        }
        
        return response;
    } catch (error) {
        // If network fails, try to return cached version
        const cachedResponse = await caches.match(request);
        if (cachedResponse) {
            return cachedResponse;
        }
        throw error;
    }
}

/**
 * Message event - handle messages from the main thread
 */
self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
    
    if (event.data && event.data.type === 'CLEAR_CACHE') {
        event.waitUntil(
            caches.keys().then((cacheNames) => {
                return Promise.all(
                    cacheNames.map((cacheName) => caches.delete(cacheName))
                );
            }).then(() => {
                console.log('[Service Worker] All caches cleared');
            })
        );
    }
});

console.log('[Service Worker] Script loaded');
