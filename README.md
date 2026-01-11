# Niko AI Ecosystem

Niko AI, gelişmiş Türkçe sesli komut desteği sunan, Android ve Web platformlarında çalışan hibrit bir kişisel yapay zeka asistanı ekosistemidir. FastAPI altyapısı, Ollama entegrasyonu ve modern kullanıcı arayüzleri ile hem mobil hem de masaüstü kullanıcıları için benzersiz bir deneyim sunar.

## 🚀 Temel Özellikler

### 🤖 Yapay Zeka & Dil Yetenekleri

- **Gelişmiş LLM Desteği:** Ollama entegrasyonu ile Llama, Gemma, RefinedNeuro gibi çeşitli modellerle yüksek kaliteli Türkçe sohbet.
- **Düşünce Akışı (Thought Process):** AI'nın yanıt üretme sürecini gerçek zamanlı izleme.
- **Kişilik Modları:** Normal, Agresif, Romantik, Akademik, Komik, Felsefeci modları.
- **Web Arama:** DuckDuckGo entegrasyonu ile güncel bilgilere erişim.

### 🔐 Güvenlik & Kullanıcı Yönetimi

- **Unified Auth System:** Tüm platformlar için merkezi JWT tabanlı kimlik doğrulama.
- **Profil Yönetimi:** Kullanıcı bilgilerini (isim, e-posta) ve profil fotoğrafını (Base64) yönetme.
- **Admin Paneli:** Kullanıcıları listeleyen, düzenleyen, yetki veren ve şifre sıfırlayan bağımsız yönetim arayüzü (`manage_users.py`).

### 📱 Mobil Yetenekler (Android)

- **Sesli Kontrol:** "Niko" uyanma kelimesi ve sesli komutlarla eller serbest kullanım.
- **Sistem Entegrasyonu:** Arama yapma, WhatsApp mesaj okuma/cevaplama, müzik (Spotify) kontrolü.
- **Donanım Kontrolü:** Wi-Fi, Bluetooth, Parlaklık, Kamera ve Fener kontrolü.
- **Cihaz Sync:** Rehber, Arama Kayıtları, Konum ve Uygulama listesinin backend ile güvenli senkronizasyonu.

### 💻 Web & Masaüstü

- **Avant-Garde UI:** Glassmorphism ve premium mikro-etkileşimlerle donatılmış Web Chat arayüzü.
- **Sohbet Geçmişi:** Tarih bazlı gruplandırma, arama, dışa aktarma (Markdown) ve silme özellikleri.

## 📁 Proje Yapısı

```text
kiro/
├── main.py                 # Ana FastAPI Backend uygulaması
├── manage_users.py         # Bağımsız Kullanıcı Yönetim Sistemi (Admin)
├── start_tunnel.py         # Cloudflare Tünel ve URL Otomasyonu
├── hizli_commit.bat         # Developer Git iş akış aracı
├── users.json              # Veritabanı (Kullanıcı bilgileri ve hashlenmiş şifreler)
├── history/                # Kullanıcı sohbet geçmişleri (JSON)
├── device_data/            # Senkronize edilen mobil cihaz verileri
├── static/                 # Web Frontend (HTML, CSS, JS)
│   ├── admin.html          # Web tabanlı admin arayüzü
│   ├── login.html          # Giriş sayfası
│   └── signup.html         # Kayıt sayfası
└── Niko Mobile App/        # Android Native (Java) kaynak kodları
```

## 🔗 Sunucu ve Bağlantı

Dış ağlardan ve mobil cihazdan erişim için Cloudflare tüneli kullanılmaktadır.

- 🌐 **Güncel API Adresi:** [https://monster-bristol-robert-anyone.trycloudflare.com](https://monster-bristol-robert-anyone.trycloudflare.com)
- 📝 **API Dokümantasyonu:** `/docs` (Swagger) veya `/redoc`

> _Not: Tünel adresi `start_tunnel.py` çalıştırıldığında otomatik olarak tüm sistemde (GitHub dahil) güncellenir._

## 🛠️ Kurulum ve Çalıştırma

### 1. Gereksinimler

- Python 3.8+
- Ollama (LLM modellerini çalıştırmak için)
- Android Studio (Mobil derleme için)

### 2. Backend Kurulumu

```bash
# Bağımlılıkları yükleyin
pip install fastapi uvicorn requests python-multipart python-jose passlib bcrypt httpx edge-tts

# Ollama modelini indirin
ollama pull RefinedNeuro/RN_TR_R2:latest

# Tüneli ve Backend'i başlatın
python start_tunnel.py
python main.py  # Varsayılan port: 8001
```

### 3. Kullanıcı Yönetimi (Admin)

```bash
# Bağımsız kullanıcı yönetim panelini açmak için:
python manage_users.py
```

## 🧑‍💻 Geliştirici Notları

- **Hizli Commit:** Değişiklikleri hızlıca GitHub'a göndermek için `hizli_commit.bat` dosyasını kullanabilirsiniz.
- **Logs:** Detaylı sistem logları konsol üzerinden takip edilebilir.

---

_Niko AI - Geleceğin Asistanı, Bugün Yanınızda._
