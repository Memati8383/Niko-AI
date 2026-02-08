import smtplib
from email.mime.text import MIMEText
import os

# .env'den bilgileri oku (veya hardcoded test için)
email = "atarroket311@gmail.com"
# Şifreyi boşluksuz deniyoruz: ccquhlcqndizlriw
password = "ccquhlcqndizlriw"

print(f"Test ediliyor: {email}")

try:
    print("Sunucuya baglaniliyor (smtp.gmail.com:587)...")
    server = smtplib.SMTP("smtp.gmail.com", 587, timeout=10) # 10sn timeout
    server.ehlo()
    print("STARTTLS baslatiliyor...")
    server.starttls()
    server.ehlo()
    print("Giris yapiliyor...")
    server.login(email, password)
    print("BASARILI! Giris yapildi.")
    
    # Kendine test maili at
    msg = MIMEText("Bu bir test mesajidir.")
    msg['Subject'] = "SMTP Test"
    msg['From'] = email
    msg['To'] = email
    
    server.send_message(msg)
    print("Test e-postasi basariyla gonderildi.")
    server.quit()
    print("Test tamamlandi.")
except Exception as e:
    print(f"HATA: {e}")
    import traceback
    traceback.print_exc()
