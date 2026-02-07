# 🤖 E-posta Doğrulama Sistemi Kurulum Promptu

Bu promptu bir AI asistanına (ChatGPT, Claude, vb.) vererek sıfırdan e-posta doğrulama sistemi kurabilirsiniz.

---

## 📋 PROMPT BAŞLANGIÇ

```
Merhaba! Niko AI adlı bir sesli asistan projesi geliştiriyorum. 
Bu proje için e-posta doğrulama sistemi kurmak istiyorum.

## PROJE YAPISI

### Backend (Python - FastAPI)
- main.py: Ana FastAPI uygulaması
- users.json: Kullanıcı veritabanı (JSON dosyası)
- Mevcut endpoint'ler:
  - POST /register: Kullanıcı kaydı
  - POST /login: Kullanıcı girişi
  - GET /me: Profil bilgileri
  - PUT /me: Profil güncelleme

### Mobil Uygulama (Android - Java)
- MainActivity.java: Ana aktivite
- Mevcut metodlar:
  - registerRequest(): Kayıt işlemi
  - loginRequest(): Giriş işlemi
  - updateProfileRequest(): Profil güncelleme

## İHTİYAÇLARIM

### 1. E-posta Doğrulama Servisi (Python)

Bana şunları içeren bir `email_verification.py` dosyası oluştur:

**Gereksinimler:**
- Resend API kullanmalı (API Key: re_Ejpe1U4w_9RD9ByjtPfh4hfF6kSMcwh1v)
- Sadece Python standart kütüphaneleri kullan (http.client, json, random, datetime)
- Dış kütüphane KULLANMA (requests, resend-python vb. KULLANMA)
- 6 haneli rastgele doğrulama kodu üret
- Kodu bellekte sakla (5 dakika geçerli)
- Brute force koruması (maksimum 5 deneme)
- Premium HTML e-posta şablonu

**Metodlar:**
```python
class EmailVerificationService:
    def send_verification_email(to_email: str, username: str) -> dict
    def verify_code(email: str, code: str) -> dict
    def resend_code(email: str) -> dict
    def cleanup_expired_codes() -> None
```

**E-posta Şablonu Özellikleri:**
- Modern gradient tasarım
- Niko AI branding (🤖 emoji)
- Büyük, okunabilir kod gösterimi
- Güvenlik uyarıları
- Mobil uyumlu

### 2. FastAPI Endpoint'leri (main.py'ye ekle)

Bana şu endpoint'leri ekle:

```python
# Pydantic Modelleri
class EmailVerificationRequest(BaseModel):
    email: str
    username: str

class EmailVerificationCheck(BaseModel):
    email: str
    code: str

# Endpoint'ler
@app.post("/send-verification-code")
async def send_verification_code(request: EmailVerificationRequest)

@app.post("/verify-email-code")
async def verify_email_code(request: EmailVerificationCheck)

@app.post("/resend-verification-code")
async def resend_verification_code(request: EmailVerificationRequest)
```

**Import ekle:**
```python
from email_verification import email_service
```

### 3. Android Entegrasyonu (MainActivity.java'ya ekle)

Bana şu metodları ekle:

**Yeni Metodlar:**
```java
private void showEmailVerificationDialog(String username, String password, String email, String fullName)
private void sendVerificationCode(String email, String username)
private void verifyEmailCode(String email, String code, Runnable onSuccess)
private void performRegistration(String username, String password, String email, String fullName)
```

**registerRequest() metodunu güncelle:**
- E-posta varsa önce `showEmailVerificationDialog()` çağır
- E-posta yoksa direkt `performRegistration()` çağır

**Dialog Tasarımı:**
- Başlık: "📧 E-posta Doğrulama"
- Açıklama metni
- 6 haneli kod girişi (EditText - sadece rakam)
- "Doğrula ve Kayıt Ol" butonu
- "Kodu Tekrar Gönder" linki
- Modern, karanlık tema (#1a1a2e arka plan)
- Neon mavi vurgular (#00E5FF)

### 4. Test Scripti

Bana bir `test_email_verification.py` dosyası oluştur:

**Test Senaryoları:**
1. Kod gönderme testi
2. Doğru kod doğrulama
3. Yanlış kod doğrulama
4. Kod tekrar gönderme

**Test E-posta:** `delivered@resend.dev` (Resend test email)

### 5. Dokümantasyon

Bana şu dokümantasyon dosyalarını oluştur:

**EMAIL_VERIFICATION_README.md:**
- Sistem özellikleri
- Kurulum adımları
- API endpoint'leri
- Güvenlik notları
- Sorun giderme

**KULLANIM_KILAVUZU.md:**
- Kullanıcı için adım adım kılavuz
- Ekran görüntüleri açıklamaları
- Sorun giderme (kullanıcı dostu)

## ÖNEMLİ NOTLAR

1. **Dış Kütüphane Kullanma:**
   - ❌ requests
   - ❌ resend-python
   - ✅ http.client (standart)
   - ✅ json (standart)
   - ✅ random (standart)

2. **Güvenlik:**
   - Kodlar 5 dakika geçerli
   - Maksimum 5 deneme hakkı
   - Kod kullanıldıktan sonra silinmeli
   - Production'da API yanıtından `code` alanını kaldır

3. **Resend API:**
   - Endpoint: https://api.resend.com/emails
   - Method: POST
   - Header: Authorization: Bearer {api_key}
   - Test email: delivered@resend.dev

4. **Android:**
   - API URL: API_BASE_URL değişkenini kullan
   - Thread kullan (network işlemleri için)
   - runOnUiThread() ile UI güncellemeleri
   - Toast mesajları göster

5. **Hata Yönetimi:**
   - Try-catch blokları kullan
   - Kullanıcı dostu hata mesajları
   - Backend logları ekle
   - HTTP status kodlarını kontrol et

## BEKLENEN ÇIKTILAR

Lütfen bana şunları ver:

1. ✅ `email_verification.py` - Tam kod
2. ✅ `main.py` için eklemeler - Sadece eklenecek kısımlar
3. ✅ `MainActivity.java` için eklemeler - Sadece eklenecek kısımlar
4. ✅ `test_email_verification.py` - Tam kod
5. ✅ `EMAIL_VERIFICATION_README.md` - Tam dokümantasyon
6. ✅ `KULLANIM_KILAVUZU.md` - Kullanıcı kılavuzu

## ÖRNEK KULLANIM AKIŞI

1. Kullanıcı kayıt ekranını açar
2. Bilgileri girer (username, password, email, full_name)
3. "Kayıt Ol" butonuna tıklar
4. E-posta varsa:
   - Doğrulama kodu gönderilir
   - Dialog açılır
   - Kullanıcı kodu girer
   - Kod doğrulanır
   - Kayıt tamamlanır
5. E-posta yoksa:
   - Direkt kayıt olur

## TEST SENARYOSU

```bash
# Backend'i başlat
python main.py

# Test et
python test_email_verification.py

# Beklenen çıktı:
✅ E-posta başarıyla gönderildi: delivered@resend.dev
✅ Kod: 123456
✅ Doğrulama başarılı!
```

Lütfen tüm kodları ve dokümantasyonu detaylı bir şekilde hazırla.
Her dosya için açıklama ekle ve kullanımı göster.

Teşekkürler!
```

---

## 📝 PROMPT KULLANIM KILAVUZU

### 1. Promptu Kopyala

Yukarıdaki "PROMPT BAŞLANGIÇ" ile "PROMPT BİTİŞ" arasındaki tüm metni kopyalayın.

### 2. AI Asistanına Yapıştır

- ChatGPT (GPT-4)
- Claude (Sonnet/Opus)
- Gemini
- Veya başka bir AI asistan

### 3. Ek Bilgiler Ver (İsteğe Bağlı)

Eğer AI daha fazla bilgi isterse:

**main.py yapısı:**
```python
# Mevcut yapı
app = FastAPI()
auth_service = AuthService()
history_service = HistoryService()

@app.post("/register")
async def register(user: UserCreate):
    # Kayıt işlemi
    pass
```

**MainActivity.java yapısı:**
```java
public class MainActivity extends Activity {
    private static String API_BASE_URL = "...";
    private String authToken = null;
    
    private void registerRequest(String username, String password, 
                                 String email, String fullName) {
        // Kayıt işlemi
    }
}
```

### 4. Kodları Al ve Uygula

AI size dosyaları verecek. Sırayla:

1. `email_verification.py` oluştur
2. `main.py`'ye eklemeleri yap
3. `MainActivity.java`'ya eklemeleri yap
4. `test_email_verification.py` oluştur
5. Dokümantasyon dosyalarını oluştur

### 5. Test Et

```bash
python test_email_verification.py
```

## 🎯 PROMPT ÖZELLEŞTİRME

### Farklı E-posta Servisi İçin

```
Resend API yerine [SendGrid/Mailgun/AWS SES] kullan.
API Key: [your-api-key]
Endpoint: [api-endpoint]
```

### Farklı Kod Uzunluğu İçin

```
6 haneli kod yerine [4/8/10] haneli kod üret.
```

### Farklı Geçerlilik Süresi İçin

```
5 dakika yerine [10/15/30] dakika geçerli olsun.
```

### SMS Doğrulama İçin

```
E-posta yerine SMS doğrulama sistemi kur.
Twilio API kullan.
API Key: [your-api-key]
```

## 💡 İPUÇLARI

### AI'dan Daha İyi Sonuç Almak İçin

1. **Spesifik Ol:**
   - ❌ "E-posta doğrulama sistemi kur"
   - ✅ "Resend API ile 6 haneli kod gönderen sistem kur"

2. **Örnekler Ver:**
   - Mevcut kod yapınızı gösterin
   - Beklenen çıktıyı açıklayın

3. **Kısıtlamaları Belirt:**
   - "Dış kütüphane kullanma"
   - "Sadece http.client kullan"

4. **Adım Adım İste:**
   - "Önce email_verification.py oluştur"
   - "Sonra main.py'ye eklemeleri göster"

### Sorun Yaşarsanız

**AI kodu eksik verirse:**
```
Lütfen [dosya_adı] için tam kodu ver. 
Sadece eklenecek kısımları değil, tüm dosyayı göster.
```

**AI dış kütüphane kullanırsa:**
```
requests kütüphanesi kullanma. 
Sadece Python standart kütüphanesi http.client kullan.
Örnek kod göster.
```

**AI test kodu vermezse:**
```
Lütfen test_email_verification.py için tam bir test scripti oluştur.
Tüm fonksiyonları test etsin.
```

## 🔄 PROMPT VERSİYONLARI

### Minimal Versiyon (Hızlı)

```
Niko AI projem için Resend API (re_Ejpe1U4w_9RD9ByjtPfh4hfF6kSMcwh1v) 
kullanarak e-posta doğrulama sistemi kur.

Backend: Python FastAPI
Mobil: Android Java
Dış kütüphane kullanma, sadece http.client

Dosyalar:
1. email_verification.py (6 haneli kod, 5 dk geçerli)
2. main.py endpoint'leri
3. MainActivity.java metodları
4. test_email_verification.py
5. README.md

Detaylı kod ve açıklama ver.
```

### Detaylı Versiyon (Yukarıdaki Tam Prompt)

Yukarıdaki "PROMPT BAŞLANGIÇ" bölümünü kullanın.

### Özelleştirilmiş Versiyon

Kendi ihtiyaçlarınıza göre promptu düzenleyin.

---

## 📞 DESTEK

Bu promptu kullanırken sorun yaşarsanız:

1. AI'ya daha spesifik sorular sorun
2. Mevcut kod yapınızı gösterin
3. Hata mesajlarını paylaşın
4. Adım adım ilerleyin

**Örnek Takip Sorusu:**
```
email_verification.py dosyasını oluşturdun ama 
send_verification_email metodunda Resend API çağrısı 
http.client ile nasıl yapılır göster.
```

---

**✨ Bu promptu kullanarak herhangi bir AI asistanı ile 
e-posta doğrulama sistemi kurabilirsiniz!**

**Hazırlayan:** Niko AI Team  
**Tarih:** 2026  
**Versiyon:** 1.0.0
