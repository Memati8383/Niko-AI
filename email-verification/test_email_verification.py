"""
E-posta Doğrulama Sistemi Test Scripti (Resend API)
"""

from email_verification import email_service

def test_send_code():
    """Doğrulama kodu gönderme testi"""
    print("=" * 60)
    print("TEST 1: Doğrulama Kodu Gönderme (Resend API)")
    print("=" * 60)
    
    # Resend test email - otomatik test için
    test_email = "delivered@resend.dev"  # Resend test email
    print(f"Test e-posta: {test_email}")
    
    result = email_service.send_verification_email(
        to_email=test_email,
        username="TestKullanici"
    )
    
    print(f"Başarılı: {result['success']}")
    print(f"Mesaj: {result['message']}")
    if 'code' in result:
        print(f"Kod (Test için): {result['code']}")
    print()
    
    return result.get('code'), test_email

def test_verify_code(email, code):
    """Doğrulama kodu kontrolü testi"""
    print("=" * 60)
    print("TEST 2: Doğrulama Kodu Kontrolü")
    print("=" * 60)
    
    # Doğru kod
    result = email_service.verify_code(email, code)
    print(f"Doğru Kod - Başarılı: {result['success']}")
    print(f"Mesaj: {result['message']}")
    print()

def test_wrong_code(email):
    """Yanlış kod testi"""
    print("=" * 60)
    print("TEST 3: Yanlış Kod Kontrolü")
    print("=" * 60)
    
    result = email_service.verify_code(email, "000000")
    print(f"Yanlış Kod - Başarılı: {result['success']}")
    print(f"Mesaj: {result['message']}")
    print()

def test_expired_code():
    """Süresi dolmuş kod testi"""
    print("=" * 60)
    print("TEST 4: Süresi Dolmuş Kod (Manuel Test)")
    print("=" * 60)
    print("Bu test için 5 dakika bekleyin ve kodu tekrar deneyin.")
    print()

def test_resend_code(email):
    """Kod tekrar gönderme testi"""
    print("=" * 60)
    print("TEST 5: Kod Tekrar Gönderme")
    print("=" * 60)
    
    result = email_service.resend_code(email)
    print(f"Başarılı: {result['success']}")
    print(f"Mesaj: {result['message']}")
    if 'code' in result:
        print(f"Yeni Kod (Test için): {result['code']}")
    print()

if __name__ == "__main__":
    print("\n🚀 E-POSTA DOĞRULAMA SİSTEMİ TEST BAŞLIYOR (Resend API)\n")
    
    # Test 1: Kod gönder
    result = test_send_code()
    code = result[0] if result else None
    email = result[1] if result and len(result) > 1 else "delivered@resend.dev"
    
    if code:
        # Test 2: Doğru kodu kontrol et
        test_verify_code(email, code)
        
        # Test 3: Yanlış kod
        # test_wrong_code(email)
        
        # Test 5: Kod tekrar gönder
        # test_resend_code(email)
    
    print("✅ TESTLER TAMAMLANDI\n")
    print("📧 E-postanızı kontrol edin!")
    print("🔐 Mobil uygulamada kayıt olurken e-posta girdiğinizde doğrulama ekranı açılacak.")
    print("\n💡 NOT: Resend API kullanıyorsunuz. Test için 'delivered@resend.dev' kullanabilirsiniz.")
