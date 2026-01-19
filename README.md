# Niko AI Ecosystem

Niko AI, gelişmiş Türkçe sesli komut desteği sunan, Android ve Web platformlarında çalışan hibrit bir kişisel yapay zeka asistanı ekosistemidir. FastAPI altyapısı, Ollama entegrasyonu ve modern kullanıcı arayüzleri ile hem mobil hem de masaüstü kullanıcıları için benzersiz bir deneyim sunar.

Proje tamamen **Türkçe** olarak yerelleştirilmiştir (kod içi dokümantasyon, loglar ve kullanıcı arayüzleri).

## 🚀 Temel Özellikler

### 🤖 Yapay Zeka & Dil Yetenekleri

- **Gelişmiş LLM Desteği:** Ollama entegrasyonu ile Llama, Gemma, RefinedNeuro gibi çeşitli modellerle yüksek kaliteli Türkçe sohbet.
- **Düşünce Akışı (Thought Process):** AI'nın yanıt üretme sürecini gerçek zamanlı izleme.
- **Kişilik Modları:** Normal, Agresif, Romantik, Akademik, Komik, Felsefeci modları.
- **Gerçek Zamanlı Web Arama:** DuckDuckGo entegrasyonu ile modelin güncel bilgilere erişmesi sağlanır.

### 🔐 Güvenlik & Kullanıcı Yönetimi

- **Unified Auth System:** Tüm platformlar için merkezi JWT tabanlı kimlik doğrulama.
- **Bütünleşik Yönetim:** `sistemi_baslat.bat` üzerinden erişilen kullanıcı yönetim paneli.
- **Profil Yönetimi:** Kullanıcı bilgilerini ve profil fotoğraflarını yönetme.

### 📱 Mobil Yetenekler (Android)

- **Sesli Kontrol:** "Niko" uyanma kelimesi ve sesli komutlarla eller serbest kullanım.
- **Sistem Entegrasyonu:** Arama yapma, WhatsApp mesaj okuma/cevaplama, müzik (Spotify) kontrolü.
- **Donanım Kontrolü:** Wi-Fi, Bluetooth, Parlaklık, Kamera ve Fener kontrolü.
- **Otomatik Güncelleme:** GitHub'dan yeni sürüm kontrolü.

### 💻 Web & Masaüstü

- **Avant-Garde UI:** Glassmorphism ve premium mikro-etkileşimlerle donatılmış Web Chat arayüzü.
- **Sohbet Geçmişi:** Tarih bazlı gruplandırma, arama ve dışa aktarma.

## 📁 Proje Yapısı

```text
kiro/
├── sistemi_baslat.bat      # 🔥 ÖNERİLEN: Tüm sistemi yöneten ana başlatıcı
├── main.py                 # Ana FastAPI Backend uygulaması (Tamamen Türkçe)
├── manage_users.py         # Kullanıcı Yönetim Sistemi (CLI Admin)
├── start_tunnel.py         # Cloudflare Tünel ve URL Otomasyonu
├── hizli_commit.bat        # Developer Git iş akış aracı
├── users.json              # Veritabanı (Kullanıcı bilgileri)
├── prompts.py              # AI Sistem Promptları ve Kişilik Ayarları
├── history/                # Kullanıcı sohbet geçmişleri (JSON)
├── static/                 # Web Frontend (HTML, CSS, JS)
│   ├── admin.html          # Web tabanlı admin arayüzü
│   ├── login.html          # Giriş sayfası
│   └── index.html          # Ana sohbet arayüzü
└── Niko Mobile App/        # Android Native (Java) kaynak kodları
```

## 🔗 Sunucu ve Bağlantı

Dış ağlardan ve mobil cihazdan erişim için Cloudflare tüneli kullanılmaktadır. Tünel adresi sistem her başladığında otomatik olarak güncellenir ve `start_tunnel.py` tarafından yönetilir.

> ℹ️ **Not:** Mobil uygulama (Android), GitHub'daki README dosyasını okuyarak güncel API adresini otomatik olarak alabilir.

## 🛠️ Kurulum ve Çalıştırma

### 1. Önerilen Yöntem (Otomatik)

En kolay ve sorunsuz başlatma yöntemi **`sistemi_baslat.bat`** dosyasını kullanmaktır. Bu araç size interaktif bir menü sunar:

- **1. Sistemi Başlat (Tam Paket):** Ollama, Backend Server ve Tüneli aynı anda sırayla başlatır.
- **2. Sadece Ollama:** Yerel LLM sunucusunu başlatır.
- **3. Sadece Backend:** FastAPI sunucusunu başlatır.
- **4. Tünel Başlat:** Cloudflare tünelini aktif eder.
- **5. Admin Paneli:** Kullanıcı ekleme/silme işlemleri için yönetim panelini açar.
- **6. Kütüphaneleri Güncelle:** `requirements.txt` üzerinden eksikleri tamamlar.

Çalıştırmak için:

1. Klasördeki `sistemi_baslat.bat` dosyasına çift tıklayın veya terminalden çalıştırın.

### 2. Manuel Kurulum (Geliştiriciler İçin)

Eğer servisleri tek tek yönetmek isterseniz:

```bash
# Gerekli Python kütüphanelerini yükleyin
pip install fastapi uvicorn requests python-multipart python-jose passlib bcrypt httpx edge-tts

# Ollama servisini başlatın (ayrı bir terminalde)
ollama serve

# Modeli indirin (eğer yoksa)
ollama pull RefinedNeuro/RN_TR_R2:latest

# Tüneli ve Backend'i başlatın
python start_tunnel.py
# Veya sadece backend:
python main.py
```

## 🧑‍💻 Geliştirici Notları

- **Yerelleştirme:** `main.py` dahil tüm backend kodları, fonksiyon açıklamaları ve loglar Türkçe'ye çevrilmiştir.
- **Hızlı Commit:** Kod değişikliklerini hızlıca GitHub'a göndermek için `hizli_commit.bat` aracını kullanabilirsiniz.
- **Testler:** Validasyon testleri için `test_validation.py` dosyasını `pytest` veya doğrudan Python ile çalıştırabilirsiniz.

---

_Niko AI - Geleceğin Asistanı, Bugün Yanınızda._


> 🌐 **Güncel Tünel Adresi:** [https://periods-kansas-tales-qui.trycloudflare.com](https://periods-kansas-tales-qui.trycloudflare.com)
