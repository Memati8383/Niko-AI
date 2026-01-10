# Niko AI

Niko AI, gelişmiş Türkçe sesli komut desteği sunan, Android ve Web platformlarında çalışan kişisel yapay zeka asistanınızdır. FastAPI altyapısı ve Ollama entegrasyonu ile güçlü bir deneyim sunar.

## 🚀 Özellikler

- **Sesli Asistan:** Türkçe konuşma tanıma ve doğal seslendirme (TTS).
- **Çoklu Platform:** Hem Web tarayıcısı hem de Android mobil uygulaması üzerinden erişim.
- **Yapay Zeka Modelleri:** Ollama entegrasyonu sayesinde Llama, Gemma gibi çeşitli LLM modellerini kullanabilme.
- **Kullanıcı Yönetimi:** Güvenli kayıt, giriş ve profil sistemi.
- **Sohbet Geçmişi:** Konuşmalarınız kaydedilir ve dilediğiniz zaman erişilebilir.
- **Mobil Yetenekler:**
  - Arama yapma
  - Müzik kontrolü (Spotify vb.)
  - Alarm ve hatırlatıcı kurma
  - Sistem ayarları kontrolü (WiFi, Bluetooth)

## 🔗 Sunucu Bağlantısı

Mobil uygulamanın ve dış ağların sunucuya erişebilmesi için Cloudflare tüneli kullanılmaktadır.

> 🌐 **Güncel Tünel Adresi:** [https://blond-thumb-step-trance.trycloudflare.com](https://blond-thumb-step-trance.trycloudflare.com)

_Not: Bu adres `start_tunnel.py` çalıştırıldığında otomatik olarak güncellenir._

## 🛠️ Kurulum ve Çalıştırma

### 1. Sunucu Tarafı

Gerekli Python kütüphanelerini yükleyin ve sunucuyu başlatın.

```bash
# Bağımlılıkları yükleyin
pip install fastapi uvicorn requests python-multipart python-jose passlib bcrypt

# Tüneli başlatın (Otomatik URL güncellemesi için gereklidir)
python start_tunnel.py

# Ana uygulamayı başlatın
python main.py
```

### 2. Mobil Uygulama

`Niko Mobile App` klasöründeki proje Android Studio ile açılıp derlenebilir. `MainActivity.java` dosyası, `start_tunnel.py` çalıştığında otomatik olarak yeni sunucu adresiyle güncellenir.

## 📂 Proje Yapısı

- **main.py:** FastAPI backend uygulaması.
- **start_tunnel.py:** Cloudflare tünelini başlatır ve GitHub/Yerel dosyalardaki URL'leri günceller.
- **Niko Mobile App/**: Android uygulama kaynak kodları.
- **static/**: Web arayüzü dosyaları (HTML, CSS, JS).

## ⚠️ Önemli Notlar

- Uygulamanın tam fonksiyonlu çalışabilmesi için yerel makinenizde **Ollama** servisinin çalışıyor olması gerekmektedir.
- Mobil uygulama sesli komutlar için cihaz izinlerine ihtiyaç duyar.
