# 📦 Kurulum Rehberi

Bu rehber, Niko AI'ı farklı platformlarda nasıl kuracağınızı adım adım açıklar.

## 📋 Gereksinimler

### Minimum Sistem Gereksinimleri
- **İşletim Sistemi:** Windows 10/11, Linux, macOS
- **Python:** 3.9 veya üzeri
- **RAM:** 8 GB (16 GB önerilir)
- **Disk:** 10 GB boş alan
- **İnternet:** Stabil bağlantı

### Yazılım Gereksinimleri
- Python 3.9+
- pip (Python paket yöneticisi)
- Git
- Ollama (LLM için)

## 🚀 Hızlı Başlangıç

### Windows

1. **Repository'yi Klonlayın**
   ```bash
   git clone https://github.com/Memati8383/niko-with-kiro.git
   cd niko-with-kiro
   ```

2. **Ollama'yı Kurun**
   - [Ollama İndirme Sayfası](https://ollama.ai/download)
   - İndirip kurun
   - Model indirin:
     ```bash
     ollama pull RefinedNeuro/RN_TR_R2:latest
     ```

3. **Python Bağımlılıklarını Yükleyin**
   ```bash
   pip install -r requirements.txt
   ```

4. **Sistemi Başlatın**
   ```bash
   sistemi_baslat.bat
   ```
   Menüden "1. Sistemi Başlat (Tam Paket)" seçeneğini seçin.

### Linux/macOS

1. **Repository'yi Klonlayın**
   ```bash
   git clone https://github.com/Memati8383/niko-with-kiro.git
   cd niko-with-kiro
   ```

2. **Ollama'yı Kurun**
   ```bash
   curl -fsSL https://ollama.ai/install.sh | sh
   ollama pull RefinedNeuro/RN_TR_R2:latest
   ```

3. **Virtual Environment Oluşturun**
   ```bash
   python3 -m venv .venv
   source .venv/bin/activate
   ```

4. **Bağımlılıkları Yükleyin**
   ```bash
   pip install -r requirements.txt
   ```

5. **Servisleri Başlatın**
   ```bash
   # Terminal 1: Ollama
   ollama serve
   
   # Terminal 2: Backend
   python main.py
   
   # Terminal 3: Tunnel (opsiyonel)
   python start_tunnel.py
   ```

## 🔧 Detaylı Kurulum

### 1. Ollama Kurulumu ve Yapılandırması

#### Model İndirme
```bash
# Önerilen model (Türkçe optimize)
ollama pull RefinedNeuro/RN_TR_R2:latest

# Alternatif modeller
ollama pull llama3.2:latest
ollama pull gemma2:latest
```

#### Ollama Ayarları
Ollama varsayılan olarak `http://localhost:11434` adresinde çalışır.

### 2. Python Ortamı Kurulumu

#### Virtual Environment (Önerilir)
```bash
# Windows
python -m venv .venv
.venv\Scripts\activate

# Linux/macOS
python3 -m venv .venv
source .venv/bin/activate
```

#### Bağımlılıkları Yükleme
```bash
pip install --upgrade pip
pip install -r requirements.txt
```

### 3. Yapılandırma

#### .env Dosyası Oluşturma
```bash
# .env dosyası oluşturun
SECRET_KEY=your-secret-key-here
OLLAMA_URL=http://localhost:11434
```

#### İlk Kullanıcı Oluşturma
```bash
python manage_users.py
```
Menüden "1. Kullanıcı Ekle" seçeneğini seçin.

### 4. Servis Başlatma

#### Otomatik (Windows)
```bash
sistemi_baslat.bat
```

#### Manuel
```bash
# Terminal 1: Ollama
ollama serve

# Terminal 2: Backend
python main.py

# Terminal 3: Tunnel (dış erişim için)
python start_tunnel.py
```

## 📱 Mobil Uygulama Kurulumu

### Android

1. **APK İndirme**
   - GitHub Releases sayfasından en son APK'yı indirin
   - Veya Android Studio ile kaynak koddan derleyin

2. **Kurulum**
   - APK dosyasını Android cihazınıza aktarın
   - "Bilinmeyen kaynaklardan yükleme" iznini verin
   - APK'yı yükleyin

3. **İlk Çalıştırma**
   - Uygulamayı açın
   - Giriş yapın veya kayıt olun
   - Mikrofon ve diğer izinleri verin

## 🌐 Web Arayüzü Erişimi

### Yerel Erişim
```
http://localhost:8000
```

### Dış Erişim (Cloudflare Tunnel)
Tunnel başlatıldıktan sonra konsola yazdırılan URL'yi kullanın.

## 🔍 Sorun Giderme

### Ollama Bağlantı Hatası
```bash
# Ollama'nın çalıştığını kontrol edin
curl http://localhost:11434/api/tags

# Çalışmıyorsa başlatın
ollama serve
```

### Port Çakışması
```bash
# main.py içinde portu değiştirin
# Varsayılan: 8000
uvicorn.run(app, host="0.0.0.0", port=8001)
```

### Bağımlılık Hataları
```bash
# Tüm bağımlılıkları yeniden yükleyin
pip install --force-reinstall -r requirements.txt
```

### Python Versiyonu Uyumsuzluğu
```bash
# Python versiyonunu kontrol edin
python --version

# 3.9+ olmalı
```

## 📊 Performans Optimizasyonu

### GPU Kullanımı (Ollama)
Ollama otomatik olarak GPU kullanır (varsa). CUDA veya ROCm kurulu olmalı.

### Bellek Optimizasyonu
```bash
# Ollama için bellek limiti ayarlama
OLLAMA_MAX_LOADED_MODELS=1 ollama serve
```

## 🔄 Güncelleme

```bash
# Repository'yi güncelleyin
git pull origin main

# Bağımlılıkları güncelleyin
pip install --upgrade -r requirements.txt

# Ollama modellerini güncelleyin
ollama pull RefinedNeuro/RN_TR_R2:latest
```

## 📞 Destek

Kurulum sırasında sorun yaşarsanız:
- [GitHub Issues](https://github.com/Memati8383/niko-with-kiro/issues)
- [Dokümantasyon](https://github.com/Memati8383/niko-with-kiro/wiki)

---

Başarılı kurulumlar! 🎉
