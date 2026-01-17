import os
import json
import webbrowser
import uvicorn
import secrets
from typing import Optional, Dict
from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import bcrypt

# --- Yapılandırma ---
USERS_FILE = "users.json"
HOST = "127.0.0.1"
PORT = 8085  # Ana uygulamayla çakışmayı önlemek için farklı port kullanılıyor

app = FastAPI(title="Niko Kullanıcı Yöneticisi")

# CORS'u etkinleştir
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Modeller ---
class UserUpdate(BaseModel):
    email: Optional[str] = None
    full_name: Optional[str] = None
    password: Optional[str] = None
    is_admin: Optional[bool] = None

# --- Yardımcı Fonksiyonlar ---
def load_users():
    if not os.path.exists(USERS_FILE):
        return {}
    try:
        with open(USERS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Kullanıcılar yüklenirken hata: {e}")
        return {}

def save_users(users):
    try:
        with open(USERS_FILE, 'w', encoding='utf-8') as f:
            json.dump(users, f, indent=2, ensure_ascii=False)
        return True
    except Exception as e:
        print(f"Kullanıcılar kaydedilirken hata: {e}")
        return False

def hash_password(password: str) -> str:
    password_bytes = password.encode('utf-8')
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password_bytes, salt)
    return hashed.decode('utf-8')

# --- HTML İçeriği ---
HTML_CONTENT = """
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Niko User Management System</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body {
            font-family: 'Outfit', sans-serif;
            background-color: #0f172a;
            color: #e2e8f0;
            overflow-x: hidden;
        }
        .glass-panel {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.08);
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        }
        .glass-card {
            background: rgba(30, 41, 59, 0.4);
            border: 1px solid rgba(255, 255, 255, 0.05);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .glass-card:hover {
            transform: translateY(-2px);
            background: rgba(30, 41, 59, 0.6);
            border-color: rgba(99, 102, 241, 0.3);
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
        }
        .neon-text {
            text-shadow: 0 0 10px rgba(99, 102, 241, 0.5);
        }
        .custom-scrollbar::-webkit-scrollbar {
            width: 8px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
            background: #0f172a;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
            background: #334155;
            border-radius: 4px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
            background: #475569;
        }
        .status-badge {
            position: relative;
            overflow: hidden;
        }
        .status-badge::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
            transition: 0.5s;
        }
        .status-badge:hover::before {
            left: 100%;
        }
        
        /* Modal Animation */
        @keyframes fadeIn {
            from { opacity: 0; transform: scale(0.95); }
            to { opacity: 1; transform: scale(1); }
        }
        .modal-animate {
            animation: fadeIn 0.2s ease-out forwards;
        }
    </style>
</head>
<body class="min-h-screen relative custom-scrollbar">
    <!-- Background Gradients -->
    <div class="fixed inset-0 z-[-1] overflow-hidden pointer-events-none">
        <div class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-indigo-500/10 rounded-full blur-[100px]"></div>
        <div class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-500/10 rounded-full blur-[100px]"></div>
    </div>

    <div class="container mx-auto px-4 py-8 max-w-7xl">
        <!-- Header -->
        <header class="flex justify-between items-center mb-10">
            <div class="flex items-center gap-4">
                <div class="p-3 rounded-lg bg-indigo-500/10 border border-indigo-500/20">
                    <i data-lucide="users" class="w-8 h-8 text-indigo-400"></i>
                </div>
                <div>
                    <h1 class="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 to-purple-400 neon-text">
                        Kullanıcı Yönetimi
                    </h1>
                    <p class="text-slate-400 text-sm mt-1">Users.json Dosyası için Gelişmiş Düzenleyici</p>
                </div>
            </div>
            
            <div class="flex gap-3">
                <button onclick="fetchUsers()" class="flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors border border-slate-700">
                    <i data-lucide="refresh-cw" class="w-4 h-4"></i> Yenile
                </button>
                <div class="px-4 py-2 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm font-medium flex items-center gap-2">
                    <div class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></div>
                    Sistem Aktif
                </div>
            </div>
        </header>

        <!-- Stats & Actions -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="glass-panel p-6 rounded-2xl">
                <div class="text-slate-400 text-sm font-medium mb-2">Toplam Kullanıcı</div>
                <div class="text-4xl font-bold text-white" id="totalUsers">0</div>
            </div>
            <div class="glass-panel p-6 rounded-2xl relative overflow-hidden group">
                <div class="absolute right-0 top-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                    <i data-lucide="alert-triangle" class="w-16 h-16 text-amber-500"></i>
                </div>
                <div class="text-slate-400 text-sm font-medium mb-2">Hatalı Hesaplar</div>
                <div class="text-4xl font-bold text-amber-500" id="faultyUsers">0</div>
                <p class="text-xs text-amber-500/60 mt-2">Eksik bilgi içeren hesaplar</p>
            </div>
            <div class="glass-panel p-6 rounded-2xl flex items-center justify-between">
                <div>
                    <div class="text-slate-400 text-sm font-medium mb-1">Json Dosyası</div>
                    <div class="text-xs text-slate-500 font-mono">kiro/users.json</div>
                </div>
                <div class="p-3 bg-blue-500/10 rounded-lg">
                    <i data-lucide="database" class="w-6 h-6 text-blue-400"></i>
                </div>
            </div>
        </div>

        <!-- Users Grid -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" id="usersGrid">
            <!-- Loading State -->
            <div class="col-span-2 flex justify-center py-20">
                <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500"></div>
            </div>
        </div>

    </div>

    <!-- Edit User Modal -->
    <div id="editModal" class="fixed inset-0 z-50 hidden">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" onclick="closeModal()"></div>
        <div class="absolute inset-0 flex items-center justify-center p-4">
            <div class="glass-panel w-full max-w-md rounded-2xl p-6 modal-animate" id="modalContent">
                <div class="flex justify-between items-center mb-6">
                    <h3 class="text-xl font-bold text-white flex items-center gap-2">
                        <i data-lucide="user-cog" class="w-5 h-5 text-indigo-400"></i>
                        Kullanıcı Düzenle
                    </h3>
                    <button onclick="closeModal()" class="text-slate-400 hover:text-white transition-colors">
                        <i data-lucide="x" class="w-5 h-5"></i>
                    </button>
                </div>

                <form id="editForm" onsubmit="handleInitialSave(event)" class="space-y-4">
                    <input type="hidden" id="editUsername">
                    
                    <div class="space-y-1">
                        <label class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Kullanıcı Adı</label>
                        <input type="text" id="displayUsername" disabled class="w-full bg-slate-800/50 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-300 cursor-not-allowed">
                    </div>

                    <div class="space-y-1">
                        <label class="text-xs font-semibold text-slate-400 uppercase tracking-wider">E-posta</label>
                        <input type="email" id="editEmail" class="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600">
                    </div>

                    <div class="space-y-1">
                        <label class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Ad Soyad</label>
                        <input type="text" id="editFullName" class="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600">
                    </div>

                    <div class="space-y-1">
                         <label class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Şifre Sıfırla (Opsiyonel)</label>
                         <input type="text" id="editPassword" placeholder="Değiştirmek için yeni şifre yazın" class="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600">
                    </div>

                    <div class="flex items-center gap-3 pt-2">
                        <label class="flex items-center gap-2 cursor-pointer group">
                            <input type="checkbox" id="editIsAdmin" class="w-4 h-4 rounded border-slate-700 bg-slate-900 text-indigo-500 focus:ring-offset-0 focus:ring-indigo-500/20">
                            <span class="text-sm text-slate-300 group-hover:text-white transition-colors">Yönetici Yetkisi</span>
                        </label>
                    </div>

                    <div class="pt-6 grid grid-cols-2 gap-3">
                        <button type="button" onclick="closeModal()" class="w-full px-4 py-2.5 rounded-lg border border-slate-700 text-slate-300 hover:bg-slate-800 hover:text-white transition-colors text-sm font-medium">İptal</button>
                        <button type="submit" class="w-full px-4 py-2.5 rounded-lg bg-indigo-500 hover:bg-indigo-600 text-white transition-colors text-sm font-medium shadow-lg shadow-indigo-500/20">Kaydet</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        lucide.createIcons();
        let usersData = {};

        async function fetchUsers() {
            try {
                const response = await fetch('/api/users');
                usersData = await response.json();
                renderUsers();
            } catch (err) {
                console.error('Kullanıcılar getirilirken hata:', err);
                alert('Kullanıcılar yüklenirken bir hata oluştu');
            }
        }

        function isFaulty(user) {
            // Eksik kritik alanları veya null değerleri kontrol et
            return !user.email || !user.full_name || !user.password;
        }

        function renderUsers() {
            const grid = document.getElementById('usersGrid');
            grid.innerHTML = '';
            
            let faultyCount = 0;
            const userList = Object.entries(usersData);
            document.getElementById('totalUsers').innerText = userList.length;

            userList.forEach(([username, user]) => {
                const faulty = isFaulty(user);
                if (faulty) faultyCount++;

                const card = document.createElement('div');
                card.className = `glass-card rounded-xl p-5 relative overflow-hidden group ${faulty ? 'border-amber-500/30 bg-amber-500/5' : ''}`;
                
                // Şifre Durumu
                const hasPassword = !!user.password;
                const passStatusHtml = hasPassword ? 
                    `<span class="px-2 py-0.5 rounded text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Güvenli</span>` : 
                    `<span class="px-2 py-0.5 rounded text-xs font-medium bg-amber-500/10 text-amber-500 border border-amber-500/20">Ayarlanmamış</span>`;

                card.innerHTML = `
                    <div class="flex justify-between items-start mb-4">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-full ${faulty ? 'bg-amber-500/10 text-amber-500' : 'bg-indigo-500/10 text-indigo-400'} flex items-center justify-center font-bold text-lg">
                                ${username.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <h3 class="font-bold text-white tracking-wide">${username}</h3>
                                <div class="text-xs ${faulty ? 'text-amber-400' : 'text-slate-400'}">${user.is_admin ? '🛡️ Yönetici' : '👤 Kullanıcı'}</div>
                            </div>
                        </div>
                        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                            <button onclick="openEditModal('${username}')" class="p-2 rounded-lg hover:bg-white/10 text-slate-300 hover:text-white transition-colors" title="Düzenle">
                                <i data-lucide="edit-2" class="w-4 h-4"></i>
                            </button>
                            <button onclick="deleteUser('${username}')" class="p-2 rounded-lg hover:bg-red-500/20 text-slate-300 hover:text-red-400 transition-colors" title="Sil">
                                <i data-lucide="trash-2" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </div>
                    
                    <div class="space-y-3 text-sm">
                        <div class="flex justify-between items-center py-1 border-b border-white/5">
                            <span class="text-slate-500">E-posta</span>
                            <span class="text-slate-300 font-mono ${!user.email ? 'text-amber-500 italic' : ''}">${user.email || 'Eksik'}</span>
                        </div>
                        <div class="flex justify-between items-center py-1 border-b border-white/5">
                            <span class="text-slate-500">Ad Soyad</span>
                            <span class="text-slate-300 ${!user.full_name ? 'text-amber-500 italic' : ''}">${user.full_name || 'Eksik'}</span>
                        </div>
                        <div class="flex justify-between items-center py-1 border-b border-white/5">
                            <span class="text-slate-500">Şifre</span>
                            <div class="font-mono text-sm text-indigo-300 bg-indigo-500/10 px-2 py-1 rounded selectable-text">
                                ${user._plain_password || '<span class="text-slate-500 text-xs italic">Hash (Görüntülenemez)</span>'}
                            </div>
                        </div>
                        <div class="flex justify-between items-center py-1 pt-2">
                             <span class="text-slate-500 text-xs">Oluşturuldu</span>
                             <span class="text-slate-400 text-xs">${user.created_at ? new Date(user.created_at).toLocaleDateString("tr-TR") : '-'}</span>
                        </div>
                    </div>
                    
                    ${faulty ? `
                        <div class="absolute bottom-0 left-0 w-full h-1 bg-amber-500/50"></div>
                        <div class="absolute top-2 right-2">
                           <span class="flex h-2 w-2">
                              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                              <span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
                            </span>
                        </div>
                    ` : ''}
                `;
                grid.appendChild(card);
            });
            
            document.getElementById('faultyUsers').innerText = faultyCount;
            lucide.createIcons();
        }

        function openEditModal(username) {
            const user = usersData[username];
            document.getElementById('editUsername').value = username;
            document.getElementById('displayUsername').value = username;
            document.getElementById('editEmail').value = user.email || '';
            document.getElementById('editFullName').value = user.full_name || '';
            document.getElementById('editIsAdmin').checked = user.is_admin || false;
            document.getElementById('editPassword').value = ''; 
            
            const modal = document.getElementById('editModal');
            modal.classList.remove('hidden');
        }

        function closeModal() {
            document.getElementById('editModal').classList.add('hidden');
        }

        async function handleInitialSave(e) {
            e.preventDefault();
            const username = document.getElementById('editUsername').value;
            const data = {
                email: document.getElementById('editEmail').value,
                full_name: document.getElementById('editFullName').value,
                is_admin: document.getElementById('editIsAdmin').checked
            };
            
            const pass = document.getElementById('editPassword').value;
            if (pass && pass.trim() !== '') {
                data.password = pass;
            }

            try {
                const res = await fetch(`/api/users/${username}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                
                if (res.ok) {
                    closeModal();
                    fetchUsers();
                } else {
                    alert('Güncelleme başarısız: ' + await res.text());
                }
            } catch (err) {
                console.error(err);
                alert('Bir hata oluştu');
            }
        }

        function toggleHash(username) {
            const container = document.getElementById(`pass-display-${username}`);
            const icon = document.getElementById(`icon-${username}`);
            const hidden = container.getAttribute('data-hidden') === 'true';
            const pass = container.getAttribute('data-pass');
            
            if (hidden) {
                container.innerHTML = `<span class="font-mono text-[10px] text-slate-400 break-all leading-tight block max-w-[150px]">${pass.substring(0, 20)}...</span>`;
                container.setAttribute('data-hidden', 'false');
                icon.setAttribute('data-lucide', 'eye-off');
            } else {
                container.innerHTML = `<span class="px-2 py-0.5 rounded text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Güvenli</span>`;
                container.setAttribute('data-hidden', 'true');
                icon.setAttribute('data-lucide', 'eye');
            }
            lucide.createIcons();
        }

        async function deleteUser(username) {
            if (!confirm(`"${username}" kullanıcısını silmek istediğinize emin misiniz?`)) return;
            
            try {
                const res = await fetch(`/api/users/${username}`, {
                    method: 'DELETE'
                });
                
                if (res.ok) {
                    fetchUsers();
                } else {
                    alert('Silme başarısız');
                }
            } catch (err) {
                console.error(err);
                alert('Bir hata oluştu');
            }
        }

        // Başlangıç
        fetchUsers();
    </script>
</body>
</html>
"""

# --- API Uç Noktaları ---

@app.get("/")
async def get_ui():
    return HTMLResponse(content=HTML_CONTENT)

@app.get("/api/users")
async def get_users():
    return load_users()

@app.put("/api/users/{username}")
async def update_user(username: str, update: UserUpdate):
    users = load_users()
    if username not in users:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    
    user = users[username]
    if update.email is not None:
        user['email'] = update.email
    if update.full_name is not None:
        user['full_name'] = update.full_name
    if update.is_admin is not None:
        user['is_admin'] = update.is_admin
    if update.password is not None and len(update.password) > 0:
        # Uygulama için HASH sakla (Güvenlik)
        user['password'] = hash_password(update.password)
        # Sadece bu Yönetici Paneli için DÜZ METİN sakla (Kolaylık)
        user['_plain_password'] = update.password
    
    users[username] = user
    if save_users(users):
        return {"message": "Kullanıcı güncellendi"}
    raise HTTPException(status_code=500, detail="Kaydetme başarısız")

@app.delete("/api/users/{username}")
async def delete_user(username: str):
    users = load_users()
    if username not in users:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    
    del users[username]
    if save_users(users):
        return {"message": "Kullanıcı silindi"}
    raise HTTPException(status_code=500, detail="Kaydetme başarısız")

if __name__ == "__main__":
    print("Kullanıcı Yönetim Sistemi başlatılıyor...")
    # Kısa bir gecikmeden sonra tarayıcıyı aç
    webbrowser.open(f"http://{HOST}:{PORT}")
    uvicorn.run(app, host=HOST, port=PORT)
