# Requirements Document

## Introduction

Niko AI, Türkçe bir yapay zeka sohbet uygulamasıdır. Kullanıcılar yerel Ollama LLM modelleri ile sohbet edebilir, web araması yapabilir, RAG (Retrieval-Augmented Generation) özelliğini kullanabilir ve sohbet geçmişlerini yönetebilir. Uygulama FastAPI backend, vanilla HTML/CSS/JavaScript frontend ve JSON tabanlı veri depolama kullanır.

## Glossary

- **Niko_System**: Ana uygulama sistemi
- **Auth_Service**: Kimlik doğrulama ve kullanıcı yönetimi servisi
- **Chat_Service**: AI sohbet işlemlerini yöneten servis
- **History_Service**: Sohbet geçmişi yönetim servisi
- **Search_Service**: Web ve RAG arama servisi
- **Rate_Limiter**: İstek sınırlama servisi
- **Ollama_Client**: Ollama API ile iletişim kuran istemci
- **User**: Sisteme kayıtlı kullanıcı
- **Session**: Bir sohbet oturumu
- **JWT**: JSON Web Token kimlik doğrulama tokenı
- **RAG**: Retrieval-Augmented Generation - vektör tabanlı bilgi erişimi

## Requirements

### Requirement 1: User Registration

**User Story:** As a new user, I want to register an account, so that I can access the chat application.

#### Acceptance Criteria

1. WHEN a user submits valid registration data (username, password, email, full_name) THEN the Auth_Service SHALL create a new user account and return success
2. WHEN a user submits a username shorter than 3 characters or longer than 30 characters THEN the Auth_Service SHALL reject the registration with a validation error
3. WHEN a user submits a username that does not start with a letter THEN the Auth_Service SHALL reject the registration with a validation error
4. WHEN a user submits a username containing characters other than letters, numbers, or underscores THEN the Auth_Service SHALL reject the registration with a validation error
5. WHEN a user submits a password shorter than 8 characters THEN the Auth_Service SHALL reject the registration with a validation error
6. WHEN a user submits a password without at least one uppercase letter, one lowercase letter, and one digit THEN the Auth_Service SHALL reject the registration with a validation error
7. WHEN a user submits an invalid email format THEN the Auth_Service SHALL reject the registration with a validation error
8. WHEN a user submits a username that already exists THEN the Auth_Service SHALL reject the registration with a duplicate error
9. WHEN storing user credentials THEN the Auth_Service SHALL hash the password using bcrypt before saving to users.json

### Requirement 2: User Authentication

**User Story:** As a registered user, I want to log in and out securely, so that I can access my personal chat history.

#### Acceptance Criteria

1. WHEN a user submits valid login credentials THEN the Auth_Service SHALL generate a JWT token with 24-hour expiration and return it
2. WHEN a user submits invalid credentials THEN the Auth_Service SHALL reject the login with an authentication error
3. WHEN a user requests logout THEN the Auth_Service SHALL invalidate the current session
4. WHEN a user accesses a protected endpoint with a valid JWT THEN the Niko_System SHALL allow the request
5. WHEN a user accesses a protected endpoint with an invalid or expired JWT THEN the Niko_System SHALL reject the request with 401 status
6. WHEN a user requests their profile via GET /me THEN the Auth_Service SHALL return the current user information
7. WHEN a user updates their profile via PUT /me THEN the Auth_Service SHALL validate and save the changes

### Requirement 3: AI Chat Functionality

**User Story:** As a user, I want to chat with an AI assistant, so that I can get helpful responses to my questions.

#### Acceptance Criteria

1. WHEN a user sends a chat message THEN the Chat_Service SHALL forward it to Ollama API and return the AI response
2. WHEN a user sends a chat message with streaming enabled THEN the Chat_Service SHALL stream the response in real-time
3. WHEN a user selects a specific AI model THEN the Chat_Service SHALL use that model for the conversation
4. WHEN a user requests available models via GET /models THEN the Ollama_Client SHALL return the list of installed Ollama models
5. WHEN a user uploads images with a message THEN the Chat_Service SHALL include the base64-encoded images in the Ollama request
6. WHEN Ollama API is unavailable THEN the Chat_Service SHALL return a user-friendly error message
7. WHEN a user sends a message THEN the Chat_Service SHALL save the conversation to the session history

### Requirement 4: Chat History Management

**User Story:** As a user, I want to manage my chat history, so that I can review past conversations and keep my data organized.

#### Acceptance Criteria

1. WHEN a user requests chat history via GET /history THEN the History_Service SHALL return a list of all chat sessions for that user
2. WHEN a user loads a specific session THEN the History_Service SHALL return all messages in that session
3. WHEN a user deletes a session via DELETE /history/{session_id} THEN the History_Service SHALL remove that session's JSON file
4. WHEN a user clears all history via DELETE /history THEN the History_Service SHALL remove all session files for that user
5. WHEN a user exports a session via GET /export/{session_id} THEN the History_Service SHALL return the conversation in Markdown format
6. WHEN a new chat session starts THEN the History_Service SHALL generate a unique session ID and create a new JSON file
7. WHEN saving chat history THEN the History_Service SHALL store messages with role, content, thought (optional), and timestamp

### Requirement 5: Search Features

**User Story:** As a user, I want to search the web and my documents, so that I can get more informed AI responses.

#### Acceptance Criteria

1. WHEN a user enables web search in a chat request THEN the Search_Service SHALL query DuckDuckGo and include results in the AI context
2. WHEN a user enables RAG search in a chat request THEN the Search_Service SHALL query ChromaDB vector database and include relevant documents
3. WHEN a user enables both web and RAG search THEN the Search_Service SHALL perform hybrid search combining both sources
4. WHEN web search fails THEN the Search_Service SHALL log the error and continue with the chat without search results
5. WHEN RAG search returns no results THEN the Search_Service SHALL inform the AI that no relevant documents were found

### Requirement 6: Rate Limiting

**User Story:** As a system administrator, I want to limit API requests, so that the system remains stable and secure.

#### Acceptance Criteria

1. WHEN a client exceeds 60 requests per minute on general endpoints THEN the Rate_Limiter SHALL reject requests with 429 status
2. WHEN a client exceeds 5 authentication attempts in 5 minutes THEN the Rate_Limiter SHALL reject requests with 429 status
3. WHEN a client exceeds 3 registration attempts per hour THEN the Rate_Limiter SHALL reject requests with 429 status
4. WHEN a client exceeds 30 chat requests per minute THEN the Rate_Limiter SHALL reject requests with 429 status
5. WHEN rate limit is exceeded THEN the Rate_Limiter SHALL return a clear error message with retry-after information

### Requirement 7: Security

**User Story:** As a user, I want my data to be secure, so that my conversations and credentials are protected.

#### Acceptance Criteria

1. WHEN any response is sent THEN the Niko_System SHALL include security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, Referrer-Policy)
2. WHEN in production mode THEN the Niko_System SHALL include Strict-Transport-Security header
3. WHEN processing user input THEN the Niko_System SHALL validate and sanitize all inputs
4. WHEN handling cross-origin requests THEN the Niko_System SHALL apply CORS middleware with appropriate restrictions
5. WHEN storing sensitive data THEN the Niko_System SHALL never store plain-text passwords

### Requirement 8: Frontend User Interface

**User Story:** As a user, I want a modern and responsive interface, so that I can comfortably use the application on any device.

#### Acceptance Criteria

1. WHEN the user loads the application THEN the Frontend SHALL display a dark-themed responsive interface
2. WHEN the user opens the sidebar THEN the Frontend SHALL display the chat history list
3. WHEN the user sends a message THEN the Frontend SHALL show a loading indicator until the response arrives
4. WHEN the AI responds THEN the Frontend SHALL render the message with proper formatting including code syntax highlighting
5. WHEN the user drags and drops an image THEN the Frontend SHALL preview and attach it to the message
6. WHEN the user clicks copy on a message THEN the Frontend SHALL copy the content to clipboard and show a toast notification
7. WHEN connection status changes THEN the Frontend SHALL update the connection indicator
8. WHEN the user types a message THEN the Frontend SHALL auto-save as draft to localStorage
9. WHEN displaying notifications THEN the Frontend SHALL use toast messages with appropriate styling
10. WHEN confirming destructive actions THEN the Frontend SHALL show a modal dialog

### Requirement 9: Data Persistence

**User Story:** As a user, I want my data to persist, so that I can access my account and history across sessions.

#### Acceptance Criteria

1. WHEN storing user data THEN the Niko_System SHALL save to users.json file
2. WHEN storing chat history THEN the Niko_System SHALL save each session as a separate JSON file in history/ directory
3. WHEN reading user data THEN the Niko_System SHALL cache results with 300-second TTL
4. WHEN the history directory does not exist THEN the Niko_System SHALL create it automatically
5. WHEN serializing chat history THEN the History_Service SHALL use the format: {id, title, timestamp, messages[{role, content, thought}]}

### Requirement 10: API Response Format

**User Story:** As a frontend developer, I want consistent API responses, so that I can reliably handle all scenarios.

#### Acceptance Criteria

1. WHEN an API request succeeds THEN the Niko_System SHALL return appropriate status code (200, 201) with JSON response
2. WHEN an API request fails due to validation THEN the Niko_System SHALL return 400 status with error details
3. WHEN an API request fails due to authentication THEN the Niko_System SHALL return 401 status
4. WHEN an API request fails due to rate limiting THEN the Niko_System SHALL return 429 status
5. WHEN an unexpected error occurs THEN the Niko_System SHALL return 500 status with a user-friendly message in Turkish
