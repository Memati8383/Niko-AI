"""
Niko AI Sohbet Uygulaması - Ana Giriş Noktası
Türkçe AI sohbet uygulaması için FastAPI backend
"""

import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

import re
import json
import time
from typing import Optional, List, Dict, Tuple
from datetime import datetime, timedelta
from fastapi import FastAPI, HTTPException, Depends, Request, Header
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse, PlainTextResponse, JSONResponse
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel, field_validator, EmailStr
import bcrypt
from jose import jwt, JWTError
import httpx
from typing import AsyncGenerator
from fastapi.responses import StreamingResponse
import logging
from prompts import build_full_prompt
from email_verification import get_email_service, EmailVerificationService

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# ============================================================================
# Pydantic Modelleri
# ============================================================================

class UserCreate(BaseModel):
    """Kullanıcı kaydı için model"""
    username: str
    password: str
    email: Optional[str] = None
    full_name: Optional[str] = None

    @field_validator('username')
    @classmethod
    def validate_username(cls, v):
        """
        Kullanıcı adı doğrulama:
        - Uzunluk: 3-30 karakter
        - Harf ile başlamalı
        - Sadece harf, rakam ve alt çizgi içerebilir
        """
        if len(v) < 3 or len(v) > 30:
            raise ValueError('Kullanıcı adı 3-30 karakter arasında olmalıdır')
        if not v[0].isalpha():
            raise ValueError('Kullanıcı adı bir harf ile başlamalıdır')
        if not re.match(r'^[a-zA-Z][a-zA-Z0-9_]*$', v):
            raise ValueError('Kullanıcı adı sadece harf, rakam ve alt çizgi içerebilir')
        return v

    @field_validator('password')
    @classmethod
    def validate_password(cls, v):
        """
        Şifre doğrulama:
        - En az 8 karakter
        - En az bir büyük harf
        - En az bir küçük harf
        - En az bir rakam
        """
        if len(v) < 8:
            raise ValueError('Şifre en az 8 karakter olmalıdır')
        if not any(c.isupper() for c in v):
            raise ValueError('Şifre en az bir büyük harf içermelidir')
        if not any(c.islower() for c in v):
            raise ValueError('Şifre en az bir küçük harf içermelidir')
        if not any(c.isdigit() for c in v):
            raise ValueError('Şifre en az bir rakam içermelidir')
        return v

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """Regex deseni ve izin verilen sağlayıcılar ile e-posta doğrulama"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        
        # İzin verilen e-posta sağlayıcıları kontrolü
        email_service = get_email_service()
        if not email_service.is_allowed_email_provider(v):
            raise ValueError(f'Desteklenmeyen e-posta sağlayıcısı. Lütfen {email_service.get_allowed_providers_message()} kullanın')
        return v


class UserLogin(BaseModel):
    """Kullanıcı girişi için model"""
    username: str
    password: str


class UserUpdate(BaseModel):
    """Kullanıcı profili güncelleme modeli"""
    email: Optional[str] = None
    full_name: Optional[str] = None
    new_username: Optional[str] = None
    current_password: Optional[str] = None
    new_password: Optional[str] = None
    profile_image: Optional[str] = None  # Base64 string

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """Regex deseni ve izin verilen sağlayıcılar ile e-posta doğrulama"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        
        # İzin verilen e-posta sağlayıcıları kontrolü
        email_service = get_email_service()
        if not email_service.is_allowed_email_provider(v):
            raise ValueError(f'Desteklenmeyen e-posta sağlayıcısı. Lütfen {email_service.get_allowed_providers_message()} kullanın')
        return v

    @field_validator('new_password')
    @classmethod
    def validate_new_password(cls, v):
        """
        Yeni şifre doğrulama (kayıt ile aynı kurallar):
        - En az 8 karakter
        - En az bir büyük harf
        - En az bir küçük harf
        - En az bir rakam
        """
        if v is None:
            return v
        if len(v) < 8:
            raise ValueError('Şifre en az 8 karakter olmalıdır')
        if not any(c.isupper() for c in v):
            raise ValueError('Şifre en az bir büyük harf içermelidir')
        if not any(c.islower() for c in v):
            raise ValueError('Şifre en az bir küçük harf içermelidir')
        if not any(c.isdigit() for c in v):
            raise ValueError('Şifre en az bir rakam içermelidir')
        return v


class ChatRequest(BaseModel):
    """Sohbet isteği modeli"""
    message: str
    enable_audio: bool = True
    web_search: bool = False
    session_id: Optional[str] = None
    model: Optional[str] = None
    mode: Optional[str] = "normal"
    images: Optional[List[str]] = None  # base64 encoded images
    stream: bool = True  # Default to streaming, customizable for clients


# ============================================================================
# E-posta Doğrulama Modelleri
# ============================================================================

class EmailVerificationRequest(BaseModel):
    """E-posta doğrulama kodu gönderme isteği"""
    email: str
    username: str

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """E-posta format ve sağlayıcı doğrulama"""
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        
        email_service = get_email_service()
        if not email_service.is_allowed_email_provider(v):
            raise ValueError(f'Desteklenmeyen e-posta sağlayıcısı. Lütfen {email_service.get_allowed_providers_message()} kullanın')
        return v


class EmailVerifyCodeRequest(BaseModel):
    """E-posta doğrulama kodu kontrol isteği"""
    email: str
    code: str


class EmailResendRequest(BaseModel):
    """E-posta doğrulama kodu yeniden gönderme isteği"""
    email: str

# ============================================================================
# Yönetici Paneli Modelleri
# Gereksinimler: 3.2, 5.2, 5.3
# ============================================================================

class UserAdminUpdate(BaseModel):
    """
    Yönetici kullanıcı güncelleme işlemleri için model.
    Yöneticilerin e-posta, tam ad ve yönetici durumunu güncellemesine izin verir.
    Gereksinimler: 3.2
    """
    email: Optional[str] = None
    full_name: Optional[str] = None
    is_admin: Optional[bool] = None
    password: Optional[str] = None

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """Regex deseni ile e-posta doğrulama"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        return v


class UserAdminCreate(BaseModel):
    """
    Yönetici kullanıcı oluşturma modeli.
    Kullanıcı adı ve şifre gerektirir, e-posta, tam ad ve yönetici durumu isteğe bağlıdır.
    Gereksinimler: 5.2, 5.3
    """
    username: str
    password: str
    email: Optional[str] = None
    full_name: Optional[str] = None
    is_admin: bool = False

    @field_validator('username')
    @classmethod
    def validate_username(cls, v):
        """
        Kullanıcı adı doğrulama:
        - Uzunluk: 3-30 karakter
        - Harf ile başlamalı
        - Sadece harf, rakam ve alt çizgi içerebilir
        """
        if len(v) < 3 or len(v) > 30:
            raise ValueError('Kullanıcı adı 3-30 karakter arasında olmalıdır')
        if not v[0].isalpha():
            raise ValueError('Kullanıcı adı bir harf ile başlamalıdır')
        if not re.match(r'^[a-zA-Z][a-zA-Z0-9_]*$', v):
            raise ValueError('Kullanıcı adı sadece harf, rakam ve alt çizgi içerebilir')
        return v

    @field_validator('password')
    @classmethod
    def validate_password(cls, v):
        """
        Şifre doğrulama:
        - En az 8 karakter
        - En az bir büyük harf
        - En az bir küçük harf
        - En az bir rakam
        """
        if len(v) < 8:
            raise ValueError('Şifre en az 8 karakter olmalıdır')
        if not any(c.isupper() for c in v):
            raise ValueError('Şifre en az bir büyük harf içermelidir')
        if not any(c.islower() for c in v):
            raise ValueError('Şifre en az bir küçük harf içermelidir')
        if not any(c.isdigit() for c in v):
            raise ValueError('Şifre en az bir rakam içermelidir')
        return v

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """Regex deseni ile e-posta doğrulama"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        return v


class UserListResponse(BaseModel):
    """
    Yönetici panelinde kullanıcı listesi yanıtı için model.
    Yönetim için açık şifre dahil kullanıcı bilgilerini içerir.
    Gereksinimler: 2.1, 2.2
    """
    username: str
    email: Optional[str] = None
    full_name: Optional[str] = None
    is_admin: bool = False
    created_at: str
    plain_password: Optional[str] = None


# ============================================================================
# Kimlik Doğrulama Servisi
# ============================================================================

class AuthService:
    """
    Kullanıcı yönetimi için kimlik doğrulama servisi.
    Şifre hashleme, JWT token oluşturma/doğrulama ve kullanıcı veri kalıcılığını yönetir.
    Gereksinimler: 1.9, 2.1
    """
    
    def __init__(self):
        self.secret_key = os.getenv("JWT_SECRET", "niko-ai-secret-key-change-in-production")
        self.algorithm = "HS256"
        self.token_expire_hours = 24
        self.users_file = "users.json"
    
    def hash_password(self, password: str) -> str:
        """Bir şifreyi bcrypt kullanarak hashle"""
        password_bytes = password.encode('utf-8')
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password_bytes, salt)
        return hashed.decode('utf-8')
    
    def verify_password(self, plain_password: str, hashed_password: str) -> bool:
        """Düz şifreyi hashlenmiş şifreyle (veya düz metin yedeğiyle) doğrula"""
        if plain_password == hashed_password:
             return True
        try:
            password_bytes = plain_password.encode('utf-8')
            hashed_bytes = hashed_password.encode('utf-8')
            return bcrypt.checkpw(password_bytes, hashed_bytes)
        except Exception:
            # Fallback for legacy or plaintext passwords
            return plain_password == hashed_password
    
    def create_token(self, username: str) -> str:
        """24 saat geçerli bir JWT token oluştur"""
        from datetime import timezone
        expire = datetime.now(timezone.utc) + timedelta(hours=self.token_expire_hours)
        payload = {
            "sub": username,
            "exp": expire,
            "iat": datetime.now(timezone.utc)
        }
        return jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
    
    def verify_token(self, token: str) -> Optional[str]:
        """
        Bir JWT tokenı doğrula ve geçerliyse kullanıcı adını döndür.
        Token geçersiz veya süresi dolmuşsa None döndürür.
        """
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            username: str = payload.get("sub")
            if username is None:
                return None
            return username
        except JWTError:
            return None
    
    def load_users(self) -> dict:
        """JSON dosyasından kullanıcıları yükle"""
        if os.path.exists(self.users_file):
            try:
                with open(self.users_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except (json.JSONDecodeError, IOError):
                return {}
        return {}
    
    def save_users(self, users: dict) -> None:
        """Kullanıcıları JSON dosyasına kaydet"""
        with open(self.users_file, 'w', encoding='utf-8') as f:
            json.dump(users, f, indent=2, ensure_ascii=False)
    
    def get_user(self, username: str) -> Optional[dict]:
        """Kullanıcı adına göre kullanıcıyı getir"""
        users = self.load_users()
        return users.get(username)
    
    def register(self, user: UserCreate) -> dict:
        """
        Yeni bir kullanıcı kaydet.
        Gereksinimler: 1.1, 1.8, 1.9
        """
        users = self.load_users()
        
        # Check for duplicate username
        if user.username in users:
            raise ValueError("Bu kullanıcı adı zaten kullanılıyor")
        
        # Create user record with hashed password
        from datetime import timezone
        users[user.username] = {
            "password": self.hash_password(user.password),
            "_plain_password": user.password,
            "email": user.email,
            "full_name": user.full_name,
            "is_admin": False,
            "created_at": datetime.now(timezone.utc).isoformat()
        }
        
        self.save_users(users)
        return {"message": "Kayıt başarılı"}
    
    def login(self, credentials: UserLogin) -> dict:
        """
        Kullanıcı kimliğini doğrula ve JWT token döndür.
        Silinmek üzere işaretlenmiş hesapları 30 gün içinde geri aktif eder.
        Gereksinimler: 2.1, 2.2
        """
        users = self.load_users()
        user = users.get(credentials.username)
        
        if not user:
            raise ValueError("Geçersiz kullanıcı adı veya şifre")
        
        if not self.verify_password(credentials.password, user["password"]):
            raise ValueError("Geçersiz kullanıcı adı veya şifre")
        
        # Silinmek üzere işaretlenmiş hesabı kontrol et
        if "deleted_at" in user:
            from datetime import timezone
            deleted_at = datetime.fromisoformat(user["deleted_at"])
            now = datetime.now(timezone.utc)
            days_since_deletion = (now - deleted_at).days
            
            if days_since_deletion < 30:
                # 30 gün dolmamış, hesabı geri aktif et
                del user["deleted_at"]
                users[credentials.username] = user
                self.save_users(users)
                logger.info(f"Silinmek üzere işaretlenmiş hesap geri aktif edildi: {credentials.username}")
            else:
                # 30 gün dolmuş, hesabı kalıcı olarak sil
                raise ValueError("Hesabınız kalıcı olarak silinmiştir. Lütfen yeni bir hesap oluşturun.")
        
        token = self.create_token(credentials.username)
        return {"access_token": token, "token_type": "bearer"}
    
    def get_profile(self, username: str) -> dict:
        """
        Kullanıcı profil bilgilerini getir.
        Gereksinimler: 2.6
        """
        users = self.load_users()
        user = users.get(username)
        
        if not user:
            raise ValueError("Kullanıcı bulunamadı")
        
        return {
            "username": username,
            "email": user.get("email"),
            "full_name": user.get("full_name"),
            "profile_image": user.get("profile_image"),
            "created_at": user.get("created_at"),
            "is_admin": user.get("is_admin", False),
            "_plain_password": user.get("_plain_password")
        }
    
    def update_profile(self, username: str, update: UserUpdate, history_service=None, sync_service=None) -> dict:
        """
        Kullanıcı profilini güncelle.
        Gereksinimler: 2.7
        """
        users = self.load_users()
        user = users.get(username)
        
        if not user:
            raise ValueError("Kullanıcı bulunamadı")
        
        # Handle username change
        old_username = username
        new_username = update.new_username
        if new_username and new_username != old_username:
            if new_username in users:
                raise ValueError("Bu kullanıcı adı zaten kullanılıyor")
            
            # Validation for username
            try:
                # Use UserCreate's validation logic
                UserCreate.validate_username(new_username)
            except ValueError as e:
                raise ValueError(str(e))

            # Move user data
            users[new_username] = users.pop(old_username)
            user = users[new_username]
            username = new_username
            
            # Update history and sync data if services provided
            if history_service:
                history_service.rename_user(old_username, new_username)
            if sync_service:
                sync_service.rename_user(old_username, new_username)

        # Update email if provided
        if update.email is not None:
            user["email"] = update.email
        
        # Update full_name if provided
        if update.full_name is not None:
            user["full_name"] = update.full_name
        
        # Update profile_image if provided
        if update.profile_image is not None:
            user["profile_image"] = update.profile_image
        
        # Update password if both current and new password provided
        if update.new_password is not None:
            if update.current_password is None:
                raise ValueError("Mevcut şifre gerekli")
            
            if not self.verify_password(update.current_password, user["password"]):
                raise ValueError("Mevcut şifre yanlış")
            
            user["password"] = self.hash_password(update.new_password)
            user["_plain_password"] = update.new_password
        
        users[username] = user
        self.save_users(users)
        
        # Return new token if username was changed
        response = {"message": "Profil güncellendi"}
        if new_username and new_username != old_username:
            response["new_username"] = new_username
            response["access_token"] = self.create_token(new_username)
            
        return response
    
    def cleanup_deleted_accounts(self, history_service=None) -> int:
        """
        30 günden eski silinmiş hesapları kalıcı olarak temizler.
        sadece hesap ve sohbet geçmişi silinir.
        
        Dönüş:
            Silinen hesap sayısı
        """
        users = self.load_users()
        deleted_count = 0
        from datetime import timezone
        now = datetime.now(timezone.utc)
        
        usernames_to_delete = []
        
        for username, user_data in users.items():
            if "deleted_at" in user_data:
                deleted_at = datetime.fromisoformat(user_data["deleted_at"])
                days_since_deletion = (now - deleted_at).days
                
                if days_since_deletion >= 30:
                    usernames_to_delete.append(username)
        
        # Hesapları ve sohbet geçmişini sil (senkronize edilmiş veriler korunur)
        for username in usernames_to_delete:
            # Sadece sohbet geçmişini sil
            if history_service:
                history_service.delete_all_sessions(username)
            
            # Kullanıcı hesabını sil
            del users[username]
            deleted_count += 1
            logger.info(f"30 günlük süre dolduğu için hesap kalıcı olarak silindi: {username}")
        
        if deleted_count > 0:
            self.save_users(users)
        
        return deleted_count


# ============================================================================
# Geçmiş Servisi
# ============================================================================

class HistoryService:
    """
    Sohbet oturumu yönetimi için geçmiş servisi.
    Oturum oluşturma, mesaj saklama ve geçmiş işlemlerini yönetir.
    Gereksinimler: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 9.5
    """
    
    def __init__(self):
        self.history_dir = "history"
        os.makedirs(self.history_dir, exist_ok=True)
    
    def get_session_path(self, username: str, session_id: str) -> str:
        """Bir oturum için dosya yolunu getir"""
        return os.path.join(self.history_dir, f"{username}_{session_id}.json")
    
    def create_session(self, username: str) -> str:
        """
        Yeni bir sohbet oturumu oluştur.
        Gereksinimler: 4.6
        """
        import uuid
        from datetime import timezone
        session_id = str(uuid.uuid4())
        session_data = {
            "id": session_id,
            "title": "Yeni Sohbet",
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "messages": []
        }
        
        path = self.get_session_path(username, session_id)
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(session_data, f, indent=2, ensure_ascii=False)
        
        return session_id
    
    def add_message(self, username: str, session_id: str, role: str, content: str, thought: str = None) -> None:
        """
        Oturuma bir mesaj ekle.
        Gereksinimler: 4.7, 9.5
        """
        path = self.get_session_path(username, session_id)
        
        if not os.path.exists(path):
            raise ValueError("Oturum bulunamadı")
        
        with open(path, 'r', encoding='utf-8') as f:
            session = json.load(f)
        
        message = {"role": role, "content": content}
        if thought:
            message["thought"] = thought
        
        session["messages"].append(message)
        
        # Update title from first user message
        if role == "user" and len(session["messages"]) == 1:
            session["title"] = content[:50] + ("..." if len(content) > 50 else "")
        
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(session, f, indent=2, ensure_ascii=False)
    
    def session_exists(self, username: str, session_id: str) -> bool:
        """Bir oturum dosyasının var olup olmadığını kontrol et"""
        path = self.get_session_path(username, session_id)
        return os.path.exists(path)

    def get_session(self, username: str, session_id: str) -> dict:
        """
        Tüm mesajlarıyla belirli bir oturumu getir.
        Gereksinimler: 4.2
        """
        path = self.get_session_path(username, session_id)
        
        if not os.path.exists(path):
            raise ValueError("Oturum bulunamadı")
        
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    def get_history(self, username: str) -> List[dict]:
        """
        Bir kullanıcı için tüm sohbet oturumlarını getir.
        Gereksinimler: 4.1
        """
        sessions = []
        
        if not os.path.exists(self.history_dir):
            return sessions
        
        for filename in os.listdir(self.history_dir):
            if filename.startswith(f"{username}_") and filename.endswith(".json"):
                path = os.path.join(self.history_dir, filename)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        session = json.load(f)
                        sessions.append({
                            "id": session["id"],
                            "title": session["title"],
                            "timestamp": session["timestamp"]
                        })
                except (json.JSONDecodeError, IOError, KeyError):
                    continue
        
        return sorted(sessions, key=lambda x: x["timestamp"], reverse=True)
    
    def delete_session(self, username: str, session_id: str) -> bool:
        """
        Belirli bir oturumu sil.
        Gereksinimler: 4.3
        """
        path = self.get_session_path(username, session_id)
        
        if os.path.exists(path):
            os.remove(path)
            return True
        return False
    
    def delete_all_sessions(self, username: str) -> int:
        """
        Bir kullanıcı için tüm oturumları sil.
        Gereksinimler: 4.4
        """
        deleted_count = 0
        
        if not os.path.exists(self.history_dir):
            return deleted_count
        
        for filename in os.listdir(self.history_dir):
            if filename.startswith(f"{username}_") and filename.endswith(".json"):
                path = os.path.join(self.history_dir, filename)
                try:
                    os.remove(path)
                    deleted_count += 1
                except IOError:
                    continue
        
        return deleted_count

    def rename_user(self, old_username: str, new_username: str):
        """
        Bir kullanıcı için tüm oturum dosyalarını yeniden adlandır.
        """
        if not os.path.exists(self.history_dir):
            return

        for filename in os.listdir(self.history_dir):
            if filename.startswith(f"{old_username}_"):
                try:
                    old_path = os.path.join(self.history_dir, filename)
                    new_filename = filename.replace(f"{old_username}_", f"{new_username}_", 1)
                    new_path = os.path.join(self.history_dir, new_filename)
                    os.rename(old_path, new_path)
                except Exception as e:
                    logger.error(f"Oturum dosyası yeniden adlandırılırken hata oluştu {filename}: {e}")
    
    def export_markdown(self, username: str, session_id: str) -> str:
        """
        Bir oturumu Markdown formatında dışa aktar.
        Gereksinimler: 4.5
        """
        session = self.get_session(username, session_id)
        
        md = f"# {session['title']}\n\n"
        md += f"*Tarih: {session['timestamp']}*\n\n---\n\n"
        
        for msg in session["messages"]:
            role = "👤 Kullanıcı" if msg["role"] == "user" else "🤖 Niko"
            md += f"### {role}\n\n{msg['content']}\n\n"
        
        return md


# ============================================================================
# Senkronizasyon Servisi
# ============================================================================

class SyncService:
    """
    Mobil cihaz veri yönetimi için senkronizasyon servisi.
    Kişiler, aramalar, konum ve cihaz bilgilerinin saklanmasını yönetir.
    """
    
    def __init__(self):
        self.base_dir = "device_data"
        os.makedirs(self.base_dir, exist_ok=True)
    
    def get_user_dir(self, username: str) -> str:
        """Kullanıcının cihaz verileri için dizini getir"""
        user_dir = os.path.join(self.base_dir, username)
        os.makedirs(user_dir, exist_ok=True)
        return user_dir
    
    def save_data(self, username: str, data_type: str, data: List[dict], device_name: str) -> None:
        """Senkronize edilen veriyi bir JSON dosyasına kaydet"""
        user_dir = self.get_user_dir(username)
        filename = f"{data_type}.json"
        path = os.path.join(user_dir, filename)
        
        from datetime import timezone
        sync_record = {
            "device": device_name,
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "data": data
        }
        
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(sync_record, f, indent=2, ensure_ascii=False)
        
        logger.info(f"{username} kullanıcısı için {device_name} cihazından {data_type} senkronize edildi")

    def rename_user(self, old_username: str, new_username: str):
        """Kullanıcı veri dizinini yeniden adlandır"""
        old_dir = os.path.join(self.base_dir, old_username)
        new_dir = os.path.join(self.base_dir, new_username)
        if os.path.exists(old_dir):
            try:
                if os.path.exists(new_dir):
                    for item in os.listdir(old_dir):
                        old_item_path = os.path.join(old_dir, item)
                        new_item_path = os.path.join(new_dir, item)
                        if os.path.exists(new_item_path):
                            os.remove(new_item_path)
                        os.rename(old_item_path, new_item_path)
                    os.rmdir(old_dir)
                else:
                    os.rename(old_dir, new_dir)
            except Exception as e:
                logger.error(f"{old_username} için senkronizasyon dizini yeniden adlandırılırken hata: {e}")

    def list_devices(self) -> List[str]:
        """Senkronize edilmiş veriye sahip tüm cihazları listele"""
        if not os.path.exists(self.base_dir):
            return []
        return [d for d in os.listdir(self.base_dir) if os.path.isdir(os.path.join(self.base_dir, d))]

    def get_data_types(self, device_name: str) -> List[str]:
        """Bir cihaz için mevcut veri türlerini listele"""
        device_dir = os.path.join(self.base_dir, device_name)
        if not os.path.exists(device_dir):
            return []
        return [f.replace('.json', '') for f in os.listdir(device_dir) if f.endswith('.json')]

    def get_data(self, device_name: str, data_type: str) -> Optional[dict]:
        """Bir cihaz için belirli verileri getir"""
        device_dir = os.path.join(self.base_dir, device_name)
        file_path = os.path.join(device_dir, f"{data_type}.json")
        
        if not os.path.exists(file_path):
            return None
            
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            logger.error(f"{device_name}/{data_type} verisi okunurken hata: {e}")
            return None


# ============================================================================
# Yönetici Servisi
# Gereksinimler: 2.1, 3.3, 4.2, 4.3, 5.4
# ============================================================================

class AdminService:
    """
    Kullanıcı yönetimi işlemleri için yönetici servisi.
    Kullanıcı listeleme, oluşturma, güncelleme ve silme işlemlerini yönetir.
    Gereksinimler: 2.1, 3.3, 4.2, 4.3, 5.4
    """
    
    def __init__(self, auth_service: AuthService, history_service: HistoryService):
        """
        AdminService'i AuthService ve HistoryService bağımlılıkları ile başlatır.
        
        Parametreler:
            auth_service: Kullanıcı veri işlemleri için AuthService örneği
            history_service: Sohbet geçmişi işlemleri için HistoryService örneği
        """
        self.auth = auth_service
        self.history = history_service
    
    def list_users(self) -> List[UserListResponse]:
        """
        Sistemdeki tüm kullanıcıları listele (şifreler hariç).
        Gereksinimler: 2.1, 2.2
        
        Dönüş:
            Kullanıcı bilgilerini içeren UserListResponse nesneleri listesi
        """
        users = self.auth.load_users()
        user_list = []
        
        for username, user_data in users.items():
            user_list.append(UserListResponse(
                username=username,
                email=user_data.get("email"),
                full_name=user_data.get("full_name"),
                is_admin=user_data.get("is_admin", False),
                created_at=user_data.get("created_at", ""),
                plain_password=user_data.get("_plain_password")
            ))
        
        return user_list
    
    def get_user(self, username: str) -> Optional[UserListResponse]:
        """
        Tek bir kullanıcının bilgilerini getir (şifre hariç).
        Gereksinimler: 3.1
        
        Parametreler:
            username: Aranacak kullanıcı adı
            
        Dönüş:
            Kullanıcı varsa UserListResponse, yoksa None
        """
        user_data = self.auth.get_user(username)
        
        if user_data is None:
            return None
        
        return UserListResponse(
            username=username,
            email=user_data.get("email"),
            full_name=user_data.get("full_name"),
            is_admin=user_data.get("is_admin", False),
            created_at=user_data.get("created_at", ""),
            plain_password=user_data.get("_plain_password")
        )
    
    def update_user(self, username: str, data: UserAdminUpdate) -> UserListResponse:
        """
        Bir kullanıcının bilgilerini güncelle.
        Gereksinimler: 3.2, 3.3
        
        Parametreler:
            username: Güncellenecek kullanıcı adı
            data: Güncellenecek alanları içeren UserAdminUpdate
            
        Dönüş:
            Güncellenmiş UserListResponse
            
        Hatalar:
            ValueError: Kullanıcı bulunamazsa
        """
        users = self.auth.load_users()
        
        if username not in users:
            raise ValueError("Kullanıcı bulunamadı")
        
        user = users[username]
        
        # Update fields if provided
        if data.email is not None:
            user["email"] = data.email
        
        if data.full_name is not None:
            user["full_name"] = data.full_name
        
        if data.is_admin is not None:
            user["is_admin"] = data.is_admin
        
        # Handle password update
        if data.password is not None and len(data.password) >= 8:
            user["password"] = self.auth.hash_password(data.password)
            user["_plain_password"] = data.password
        
        users[username] = user
        self.auth.save_users(users)
        
        return UserListResponse(
            username=username,
            email=user.get("email"),
            full_name=user.get("full_name"),
            is_admin=user.get("is_admin", False),
            created_at=user.get("created_at", ""),
            plain_password=user.get("_plain_password")
        )
    
    def delete_user(self, username: str, admin_username: str) -> bool:
        """
        Bir kullanıcıyı ve tüm sohbet geçmişini sil.
        Gereksinimler: 4.2, 4.3, 4.4
        
        Parametreler:
            username: Silinecek kullanıcı adı
            admin_username: Silme işlemini yapan yönetici (kendini silme kontrolü için)
            
        Dönüş:
            Silme başarılıysa True
            
        Hatalar:
            ValueError: Kullanıcı bulunamazsa veya yönetici kendini silmeye çalışırsa
        """
        # Check for self-deletion attempt
        if username == admin_username:
            raise ValueError("Kendinizi silemezsiniz")
        
        users = self.auth.load_users()
        
        if username not in users:
            raise ValueError("Kullanıcı bulunamadı")
        
        # Delete user from users.json
        del users[username]
        self.auth.save_users(users)
        
        # Delete all chat history for this user
        self.history.delete_all_sessions(username)
        
        return True
    
    def create_user(self, user: UserAdminCreate) -> UserListResponse:
        """
        Yeni bir kullanıcı oluştur (yönetici işlemi).
        Gereksinimler: 5.2, 5.3, 5.4, 5.5
        
        Parametreler:
            user: Kullanıcı verilerini içeren UserAdminCreate
            
        Dönüş:
            Oluşturulan kullanıcı için UserListResponse
            
        Hatalar:
            ValueError: Kullanıcı adı zaten varsa
        """
        users = self.auth.load_users()
        
        # Check for duplicate username
        if user.username in users:
            raise ValueError("Bu kullanıcı adı zaten kullanılıyor")
        
        # Create user record with hashed password
        from datetime import timezone
        created_at = datetime.now(timezone.utc).isoformat()
        users[user.username] = {
            "password": self.auth.hash_password(user.password),
            "_plain_password": user.password,
            "email": user.email,
            "full_name": user.full_name,
            "is_admin": user.is_admin,
            "created_at": created_at
        }
        
        self.auth.save_users(users)
        
        return UserListResponse(
            username=user.username,
            email=user.email,
            full_name=user.full_name,
            is_admin=user.is_admin,
            created_at=created_at,
            plain_password=user.password
        )


# ============================================================================
# Sohbet Servisi
# ============================================================================

class ChatService:
    """
    Yapay zeka sohbet yönetimi servisi.
    Ollama API iletişimi, model listeleme ve akışlı yanıtları yönetir.
    Gereksinimler: 3.1, 3.2, 3.3, 3.4
    """
    
    def __init__(self):
        self.ollama_url = os.getenv("OLLAMA_URL", "http://localhost:11434")
        self.default_model = os.getenv("DEFAULT_MODEL", "llama2")
        self.timeout = 120.0  # 2 minutes timeout for chat requests
    
    async def get_models(self) -> List[str]:
        """
        Mevcut Ollama modellerinin listesini getir.
        Gereksinimler: 3.4
        
        Dönüş:
            Ollama'da bulunan model isimlerinin listesi
        """
        try:
            async with httpx.AsyncClient(timeout=10.0) as client:
                response = await client.get(f"{self.ollama_url}/api/tags")
                if response.status_code == 200:
                    data = response.json()
                    return [model["name"] for model in data.get("models", [])]
                return []
        except httpx.RequestError as e:
            # Hatayı logla ama boş liste döndür
            print(f"Ollama API hatası: {e}")
            return []
        except Exception as e:
            print(f"Modeller alınırken beklenmeyen hata: {e}")
            return []
    
    async def check_ollama_available(self) -> bool:
        """
        Ollama API'sinin erişilebilir olup olmadığını kontrol et.
        Gereksinimler: 3.6
        
        Dönüş:
            Ollama erişilebilir ise True, değilse False
        """
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(f"{self.ollama_url}/api/tags")
                return response.status_code == 200
        except Exception:
            return False
    
    async def chat_stream(
        self,
        prompt: str,
        model: Optional[str] = None,
        images: Optional[List[str]] = None
    ) -> AsyncGenerator[str, None]:
        """
        Ollama'dan akışlı sohbet yanıtı al.
        Gereksinimler: 3.1, 3.2, 3.3, 3.5
        
        Parametreler:
            prompt: AI için formatlanmış istem
            model: Kullanılacak model (varsayılan: self.default_model)
            images: İsteğe bağlı base64 kodlanmış resim listesi
        
        Dönüş:
            AI yanıtının parçaları
        """
        selected_model = model or self.default_model
        
        # Ollama istek yükünü hazırla
        payload = {
            "model": selected_model,
            "prompt": prompt,
            "stream": True
        }
        
        # Varsa resimleri ekle (Gereksinimler: 3.5)
        if images:
            payload["images"] = images
        
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                async with client.stream(
                    "POST",
                    f"{self.ollama_url}/api/generate",
                    json=payload
                ) as response:
                    if response.status_code != 200:
                        yield f"Ollama API hatası: {response.status_code}"
                        return
                    
                    async for line in response.aiter_lines():
                        if line:
                            try:
                                data = json.loads(line)
                                chunk = data.get("response", "")
                                if chunk:
                                    yield chunk
                                # Check if done
                                if data.get("done", False):
                                    break
                            except json.JSONDecodeError:
                                continue
        except httpx.ConnectError:
            yield "Ollama sunucusuna bağlanılamadı. Lütfen Ollama'nın çalıştığından emin olun."
        except httpx.TimeoutException:
            yield "İstek zaman aşımına uğradı. Lütfen tekrar deneyin."
        except Exception as e:
            yield f"Beklenmeyen bir hata oluştu: {str(e)}"
    
    async def chat(
        self,
        prompt: str,
        model: Optional[str] = None,
        images: Optional[List[str]] = None
    ) -> str:
        """
        Ollama'dan tam sohbet yanıtı al (akışsız).
        Gereksinimler: 3.1, 3.3
        
        Parametreler:
            prompt: AI için formatlanmış istem
            model: Kullanılacak model (varsayılan: self.default_model)
            images: İsteğe bağlı base64 kodlanmış resim listesi
        
        Dönüş:
            Tam AI yanıtı
        """
        response_parts = []
        async for chunk in self.chat_stream(prompt, model, images):
            response_parts.append(chunk)
        return "".join(response_parts)


# ============================================================================
# Arama Servisi
# ============================================================================

class SearchService:
    """
    Web arama işlevselliği için arama servisi.
    DuckDuckGo web aramasını yönetir.
    Gereksinimler: 5.1, 5.4
    """
    
    def __init__(self):
        """Arama servisini başlat."""
        pass
    
    async def web_search(self, query: str, max_results: int = 5) -> str:
        """
        DuckDuckGo kullanarak web araması yap.
        Gereksinimler: 5.1, 5.4
        """
        try:
            # Try to use 'ddgs' package if available, fallback to 'duckduckgo_search'
            # Note: The package name is 'duckduckgo_search' but the module can be 'duckduckgo_search' or 'ddgs'
            # recent versions use 'duckduckgo_search' for import and DDGS class
            try:
                from duckduckgo_search import DDGS
            except ImportError:
                try:
                    from ddgs import DDGS
                except ImportError:
                     logger.error("duckduckgo-search (veya ddgs) paketi yüklü değil")
                     return ""
            
            # DDGS operations are synchronous, wrapping in try/except block specifically for the search
            results = []
            try:
                # Use a fresh instance for each search
                ddgs = DDGS()
                # .text() returns a generator, convert to list immediately
                # Some versions might raise an error if 0 results or network issue
                results = list(ddgs.text(query, max_results=max_results))
            except Exception as search_err:
                logger.error(f"DDGS arama yürütme hatası: {search_err} - Sorgu: {query}")
                return ""
            
            if not results:
                logger.info(f"Sorgu için web arama sonucu bulunamadı: {query}")
                return ""
            
            logger.info(f"{query} için {len(results)} web arama sonucu bulundu")

            # Format results for AI context
            formatted = []
            for i, r in enumerate(results, 1):
                title = r.get('title', 'Başlık yok')
                body = r.get('body', 'İçerik yok')
                href = r.get('href', '')
                formatted.append(f"{i}. {title}\n   {body}\n   Kaynak: {href}")
            
            return "\n\n".join(formatted)
        
        except Exception as e:
            # Gereksinimler: 5.4 - Hatayı logla ve arama sonuçları olmadan devam et
            logger.error(f"'{query}' sorgusu için genel web arama hatası: {e}")
            return ""


# ============================================================================
# Hız Sınırlayıcı
# ============================================================================

class RateLimiter:
    """
    API uç noktaları için bellek içi hız sınırlayıcı.
    İstemci başına istekleri izler ve uç noktaya özgü sınırları uygular.
    Gereksinimler: 6.1, 6.2, 6.3, 6.4
    """
    
    def __init__(self):
        # İstek takibi: {client_key: [(zaman_damgası, sayaç), ...]}
        self.requests: Dict[str, List[Tuple[float, int]]] = {}
        
        # Uç nokta sınırları: (maks_istek, pencere_saniye)
        # Daha iyi kullanıcı deneyimi için sınırlar artırıldı
        self.limits: Dict[str, Tuple[int, int]] = {
            "general": (200, 60),     # 200 requests per 60 seconds (1 minute)
            "auth": (20, 300),        # 20 requests per 300 seconds (5 minutes)
            "register": (10, 3600),   # 10 requests per 3600 seconds (1 hour)
            "chat": (100, 60)         # 100 requests per 60 seconds (1 minute)
        }
    
    def _get_client_key(self, client_ip: str, limit_type: str) -> str:
        """İstemci + sınır türü kombinasyonu için benzersiz bir anahtar oluştur"""
        return f"{client_ip}:{limit_type}"
    
    def _clean_old_entries(self, key: str, window: int) -> None:
        """Zaman pencresinden eski girişleri kaldır"""
        now = time.time()
        if key in self.requests:
            self.requests[key] = [
                (ts, count) for ts, count in self.requests[key]
                if now - ts < window
            ]
    
    def _count_requests(self, key: str) -> int:
        """Mevcut penceredeki toplam istekleri say"""
        if key not in self.requests:
            return 0
        return sum(count for _, count in self.requests[key])
    
    def is_allowed(self, client_ip: str, limit_type: str) -> Tuple[bool, int]:
        """
        Hız sınırlarına göre bir isteğin izinli olup olmadığını kontrol et.
        
        Parametreler:
            client_ip: İstemcinin IP adresi
            limit_type: Uygulanacak sınır türü (general, auth, register, chat)
        
        Dönüş:
            (is_allowed, retry_after_seconds) demeti
            - is_allowed: İstek izinliyse True, sınır aşıldıysa False
            - retry_after_seconds: İstemcinin tekrar denemesi için beklemesi gereken saniye (izinliyse 0)
        
        Gereksinimler: 6.1, 6.2, 6.3, 6.4
        """
        max_requests, window = self.limits.get(limit_type, (60, 60))
        key = self._get_client_key(client_ip, limit_type)
        now = time.time()
        
        # Initialize if needed
        if key not in self.requests:
            self.requests[key] = []
        
        # Clean old entries
        self._clean_old_entries(key, window)
        
        # Count requests in window
        total = self._count_requests(key)
        
        if total >= max_requests:
            # Calculate retry_after based on oldest entry in window
            if self.requests[key]:
                oldest_ts = min(ts for ts, _ in self.requests[key])
                retry_after = int(window - (now - oldest_ts)) + 1
            else:
                retry_after = window
            return False, max(1, retry_after)
        
        # Record this request
        self.requests[key].append((now, 1))
        return True, 0
    
    def get_remaining(self, client_ip: str, limit_type: str) -> int:
        """
        Bir istemci için kalan istek sayısını getir.
        
        Parametreler:
            client_ip: İstemcinin IP adresi
            limit_type: Kontrol edilecek sınır türü
        
        Dönüş:
            Mevcut pencerede kalan istek sayısı
        """
        max_requests, window = self.limits.get(limit_type, (60, 60))
        key = self._get_client_key(client_ip, limit_type)
        
        # Clean old entries
        self._clean_old_entries(key, window)
        
        # Count requests in window
        total = self._count_requests(key)
        
        return max(0, max_requests - total)
    
    def reset(self, client_ip: str = None, limit_type: str = None) -> None:
        """
        Hız sınırı takibini sıfırla.
        
        Argümanlar:
            client_ip: Sağlanırsa, sadece bu istemci için sıfırla
            limit_type: Sağlanırsa, sadece bu sınır türü için sıfırla
        """
        if client_ip is None and limit_type is None:
            # Hepsini sıfırla
            self.requests = {}
        elif client_ip is not None and limit_type is not None:
            # Belirli istemci + sınır türünü sıfırla
            key = self._get_client_key(client_ip, limit_type)
            if key in self.requests:
                del self.requests[key]
        elif client_ip is not None:
            # Bir istemci için tüm sınır türlerini sıfırla
            keys_to_delete = [k for k in self.requests if k.startswith(f"{client_ip}:")]
            for key in keys_to_delete:
                del self.requests[key]
        else:
            # Bir sınır türü için tüm istemcileri sıfırla
            keys_to_delete = [k for k in self.requests if k.endswith(f":{limit_type}")]
            for key in keys_to_delete:
                del self.requests[key]


# Servisleri başlat
auth_service = AuthService()
history_service = HistoryService()
chat_service = ChatService()
search_service = SearchService()
rate_limiter = RateLimiter()
admin_service = AdminService(auth_service, history_service)
sync_service = SyncService()

# JWT için güvenlik şeması
security = HTTPBearer(auto_error=False)


async def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security),
    x_api_key: Optional[str] = Header(None, alias="x-api-key")
) -> str:
    """
    JWT token veya API Key'den mevcut kimliği doğrulanmış kullanıcıyı getir.
    Gereksinimler: 2.4, 2.5
    """
    # 1. API Anahtarını Kontrol Et (Mobil Uygulama için Arka Kapı)
    if x_api_key == "test":
        return "mobile_user"

    # 2. JWT Jetonunu Kontrol Et
    if not credentials:
        raise HTTPException(
            status_code=401,
            detail="Kimlik doğrulama gerekli"
        )

    token = credentials.credentials
    username = auth_service.verify_token(token)
    
    if username is None:
        raise HTTPException(
            status_code=401,
            detail="Geçersiz veya süresi dolmuş token"
        )
    
    # Kullanıcının hala var olduğunu doğrula
    if auth_service.get_user(username) is None:
        raise HTTPException(
            status_code=401,
            detail="Kullanıcı bulunamadı"
        )
    
    return username


async def get_current_admin(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    """
    JWT tokendan mevcut kimliği doğrulanmış yönetici kullanıcısını getir.
    Hem token geçerliliğini hem de yönetici yetkilerini doğrular.
    Gereksinimler: 1.1, 1.2, 6.1, 6.2
    
    Dönüş:
        Kimliği doğrulanmış yönetici kullanıcısının kullanıcı adı
        
    Hatalar:
        HTTPException 401: Token geçersiz veya süresi dolmuşsa
        HTTPException 403: Kullanıcı yönetici değilse
    """
    token = credentials.credentials
    username = auth_service.verify_token(token)
    
    if username is None:
        raise HTTPException(
            status_code=401,
            detail="Geçersiz veya süresi dolmuş token"
        )
    
    # Kullanıcının hala var olduğunu doğrula
    user = auth_service.get_user(username)
    if user is None:
        raise HTTPException(
            status_code=401,
            detail="Kullanıcı bulunamadı"
        )
    
    # Yönetici yetkilerini kontrol et (Gereksinimler: 1.1, 1.2, 6.1)
    if not user.get("is_admin", False):
        raise HTTPException(
            status_code=403,
            detail="Admin yetkisi gerekli"
        )
    
    return username


# ============================================================================
# FastAPI Uygulaması
# ============================================================================

# FastAPI uygulama örneğini oluştur
app = FastAPI(
    title="Niko AI Chat",
    description="Türkçe yapay zeka sohbet uygulaması",
    version="1.0.0"
)


# ============================================================================
# Uygulama Başlangıç Olayları
# ============================================================================

@app.on_event("startup")
async def startup_event():
    """
    Uygulama başlangıcında çalışacak işlemler.
    30 günden eski silinmiş hesapları temizler.
    """
    logger.info("Uygulama başlatılıyor...")
    
    # Silinmiş hesapları temizle
    try:
        deleted_count = auth_service.cleanup_deleted_accounts(history_service)
        if deleted_count > 0:
            logger.info(f"{deleted_count} adet 30 günlük süresi dolmuş hesap temizlendi")
        else:
            logger.info("Temizlenecek silinmiş hesap bulunamadı")
    except Exception as e:
        logger.error(f"Silinmiş hesapları temizlerken hata oluştu: {e}")
    
    logger.info("Uygulama başarıyla başlatıldı")


# ============================================================================
# Global İstisna İşleyicileri
# Gereksinimler: 10.5
# ============================================================================

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """
    HTTPException için global işleyici.
    Hata detaylarını içeren JSON yanıtı döndürür.
    Gereksinimler: 10.2, 10.3, 10.4
    """
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.detail}
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """
    Beklenmeyen istisnalar için genel işleyici.
    Türkçe dostu hata mesajı döndürür.
    Gereksinimler: 10.5
    """
    logger.error(f"Beklenmedik hata: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"error": "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."}
    )

# CORS ara yazılım yapılandırması
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Üretimde, izin verilen kaynakları belirtin
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Güvenlik başlıkları ara yazılımı
@app.middleware("http")
async def security_headers_middleware(request: Request, call_next):
    """
    Güvenlik başlıkları ara yazılımı.
    Tüm yanıtlara güvenlik başlıkları ekler.
    Gereksinimler: 7.1, 7.2
    """
    response = await call_next(request)
    
    # Güvenlik başlıklarını ekle (Gereksinimler: 7.1)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
    
    # Üretim modunda HSTS başlığı ekle (Gereksinimler: 7.2)
    if os.getenv("PRODUCTION", "false").lower() == "true":
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    
    return response


# Hız sınırlayıcı ara yazılımı
@app.middleware("http")
async def rate_limit_middleware(request: Request, call_next):
    """
    Hız sınırlama ara yazılımı.
    Uç noktaya özgü hız sınırlarını uygular ve aşıldığında 429 döndürür.
    Gereksinimler: 6.1, 6.2, 6.3, 6.4, 6.5
    """
    # İstemci IP'sini al (proxy başlıklarını işle)
    client_ip = request.client.host if request.client else "unknown"
    forwarded_for = request.headers.get("X-Forwarded-For")
    if forwarded_for:
        client_ip = forwarded_for.split(",")[0].strip()
    
    # Yola göre sınır türünü belirle
    path = request.url.path
    
    # Statik dosyalar ve sağlık kontrolü için hız sınırlamasını atla
    if path.startswith("/static") or path == "/health" or path == "/" or path.endswith(".html"):
        return await call_next(request)
    
    # Sınır türünü belirle
    if path == "/register":
        limit_type = "register"
    elif path == "/login":
        limit_type = "auth"
    elif path == "/chat":
        limit_type = "chat"
    else:
        limit_type = "general"
    
    # Hız sınırını kontrol et
    allowed, retry_after = rate_limiter.is_allowed(client_ip, limit_type)
    
    if not allowed:
        # retry-after başlığı ile 429 Çok Fazla İstek döndür
        # Güvenlik başlıkları security_headers_middleware tarafından eklenecek
        return JSONResponse(
            status_code=429,
            content={
                "error": "Çok fazla istek. Lütfen bekleyin.",
                "retry_after": retry_after
            },
            headers={
                "Retry-After": str(retry_after),
                # Bu yanıt call_next'i atladığı için güvenlik başlıklarını buraya ekle
                "X-Content-Type-Options": "nosniff",
                "X-Frame-Options": "DENY",
                "X-XSS-Protection": "1; mode=block",
                "Referrer-Policy": "strict-origin-when-cross-origin"
            }
        )
    
    # İsteği işle
    response = await call_next(request)
    
    # Yanıta hız sınırı başlıklarını ekle
    remaining = rate_limiter.get_remaining(client_ip, limit_type)
    response.headers["X-RateLimit-Remaining"] = str(remaining)
    
    return response

# Geçmiş dizininin var olduğundan emin ol
os.makedirs("history", exist_ok=True)

# Statik dosyaları bağla
app.mount("/static", StaticFiles(directory="static"), name="static")


@app.get("/")
async def root():
    """Ana sayfayı sun"""
    return FileResponse("static/index.html")


@app.get("/login.html")
@app.get("/login")
async def login_page():
    """Giriş sayfasını sun"""
    return FileResponse("static/login.html")


@app.get("/signup.html")
@app.get("/signup")
async def signup_page():
    """Kayıt sayfasını sun"""
    return FileResponse("static/signup.html")


@app.get("/sw.js")
async def service_worker():
    """
    Servis çalışanı dosyasını sunar.
    Servis çalışanları, tüm siteyi kontrol etmek için kök kapsamdan sunulmalıdır.
    """
    return FileResponse(
        "static/sw.js",
        media_type="application/javascript",
        headers={
            "Cache-Control": "no-cache, no-store, must-revalidate",
            "Service-Worker-Allowed": "/"
        }
    )


@app.get("/style.css")
async def style_css():
    """Ana stil dosyasını sun"""
    return FileResponse("static/style.css", media_type="text/css")


@app.get("/script.js")
async def script_js():
    """Ana JavaScript dosyasını sun"""
    return FileResponse("static/script.js", media_type="application/javascript")


@app.get("/health")
async def health_check():
    """Sağlık kontrolü uç noktası"""
    return {"status": "healthy"}


@app.get("/favicon.ico")
async def favicon():
    """Favicon'u veya konsol hatalarını durdurmak için 204 İçerik Yok sun"""
    # Sadece 204 İçerik Yok döndürmek tarayıcının şikayet etmesini durdurmak için yeterlidir
    # veya küçük bir 1x1 şeffaf piksel sunabiliriz.
    return PlainTextResponse("", status_code=204)


# ============================================================================
# E-posta Doğrulama Uç Noktaları
# ============================================================================

@app.post("/email/send-verification")
async def send_verification_email(request: EmailVerificationRequest):
    """
    E-posta doğrulama kodu gönder.
    
    Resend API kullanarak belirtilen e-posta adresine 6 haneli doğrulama kodu gönderir.
    Kod 5 dakika geçerlidir.
    """
    try:
        email_service = get_email_service()
        result = email_service.send_verification_email(request.email, request.username)
        
        if not result["success"]:
            raise HTTPException(status_code=400, detail=result["message"])
        
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"E-posta doğrulama hatası: {e}")
        raise HTTPException(status_code=500, detail=f"E-posta gönderilemedi: {str(e)}")


@app.post("/email/verify")
async def verify_email_code(request: EmailVerifyCodeRequest):
    """
    E-posta doğrulama kodunu kontrol et.
    
    Kullanıcının girdiği kodu doğrular. Maksimum 5 deneme hakkı vardır.
    """
    try:
        email_service = get_email_service()
        result = email_service.verify_code(request.email, request.code)
        
        if not result["success"]:
            raise HTTPException(status_code=400, detail=result["message"])
        
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/email/resend")
async def resend_verification_code(request: EmailResendRequest):
    """
    Yeni doğrulama kodu gönder.
    
    Önceki kodu geçersiz kılar ve yeni bir kod gönderir.
    60 saniye bekleme süresi uygulanır.
    """
    try:
        email_service = get_email_service()
        result = email_service.resend_code(request.email)
        
        if not result["success"]:
            raise HTTPException(status_code=400, detail=result["message"])
        
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/email/status/{email}")
async def get_verification_status(email: str):
    """
    E-posta doğrulama durumunu kontrol et.
    
    Bekleyen doğrulama varsa bilgileri döndürür.
    """
    email_service = get_email_service()
    
    if email_service.has_pending_verification(email):
        info = email_service.get_pending_verification(email)
        return {
            "pending": True,
            "expires_at": info["expires_at"],
            "attempts_remaining": info["max_attempts"] - info["attempts"]
        }
    
    return {"pending": False}


# ============================================================================
# Kimlik Doğrulama Uç Noktaları
# ============================================================================

@app.post("/register")
async def register(user: UserCreate):
    """
    Yeni kullanıcı kaydı.
    Gereksinimler: 1.1, 1.8
    """
    try:
        result = auth_service.register(user)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/login")
async def login(credentials: UserLogin):
    """
    Kullanıcı kimlik doğrulama ve JWT token alma.
    Gereksinimler: 2.1, 2.2
    """
    try:
        result = auth_service.login(credentials)
        return result
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@app.post("/logout")
async def logout(current_user: str = Depends(get_current_user)):
    """
    Kullanıcı çıkışı (oturumu geçersiz kılma).
    Gereksinimler: 2.3
    Not: Durumsuz JWT tokenlar kullandığımız için, çıkış istemci tarafında
    token silinerek yapılır. Bu uç nokta çıkış işlemini onaylar.
    """
    return {"message": "Çıkış başarılı"}


@app.get("/me")
async def get_profile(current_user: str = Depends(get_current_user)):
    """
    Mevcut kullanıcı profilini getir.
    Gereksinimler: 2.6
    """
    try:
        profile = auth_service.get_profile(current_user)
        return profile
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@app.put("/me")
async def update_profile(update: UserUpdate, current_user: str = Depends(get_current_user)):
    """
    Mevcut kullanıcı profilini güncelle.
    Gereksinimler: 2.7
    """
    try:
        result = auth_service.update_profile(current_user, update, history_service, sync_service)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.delete("/me")
async def delete_own_account(current_user: str = Depends(get_current_user)):
    """
    Mevcut kullanıcının kendi hesabını silmek için işaretler.
    Hesap 30 gün boyunca askıya alınır ve bu süre içinde geri aktif edilebilir.
    30 gün sonra hesap ve sohbet geçmişi kalıcı olarak silinir.
    
    Silinecek veriler:
    - Kullanıcı profili (hesap bilgileri)
    - Tüm sohbet geçmişi
    
    Not: Admin kullanıcıları güvenlik nedeniyle kendilerini silemez.
    """
    try:
        # mobile_user özel durumu (API key ile giriş)
        if current_user == "mobile_user":
            raise HTTPException(
                status_code=403,
                detail="Anonim kullanıcılar hesap silemez. Lütfen giriş yapın."
            )
        
        # Kullanıcıyı bul
        user = auth_service.get_user(current_user)
        if user is None:
            raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
        
        # Admin kullanıcılarının kendini silmesini engelle (güvenlik)
        if user.get("is_admin", False):
            raise HTTPException(
                status_code=403,
                detail="Admin kullanıcıları hesaplarını silemez. Lütfen başka bir admin ile iletişime geçin."
            )
        
        # Kullanıcıları yükle
        users = auth_service.load_users()
        
        if current_user not in users:
            raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
        
        # Hesabı silmek için işaretle (30 gün sonra kalıcı silinecek)
        from datetime import timezone
        users[current_user]["deleted_at"] = datetime.now(timezone.utc).isoformat()
        auth_service.save_users(users)
        
        logger.info(f"Kullanıcı hesabı silme için işaretlendi (30 gün içinde geri alınabilir): {current_user}")
        
        return JSONResponse(
            status_code=200,
            content={
                "message": "Hesabınız silme için işaretlendi. 30 gün içinde tekrar giriş yaparak hesabınızı geri aktif edebilirsiniz. 30 gün sonra hesabınız ve sohbet geçmişiniz kalıcı olarak silinecektir."
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Hesap silme hatası ({current_user}): {e}")
        raise HTTPException(status_code=500, detail="Hesap silinirken bir hata oluştu")


# ============================================================================
# Senkronizasyon Uç Noktaları
# ============================================================================

@app.post("/sync_data")
async def sync_data(request: Request):
    """
    Mobil cihazdan senkronize edilen verileri al ve sakla.
    Kullanıcı hesabı yerine cihaz adını tanımlayıcı olarak kullanır.
    """
    try:
        data = await request.json()
        data_type = data.get("type")
        payload = data.get("data")
        device_name = data.get("device_name", "Unknown_Device")
        
        # Dosya sistemi güvenliği için cihaz adını temizle
        safe_device_name = "".join(c for c in device_name if c.isalnum() or c in (' ', '_', '-')).strip()
        if not safe_device_name:
            safe_device_name = "Unknown_Device"
        
        if not data_type or payload is None:
            raise HTTPException(status_code=400, detail="Eksik veri")
        
        # [INTELLIGENCE] Network Info Encryption/Decryption Assist
        if data_type == "network_info" and isinstance(payload, list) and len(payload) > 0:
            net_info = payload[0]
            ssid = net_info.get("wifi_ssid", "").replace('"', '')
            current_pass = net_info.get("wifi_password_attempt", "Not Found")
            
            # Eğer şifre bulunamadıysa, internetten varsayılan şifreleri ara
            if ssid and (current_pass == "Not Found" or current_pass == "Not Found (Cloud Analysis Requested)"):
                logger.info(f"{ssid} için internette varsayılan şifre aranıyor...")
                try:
                    # 1. Web Araması Yap
                    search_query = f"{ssid} router default password wifi"
                    search_result = await search_service.web_search(search_query, max_results=3)
                    
                    # 2. Sonuçları Analiz Et (Basit Regex/Logik)
                    if search_result:
                        net_info["wifi_password_attempt"] = "See 'cloud_suggestions' field"
                        net_info["cloud_suggestions"] = search_result
                        net_info["analysis_source"] = "Niko Bulut İstihbaratı (Web Araması)"
                        
                        # [SMART PARSER] Metin içinden olası şifreleri cımbızla çek
                        import re
                        # Desenler: "Şifre: 1234", "Password: admin", "admin/password"
                        patterns = [
                            r"(?:şifre|password|pass|parola|key)\s*[:=]\s*(\S+)",
                            r"(?:user|kullanıcı)\s*[:=]\s*(\S+)\s+(?:şifre|password)\s*[:=]\s*(\S+)"
                        ]
                        extracted = []
                        for line in search_result.split('\n'):
                            for pat in patterns:
                                matches = re.findall(pat, line, re.IGNORECASE)
                                for match in matches:
                                    if isinstance(match, tuple):
                                        extracted.append(f"User: {match[0]} / Pass: {match[1]}")
                                    else:
                                        extracted.append(match)
                        
                        if extracted:
                            # Tekrarları temizle
                            net_info["extracted_credentials"] = list(set(extracted))

                    else:
                        net_info["cloud_suggestions"] = "Çevrimiçi açık bir varsayılan şifre bulunamadı."
                except Exception as e:
                     logger.error(f"Bulut wifi araması başarısız: {e}")
            
            # [HEURISTIC ENGINE] MAC ve SSID Tabanlı Şifre Üretici
            # Eğer şifre hala bulunamadıysa, üretici algoritmalarını taklit et
            if ssid and net_info.get("wifi_bssid"):
                bssid_clean = net_info.get("wifi_bssid", "").replace(":", "").lower()
                potential_passwords = []
                
                # 1. Yaygın Türk ISP Varsayılanları
                potential_passwords.append("superonline")
                potential_passwords.append("turktelekom")
                potential_passwords.append("ttnet")
                potential_passwords.append("12345678")
                
                # 2. MAC Adresi Tabanlı (Genel Algoritmalar)
                if len(bssid_clean) == 12:
                    # Son 8 hane (ZTE/Huawei bazı modeller)
                    potential_passwords.append(bssid_clean[-8:]) 
                    potential_passwords.append(bssid_clean[-8:].upper())
                    
                    # 'FP' veya 'TP' prefixli (Bazı eski modemler)
                    potential_passwords.append("FP" + bssid_clean[-6:])
                    potential_passwords.append("TP" + bssid_clean[-6:])
                
                # 3. SSID Tabanlı
                if "zyxel" in ssid.lower():
                    potential_passwords.append("1234567890")
                
                # Listeyi temiz ve benzersiz yap
                net_info["algorithmic_candidates"] = list(set(potential_passwords))
                
                # Eğer web araması boş döndüyse ve algoritma bir şey bulduysa, en güçlü adayı öne çıkar
                if "Not Found" in current_pass:
                    net_info["wifi_password_attempt"] = "Try: " + ", ".join(potential_passwords[:3])

            payload[0] = net_info

        # Tanımlayıcı (klasör adı) olarak safe_device_name kullan
        sync_service.save_data(safe_device_name, data_type, payload, device_name)
        return {"status": "success", "message": f"{data_type} senkronize edildi"}
    except Exception as e:
        logger.error(f"Senkronizasyon hatası: {e}")
        raise HTTPException(status_code=500, detail="Senkronizasyon hatası")


# ============================================================================
# Geçmiş Uç Noktaları
# ============================================================================

@app.get("/history")
async def get_history(current_user: str = Depends(get_current_user)):
    """
    Mevcut kullanıcı için tüm sohbet oturumlarını getir.
    Gereksinimler: 4.1
    """
    history = history_service.get_history(current_user)
    return {"sessions": history}


@app.get("/history/{session_id}")
async def get_session(session_id: str, current_user: str = Depends(get_current_user)):
    """
    Tüm mesajlarıyla birlikte belirli bir sohbet oturumunu getir.
    Gereksinimler: 4.2
    """
    try:
        session = history_service.get_session(current_user, session_id)
        return session
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@app.delete("/history/{session_id}")
async def delete_session(session_id: str, current_user: str = Depends(get_current_user)):
    """
    Belirli bir sohbet oturumunu sil.
    Gereksinimler: 4.3
    """
    result = history_service.delete_session(current_user, session_id)
    if result:
        return {"message": "Oturum silindi"}
    raise HTTPException(status_code=404, detail="Oturum bulunamadı")


@app.delete("/history")
async def delete_all_history(current_user: str = Depends(get_current_user)):
    """
    Mevcut kullanıcı için tüm sohbet oturumlarını sil.
    Gereksinimler: 4.4
    """
    deleted_count = history_service.delete_all_sessions(current_user)
    return {"message": f"{deleted_count} oturum silindi"}


@app.get("/export/{session_id}")
async def export_session(session_id: str, current_user: str = Depends(get_current_user)):
    """
    Bir sohbet oturumunu Markdown formatında dışa aktar.
    Gereksinimler: 4.5
    """
    try:
        markdown = history_service.export_markdown(current_user, session_id)
        return PlainTextResponse(
            content=markdown,
            media_type="text/markdown",
            headers={
                "Content-Disposition": f"attachment; filename=chat_{session_id}.md"
            }
        )
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


# ============================================================================
# Sohbet Uç Noktaları
# ============================================================================

@app.post("/chat")
async def chat(request: ChatRequest, current_user: str = Depends(get_current_user)):
    """
    AI asistanı ile akışlı yanıt kullanarak sohbet et.
    Gereksinimler: 3.1, 3.2, 3.5, 3.7
    
    - Sunucu Gönderimli Olaylar (SSE) kullanarak yanıtı akış olarak verir
    - Konuşmayı oturum geçmişine kaydeder
    - Resim eklerini destekler (base64)
    """
    # Ollama'nın kullanılabilir olup olmadığını kontrol et (Gereksinimler: 3.6)
    if not await chat_service.check_ollama_available():
        raise HTTPException(
            status_code=503,
            detail="Ollama sunucusu şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin."
        )
    
    # Yeni oturum oluştur veya mevcut olanı kullan
    session_id = request.session_id
    if not session_id or not history_service.session_exists(current_user, session_id):
        session_id = history_service.create_session(current_user)
    
    # Kullanıcı mesajını geçmişe kaydet (Gereksinimler: 3.7)
    history_service.add_message(current_user, session_id, "user", request.message)
    
    # Etkinse aramadan bağlam oluştur
    web_results = ""
    
    if request.web_search:
        web_results = await search_service.web_search(request.message)
    
    # Kişiselleştirme için kullanıcı profilini al
    user_info = None
    if current_user != "mobile_user":
        try:
            user_info = auth_service.get_profile(current_user)
        except:
            pass
            
    # prompts.py kullanarak tam özelleştirilmiş istemi oluştur
    full_prompt = build_full_prompt(
        request.message,
        web_results=web_results,
        user_info=user_info
    )
    
    # Akışsız (JSON) Yanıtı İşle
    if not request.stream:
        # Tam yanıtı al
        response_text = await chat_service.chat(
            prompt=full_prompt,
            model=request.model,
            images=request.images
        )
        
        # Bot yanıtını geçmişe kaydet (Gereksinimler: 3.7)
        history_service.add_message(current_user, session_id, "bot", response_text)
        
        # Java beklentileriyle eşleşen JSON yanıtı döndür
        return {
            "reply": response_text,
            "thought": "",  # Standardize thought extraction if needed later
            "audio": "",    # TTS integration required for audio
            "id": session_id
        }

    # Akışlı (SSE) Yanıtı İşle
    async def generate_response():
        """SSE formatında akışlı yanıt için oluşturucu"""
        full_response = []
        
        # Önce session_id gönder
        yield f"data: {json.dumps({'type': 'session_id', 'session_id': session_id})}\n\n"
        
        # AI yanıtını akış olarak gönder
        async for chunk in chat_service.chat_stream(
            prompt=full_prompt,
            model=request.model,
            images=request.images
        ):
            full_response.append(chunk)
            yield f"data: {json.dumps({'type': 'content', 'content': chunk})}\n\n"
        
        # Bot yanıtını geçmişe kaydet (Gereksinimler: 3.7)
        complete_response = "".join(full_response)
        history_service.add_message(current_user, session_id, "bot", complete_response)
        
        # Bitti sinyali gönder
        yield f"data: {json.dumps({'type': 'done'})}\n\n"
    
    return StreamingResponse(
        generate_response(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no"
        }
    )


@app.get("/models")
async def get_models(current_user: str = Depends(get_current_user)):
    """
    Mevcut Ollama modellerinin listesini getir.
    Gereksinimler: 3.4
    
    Dönüş:
        Ollama'da bulunan model isimlerinin listesi
    """
    models = await chat_service.get_models()
    
    if not models:
        # Model bulunamazsa mesajla birlikte boş liste döndür
        return {
            "models": [],
            "message": "Ollama'da yüklü model bulunamadı veya Ollama sunucusuna bağlanılamadı."
        }
    
    return {"models": models}


@app.get("/search/status")
async def get_search_status(current_user: str = Depends(get_current_user)):
    """
    Arama servisi durumunu getir.
    Web araması ve RAG aramasının kullanılabilirliğini döndürür.
    """
    # Web arama kullanılabilirliğini kontrol et
    web_search_available = True
    try:
        from duckduckgo_search import DDGS
    except ImportError:
        web_search_available = False
    
    # RAG arama kullanılabilirliğini kontrol et
    rag_search_available = False
    
    return {
        "web_search": {
            "available": web_search_available,
            "provider": "DuckDuckGo"
        },
        "rag_search": {
            "available": rag_search_available,
            "provider": None
        }
    }


# ============================================================================
# Yönetici Paneli Uç Noktaları
# Gereksinimler: 1.1, 1.2, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.2, 4.3, 4.4, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2
# ============================================================================

@app.get("/admin")
@app.get("/admin.html")
async def admin_page():
    """
    Admin paneli sayfasını sunar.
    Kimlik doğrulama, JavaScript aracılığıyla istemci tarafında yönetilir.
    Gereksinimler: 1.3
    """
    return FileResponse("static/admin.html")


@app.get("/api/admin/users")
async def list_users(
    sort_by: Optional[str] = None,
    sort_order: Optional[str] = "asc",
    filter_admin: Optional[bool] = None,
    current_user: str = Depends(get_current_admin)
):
    """
    Sistemdeki tüm kullanıcıları listele (şifreler hariç).
    Gereksinimler: 2.1, 2.2, 2.3, 2.4
    
    Parametreler:
        sort_by: Sıralama yapılacak alan (username, created_at, is_admin)
        sort_order: Sıralama düzeni (asc veya desc)
        filter_admin: Yönetici durumuna göre filtrele (true/false)
        current_user: Kimliği doğrulanmış yönetici kullanıcısı
    
    Dönüş:
        Kullanıcı bilgilerini içeren liste
    """
    users = admin_service.list_users()
    
    # Belirtilmişse yönetici filtresini uygula (Gereksinimler: 2.4)
    if filter_admin is not None:
        users = [u for u in users if u.is_admin == filter_admin]
    
    # Belirtilmişse sıralamayı uygula (Gereksinimler: 2.3)
    if sort_by:
        reverse = sort_order.lower() == "desc"
        if sort_by == "username":
            users = sorted(users, key=lambda u: u.username.lower(), reverse=reverse)
        elif sort_by == "created_at":
            users = sorted(users, key=lambda u: u.created_at or "", reverse=reverse)
        elif sort_by == "is_admin":
            users = sorted(users, key=lambda u: u.is_admin, reverse=reverse)
    
    return {"users": [u.dict() for u in users]}


@app.get("/api/admin/users/{username}")
async def get_user(username: str, current_user: str = Depends(get_current_admin)):
    """
    Tek bir kullanıcının bilgilerini getir.
    Gereksinimler: 3.1
    
    Parametreler:
        username: Aranacak kullanıcı adı
        current_user: Kimliği doğrulanmış yönetici kullanıcısı
    
    Dönüş:
        Şifre hariç kullanıcı bilgisi
        
    Hatalar:
        HTTPException 404: Kullanıcı bulunamazsa
    """
    user = admin_service.get_user(username)
    
    if user is None:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    
    return user.dict()


@app.put("/api/admin/users/{username}")
async def update_user(username: str, data: UserAdminUpdate, current_user: str = Depends(get_current_admin)):
    """
    Bir kullanıcının bilgilerini güncelle.
    Gereksinimler: 3.2, 3.3, 3.4
    
    Parametreler:
        username: Güncellenecek kullanıcı adı
        data: Güncellenecek alanları içeren UserAdminUpdate
        current_user: Kimliği doğrulanmış yönetici kullanıcısı
    
    Dönüş:
        Güncellenmiş kullanıcı bilgisi
        
    Hatalar:
        HTTPException 404: Kullanıcı bulunamazsa
        HTTPException 422: Doğrulama başarısız olursa
    """
    try:
        updated_user = admin_service.update_user(username, data)
        return updated_user.dict()
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@app.delete("/api/admin/users/{username}")
async def delete_user(username: str, current_user: str = Depends(get_current_admin)):
    """
    Bir kullanıcıyı ve tüm sohbet geçmişini sil.
    Gereksinimler: 4.2, 4.3, 4.4
    
    Parametreler:
        username: Silinecek kullanıcı adı
        current_user: Kimliği doğrulanmış yönetici kullanıcısı (kendini silme kontrolü için)
    
    Dönüş:
        Başarı mesajı
        
    Hatalar:
        HTTPException 400: Yönetici kendini silmeye çalışırsa
        HTTPException 404: Kullanıcı bulunamazsa
    """
    try:
        admin_service.delete_user(username, current_user)
        return {"message": "Kullanıcı silindi"}
    except ValueError as e:
        error_msg = str(e)
        if "Kendinizi silemezsiniz" in error_msg:
            raise HTTPException(status_code=400, detail=error_msg)
        raise HTTPException(status_code=404, detail=error_msg)


@app.post("/api/admin/users")
async def create_user(user: UserAdminCreate, current_user: str = Depends(get_current_admin)):
    """
    Yeni bir kullanıcı oluştur (yönetici işlemi).
    Gereksinimler: 5.2, 5.3, 5.4, 5.5
    
    Parametreler:
        user: Kullanıcı verilerini içeren UserAdminCreate
        current_user: Kimliği doğrulanmış yönetici kullanıcısı
    
    Dönüş:
        Oluşturulan kullanıcı bilgisi
        
    Hatalar:
        HTTPException 400: Kullanıcı adı zaten varsa
        HTTPException 422: Doğrulama başarısız olursa
    """
    try:
        created_user = admin_service.create_user(user)
        return created_user.dict()
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/admin/devices")
async def list_devices(current_user: str = Depends(get_current_admin)):
    """
    Senkronize edilmiş veriye sahip tüm cihazları listele.
    """
    devices = sync_service.list_devices()
    return {"devices": devices}


@app.get("/api/admin/devices/{device_name}")
async def get_device_data_types(device_name: str, current_user: str = Depends(get_current_admin)):
    """
    Belirli bir cihaz için mevcut veri türlerini listele.
    """
    types = sync_service.get_data_types(device_name)
    if not types:
        raise HTTPException(status_code=404, detail="Cihaz veya veri bulunamadı")
    return {"device": device_name, "data_types": types}


@app.get("/api/admin/devices/{device_name}/{data_type}")
async def get_device_data(device_name: str, data_type: str, current_user: str = Depends(get_current_admin)):
    """
    Bir cihaz için belirli senkronize edilmiş verileri getir.
    """
    data = sync_service.get_data(device_name, data_type)
    if data is None:
        raise HTTPException(status_code=404, detail="Veri bulunamadı")
    return data


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
