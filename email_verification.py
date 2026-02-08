"""
Niko AI - E-posta Doğrulama Servisi
Resend API kullanarak e-posta doğrulama işlemleri

Özellikler:
- 6 haneli rastgele doğrulama kodu
- 5 dakika geçerlilik süresi
- Brute force koruması (maksimum 5 deneme)
- Premium HTML e-posta şablonu
"""

import http.client
import json
import random
import string
import ssl
from datetime import datetime, timedelta, timezone
from typing import Dict, Optional
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os


class EmailVerificationService:
    """
    E-posta doğrulama servisi.
    Resend API kullanarak doğrulama e-postaları gönderir ve kodları yönetir.
    """
    
    def __init__(self, api_key: str = None):
        """
        EmailVerificationService'i başlat.
        
        Args:
            api_key: Resend API anahtarı (.env dosyasından veya direkt parametre)
        """
        import os
        self.api_key = api_key or os.getenv("RESEND_API_KEY", "")
        
        # SMTP Ayarları (Gmail vb. için)
        self.smtp_email = os.getenv("SMTP_EMAIL", "")
        self.smtp_password = os.getenv("SMTP_PASSWORD", "")
        self.use_smtp = bool(self.smtp_email and self.smtp_password)
        
        if self.use_smtp:
            self.from_email = f"Niko AI <{self.smtp_email}>"
            print(f"SMTP EMAIL ACTIVE: {self.smtp_email}")
        else:
            self.from_email = "Niko AI <onboarding@resend.dev>"
            print("EMAIL MODE: RESEND API (Test Mode)")
        
        # Bellekte saklanan kodlar
        # Format: {email: {"code": str, "expires_at": datetime, "attempts": int, "username": str}}
        self._verification_codes: Dict[str, dict] = {}
        
        # Yapılandırma
        self.CODE_LENGTH = 6
        self.CODE_EXPIRY_MINUTES = 5
        self.MAX_ATTEMPTS = 5
        self.RESEND_COOLDOWN_SECONDS = 60
        
        # İzin verilen e-posta sağlayıcıları (domain listesi)
        self.ALLOWED_EMAIL_DOMAINS = [
            # Google
            "gmail.com", "googlemail.com",
            # Microsoft
            "hotmail.com", "hotmail.co.uk", "hotmail.fr", "hotmail.de", "hotmail.it",
            "outlook.com", "outlook.co.uk", "outlook.fr", "outlook.de",
            "live.com", "live.co.uk", "live.fr",
            "msn.com",
            # Yahoo
            "yahoo.com", "yahoo.co.uk", "yahoo.fr", "yahoo.de", "yahoo.com.tr",
            "ymail.com", "rocketmail.com",
            # Yandex
            "yandex.com", "yandex.ru", "yandex.com.tr", "yandex.ua",
            # iCloud / Apple
            "icloud.com", "me.com", "mac.com",
            # ProtonMail
            "protonmail.com", "proton.me", "pm.me",
            # Diğer popüler servisler
            "aol.com",
            "zoho.com",
            "mail.com",
            "gmx.com", "gmx.de", "gmx.net",
            # Türkiye'ye özel
            "mynet.com", "superonline.com", "turk.net",
        ]
    
    def is_allowed_email_provider(self, email: str) -> bool:
        """
        E-posta adresinin izin verilen sağlayıcılardan biri olup olmadığını kontrol et.
        
        Args:
            email: E-posta adresi
        
        Returns:
            bool: İzin verilen sağlayıcıysa True
        """
        if not email or "@" not in email:
            return False
        
        domain = email.lower().split("@")[-1]
        return domain in self.ALLOWED_EMAIL_DOMAINS
    
    def get_allowed_providers_message(self) -> str:
        """İzin verilen sağlayıcıların listesini kullanıcı dostu mesaj olarak döndür."""
        return "Gmail, Hotmail, Outlook, Yahoo, Yandex, iCloud veya ProtonMail"
    
    def _generate_code(self) -> str:
        """6 haneli rastgele doğrulama kodu üret."""
        return ''.join(random.choices(string.digits, k=self.CODE_LENGTH))
    
    def _get_html_template(self, username: str, code: str) -> str:
        """Premium HTML e-posta şablonu oluştur."""
        return f'''
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Niko AI - E-posta Doğrulama</title>
</head>
<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f0f23;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td align="center" style="padding: 40px 0;">
                <table role="presentation" style="width: 100%; max-width: 600px; border-collapse: collapse; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); border-radius: 24px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);">
                    
                    <!-- Header -->
                    <tr>
                        <td style="padding: 48px 40px 32px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1);">
                            <div style="width: 80px; height: 80px; margin: 0 auto 24px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 20px; display: flex; align-items: center; justify-content: center; box-shadow: 0 10px 40px rgba(102, 126, 234, 0.4);">
                                <table role="presentation" style="width: 80px; height: 80px;">
                                    <tr>
                                        <td align="center" valign="middle">
                                            <span style="font-size: 36px; font-weight: 700; color: #ffffff; text-shadow: 0 2px 4px rgba(0,0,0,0.2);">N</span>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <h1 style="margin: 0; font-size: 28px; font-weight: 700; color: #ffffff; letter-spacing: -0.5px;">Niko AI</h1>
                            <p style="margin: 8px 0 0; font-size: 14px; color: rgba(255,255,255,0.6); text-transform: uppercase; letter-spacing: 2px;">Sesli Asistan</p>
                        </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                        <td style="padding: 48px 40px;">
                            <h2 style="margin: 0 0 16px; font-size: 24px; font-weight: 600; color: #ffffff; text-align: center;">Merhaba, {username}! 👋</h2>
                            <p style="margin: 0 0 32px; font-size: 16px; line-height: 1.6; color: rgba(255,255,255,0.8); text-align: center;">
                                E-posta adresinizi doğrulamak için aşağıdaki kodu kullanın.
                            </p>
                            
                            <!-- Verification Code Box -->
                            <div style="background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%); border: 2px solid rgba(102, 126, 234, 0.4); border-radius: 16px; padding: 32px; text-align: center; margin-bottom: 32px;">
                                <p style="margin: 0 0 12px; font-size: 12px; color: rgba(255,255,255,0.5); text-transform: uppercase; letter-spacing: 3px;">Doğrulama Kodu</p>
                                <div style="font-size: 48px; font-weight: 700; letter-spacing: 12px; color: #667eea; font-family: 'Courier New', monospace; text-shadow: 0 0 20px rgba(102, 126, 234, 0.5);">
                                    {code}
                                </div>
                            </div>
                            
                            <!-- Timer Warning -->
                            <div style="background: rgba(255, 107, 107, 0.1); border-left: 4px solid #ff6b6b; border-radius: 8px; padding: 16px 20px; margin-bottom: 32px;">
                                <p style="margin: 0; font-size: 14px; color: rgba(255,255,255,0.8);">
                                    <span style="color: #ff6b6b; font-weight: 600;">⏱️ Önemli:</span> Bu kod <strong>5 dakika</strong> içinde geçerliliğini yitirecektir.
                                </p>
                            </div>
                            
                            <!-- Security Notice -->
                            <p style="margin: 0; font-size: 13px; line-height: 1.6; color: rgba(255,255,255,0.5); text-align: center;">
                                Bu e-postayı siz talep etmediyseniz, lütfen görmezden gelin.<br>
                                Hesabınız güvende ve herhangi bir işlem yapmanıza gerek yok.
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="padding: 32px 40px; background: rgba(0,0,0,0.2); border-top: 1px solid rgba(255,255,255,0.05);">
                            <table role="presentation" style="width: 100%;">
                                <tr>
                                    <td style="text-align: center;">
                                        <p style="margin: 0 0 8px; font-size: 14px; color: rgba(255,255,255,0.6);">
                                            ❤️ Niko AI ile yapıldı
                                        </p>
                                        <p style="margin: 0; font-size: 12px; color: rgba(255,255,255,0.4);">
                                            © 2025 Niko AI. Tüm hakları saklıdır.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
'''
    
    def _send_email_via_resend(self, to_email: str, subject: str, html_content: str) -> dict:
        """
        Resend API kullanarak e-posta gönder.
        Sadece http.client kullanır, dış kütüphane gerektirmez.
        
        Returns:
            dict: {"success": bool, "message": str, "id": str (başarılıysa)}
        """
        if not self.api_key:
            return {
                "success": False,
                "message": "API anahtarı yapılandırılmamış"
            }
        
        try:
            # SSL bağlamı oluştur
            context = ssl.create_default_context()
            
            # Resend API'ye bağlan
            conn = http.client.HTTPSConnection("api.resend.com", context=context)
            
            # İstek gövdesi
            payload = json.dumps({
                "from": self.from_email,
                "to": [to_email],
                "subject": subject,
                "html": html_content
            })
            
            # Headers
            headers = {
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json"
            }
            
            # POST isteği gönder
            conn.request("POST", "/emails", payload, headers)
            
            # Yanıtı al
            response = conn.getresponse()
            response_data = response.read().decode("utf-8")
            
            conn.close()
            
            # Yanıtı parse et
            if response.status == 200:
                result = json.loads(response_data)
                return {
                    "success": True,
                    "message": "E-posta başarıyla gönderildi",
                    "id": result.get("id", "")
                }
            else:
                error_data = json.loads(response_data) if response_data else {}
                return {
                    "success": False,
                    "message": error_data.get("message", f"API hatası: {response.status}")
                }
                
        except json.JSONDecodeError:
            return {
                "success": False,
                "message": "API yanıtı parse edilemedi"
            }
        except Exception as e:
            return {
                "success": False,
                "message": f"Bağlantı hatası: {str(e)}"
            }
    
    
    def _send_email_via_smtp(self, to_email: str, subject: str, html_content: str) -> dict:
        """
        Gmail SMTP kullanarak e-posta gönder.
        Ücretsiz ve herhangi bir adrese gönderim sağlar.
        """
        try:
            msg = MIMEMultipart()
            msg['From'] = self.from_email
            msg['To'] = to_email
            msg['Subject'] = subject
            msg.attach(MIMEText(html_content, 'html'))
            
            # Gmail SMTP Sunucusu
            if not self.smtp_email or not self.smtp_password:
                return {"success": False, "message": "SMTP bilgileri eksik (.env kontrol edin)"}

            print(f"Sending via Gmail SMTP to: {to_email}")
            server = smtplib.SMTP("smtp.gmail.com", 587)
            server.starttls()
            server.login(self.smtp_email, self.smtp_password)
            server.send_message(msg)
            server.quit()
                
            return {
                "success": True, 
                "message": "E-posta başarıyla gönderildi (SMTP)",
                "id": "smtp-sent"
            }
        except Exception as e:
            return {"success": False, "message": f"SMTP Hatası: {str(e)}"}

    def send_verification_email(self, to_email: str, username: str) -> dict:
        """
        Doğrulama e-postası gönder.
        
        Args:
            to_email: Hedef e-posta adresi
            username: Kullanıcı adı (şablonda kullanılır)
        
        Returns:
            dict: {
                "success": bool,
                "message": str,
                "expires_at": str (ISO format, başarılıysa)
            }
        """
        # E-posta formatı kontrolü
        import re
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, to_email):
            return {
                "success": False,
                "message": "Geçersiz e-posta formatı"
            }
        
        # İzin verilen sağlayıcı kontrolü
        if not self.is_allowed_email_provider(to_email):
            return {
                "success": False,
                "message": f"Desteklenmeyen e-posta sağlayıcısı. Lütfen {self.get_allowed_providers_message()} kullanın"
            }
        
        # Mevcut kod var mı kontrol et (cooldown)
        if to_email in self._verification_codes:
            existing = self._verification_codes[to_email]
            created_at = existing.get("created_at")
            
            if created_at:
                now = datetime.now(timezone.utc)
                elapsed = (now - created_at).total_seconds()
                
                if elapsed < self.RESEND_COOLDOWN_SECONDS:
                    remaining = int(self.RESEND_COOLDOWN_SECONDS - elapsed)
                    return {
                        "success": False,
                        "message": f"Lütfen {remaining} saniye bekleyin"
                    }
        
        # Yeni kod üret
        code = self._generate_code()
        now = datetime.now(timezone.utc)
        expires_at = now + timedelta(minutes=self.CODE_EXPIRY_MINUTES)
        
        # HTML şablonu oluştur
        html_content = self._get_html_template(username, code)
        
        # E-posta gönder (SMTP veya Resend)
        if self.use_smtp:
            result = self._send_email_via_smtp(
                to_email=to_email,
                subject=f"🔐 Niko AI - Doğrulama Kodunuz: {code}",
                html_content=html_content
            )
        else:
            result = self._send_email_via_resend(
                to_email=to_email,
                subject=f"🔐 Niko AI - Doğrulama Kodunuz: {code}",
                html_content=html_content
            )
        
        if result["success"]:
            # Kodu bellekte sakla
            self._verification_codes[to_email] = {
                "code": code,
                "expires_at": expires_at,
                "created_at": now,
                "attempts": 0,
                "username": username
            }
            
            return {
                "success": True,
                "message": "Doğrulama kodu e-postanıza gönderildi",
                "expires_at": expires_at.isoformat()
            }
        else:
            return result
    
    def verify_code(self, email: str, code: str) -> dict:
        """
        Doğrulama kodunu kontrol et.
        
        Args:
            email: E-posta adresi
            code: Kullanıcının girdiği kod
        
        Returns:
            dict: {
                "success": bool,
                "message": str,
                "verified": bool (başarılıysa)
            }
        """
        # E-posta için kayıt var mı?
        if email not in self._verification_codes:
            return {
                "success": False,
                "message": "Bu e-posta için aktif doğrulama kodu bulunamadı",
                "verified": False
            }
        
        record = self._verification_codes[email]
        now = datetime.now(timezone.utc)
        
        # Kod süresi dolmuş mu?
        if now > record["expires_at"]:
            # Süresi dolmuş kodu temizle
            del self._verification_codes[email]
            return {
                "success": False,
                "message": "Doğrulama kodunun süresi dolmuş. Lütfen yeni kod isteyin",
                "verified": False
            }
        
        # Maksimum deneme sayısı aşıldı mı? (Brute force koruması)
        if record["attempts"] >= self.MAX_ATTEMPTS:
            # Kodu geçersiz kıl
            del self._verification_codes[email]
            return {
                "success": False,
                "message": "Maksimum deneme sayısına ulaşıldı. Lütfen yeni kod isteyin",
                "verified": False
            }
        
        # Deneme sayısını artır
        record["attempts"] += 1
        
        # Kod doğru mu?
        if record["code"] == code.strip():
            # Başarılı doğrulama - kodu temizle
            username = record.get("username", "")
            del self._verification_codes[email]
            
            return {
                "success": True,
                "message": "E-posta başarıyla doğrulandı",
                "verified": True,
                "username": username
            }
        else:
            remaining = self.MAX_ATTEMPTS - record["attempts"]
            return {
                "success": False,
                "message": f"Geçersiz kod. {remaining} deneme hakkınız kaldı",
                "verified": False
            }
    
    def resend_code(self, email: str) -> dict:
        """
        Yeni doğrulama kodu gönder.
        
        Args:
            email: E-posta adresi
        
        Returns:
            dict: {
                "success": bool,
                "message": str,
                "expires_at": str (ISO format, başarılıysa)
            }
        """
        # Mevcut kayıt var mı? (Kullanıcı adını al)
        username = "Kullanıcı"
        if email in self._verification_codes:
            username = self._verification_codes[email].get("username", "Kullanıcı")
        
        # Eski kodu sil ve yenisini gönder
        if email in self._verification_codes:
            del self._verification_codes[email]
        
        return self.send_verification_email(email, username)
    
    def cleanup_expired_codes(self) -> int:
        """
        Süresi dolmuş tüm doğrulama kodlarını temizle.
        
        Returns:
            int: Temizlenen kod sayısı
        """
        now = datetime.now(timezone.utc)
        expired_emails = []
        
        for email, record in self._verification_codes.items():
            if now > record["expires_at"]:
                expired_emails.append(email)
        
        for email in expired_emails:
            del self._verification_codes[email]
        
        return len(expired_emails)
    
    def get_pending_verification(self, email: str) -> Optional[dict]:
        """
        Bekleyen doğrulama bilgisini getir (debug/admin için).
        
        Args:
            email: E-posta adresi
        
        Returns:
            dict veya None: Bekleyen doğrulama bilgisi
        """
        if email not in self._verification_codes:
            return None
        
        record = self._verification_codes[email]
        return {
            "email": email,
            "expires_at": record["expires_at"].isoformat(),
            "attempts": record["attempts"],
            "max_attempts": self.MAX_ATTEMPTS,
            "username": record.get("username", "")
        }
    
    def has_pending_verification(self, email: str) -> bool:
        """
        E-posta için bekleyen doğrulama var mı kontrol et.
        
        Args:
            email: E-posta adresi
        
        Returns:
            bool: Bekleyen doğrulama varsa True
        """
        if email not in self._verification_codes:
            return False
        
        # Süresi dolmuş mu kontrol et
        record = self._verification_codes[email]
        now = datetime.now(timezone.utc)
        
        if now > record["expires_at"]:
            del self._verification_codes[email]
            return False
        
        return True


# Singleton instance
_email_service: Optional[EmailVerificationService] = None


def get_email_service() -> EmailVerificationService:
    """
    EmailVerificationService singleton instance'ını getir.
    
    Returns:
        EmailVerificationService: Servis instance'ı
    """
    global _email_service
    if _email_service is None:
        _email_service = EmailVerificationService()
    return _email_service


# ============================================================================
# Kullanım Örnekleri
# ============================================================================

if __name__ == "__main__":
    from dotenv import load_dotenv
    load_dotenv()
    
    # Test için örnek kullanım
    service = EmailVerificationService(api_key="re_Ejpe1U4w_9RD9ByjtPfh4hfF6kSMcwh1v")
    
    print("=== Niko AI E-posta Doğrulama Servisi ===\n")
    
    # 1. Doğrulama kodu gönder
    result = service.send_verification_email(
        to_email="atarroket311@gmail.com",
        username="TestKullanici"
    )
    print(f"1. Kod Gönderme: {result}\n")
    
    # 2. Yanlış kod ile doğrulama dene
    result = service.verify_code("atarroket311@gmail.com", "000000")
    print(f"2. Yanlış Kod: {result}\n")
    
    # 3. Bekleyen doğrulama bilgisi
    info = service.get_pending_verification("atarroket311@gmail.com")
    print(f"3. Bekleyen Doğrulama: {info}\n")
    
    # 4. Süresi dolmuş kodları temizle
    cleaned = service.cleanup_expired_codes()
    print(f"4. Temizlenen Kod Sayısı: {cleaned}")
