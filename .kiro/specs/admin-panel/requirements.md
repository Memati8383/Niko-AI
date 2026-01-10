# Requirements Document

## Introduction

Bu doküman, Niko AI Chat uygulaması için bir admin paneli özelliğinin gereksinimlerini tanımlar. Admin paneli, yetkili kullanıcıların sistemdeki tüm kullanıcıları görüntülemesine, düzenlemesine ve yönetmesine olanak tanır.

## Glossary

- **Admin_Panel**: Yetkili kullanıcıların sistemi yönetmesine olanak tanıyan web arayüzü
- **Admin_User**: Admin yetkisine sahip kullanıcı (is_admin: true)
- **Regular_User**: Normal kullanıcı (is_admin: false veya tanımsız)
- **User_Management_System**: Kullanıcı CRUD işlemlerini gerçekleştiren backend servisi
- **Authentication_System**: Kullanıcı kimlik doğrulama ve yetkilendirme sistemi

## Requirements

### Requirement 1: Admin Kimlik Doğrulama

**User Story:** As an admin, I want to access the admin panel with my credentials, so that I can manage users securely.

#### Acceptance Criteria

1. WHEN a user navigates to /admin, THE Authentication_System SHALL check if the user has admin privileges
2. IF a non-admin user attempts to access /admin, THEN THE Authentication_System SHALL redirect them to the main page with an error message
3. WHEN an admin user logs in, THE Admin_Panel SHALL display the user management interface
4. THE Authentication_System SHALL store admin status in the user record as "is_admin" boolean field

### Requirement 2: Kullanıcı Listeleme

**User Story:** As an admin, I want to view all registered users, so that I can monitor and manage the user base.

#### Acceptance Criteria

1. WHEN an admin accesses the admin panel, THE User_Management_System SHALL display a list of all users
2. THE Admin_Panel SHALL display username, email, full_name, created_at, and is_admin status for each user
3. THE Admin_Panel SHALL support sorting users by username, created_at, or is_admin status
4. THE Admin_Panel SHALL support filtering users by admin status

### Requirement 3: Kullanıcı Düzenleme

**User Story:** As an admin, I want to edit user information, so that I can update user details when needed.

#### Acceptance Criteria

1. WHEN an admin clicks edit on a user, THE Admin_Panel SHALL display an edit form with current user data
2. THE Admin_Panel SHALL allow editing email, full_name, and is_admin status
3. WHEN an admin saves changes, THE User_Management_System SHALL update the user record
4. IF validation fails, THEN THE Admin_Panel SHALL display appropriate error messages

### Requirement 4: Kullanıcı Silme

**User Story:** As an admin, I want to delete users, so that I can remove inactive or problematic accounts.

#### Acceptance Criteria

1. WHEN an admin clicks delete on a user, THE Admin_Panel SHALL display a confirmation dialog
2. WHEN deletion is confirmed, THE User_Management_System SHALL remove the user from the system
3. THE User_Management_System SHALL also delete all chat history associated with the deleted user
4. IF an admin tries to delete themselves, THEN THE Admin_Panel SHALL prevent the action with an error message

### Requirement 5: Kullanıcı Oluşturma

**User Story:** As an admin, I want to create new users, so that I can add users directly without them going through registration.

#### Acceptance Criteria

1. WHEN an admin clicks "Yeni Kullanıcı", THE Admin_Panel SHALL display a user creation form
2. THE Admin_Panel SHALL require username and password fields
3. THE Admin_Panel SHALL allow optional email, full_name, and is_admin fields
4. WHEN a new user is created, THE User_Management_System SHALL apply the same validation rules as registration
5. IF username already exists, THEN THE User_Management_System SHALL return an error

### Requirement 6: Admin Panel Güvenliği

**User Story:** As a system administrator, I want the admin panel to be secure, so that unauthorized users cannot access sensitive data.

#### Acceptance Criteria

1. THE Authentication_System SHALL verify admin status on every admin API request
2. IF a token is invalid or expired, THEN THE Admin_Panel SHALL redirect to login page
3. THE Admin_Panel SHALL use HTTPS-only cookies for session management in production
4. THE User_Management_System SHALL log all admin actions for audit purposes

### Requirement 7: Örnek Kullanıcılar

**User Story:** As a developer, I want sample users with real names, so that I can test the admin panel functionality.

#### Acceptance Criteria

1. THE User_Management_System SHALL include at least 5 sample users with Turkish names
2. THE sample users SHALL have varied data (some with email, some without, some admins)
3. THE sample users SHALL have realistic created_at timestamps
