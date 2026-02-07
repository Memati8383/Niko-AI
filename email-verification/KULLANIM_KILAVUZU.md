# 📱 Niko AI - E-posta Doğrulama Kullanım Kılavuzu

## 🎯 Sistem Özeti

Kullanıcı kayıt olurken e-posta adresi girerse, otomatik olarak 6 haneli doğrulama kodu gönderilir ve doğrulama ekranı açılır.

## 🚀 Hızlı Başlangıç

### 1. Backend'i Başlatın

```bash
python main.py
```

### 2. Mobil Uygulamayı Açın

APK'yı yükleyin ve çalıştırın.

### 3. Kayıt Olun

1. **Kayıt Ekranını Açın**
   - Profil ikonuna tıklayın
   - "Hesabınız yok mu? Kayıt Olun" seçeneğine tıklayın

2. **Bilgileri Girin**
   - Kullanıcı Adı: `test_user`
   - Şifre: `Test1234` (en az 8 karakter, büyük harf, küçük harf, rakam)
   - E-posta: `sizin@email.com` ✅ **ZORUNLU DEĞİL AMA ÖNERİLİR**
   - Tam Ad: `Test Kullanıcı` (opsiyonel)

3. **Kayıt Ol Butonuna Tıklayın**

### 4. E-posta Doğrulama (E-posta Girdiyseniz)

1. **Doğrulama Ekranı Açılır**
   - E-postanıza gelen 6 haneli kodu girin
   - Kod 5 dakika geçerlidir

2. **Kodu Girin ve Doğrulayın**
   - Örnek: `855135`
   - "Doğrula ve Kayıt Ol" butonuna tıklayın

3. **Başarılı!**
   - Hesabınız oluşturuldu
   - Artık giriş yapabilirsiniz

## 📧 E-posta Şablonu Örneği

```
Konu: 🔐 Niko AI Doğrulama Kodu: 855135

Merhaba test_user! 👋

Niko AI'a hoş geldiniz! Hesabınızı aktifleştirmek için 
aşağıdaki 6 haneli doğrulama kodunu kullanın.

┌─────────────────┐
│   8 5 5 1 3 5   │
└─────────────────┘

⚠️ Önemli: Bu kod 5 dakika içinde geçerliliğini yitirecektir.
Kodu kimseyle paylaşmayın!

© 2026 Niko AI - Yapay Zeka Asistanınız
```

## 🔧 Sorun Giderme

### E-posta Gelmiyor

1. **Spam/Gereksiz klasörünü kontrol edin**
2. **"Kodu Tekrar Gönder" butonuna tıklayın**
3. **Backend loglarını kontrol edin:**
   ```bash
   # Terminal'de göreceksiniz:
   [EMAIL] Doğrulama kodu gönderildi: email@example.com -> 123456
   ```

### Kod Hatalı Diyor

1. **Kodu doğru girdiğinizden emin olun** (6 hane)
2. **5 dakika geçmediğinden emin olun**
3. **Maksimum 5 deneme hakkınız var**
4. **Yeni kod isteyin** ("Kodu Tekrar Gönder")

### Bağlantı Hatası

1. **Backend çalışıyor mu?**
   ```bash
   python main.py
   ```

2. **API URL doğru mu?**
   - MainActivity.java'da `API_BASE_URL` kontrol edin
   - Varsayılan: GitHub'dan otomatik güncellenir

3. **İnternet bağlantınız var mı?**

## 💡 İpuçları

### E-posta Olmadan Kayıt

E-posta alanını boş bırakırsanız doğrulama ekranı açılmaz ve direkt kayıt olursunuz.

### Güvenli Şifre

Şifreniz şunları içermelidir:
- ✅ En az 8 karakter
- ✅ En az 1 büyük harf (A-Z)
- ✅ En az 1 küçük harf (a-z)
- ✅ En az 1 rakam (0-9)

Örnek: `Niko2026!`

### Test Modu

Backend'de `code` alanı döndürülür (sadece geliştirme için):

```json
{
  "success": true,
  "message": "Doğrulama kodu e-posta adresinize gönderildi.",
  "code": "855135"  // ← Bu satırı production'da kaldırın
}
```

## 🎨 Özelleştirme

### E-posta Şablonunu Değiştirme

`email_verification.py` dosyasında `html_body` değişkenini düzenleyin:

```python
html_body = f"""
<!DOCTYPE html>
<html>
<head>
    <style>
        /* Kendi CSS'inizi buraya ekleyin */
    </style>
</head>
<body>
    <!-- Kendi HTML'inizi buraya ekleyin -->
    <h1>Doğrulama Kodu: {code}</h1>
</body>
</html>
"""
```

### Geçerlilik Süresini Değiştirme

`email_verification.py` dosyasında:

```python
"expires_at": datetime.now() + timedelta(minutes=5)  # ← 5'i değiştirin
```

### Deneme Sayısını Değiştirme

`email_verification.py` dosyasında:

```python
if stored_data["attempts"] >= 5:  # ← 5'i değiştirin
```

## 📊 Sistem Akışı

```
┌─────────────┐
│  Kullanıcı  │
│  Kayıt Ol   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ E-posta var mı? │
└────┬────────┬───┘
     │ Evet   │ Hayır
     ▼        ▼
┌─────────┐  ┌──────────┐
│ Kod     │  │ Direkt   │
│ Gönder  │  │ Kayıt    │
└────┬────┘  └──────────┘
     │
     ▼
┌─────────────┐
│ Doğrulama   │
│ Ekranı Aç   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Kodu Gir    │
│ (6 hane)    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Kod Doğru?  │
└────┬────┬───┘
     │ Evet│ Hayır
     ▼     ▼
┌─────────┐ ┌──────────┐
│ Kayıt   │ │ Tekrar   │
│ Tamamla │ │ Dene     │
└─────────┘ └──────────┘
```

## 🔐 Güvenlik Notları

1. **Production'da `code` alanını kaldırın** (API yanıtından)
2. **HTTPS kullanın** (HTTP yerine)
3. **Rate limiting ekleyin** (aynı e-postaya çok fazla kod gönderilmesini engelleyin)
4. **Veritabanı kullanın** (bellekteki kod saklama yerine)

## 📞 Yardım

Sorun yaşarsanız:

1. **Test scripti çalıştırın:**
   ```bash
   python test_email_verification.py
   ```

2. **Backend loglarını kontrol edin**

3. **Elastic Email dashboard'unu kontrol edin:**
   - https://elasticemail.com/

---

**✨ Başarılar! Niko AI ile harika bir deneyim yaşayın!**
