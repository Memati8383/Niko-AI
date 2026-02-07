# 🚀 Resend API - Hızlı Kurulum Kılavuzu

## ✅ Sistem Hazır!

E-posta doğrulama sistemi **Resend API** ile başarıyla kuruldu ve test edildi.

## 🎯 Test Sonucu

```
✅ E-posta başarıyla gönderildi: delivered@resend.dev
✅ Kod: 536130
✅ Doğrulama başarılı!
```

## 📋 Yapılandırma

### API Bilgileri

- **API Key:** `buraya api key yaz`
- **Test E-posta:** `delivered@resend.dev`
- **Gönderen:** `Niko AI <onboarding@resend.dev>`

### Dosyalar

1. ✅ `email_verification.py` - Resend API entegrasyonu
2. ✅ `main.py` - FastAPI endpoint'leri
3. ✅ `Niko Mobile App/MainActivity.java` - Android entegrasyonu
4. ✅ `test_email_verification.py` - Test scripti

## 🚀 Kullanım

### 1. Backend'i Başlat

```bash
python main.py
```

### 2. Test Et

```bash
python test_email_verification.py
```

### 3. Mobil Uygulamada Kullan

1. Kayıt ekranını aç
2. E-posta gir (örn: `test@example.com`)
3. "Kayıt Ol" butonuna tıkla
4. E-postana gelen 6 haneli kodu gir
5. "Doğrula ve Kayıt Ol" butonuna tıkla

## 🌟 Resend API Avantajları

| Özellik | Açıklama |
|---------|----------|
| 🚀 **Hızlı** | Anında e-posta gönderimi |
| 💰 **Ücretsiz** | Ayda 3,000 e-posta |
| 🎯 **Basit** | Sadece 3 satır kod |
| 🔒 **Güvenilir** | %99.9 uptime |
| 📊 **Dashboard** | Gerçek zamanlı izleme |
| 🧪 **Test Modu** | `delivered@resend.dev` |

## 📧 E-posta Şablonu

Premium HTML şablon özellikleri:
- 🎨 Modern gradient tasarım
- 🤖 Niko AI branding
- 🔐 Büyük, okunabilir kod
- ⚠️ Güvenlik uyarıları
- 📱 Mobil uyumlu

## 🔧 Production Ayarları

### 1. Kendi Domain'inizi Ekleyin

1. https://resend.com/domains adresine gidin
2. Domain'inizi ekleyin (örn: `nikoai.com`)
3. DNS kayıtlarını yapılandırın
4. `email_verification.py` dosyasında güncelleyin:

```python
self.from_email = "noreply@nikoai.com"  # Kendi domain'iniz
```

### 2. Production'da Kod Alanını Kaldırın

`email_verification.py` dosyasında:

```python
return {
    "success": True,
    "message": "Doğrulama kodu e-posta adresinize gönderildi.",
    # "code": code  # ← Bu satırı kaldırın (güvenlik)
}
```

### 3. Rate Limiting Ekleyin

Aynı e-postaya çok fazla kod gönderilmesini engelleyin:

```python
# main.py'de
from slowapi import Limiter
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)

@app.post("/send-verification-code")
@limiter.limit("3/minute")  # Dakikada 3 istek
async def send_verification_code(request: Request, ...):
    ...
```

## 🐛 Sorun Giderme

### E-posta Gelmiyor

1. **Spam klasörünü kontrol edin**
2. **Resend dashboard'unu kontrol edin:** https://resend.com/emails
3. **API Key'i kontrol edin**
4. **Test e-postası kullanın:** `delivered@resend.dev`

### API Hatası

```bash
# Backend loglarını kontrol edin
[EMAIL ERROR] 401: {"message": "Invalid API key"}
```

**Çözüm:** API Key'i kontrol edin ve güncelleyin.

### Kod Doğrulanmıyor

1. **5 dakika geçmediğinden emin olun**
2. **Maksimum 5 deneme hakkınız var**
3. **Yeni kod isteyin**

## 📊 API Endpoint'leri

### 1. Kod Gönder

```http
POST /send-verification-code
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "kullanici"
}
```

### 2. Kodu Doğrula

```http
POST /verify-email-code
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456"
}
```

### 3. Kodu Tekrar Gönder

```http
POST /resend-verification-code
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "kullanici"
}
```

## 💡 İpuçları

### Test İçin

```python
# Test e-postası kullanın (gerçek e-posta gönderilmez)
test_email = "delivered@resend.dev"
```

### Kendi E-postanızla Test

```python
# email_verification.py'de
result = email_service.send_verification_email(
    to_email="sizin@email.com",  # Kendi e-postanız
    username="TestUser"
)
```

### Kod Geçerlilik Süresini Değiştir

```python
# email_verification.py'de
"expires_at": datetime.now() + timedelta(minutes=10)  # 5 yerine 10 dakika
```

## 📈 Resend Dashboard

E-posta gönderimlerini izleyin:
- https://resend.com/emails
- Gönderim durumu
- Açılma oranları
- Hata logları

## 🔐 Güvenlik

- ✅ 5 dakika geçerlilik
- ✅ 5 deneme hakkı
- ✅ Kod kullanıldıktan sonra silinir
- ✅ HTTPS zorunlu
- ✅ API Key güvenli saklanmalı

## 📞 Destek

- **Resend Docs:** https://resend.com/docs
- **Resend Status:** https://status.resend.com
- **Resend Support:** support@resend.com

---

**✨ Başarılar! Resend API ile hızlı ve güvenilir e-posta gönderimi!**

**Geliştirici:** Niko AI Team  
**Tarih:** 2026  
**API:** Resend (https://resend.com)
