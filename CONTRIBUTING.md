# Katkıda Bulunma Rehberi

Niko AI projesine katkıda bulunmak istediğiniz için teşekkür ederiz! Bu rehber, projeye nasıl katkıda bulunabileceğinizi açıklar.

## 🚀 Başlarken

1. **Repository'yi Fork Edin**
   - GitHub'da projeyi fork edin
   - Yerel makinenize klonlayın:
     ```bash
     git clone https://github.com/KULLANICI_ADINIZ/Niko-AI.git
     cd Niko-AI
     ```

2. **Geliştirme Ortamını Kurun**

   ```bash
   python -m venv .venv
   .venv\Scripts\activate  # Windows
   pip install -r requirements.txt
   ```

3. **Yeni Bir Branch Oluşturun**
   ```bash
   git checkout -b feature/yeni-ozellik
   ```

## 📝 Kod Standartları

- **Dil:** Tüm kod içi dokümantasyon ve yorumlar Türkçe olmalıdır
- **Stil:** PEP 8 standartlarına uyun
- **Fonksiyonlar:** Her fonksiyon için Türkçe docstring yazın
- **Değişkenler:** Anlamlı ve Türkçe değişken isimleri kullanın

## 🔄 Pull Request Süreci

1. Değişikliklerinizi commit edin:

   ```bash
   git add .
   git commit -m "feat: yeni özellik eklendi"
   ```

2. Branch'inizi push edin:

   ```bash
   git push origin feature/yeni-ozellik
   ```

3. GitHub'da Pull Request oluşturun

## 🐛 Bug Raporlama

Bug bulduğunuzda lütfen şu bilgileri ekleyin:

- Bug'ın açıklaması
- Yeniden üretme adımları
- Beklenen davranış
- Gerçek davranış
- Ekran görüntüleri (varsa)
- Sistem bilgileri (OS, Python versiyonu, vb.)

## 💡 Özellik Önerileri

Yeni özellik önerilerinizi GitHub Issues üzerinden paylaşabilirsiniz. Lütfen:

- Özelliğin amacını açıklayın
- Kullanım senaryolarını belirtin
- Mümkünse mockup veya örnek ekleyin

## 📋 Commit Mesaj Formatı

```
tip: kısa açıklama

Detaylı açıklama (opsiyonel)
```

**Tipler:**

- `feat`: Yeni özellik
- `fix`: Bug düzeltmesi
- `docs`: Dokümantasyon değişikliği
- `style`: Kod formatı değişikliği
- `refactor`: Kod yeniden yapılandırma
- `test`: Test ekleme/düzeltme
- `chore`: Bakım işleri

## 🧪 Test

Değişikliklerinizi test etmek için:

```bash
pytest test_validation.py
```

## 📞 İletişim

Sorularınız için GitHub Issues kullanabilirsiniz.

---

Teşekkürler! 🙏
