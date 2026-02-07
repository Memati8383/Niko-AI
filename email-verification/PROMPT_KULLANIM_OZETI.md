# 🎯 Prompt Kullanım Özeti - Hızlı Başlangıç

## 📋 3 Adımda E-posta Doğrulama Sistemi Kur

### Adım 1: Promptu Seç

**Hızlı Prototip İçin:**
```bash
QUICK_PROMPT.txt
```

**Production İçin:**
```bash
AI_PROMPT_EMAIL_VERIFICATION.md
```

### Adım 2: AI'ya Ver

1. Dosyayı aç
2. İçeriği kopyala
3. ChatGPT/Claude'a yapıştır
4. Enter'a bas

### Adım 3: Kodları Uygula

AI size 5 dosya verecek:
1. ✅ `email_verification.py`
2. ✅ `main.py` (eklemeler)
3. ✅ `MainActivity.java` (eklemeler)
4. ✅ `test_email_verification.py`
5. ✅ `README.md`

---

## 🚀 QUICK_PROMPT.txt Kullanımı

### Kopyala-Yapıştır

```
Niko AI projem için e-posta doğrulama sistemi kur.

BACKEND: Python FastAPI
- email_verification.py: Resend API (re_Ejpe1U4w_9RD9ByjtPfh4hfF6kSMcwh1v)
- 6 haneli kod, 5 dakika geçerli, 5 deneme hakkı
- Sadece http.client kullan (requests KULLANMA)
- Premium HTML e-posta şablonu

ENDPOINT'LER (main.py):
- POST /send-verification-code
- POST /verify-email-code  
- POST /resend-verification-code

MOBİL: Android Java (MainActivity.java)
- showEmailVerificationDialog()
- sendVerificationCode()
- verifyEmailCode()
- performRegistration()
- registerRequest() metodunu güncelle (e-posta varsa dialog aç)

TEST: test_email_verification.py
- Test email: delivered@resend.dev

DÖKÜMANTASYON:
- EMAIL_VERIFICATION_README.md
- KULLANIM_KILAVUZU.md

Tüm dosyalar için tam kod ver. Detaylı açıklama ekle.
```

### Beklenen Süre
⏱️ **5 dakika**

---

## 📚 AI_PROMPT_EMAIL_VERIFICATION.md Kullanımı

### Nasıl Kullanılır?

1. **Dosyayı Aç:**
   ```bash
   cat AI_PROMPT_EMAIL_VERIFICATION.md
   ```

2. **"PROMPT BAŞLANGIÇ" Bölümünü Bul**

3. **Tüm Metni Kopyala** (başlangıçtan bitişe kadar)

4. **AI Asistanına Yapıştır**

5. **Bekle** (AI tüm dosyaları oluşturacak)

### Beklenen Süre
⏱️ **15 dakika**

---

## 💡 Hangi AI Asistanını Kullanmalıyım?

| AI | Öneri | Neden? |
|----|-------|--------|
| **ChatGPT-4** | ⭐⭐⭐⭐⭐ | En iyi kod kalitesi |
| **Claude Opus** | ⭐⭐⭐⭐⭐ | En detaylı açıklamalar |
| **Claude Sonnet** | ⭐⭐⭐⭐ | Hızlı ve kaliteli |
| **Gemini Pro** | ⭐⭐⭐ | Basit görevler için |

**Önerimiz:** ChatGPT-4 veya Claude Opus

---

## 🔧 Özelleştirme Örnekleri

### Farklı E-posta Servisi

**Prompta ekle:**
```
Resend yerine SendGrid kullan.
API Key: SG.xxxxx
Endpoint: https://api.sendgrid.com/v3/mail/send
```

### SMS Doğrulama

**Prompta ekle:**
```
E-posta yerine SMS doğrulama.
Twilio API kullan.
API Key: ACxxxxx
```

### 4 Haneli Kod

**Prompta ekle:**
```
6 haneli yerine 4 haneli kod kullan.
```

### 10 Dakika Geçerlilik

**Prompta ekle:**
```
5 dakika yerine 10 dakika geçerli olsun.
```

---

## 🐛 Sorun Giderme

### AI Eksik Kod Verirse

**Sor:**
```
Lütfen [dosya_adı] için tam kodu ver.
Sadece eklenecek kısımları değil, tüm dosyayı göster.
```

### AI Dış Kütüphane Kullanırsa

**Sor:**
```
requests kütüphanesi kullanma.
Sadece Python standart kütüphanesi http.client kullan.
Örnek kod göster.
```

### AI Test Kodu Vermezse

**Sor:**
```
Lütfen test_email_verification.py için tam bir test scripti oluştur.
Tüm fonksiyonları test etsin.
```

---

## ✅ Kontrol Listesi

Kodları aldıktan sonra kontrol edin:

### Backend (Python)
- [ ] `email_verification.py` oluşturuldu
- [ ] `main.py`'ye import eklendi
- [ ] 3 endpoint eklendi
- [ ] Pydantic modelleri eklendi

### Mobil (Android)
- [ ] 4 yeni metod eklendi
- [ ] `registerRequest()` güncellendi
- [ ] Dialog tasarımı eklendi
- [ ] API çağrıları yapıldı

### Test
- [ ] `test_email_verification.py` oluşturuldu
- [ ] Test çalışıyor
- [ ] E-posta gönderiliyor
- [ ] Kod doğrulanıyor

### Dokümantasyon
- [ ] README oluşturuldu
- [ ] Kullanım kılavuzu oluşturuldu
- [ ] API endpoint'leri dokümante edildi

---

## 🎯 Hızlı Test

```bash
# 1. Backend'i başlat
python main.py

# 2. Test et
python test_email_verification.py

# Beklenen çıktı:
✅ E-posta başarıyla gönderildi
✅ Kod: 123456
✅ Doğrulama başarılı!
```

---

## 📞 Yardım

### Prompt Çalışmıyorsa

1. **AI'ya daha spesifik sor**
2. **Mevcut kod yapınızı göster**
3. **Hata mesajlarını paylaş**
4. **Adım adım ilerle**

### Örnek Takip Sorusu

```
email_verification.py dosyasını oluşturdun ama 
send_verification_email metodunda Resend API çağrısı 
http.client ile nasıl yapılır göster.

Örnek:
conn = http.client.HTTPSConnection("api.resend.com")
headers = {"Authorization": "Bearer xxx"}
...
```

---

## 🎁 Bonus: Hazır Komutlar

### Tüm Dosyaları Görüntüle

```bash
# Promptları göster
cat QUICK_PROMPT.txt
cat AI_PROMPT_EMAIL_VERIFICATION.md

# Karşılaştırma
cat PROMPT_COMPARISON.md
```

### Test Et

```bash
# Backend
python main.py

# Test
python test_email_verification.py
```

### Dokümantasyon

```bash
# README'leri oku
cat EMAIL_VERIFICATION_README.md
cat KULLANIM_KILAVUZU.md
cat RESEND_SETUP.md
```

---

## 🌟 Başarı İpuçları

### 1. Doğru Promptu Seç
- Prototip → QUICK_PROMPT.txt
- Production → AI_PROMPT_EMAIL_VERIFICATION.md

### 2. Spesifik Ol
- ❌ "E-posta sistemi kur"
- ✅ "Resend API ile 6 haneli kod gönderen sistem kur"

### 3. Örnekler Ver
- Mevcut kod yapınızı gösterin
- Beklenen çıktıyı açıklayın

### 4. Adım Adım İlerle
- Önce backend
- Sonra mobil
- En son test

### 5. Test Et
- Her adımda test edin
- Hataları hemen düzeltin

---

## 📊 Özet Tablo

| Özellik | QUICK | FULL |
|---------|-------|------|
| Süre | 5 dk | 15 dk |
| Detay | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Dokümantasyon | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Production Ready | ❌ | ✅ |

---

**✨ Şimdi başlayın! Promptu kopyalayın ve AI'ya verin!**

**Hazırlayan:** Niko AI Team  
**Tarih:** 2026  
**Versiyon:** 1.0.0

---

## 🚀 Hemen Başla

```bash
# 1. Promptu kopyala
cat QUICK_PROMPT.txt

# 2. ChatGPT'ye yapıştır
# https://chat.openai.com

# 3. Kodları al ve uygula

# 4. Test et
python test_email_verification.py

# 5. Başarı! 🎉
```
