# Implementation Plan: Admin Panel

## Overview

Bu plan, Niko AI Chat uygulamasına admin paneli eklemek için gerekli görevleri tanımlar. Mevcut FastAPI backend'e yeni endpoint'ler ve static dosyalara admin.html sayfası eklenecektir.

## Tasks

- [x] 1. Backend: Pydantic modelleri ve AdminService oluştur
  - [x] 1.1 UserAdminUpdate, UserAdminCreate, UserListResponse modellerini ekle
    - main.py'ye yeni Pydantic modelleri ekle
    - Mevcut validation kurallarını kullan
    - _Requirements: 3.2, 5.2, 5.3_
  - [x] 1.2 AdminService sınıfını oluştur
    - list_users, get_user, update_user, delete_user, create_user metodları
    - AuthService ve HistoryService ile entegrasyon
    - _Requirements: 2.1, 3.3, 4.2, 4.3, 5.4_
  - [x] 1.3 Write property test for user list completeness
    - **Property 2: User List Completeness**
    - **Validates: Requirements 2.1, 2.2**

- [x] 2. Backend: Admin API endpoint'lerini ekle
  - [x] 2.1 Admin middleware/dependency oluştur
    - Token doğrulama + is_admin kontrolü
    - _Requirements: 1.1, 1.2, 6.1, 6.2_
  - [x] 2.2 GET /api/admin/users endpoint'i
    - Tüm kullanıcıları listele (şifre hariç)
    - Sorting ve filtering desteği
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [x] 2.3 GET /api/admin/users/{username} endpoint'i
    - Tek kullanıcı detayı
    - _Requirements: 3.1_
  - [x] 2.4 PUT /api/admin/users/{username} endpoint'i
    - Kullanıcı güncelleme
    - _Requirements: 3.2, 3.3, 3.4_
  - [x] 2.5 DELETE /api/admin/users/{username} endpoint'i
    - Kullanıcı ve geçmiş silme
    - Self-deletion kontrolü
    - _Requirements: 4.2, 4.3, 4.4_
  - [x] 2.6 POST /api/admin/users endpoint'i
    - Yeni kullanıcı oluşturma
    - _Requirements: 5.2, 5.3, 5.4, 5.5_
  - [x] 2.7 Write property test for admin access control
    - **Property 1: Admin Access Control**
    - **Validates: Requirements 1.1, 1.2, 6.1, 6.2**
  - [x] 2.8 Write property test for user update round-trip
    - **Property 3: User Update Round-Trip**
    - **Validates: Requirements 3.2, 3.3**
  - [x] 2.9 Write property test for user deletion completeness
    - **Property 4: User Deletion Completeness**
    - **Validates: Requirements 4.2, 4.3**
  - [x] 2.10 Write property test for self-deletion prevention
    - **Property 7: Self-Deletion Prevention**
    - **Validates: Requirements 4.4**

- [x] 3. Checkpoint - Backend testleri
  - Tüm testlerin geçtiğinden emin ol
  - Kullanıcıya soru sor gerekirse

- [x] 4. Frontend: Admin panel sayfası oluştur
  - [x] 4.1 static/admin.html dosyasını oluştur
    - Mevcut style.css ile tutarlı tasarım
    - Kullanıcı tablosu
    - Modal formlar (düzenleme/oluşturma)
    - Silme onay dialogu
    - _Requirements: 2.2, 3.1, 4.1, 5.1_
  - [x] 4.2 Admin panel JavaScript kodunu ekle
    - API çağrıları
    - Tablo render ve güncelleme
    - Form validasyonu
    - _Requirements: 2.3, 2.4, 3.4_
  - [x] 4.3 GET /admin route'unu ekle
    - Admin sayfasını serve et
    - _Requirements: 1.3_

- [x] 5. Data: Örnek kullanıcıları ekle
  - [x] 5.1 users.json'a örnek kullanıcılar ekle
    - En az 5 Türkçe isimli kullanıcı
    - Çeşitli veriler (email var/yok, admin/normal)
    - Gerçekçi tarihler
    - _Requirements: 7.1, 7.2, 7.3_
  - [x] 5.2 Mevcut kullanıcılara is_admin alanı ekle
    - Varsayılan olarak false
    - _Requirements: 1.4_

- [x] 6. Final Checkpoint
  - Tüm testlerin geçtiğinden emin ol
  - Admin panelinin çalıştığını doğrula
  - Kullanıcıya soru sor gerekirse

## Notes

- All tasks are required for comprehensive implementation
- Python hypothesis kütüphanesi property-based testing için kullanılacak
- Mevcut AuthService ve HistoryService sınıfları genişletilecek
- Frontend mevcut style.css ile tutarlı olacak
