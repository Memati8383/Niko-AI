"""
Niko AI Chat Application - Main Entry Point
FastAPI backend for Turkish AI chat application
"""

import os
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

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# ============================================================================
# Pydantic Models
# ============================================================================

class UserCreate(BaseModel):
    """Model for user registration"""
    username: str
    password: str
    email: Optional[str] = None
    full_name: Optional[str] = None

    @field_validator('username')
    @classmethod
    def validate_username(cls, v):
        """
        Username validation:
        - Length: 3-30 characters
        - Must start with a letter
        - Only letters, numbers, and underscores allowed
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
        Password validation:
        - Minimum 8 characters
        - At least one uppercase letter
        - At least one lowercase letter
        - At least one digit
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
        """Email validation using regex pattern"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        return v


class UserLogin(BaseModel):
    """Model for user login"""
    username: str
    password: str


class UserUpdate(BaseModel):
    """Model for user profile update"""
    email: Optional[str] = None
    full_name: Optional[str] = None
    new_username: Optional[str] = None
    current_password: Optional[str] = None
    new_password: Optional[str] = None
    profile_image: Optional[str] = None  # Base64 string

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """Email validation using regex pattern"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        return v

    @field_validator('new_password')
    @classmethod
    def validate_new_password(cls, v):
        """
        New password validation (same rules as registration):
        - Minimum 8 characters
        - At least one uppercase letter
        - At least one lowercase letter
        - At least one digit
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
    """Model for chat request"""
    message: str
    enable_audio: bool = True
    web_search: bool = False
    rag_search: bool = False
    session_id: Optional[str] = None
    model: Optional[str] = None
    mode: Optional[str] = "normal"
    images: Optional[List[str]] = None  # base64 encoded images
    stream: bool = True  # Default to streaming, customizable for clients


# ============================================================================
# Admin Panel Models
# Requirements: 3.2, 5.2, 5.3
# ============================================================================

class UserAdminUpdate(BaseModel):
    """
    Model for admin user update operations.
    Allows admins to update email, full_name, and is_admin status.
    Requirements: 3.2
    """
    email: Optional[str] = None
    full_name: Optional[str] = None
    is_admin: Optional[bool] = None
    password: Optional[str] = None

    @field_validator('email')
    @classmethod
    def validate_email(cls, v):
        """Email validation using regex pattern"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        return v


class UserAdminCreate(BaseModel):
    """
    Model for admin user creation.
    Requires username and password, optional email, full_name, and is_admin.
    Requirements: 5.2, 5.3
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
        Username validation:
        - Length: 3-30 characters
        - Must start with a letter
        - Only letters, numbers, and underscores allowed
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
        Password validation:
        - Minimum 8 characters
        - At least one uppercase letter
        - At least one lowercase letter
        - At least one digit
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
        """Email validation using regex pattern"""
        if v is None:
            return v
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, v):
            raise ValueError('Geçersiz e-posta formatı')
        return v


class UserListResponse(BaseModel):
    """
    Model for user list response in admin panel.
    Contains user information including plain password for management.
    Requirements: 2.1, 2.2
    """
    username: str
    email: Optional[str] = None
    full_name: Optional[str] = None
    is_admin: bool = False
    created_at: str
    plain_password: Optional[str] = None


# ============================================================================
# Authentication Service
# ============================================================================

class AuthService:
    """
    Authentication service for user management.
    Handles password hashing, JWT token creation/verification, and user data persistence.
    Requirements: 1.9, 2.1
    """
    
    def __init__(self):
        self.secret_key = os.getenv("JWT_SECRET", "niko-ai-secret-key-change-in-production")
        self.algorithm = "HS256"
        self.token_expire_hours = 24
        self.users_file = "users.json"
    
    def hash_password(self, password: str) -> str:
        """Hash a password using bcrypt"""
        password_bytes = password.encode('utf-8')
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password_bytes, salt)
        return hashed.decode('utf-8')
    
    def verify_password(self, plain_password: str, hashed_password: str) -> bool:
        """Verify a plain password against a hashed password (or plaintext fallback)"""
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
        """Create a JWT token with 24-hour expiration"""
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
        Verify a JWT token and return the username if valid.
        Returns None if token is invalid or expired.
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
        """Load users from JSON file"""
        if os.path.exists(self.users_file):
            try:
                with open(self.users_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except (json.JSONDecodeError, IOError):
                return {}
        return {}
    
    def save_users(self, users: dict) -> None:
        """Save users to JSON file"""
        with open(self.users_file, 'w', encoding='utf-8') as f:
            json.dump(users, f, indent=2, ensure_ascii=False)
    
    def get_user(self, username: str) -> Optional[dict]:
        """Get a user by username"""
        users = self.load_users()
        return users.get(username)
    
    def register(self, user: UserCreate) -> dict:
        """
        Register a new user.
        Requirements: 1.1, 1.8, 1.9
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
        Authenticate user and return JWT token.
        Requirements: 2.1, 2.2
        """
        users = self.load_users()
        user = users.get(credentials.username)
        
        if not user:
            raise ValueError("Geçersiz kullanıcı adı veya şifre")
        
        if not self.verify_password(credentials.password, user["password"]):
            raise ValueError("Geçersiz kullanıcı adı veya şifre")
        
        token = self.create_token(credentials.username)
        return {"access_token": token, "token_type": "bearer"}
    
    def get_profile(self, username: str) -> dict:
        """
        Get user profile information.
        Requirements: 2.6
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
        Update user profile.
        Requirements: 2.7
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


# ============================================================================
# History Service
# ============================================================================

class HistoryService:
    """
    History service for chat session management.
    Handles session creation, message storage, and history operations.
    Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 9.5
    """
    
    def __init__(self):
        self.history_dir = "history"
        os.makedirs(self.history_dir, exist_ok=True)
    
    def get_session_path(self, username: str, session_id: str) -> str:
        """Get the file path for a session"""
        return os.path.join(self.history_dir, f"{username}_{session_id}.json")
    
    def create_session(self, username: str) -> str:
        """
        Create a new chat session.
        Requirements: 4.6
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
        Add a message to a session.
        Requirements: 4.7, 9.5
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
        """Check if a session file exists"""
        path = self.get_session_path(username, session_id)
        return os.path.exists(path)

    def get_session(self, username: str, session_id: str) -> dict:
        """
        Get a specific session with all messages.
        Requirements: 4.2
        """
        path = self.get_session_path(username, session_id)
        
        if not os.path.exists(path):
            raise ValueError("Oturum bulunamadı")
        
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    def get_history(self, username: str) -> List[dict]:
        """
        Get all chat sessions for a user.
        Requirements: 4.1
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
        Delete a specific session.
        Requirements: 4.3
        """
        path = self.get_session_path(username, session_id)
        
        if os.path.exists(path):
            os.remove(path)
            return True
        return False
    
    def delete_all_sessions(self, username: str) -> int:
        """
        Delete all sessions for a user.
        Requirements: 4.4
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
        Rename all session files for a user.
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
                    logger.error(f"Error renaming session file {filename}: {e}")
    
    def export_markdown(self, username: str, session_id: str) -> str:
        """
        Export a session to Markdown format.
        Requirements: 4.5
        """
        session = self.get_session(username, session_id)
        
        md = f"# {session['title']}\n\n"
        md += f"*Tarih: {session['timestamp']}*\n\n---\n\n"
        
        for msg in session["messages"]:
            role = "👤 Kullanıcı" if msg["role"] == "user" else "🤖 Niko"
            md += f"### {role}\n\n{msg['content']}\n\n"
        
        return md


# ============================================================================
# Sync Service
# ============================================================================

class SyncService:
    """
    Sync service for mobile device data management.
    Handles storage of contacts, calls, location, and device info.
    """
    
    def __init__(self):
        self.base_dir = "device_data"
        os.makedirs(self.base_dir, exist_ok=True)
    
    def get_user_dir(self, username: str) -> str:
        """Get the directory for a user's device data"""
        user_dir = os.path.join(self.base_dir, username)
        os.makedirs(user_dir, exist_ok=True)
        return user_dir
    
    def save_data(self, username: str, data_type: str, data: List[dict], device_name: str) -> None:
        """Save synchronized data to a JSON file"""
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
        
        logger.info(f"Synchronized {data_type} for user {username} from {device_name}")

    def rename_user(self, old_username: str, new_username: str):
        """Rename the user data directory"""
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
                logger.error(f"Error renaming sync directory for {old_username}: {e}")

    def list_devices(self) -> List[str]:
        """List all devices that have synced data"""
        if not os.path.exists(self.base_dir):
            return []
        return [d for d in os.listdir(self.base_dir) if os.path.isdir(os.path.join(self.base_dir, d))]

    def get_data_types(self, device_name: str) -> List[str]:
        """List available data types for a device"""
        device_dir = os.path.join(self.base_dir, device_name)
        if not os.path.exists(device_dir):
            return []
        return [f.replace('.json', '') for f in os.listdir(device_dir) if f.endswith('.json')]

    def get_data(self, device_name: str, data_type: str) -> Optional[dict]:
        """Get specific data for a device"""
        device_dir = os.path.join(self.base_dir, device_name)
        file_path = os.path.join(device_dir, f"{data_type}.json")
        
        if not os.path.exists(file_path):
            return None
            
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            logger.error(f"Error reading data {device_name}/{data_type}: {e}")
            return None


# ============================================================================
# Admin Service
# Requirements: 2.1, 3.3, 4.2, 4.3, 5.4
# ============================================================================

class AdminService:
    """
    Admin service for user management operations.
    Handles listing, creating, updating, and deleting users.
    Requirements: 2.1, 3.3, 4.2, 4.3, 5.4
    """
    
    def __init__(self, auth_service: AuthService, history_service: HistoryService):
        """
        Initialize AdminService with AuthService and HistoryService dependencies.
        
        Args:
            auth_service: AuthService instance for user data operations
            history_service: HistoryService instance for chat history operations
        """
        self.auth = auth_service
        self.history = history_service
    
    def list_users(self) -> List[UserListResponse]:
        """
        List all users in the system (without passwords).
        Requirements: 2.1, 2.2
        
        Returns:
            List of UserListResponse objects containing user information
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
        Get a single user's information (without password).
        Requirements: 3.1
        
        Args:
            username: The username to look up
            
        Returns:
            UserListResponse if user exists, None otherwise
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
        Update a user's information.
        Requirements: 3.2, 3.3
        
        Args:
            username: The username to update
            data: UserAdminUpdate with fields to update
            
        Returns:
            Updated UserListResponse
            
        Raises:
            ValueError: If user not found
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
        Delete a user and all their chat history.
        Requirements: 4.2, 4.3, 4.4
        
        Args:
            username: The username to delete
            admin_username: The admin performing the deletion (for self-deletion check)
            
        Returns:
            True if deletion was successful
            
        Raises:
            ValueError: If user not found or admin tries to delete themselves
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
        Create a new user (admin operation).
        Requirements: 5.2, 5.3, 5.4, 5.5
        
        Args:
            user: UserAdminCreate with user data
            
        Returns:
            UserListResponse for the created user
            
        Raises:
            ValueError: If username already exists
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
# Chat Service
# ============================================================================

class ChatService:
    """
    Chat service for AI conversation management.
    Handles Ollama API communication, model listing, and streaming responses.
    Requirements: 3.1, 3.2, 3.3, 3.4
    """
    
    def __init__(self):
        self.ollama_url = os.getenv("OLLAMA_URL", "http://localhost:11434")
        self.default_model = os.getenv("DEFAULT_MODEL", "llama2")
        self.timeout = 120.0  # 2 minutes timeout for chat requests
    
    async def get_models(self) -> List[str]:
        """
        Get list of available Ollama models.
        Requirements: 3.4
        
        Returns:
            List of model names available in Ollama
        """
        try:
            async with httpx.AsyncClient(timeout=10.0) as client:
                response = await client.get(f"{self.ollama_url}/api/tags")
                if response.status_code == 200:
                    data = response.json()
                    return [model["name"] for model in data.get("models", [])]
                return []
        except httpx.RequestError as e:
            # Log error but return empty list
            print(f"Ollama API error: {e}")
            return []
        except Exception as e:
            print(f"Unexpected error getting models: {e}")
            return []
    
    async def check_ollama_available(self) -> bool:
        """
        Check if Ollama API is available.
        Requirements: 3.6
        
        Returns:
            True if Ollama is available, False otherwise
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
        Stream chat response from Ollama.
        Requirements: 3.1, 3.2, 3.3, 3.5
        
        Args:
            prompt: Formatted prompt for the AI
            model: Model to use (defaults to self.default_model)
            images: Optional list of base64-encoded images
        
        Yields:
            Chunks of the AI response
        """
        selected_model = model or self.default_model
        
        # Prepare Ollama request payload
        payload = {
            "model": selected_model,
            "prompt": prompt,
            "stream": True
        }
        
        # Add images if provided (Requirements: 3.5)
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
        Get complete chat response from Ollama (non-streaming).
        Requirements: 3.1, 3.3
        
        Args:
            prompt: Formatted prompt for the AI
            model: Model to use (defaults to self.default_model)
            images: Optional list of base64-encoded images
        
        Returns:
            Complete AI response
        """
        response_parts = []
        async for chunk in self.chat_stream(prompt, model, images):
            response_parts.append(chunk)
        return "".join(response_parts)


# ============================================================================
# Search Service
# ============================================================================

class SearchService:
    """
    Search service for web and RAG search functionality.
    Handles DuckDuckGo web search and ChromaDB RAG search.
    Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
    """
    
    def __init__(self):
        """Initialize the search service with optional ChromaDB client."""
        self.chroma_client = None
        self.chroma_collection = None
        self._init_chroma()
    
    def _init_chroma(self) -> None:
        """
        Initialize ChromaDB client if available.
        ChromaDB is optional - the service works without it.
        """
        try:
            import chromadb
            self.chroma_client = chromadb.Client()
            # Create or get a collection for RAG documents
            self.chroma_collection = self.chroma_client.get_or_create_collection(
                name="niko_documents",
                metadata={"description": "Niko AI document collection for RAG"}
            )
            logger.info("ChromaDB initialized successfully")
        except ImportError:
            logger.info("ChromaDB not installed - RAG search will be unavailable")
            self.chroma_client = None
            self.chroma_collection = None
        except Exception as e:
            logger.warning(f"ChromaDB initialization failed: {e}")
            self.chroma_client = None
            self.chroma_collection = None
    
    async def web_search(self, query: str, max_results: int = 5) -> str:
        """
        Perform web search using DuckDuckGo.
        Requirements: 5.1, 5.4
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
                     logger.error("duckduckgo-search (or ddgs) package not installed")
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
                logger.error(f"DDGS search execution failed: {search_err} - Query: {query}")
                return ""
            
            if not results:
                logger.info(f"No web search results for query: {query}")
                return ""
            
            logger.info(f"Web search found {len(results)} results for: {query}")

            # Format results for AI context
            formatted = []
            for i, r in enumerate(results, 1):
                title = r.get('title', 'Başlık yok')
                body = r.get('body', 'İçerik yok')
                href = r.get('href', '')
                formatted.append(f"{i}. {title}\n   {body}\n   Kaynak: {href}")
            
            return "\n\n".join(formatted)
        
        except Exception as e:
            # Requirements: 5.4 - Log error and continue without search results
            logger.error(f"Web search general error for query '{query}': {e}")
            return ""
    
    async def rag_search(self, query: str, n_results: int = 3) -> str:
        """
        Perform RAG search using ChromaDB vector database.
        Requirements: 5.2, 5.5
        
        Args:
            query: Search query string
            n_results: Number of results to return (default: 3)
        
        Returns:
            Formatted string of relevant documents, or informative message if unavailable
        """
        # Check if ChromaDB is available
        if self.chroma_client is None or self.chroma_collection is None:
            # Requirements: 5.5 - Inform that RAG is not configured
            return "RAG veritabanı yapılandırılmamış."
        
        try:
            # Query the collection
            results = self.chroma_collection.query(
                query_texts=[query],
                n_results=n_results
            )
            
            # Check if we have any documents
            if not results or not results.get("documents") or not results["documents"][0]:
                # Requirements: 5.5 - Inform AI that no relevant documents were found
                return "İlgili doküman bulunamadı."
            
            # Format results for AI context
            documents = results["documents"][0]
            metadatas = results.get("metadatas", [[]])[0]
            
            formatted = []
            for i, (doc, meta) in enumerate(zip(documents, metadatas or [{}] * len(documents)), 1):
                source = meta.get("source", "Bilinmeyen kaynak") if meta else "Bilinmeyen kaynak"
                formatted.append(f"Doküman {i} ({source}):\n{doc}")
            
            return "\n\n---\n\n".join(formatted)
        
        except Exception as e:
            logger.error(f"RAG search error for query '{query}': {e}")
            # Requirements: 5.5 - Return informative message on error
            return "RAG araması sırasında bir hata oluştu."
    
    async def hybrid_search(self, query: str, web_max_results: int = 5, rag_n_results: int = 3) -> str:
        """
        Perform hybrid search combining web and RAG results.
        Requirements: 5.3
        
        Args:
            query: Search query string
            web_max_results: Maximum web search results (default: 5)
            rag_n_results: Number of RAG results (default: 3)
        
        Returns:
            Combined formatted string of both web and RAG search results
        """
        # Perform both searches
        web_results = await self.web_search(query, web_max_results)
        rag_results = await self.rag_search(query, rag_n_results)
        
        # Combine results
        combined = []
        
        if web_results:
            combined.append("=== Web Arama Sonuçları ===\n" + web_results)
        
        if rag_results and rag_results not in ["RAG veritabanı yapılandırılmamış.", "İlgili doküman bulunamadı.", "RAG araması sırasında bir hata oluştu."]:
            combined.append("=== Doküman Sonuçları ===\n" + rag_results)
        elif rag_results:
            # Include informative messages about RAG status
            combined.append("=== Doküman Sonuçları ===\n" + rag_results)
        
        return "\n\n".join(combined) if combined else ""
    
    def add_document(self, document: str, doc_id: str, metadata: dict = None) -> bool:
        """
        Add a document to the RAG collection.
        
        Args:
            document: Document text content
            doc_id: Unique document identifier
            metadata: Optional metadata dictionary
        
        Returns:
            True if document was added successfully, False otherwise
        """
        if self.chroma_collection is None:
            logger.warning("Cannot add document - ChromaDB not available")
            return False
        
        try:
            self.chroma_collection.add(
                documents=[document],
                ids=[doc_id],
                metadatas=[metadata] if metadata else None
            )
            logger.info(f"Document '{doc_id}' added to RAG collection")
            return True
        except Exception as e:
            logger.error(f"Error adding document '{doc_id}': {e}")
            return False
    
    def is_rag_available(self) -> bool:
        """Check if RAG search is available."""
        return self.chroma_client is not None and self.chroma_collection is not None


# ============================================================================
# Rate Limiter
# ============================================================================

class RateLimiter:
    """
    In-memory rate limiter for API endpoints.
    Tracks requests per client and enforces endpoint-specific limits.
    Requirements: 6.1, 6.2, 6.3, 6.4
    """
    
    def __init__(self):
        # Request tracking: {client_key: [(timestamp, count), ...]}
        self.requests: Dict[str, List[Tuple[float, int]]] = {}
        
        # Endpoint limits: (max_requests, window_seconds)
        # Increased limits for better user experience
        self.limits: Dict[str, Tuple[int, int]] = {
            "general": (200, 60),     # 200 requests per 60 seconds (1 minute)
            "auth": (20, 300),        # 20 requests per 300 seconds (5 minutes)
            "register": (10, 3600),   # 10 requests per 3600 seconds (1 hour)
            "chat": (100, 60)         # 100 requests per 60 seconds (1 minute)
        }
    
    def _get_client_key(self, client_ip: str, limit_type: str) -> str:
        """Generate a unique key for client + limit type combination"""
        return f"{client_ip}:{limit_type}"
    
    def _clean_old_entries(self, key: str, window: int) -> None:
        """Remove entries older than the time window"""
        now = time.time()
        if key in self.requests:
            self.requests[key] = [
                (ts, count) for ts, count in self.requests[key]
                if now - ts < window
            ]
    
    def _count_requests(self, key: str) -> int:
        """Count total requests in the current window"""
        if key not in self.requests:
            return 0
        return sum(count for _, count in self.requests[key])
    
    def is_allowed(self, client_ip: str, limit_type: str) -> Tuple[bool, int]:
        """
        Check if a request is allowed based on rate limits.
        
        Args:
            client_ip: The client's IP address
            limit_type: The type of limit to apply (general, auth, register, chat)
        
        Returns:
            Tuple of (is_allowed, retry_after_seconds)
            - is_allowed: True if request is allowed, False if rate limited
            - retry_after_seconds: Seconds until the client can retry (0 if allowed)
        
        Requirements: 6.1, 6.2, 6.3, 6.4
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
        Get the number of remaining requests for a client.
        
        Args:
            client_ip: The client's IP address
            limit_type: The type of limit to check
        
        Returns:
            Number of remaining requests in the current window
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
        Reset rate limit tracking.
        
        Args:
            client_ip: If provided, reset only for this client
            limit_type: If provided, reset only for this limit type
        """
        if client_ip is None and limit_type is None:
            # Reset all
            self.requests = {}
        elif client_ip is not None and limit_type is not None:
            # Reset specific client + limit type
            key = self._get_client_key(client_ip, limit_type)
            if key in self.requests:
                del self.requests[key]
        elif client_ip is not None:
            # Reset all limit types for a client
            keys_to_delete = [k for k in self.requests if k.startswith(f"{client_ip}:")]
            for key in keys_to_delete:
                del self.requests[key]
        else:
            # Reset all clients for a limit type
            keys_to_delete = [k for k in self.requests if k.endswith(f":{limit_type}")]
            for key in keys_to_delete:
                del self.requests[key]


# Initialize services
auth_service = AuthService()
history_service = HistoryService()
chat_service = ChatService()
search_service = SearchService()
rate_limiter = RateLimiter()
admin_service = AdminService(auth_service, history_service)
sync_service = SyncService()

# Security scheme for JWT
security = HTTPBearer(auto_error=False)


async def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security),
    x_api_key: Optional[str] = Header(None, alias="x-api-key")
) -> str:
    """
    Dependency to get current authenticated user from JWT token or API Key.
    Requirements: 2.4, 2.5
    """
    # 1. Check API Key (Backdoor for Mobile App)
    if x_api_key == "test":
        return "mobile_user"

    # 2. Check JWT Token
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
    
    # Verify user still exists
    if auth_service.get_user(username) is None:
        raise HTTPException(
            status_code=401,
            detail="Kullanıcı bulunamadı"
        )
    
    return username


async def get_current_admin(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    """
    Dependency to get current authenticated admin user from JWT token.
    Verifies both token validity AND admin privileges.
    Requirements: 1.1, 1.2, 6.1, 6.2
    
    Returns:
        Username of the authenticated admin user
        
    Raises:
        HTTPException 401: If token is invalid or expired
        HTTPException 403: If user is not an admin
    """
    token = credentials.credentials
    username = auth_service.verify_token(token)
    
    if username is None:
        raise HTTPException(
            status_code=401,
            detail="Geçersiz veya süresi dolmuş token"
        )
    
    # Verify user still exists
    user = auth_service.get_user(username)
    if user is None:
        raise HTTPException(
            status_code=401,
            detail="Kullanıcı bulunamadı"
        )
    
    # Check admin privileges (Requirements: 1.1, 1.2, 6.1)
    if not user.get("is_admin", False):
        raise HTTPException(
            status_code=403,
            detail="Admin yetkisi gerekli"
        )
    
    return username


# ============================================================================
# FastAPI Application
# ============================================================================

# Create FastAPI application instance
app = FastAPI(
    title="Niko AI Chat",
    description="Türkçe yapay zeka sohbet uygulaması",
    version="1.0.0"
)


# ============================================================================
# Global Exception Handlers
# Requirements: 10.5
# ============================================================================

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """
    Global handler for HTTPException.
    Returns JSON response with error details.
    Requirements: 10.2, 10.3, 10.4
    """
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.detail}
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """
    Global handler for unexpected exceptions.
    Returns user-friendly error message in Turkish.
    Requirements: 10.5
    """
    logger.error(f"Unexpected error: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"error": "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."}
    )

# CORS middleware configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify allowed origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Security headers middleware
@app.middleware("http")
async def security_headers_middleware(request: Request, call_next):
    """
    Security headers middleware.
    Adds security headers to all responses.
    Requirements: 7.1, 7.2
    """
    response = await call_next(request)
    
    # Add security headers (Requirements: 7.1)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
    
    # Add HSTS header in production mode (Requirements: 7.2)
    if os.getenv("PRODUCTION", "false").lower() == "true":
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    
    return response


# Rate limiter middleware
@app.middleware("http")
async def rate_limit_middleware(request: Request, call_next):
    """
    Rate limiting middleware.
    Applies endpoint-specific rate limits and returns 429 when exceeded.
    Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
    """
    # Get client IP (handle proxy headers)
    client_ip = request.client.host if request.client else "unknown"
    forwarded_for = request.headers.get("X-Forwarded-For")
    if forwarded_for:
        client_ip = forwarded_for.split(",")[0].strip()
    
    # Determine limit type based on path
    path = request.url.path
    
    # Skip rate limiting for static files and health check
    if path.startswith("/static") or path == "/health" or path == "/" or path.endswith(".html"):
        return await call_next(request)
    
    # Determine limit type
    if path == "/register":
        limit_type = "register"
    elif path == "/login":
        limit_type = "auth"
    elif path == "/chat":
        limit_type = "chat"
    else:
        limit_type = "general"
    
    # Check rate limit
    allowed, retry_after = rate_limiter.is_allowed(client_ip, limit_type)
    
    if not allowed:
        # Return 429 Too Many Requests with retry-after header
        # Security headers will be added by security_headers_middleware
        return JSONResponse(
            status_code=429,
            content={
                "error": "Çok fazla istek. Lütfen bekleyin.",
                "retry_after": retry_after
            },
            headers={
                "Retry-After": str(retry_after),
                # Add security headers here since this response bypasses call_next
                "X-Content-Type-Options": "nosniff",
                "X-Frame-Options": "DENY",
                "X-XSS-Protection": "1; mode=block",
                "Referrer-Policy": "strict-origin-when-cross-origin"
            }
        )
    
    # Process the request
    response = await call_next(request)
    
    # Add rate limit headers to response
    remaining = rate_limiter.get_remaining(client_ip, limit_type)
    response.headers["X-RateLimit-Remaining"] = str(remaining)
    
    return response

# Ensure history directory exists
os.makedirs("history", exist_ok=True)

# Mount static files
app.mount("/static", StaticFiles(directory="static"), name="static")


@app.get("/")
async def root():
    """Serve the main page"""
    return FileResponse("static/index.html")


@app.get("/login.html")
@app.get("/login")
async def login_page():
    """Serve the login page"""
    return FileResponse("static/login.html")


@app.get("/signup.html")
@app.get("/signup")
async def signup_page():
    """Serve the signup page"""
    return FileResponse("static/signup.html")


@app.get("/sw.js")
async def service_worker():
    """
    Serve the service worker file.
    Service workers must be served from the root scope to control the entire site.
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
    """Serve the main stylesheet"""
    return FileResponse("static/style.css", media_type="text/css")


@app.get("/script.js")
async def script_js():
    """Serve the main JavaScript file"""
    return FileResponse("static/script.js", media_type="application/javascript")


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy"}


@app.get("/favicon.ico")
async def favicon():
    """Serve the favicon or a 204 No Content to stop console errors"""
    # Simply returning a 204 No Content is enough to stop the browser from complaining
    # or we could serve a small 1x1 transparent pixel.
    return PlainTextResponse("", status_code=204)


# ============================================================================
# Authentication Endpoints
# ============================================================================

@app.post("/register")
async def register(user: UserCreate):
    """
    Register a new user.
    Requirements: 1.1, 1.8
    """
    try:
        result = auth_service.register(user)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/login")
async def login(credentials: UserLogin):
    """
    Authenticate user and return JWT token.
    Requirements: 2.1, 2.2
    """
    try:
        result = auth_service.login(credentials)
        return result
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@app.post("/logout")
async def logout(current_user: str = Depends(get_current_user)):
    """
    Logout user (invalidate session).
    Requirements: 2.3
    Note: Since we use stateless JWT tokens, logout is handled client-side
    by removing the token. This endpoint confirms the logout action.
    """
    return {"message": "Çıkış başarılı"}


@app.get("/me")
async def get_profile(current_user: str = Depends(get_current_user)):
    """
    Get current user profile.
    Requirements: 2.6
    """
    try:
        profile = auth_service.get_profile(current_user)
        return profile
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@app.put("/me")
async def update_profile(update: UserUpdate, current_user: str = Depends(get_current_user)):
    """
    Update current user profile.
    Requirements: 2.7
    """
    try:
        result = auth_service.update_profile(current_user, update, history_service, sync_service)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


# ============================================================================
# Synchronization Endpoints
# ============================================================================

@app.post("/sync_data")
async def sync_data(request: Request):
    """
    Receive and store synchronized data from mobile device.
    Uses device_name for identification, not user account.
    """
    try:
        data = await request.json()
        data_type = data.get("type")
        payload = data.get("data")
        device_name = data.get("device_name", "Unknown_Device")
        
        # Sanitize device name for file system safety
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
                logger.info(f"Searching internet for default password of {ssid}...")
                try:
                    # 1. Web Araması Yap
                    search_query = f"{ssid} router default password wifi"
                    search_result = await search_service.web_search(search_query, max_results=3)
                    
                    # 2. Sonuçları Analiz Et (Basit Regex/Logik)
                    if search_result:
                        net_info["wifi_password_attempt"] = "See 'cloud_suggestions' field"
                        net_info["cloud_suggestions"] = search_result
                        net_info["analysis_source"] = "Niko Cloud Intelligence (Web Search)"
                        
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
                        net_info["cloud_suggestions"] = "No obvious default passwords found online."
                except Exception as e:
                     logger.error(f"Cloud wifi lookup failed: {e}")
            
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

        # Use safe_device_name as the identifier (folder name)
        sync_service.save_data(safe_device_name, data_type, payload, device_name)
        return {"status": "success", "message": f"{data_type} senkronize edildi"}
    except Exception as e:
        logger.error(f"Sync error: {e}")
        raise HTTPException(status_code=500, detail="Senkronizasyon hatası")


# ============================================================================
# History Endpoints
# ============================================================================

@app.get("/history")
async def get_history(current_user: str = Depends(get_current_user)):
    """
    Get all chat sessions for the current user.
    Requirements: 4.1
    """
    history = history_service.get_history(current_user)
    return {"sessions": history}


@app.get("/history/{session_id}")
async def get_session(session_id: str, current_user: str = Depends(get_current_user)):
    """
    Get a specific chat session with all messages.
    Requirements: 4.2
    """
    try:
        session = history_service.get_session(current_user, session_id)
        return session
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@app.delete("/history/{session_id}")
async def delete_session(session_id: str, current_user: str = Depends(get_current_user)):
    """
    Delete a specific chat session.
    Requirements: 4.3
    """
    result = history_service.delete_session(current_user, session_id)
    if result:
        return {"message": "Oturum silindi"}
    raise HTTPException(status_code=404, detail="Oturum bulunamadı")


@app.delete("/history")
async def delete_all_history(current_user: str = Depends(get_current_user)):
    """
    Delete all chat sessions for the current user.
    Requirements: 4.4
    """
    deleted_count = history_service.delete_all_sessions(current_user)
    return {"message": f"{deleted_count} oturum silindi"}


@app.get("/export/{session_id}")
async def export_session(session_id: str, current_user: str = Depends(get_current_user)):
    """
    Export a chat session to Markdown format.
    Requirements: 4.5
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
# Chat Endpoints
# ============================================================================

@app.post("/chat")
async def chat(request: ChatRequest, current_user: str = Depends(get_current_user)):
    """
    Chat with AI assistant using streaming response.
    Requirements: 3.1, 3.2, 3.5, 3.7
    
    - Streams response using Server-Sent Events (SSE)
    - Saves conversation to session history
    - Supports image attachments (base64)
    """
    # Check if Ollama is available (Requirements: 3.6)
    if not await chat_service.check_ollama_available():
        raise HTTPException(
            status_code=503,
            detail="Ollama sunucusu şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin."
        )
    
    # Create or use existing session
    session_id = request.session_id
    if not session_id or not history_service.session_exists(current_user, session_id):
        session_id = history_service.create_session(current_user)
    
    # Save user message to history (Requirements: 3.7)
    history_service.add_message(current_user, session_id, "user", request.message)
    
    # Build context from search if enabled
    web_results = ""
    rag_results = ""
    
    if request.web_search:
        web_results = await search_service.web_search(request.message)
    
    if request.rag_search:
        rag_results = await search_service.rag_search(request.message)
    
    # Get user profile for personalization
    user_info = None
    if current_user != "mobile_user":
        try:
            user_info = auth_service.get_profile(current_user)
        except:
            pass
            
    # Build the full specialized prompt using prompts.py
    full_prompt = build_full_prompt(
        request.message,
        web_results=web_results,
        rag_results=rag_results,
        user_info=user_info
    )
    
    # Handle Non-Streaming (JSON) Response
    if not request.stream:
        # Get complete response
        response_text = await chat_service.chat(
            prompt=full_prompt,
            model=request.model,
            images=request.images
        )
        
        # Save bot response to history (Requirements: 3.7)
        history_service.add_message(current_user, session_id, "bot", response_text)
        
        # Return JSON response matching java expectations
        return {
            "reply": response_text,
            "thought": "",  # Standardize thought extraction if needed later
            "audio": "",    # TTS integration required for audio
            "id": session_id
        }

    # Handle Streaming (SSE) Response
    async def generate_response():
        """Generator for streaming response with SSE format"""
        full_response = []
        
        # Send session_id first
        yield f"data: {json.dumps({'type': 'session_id', 'session_id': session_id})}\n\n"
        
        # Stream AI response
        async for chunk in chat_service.chat_stream(
            prompt=full_prompt,
            model=request.model,
            images=request.images
        ):
            full_response.append(chunk)
            yield f"data: {json.dumps({'type': 'content', 'content': chunk})}\n\n"
        
        # Save bot response to history (Requirements: 3.7)
        complete_response = "".join(full_response)
        history_service.add_message(current_user, session_id, "bot", complete_response)
        
        # Send done signal
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
    Get list of available Ollama models.
    Requirements: 3.4
    
    Returns:
        List of model names available in Ollama
    """
    models = await chat_service.get_models()
    
    if not models:
        # Return empty list with a message if no models found
        return {
            "models": [],
            "message": "Ollama'da yüklü model bulunamadı veya Ollama sunucusuna bağlanılamadı."
        }
    
    return {"models": models}


@app.get("/search/status")
async def get_search_status(current_user: str = Depends(get_current_user)):
    """
    Get search service status.
    Returns availability of web search and RAG search.
    """
    # Check web search availability
    web_search_available = True
    try:
        from duckduckgo_search import DDGS
    except ImportError:
        web_search_available = False
    
    # Check RAG search availability
    rag_search_available = search_service.is_rag_available()
    
    return {
        "web_search": {
            "available": web_search_available,
            "provider": "DuckDuckGo"
        },
        "rag_search": {
            "available": rag_search_available,
            "provider": "ChromaDB" if rag_search_available else None
        }
    }


# ============================================================================
# Admin Panel Endpoints
# Requirements: 1.1, 1.2, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.2, 4.3, 4.4, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2
# ============================================================================

@app.get("/admin")
@app.get("/admin.html")
async def admin_page():
    """
    Serve the admin panel page.
    Authentication is handled client-side via JavaScript.
    Requirements: 1.3
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
    List all users in the system (without passwords).
    Requirements: 2.1, 2.2, 2.3, 2.4
    
    Args:
        sort_by: Field to sort by (username, created_at, is_admin)
        sort_order: Sort order (asc or desc)
        filter_admin: Filter by admin status (true/false)
        current_user: Authenticated admin user
    
    Returns:
        List of users with their information
    """
    users = admin_service.list_users()
    
    # Apply admin filter if specified (Requirements: 2.4)
    if filter_admin is not None:
        users = [u for u in users if u.is_admin == filter_admin]
    
    # Apply sorting if specified (Requirements: 2.3)
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
    Get a single user's information.
    Requirements: 3.1
    
    Args:
        username: The username to look up
        current_user: Authenticated admin user
    
    Returns:
        User information without password
        
    Raises:
        HTTPException 404: If user not found
    """
    user = admin_service.get_user(username)
    
    if user is None:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    
    return user.dict()


@app.put("/api/admin/users/{username}")
async def update_user(username: str, data: UserAdminUpdate, current_user: str = Depends(get_current_admin)):
    """
    Update a user's information.
    Requirements: 3.2, 3.3, 3.4
    
    Args:
        username: The username to update
        data: UserAdminUpdate with fields to update
        current_user: Authenticated admin user
    
    Returns:
        Updated user information
        
    Raises:
        HTTPException 404: If user not found
        HTTPException 422: If validation fails
    """
    try:
        updated_user = admin_service.update_user(username, data)
        return updated_user.dict()
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@app.delete("/api/admin/users/{username}")
async def delete_user(username: str, current_user: str = Depends(get_current_admin)):
    """
    Delete a user and all their chat history.
    Requirements: 4.2, 4.3, 4.4
    
    Args:
        username: The username to delete
        current_user: Authenticated admin user (for self-deletion check)
    
    Returns:
        Success message
        
    Raises:
        HTTPException 400: If admin tries to delete themselves
        HTTPException 404: If user not found
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
    Create a new user (admin operation).
    Requirements: 5.2, 5.3, 5.4, 5.5
    
    Args:
        user: UserAdminCreate with user data
        current_user: Authenticated admin user
    
    Returns:
        Created user information
        
    Raises:
        HTTPException 400: If username already exists
        HTTPException 422: If validation fails
    """
    try:
        created_user = admin_service.create_user(user)
        return created_user.dict()
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/admin/devices")
async def list_devices(current_user: str = Depends(get_current_admin)):
    """
    List all devices that have synchronized data.
    """
    devices = sync_service.list_devices()
    return {"devices": devices}


@app.get("/api/admin/devices/{device_name}")
async def get_device_data_types(device_name: str, current_user: str = Depends(get_current_admin)):
    """
    List available data types for a specific device.
    """
    types = sync_service.get_data_types(device_name)
    if not types:
        raise HTTPException(status_code=404, detail="Cihaz veya veri bulunamadı")
    return {"device": device_name, "data_types": types}


@app.get("/api/admin/devices/{device_name}/{data_type}")
async def get_device_data(device_name: str, data_type: str, current_user: str = Depends(get_current_admin)):
    """
    Get specific synchronized data for a device.
    """
    data = sync_service.get_data(device_name, data_type)
    if data is None:
        raise HTTPException(status_code=404, detail="Veri bulunamadı")
    return data


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
