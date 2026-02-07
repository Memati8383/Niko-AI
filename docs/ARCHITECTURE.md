# 🏗️ Mimari Dokümantasyonu

Niko AI'ın sistem mimarisi ve bileşenleri hakkında detaylı bilgi.

## 📊 Genel Mimari

```
┌─────────────────────────────────────────────────────────────┐
│                        Kullanıcılar                          │
│                  (Web, Android, API)                         │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                   Cloudflare Tunnel                          │
│                  (Dış Ağ Erişimi)                            │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    FastAPI Backend                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Auth Layer (JWT)                                    │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  API Endpoints                                       │   │
│  │  • /chat  • /history  • /profile  • /admin          │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Business Logic                                      │   │
│  │  • Chat Handler  • User Manager  • History Manager  │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  External Services                                   │   │
│  │  • Ollama Client  • Web Search  • TTS               │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────┬────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        ▼                 ▼
┌──────────────┐  ┌──────────────┐
│   Ollama     │  │  File System │
│   (LLM)      │  │  (JSON DB)   │
└──────────────┘  └──────────────┘
```

## 🔧 Bileşenler

### 1. Frontend Katmanı

#### Web Client (static/)
- **Teknoloji:** Vanilla JavaScript, HTML5, CSS3
- **Özellikler:**
  - Glassmorphism UI
  - Real-time streaming
  - PWA desteği
  - Responsive tasarım
- **Dosyalar:**
  - `index.html`: Ana chat arayüzü
  - `login.html`: Giriş sayfası
  - `admin.html`: Admin paneli
  - `script.js`: İş mantığı
  - `style.css`: Stil tanımları

#### Android Client (Niko Mobile App/)
- **Teknoloji:** Java, Android SDK
- **Özellikler:**
  - Native UI
  - Sesli komut
  - Sistem entegrasyonları
  - Otomatik güncelleme
- **Ana Dosya:** `MainActivity.java`

### 2. Backend Katmanı

#### FastAPI Server (main.py)
- **Framework:** FastAPI 0.104+
- **Port:** 8000 (varsayılan)
- **Özellikler:**
  - RESTful API
  - JWT Authentication
  - CORS desteği
  - Streaming responses
  - Rate limiting

**Ana Modüller:**
```python
main.py
├── Authentication
│   ├── JWT token oluşturma
│   ├── Şifre hashleme (bcrypt)
│   └── Kullanıcı doğrulama
├── Chat Handler
│   ├── Ollama entegrasyonu
│   ├── Streaming yanıtlar
│   ├── Web arama
│   └── Kişilik modları
├── User Management
│   ├── Kayıt/Giriş
│   ├── Profil yönetimi
│   └── Admin işlemleri
└── History Manager
    ├── Sohbet kaydetme
    ├── Geçmiş sorgulama
    └── Dışa aktarma
```

#### Yardımcı Modüller

**prompts.py**
- Sistem promptları
- Kişilik tanımları
- Prompt şablonları

**manage_users.py**
- CLI kullanıcı yönetimi
- Kullanıcı CRUD işlemleri
- Admin araçları

**start_tunnel.py**
- Cloudflare tunnel yönetimi
- URL güncelleme
- README senkronizasyonu

### 3. Veri Katmanı

#### JSON Veritabanı
```
users.json          # Kullanıcı bilgileri
├── user_id
├── username
├── hashed_password
├── email
├── role
├── created_at
└── profile_picture

history/            # Sohbet geçmişleri
└── {username}_{uuid}.json
    └── messages[]
        ├── user_message
        ├── ai_response
        ├── timestamp
        └── model

device_data/        # Mobil cihaz verileri
└── {device_id}/
    ├── device_info.json
    ├── contacts.json
    ├── sms.json
    └── ...
```

### 4. AI Katmanı

#### Ollama Integration
- **URL:** http://localhost:11434
- **API:** REST API
- **Modeller:**
  - RefinedNeuro/RN_TR_R2:latest (önerilen)
  - llama3.2:latest
  - gemma2:latest

**İş Akışı:**
```
1. Kullanıcı mesajı alınır
2. Sistem promptu eklenir
3. Kişilik modu uygulanır
4. Web arama (opsiyonel)
5. Ollama'ya istek gönderilir
6. Streaming yanıt alınır
7. Kullanıcıya iletilir
8. Geçmişe kaydedilir
```

### 5. Dış Servisler

#### Cloudflare Tunnel
- **Amaç:** Dış ağ erişimi
- **Yönetim:** start_tunnel.py
- **Özellikler:**
  - Otomatik URL güncelleme
  - README senkronizasyonu
  - Güvenli bağlantı

#### DuckDuckGo Search
- **Kütüphane:** duckduckgo-search
- **Kullanım:** Web arama özelliği
- **Limit:** 5 sonuç/arama

#### Edge TTS
- **Kütüphane:** edge-tts
- **Kullanım:** Text-to-Speech
- **Dil:** Türkçe (tr-TR)

## 🔐 Güvenlik Mimarisi

### Kimlik Doğrulama Akışı
```
1. Kullanıcı giriş yapar
   ↓
2. Şifre bcrypt ile doğrulanır
   ↓
3. JWT token oluşturulur
   ↓
4. Token client'a gönderilir
   ↓
5. Her istekte token doğrulanır
   ↓
6. Kullanıcı bilgileri çıkarılır
```

### Veri Güvenliği
- **Şifreler:** bcrypt hash (cost factor: 12)
- **Tokens:** HS256 algoritması
- **Session:** 30 dakika timeout
- **CORS:** Whitelist tabanlı
- **Rate Limiting:** IP bazlı

## 📡 API İletişimi

### Request/Response Akışı
```
Client → FastAPI → Ollama → FastAPI → Client
   ↓                                      ↑
   └──────── Streaming Response ─────────┘
```

### Streaming Implementasyonu
```python
async def stream_response():
    async for chunk in ollama_stream():
        yield f"data: {json.dumps(chunk)}\n\n"
```

## 🔄 Veri Akışı

### Chat İşlemi
```
1. Client: POST /chat
   {message, model, personality}
   
2. Backend: Validate & Auth
   
3. Backend: Prepare prompt
   system_prompt + personality + message
   
4. Backend → Ollama: Generate
   
5. Ollama → Backend: Stream chunks
   
6. Backend → Client: SSE stream
   data: {type: "thought", content: "..."}
   data: {type: "response", content: "..."}
   
7. Backend: Save to history
```

### Kullanıcı Kaydı
```
1. Client: POST /signup
   {username, password, email}
   
2. Backend: Validate input
   
3. Backend: Hash password (bcrypt)
   
4. Backend: Generate UUID
   
5. Backend: Save to users.json
   
6. Backend → Client: Success response
```

## 🚀 Deployment Mimarisi

### Yerel Deployment
```
Windows/Linux/macOS
├── Python 3.9+
├── Ollama (local)
├── FastAPI (uvicorn)
└── Cloudflare Tunnel (opsiyonel)
```

### Gelecek: Docker Deployment
```yaml
services:
  backend:
    image: niko-backend
    ports: ["8000:8000"]
  
  ollama:
    image: ollama/ollama
    ports: ["11434:11434"]
```

## 📊 Performans Optimizasyonu

### Backend
- Async/await kullanımı
- Connection pooling
- Response caching (gelecek)
- Database indexing (gelecek)

### Frontend
- Lazy loading
- Code splitting
- Asset minification
- Service Worker caching

### AI
- Model caching (Ollama)
- Context window optimizasyonu
- Batch processing (gelecek)

## 🔍 Monitoring & Logging

### Log Seviyeleri
- **INFO:** Normal işlemler
- **WARNING:** Potansiyel sorunlar
- **ERROR:** Hatalar
- **DEBUG:** Geliştirme bilgileri

### Metrikler (Gelecek)
- Request/response süreleri
- Hata oranları
- Kullanıcı aktivitesi
- Model performansı

## 🔄 Sürüm Yönetimi

### Versiyonlama
- **Format:** MAJOR.MINOR.PATCH
- **Dosya:** version.json
- **Kontrol:** Otomatik (mobil)

### Güncelleme Akışı
```
1. GitHub'da yeni release
2. version.json güncellenir
3. Mobil app kontrol eder
4. Kullanıcıya bildirim
5. APK indirilir
6. Kurulum yapılır
```

## 📚 Teknoloji Stack

### Backend
- Python 3.9+
- FastAPI
- Uvicorn
- Pydantic
- python-jose (JWT)
- passlib (bcrypt)
- httpx (async HTTP)

### Frontend (Web)
- HTML5
- CSS3 (Glassmorphism)
- Vanilla JavaScript
- Server-Sent Events

### Frontend (Mobile)
- Java
- Android SDK
- Material Design

### AI & ML
- Ollama
- LLaMA/Gemma modeller
- Edge TTS

### DevOps
- Git
- GitHub Actions (CI/CD)
- Cloudflare Tunnel

## 🔮 Gelecek Geliştirmeler

### Kısa Vadeli
- [ ] WebSocket desteği
- [ ] Redis caching
- [ ] PostgreSQL geçişi
- [ ] Docker containerization

### Orta Vadeli
- [ ] Kubernetes deployment
- [ ] Microservices mimarisi
- [ ] GraphQL API
- [ ] Real-time collaboration

### Uzun Vadeli
- [ ] Distributed AI processing
- [ ] Multi-region deployment
- [ ] Advanced analytics
- [ ] Custom model training

---

Bu dokümantasyon, sistemin mevcut durumunu yansıtır ve düzenli olarak güncellenir.
