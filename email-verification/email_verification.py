"""
E-posta Doğrulama Servisi (Resend API)
6 haneli kod üretir, gönderir ve doğrular.
"""

import http.client
import json
import random
import string
import time
from typing import Dict, Optional
from datetime import datetime, timedelta

class EmailVerificationService:
    def __init__(self):
        self.api_key = ""
        self.from_email = "onboarding@resend.dev"  # Resend test email
        self.from_name = "Niko AI"
        
        # Bellekte doğrulama kodlarını sakla (Production'da Redis/DB kullanılmalı)
        self.verification_codes: Dict[str, Dict] = {}
        
    def generate_code(self) -> str:
        """6 haneli rastgele doğrulama kodu üretir"""
        return ''.join(random.choices(string.digits, k=6))
    
    def send_verification_email(self, to_email: str, username: str) -> Dict:
        """
        Elastic Email API ile doğrulama kodu gönderir
        
        Args:
            to_email: Alıcı e-posta adresi
            username: Kullanıcı adı
            
        Returns:
            Dict: {"success": bool, "message": str, "code": str (sadece test için)}
        """
        try:
            # Kod üret
            code = self.generate_code()
            
            # Kodu bellekte sakla (5 dakika geçerli)
            self.verification_codes[to_email] = {
                "code": code,
                "username": username,
                "created_at": datetime.now(),
                "expires_at": datetime.now() + timedelta(minutes=5),
                "attempts": 0
            }
            
            # E-posta içeriği (HTML)
            html_body = f"""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {{
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 40px 20px;
                    }}
                    .container {{
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 20px;
                        overflow: hidden;
                        box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                    }}
                    .header {{
                        background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
                        padding: 40px 30px;
                        text-align: center;
                    }}
                    .logo {{
                        font-size: 48px;
                        margin-bottom: 10px;
                    }}
                    .header h1 {{
                        color: #00E5FF;
                        margin: 0;
                        font-size: 32px;
                        text-shadow: 0 0 20px rgba(0,229,255,0.5);
                    }}
                    .content {{
                        padding: 50px 40px;
                        text-align: center;
                    }}
                    .greeting {{
                        font-size: 20px;
                        color: #333;
                        margin-bottom: 20px;
                    }}
                    .message {{
                        font-size: 16px;
                        color: #666;
                        line-height: 1.6;
                        margin-bottom: 40px;
                    }}
                    .code-container {{
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        border-radius: 15px;
                        padding: 30px;
                        margin: 30px 0;
                        box-shadow: 0 10px 30px rgba(102,126,234,0.3);
                    }}
                    .code {{
                        font-size: 48px;
                        font-weight: bold;
                        color: white;
                        letter-spacing: 10px;
                        font-family: 'Courier New', monospace;
                        text-shadow: 0 2px 10px rgba(0,0,0,0.3);
                    }}
                    .code-label {{
                        color: rgba(255,255,255,0.9);
                        font-size: 14px;
                        margin-top: 10px;
                        text-transform: uppercase;
                        letter-spacing: 2px;
                    }}
                    .warning {{
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px 20px;
                        margin: 30px 0;
                        border-radius: 5px;
                        text-align: left;
                    }}
                    .war
ning-icon {{
                        color: #ffc107;
                        font-size: 20px;
                        margin-right: 10px;
                    }}
                    .warning-text {{
                        color: #856404;
                        font-size: 14px;
                        margin: 0;
                    }}
                    .footer {{
                        background: #f8f9fa;
                        padding: 30px;
                        text-align: center;
                        color: #6c757d;
                        font-size: 14px;
                    }}
                    .footer-links {{
                        margin-top: 15px;
                    }}
                    .footer-links a {{
                        color: #667eea;
                        text-decoration: none;
                        margin: 0 10px;
                    }}
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🤖</div>
                        <h1>NİKO AI</h1>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Merhaba <strong>{username}</strong>! 👋
                        </div>
                        
                        <div class="message">
                            Niko AI'a hoş geldiniz! Hesabınızı aktifleştirmek için aşağıdaki 6 haneli doğrulama kodunu kullanın.
                        </div>
                        
                        <div class="code-container">
                            <div class="code">{code}</div>
                            <div class="code-label">Doğrulama Kodu</div>
                        </div>
                        
                        <div class="warning">
                            <span class="warning-icon">⚠️</span>
                            <p class="warning-text">
                                <strong>Önemli:</strong> Bu kod 5 dakika içinde geçerliliğini yitirecektir. 
                                Kodu kimseyle paylaşmayın!
                            </p>
                        </div>
                        
                        <div class="message">
                            Eğer bu hesabı siz oluşturmadıysanız, bu e-postayı görmezden gelebilirsiniz.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>© 2026 Niko AI - Yapay Zeka Asistanınız</p>
                        <div class="footer-links">
                            <a href="https://github.com/Memati8383/niko-with-kiro">GitHub</a>
                            <a href="#">Gizlilik Politikası</a>
                            <a href="#">Destek</a>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """
            
            # Plain text alternatifi
            text_body = f"""
            Merhaba {username}!
            
            Niko AI'a hoş geldiniz!
            
            Doğrulama Kodunuz: {code}
            
            Bu kod 5 dakika içinde geçerliliğini yitirecektir.
            
            Eğer bu hesabı siz oluşturmadıysanız, bu e-postayı görmezden gelebilirsiniz.
            
            © 2026 Niko AI
            """
            
            # Resend API isteği
            conn = http.client.HTTPSConnection("api.resend.com")
            
            payload = json.dumps({
                "from": f"{self.from_name} <{self.from_email}>",
                "to": [to_email],
                "subject": f"🔐 Niko AI Doğrulama Kodu: {code}",
                "html": html_body,
                "text": text_body
            })
            
            headers = {
                'Content-Type': 'application/json',
                'Authorization': f'Bearer {self.api_key}'
            }
            
            conn.request("POST", "/emails", payload, headers)
            response = conn.getresponse()
            data = response.read().decode("utf-8")
            
            if response.status == 200:
                print(f"[EMAIL] Doğrulama kodu gönderildi: {to_email} -> {code}")
                return {
                    "success": True,
                    "message": "Doğrulama kodu e-posta adresinize gönderildi.",
                    "code": code  # Sadece test/debug için, production'da kaldırılmalı
                }
            else:
                print(f"[EMAIL ERROR] {response.status}: {data}")
                return {
                    "success": False,
                    "message": f"E-posta gönderilemedi: {data}"
                }
                
        except Exception as e:
            print(f"[EMAIL EXCEPTION] {str(e)}")
            return {
                "success": False,
                "message": f"Hata: {str(e)}"
            }
    
    def verify_code(self, email: str, code: str) -> Dict:
        """
        Doğrulama kodunu kontrol eder
        
        Args:
            email: Kullanıcı e-posta adresi
            code: Girilen doğrulama kodu
            
        Returns:
            Dict: {"success": bool, "message": str}
        """
        try:
            # E-posta için kod var mı?
            if email not in self.verification_codes:
                return {
                    "success": False,
                    "message": "Bu e-posta için doğrulama kodu bulunamadı."
                }
            
            stored_data = self.verification_codes[email]
            
            # Süre dolmuş mu?
            if datetime.now() > stored_data["expires_at"]:
                del self.verification_codes[email]
                return {
                    "success": False,
                    "message": "Doğrulama kodunun süresi dolmuş. Lütfen yeni kod isteyin."
                }
            
            # Deneme sayısı kontrolü (brute force koruması)
            if stored_data["attempts"] >= 5:
                del self.verification_codes[email]
                return {
                    "success": False,
                    "message": "Çok fazla hatalı deneme. Lütfen yeni kod isteyin."
                }
            
            # Kod doğru mu?
            if stored_data["code"] == code:
                # Başarılı, kodu sil
                del self.verification_codes[email]
                print(f"[EMAIL] Doğrulama başarılı: {email}")
                return {
                    "success": True,
                    "message": "E-posta adresiniz başarıyla doğrulandı!"
                }
            else:
                # Hatalı kod, deneme sayısını artır
                stored_data["attempts"] += 1
                remaining = 5 - stored_data["attempts"]
                return {
                    "success": False,
                    "message": f"Hatalı doğrulama kodu. Kalan deneme: {remaining}"
                }
                
        except Exception as e:
            print(f"[VERIFY EXCEPTION] {str(e)}")
            return {
                "success": False,
                "message": f"Doğrulama hatası: {str(e)}"
            }
    
    def resend_code(self, email: str) -> Dict:
        """
        Aynı e-posta için yeni kod gönderir
        
        Args:
            email: Kullanıcı e-posta adresi
            
        Returns:
            Dict: {"success": bool, "message": str}
        """
        if email in self.verification_codes:
            username = self.verification_codes[email]["username"]
            # Eski kodu sil
            del self.verification_codes[email]
            # Yeni kod gönder
            return self.send_verification_email(email, username)
        else:
            return {
                "success": False,
                "message": "Bu e-posta için önceden gönderilmiş kod bulunamadı."
            }
    
    def cleanup_expired_codes(self):
        """Süresi dolmuş kodları temizler (Periyodik olarak çağrılmalı)"""
        now = datetime.now()
        expired = [email for email, data in self.verification_codes.items() 
                   if now > data["expires_at"]]
        for email in expired:
            del self.verification_codes[email]
        if expired:
            print(f"[EMAIL] {len(expired)} süresi dolmuş kod temizlendi.")


# Global instance
email_service = EmailVerificationService()
