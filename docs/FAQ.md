# ❓ Sık Sorulan Sorular (FAQ)

## 🚀 Genel Sorular

### Niko AI nedir?
Niko AI, Türkçe optimize edilmiş, sesli komut destekli, hibrit (web + mobil) bir yapay zeka asistanıdır. Ollama altyapısı kullanarak yerel LLM modelleriyle çalışır.

### Hangi platformlarda çalışır?
- **Web:** Tüm modern tarayıcılar (Chrome, Firefox, Edge, Safari)
- **Mobil:** Android 8.0+ (iOS planlanıyor)
- **Desktop:** Windows, Linux, macOS

### Ücretsiz mi?
Evet, Niko AI tamamen açık kaynak ve ücretsizdir. MIT lisansı altında dağıtılır.

### İnternet bağlantısı gerekli mi?
- **Backend:** Evet, Cloudflare tunnel için
- **AI İşleme:** Hayır, Ollama yerel çalışır
- **Web Arama:** Evet, opsiyonel özellik

## 🔧 Kurulum & Yapılandırma

### Minimum sistem gereksinimleri nedir?
- **CPU:** 4 çekirdek (8 önerilir)
- **RAM:** 8 GB (16 GB önerilir)
- **Disk:** 10 GB boş alan
- **GPU:** Opsiyonel (CUDA/ROCm destekli)

### Ollama nedir ve neden gerekli?
Ollama, yerel olarak LLM modellerini çalıştırmak için bir platformdur. Niko AI, AI işlemlerini Ollama üzerinden yapar.

### Hangi modelleri kullanabilirim?
- **Önerilen:** RefinedNeuro/RN_TR_R2:latest (Türkçe optimize)
- **Alternatifler:** llama3.2, gemma2, mistral
- **Boyut:** 7B-70B arası modeller

### GPU kullanımı nasıl aktif edilir?
Ollama otomatik olarak GPU kullanır. CUDA (NVIDIA) veya ROCm (AMD) kurulu olmalı.

### Port 8000 kullanımda, nasıl değiştirebilirim?
`main.py` dosyasında:
```python
uvicorn.run(app, host="0.0.0.0", port=8001)  # 8001'e değiştirin
```

## 🔐 Güvenlik & Gizlilik

### Verilerim güvende mi?
Evet. Tüm veriler yerel olarak saklanır. Şifreler bcrypt ile hashlenir, oturumlar JWT ile yönetilir.

### Sohbet geçmişim nerede saklanır?
`history/` klasöründe, kullanıcı bazlı JSON dosyalarında saklanır.

### Verilerimi nasıl silebilirim?
- **Web:** Profil → Geçmişi Temizle
- **Mobil:** Ayarlar → Hesabı Sil
- **Manuel:** `history/` klasöründeki dosyaları silin

### Cloudflare tunnel güvenli mi?
Evet. Cloudflare tunnel, şifreli (HTTPS) bağlantı sağlar ve IP adresinizi gizler.

## 💬 Kullanım

### Sesli komutlar nasıl çalışır?
Mobil uygulamada "Niko" diyerek asistanı uyandırın, ardından komutunuzu söyleyin.

### Hangi sesli komutlar destekleniyor?
- "Niko, [kişi] ara"
- "Niko, WhatsApp'tan [kişi]'ye mesaj gönder"
- "Niko, müzik aç/durdur"
- "Niko, Wi-Fi aç/kapat"
- "Niko, fener aç/kapat"
- Ve daha fazlası...

### Kişilik modları nedir?
AI'nın yanıt tarzını değiştirir:
- **Normal:** Dengeli ve yardımsever
- **Agresif:** Direkt ve keskin
- **Romantik:** Duygusal ve şiirsel
- **Akademik:** Bilimsel ve detaylı
- **Komik:** Esprili ve neşeli
- **Felsefeci:** Derin ve düşündürücü

### Web arama özelliği nasıl kullanılır?
Chat sırasında "Web araması yap" seçeneğini aktif edin. AI, güncel bilgilere erişebilir.

### Düşünce akışı nedir?
AI'nın yanıt üretirken düşünme sürecini gösterir. Nasıl sonuca vardığını anlamanızı sağlar.

## 📱 Mobil Uygulama

### APK nereden indirilir?
GitHub Releases sayfasından: [Releases](https://github.com/Memati8383/niko-with-kiro/releases)

### Otomatik güncelleme nasıl çalışır?
Uygulama, GitHub'daki `version.json` dosyasını kontrol eder. Yeni sürüm varsa bildirim gönderir.

### Hangi izinler gerekli?
- **Mikrofon:** Sesli komutlar için
- **Kişiler:** Arama ve mesaj için
- **Telefon:** Arama yapma için
- **SMS:** Mesaj okuma/gönderme için
- **Depolama:** Güncelleme indirme için

### iOS versiyonu var mı?
Henüz yok, ancak gelecek sürümlerde planlanıyor.

## 🐛 Sorun Giderme

### "Ollama bağlantı hatası" alıyorum
```bash
# Ollama'nın çalıştığını kontrol edin
curl http://localhost:11434/api/tags

# Çalışmıyorsa başlatın
ollama serve
```

### "Model bulunamadı" hatası
```bash
# Modeli indirin
ollama pull RefinedNeuro/RN_TR_R2:latest
```

### Backend başlamıyor
```bash
# Bağımlılıkları kontrol edin
pip install -r requirements.txt

# Port çakışması varsa değiştirin
# main.py içinde port numarasını değiştirin
```

### Mobil uygulama bağlanamıyor
1. Backend'in çalıştığından emin olun
2. Tunnel URL'sini kontrol edin
3. İnternet bağlantınızı kontrol edin
4. Firewall ayarlarını kontrol edin

### Sesli komutlar çalışmıyor
1. Mikrofon iznini kontrol edin
2. "Niko" kelimesini net söyleyin
3. Arka plan gürültüsünü azaltın
4. Mikrofon ayarlarını kontrol edin

### Yavaş yanıt alıyorum
- **CPU kullanıyorsanız:** Daha küçük model kullanın (7B)
- **GPU varsa:** CUDA/ROCm kurulu olduğundan emin olun
- **Bellek yetersizse:** Diğer uygulamaları kapatın

## 🔄 Güncelleme & Bakım

### Nasıl güncellerim?
```bash
git pull origin main
pip install --upgrade -r requirements.txt
ollama pull RefinedNeuro/RN_TR_R2:latest
```

### Veritabanını nasıl yedeklerim?
```bash
# Tüm veriyi yedekleyin
cp users.json users_backup.json
cp -r history/ history_backup/
```

### Eski sohbet geçmişini nasıl temizlerim?
```bash
# 30 günden eski dosyaları silin (Linux/macOS)
find history/ -name "*.json" -mtime +30 -delete

# Windows PowerShell
Get-ChildItem history/ -Filter *.json | Where-Object {$_.LastWriteTime -lt (Get-Date).AddDays(-30)} | Remove-Item
```

## 🛠️ Geliştirme

### Katkıda nasıl bulunabilirim?
[CONTRIBUTING.md](../CONTRIBUTING.md) dosyasını okuyun.

### Yeni özellik nasıl önerebilirim?
GitHub Issues'da "Feature Request" şablonunu kullanın.

### Bug nasıl bildiririm?
GitHub Issues'da "Bug Report" şablonunu kullanın.

### Dokümantasyon nasıl güncellenir?
1. Fork edin
2. `docs/` klasöründe değişiklik yapın
3. Pull Request gönderin

## 📞 Destek

### Yardım nereden alabilirim?
- **GitHub Issues:** Teknik sorunlar için
- **Discussions:** Genel sorular için
- **Wiki:** Detaylı dokümantasyon

### Hata raporu nasıl gönderilir?
1. GitHub Issues'a gidin
2. "New Issue" tıklayın
3. "Bug Report" şablonunu seçin
4. Formu doldurun

### Özellik isteği nasıl yapılır?
1. GitHub Issues'a gidin
2. "New Issue" tıklayın
3. "Feature Request" şablonunu seçin
4. Önerinizi detaylandırın

## 🔮 Gelecek Planları

### Hangi özellikler planlanıyor?
- iOS uygulaması
- WebSocket desteği
- Görüntü analizi
- Dosya yükleme
- Takvim entegrasyonu
- Özel model eğitimi

### Ne zaman çıkacak?
Roadmap için [CHANGELOG.md](../CHANGELOG.md) dosyasını kontrol edin.

## 📚 Ek Kaynaklar

- [Kurulum Rehberi](INSTALLATION.md)
- [API Dokümantasyonu](API.md)
- [Mimari Dokümantasyonu](ARCHITECTURE.md)
- [Katkıda Bulunma Rehberi](../CONTRIBUTING.md)
- [Güvenlik Politikası](../SECURITY.md)

---

Sorunuz burada yanıtlanmadı mı? [GitHub Discussions](https://github.com/Memati8383/niko-with-kiro/discussions)'da sorun!
