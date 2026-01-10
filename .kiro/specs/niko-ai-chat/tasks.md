# Implementation Plan: Niko AI Chat Application

## Overview

Bu plan, Niko AI sohbet uygulamasını adım adım oluşturmak için gereken görevleri içerir. Her görev önceki görevlerin üzerine inşa edilir ve sonunda tam çalışır bir uygulama elde edilir.

## Tasks

- [x] 1. Proje yapısı ve temel dosyaları oluştur
  - [x] 1.1 Proje dizin yapısını oluştur (static/, history/)
    - Ana dizinde main.py, prompts.py, requirements.txt
    - static/ klasöründe HTML, CSS, JS dosyaları
    - _Requirements: 9.1, 9.2_
  - [x] 1.2 requirements.txt dosyasını oluştur
    - fastapi, uvicorn, httpx, pydantic, passlib[bcrypt], python-jose[cryptography], python-multipart, duckduckgo-search
    - _Requirements: Tüm bağımlılıklar_
  - [x] 1.3 Temel FastAPI uygulamasını oluştur
    - App instance, CORS middleware, static files mount
    - _Requirements: 7.4_

- [x] 2. Pydantic modelleri ve validasyon
  - [x] 2.1 UserCreate, UserLogin, UserUpdate modellerini oluştur
    - Username validasyonu (3-30 karakter, harf ile başlama, alfanumerik + underscore)
    - Password validasyonu (min 8, büyük/küçük harf, rakam)
    - Email validasyonu
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_
  - [x] 2.2 Username validasyonu için property test yaz
    - **Property 1: Username Validation**
    - **Validates: Requirements 1.2, 1.3, 1.4**
  - [x] 2.3 Password validasyonu için property test yaz
    - **Property 2: Password Validation**
    - **Validates: Requirements 1.5, 1.6**
  - [x] 2.4 ChatRequest modelini oluştur
    - message, enable_audio, web_search, rag_search, session_id, model, mode, images
    - _Requirements: 3.5_

- [x] 3. Authentication Service
  - [x] 3.1 AuthService sınıfını oluştur
    - bcrypt ile password hashing
    - JWT token oluşturma ve doğrulama (24 saat expiry)
    - users.json dosya işlemleri
    - _Requirements: 1.9, 2.1_
  - [x] 3.2 Password hashing round-trip property test yaz
    - **Property 3: Password Hashing Round-Trip**
    - **Validates: Requirements 1.9, 7.5**
  - [x] 3.3 Register endpoint (/register) oluştur
    - Duplicate username kontrolü
    - Başarılı kayıt mesajı
    - _Requirements: 1.1, 1.8_
  - [x] 3.4 Registration uniqueness property test yaz
    - **Property 4: Registration Uniqueness**
    - **Validates: Requirements 1.1, 1.8**
  - [x] 3.5 Login endpoint (/login) oluştur
    - Credential doğrulama
    - JWT token dönüşü
    - _Requirements: 2.1, 2.2_
  - [x] 3.6 JWT authentication property test yaz
    - **Property 5: JWT Authentication**
    - **Validates: Requirements 2.1, 2.4, 2.5**
  - [x] 3.7 Logout endpoint (/logout) oluştur
    - Session invalidation
    - _Requirements: 2.3_
  - [x] 3.8 Profile endpoints (/me GET, PUT) oluştur
    - Kullanıcı bilgisi getirme
    - Profil güncelleme
    - _Requirements: 2.6, 2.7_
  - [x] 3.9 Profile data consistency property test yaz
    - **Property 7: Profile Data Consistency**
    - **Validates: Requirements 2.6, 2.7**

- [x] 4. Checkpoint - Auth sistemi testi
  - Tüm testlerin geçtiğinden emin ol
  - Kullanıcıya soru varsa sor

- [x] 5. History Service
  - [x] 5.1 HistoryService sınıfını oluştur
    - Session oluşturma (UUID)
    - Mesaj ekleme
    - Session listeleme, yükleme, silme
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.6_
  - [x] 5.2 History CRUD operations property test yaz
    - **Property 9: History CRUD Operations**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.6**
  - [x] 5.3 History endpoints oluştur
    - GET /history - Liste
    - DELETE /history/{session_id} - Tek silme
    - DELETE /history - Tümünü silme
    - _Requirements: 4.1, 4.3, 4.4_
  - [x] 5.4 Markdown export endpoint (/export/{session_id}) oluştur
    - Sohbeti Markdown formatında dışa aktar
    - _Requirements: 4.5_
  - [x] 5.5 Markdown export format property test yaz
    - **Property 11: Markdown Export Format**
    - **Validates: Requirements 4.5**
  - [x] 5.6 History message format property test yaz
    - **Property 10: History Message Format**
    - **Validates: Requirements 4.7, 9.5**

- [x] 6. Rate Limiter
  - [x] 6.1 RateLimiter sınıfını oluştur
    - In-memory request tracking
    - Endpoint bazlı limitler (general: 60/dk, auth: 5/5dk, register: 3/saat, chat: 30/dk)
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  - [x] 6.2 Rate limiting enforcement property test yaz
    - **Property 8: Rate Limiting Enforcement**
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**
  - [x] 6.3 Rate limiter middleware'i entegre et
    - Tüm endpointlere uygula
    - 429 response with retry-after
    - _Requirements: 6.5_

- [x] 7. Security Middleware
  - [x] 7.1 Security headers middleware oluştur
    - X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, Referrer-Policy
    - Production'da HSTS
    - _Requirements: 7.1, 7.2_
  - [x] 7.2 Security headers property test yaz
    - **Property 12: Security Headers**
    - **Validates: Requirements 7.1**

- [x] 8. Checkpoint - Backend core testi
  - Tüm testlerin geçtiğinden emin ol
  - Kullanıcıya soru varsa sor

- [x] 9. Chat Service ve Ollama Entegrasyonu
  - [x] 9.1 ChatService sınıfını oluştur
    - Ollama API client (httpx async)
    - Model listesi getirme
    - Streaming chat response
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  - [x] 9.2 Chat endpoint (/chat) oluştur
    - StreamingResponse ile SSE
    - Session history'ye kaydetme
    - Image handling (base64)
    - _Requirements: 3.1, 3.2, 3.5, 3.7_
  - [x] 9.3 Image attachment handling property test yaz
    - **Property 15: Image Attachment Handling**
    - **Validates: Requirements 3.5**
  - [x] 9.4 Models endpoint (/models) oluştur
    - Ollama'dan model listesi
    - _Requirements: 3.4_
  - [x] 9.5 Ollama hata yönetimi ekle
    - API unavailable durumu
    - User-friendly error messages
    - _Requirements: 3.6_

- [x] 10. Search Service
  - [x] 10.1 SearchService sınıfını oluştur
    - DuckDuckGo web search
    - ChromaDB RAG search (opsiyonel)
    - Hybrid search mode
    - _Requirements: 5.1, 5.2, 5.3_
  - [x] 10.2 Search hata yönetimi ekle
    - Web search failure handling
    - Empty RAG results handling
    - _Requirements: 5.4, 5.5_

- [x] 11. API Response Format ve Error Handling
  - [x] 11.1 Global exception handlers oluştur
    - HTTPException handler
    - General exception handler (Türkçe mesajlar)
    - _Requirements: 10.5_
  - [x] 11.2 API response codes property test yaz
    - **Property 13: API Response Codes**
    - **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
  - [x] 11.3 Data persistence format property test yaz
    - **Property 14: Data Persistence Format**
    - **Validates: Requirements 9.1, 9.2, 9.5**

- [x] 12. Checkpoint - Backend tamamlandı
  - Tüm backend testlerinin geçtiğinden emin ol
  - Kullanıcıya soru varsa sor

- [x] 13. Frontend - Login ve Signup Sayfaları
  - [x] 13.1 login.html oluştur
    - Username/password form
    - Hata mesajları
    - Signup linki
    - _Requirements: 2.1, 2.2_
  - [x] 13.2 signup.html oluştur
    - Registration form (username, password, email, full_name)
    - Validasyon mesajları
    - Login linki
    - _Requirements: 1.1_
  - [x] 13.3 style.css oluştur
    - Dark theme
    - CSS variables
    - Responsive design
    - Glassmorphism effects
    - _Requirements: 8.1_

- [x] 14. Frontend - Ana Sayfa
  - [x] 14.1 index.html oluştur
    - Chat container
    - Sidebar (history)
    - Message input area
    - Model selector
    - _Requirements: 8.1, 8.2_
  - [x] 14.2 script.js - Temel fonksiyonlar
    - scrollToBottom, debounce, fetchModels
    - toggleSidebar, startNewChat
    - _Requirements: 8.2_
  - [x] 14.3 script.js - Mesajlaşma fonksiyonları
    - appendMessage, sendMessage, stopGeneration
    - updateSendButtonState
    - Streaming response handling
    - _Requirements: 8.3, 8.4_
  - [x] 14.4 script.js - History fonksiyonları
    - fetchHistory, renderHistory, loadHistoryItem
    - deleteHistoryItem, clearAllHistory, exportHistoryItem
    - _Requirements: 8.2_
  - [x] 14.5 script.js - Profil fonksiyonları
    - loadUserProfile, saveUserProfile
    - showProfileError, showProfileSuccess
    - _Requirements: 2.6, 2.7_

- [x] 15. Frontend - Yardımcı Özellikler
  - [x] 15.1 Toast bildirimleri ve modal dialoglar
    - showToast(message, type)
    - showConfirmModal(title, description)
    - _Requirements: 8.9, 8.10_
  - [x] 15.2 Görsel yükleme ve önizleme
    - resizeImage, handleFiles
    - Drag & drop support
    - _Requirements: 8.5_
  - [x] 15.3 Mesaj kopyalama ve kod highlighting
    - Copy to clipboard
    - Syntax highlighting for code blocks
    - _Requirements: 8.6, 8.4_
  - [x] 15.4 Connection status ve draft saving
    - updateConnectionStatus
    - saveDraft, loadDraft (localStorage)
    - _Requirements: 8.7, 8.8_
  - [x] 15.5 Draft auto-save property test yaz
    - **Property 16: Draft Auto-Save**
    - **Validates: Requirements 8.8**

- [x] 16. Frontend - Service Worker
  - [x] 16.1 sw.js oluştur
    - Offline caching
    - _Requirements: Service worker endpoint_

- [x] 17. Checkpoint - Frontend tamamlandı
  - Tüm frontend özelliklerinin çalıştığından emin ol
  - Kullanıcıya soru varsa sor

- [x] 18. prompts.py - AI Sistem Promptları
  - [x] 18.1 Sistem promptlarını oluştur
    - Türkçe AI asistan promptu
    - Web search context promptu
    - RAG context promptu
    - _Requirements: 3.1_

- [x] 19. E2E Testler
  - [x] 19.1 browser_test.py oluştur
    - BrowserTester sınıfı
    - Selenium WebDriver setup
    - WebDriverWait (10 saniye timeout)
    - _Requirements: E2E testing_
  - [x] 19.2 Test senaryolarını yaz
    - test_signup_page
    - test_login
    - test_main_page_elements
    - test_send_message
    - test_sidebar_history
    - test_profile_functionality
    - test_logout_functionality
    - check_console_errors
    - _Requirements: E2E testing_

- [x] 20. Final Checkpoint
  - Tüm testlerin geçtiğinden emin ol
  - Uygulamayı başlat ve manuel test yap
  - Kullanıcıya soru varsa sor

## Notes

- Tüm görevler zorunludur
- Her görev belirli gereksinimlere referans verir
- Checkpoint'ler artımlı doğrulama sağlar
- Property testleri evrensel doğruluk özelliklerini doğrular
- Unit testler belirli örnekleri ve edge case'leri doğrular
