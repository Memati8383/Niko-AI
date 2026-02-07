# Güvenlik Politikası

## 🔒 Desteklenen Versiyonlar

| Versiyon | Destek Durumu |
| ------- | ------------- |
| 1.0.x   | ✅ Destekleniyor |
| < 1.0   | ❌ Desteklenmiyor |

## 🐛 Güvenlik Açığı Bildirimi

Niko AI'da bir güvenlik açığı bulduysanız, lütfen sorumlu bir şekilde bildirin.

### Bildirme Süreci

1. **GitHub Issues kullanmayın** - Güvenlik açıkları herkese açık olmamalıdır
2. Proje sahibine özel mesaj gönderin
3. Aşağıdaki bilgileri ekleyin:
   - Açığın detaylı açıklaması
   - Yeniden üretme adımları
   - Potansiyel etki analizi
   - Önerilen çözüm (varsa)

### Yanıt Süresi

- İlk yanıt: 48 saat içinde
- Düzeltme süresi: Kritikliğe göre 7-30 gün

## 🛡️ Güvenlik En İyi Uygulamaları

### Kullanıcılar İçin

1. **Şifreler:**
   - Güçlü şifreler kullanın (min. 8 karakter)
   - Şifreleri düzenli olarak değiştirin
   - Aynı şifreyi farklı servislerde kullanmayın

2. **API Anahtarları:**
   - `.env` dosyasını asla paylaşmayın
   - API anahtarlarını GitHub'a yüklemeyin
   - Düzenli olarak yenileyin

3. **Güncellemeler:**
   - Sistemi düzenli olarak güncelleyin
   - Güvenlik yamalarını hemen uygulayın

### Geliştiriciler İçin

1. **Kod Güvenliği:**
   - Kullanıcı girdilerini her zaman doğrulayın
   - SQL injection'a karşı korunun
   - XSS saldırılarına karşı önlem alın

2. **Bağımlılıklar:**
   - `requirements.txt` dosyasını güncel tutun
   - Bilinen güvenlik açığı olan paketleri kullanmayın
   - Düzenli olarak `pip audit` çalıştırın

3. **Kimlik Doğrulama:**
   - JWT token'ları güvenli saklayın
   - Token süre sınırlarını uygun ayarlayın
   - Hassas işlemler için ek doğrulama kullanın

## 🔐 Veri Güvenliği

- Kullanıcı şifreleri bcrypt ile hashlenir
- JWT token'lar HS256 algoritması ile imzalanır
- Hassas veriler `.env` dosyasında saklanır
- Sohbet geçmişleri kullanıcı bazlı izole edilir

## 📋 Bilinen Güvenlik Konuları

Şu anda bilinen kritik güvenlik açığı bulunmamaktadır.

## 🔄 Güvenlik Güncellemeleri

Güvenlik güncellemeleri için:
- GitHub Releases sayfasını takip edin
- CHANGELOG.md dosyasını kontrol edin
- Otomatik güncelleme özelliğini aktif tutun

---

Güvenliğiniz bizim için önemlidir. Teşekkürler! 🙏
