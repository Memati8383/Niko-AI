# 🤖 Niko AI Ecosystem

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Python](https://img.shields.io/badge/python-3.9+-green.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)
![Platform](https://img.shields.io/badge/platform-Web%20%7C%20Android-lightgrey.svg)
![Status](https://img.shields.io/badge/status-active-success.svg)

**Türkçe optimize edilmiş, sesli komut destekli, hibrit yapay zeka asistanı**

[🚀 Hızlı Başlangıç](#-kurulum-ve-çalıştırma) • [📖 Dokümantasyon](docs/) • [🐛 Hata Bildir](https://github.com/Memati8383/niko-with-kiro/issues) • [💡 Özellik İste](https://github.com/Memati8383/niko-with-kiro/issues/new?template=feature_request.md)

</div>

---

## 📝 Hakkında

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

## 📸 Ekran Görüntüleri

<div align="center">

| Web Chat                | Mobil Uygulama      | Admin Paneli       |
| ----------------------- | ------------------- | ------------------ |
| Modern glassmorphism UI | Sesli komut desteği | Kullanıcı yönetimi |

</div>

## 🏗️ Sıfırdan Adım Adım Kurulum

Eğer projeyi ilk kez kuruyorsanız, aşağıdaki adımları sırasıyla takip edin:

### 1. Ön Hazırlıklar

- Sisteminizde **Python 3.9 veya üzeri** yüklü olmalıdır.
- LLM modellerini çalıştırmak için [Ollama](https://ollama.ai/) indirilmiş ve kurulmuş olmalıdır.
- [Git](https://git-scm.com/) yüklü olmalıdır.
- Google play store üzerinden CodeAssist uygulamasını indirip kurmalısınız. (AndroidX Project - Uygulama Adı - Package Name - Dosya Konumu - Dil(önerilen java) - Minimum SDK(önerilen API 21))

### 2. Projeyi İndirme (Clone)

```bash
git clone https://github.com/Memati8383/niko-with-kiro.git
cd niko-with-kiro/kiro
```

### 3. Sanal Ortam Oluşturma ve Bağımlılıklar (Önerilen)

Projenin temiz bir ortamda çalışması için sanal ortam kullanmanız önerilir:

```bash
# Sanal ortam oluşturma
python -m venv venv

# Sanal ortamı aktif etme (Windows)
venv\Scripts\activate

# Gerekli paketleri yükleme
pip install -r requirements.txt
```

### 4. Dil Modelini İndirme

Niko'nun farklı yetenekler kazanması için aşağıdaki modellerden ihtiyacınız olanı Ollama üzerinden çekebilirsiniz (**Önerilen: RefinedNeuro/RN_TR_R2**):

```bash
# Ana Türkçe Model (Önerilen)
ollama pull RefinedNeuro/RN_TR_R2:latest

# Diğer Desteklenen Modeller
ollama pull medllama2:latest
ollama pull gemma2:2b
ollama pull feu/warnchat:12b
ollama pull alibayram/doktorllama3:latest
ollama pull necdetuygur/developer:latest
ollama pull alibayram/kumru:latest
ollama pull alibayram/turkish-gemma-9b-v0.1:latest
```

### 5. Başlatma

Her şeyi otomatik olarak başlatmak için:

```bash
sistemi_baslat.bat
```

---

## 🛠️ Kurulum ve Çalıştırma

> 📚 **Detaylı kurulum için:** [Kurulum Rehberi](docs/INSTALLATION.md)

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

## 📚 Dokümantasyon

- 📖 [Kurulum Rehberi](docs/INSTALLATION.md) - Detaylı kurulum adımları
- 🔌 [API Dokümantasyonu](docs/API.md) - REST API referansı
- 🏗️ [Mimari Dokümantasyonu](docs/ARCHITECTURE.md) - Sistem mimarisi
- ❓ [SSS](docs/FAQ.md) - Sık sorulan sorular
- 🤝 [Katkıda Bulunma](CONTRIBUTING.md) - Nasıl katkıda bulunulur
- 🔒 [Güvenlik](SECURITY.md) - Güvenlik politikası

## 🤝 Katkıda Bulunma

Katkılarınızı bekliyoruz! Lütfen [CONTRIBUTING.md](CONTRIBUTING.md) dosyasını okuyun.

1. Fork edin
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit edin (`git commit -m 'feat: harika özellik eklendi'`)
4. Push edin (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) altında lisanslanmıştır.

## 🙏 Teşekkürler

- [Ollama](https://ollama.ai/) - Yerel LLM altyapısı
- [FastAPI](https://fastapi.tiangolo.com/) - Modern web framework
- [Cloudflare](https://www.cloudflare.com/) - Tunnel servisi
- Tüm katkıda bulunanlara ❤️

## 📞 İletişim

- 🐛 **Bug Raporu:** [GitHub Issues](https://github.com/Memati8383/niko-with-kiro/issues)
- 💡 **Özellik İsteği:** [Feature Request](https://github.com/Memati8383/niko-with-kiro/issues/new?template=feature_request.md)
- 💬 **Tartışma:** [GitHub Discussions](https://github.com/Memati8383/niko-with-kiro/discussions)

## ⭐ Yıldız Geçmişi

[![Star History Chart](https://api.star-history.com/svg?repos=Memati8383/niko-with-kiro&type=Date)](https://star-history.com/#Memati8383/niko-with-kiro&Date)

---

<div align="center">

**Niko AI - Geleceğin Asistanı, Bugün Yanınızda** 🚀

Made with ❤️ in Turkey 🇹🇷

[⬆ Başa Dön](#-niko-ai-ecosystem)

</div>

---

_Niko AI - Geleceğin Asistanı, Bugün Yanınızda._

> 🌐 **Güncel Tünel Adresi:** [https://african-erp-believe-rebate.trycloudflare.com](https://african-erp-believe-rebate.trycloudflare.com)
