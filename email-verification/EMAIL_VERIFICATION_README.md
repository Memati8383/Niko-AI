# 📧 E-posta Doğrulama Sistemi

Niko AI için Resend API kullanarak e-posta doğrulama sistemi.

## 🎯 Özellikler

- ✅ 6 haneli rastgele doğrulama kodu üretimi
- ✅ Premium HTML e-posta şablonu
- ✅ 5 dakika geçerlilik süresi
- ✅ Brute force koruması (5 deneme hakkı)
- ✅ Kod tekrar gönderme
- ✅ Mobil uygulama entegrasyonu
- ✅ **Resend API** - Modern ve basit e-posta servisi

## 📦 Kurulum

### Backend (Python)

1. **Dosyalar:**
   - `email_verification.py` - E-posta servisi
   - `main.py` - API endpoint'leri (güncellenmiş)

2. **Gerekli Kütüphaneler:**
   ```bash
   # Zaten mevcut (FastAPI projesi için)
   pip install fastapi pydantic
   ```

3. **API Ayarları:**
   - Resend API Key: ``
   - Gönderen E-posta: `onboarding@resend.dev` (Test için)
   - Production için kendi domain'inizi ekleyin: https://resend.com/domains

### Mobil Uygulama (Android)

1. **Dosya:**
   - `Niko Mobile App/MainActivity.java` (güncellenmiş)

2. **Yeni Metodlar:**
   - `showEmailVerificationDialog()` - Doğrulama ekranı
   - `sendVerificationCode()` - Kod gönderme
   - `verifyEmailCode()` - Kod kontrolü
   - `performRegistration()` - Kayıt işlemi

## 🚀 Kullanım

### Backend Başlatma

```bash
python main.py
```

### Test Etme

```bash
python test_email_verification.py
```

### Mobil Uygulama

1. Kayıt ekranını açın
2. E-posta adresi girin
3. "Kayıt Ol" butonuna tıklayın
4. E-postanıza gelen 6 haneli kodu girin
5. "Doğrula ve Kayıt Ol" butonuna tıklayın

## 📡 API Endpoint'leri

### 1. Doğrulama Kodu Gönder

```http
POST /send-verification-code
Content-Type: application/json

{
  "email": "kullanici@example.com",
  "username": "kullanici_adi"
}
```

**Yanıt:**
```json
{
  "success": true,
  "message": "Doğrulama kodu e-posta adresinize gönderildi.",
  "code": "123456"  // Sadece test için
}
```

### 2. Kodu Doğrula

```http
POST /verify-email-code
Content-Type: application/json

{
  "email": "kullanici@example.com",
  "code": "123456"
}
```

**Yanıt (Başarılı):**
```json
{
  "success": true,
  "message": "E-posta adresiniz başarıyla doğrulandı!"
}
```

**Yanıt (Hatalı):**
```json
{
  "success": false,
  "message": "Hatalı doğrulama kodu. Kalan deneme: 4"
}
```

### 3. Kodu Tekrar Gönder

```http
POST /resend-verification-code
Content-Type: application/json

{
  "email": "kullanici@example.com",
  "username": "kullanici_adi"
}
```

## 🎨 E-posta Şablonu

Premium HTML şablon özellikleri:
- 🎨 Gradient arka plan
- 🤖 Niko AI branding
- 🔐 Büyük, okunabilir kod gösterimi
- ⚠️ Güvenlik uyarıları
- 📱 Mobil uyumlu tasarım

## 🔒 Güvenlik

- ✅ 5 dakika geçerlilik süresi
- ✅ Maksimum 5 deneme hakkı
- ✅ Kod kullanıldıktan sonra otomatik silme
- ✅ Süresi dolmuş kodların otomatik temizlenmesi

## 🐛 Hata Ayıklama

### E-posta Gönderilmiyor

1. API Key'i kontrol edin
2. Resend hesabınızın aktif olduğundan emin olun
3. Test için `delivered@resend.dev` kullanın
4. Production için kendi domain'inizi ekleyin: https://resend.com/domains

### Kod Doğrulanmıyor

1. Kodun 5 dakika içinde girildiğinden emin olun
2. Deneme sayısını kontrol edin (maksimum 5)
3. Backend loglarını kontrol edin

### Mobil Uygulama Bağlanamıyor

1. `API_BASE_URL` adresinin doğru olduğundan emin olun
2. Backend'in çalıştığından emin olun
3. İnternet bağlantısını kontrol edin

## 📝 Notlar

- **Production'da:** `code` alanını API yanıtından kaldırın (güvenlik)
- **Veritabanı:** Bellekteki kod saklama yerine Redis veya veritabanı kullanın
- **Rate Limiting:** Aynı e-postaya çok fazla kod gönderilmesini engelleyin
- **E-posta Şablonu:** İhtiyaca göre özelleştirilebilir

## 🎯 Gelecek Geliştirmeler

- [ ] SMS doğrulama desteği
- [ ] 2FA (İki faktörlü kimlik doğrulama)
- [ ] E-posta şablonu özelleştirme paneli
- [ ] Çoklu dil desteği
- [ ] Rate limiting middleware

## 📞 Destek

Sorun yaşarsanız:
1. `test_email_verification.py` ile test edin
2. Backend loglarını kontrol edin
3. Resend dashboard'unu kontrol edin: https://resend.com/emails

## 🌟 Resend API Avantajları

- ✅ **Basit API** - Sadece 3 satır kod
- ✅ **Hızlı** - Anında e-posta gönderimi
- ✅ **Güvenilir** - %99.9 uptime
- ✅ **Modern** - RESTful API
- ✅ **Test Modu** - `delivered@resend.dev` ile test
- ✅ **Ücretsiz Plan** - Ayda 3,000 e-posta

---

**Geliştirici:** Niko AI Team  
**Tarih:** 2026  
**Versiyon:** 1.0.0
