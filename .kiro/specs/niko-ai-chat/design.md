# Design Document: Niko AI Chat Application

## Overview

Niko AI, Türkçe bir yapay zeka sohbet uygulamasıdır. Sistem, FastAPI tabanlı bir backend, vanilla HTML/CSS/JavaScript frontend ve Ollama LLM entegrasyonu kullanır. Veriler JSON dosyalarında saklanır ve uygulama web araması ile RAG özellikleri sunar.

### Key Design Decisions

1. **JSON File Storage**: Basitlik ve taşınabilirlik için veritabanı yerine JSON dosyaları kullanılır
2. **Custom JWT Implementation**: python-jose kütüphanesi ile JWT token yönetimi
3. **Streaming Responses**: Server-Sent Events (SSE) ile gerçek zamanlı AI yanıtları
4. **Modular Architecture**: Servisler ayrı modüller olarak organize edilir
5. **Rate Limiting**: In-memory dictionary ile endpoint bazlı istek sınırlama

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (Static)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │login.html│  │signup.html│  │index.html│  │    script.js     │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTP/SSE
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FastAPI Backend (main.py)                     │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐   │
│  │  Auth Routes   │  │  Chat Routes   │  │  History Routes  │   │
│  │ /register      │  │ /chat          │  │ /history         │   │
│  │ /login         │  │ /models        │  │ /export          │   │
│  │ /logout        │  │                │  │                  │   │
│  │ /me            │  │                │  │                  │   │
│  └────────────────┘  └────────────────┘  └──────────────────┘   │
│                              │                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                      Middleware Layer                       │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │ │
│  │  │ Rate Limiter │  │Security Hdrs │  │      CORS        │  │ │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                      Service Layer                          │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │ │
│  │  │ Auth Service │  │ Chat Service │  │ History Service  │  │ │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │ │
│  │  ┌──────────────┐  ┌──────────────┐                        │ │
│  │  │Search Service│  │ User Cache   │                        │ │
│  │  └──────────────┘  └──────────────┘                        │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────────────┐
│  users.json  │    │  history/*.json│   │    External APIs     │
│              │    │              │    │  ┌────────────────┐  │
│              │    │              │    │  │  Ollama API    │  │
│              │    │              │    │  │  DuckDuckGo    │  │
│              │    │              │    │  │  ChromaDB      │  │
│              │    │              │    │  └────────────────┘  │
└──────────────┘    └──────────────┘    └──────────────────────┘
```

## Components and Interfaces

### 1. FastAPI Application (main.py)

```python
from fastapi import FastAPI, HTTPException, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import StreamingResponse, JSONResponse
from pydantic import BaseModel, validator
from passlib.context import CryptContext
from jose import jwt, JWTError
from datetime import datetime, timedelta
import httpx
import json
import uuid
import os

app = FastAPI(title="Niko AI Chat")
```

### 2. Pydantic Models

```python
class UserCreate(BaseModel):
    username: str
    password: str
    email: Optional[str] = None
    full_name: Optional[str] = None
    
    @validator('username')
    def validate_username(cls, v):
        if len(v) < 3 or len(v) > 30:
            raise ValueError('Username must be 3-30 characters')
        if not v[0].isalpha():
            raise ValueError('Username must start with a letter')
        if not v.replace('_', '').isalnum():
            raise ValueError('Username can only contain letters, numbers, underscores')
        return v
    
    @validator('password')
    def validate_password(cls, v):
        if len(v) < 8:
            raise ValueError('Password must be at least 8 characters')
        if not any(c.isupper() for c in v):
            raise ValueError('Password must contain uppercase letter')
        if not any(c.islower() for c in v):
            raise ValueError('Password must contain lowercase letter')
        if not any(c.isdigit() for c in v):
            raise ValueError('Password must contain a digit')
        return v

class UserLogin(BaseModel):
    username: str
    password: str

class UserUpdate(BaseModel):
    email: Optional[str] = None
    full_name: Optional[str] = None
    current_password: Optional[str] = None
    new_password: Optional[str] = None

class ChatRequest(BaseModel):
    message: str
    enable_audio: bool = True
    web_search: bool = False
    rag_search: bool = False
    session_id: Optional[str] = None
    model: Optional[str] = None
    mode: Optional[str] = "normal"
    images: Optional[List[str]] = None  # base64 encoded
```

### 3. Authentication Service

```python
class AuthService:
    def __init__(self):
        self.pwd_context = CryptContext(schemes=["bcrypt"])
        self.secret_key = os.getenv("JWT_SECRET", "your-secret-key")
        self.algorithm = "HS256"
        self.token_expire_hours = 24
        self.users_file = "users.json"
    
    def hash_password(self, password: str) -> str:
        return self.pwd_context.hash(password)
    
    def verify_password(self, plain: str, hashed: str) -> bool:
        return self.pwd_context.verify(plain, hashed)
    
    def create_token(self, username: str) -> str:
        expire = datetime.utcnow() + timedelta(hours=self.token_expire_hours)
        payload = {"sub": username, "exp": expire}
        return jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
    
    def verify_token(self, token: str) -> Optional[str]:
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            return payload.get("sub")
        except JWTError:
            return None
    
    def load_users(self) -> dict:
        if os.path.exists(self.users_file):
            with open(self.users_file, 'r') as f:
                return json.load(f)
        return {}
    
    def save_users(self, users: dict):
        with open(self.users_file, 'w') as f:
            json.dump(users, f, indent=2)
    
    def register(self, user: UserCreate) -> dict:
        users = self.load_users()
        if user.username in users:
            raise ValueError("Username already exists")
        
        users[user.username] = {
            "password": self.hash_password(user.password),
            "email": user.email,
            "full_name": user.full_name,
            "created_at": datetime.utcnow().isoformat()
        }
        self.save_users(users)
        return {"message": "Kayıt başarılı"}
    
    def login(self, credentials: UserLogin) -> dict:
        users = self.load_users()
        user = users.get(credentials.username)
        if not user or not self.verify_password(credentials.password, user["password"]):
            raise ValueError("Geçersiz kullanıcı adı veya şifre")
        
        token = self.create_token(credentials.username)
        return {"access_token": token, "token_type": "bearer"}
```

### 4. Chat Service

```python
class ChatService:
    def __init__(self):
        self.ollama_url = os.getenv("OLLAMA_URL", "http://localhost:11434")
        self.default_model = "llama2"
    
    async def get_models(self) -> List[str]:
        async with httpx.AsyncClient() as client:
            response = await client.get(f"{self.ollama_url}/api/tags")
            if response.status_code == 200:
                data = response.json()
                return [model["name"] for model in data.get("models", [])]
            return []
    
    async def chat(self, request: ChatRequest, username: str) -> AsyncGenerator:
        context = ""
        
        # Web search if enabled
        if request.web_search:
            search_results = await self.search_service.web_search(request.message)
            context += f"\nWeb Arama Sonuçları:\n{search_results}\n"
        
        # RAG search if enabled
        if request.rag_search:
            rag_results = await self.search_service.rag_search(request.message)
            context += f"\nDoküman Sonuçları:\n{rag_results}\n"
        
        # Prepare Ollama request
        payload = {
            "model": request.model or self.default_model,
            "prompt": f"{context}\n\nKullanıcı: {request.message}",
            "stream": True
        }
        
        if request.images:
            payload["images"] = request.images
        
        # Stream response
        async with httpx.AsyncClient(timeout=120.0) as client:
            async with client.stream(
                "POST",
                f"{self.ollama_url}/api/generate",
                json=payload
            ) as response:
                async for line in response.aiter_lines():
                    if line:
                        data = json.loads(line)
                        yield data.get("response", "")
```

### 5. History Service

```python
class HistoryService:
    def __init__(self):
        self.history_dir = "history"
        os.makedirs(self.history_dir, exist_ok=True)
    
    def get_session_path(self, username: str, session_id: str) -> str:
        return os.path.join(self.history_dir, f"{username}_{session_id}.json")
    
    def create_session(self, username: str) -> str:
        session_id = str(uuid.uuid4())
        session_data = {
            "id": session_id,
            "title": "Yeni Sohbet",
            "timestamp": datetime.utcnow().isoformat(),
            "messages": []
        }
        
        path = self.get_session_path(username, session_id)
        with open(path, 'w') as f:
            json.dump(session_data, f, indent=2)
        
        return session_id
    
    def add_message(self, username: str, session_id: str, role: str, content: str, thought: str = None):
        path = self.get_session_path(username, session_id)
        
        with open(path, 'r') as f:
            session = json.load(f)
        
        message = {"role": role, "content": content}
        if thought:
            message["thought"] = thought
        
        session["messages"].append(message)
        
        # Update title from first user message
        if role == "user" and len(session["messages"]) == 1:
            session["title"] = content[:50] + ("..." if len(content) > 50 else "")
        
        with open(path, 'w') as f:
            json.dump(session, f, indent=2)
    
    def get_history(self, username: str) -> List[dict]:
        sessions = []
        for filename in os.listdir(self.history_dir):
            if filename.startswith(f"{username}_") and filename.endswith(".json"):
                path = os.path.join(self.history_dir, filename)
                with open(path, 'r') as f:
                    session = json.load(f)
                    sessions.append({
                        "id": session["id"],
                        "title": session["title"],
                        "timestamp": session["timestamp"]
                    })
        
        return sorted(sessions, key=lambda x: x["timestamp"], reverse=True)
    
    def delete_session(self, username: str, session_id: str) -> bool:
        path = self.get_session_path(username, session_id)
        if os.path.exists(path):
            os.remove(path)
            return True
        return False
    
    def export_markdown(self, username: str, session_id: str) -> str:
        path = self.get_session_path(username, session_id)
        
        with open(path, 'r') as f:
            session = json.load(f)
        
        md = f"# {session['title']}\n\n"
        md += f"*Tarih: {session['timestamp']}*\n\n---\n\n"
        
        for msg in session["messages"]:
            role = "👤 Kullanıcı" if msg["role"] == "user" else "🤖 Niko"
            md += f"### {role}\n\n{msg['content']}\n\n"
        
        return md
```

### 6. Rate Limiter

```python
class RateLimiter:
    def __init__(self):
        self.requests = {}  # {key: [(timestamp, count)]}
        self.limits = {
            "general": (60, 60),      # 60 requests per 60 seconds
            "auth": (5, 300),         # 5 requests per 5 minutes
            "register": (3, 3600),    # 3 requests per hour
            "chat": (30, 60)          # 30 requests per minute
        }
    
    def is_allowed(self, key: str, limit_type: str) -> bool:
        max_requests, window = self.limits.get(limit_type, (60, 60))
        now = time.time()
        
        if key not in self.requests:
            self.requests[key] = []
        
        # Clean old entries
        self.requests[key] = [
            (ts, count) for ts, count in self.requests[key]
            if now - ts < window
        ]
        
        # Count requests in window
        total = sum(count for _, count in self.requests[key])
        
        if total >= max_requests:
            return False
        
        self.requests[key].append((now, 1))
        return True
```

### 7. Search Service

```python
class SearchService:
    def __init__(self):
        self.ddg = DDGS()
        self.chroma_client = None  # Initialize ChromaDB if available
    
    async def web_search(self, query: str, max_results: int = 5) -> str:
        try:
            results = self.ddg.text(query, max_results=max_results)
            formatted = []
            for r in results:
                formatted.append(f"- {r['title']}: {r['body']}")
            return "\n".join(formatted)
        except Exception as e:
            logger.error(f"Web search error: {e}")
            return ""
    
    async def rag_search(self, query: str) -> str:
        if not self.chroma_client:
            return "RAG veritabanı yapılandırılmamış"
        
        try:
            results = self.chroma_client.query(query_texts=[query], n_results=3)
            return "\n".join(results["documents"][0]) if results["documents"] else ""
        except Exception as e:
            logger.error(f"RAG search error: {e}")
            return ""
```

### 8. Security Middleware

```python
@app.middleware("http")
async def security_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
    
    if os.getenv("PRODUCTION", "false").lower() == "true":
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    
    return response
```

## Data Models

### User Data (users.json)

```json
{
  "username": {
    "password": "$2b$12$...",  // bcrypt hash
    "email": "user@example.com",
    "full_name": "User Name",
    "created_at": "2024-01-01T00:00:00"
  }
}
```

### Chat Session (history/{username}_{session_id}.json)

```json
{
  "id": "uuid-string",
  "title": "Sohbet başlığı",
  "timestamp": "2024-01-01T00:00:00",
  "messages": [
    {
      "role": "user",
      "content": "Merhaba!"
    },
    {
      "role": "bot",
      "content": "Merhaba! Size nasıl yardımcı olabilirim?",
      "thought": "Kullanıcı selamlama yapıyor, nazik bir karşılık vermeliyim."
    }
  ]
}
```

### JWT Token Payload

```json
{
  "sub": "username",
  "exp": 1704153600  // Unix timestamp
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Username Validation

*For any* string submitted as a username, the Auth_Service SHALL accept it if and only if:
- Length is between 3 and 30 characters
- First character is a letter
- All characters are letters, numbers, or underscores

**Validates: Requirements 1.2, 1.3, 1.4**

### Property 2: Password Validation

*For any* string submitted as a password, the Auth_Service SHALL accept it if and only if:
- Length is at least 8 characters
- Contains at least one uppercase letter
- Contains at least one lowercase letter
- Contains at least one digit

**Validates: Requirements 1.5, 1.6**

### Property 3: Password Hashing Round-Trip

*For any* valid password, hashing it with bcrypt and then verifying the original password against the hash SHALL return true, and verifying any different password SHALL return false.

**Validates: Requirements 1.9, 7.5**

### Property 4: Registration Uniqueness

*For any* valid user registration, if the username already exists in the system, the Auth_Service SHALL reject the registration. If the username is new, the registration SHALL succeed and the user SHALL be retrievable.

**Validates: Requirements 1.1, 1.8**

### Property 5: JWT Authentication

*For any* JWT token:
- If the token is valid and not expired, accessing protected endpoints SHALL succeed
- If the token is invalid, malformed, or expired, accessing protected endpoints SHALL return 401 status

**Validates: Requirements 2.1, 2.4, 2.5**

### Property 6: Login Credential Verification

*For any* login attempt, the Auth_Service SHALL return a valid JWT if and only if the username exists and the password matches the stored hash.

**Validates: Requirements 2.1, 2.2**

### Property 7: Profile Data Consistency

*For any* registered user, requesting their profile via GET /me SHALL return the same email and full_name that were provided during registration or the most recent profile update.

**Validates: Requirements 2.6, 2.7**

### Property 8: Rate Limiting Enforcement

*For any* client making requests:
- After exceeding the configured limit for an endpoint type, subsequent requests SHALL receive 429 status
- The 429 response SHALL include retry-after information

**Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**

### Property 9: History CRUD Operations

*For any* user with chat sessions:
- Creating a session SHALL generate a unique ID and create a JSON file
- Listing history SHALL return all sessions for that user
- Loading a session SHALL return all messages in that session
- Deleting a session SHALL remove the JSON file
- Clearing all history SHALL remove all session files for that user

**Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.6**

### Property 10: History Message Format

*For any* message saved to chat history, the JSON structure SHALL contain:
- role (user or bot)
- content (message text)
- thought (optional, for bot messages)

**Validates: Requirements 4.7, 9.5**

### Property 11: Markdown Export Format

*For any* chat session exported to Markdown, the output SHALL contain:
- Session title as heading
- Timestamp
- All messages with role indicators (👤 Kullanıcı / 🤖 Niko)

**Validates: Requirements 4.5**

### Property 12: Security Headers

*For any* HTTP response from the Niko_System, the following headers SHALL be present:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Referrer-Policy: strict-origin-when-cross-origin

**Validates: Requirements 7.1**

### Property 13: API Response Codes

*For any* API request:
- Successful requests SHALL return 200 or 201 with JSON body
- Validation errors SHALL return 400 with error details
- Authentication errors SHALL return 401
- Rate limit errors SHALL return 429

**Validates: Requirements 10.1, 10.2, 10.3, 10.4**

### Property 14: Data Persistence Format

*For any* data stored by the system:
- User data SHALL be saved to users.json with hashed passwords
- Chat sessions SHALL be saved as separate JSON files in history/ directory
- Session files SHALL follow the format: {id, title, timestamp, messages[]}

**Validates: Requirements 9.1, 9.2, 9.5**

### Property 15: Image Attachment Handling

*For any* chat request with images, the images SHALL be included in the Ollama API request payload as base64-encoded strings.

**Validates: Requirements 3.5**

### Property 16: Draft Auto-Save

*For any* text typed in the message input, the Frontend SHALL save it to localStorage, and reloading the page SHALL restore the draft.

**Validates: Requirements 8.8**

## Error Handling

### Backend Error Handling

```python
@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.detail}
    )

@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unexpected error: {exc}")
    return JSONResponse(
        status_code=500,
        content={"error": "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."}
    )
```

### Error Response Format

| Status Code | Scenario | Response Body |
|-------------|----------|---------------|
| 400 | Validation Error | `{"error": "Geçersiz kullanıcı adı formatı"}` |
| 401 | Authentication Error | `{"error": "Geçersiz veya süresi dolmuş token"}` |
| 404 | Not Found | `{"error": "Kaynak bulunamadı"}` |
| 429 | Rate Limited | `{"error": "Çok fazla istek. Lütfen bekleyin.", "retry_after": 60}` |
| 500 | Server Error | `{"error": "Beklenmeyen bir hata oluştu"}` |

### Frontend Error Handling

```javascript
async function handleApiError(response) {
    if (response.status === 401) {
        // Redirect to login
        window.location.href = '/login.html';
        return;
    }
    
    const data = await response.json();
    showToast(data.error || 'Bir hata oluştu', 'error');
}
```

## Testing Strategy

### Unit Tests

Unit tests will cover:
- Validation functions (username, password, email)
- Password hashing and verification
- JWT token creation and verification
- History file operations
- Rate limiter logic

### Property-Based Tests

Property-based tests will use **Hypothesis** library for Python to verify:
- Username validation accepts/rejects correct inputs
- Password validation enforces all requirements
- Password hashing is consistent
- JWT tokens are properly validated
- Rate limiting enforces configured limits
- History operations maintain data integrity

**Configuration:**
- Minimum 100 iterations per property test
- Use `@given` decorator with appropriate strategies
- Tag format: `# Feature: niko-ai-chat, Property N: description`

### Integration Tests

Integration tests will cover:
- Full registration → login → chat flow
- History creation → update → delete flow
- Rate limiting across multiple requests

### E2E Tests (browser_test.py)

Selenium-based tests will verify:
- Signup page functionality
- Login flow
- Main page elements
- Message sending
- Sidebar history
- Profile functionality
- Logout functionality
- Console error checking

**Configuration:**
- WebDriverWait with 10-second timeout
- Chrome WebDriver
- Base URL: http://localhost:8001
