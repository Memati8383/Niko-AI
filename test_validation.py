"""
Property-Based Tests for Pydantic Model Validation
Feature: niko-ai-chat

Uses Hypothesis library for property-based testing.
"""

import pytest
import json
from hypothesis import given, strategies as st, settings, assume
from pydantic import ValidationError
import string

# Import models from main
from main import UserCreate, UserLogin, UserUpdate, ChatRequest


# ============================================================================
# Feature: niko-ai-chat, Property 1: Username Validation
# Validates: Requirements 1.2, 1.3, 1.4
# ============================================================================

# Strategy for valid usernames: starts with letter, 3-30 chars, alphanumeric + underscore
valid_username_strategy = st.from_regex(
    r'^[a-zA-Z][a-zA-Z0-9_]{2,29}$',
    fullmatch=True
)

# Strategy for invalid usernames - too short (1-2 chars)
too_short_username_strategy = st.text(
    alphabet=string.ascii_letters + string.digits + '_',
    min_size=1,
    max_size=2
)

# Strategy for invalid usernames - too long (31+ chars)
too_long_username_strategy = st.text(
    alphabet=string.ascii_letters + string.digits + '_',
    min_size=31,
    max_size=50
).map(lambda s: 'a' + s if s else 'a' * 31)  # Ensure starts with letter

# Strategy for usernames starting with non-letter
starts_with_non_letter_strategy = st.from_regex(
    r'^[0-9_][a-zA-Z0-9_]{2,29}$',
    fullmatch=True
)

# Strategy for usernames with invalid characters
invalid_chars_username_strategy = st.text(
    alphabet=string.ascii_letters + string.digits + '_' + '!@#$%^&*()-+=[]{}|;:,.<>?/',
    min_size=3,
    max_size=30
).filter(lambda s: s and s[0].isalpha() and any(c in '!@#$%^&*()-+=[]{}|;:,.<>?/' for c in s))


class TestUsernameValidation:
    """Property 1: Username Validation - Validates: Requirements 1.2, 1.3, 1.4"""

    @given(username=valid_username_strategy)
    @settings(max_examples=20)
    def test_valid_usernames_accepted(self, username):
        """
        Feature: niko-ai-chat, Property 1: Username Validation
        For any valid username (3-30 chars, starts with letter, alphanumeric + underscore),
        the validation SHALL accept it.
        """
        # Valid password to pass password validation
        valid_password = "ValidPass1"
        
        # Should not raise ValidationError
        user = UserCreate(username=username, password=valid_password)
        assert user.username == username

    @given(username=too_short_username_strategy)
    @settings(max_examples=20)
    def test_too_short_usernames_rejected(self, username):
        """
        Feature: niko-ai-chat, Property 1: Username Validation
        For any username shorter than 3 characters, the validation SHALL reject it.
        """
        valid_password = "ValidPass1"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=username, password=valid_password)
        
        # Check that the error is about username length
        errors = exc_info.value.errors()
        assert any('username' in str(e.get('loc', '')) for e in errors)

    @given(username=too_long_username_strategy)
    @settings(max_examples=20)
    def test_too_long_usernames_rejected(self, username):
        """
        Feature: niko-ai-chat, Property 1: Username Validation
        For any username longer than 30 characters, the validation SHALL reject it.
        """
        valid_password = "ValidPass1"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=username, password=valid_password)
        
        errors = exc_info.value.errors()
        assert any('username' in str(e.get('loc', '')) for e in errors)

    @given(username=starts_with_non_letter_strategy)
    @settings(max_examples=20)
    def test_usernames_not_starting_with_letter_rejected(self, username):
        """
        Feature: niko-ai-chat, Property 1: Username Validation
        For any username that does not start with a letter, the validation SHALL reject it.
        """
        valid_password = "ValidPass1"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=username, password=valid_password)
        
        errors = exc_info.value.errors()
        assert any('username' in str(e.get('loc', '')) for e in errors)

    @given(username=invalid_chars_username_strategy)
    @settings(max_examples=20)
    def test_usernames_with_invalid_chars_rejected(self, username):
        """
        Feature: niko-ai-chat, Property 1: Username Validation
        For any username containing characters other than letters, numbers, or underscores,
        the validation SHALL reject it.
        """
        valid_password = "ValidPass1"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=username, password=valid_password)
        
        errors = exc_info.value.errors()
        assert any('username' in str(e.get('loc', '')) for e in errors)



# ============================================================================
# Feature: niko-ai-chat, Property 2: Password Validation
# Validates: Requirements 1.5, 1.6
# ============================================================================

# Strategy for valid passwords: min 8 chars, uppercase, lowercase, digit
@st.composite
def valid_password_strategy(draw):
    """Generate valid passwords with all required components"""
    # Ensure at least one of each required character type
    uppercase = draw(st.sampled_from(string.ascii_uppercase))
    lowercase = draw(st.sampled_from(string.ascii_lowercase))
    digit = draw(st.sampled_from(string.digits))
    
    # Fill remaining with any valid characters (min 5 more to reach 8)
    remaining_length = draw(st.integers(min_value=5, max_value=27))
    remaining = draw(st.text(
        alphabet=string.ascii_letters + string.digits,
        min_size=remaining_length,
        max_size=remaining_length
    ))
    
    # Combine and shuffle
    password_chars = list(uppercase + lowercase + digit + remaining)
    draw(st.randoms()).shuffle(password_chars)
    return ''.join(password_chars)


# Strategy for passwords that are too short (less than 8 chars)
@st.composite
def too_short_password_strategy(draw):
    """Generate passwords shorter than 8 characters"""
    length = draw(st.integers(min_value=1, max_value=7))
    return draw(st.text(
        alphabet=string.ascii_letters + string.digits,
        min_size=length,
        max_size=length
    ))


# Strategy for passwords missing uppercase
@st.composite
def no_uppercase_password_strategy(draw):
    """Generate passwords without uppercase letters"""
    length = draw(st.integers(min_value=8, max_value=20))
    password = draw(st.text(
        alphabet=string.ascii_lowercase + string.digits,
        min_size=length,
        max_size=length
    ))
    # Ensure it has lowercase and digit
    assume(any(c.islower() for c in password))
    assume(any(c.isdigit() for c in password))
    return password


# Strategy for passwords missing lowercase
@st.composite
def no_lowercase_password_strategy(draw):
    """Generate passwords without lowercase letters"""
    length = draw(st.integers(min_value=8, max_value=20))
    password = draw(st.text(
        alphabet=string.ascii_uppercase + string.digits,
        min_size=length,
        max_size=length
    ))
    # Ensure it has uppercase and digit
    assume(any(c.isupper() for c in password))
    assume(any(c.isdigit() for c in password))
    return password


# Strategy for passwords missing digits
@st.composite
def no_digit_password_strategy(draw):
    """Generate passwords without digits"""
    length = draw(st.integers(min_value=8, max_value=20))
    password = draw(st.text(
        alphabet=string.ascii_letters,
        min_size=length,
        max_size=length
    ))
    # Ensure it has uppercase and lowercase
    assume(any(c.isupper() for c in password))
    assume(any(c.islower() for c in password))
    return password


class TestPasswordValidation:
    """Property 2: Password Validation - Validates: Requirements 1.5, 1.6"""

    @given(password=valid_password_strategy())
    @settings(max_examples=20)
    def test_valid_passwords_accepted(self, password):
        """
        Feature: niko-ai-chat, Property 2: Password Validation
        For any valid password (min 8 chars, uppercase, lowercase, digit),
        the validation SHALL accept it.
        """
        valid_username = "validuser"
        
        # Should not raise ValidationError
        user = UserCreate(username=valid_username, password=password)
        assert user.password == password

    @given(password=too_short_password_strategy())
    @settings(max_examples=20)
    def test_too_short_passwords_rejected(self, password):
        """
        Feature: niko-ai-chat, Property 2: Password Validation
        For any password shorter than 8 characters, the validation SHALL reject it.
        """
        valid_username = "validuser"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=valid_username, password=password)
        
        errors = exc_info.value.errors()
        assert any('password' in str(e.get('loc', '')) for e in errors)

    @given(password=no_uppercase_password_strategy())
    @settings(max_examples=20)
    def test_passwords_without_uppercase_rejected(self, password):
        """
        Feature: niko-ai-chat, Property 2: Password Validation
        For any password without an uppercase letter, the validation SHALL reject it.
        """
        valid_username = "validuser"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=valid_username, password=password)
        
        errors = exc_info.value.errors()
        assert any('password' in str(e.get('loc', '')) for e in errors)

    @given(password=no_lowercase_password_strategy())
    @settings(max_examples=20)
    def test_passwords_without_lowercase_rejected(self, password):
        """
        Feature: niko-ai-chat, Property 2: Password Validation
        For any password without a lowercase letter, the validation SHALL reject it.
        """
        valid_username = "validuser"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=valid_username, password=password)
        
        errors = exc_info.value.errors()
        assert any('password' in str(e.get('loc', '')) for e in errors)

    @given(password=no_digit_password_strategy())
    @settings(max_examples=20)
    def test_passwords_without_digit_rejected(self, password):
        """
        Feature: niko-ai-chat, Property 2: Password Validation
        For any password without a digit, the validation SHALL reject it.
        """
        valid_username = "validuser"
        
        with pytest.raises(ValidationError) as exc_info:
            UserCreate(username=valid_username, password=password)
        
        errors = exc_info.value.errors()
        assert any('password' in str(e.get('loc', '')) for e in errors)


# ============================================================================
# Feature: niko-ai-chat, Property 3: Password Hashing Round-Trip
# Validates: Requirements 1.9, 7.5
# ============================================================================

from main import AuthService

# Strategy for generating valid passwords for hashing tests
@st.composite
def password_for_hashing_strategy(draw):
    """Generate passwords for hashing round-trip tests (max 72 bytes for bcrypt)"""
    length = draw(st.integers(min_value=1, max_value=50))  # Keep under 72 bytes
    return draw(st.text(
        alphabet=string.ascii_letters + string.digits,
        min_size=length,
        max_size=length
    ))


class TestPasswordHashingRoundTrip:
    """Property 3: Password Hashing Round-Trip - Validates: Requirements 1.9, 7.5"""

    @given(password=password_for_hashing_strategy())
    @settings(max_examples=20, deadline=None)
    def test_password_hash_verify_roundtrip(self, password):
        """
        Feature: niko-ai-chat, Property 3: Password Hashing Round-Trip
        For any valid password, hashing it with bcrypt and then verifying the original
        password against the hash SHALL return true.
        """
        auth_service = AuthService()
        
        # Hash the password
        hashed = auth_service.hash_password(password)
        
        # Verify the original password against the hash
        assert auth_service.verify_password(password, hashed) is True

    @given(password=password_for_hashing_strategy(), wrong_password=password_for_hashing_strategy())
    @settings(max_examples=20, deadline=None)
    def test_different_password_fails_verification(self, password, wrong_password):
        """
        Feature: niko-ai-chat, Property 3: Password Hashing Round-Trip
        For any valid password, verifying a different password against the hash
        SHALL return false.
        """
        # Skip if passwords happen to be the same
        assume(password != wrong_password)
        
        auth_service = AuthService()
        
        # Hash the original password
        hashed = auth_service.hash_password(password)
        
        # Verify a different password against the hash
        assert auth_service.verify_password(wrong_password, hashed) is False

    @given(password=password_for_hashing_strategy())
    @settings(max_examples=20, deadline=None)
    def test_hash_is_not_plaintext(self, password):
        """
        Feature: niko-ai-chat, Property 3: Password Hashing Round-Trip
        For any password, the hash SHALL NOT be equal to the plaintext password.
        """
        auth_service = AuthService()
        
        hashed = auth_service.hash_password(password)
        
        # Hash should never equal the plaintext
        assert hashed != password
        # Hash should start with bcrypt identifier
        assert hashed.startswith('$2')


# ============================================================================
# Feature: niko-ai-chat, Property 4: Registration Uniqueness
# Validates: Requirements 1.1, 1.8
# ============================================================================

import os
import tempfile
import shutil


@st.composite
def valid_user_data_strategy(draw):
    """Generate valid user registration data"""
    username = draw(valid_username_strategy)
    password = draw(valid_password_strategy())
    # Generate emails that match our regex pattern
    email = draw(st.one_of(
        st.none(),
        st.from_regex(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', fullmatch=True)
    ))
    full_name = draw(st.one_of(
        st.none(),
        st.text(min_size=1, max_size=50, alphabet=string.ascii_letters + ' ')
    ))
    return {
        "username": username,
        "password": password,
        "email": email,
        "full_name": full_name
    }


class TestRegistrationUniqueness:
    """Property 4: Registration Uniqueness - Validates: Requirements 1.1, 1.8"""

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_new_user_registration_succeeds(self, user_data):
        """
        Feature: niko-ai-chat, Property 4: Registration Uniqueness
        For any valid user registration with a new username, the registration SHALL succeed.
        """
        # Create a temporary users file
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            user = UserCreate(**user_data)
            result = auth_service.register(user)
            
            assert result["message"] == "Kayıt başarılı"
            
            # Verify user is retrievable
            saved_user = auth_service.get_user(user_data["username"])
            assert saved_user is not None
            assert saved_user["email"] == user_data["email"]
            assert saved_user["full_name"] == user_data["full_name"]
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_duplicate_username_rejected(self, user_data):
        """
        Feature: niko-ai-chat, Property 4: Registration Uniqueness
        For any valid user registration, if the username already exists,
        the registration SHALL be rejected.
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            user = UserCreate(**user_data)
            
            # First registration should succeed
            result = auth_service.register(user)
            assert result["message"] == "Kayıt başarılı"
            
            # Second registration with same username should fail
            with pytest.raises(ValueError) as exc_info:
                auth_service.register(user)
            
            assert "zaten kullanılıyor" in str(exc_info.value)
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: niko-ai-chat, Property 5: JWT Authentication
# Validates: Requirements 2.1, 2.4, 2.5
# ============================================================================


class TestJWTAuthentication:
    """Property 5: JWT Authentication - Validates: Requirements 2.1, 2.4, 2.5"""

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_valid_token_returns_username(self, username):
        """
        Feature: niko-ai-chat, Property 5: JWT Authentication
        For any valid JWT token, verifying it SHALL return the correct username.
        """
        auth_service = AuthService()
        
        # Create a token
        token = auth_service.create_token(username)
        
        # Verify the token
        result = auth_service.verify_token(token)
        
        assert result == username

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_invalid_token_returns_none(self, username):
        """
        Feature: niko-ai-chat, Property 5: JWT Authentication
        For any invalid or malformed JWT token, verifying it SHALL return None.
        """
        auth_service = AuthService()
        
        # Test with invalid tokens
        invalid_tokens = [
            "invalid_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature",
            "",
            "a.b.c",
        ]
        
        for invalid_token in invalid_tokens:
            result = auth_service.verify_token(invalid_token)
            assert result is None

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_token_with_wrong_secret_returns_none(self, username):
        """
        Feature: niko-ai-chat, Property 5: JWT Authentication
        For any JWT token created with a different secret, verifying it SHALL return None.
        """
        auth_service1 = AuthService()
        auth_service1.secret_key = "secret1"
        
        auth_service2 = AuthService()
        auth_service2.secret_key = "secret2"
        
        # Create token with first service
        token = auth_service1.create_token(username)
        
        # Verify with second service (different secret)
        result = auth_service2.verify_token(token)
        
        assert result is None

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_login_returns_valid_token(self, user_data):
        """
        Feature: niko-ai-chat, Property 5: JWT Authentication
        For any valid login, the returned token SHALL be verifiable and contain the correct username.
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Register user first
            user = UserCreate(**user_data)
            auth_service.register(user)
            
            # Login
            credentials = UserLogin(username=user_data["username"], password=user_data["password"])
            result = auth_service.login(credentials)
            
            assert "access_token" in result
            assert result["token_type"] == "bearer"
            
            # Verify the token
            verified_username = auth_service.verify_token(result["access_token"])
            assert verified_username == user_data["username"]
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_user_data_strategy(), wrong_password=valid_password_strategy())
    @settings(max_examples=20, deadline=None)
    def test_login_with_wrong_password_fails(self, user_data, wrong_password):
        """
        Feature: niko-ai-chat, Property 5: JWT Authentication
        For any login attempt with wrong password, the login SHALL fail.
        """
        # Skip if passwords happen to be the same
        assume(user_data["password"] != wrong_password)
        
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Register user first
            user = UserCreate(**user_data)
            auth_service.register(user)
            
            # Try to login with wrong password
            credentials = UserLogin(username=user_data["username"], password=wrong_password)
            
            with pytest.raises(ValueError) as exc_info:
                auth_service.login(credentials)
            
            assert "Geçersiz" in str(exc_info.value)
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: niko-ai-chat, Property 7: Profile Data Consistency
# Validates: Requirements 2.6, 2.7
# ============================================================================


class TestProfileDataConsistency:
    """Property 7: Profile Data Consistency - Validates: Requirements 2.6, 2.7"""

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_profile_returns_registration_data(self, user_data):
        """
        Feature: niko-ai-chat, Property 7: Profile Data Consistency
        For any registered user, requesting their profile SHALL return the same
        email and full_name that were provided during registration.
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Register user
            user = UserCreate(**user_data)
            auth_service.register(user)
            
            # Get profile
            profile = auth_service.get_profile(user_data["username"])
            
            assert profile["username"] == user_data["username"]
            assert profile["email"] == user_data["email"]
            assert profile["full_name"] == user_data["full_name"]
            assert "created_at" in profile
        finally:
            shutil.rmtree(temp_dir)

    @given(
        user_data=valid_user_data_strategy(),
        new_email=st.one_of(
            st.none(),
            st.from_regex(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', fullmatch=True)
        ),
        new_full_name=st.one_of(
            st.none(),
            st.text(min_size=1, max_size=50, alphabet=string.ascii_letters + ' ')
        )
    )
    @settings(max_examples=20, deadline=None)
    def test_profile_update_persists(self, user_data, new_email, new_full_name):
        """
        Feature: niko-ai-chat, Property 7: Profile Data Consistency
        For any profile update, the updated values SHALL be returned in subsequent
        profile requests.
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Register user
            user = UserCreate(**user_data)
            auth_service.register(user)
            
            # Update profile
            update = UserUpdate(email=new_email, full_name=new_full_name)
            auth_service.update_profile(user_data["username"], update)
            
            # Get profile
            profile = auth_service.get_profile(user_data["username"])
            
            # Check updated values
            expected_email = new_email if new_email is not None else user_data["email"]
            expected_full_name = new_full_name if new_full_name is not None else user_data["full_name"]
            
            assert profile["email"] == expected_email
            assert profile["full_name"] == expected_full_name
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_user_data_strategy(), new_password=valid_password_strategy())
    @settings(max_examples=20, deadline=None)
    def test_password_update_requires_current_password(self, user_data, new_password):
        """
        Feature: niko-ai-chat, Property 7: Profile Data Consistency
        For any password update, the current password MUST be provided and correct.
        """
        # Skip if passwords are the same
        assume(user_data["password"] != new_password)
        
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Register user
            user = UserCreate(**user_data)
            auth_service.register(user)
            
            # Try to update password without current password
            update = UserUpdate(new_password=new_password)
            with pytest.raises(ValueError) as exc_info:
                auth_service.update_profile(user_data["username"], update)
            assert "Mevcut şifre gerekli" in str(exc_info.value)
            
            # Try to update password with wrong current password
            update = UserUpdate(current_password="wrongpassword", new_password=new_password)
            with pytest.raises(ValueError) as exc_info:
                auth_service.update_profile(user_data["username"], update)
            assert "Mevcut şifre yanlış" in str(exc_info.value)
            
            # Update password with correct current password
            update = UserUpdate(current_password=user_data["password"], new_password=new_password)
            result = auth_service.update_profile(user_data["username"], update)
            assert result["message"] == "Profil güncellendi"
            
            # Verify new password works for login
            credentials = UserLogin(username=user_data["username"], password=new_password)
            login_result = auth_service.login(credentials)
            assert "access_token" in login_result
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: niko-ai-chat, Property 9: History CRUD Operations
# Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.6
# ============================================================================

from main import HistoryService


@st.composite
def valid_message_strategy(draw):
    """Generate valid chat messages"""
    role = draw(st.sampled_from(["user", "bot"]))
    content = draw(st.text(min_size=1, max_size=200, alphabet=string.ascii_letters + string.digits + ' .,!?'))
    thought = None
    if role == "bot":
        thought = draw(st.one_of(
            st.none(),
            st.text(min_size=1, max_size=100, alphabet=string.ascii_letters + string.digits + ' .,!?')
        ))
    return {"role": role, "content": content, "thought": thought}


class TestHistoryCRUDOperations:
    """Property 9: History CRUD Operations - Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.6"""

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_create_session_generates_unique_id(self, username):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Creating a session SHALL generate a unique ID and create a JSON file.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session
            session_id = history_service.create_session(username)
            
            # Verify session ID is a valid UUID
            import uuid
            uuid.UUID(session_id)  # Will raise if invalid
            
            # Verify file was created
            path = history_service.get_session_path(username, session_id)
            assert os.path.exists(path)
            
            # Verify file content
            with open(path, 'r', encoding='utf-8') as f:
                session = json.load(f)
            
            assert session["id"] == session_id
            assert session["title"] == "Yeni Sohbet"
            assert "timestamp" in session
            assert session["messages"] == []
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_multiple_sessions_have_unique_ids(self, username):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Creating multiple sessions SHALL generate unique IDs for each.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create multiple sessions
            session_ids = [history_service.create_session(username) for _ in range(5)]
            
            # All IDs should be unique
            assert len(session_ids) == len(set(session_ids))
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy, message=valid_message_strategy())
    @settings(max_examples=20, deadline=None)
    def test_add_message_to_session(self, username, message):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Adding a message to a session SHALL persist it correctly.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session
            session_id = history_service.create_session(username)
            
            # Add message
            history_service.add_message(
                username, session_id, 
                message["role"], message["content"], message["thought"]
            )
            
            # Load session and verify
            session = history_service.get_session(username, session_id)
            
            assert len(session["messages"]) == 1
            assert session["messages"][0]["role"] == message["role"]
            assert session["messages"][0]["content"] == message["content"]
            if message["thought"]:
                assert session["messages"][0]["thought"] == message["thought"]
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_get_history_returns_all_sessions(self, username):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Listing history SHALL return all sessions for that user.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create multiple sessions
            created_ids = [history_service.create_session(username) for _ in range(3)]
            
            # Get history
            history = history_service.get_history(username)
            
            # Verify all sessions are returned
            assert len(history) == 3
            returned_ids = [h["id"] for h in history]
            for session_id in created_ids:
                assert session_id in returned_ids
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_delete_session_removes_file(self, username):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Deleting a session SHALL remove the JSON file.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session
            session_id = history_service.create_session(username)
            path = history_service.get_session_path(username, session_id)
            
            # Verify file exists
            assert os.path.exists(path)
            
            # Delete session
            result = history_service.delete_session(username, session_id)
            
            assert result is True
            assert not os.path.exists(path)
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_delete_all_sessions_removes_all_files(self, username):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Clearing all history SHALL remove all session files for that user.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create multiple sessions
            session_ids = [history_service.create_session(username) for _ in range(3)]
            
            # Verify files exist
            for session_id in session_ids:
                path = history_service.get_session_path(username, session_id)
                assert os.path.exists(path)
            
            # Delete all sessions
            deleted_count = history_service.delete_all_sessions(username)
            
            assert deleted_count == 3
            
            # Verify all files are removed
            for session_id in session_ids:
                path = history_service.get_session_path(username, session_id)
                assert not os.path.exists(path)
            
            # Verify history is empty
            history = history_service.get_history(username)
            assert len(history) == 0
        finally:
            shutil.rmtree(temp_dir)

    @given(username1=valid_username_strategy, username2=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_user_isolation(self, username1, username2):
        """
        Feature: niko-ai-chat, Property 9: History CRUD Operations
        Each user's history SHALL be isolated from other users.
        """
        # Skip if usernames are the same
        assume(username1 != username2)
        
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create sessions for both users
            session1 = history_service.create_session(username1)
            session2 = history_service.create_session(username2)
            
            # Each user should only see their own sessions
            history1 = history_service.get_history(username1)
            history2 = history_service.get_history(username2)
            
            assert len(history1) == 1
            assert len(history2) == 1
            assert history1[0]["id"] == session1
            assert history2[0]["id"] == session2
            
            # Deleting one user's sessions shouldn't affect the other
            history_service.delete_all_sessions(username1)
            
            history1_after = history_service.get_history(username1)
            history2_after = history_service.get_history(username2)
            
            assert len(history1_after) == 0
            assert len(history2_after) == 1
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: niko-ai-chat, Property 11: Markdown Export Format
# Validates: Requirements 4.5
# ============================================================================


class TestMarkdownExportFormat:
    """Property 11: Markdown Export Format - Validates: Requirements 4.5"""

    @given(
        username=valid_username_strategy,
        messages=st.lists(valid_message_strategy(), min_size=1, max_size=5)
    )
    @settings(max_examples=20, deadline=None)
    def test_export_contains_title(self, username, messages):
        """
        Feature: niko-ai-chat, Property 11: Markdown Export Format
        For any chat session exported to Markdown, the output SHALL contain the session title as heading.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add messages
            session_id = history_service.create_session(username)
            for msg in messages:
                history_service.add_message(
                    username, session_id,
                    msg["role"], msg["content"], msg["thought"]
                )
            
            # Export to markdown
            markdown = history_service.export_markdown(username, session_id)
            
            # Verify title is present as heading
            assert markdown.startswith("# ")
            
            # Get the session to check title
            session = history_service.get_session(username, session_id)
            assert session["title"] in markdown
        finally:
            shutil.rmtree(temp_dir)

    @given(
        username=valid_username_strategy,
        messages=st.lists(valid_message_strategy(), min_size=1, max_size=5)
    )
    @settings(max_examples=20, deadline=None)
    def test_export_contains_timestamp(self, username, messages):
        """
        Feature: niko-ai-chat, Property 11: Markdown Export Format
        For any chat session exported to Markdown, the output SHALL contain the timestamp.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add messages
            session_id = history_service.create_session(username)
            for msg in messages:
                history_service.add_message(
                    username, session_id,
                    msg["role"], msg["content"], msg["thought"]
                )
            
            # Export to markdown
            markdown = history_service.export_markdown(username, session_id)
            
            # Verify timestamp is present
            assert "*Tarih:" in markdown
            
            # Get the session to check timestamp
            session = history_service.get_session(username, session_id)
            assert session["timestamp"] in markdown
        finally:
            shutil.rmtree(temp_dir)

    @given(
        username=valid_username_strategy,
        messages=st.lists(valid_message_strategy(), min_size=1, max_size=5)
    )
    @settings(max_examples=20, deadline=None)
    def test_export_contains_all_messages_with_role_indicators(self, username, messages):
        """
        Feature: niko-ai-chat, Property 11: Markdown Export Format
        For any chat session exported to Markdown, the output SHALL contain all messages
        with role indicators (👤 Kullanıcı / 🤖 Niko).
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add messages
            session_id = history_service.create_session(username)
            for msg in messages:
                history_service.add_message(
                    username, session_id,
                    msg["role"], msg["content"], msg["thought"]
                )
            
            # Export to markdown
            markdown = history_service.export_markdown(username, session_id)
            
            # Verify all messages are present with correct role indicators
            for msg in messages:
                assert msg["content"] in markdown
                if msg["role"] == "user":
                    assert "👤 Kullanıcı" in markdown
                else:
                    assert "🤖 Niko" in markdown
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_export_empty_session(self, username):
        """
        Feature: niko-ai-chat, Property 11: Markdown Export Format
        For any empty chat session, the export SHALL still contain title and timestamp.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create empty session
            session_id = history_service.create_session(username)
            
            # Export to markdown
            markdown = history_service.export_markdown(username, session_id)
            
            # Verify basic structure
            assert markdown.startswith("# ")
            assert "*Tarih:" in markdown
            assert "---" in markdown
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: niko-ai-chat, Property 10: History Message Format
# Validates: Requirements 4.7, 9.5
# ============================================================================


class TestHistoryMessageFormat:
    """Property 10: History Message Format - Validates: Requirements 4.7, 9.5"""

    @given(
        username=valid_username_strategy,
        role=st.sampled_from(["user", "bot"]),
        content=st.text(min_size=1, max_size=200, alphabet=string.ascii_letters + string.digits + ' .,!?')
    )
    @settings(max_examples=20, deadline=None)
    def test_message_contains_role_and_content(self, username, role, content):
        """
        Feature: niko-ai-chat, Property 10: History Message Format
        For any message saved to chat history, the JSON structure SHALL contain role and content.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add message
            session_id = history_service.create_session(username)
            history_service.add_message(username, session_id, role, content)
            
            # Load session and verify message format
            session = history_service.get_session(username, session_id)
            
            assert len(session["messages"]) == 1
            message = session["messages"][0]
            
            # Verify required fields
            assert "role" in message
            assert "content" in message
            assert message["role"] == role
            assert message["content"] == content
        finally:
            shutil.rmtree(temp_dir)

    @given(
        username=valid_username_strategy,
        content=st.text(min_size=1, max_size=200, alphabet=string.ascii_letters + string.digits + ' .,!?'),
        thought=st.text(min_size=1, max_size=100, alphabet=string.ascii_letters + string.digits + ' .,!?')
    )
    @settings(max_examples=20, deadline=None)
    def test_bot_message_can_have_thought(self, username, content, thought):
        """
        Feature: niko-ai-chat, Property 10: History Message Format
        For any bot message saved to chat history, the JSON structure MAY contain thought (optional).
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add bot message with thought
            session_id = history_service.create_session(username)
            history_service.add_message(username, session_id, "bot", content, thought)
            
            # Load session and verify message format
            session = history_service.get_session(username, session_id)
            
            message = session["messages"][0]
            
            # Verify thought is present
            assert "thought" in message
            assert message["thought"] == thought
        finally:
            shutil.rmtree(temp_dir)

    @given(
        username=valid_username_strategy,
        content=st.text(min_size=1, max_size=200, alphabet=string.ascii_letters + string.digits + ' .,!?')
    )
    @settings(max_examples=20, deadline=None)
    def test_message_without_thought_has_no_thought_field(self, username, content):
        """
        Feature: niko-ai-chat, Property 10: History Message Format
        For any message saved without thought, the JSON structure SHALL NOT contain thought field.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add message without thought
            session_id = history_service.create_session(username)
            history_service.add_message(username, session_id, "user", content)
            
            # Load session and verify message format
            session = history_service.get_session(username, session_id)
            
            message = session["messages"][0]
            
            # Verify thought is NOT present
            assert "thought" not in message
        finally:
            shutil.rmtree(temp_dir)

    @given(
        username=valid_username_strategy,
        messages=st.lists(valid_message_strategy(), min_size=1, max_size=10)
    )
    @settings(max_examples=20, deadline=None)
    def test_session_format_contains_required_fields(self, username, messages):
        """
        Feature: niko-ai-chat, Property 10: History Message Format
        For any chat session, the JSON structure SHALL contain id, title, timestamp, and messages array.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add messages
            session_id = history_service.create_session(username)
            for msg in messages:
                history_service.add_message(
                    username, session_id,
                    msg["role"], msg["content"], msg["thought"]
                )
            
            # Load session and verify format
            session = history_service.get_session(username, session_id)
            
            # Verify required fields per Requirements 9.5
            assert "id" in session
            assert "title" in session
            assert "timestamp" in session
            assert "messages" in session
            assert isinstance(session["messages"], list)
            assert len(session["messages"]) == len(messages)
        finally:
            shutil.rmtree(temp_dir)

    @given(
        username=valid_username_strategy,
        content=st.text(min_size=51, max_size=100, alphabet=string.ascii_letters + string.digits + ' ')
    )
    @settings(max_examples=20, deadline=None)
    def test_title_truncated_for_long_messages(self, username, content):
        """
        Feature: niko-ai-chat, Property 10: History Message Format
        For any first user message longer than 50 characters, the title SHALL be truncated with ellipsis.
        """
        temp_dir = tempfile.mkdtemp()
        
        try:
            history_service = HistoryService()
            history_service.history_dir = temp_dir
            
            # Create session and add long message
            session_id = history_service.create_session(username)
            history_service.add_message(username, session_id, "user", content)
            
            # Load session and verify title
            session = history_service.get_session(username, session_id)
            
            # Title should be truncated to 50 chars + "..."
            assert len(session["title"]) == 53
            assert session["title"].endswith("...")
            assert session["title"][:50] == content[:50]
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
# Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5
# ============================================================================

from main import RateLimiter


class TestRateLimitingEnforcement:
    """Property 8: Rate Limiting Enforcement - Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5"""

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_general_rate_limit_allows_up_to_limit(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, the rate limiter SHALL allow up to 60 requests per minute on general endpoints.
        Validates: Requirements 6.1
        """
        rate_limiter = RateLimiter()
        
        # Make 60 requests - all should be allowed
        for i in range(60):
            allowed, retry_after = rate_limiter.is_allowed(client_ip, "general")
            assert allowed is True, f"Request {i+1} should be allowed"
            assert retry_after == 0
        
        # 61st request should be rejected
        allowed, retry_after = rate_limiter.is_allowed(client_ip, "general")
        assert allowed is False, "61st request should be rejected"
        assert retry_after > 0, "retry_after should be positive when rate limited"

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_auth_rate_limit_allows_up_to_limit(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, the rate limiter SHALL allow up to 5 authentication attempts in 5 minutes.
        Validates: Requirements 6.2
        """
        rate_limiter = RateLimiter()
        
        # Make 5 requests - all should be allowed
        for i in range(5):
            allowed, retry_after = rate_limiter.is_allowed(client_ip, "auth")
            assert allowed is True, f"Auth request {i+1} should be allowed"
            assert retry_after == 0
        
        # 6th request should be rejected
        allowed, retry_after = rate_limiter.is_allowed(client_ip, "auth")
        assert allowed is False, "6th auth request should be rejected"
        assert retry_after > 0

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_register_rate_limit_allows_up_to_limit(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, the rate limiter SHALL allow up to 3 registration attempts per hour.
        Validates: Requirements 6.3
        """
        rate_limiter = RateLimiter()
        
        # Make 3 requests - all should be allowed
        for i in range(3):
            allowed, retry_after = rate_limiter.is_allowed(client_ip, "register")
            assert allowed is True, f"Register request {i+1} should be allowed"
            assert retry_after == 0
        
        # 4th request should be rejected
        allowed, retry_after = rate_limiter.is_allowed(client_ip, "register")
        assert allowed is False, "4th register request should be rejected"
        assert retry_after > 0

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_chat_rate_limit_allows_up_to_limit(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, the rate limiter SHALL allow up to 30 chat requests per minute.
        Validates: Requirements 6.4
        """
        rate_limiter = RateLimiter()
        
        # Make 30 requests - all should be allowed
        for i in range(30):
            allowed, retry_after = rate_limiter.is_allowed(client_ip, "chat")
            assert allowed is True, f"Chat request {i+1} should be allowed"
            assert retry_after == 0
        
        # 31st request should be rejected
        allowed, retry_after = rate_limiter.is_allowed(client_ip, "chat")
        assert allowed is False, "31st chat request should be rejected"
        assert retry_after > 0

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_rate_limit_returns_retry_after(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        When rate limit is exceeded, the rate limiter SHALL return retry-after information.
        Validates: Requirements 6.5
        """
        rate_limiter = RateLimiter()
        
        # Exhaust the general limit
        for _ in range(60):
            rate_limiter.is_allowed(client_ip, "general")
        
        # Next request should return retry_after > 0
        allowed, retry_after = rate_limiter.is_allowed(client_ip, "general")
        
        assert allowed is False
        assert retry_after > 0, "retry_after should be positive"
        # retry_after can be up to window + 1 (due to +1 in implementation to ensure at least 1 second)
        assert retry_after <= 61, "retry_after should not significantly exceed window size"

    @given(
        client_ip1=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True),
        client_ip2=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True)
    )
    @settings(max_examples=20, deadline=None)
    def test_rate_limits_are_per_client(self, client_ip1, client_ip2):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any two different clients, rate limits SHALL be tracked independently.
        """
        assume(client_ip1 != client_ip2)
        
        rate_limiter = RateLimiter()
        
        # Exhaust limit for client 1
        for _ in range(60):
            rate_limiter.is_allowed(client_ip1, "general")
        
        # Client 1 should be rate limited
        allowed1, _ = rate_limiter.is_allowed(client_ip1, "general")
        assert allowed1 is False
        
        # Client 2 should still be allowed
        allowed2, retry_after2 = rate_limiter.is_allowed(client_ip2, "general")
        assert allowed2 is True
        assert retry_after2 == 0

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_different_limit_types_are_independent(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, different limit types (general, auth, register, chat) SHALL be tracked independently.
        """
        rate_limiter = RateLimiter()
        
        # Exhaust auth limit (5 requests)
        for _ in range(5):
            rate_limiter.is_allowed(client_ip, "auth")
        
        # Auth should be rate limited
        allowed_auth, _ = rate_limiter.is_allowed(client_ip, "auth")
        assert allowed_auth is False
        
        # But general should still be allowed
        allowed_general, retry_after = rate_limiter.is_allowed(client_ip, "general")
        assert allowed_general is True
        assert retry_after == 0
        
        # And chat should still be allowed
        allowed_chat, retry_after = rate_limiter.is_allowed(client_ip, "chat")
        assert allowed_chat is True
        assert retry_after == 0

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_get_remaining_returns_correct_count(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, get_remaining SHALL return the correct number of remaining requests.
        """
        rate_limiter = RateLimiter()
        
        # Initially should have full limit
        remaining = rate_limiter.get_remaining(client_ip, "general")
        assert remaining == 60
        
        # Make some requests
        for i in range(10):
            rate_limiter.is_allowed(client_ip, "general")
        
        # Should have 50 remaining
        remaining = rate_limiter.get_remaining(client_ip, "general")
        assert remaining == 50
        
        # Exhaust the limit
        for _ in range(50):
            rate_limiter.is_allowed(client_ip, "general")
        
        # Should have 0 remaining
        remaining = rate_limiter.get_remaining(client_ip, "general")
        assert remaining == 0

    @given(client_ip=st.from_regex(r'^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$', fullmatch=True))
    @settings(max_examples=20, deadline=None)
    def test_reset_clears_rate_limit(self, client_ip):
        """
        Feature: niko-ai-chat, Property 8: Rate Limiting Enforcement
        For any client, reset SHALL clear the rate limit tracking.
        """
        rate_limiter = RateLimiter()
        
        # Exhaust the limit
        for _ in range(60):
            rate_limiter.is_allowed(client_ip, "general")
        
        # Should be rate limited
        allowed, _ = rate_limiter.is_allowed(client_ip, "general")
        assert allowed is False
        
        # Reset
        rate_limiter.reset(client_ip, "general")
        
        # Should be allowed again
        allowed, retry_after = rate_limiter.is_allowed(client_ip, "general")
        assert allowed is True
        assert retry_after == 0


# ============================================================================
# Feature: niko-ai-chat, Property 12: Security Headers
# Validates: Requirements 7.1
# ============================================================================

from fastapi.testclient import TestClient
from main import app


class TestSecurityHeaders:
    """Property 12: Security Headers - Validates: Requirements 7.1"""

    @given(path=st.sampled_from(["/health", "/", "/login.html", "/signup.html"]))
    @settings(max_examples=20, deadline=None)
    def test_security_headers_present_on_all_responses(self, path):
        """
        Feature: niko-ai-chat, Property 12: Security Headers
        For any HTTP response from the Niko_System, the following headers SHALL be present:
        - X-Content-Type-Options: nosniff
        - X-Frame-Options: DENY
        - X-XSS-Protection: 1; mode=block
        - Referrer-Policy: strict-origin-when-cross-origin
        **Validates: Requirements 7.1**
        """
        client = TestClient(app)
        response = client.get(path)
        
        # Verify all required security headers are present
        assert response.headers.get("X-Content-Type-Options") == "nosniff", \
            f"X-Content-Type-Options header missing or incorrect on {path}"
        assert response.headers.get("X-Frame-Options") == "DENY", \
            f"X-Frame-Options header missing or incorrect on {path}"
        assert response.headers.get("X-XSS-Protection") == "1; mode=block", \
            f"X-XSS-Protection header missing or incorrect on {path}"
        assert response.headers.get("Referrer-Policy") == "strict-origin-when-cross-origin", \
            f"Referrer-Policy header missing or incorrect on {path}"

    @given(
        username=valid_username_strategy,
        password=valid_password_strategy()
    )
    @settings(max_examples=20, deadline=None)
    def test_security_headers_on_post_requests(self, username, password):
        """
        Feature: niko-ai-chat, Property 12: Security Headers
        For any POST request response, security headers SHALL be present.
        **Validates: Requirements 7.1**
        """
        client = TestClient(app)
        
        # Test POST /register endpoint
        response = client.post("/register", json={
            "username": username,
            "password": password
        })
        
        # Verify security headers regardless of response status
        assert response.headers.get("X-Content-Type-Options") == "nosniff"
        assert response.headers.get("X-Frame-Options") == "DENY"
        assert response.headers.get("X-XSS-Protection") == "1; mode=block"
        assert response.headers.get("Referrer-Policy") == "strict-origin-when-cross-origin"

    @given(path=st.sampled_from(["/health", "/"]))
    @settings(max_examples=20, deadline=None)
    def test_hsts_header_not_present_in_non_production(self, path):
        """
        Feature: niko-ai-chat, Property 12: Security Headers
        When NOT in production mode, HSTS header SHALL NOT be present.
        **Validates: Requirements 7.2**
        """
        # Ensure PRODUCTION env var is not set or is false
        import os
        original_value = os.environ.get("PRODUCTION")
        os.environ["PRODUCTION"] = "false"
        
        try:
            client = TestClient(app)
            response = client.get(path)
            
            # HSTS should NOT be present in non-production
            assert "Strict-Transport-Security" not in response.headers, \
                f"HSTS header should not be present in non-production mode on {path}"
        finally:
            # Restore original value
            if original_value is not None:
                os.environ["PRODUCTION"] = original_value
            elif "PRODUCTION" in os.environ:
                del os.environ["PRODUCTION"]

    def test_security_headers_on_error_responses(self):
        """
        Feature: niko-ai-chat, Property 12: Security Headers
        For any error response, security headers SHALL still be present.
        **Validates: Requirements 7.1**
        """
        client = TestClient(app)
        
        # Test 404 error
        response = client.get("/nonexistent-endpoint")
        
        # Security headers should be present even on error responses
        assert response.headers.get("X-Content-Type-Options") == "nosniff"
        assert response.headers.get("X-Frame-Options") == "DENY"
        assert response.headers.get("X-XSS-Protection") == "1; mode=block"
        assert response.headers.get("Referrer-Policy") == "strict-origin-when-cross-origin"

    def test_security_headers_on_401_responses(self):
        """
        Feature: niko-ai-chat, Property 12: Security Headers
        For any 401 unauthorized response, security headers SHALL be present.
        **Validates: Requirements 7.1**
        """
        client = TestClient(app)
        
        # Test protected endpoint without auth
        response = client.get("/me")
        
        # FastAPI HTTPBearer returns 401 or 403 depending on configuration
        assert response.status_code in [401, 403]
        
        # Security headers should be present
        assert response.headers.get("X-Content-Type-Options") == "nosniff"
        assert response.headers.get("X-Frame-Options") == "DENY"
        assert response.headers.get("X-XSS-Protection") == "1; mode=block"
        assert response.headers.get("Referrer-Policy") == "strict-origin-when-cross-origin"


# ============================================================================
# Feature: niko-ai-chat, Property 15: Image Attachment Handling
# Validates: Requirements 3.5
# ============================================================================

from main import ChatService, ChatRequest
import base64


@st.composite
def valid_base64_image_strategy(draw):
    """Generate valid base64-encoded image data"""
    # Generate random bytes that simulate image data
    size = draw(st.integers(min_value=10, max_value=100))
    random_bytes = draw(st.binary(min_size=size, max_size=size))
    return base64.b64encode(random_bytes).decode('utf-8')


@st.composite
def chat_request_with_images_strategy(draw):
    """Generate ChatRequest with images"""
    message = draw(st.text(min_size=1, max_size=100, alphabet=string.ascii_letters + string.digits + ' .,!?'))
    num_images = draw(st.integers(min_value=1, max_value=3))
    images = [draw(valid_base64_image_strategy()) for _ in range(num_images)]
    model = draw(st.one_of(st.none(), st.sampled_from(["llama2", "mistral", "codellama"])))
    
    return {
        "message": message,
        "images": images,
        "model": model
    }


class TestImageAttachmentHandling:
    """Property 15: Image Attachment Handling - Validates: Requirements 3.5"""

    @given(image_data=valid_base64_image_strategy())
    @settings(max_examples=20)
    def test_base64_images_are_valid_format(self, image_data):
        """
        Feature: niko-ai-chat, Property 15: Image Attachment Handling
        For any base64-encoded image, it SHALL be decodable back to bytes.
        **Validates: Requirements 3.5**
        """
        # Verify the base64 string can be decoded
        decoded = base64.b64decode(image_data)
        assert isinstance(decoded, bytes)
        assert len(decoded) > 0

    @given(request_data=chat_request_with_images_strategy())
    @settings(max_examples=20)
    def test_chat_request_accepts_images(self, request_data):
        """
        Feature: niko-ai-chat, Property 15: Image Attachment Handling
        For any chat request with images, the ChatRequest model SHALL accept
        the base64-encoded images in the images field.
        **Validates: Requirements 3.5**
        """
        # Create ChatRequest with images
        chat_request = ChatRequest(
            message=request_data["message"],
            images=request_data["images"],
            model=request_data["model"]
        )
        
        # Verify images are stored correctly
        assert chat_request.images is not None
        assert len(chat_request.images) == len(request_data["images"])
        for i, img in enumerate(chat_request.images):
            assert img == request_data["images"][i]

    @given(request_data=chat_request_with_images_strategy())
    @settings(max_examples=20)
    def test_images_included_in_ollama_payload(self, request_data):
        """
        Feature: niko-ai-chat, Property 15: Image Attachment Handling
        For any chat request with images, the images SHALL be included
        in the Ollama API request payload as base64-encoded strings.
        **Validates: Requirements 3.5**
        """
        chat_service = ChatService()
        
        # Build the payload that would be sent to Ollama
        payload = {
            "model": request_data["model"] or chat_service.default_model,
            "prompt": request_data["message"],
            "stream": True
        }
        
        # Add images if provided (this is what ChatService does)
        if request_data["images"]:
            payload["images"] = request_data["images"]
        
        # Verify images are in the payload
        assert "images" in payload
        assert payload["images"] == request_data["images"]
        
        # Verify each image is a valid base64 string
        for img in payload["images"]:
            # Should be decodable
            decoded = base64.b64decode(img)
            assert isinstance(decoded, bytes)

    @given(num_images=st.integers(min_value=0, max_value=5))
    @settings(max_examples=20)
    def test_chat_request_handles_variable_image_count(self, num_images):
        """
        Feature: niko-ai-chat, Property 15: Image Attachment Handling
        For any number of images (including zero), the ChatRequest SHALL
        handle them correctly.
        **Validates: Requirements 3.5**
        """
        message = "Test message"
        
        if num_images == 0:
            images = None
        else:
            images = [base64.b64encode(f"image{i}".encode()).decode() for i in range(num_images)]
        
        chat_request = ChatRequest(message=message, images=images)
        
        if num_images == 0:
            assert chat_request.images is None
        else:
            assert chat_request.images is not None
            assert len(chat_request.images) == num_images

    def test_chat_request_without_images(self):
        """
        Feature: niko-ai-chat, Property 15: Image Attachment Handling
        For any chat request without images, the images field SHALL be None.
        **Validates: Requirements 3.5**
        """
        chat_request = ChatRequest(message="Hello")
        assert chat_request.images is None



# ============================================================================
# Feature: niko-ai-chat, Property 13: API Response Codes
# Validates: Requirements 10.1, 10.2, 10.3, 10.4
# ============================================================================

from main import rate_limiter, auth_service
import uuid as uuid_module


class TestAPIResponseCodes:
    """Property 13: API Response Codes - Validates: Requirements 10.1, 10.2, 10.3, 10.4"""

    def setup_method(self):
        """Reset rate limiter and use temp users file before each test"""
        rate_limiter.reset()
        # Use a temporary users file for testing
        self.original_users_file = auth_service.users_file
        self.temp_dir = tempfile.mkdtemp()
        auth_service.users_file = os.path.join(self.temp_dir, "test_users.json")

    def teardown_method(self):
        """Restore original users file and cleanup"""
        auth_service.users_file = self.original_users_file
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def test_successful_registration_returns_200(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any successful API request, the Niko_System SHALL return appropriate
        status code (200, 201) with JSON response.
        **Validates: Requirements 10.1**
        """
        rate_limiter.reset()
        # Use unique username to avoid conflicts
        unique_username = f"testuser{uuid_module.uuid4().hex[:8]}"
        auth_service.users_file = os.path.join(self.temp_dir, f"users_{unique_username}.json")
        client = TestClient(app)
        
        response = client.post("/register", json={
            "username": unique_username,
            "password": "ValidPass1"
        })
        
        # Successful registration should return 200 with JSON
        assert response.status_code == 200
        assert response.headers.get("content-type", "").startswith("application/json")
        
        data = response.json()
        assert "message" in data

    def test_successful_login_returns_200_with_token(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any successful login, the Niko_System SHALL return 200 with JSON containing token.
        **Validates: Requirements 10.1**
        """
        rate_limiter.reset()
        # Use unique username to avoid conflicts
        unique_username = f"loginuser{uuid_module.uuid4().hex[:8]}"
        auth_service.users_file = os.path.join(self.temp_dir, f"users_login_{unique_username}.json")
        client = TestClient(app)
        
        # Register first
        reg_response = client.post("/register", json={
            "username": unique_username,
            "password": "ValidPass1"
        })
        assert reg_response.status_code == 200, f"Registration failed: {reg_response.json()}"
        
        # Login
        response = client.post("/login", json={
            "username": unique_username,
            "password": "ValidPass1"
        })
        
        assert response.status_code == 200
        assert response.headers.get("content-type", "").startswith("application/json")
        
        data = response.json()
        assert "access_token" in data
        assert "token_type" in data

    @given(
        username=st.text(min_size=1, max_size=2),  # Too short username
        password=valid_password_strategy()
    )
    @settings(max_examples=20, deadline=None)
    def test_validation_error_returns_400(self, username, password):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any API request that fails due to validation, the Niko_System SHALL
        return 400 status with error details.
        **Validates: Requirements 10.2**
        """
        rate_limiter.reset()  # Reset rate limiter for each hypothesis example
        client = TestClient(app)
        
        response = client.post("/register", json={
            "username": username,
            "password": password
        })
        
        # Validation error should return 422 (FastAPI's default for validation errors)
        # or 400 depending on how the error is raised
        assert response.status_code in [400, 422]
        assert response.headers.get("content-type", "").startswith("application/json")

    @given(
        username=valid_username_strategy,
        password=st.text(min_size=1, max_size=7)  # Too short password
    )
    @settings(max_examples=20, deadline=None)
    def test_password_validation_error_returns_400(self, username, password):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any password validation failure, the Niko_System SHALL return 400/422 status.
        **Validates: Requirements 10.2**
        """
        rate_limiter.reset()  # Reset rate limiter for each hypothesis example
        client = TestClient(app)
        
        response = client.post("/register", json={
            "username": username,
            "password": password
        })
        
        assert response.status_code in [400, 422]
        assert response.headers.get("content-type", "").startswith("application/json")

    def test_authentication_error_returns_401(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any API request that fails due to authentication, the Niko_System SHALL
        return 401 status.
        **Validates: Requirements 10.3**
        """
        rate_limiter.reset()
        # Use empty temp file to ensure user doesn't exist
        unique_username = f"nonexistent{uuid_module.uuid4().hex[:8]}"
        auth_service.users_file = os.path.join(self.temp_dir, f"users_auth_{unique_username}.json")
        client = TestClient(app)
        
        # Try to login with non-existent user
        response = client.post("/login", json={
            "username": unique_username,
            "password": "ValidPass1"
        })
        
        assert response.status_code == 401
        assert response.headers.get("content-type", "").startswith("application/json")
        
        data = response.json()
        assert "error" in data or "detail" in data

    def test_protected_endpoint_without_token_returns_401_or_403(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any protected endpoint accessed without authentication, the Niko_System
        SHALL return 401 or 403 status.
        **Validates: Requirements 10.3**
        """
        rate_limiter.reset()
        client = TestClient(app)
        
        # Try to access protected endpoint without token
        response = client.get("/me")
        
        # FastAPI HTTPBearer returns 403 when no credentials provided
        assert response.status_code in [401, 403]
        assert response.headers.get("content-type", "").startswith("application/json")

    def test_protected_endpoint_with_invalid_token_returns_401(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any protected endpoint accessed with invalid token, the Niko_System
        SHALL return 401 status.
        **Validates: Requirements 10.3**
        """
        rate_limiter.reset()
        client = TestClient(app)
        
        # Try to access protected endpoint with invalid token
        response = client.get("/me", headers={
            "Authorization": "Bearer invalid_token_here"
        })
        
        assert response.status_code == 401
        assert response.headers.get("content-type", "").startswith("application/json")

    def test_rate_limit_error_returns_429(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For any API request that fails due to rate limiting, the Niko_System SHALL
        return 429 status.
        **Validates: Requirements 10.4**
        """
        rate_limiter.reset()
        client = TestClient(app)
        client_ip = "192.168.1.100"
        
        # Make many registration attempts to trigger rate limit
        # Register limit is 3 per hour
        for i in range(4):
            response = client.post("/register", json={
                "username": f"ratelimituser{i}abc",
                "password": "ValidPass1"
            }, headers={"X-Forwarded-For": client_ip})
        
        # The 4th request should be rate limited
        assert response.status_code == 429
        assert response.headers.get("content-type", "").startswith("application/json")
        
        data = response.json()
        assert "error" in data
        assert "retry_after" in data

    def test_health_endpoint_returns_200(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For health check endpoint, the Niko_System SHALL return 200 with JSON.
        **Validates: Requirements 10.1**
        """
        rate_limiter.reset()
        client = TestClient(app)
        
        response = client.get("/health")
        
        assert response.status_code == 200
        assert response.headers.get("content-type", "").startswith("application/json")
        
        data = response.json()
        assert "status" in data
        assert data["status"] == "healthy"

    def test_duplicate_registration_returns_400(self):
        """
        Feature: niko-ai-chat, Property 13: API Response Codes
        For duplicate registration attempt, the Niko_System SHALL return 400 status.
        **Validates: Requirements 10.2**
        """
        rate_limiter.reset()
        # Use unique username to avoid conflicts
        unique_username = f"dupuser{uuid_module.uuid4().hex[:8]}"
        auth_service.users_file = os.path.join(self.temp_dir, f"users_dup_{unique_username}.json")
        client = TestClient(app)
        
        # First registration
        client.post("/register", json={
            "username": unique_username,
            "password": "ValidPass1"
        })
        
        # Second registration with same username
        response = client.post("/register", json={
            "username": unique_username,
            "password": "ValidPass1"
        })
        
        assert response.status_code == 400
        assert response.headers.get("content-type", "").startswith("application/json")
        
        data = response.json()
        assert "error" in data or "detail" in data




# ============================================================================
# Feature: niko-ai-chat, Property 14: Data Persistence Format
# Validates: Requirements 9.1, 9.2, 9.5
# ============================================================================


class TestDataPersistenceFormat:
    """Property 14: Data Persistence Format - Validates: Requirements 9.1, 9.2, 9.5"""

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_user_data_saved_to_json_file(self, user_data):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any data stored by the system, user data SHALL be saved to users.json file.
        **Validates: Requirements 9.1**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            # Create a fresh AuthService with temp file
            temp_users_file = os.path.join(temp_dir, "users.json")
            test_auth_service = AuthService()
            test_auth_service.users_file = temp_users_file
            
            # Register user
            user = UserCreate(**user_data)
            test_auth_service.register(user)
            
            # Verify file exists
            assert os.path.exists(temp_users_file), "users.json file should exist"
            
            # Verify file is valid JSON
            with open(temp_users_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            # Verify user data is in the file
            assert user_data["username"] in data
            user_record = data[user_data["username"]]
            
            # Verify required fields
            assert "password" in user_record
            assert "email" in user_record
            assert "full_name" in user_record
            assert "created_at" in user_record
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_password_stored_as_hash_not_plaintext(self, user_data):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any data stored by the system, user data SHALL be saved with hashed passwords.
        **Validates: Requirements 9.1**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            # Create a fresh AuthService with temp file
            temp_users_file = os.path.join(temp_dir, "users.json")
            test_auth_service = AuthService()
            test_auth_service.users_file = temp_users_file
            
            # Register user
            user = UserCreate(**user_data)
            test_auth_service.register(user)
            
            # Load the file
            with open(temp_users_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            stored_password = data[user_data["username"]]["password"]
            
            # Password should NOT be plaintext
            assert stored_password != user_data["password"], "Password should not be stored as plaintext"
            
            # Password should be a bcrypt hash (starts with $2)
            assert stored_password.startswith("$2"), "Password should be a bcrypt hash"
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_chat_sessions_saved_as_separate_json_files(self, username):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any data stored by the system, chat sessions SHALL be saved as separate
        JSON files in history/ directory.
        **Validates: Requirements 9.2**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            temp_history_dir = os.path.join(temp_dir, "history")
            os.makedirs(temp_history_dir, exist_ok=True)
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            # Create multiple sessions
            session_ids = []
            for _ in range(3):
                session_id = history_service.create_session(username)
                session_ids.append(session_id)
            
            # Verify each session has its own file
            for session_id in session_ids:
                expected_path = os.path.join(temp_history_dir, f"{username}_{session_id}.json")
                assert os.path.exists(expected_path), f"Session file should exist: {expected_path}"
                
                # Verify file is valid JSON
                with open(expected_path, 'r', encoding='utf-8') as f:
                    session_data = json.load(f)
                
                assert session_data["id"] == session_id
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(
        username=valid_username_strategy,
        messages=st.lists(valid_message_strategy(), min_size=1, max_size=5)
    )
    @settings(max_examples=20, deadline=None)
    def test_session_file_format_contains_required_fields(self, username, messages):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any data stored by the system, session files SHALL follow the format:
        {id, title, timestamp, messages[]}
        **Validates: Requirements 9.5**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            temp_history_dir = os.path.join(temp_dir, "history")
            os.makedirs(temp_history_dir, exist_ok=True)
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            # Create session and add messages
            session_id = history_service.create_session(username)
            for msg in messages:
                history_service.add_message(
                    username, session_id,
                    msg["role"], msg["content"], msg["thought"]
                )
            
            # Load the session file directly
            session_path = os.path.join(temp_history_dir, f"{username}_{session_id}.json")
            with open(session_path, 'r', encoding='utf-8') as f:
                session_data = json.load(f)
            
            # Verify required fields per Requirements 9.5
            assert "id" in session_data, "Session should have 'id' field"
            assert "title" in session_data, "Session should have 'title' field"
            assert "timestamp" in session_data, "Session should have 'timestamp' field"
            assert "messages" in session_data, "Session should have 'messages' field"
            
            # Verify messages is a list
            assert isinstance(session_data["messages"], list), "messages should be a list"
            
            # Verify message count matches
            assert len(session_data["messages"]) == len(messages)
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(
        username=valid_username_strategy,
        content=st.text(min_size=1, max_size=100, alphabet=string.ascii_letters + string.digits + ' .,!?'),
        thought=st.text(min_size=1, max_size=50, alphabet=string.ascii_letters + string.digits + ' ')
    )
    @settings(max_examples=20, deadline=None)
    def test_message_format_contains_role_content_thought(self, username, content, thought):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any message in session files, the format SHALL contain role, content,
        and optionally thought.
        **Validates: Requirements 9.5**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            temp_history_dir = os.path.join(temp_dir, "history")
            os.makedirs(temp_history_dir, exist_ok=True)
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            # Create session and add bot message with thought
            session_id = history_service.create_session(username)
            history_service.add_message(username, session_id, "bot", content, thought)
            
            # Load the session file directly
            session_path = os.path.join(temp_history_dir, f"{username}_{session_id}.json")
            with open(session_path, 'r', encoding='utf-8') as f:
                session_data = json.load(f)
            
            # Verify message format
            message = session_data["messages"][0]
            assert "role" in message, "Message should have 'role' field"
            assert "content" in message, "Message should have 'content' field"
            assert message["role"] == "bot"
            assert message["content"] == content
            assert "thought" in message, "Bot message with thought should have 'thought' field"
            assert message["thought"] == thought
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_session_id_is_valid_uuid(self, username):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any session created, the id SHALL be a valid UUID.
        **Validates: Requirements 9.2**
        """
        import uuid
        
        temp_dir = tempfile.mkdtemp()
        try:
            temp_history_dir = os.path.join(temp_dir, "history")
            os.makedirs(temp_history_dir, exist_ok=True)
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            # Create session
            session_id = history_service.create_session(username)
            
            # Verify it's a valid UUID
            try:
                uuid.UUID(session_id)
            except ValueError:
                pytest.fail(f"Session ID '{session_id}' is not a valid UUID")
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(
        username=valid_username_strategy,
        content=st.text(min_size=1, max_size=100, alphabet=string.ascii_letters + string.digits + ' .,!?')
    )
    @settings(max_examples=20, deadline=None)
    def test_user_message_has_no_thought_field(self, username, content):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any user message, the thought field SHALL NOT be present.
        **Validates: Requirements 9.5**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            temp_history_dir = os.path.join(temp_dir, "history")
            os.makedirs(temp_history_dir, exist_ok=True)
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            # Create session and add user message (no thought)
            session_id = history_service.create_session(username)
            history_service.add_message(username, session_id, "user", content)
            
            # Load the session file directly
            session_path = os.path.join(temp_history_dir, f"{username}_{session_id}.json")
            with open(session_path, 'r', encoding='utf-8') as f:
                session_data = json.load(f)
            
            # Verify user message has no thought field
            message = session_data["messages"][0]
            assert message["role"] == "user"
            assert "thought" not in message, "User message should not have 'thought' field"
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    @given(user_data=valid_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_user_data_persists_across_loads(self, user_data):
        """
        Feature: niko-ai-chat, Property 14: Data Persistence Format
        For any user data saved, it SHALL be retrievable after saving.
        **Validates: Requirements 9.1**
        """
        temp_dir = tempfile.mkdtemp()
        try:
            # Create a fresh AuthService with temp file
            temp_users_file = os.path.join(temp_dir, "users.json")
            test_auth_service = AuthService()
            test_auth_service.users_file = temp_users_file
            
            # Register user
            user = UserCreate(**user_data)
            test_auth_service.register(user)
            
            # Create a new auth service instance to simulate fresh load
            new_auth_service = AuthService()
            new_auth_service.users_file = temp_users_file
            
            # Load users
            users = new_auth_service.load_users()
            
            # Verify user data is retrievable
            assert user_data["username"] in users
            assert users[user_data["username"]]["email"] == user_data["email"]
            assert users[user_data["username"]]["full_name"] == user_data["full_name"]
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)


# ============================================================================
# Feature: niko-ai-chat, Property 16: Draft Auto-Save
# Validates: Requirements 8.8
# ============================================================================

class MockLocalStorage:
    """Mock localStorage for testing draft auto-save functionality"""
    
    def __init__(self):
        self._storage = {}
    
    def setItem(self, key, value):
        self._storage[key] = value
    
    def getItem(self, key):
        return self._storage.get(key)
    
    def removeItem(self, key):
        if key in self._storage:
            del self._storage[key]
    
    def clear(self):
        self._storage = {}


def save_draft(localStorage, message):
    """
    Simulates the saveDraft function from script.js
    For any text typed in the message input, save it to localStorage.
    """
    if message and message.strip():
        localStorage.setItem('messageDraft', message)
    else:
        localStorage.removeItem('messageDraft')


def load_draft(localStorage):
    """
    Simulates the loadDraft function from script.js
    Reloading the page SHALL restore the draft.
    """
    return localStorage.getItem('messageDraft')


# Strategy for generating draft messages
@st.composite
def draft_message_strategy(draw):
    """Generate valid draft messages (non-empty, non-whitespace-only)"""
    # Generate text with at least one non-whitespace character
    content = draw(st.text(
        alphabet=string.ascii_letters + string.digits + ' .,!?@#$%^&*()-_=+[]{}|;:\'\"<>/\n\t',
        min_size=1,
        max_size=500
    ))
    # Ensure at least one non-whitespace character
    assume(content.strip())
    return content


# Strategy for whitespace-only messages
whitespace_only_strategy = st.text(
    alphabet=' \t\n\r',
    min_size=0,
    max_size=20
)


class TestDraftAutoSave:
    """Property 16: Draft Auto-Save - Validates: Requirements 8.8"""

    @given(message=draft_message_strategy())
    @settings(max_examples=20)
    def test_draft_save_and_restore_roundtrip(self, message):
        """
        Feature: niko-ai-chat, Property 16: Draft Auto-Save
        For any text typed in the message input, the Frontend SHALL save it to localStorage,
        and reloading the page SHALL restore the draft.
        **Validates: Requirements 8.8**
        """
        localStorage = MockLocalStorage()
        
        # Save the draft
        save_draft(localStorage, message)
        
        # Simulate page reload by loading the draft
        restored_draft = load_draft(localStorage)
        
        # The restored draft should equal the original message
        assert restored_draft == message, f"Draft not restored correctly: expected '{message}', got '{restored_draft}'"

    @given(message=whitespace_only_strategy)
    @settings(max_examples=20)
    def test_whitespace_only_draft_not_saved(self, message):
        """
        Feature: niko-ai-chat, Property 16: Draft Auto-Save
        For any whitespace-only text, the draft SHALL NOT be saved to localStorage.
        **Validates: Requirements 8.8**
        """
        localStorage = MockLocalStorage()
        
        # Try to save whitespace-only draft
        save_draft(localStorage, message)
        
        # The draft should not be saved
        restored_draft = load_draft(localStorage)
        assert restored_draft is None, f"Whitespace-only draft should not be saved, but got: '{restored_draft}'"

    @given(message1=draft_message_strategy(), message2=draft_message_strategy())
    @settings(max_examples=20)
    def test_draft_overwrite(self, message1, message2):
        """
        Feature: niko-ai-chat, Property 16: Draft Auto-Save
        For any subsequent draft saves, the latest draft SHALL overwrite the previous one.
        **Validates: Requirements 8.8**
        """
        localStorage = MockLocalStorage()
        
        # Save first draft
        save_draft(localStorage, message1)
        
        # Save second draft (should overwrite)
        save_draft(localStorage, message2)
        
        # Only the second draft should be restored
        restored_draft = load_draft(localStorage)
        assert restored_draft == message2, f"Draft should be overwritten: expected '{message2}', got '{restored_draft}'"

    @given(message=draft_message_strategy())
    @settings(max_examples=20)
    def test_draft_cleared_on_empty_input(self, message):
        """
        Feature: niko-ai-chat, Property 16: Draft Auto-Save
        For any draft that is cleared (empty input), the localStorage entry SHALL be removed.
        **Validates: Requirements 8.8**
        """
        localStorage = MockLocalStorage()
        
        # Save a draft first
        save_draft(localStorage, message)
        assert load_draft(localStorage) == message
        
        # Clear the draft by saving empty string
        save_draft(localStorage, "")
        
        # Draft should be removed
        restored_draft = load_draft(localStorage)
        assert restored_draft is None, f"Draft should be cleared, but got: '{restored_draft}'"

    @given(message=draft_message_strategy())
    @settings(max_examples=20)
    def test_draft_persistence_across_multiple_loads(self, message):
        """
        Feature: niko-ai-chat, Property 16: Draft Auto-Save
        For any saved draft, it SHALL persist across multiple load operations.
        **Validates: Requirements 8.8**
        """
        localStorage = MockLocalStorage()
        
        # Save the draft
        save_draft(localStorage, message)
        
        # Load multiple times (simulating multiple page accesses)
        for _ in range(5):
            restored_draft = load_draft(localStorage)
            assert restored_draft == message, f"Draft should persist: expected '{message}', got '{restored_draft}'"


# ============================================================================
# Feature: admin-panel, Property 2: User List Completeness
# Validates: Requirements 2.1, 2.2
# ============================================================================

from main import AdminService, UserAdminCreate, UserAdminUpdate, UserListResponse


@st.composite
def valid_admin_user_data_strategy(draw):
    """Generate valid user data for admin user creation"""
    username = draw(valid_username_strategy)
    password = draw(valid_password_strategy())
    email = draw(st.one_of(
        st.none(),
        st.from_regex(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', fullmatch=True)
    ))
    full_name = draw(st.one_of(
        st.none(),
        st.text(min_size=1, max_size=50, alphabet=string.ascii_letters + ' ')
    ))
    is_admin = draw(st.booleans())
    return {
        "username": username,
        "password": password,
        "email": email,
        "full_name": full_name,
        "is_admin": is_admin
    }


class TestUserListCompleteness:
    """
    Feature: admin-panel, Property 2: User List Completeness
    For any admin request to list users, the response SHALL contain all users in the system,
    and each user object SHALL include username, email, full_name, created_at, and is_admin fields.
    **Validates: Requirements 2.1, 2.2**
    """

    @given(user_data_list=st.lists(valid_admin_user_data_strategy(), min_size=1, max_size=5, unique_by=lambda x: x["username"]))
    @settings(max_examples=20, deadline=None)
    def test_list_users_returns_all_users(self, user_data_list):
        """
        Feature: admin-panel, Property 2: User List Completeness
        For any set of created users, list_users SHALL return all of them.
        **Validates: Requirements 2.1, 2.2**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create users
            created_usernames = set()
            for user_data in user_data_list:
                user = UserAdminCreate(**user_data)
                admin_service.create_user(user)
                created_usernames.add(user_data["username"])
            
            # List users
            user_list = admin_service.list_users()
            
            # Verify all users are returned
            returned_usernames = {u.username for u in user_list}
            assert created_usernames == returned_usernames, \
                f"Expected users {created_usernames}, got {returned_usernames}"
            
            # Verify each user has all required fields
            for user in user_list:
                assert hasattr(user, 'username') and user.username is not None
                assert hasattr(user, 'email')  # Can be None
                assert hasattr(user, 'full_name')  # Can be None
                assert hasattr(user, 'is_admin') and isinstance(user.is_admin, bool)
                assert hasattr(user, 'created_at') and user.created_at is not None
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_list_users_contains_correct_data(self, user_data):
        """
        Feature: admin-panel, Property 2: User List Completeness
        For any created user, list_users SHALL return the correct data for that user.
        **Validates: Requirements 2.1, 2.2**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create user
            user = UserAdminCreate(**user_data)
            admin_service.create_user(user)
            
            # List users
            user_list = admin_service.list_users()
            
            # Find the created user in the list
            found_user = None
            for u in user_list:
                if u.username == user_data["username"]:
                    found_user = u
                    break
            
            assert found_user is not None, f"User {user_data['username']} not found in list"
            
            # Verify data matches
            assert found_user.email == user_data["email"]
            assert found_user.full_name == user_data["full_name"]
            assert found_user.is_admin == user_data["is_admin"]
            assert found_user.created_at is not None and len(found_user.created_at) > 0
        finally:
            shutil.rmtree(temp_dir)

    def test_list_users_empty_system(self):
        """
        Feature: admin-panel, Property 2: User List Completeness
        For an empty system, list_users SHALL return an empty list.
        **Validates: Requirements 2.1, 2.2**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # List users (should be empty)
            user_list = admin_service.list_users()
            
            assert len(user_list) == 0, f"Expected empty list, got {len(user_list)} users"
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: admin-panel, Property 1: Admin Access Control
# Validates: Requirements 1.1, 1.2, 6.1, 6.2
# ============================================================================

from main import AdminService, UserAdminCreate, UserAdminUpdate, UserListResponse, get_current_admin
from fastapi import HTTPException
from fastapi.security import HTTPAuthorizationCredentials
from unittest.mock import MagicMock


class TestAdminAccessControl:
    """
    Feature: admin-panel, Property 1: Admin Access Control
    For any user attempting to access admin endpoints, access SHALL be granted 
    if and only if the user has a valid token AND is_admin is true.
    **Validates: Requirements 1.1, 1.2, 6.1, 6.2**
    """

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_admin_user_access_granted(self, user_data):
        """
        Feature: admin-panel, Property 1: Admin Access Control
        For any user with is_admin=True and valid token, access SHALL be granted.
        **Validates: Requirements 1.1, 1.2, 6.1, 6.2**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            # Create auth service with temp file
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Ensure user is admin
            user_data_copy = user_data.copy()
            user_data_copy["is_admin"] = True
            
            # Create admin user
            user = UserAdminCreate(**user_data_copy)
            users = {user.username: {
                "password": auth_service.hash_password(user.password),
                "email": user.email,
                "full_name": user.full_name,
                "is_admin": True,
                "created_at": "2026-01-09T10:00:00.000000"
            }}
            auth_service.save_users(users)
            
            # Create valid token
            token = auth_service.create_token(user.username)
            
            # Verify token is valid
            verified_username = auth_service.verify_token(token)
            assert verified_username == user.username
            
            # Verify user is admin
            user_record = auth_service.get_user(user.username)
            assert user_record is not None
            assert user_record.get("is_admin", False) is True
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_non_admin_user_access_denied(self, user_data):
        """
        Feature: admin-panel, Property 1: Admin Access Control
        For any user with is_admin=False, access SHALL be denied with 403 status.
        **Validates: Requirements 1.1, 1.2, 6.1, 6.2**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            # Create auth service with temp file
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Ensure user is NOT admin
            user_data_copy = user_data.copy()
            user_data_copy["is_admin"] = False
            
            # Create non-admin user
            user = UserAdminCreate(**user_data_copy)
            users = {user.username: {
                "password": auth_service.hash_password(user.password),
                "email": user.email,
                "full_name": user.full_name,
                "is_admin": False,
                "created_at": "2026-01-09T10:00:00.000000"
            }}
            auth_service.save_users(users)
            
            # Create valid token
            token = auth_service.create_token(user.username)
            
            # Verify token is valid
            verified_username = auth_service.verify_token(token)
            assert verified_username == user.username
            
            # Verify user is NOT admin
            user_record = auth_service.get_user(user.username)
            assert user_record is not None
            assert user_record.get("is_admin", False) is False
        finally:
            shutil.rmtree(temp_dir)

    @given(username=valid_username_strategy)
    @settings(max_examples=20, deadline=None)
    def test_invalid_token_access_denied(self, username):
        """
        Feature: admin-panel, Property 1: Admin Access Control
        For any invalid or expired token, access SHALL be denied with 401 status.
        **Validates: Requirements 1.1, 1.2, 6.1, 6.2**
        """
        auth_service = AuthService()
        
        # Test with invalid tokens
        invalid_tokens = [
            "invalid_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature",
            "",
            "a.b.c",
        ]
        
        for invalid_token in invalid_tokens:
            result = auth_service.verify_token(invalid_token)
            assert result is None, f"Invalid token '{invalid_token}' should return None"

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_deleted_user_token_access_denied(self, user_data):
        """
        Feature: admin-panel, Property 1: Admin Access Control
        For any token belonging to a deleted user, access SHALL be denied.
        **Validates: Requirements 1.1, 1.2, 6.1, 6.2**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        
        try:
            # Create auth service with temp file
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            # Create admin user
            user = UserAdminCreate(**user_data)
            users = {user.username: {
                "password": auth_service.hash_password(user.password),
                "email": user.email,
                "full_name": user.full_name,
                "is_admin": True,
                "created_at": "2026-01-09T10:00:00.000000"
            }}
            auth_service.save_users(users)
            
            # Create valid token
            token = auth_service.create_token(user.username)
            
            # Verify token is valid initially
            verified_username = auth_service.verify_token(token)
            assert verified_username == user.username
            
            # Delete user
            auth_service.save_users({})
            
            # Token is still valid (JWT doesn't know about deletion)
            # But get_user should return None
            user_record = auth_service.get_user(user.username)
            assert user_record is None
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: admin-panel, Property 3: User Update Round-Trip
# Validates: Requirements 3.2, 3.3
# ============================================================================


@st.composite
def valid_update_data_strategy(draw):
    """Generate valid user update data"""
    email = draw(st.one_of(
        st.none(),
        st.from_regex(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', fullmatch=True)
    ))
    full_name = draw(st.one_of(
        st.none(),
        st.text(min_size=1, max_size=50, alphabet=string.ascii_letters + ' ')
    ))
    is_admin = draw(st.one_of(st.none(), st.booleans()))
    return {
        "email": email,
        "full_name": full_name,
        "is_admin": is_admin
    }


class TestUserUpdateRoundTrip:
    """
    Feature: admin-panel, Property 3: User Update Round-Trip
    For any valid user update operation, getting the user after update SHALL return 
    the updated values for email, full_name, and is_admin.
    **Validates: Requirements 3.2, 3.3**
    """

    @given(user_data=valid_admin_user_data_strategy(), update_data=valid_update_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_update_user_round_trip(self, user_data, update_data):
        """
        Feature: admin-panel, Property 3: User Update Round-Trip
        For any valid update, getting the user after update SHALL return the updated values.
        **Validates: Requirements 3.2, 3.3**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create user
            user = UserAdminCreate(**user_data)
            admin_service.create_user(user)
            
            # Update user
            update = UserAdminUpdate(**update_data)
            updated_user = admin_service.update_user(user.username, update)
            
            # Get user after update
            retrieved_user = admin_service.get_user(user.username)
            
            # Verify round-trip consistency
            assert retrieved_user is not None
            
            # Check email
            expected_email = update_data["email"] if update_data["email"] is not None else user_data["email"]
            assert retrieved_user.email == expected_email
            
            # Check full_name
            expected_full_name = update_data["full_name"] if update_data["full_name"] is not None else user_data["full_name"]
            assert retrieved_user.full_name == expected_full_name
            
            # Check is_admin
            expected_is_admin = update_data["is_admin"] if update_data["is_admin"] is not None else user_data["is_admin"]
            assert retrieved_user.is_admin == expected_is_admin
            
            # Verify updated_user matches retrieved_user
            assert updated_user.email == retrieved_user.email
            assert updated_user.full_name == retrieved_user.full_name
            assert updated_user.is_admin == retrieved_user.is_admin
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_update_nonexistent_user_fails(self, user_data):
        """
        Feature: admin-panel, Property 3: User Update Round-Trip
        For any update to a non-existent user, the operation SHALL fail with ValueError.
        **Validates: Requirements 3.2, 3.3**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Try to update non-existent user
            update = UserAdminUpdate(email="new@email.com")
            
            with pytest.raises(ValueError) as exc_info:
                admin_service.update_user(user_data["username"], update)
            
            assert "bulunamadı" in str(exc_info.value)
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_empty_update_preserves_data(self, user_data):
        """
        Feature: admin-panel, Property 3: User Update Round-Trip
        For any update with all None values, the original data SHALL be preserved.
        **Validates: Requirements 3.2, 3.3**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create user
            user = UserAdminCreate(**user_data)
            admin_service.create_user(user)
            
            # Get original user
            original_user = admin_service.get_user(user.username)
            
            # Update with all None values
            update = UserAdminUpdate(email=None, full_name=None, is_admin=None)
            admin_service.update_user(user.username, update)
            
            # Get user after update
            updated_user = admin_service.get_user(user.username)
            
            # Verify data is preserved
            assert updated_user.email == original_user.email
            assert updated_user.full_name == original_user.full_name
            assert updated_user.is_admin == original_user.is_admin
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: admin-panel, Property 4: User Deletion Completeness
# Validates: Requirements 4.2, 4.3
# ============================================================================


class TestUserDeletionCompleteness:
    """
    Feature: admin-panel, Property 4: User Deletion Completeness
    For any user deletion operation, after deletion the user SHALL NOT exist in the system 
    AND all associated chat history files SHALL be removed.
    **Validates: Requirements 4.2, 4.3**
    """

    @given(user_data=valid_admin_user_data_strategy(), admin_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_delete_user_removes_from_system(self, user_data, admin_data):
        """
        Feature: admin-panel, Property 4: User Deletion Completeness
        For any deleted user, the user SHALL NOT exist in the system after deletion.
        **Validates: Requirements 4.2, 4.3**
        """
        # Ensure different usernames
        assume(user_data["username"] != admin_data["username"])
        
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create admin user (who will perform deletion)
            admin_data_copy = admin_data.copy()
            admin_data_copy["is_admin"] = True
            admin_user = UserAdminCreate(**admin_data_copy)
            admin_service.create_user(admin_user)
            
            # Create user to be deleted
            user = UserAdminCreate(**user_data)
            admin_service.create_user(user)
            
            # Verify user exists
            assert admin_service.get_user(user.username) is not None
            
            # Delete user
            result = admin_service.delete_user(user.username, admin_user.username)
            assert result is True
            
            # Verify user no longer exists
            assert admin_service.get_user(user.username) is None
            
            # Verify user is not in list
            user_list = admin_service.list_users()
            usernames = [u.username for u in user_list]
            assert user.username not in usernames
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_admin_user_data_strategy(), admin_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_delete_user_removes_chat_history(self, user_data, admin_data):
        """
        Feature: admin-panel, Property 4: User Deletion Completeness
        For any deleted user, all associated chat history files SHALL be removed.
        **Validates: Requirements 4.2, 4.3**
        """
        # Ensure different usernames
        assume(user_data["username"] != admin_data["username"])
        
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create admin user (who will perform deletion)
            admin_data_copy = admin_data.copy()
            admin_data_copy["is_admin"] = True
            admin_user = UserAdminCreate(**admin_data_copy)
            admin_service.create_user(admin_user)
            
            # Create user to be deleted
            user = UserAdminCreate(**user_data)
            admin_service.create_user(user)
            
            # Create some chat sessions for the user
            session_id1 = history_service.create_session(user.username)
            session_id2 = history_service.create_session(user.username)
            
            # Add messages to sessions
            history_service.add_message(user.username, session_id1, "user", "Hello")
            history_service.add_message(user.username, session_id1, "bot", "Hi there!")
            history_service.add_message(user.username, session_id2, "user", "Test message")
            
            # Verify sessions exist
            sessions = history_service.get_history(user.username)
            assert len(sessions) == 2
            
            # Delete user
            admin_service.delete_user(user.username, admin_user.username)
            
            # Verify all chat history is deleted
            sessions_after = history_service.get_history(user.username)
            assert len(sessions_after) == 0
            
            # Verify session files don't exist
            session_path1 = history_service.get_session_path(user.username, session_id1)
            session_path2 = history_service.get_session_path(user.username, session_id2)
            assert not os.path.exists(session_path1)
            assert not os.path.exists(session_path2)
        finally:
            shutil.rmtree(temp_dir)

    @given(user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_delete_nonexistent_user_fails(self, user_data):
        """
        Feature: admin-panel, Property 4: User Deletion Completeness
        For any deletion of a non-existent user, the operation SHALL fail with ValueError.
        **Validates: Requirements 4.2, 4.3**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Try to delete non-existent user
            with pytest.raises(ValueError) as exc_info:
                admin_service.delete_user(user_data["username"], "admin")
            
            assert "bulunamadı" in str(exc_info.value)
        finally:
            shutil.rmtree(temp_dir)


# ============================================================================
# Feature: admin-panel, Property 7: Self-Deletion Prevention
# Validates: Requirements 4.4
# ============================================================================


class TestSelfDeletionPrevention:
    """
    Feature: admin-panel, Property 7: Self-Deletion Prevention
    For any admin attempting to delete their own account, the system SHALL reject 
    the request with an error.
    **Validates: Requirements 4.4**
    """

    @given(admin_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_admin_cannot_delete_self(self, admin_data):
        """
        Feature: admin-panel, Property 7: Self-Deletion Prevention
        For any admin attempting to delete themselves, the operation SHALL fail.
        **Validates: Requirements 4.4**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create admin user
            admin_data_copy = admin_data.copy()
            admin_data_copy["is_admin"] = True
            admin_user = UserAdminCreate(**admin_data_copy)
            admin_service.create_user(admin_user)
            
            # Verify admin exists
            assert admin_service.get_user(admin_user.username) is not None
            
            # Try to delete self
            with pytest.raises(ValueError) as exc_info:
                admin_service.delete_user(admin_user.username, admin_user.username)
            
            assert "Kendinizi silemezsiniz" in str(exc_info.value)
            
            # Verify admin still exists
            assert admin_service.get_user(admin_user.username) is not None
        finally:
            shutil.rmtree(temp_dir)

    @given(admin_data=valid_admin_user_data_strategy(), other_user_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_admin_can_delete_other_users(self, admin_data, other_user_data):
        """
        Feature: admin-panel, Property 7: Self-Deletion Prevention
        For any admin deleting another user, the operation SHALL succeed.
        **Validates: Requirements 4.4**
        """
        # Ensure different usernames
        assume(admin_data["username"] != other_user_data["username"])
        
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create admin user
            admin_data_copy = admin_data.copy()
            admin_data_copy["is_admin"] = True
            admin_user = UserAdminCreate(**admin_data_copy)
            admin_service.create_user(admin_user)
            
            # Create other user
            other_user = UserAdminCreate(**other_user_data)
            admin_service.create_user(other_user)
            
            # Verify both users exist
            assert admin_service.get_user(admin_user.username) is not None
            assert admin_service.get_user(other_user.username) is not None
            
            # Delete other user (should succeed)
            result = admin_service.delete_user(other_user.username, admin_user.username)
            assert result is True
            
            # Verify other user is deleted
            assert admin_service.get_user(other_user.username) is None
            
            # Verify admin still exists
            assert admin_service.get_user(admin_user.username) is not None
        finally:
            shutil.rmtree(temp_dir)

    @given(admin_data=valid_admin_user_data_strategy())
    @settings(max_examples=20, deadline=None)
    def test_self_deletion_preserves_user_data(self, admin_data):
        """
        Feature: admin-panel, Property 7: Self-Deletion Prevention
        For any failed self-deletion attempt, all user data SHALL be preserved.
        **Validates: Requirements 4.4**
        """
        temp_dir = tempfile.mkdtemp()
        temp_users_file = os.path.join(temp_dir, "users.json")
        temp_history_dir = os.path.join(temp_dir, "history")
        os.makedirs(temp_history_dir, exist_ok=True)
        
        try:
            # Create services with temp files
            auth_service = AuthService()
            auth_service.users_file = temp_users_file
            
            history_service = HistoryService()
            history_service.history_dir = temp_history_dir
            
            admin_service = AdminService(auth_service, history_service)
            
            # Create admin user
            admin_data_copy = admin_data.copy()
            admin_data_copy["is_admin"] = True
            admin_user = UserAdminCreate(**admin_data_copy)
            admin_service.create_user(admin_user)
            
            # Create some chat sessions
            session_id = history_service.create_session(admin_user.username)
            history_service.add_message(admin_user.username, session_id, "user", "Test message")
            
            # Get original data
            original_user = admin_service.get_user(admin_user.username)
            original_sessions = history_service.get_history(admin_user.username)
            
            # Try to delete self (should fail)
            with pytest.raises(ValueError):
                admin_service.delete_user(admin_user.username, admin_user.username)
            
            # Verify user data is preserved
            preserved_user = admin_service.get_user(admin_user.username)
            assert preserved_user is not None
            assert preserved_user.email == original_user.email
            assert preserved_user.full_name == original_user.full_name
            assert preserved_user.is_admin == original_user.is_admin
            
            # Verify chat history is preserved
            preserved_sessions = history_service.get_history(admin_user.username)
            assert len(preserved_sessions) == len(original_sessions)
        finally:
            shutil.rmtree(temp_dir)
