// --- Paket Tanımı: Uygulamanın merkezi kimliği ---
package com.example.niko;

// --- Android Çekirdek Bileşenleri: Uygulama yapısı ve sistem izinleri ---
import android.Manifest; // Uygulama izin yönetimi
import android.app.Activity; // Temel ekran yapısı
import android.app.Notification; // Sistem bildirimleri
import android.app.PendingIntent; // Gelecekte yürütülecek niyetler
import android.app.RemoteInput; // Uzaktan giriş (bildirim yanıtları vb.)
import android.content.ContentUris; // İçerik URI yönetimi
import android.content.Intent; // Ekranlar arası geçiş ve mesajlaşma
import android.content.pm.PackageManager; // Paket ve izin kontrolü
import android.content.IntentFilter; // Sistem olaylarını filtreleme
import android.content.BroadcastReceiver; // Yayın alıcısı
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.database.Cursor; // Veritabanı sorgu sonuçları
import android.net.Uri; // Kaynak belirleyiciler (dosya/web yolları)
import android.os.Bundle; // Ekran geçişlerinde veri taşıma
import android.os.Handler; // İş parçacıkları arası mesajlaşma
import android.os.Looper; // Mesaj döngüsü yönetimi
import android.net.wifi.WifiManager; // Wi-Fi yönetimi
import android.bluetooth.BluetoothAdapter; // Bluetooth yönetimi
import android.provider.Settings; // Sistem ayarları erişimi
import android.provider.ContactsContract; // Rehber verileri erişimi
import android.provider.CallLog; // Arama kaydı erişimi (Eksik olan buydu)
import android.provider.MediaStore; // Medya erişimi (Görsel seçimi ve kamera için)
import android.provider.CalendarContract; // Takvim erişimi (Hatırlatıcılar için)
import android.speech.RecognitionListener; // Konuşma tanıma dinleyicisi
import android.speech.RecognizerIntent; // Ses tanıma başlatma niyeti
import android.speech.SpeechRecognizer; // Ses tanıma motoru
import android.speech.tts.TextToSpeech; // Metni sese dönüştürme motoru
import android.speech.tts.UtteranceProgressListener; // Okuma süreci takibi

// --- Kullanıcı Arayüzü (UI) Temel Bileşenleri ---
import android.view.View; // Temel görsel yapı taşı
import android.widget.ImageButton; // Resimli buton
import android.widget.TextView; // Metin görüntüleme alanı
import android.widget.ImageView; // Resim görüntüleme alanı
import android.widget.RelativeLayout; // Esnek yerleşim düzeni
import android.widget.Toast; // Kısa süreli mesaj bildirimleri
import android.widget.LinearLayout; // Sıralı yerleşim düzeni
import android.widget.Button; // Standart tıklanabilir buton
import android.widget.EditText; // Metin giriş alanı

import android.media.MediaPlayer; // Ses/Video oynatma
import android.media.AudioManager; // Sistem ses kontrolleri
import android.view.KeyEvent; // Tuş olaylarını yakalama
import android.util.Base64; // Veri şifreleme/çözme (Base64)
import android.os.Build; // Cihaz donanım ve sürüm bilgisi
import android.os.BatteryManager; // Pil durumu yönetimi
import android.os.Environment; // Dosya sistemi erişimi
import java.util.List; // Liste arayüzü
import android.content.pm.ResolveInfo; // Çözümleme bilgisi
import java.io.ByteArrayOutputStream; // Bellek içi veri akışı

// --- Veri Giriş/Çıkış (I/O) ve Dosya İşlemleri ---
import java.io.File; // Dosya nesnesi
import java.io.FileOutputStream; // Dosya yazma akışı
import java.io.InputStream; // Veri okuma akışı
import java.io.OutputStream; // Veri yazma akışı
import java.io.BufferedReader; // Metin okuyucu
import java.io.InputStreamReader; // Akıştan okuyucuya dönüştürücü
import java.io.DataOutputStream; // Ham veri yazma akışı
import java.io.IOException; // Giriş/Çıkış hata yönetimi

// --- Veri Yapıları ve JSON İşleme ---
import org.json.JSONArray; // JSON dizi yapısı
import org.json.JSONObject; // JSON nesne yapısı
import java.util.ArrayList; // Dinamik dizi listesi
import java.util.LinkedList; // Bağlı liste yapısı
import java.util.Queue; // Kuyruk veri yapısı
import java.util.UUID; // Benzersiz kimlik oluşturucu

import java.net.HttpURLConnection; // HTTP bağlantı yönetimi
import java.net.URL; // Web adresi nesnesi
import java.net.URLEncoder; // URL karakter kodlama
import android.net.ConnectivityManager; // İnternet bağlantı kontrolü
import android.net.NetworkInfo; // Ağ detayları

import java.util.Date; // Tarih nesnesi
import java.text.SimpleDateFormat; // Tarih formatlama
import java.util.Calendar; // Takvim işlemleri
import android.provider.AlarmClock; // Alarm sistemi erişimi
import java.util.concurrent.ExecutorService; // İş parçacığı havuzu yönetimi
import java.util.concurrent.Executors; // İş parçacığı oluşturucu
import java.util.concurrent.atomic.AtomicInteger; // Güvenli tamsayı işlemleri
import java.util.concurrent.TimeUnit; // Zaman birimleri dönüşümü
import android.os.AsyncTask; // Arka plan görevleri
import java.util.Locale; // Dil ve bölge ayarları

// --- Grafik, Animasyon ve Metin Stil İşlemleri ---
import android.graphics.Color; // Renk yönetimi
import android.graphics.Bitmap; // Dijital resim verisi
import android.graphics.BitmapFactory; // Resim çözümleyici
import android.graphics.Matrix; // Resim döndürme/boyutlandırma matrisi
import android.text.TextWatcher; // Metin değişim dinleyicisi
import android.text.Editable; // Düzenlenebilir metin nesnesi
import android.text.SpannableString; // Stil verilebilen metin
import android.text.style.BackgroundColorSpan; // Metin arka plan rengi stili
import android.text.Spanned; // Biçimlendirilmiş metin arayüzü
import android.view.animation.Animation; // Temel animasyon sınıfı
import android.view.animation.TranslateAnimation; // Kaydırma animasyonu
import android.view.animation.AlphaAnimation; // Şeffaflık animasyonu
import android.view.animation.AnimationSet; // Çoklu animasyon grubu
import android.view.animation.ScaleAnimation; // Boyutlandırma animasyonu
import android.view.animation.AccelerateDecelerateInterpolator; // Yumuşak geçişli animasyon eğrisi

// --- Diğer Sistem Bileşenleri ve Yardımcı Sınıflar ---
import android.content.Context; // Uygulama bağlamı
import android.content.SharedPreferences; // Yerel küçük veri depolama
import android.view.Window; // Ekran penceresi yönetimi
import android.view.WindowManager; // Pencere yöneticisi özellikleri
import android.view.ViewGroup; // Görsel grup sarmalayıcı
import android.content.ClipboardManager; // Pano (Kopyala/Yapıştır) yönetimi
import android.content.ClipData; // Pano veri yapısı
import android.view.WindowInsets; // Ekran içi boşluklar (çentik vb.)
import android.view.inputmethod.InputMethodManager; // Klavye yönetimi
import androidx.core.content.FileProvider; // Dosya paylaşım sağlayıcısı
import android.hardware.camera2.CameraManager; // Kamera servisi
import android.hardware.camera2.CameraCharacteristics; // Kamera teknik özellikleri
import java.util.regex.Matcher; // Düzenli ifade eşleştirici (Regex)
import java.util.regex.Pattern; // Düzenli ifade kalıbı (Regex)
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;



/* *********************************************************************************
 * Niko Mobil Asistan'ın merkezi aktivite sınıfı. Bu sınıf; ses tanıma, 
 * yapay zeka entegrasyonu, cihaz kontrolü ve kullanıcı yönetimi gibi 
 * tüm çekirdek işlevlerin orkestrasyonunu sağlar.
 * *********************************************************************************/
public class MainActivity extends Activity {

    // --- Singleton ve Global Durum ---
    
    /** Arka plan servislerinin erişimi için statik örnek */
    private static MainActivity instance;

    // --- Animasyon Mantığı ve Kimlik Belirleyiciler ---
    
    /** Aktif animasyonları yöneten önbellek yapısı */
    private final android.util.SparseArray<android.animation.Animator> activeAnimations = new android.util.SparseArray<>();
    
    private static final int ANIM_ACCOUNT_ENTRY = 1;
    private static final int ANIM_VERIFICATION_BG = 2;
    private static final int ANIM_MODEL_GLOW = 3;

    // --- Yapay Zeka Motoru ve Performans Optimizasyonu ---
    
    /** Paralel işlemler ve performans yönetimi için Thread Havuzu */
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    /** İptal edilebilir aktif AI görevi referansı */
    private java.util.concurrent.Future<?> currentAiTask;

    // --- Sabitler ---
    
    /** Çalışma zamanı izin talebi kodu */
    private static final int PERMISSION_CODE = 100;

    // --- UI Bileşenleri: Çekirdek ---
    
    private View voiceOrb;              // Ses aktivitesini simgeleyen görsel element
    private ImageButton btnMic;        // Birincil etkileşim (mikrofon) butonu
    private TextView txtAIResponse;     // AI yanıtlarının görüntülendiği metin alanı
    private View aiResponseContainer;   // Yanıt metni için sarmalayıcı (ScrollView)

    // --- Ses ve TTS (Metin Okuma) Motoru ---
    
    private SpeechRecognizer speechRecognizer; 
    private Intent speechIntent;
    private TextToSpeech tts;

    // --- Durum ve Kontrol Akışı ---
    
    /** Mikrofonun aktif dinleme durumunu takip eder */
    private boolean isListening = false;
    
    /** Metin okuma sırasını yöneten kuyruk yapısı */
    private final Queue<String> ttsQueue = new LinkedList<>();

    // --- UI Bileşenleri: Geçmiş Paneli ---
    
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
    
    // --- İstatistik Paneli ---
    
    private TextView txtStatTotalChats;
    private TextView txtStatThisWeek;
    private TextView txtStatToday;
    private View layoutHistoryStats;
    private View cardStatTotal, cardStatWeekly, cardStatToday;
    
    private final Object historyLock = new Object();
    
    /** Geçmişte tutulacak maksimum kayıt sınırı */
    private static final int MAX_HISTORY_ITEMS = 100;
    
    // --- Oturum ve Model Yapılandırması ---
    
    /** Mevcut AI konuşma oturumu kimliği */
    private String sessionId = null;
    
    private SharedPreferences sessionPrefs;
    private SharedPreferences modelPrefs;
    
    /** Aktif yapay zeka modeli */
    private String selectedModel = null;

    // --- Arama ve Etkileşim Modları ---
    
    private boolean isWebSearchEnabled = false;
    private ImageButton btnWebSearch;
    private ImageButton btnStop;
    private SharedPreferences searchPrefs;

    // --- Model Seçimi Arayüzü ---
    
    private ImageButton btnModel;
    private View layoutModels;
    private ImageButton btnCloseModels;
    private LinearLayout containerModelItems;
    private TextView txtCurrentModel;
    private TextView txtMainActiveModel;

    // --- Yapılandırma: Gizli Modeller ---
    
    /** Mobil arayüzde gösterilmeyecek teknik AI modelleri */
    private static final String[] HIDDEN_MODELS = {
            "translategemma:latest",
            "llama3.2-vision:11b",
            "necdetuygur/developer:latest",
            "nomic-embed-text:latest",
            "codegemma:7b",
            "qwen2.5-coder:7b",
            "alibayram/kumru:latest"
    };

    // --- Hesap ve Profil Yönetimi ---
    
    private ImageView imgTopProfile, imgMainProfile;
    private View layoutAccount;
    private ImageButton btnCloseAccount;
    private TextView txtAccountTitle;
    private EditText edtUsername, edtPassword, edtEmail, edtFullName;
    private View layoutRegisterExtras, layoutAccountFields;
    
    // --- Doğrulama Sistemi ---
    
    private View layoutVerification;
    private EditText edtVerifyCode;
    private Button btnVerifyCode;
    private TextView btnResendCode, btnCancelVerification;
    private Button btnSubmitAccount;
    private TextView btnSwitchMode;
    private View layoutLoggedIn, layoutAvatarSelection;
    private TextView txtLoginStatus;
    private Button btnLogout, btnEditProfile, btnDeleteAccount;
    
    // --- Kullanıcı Profil Varlıkları ---
    
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

    // --- WhatsApp İzleme ve Yanıtlama Durumu ---
    private String lastWhatsAppMessage = null;
    private String lastWhatsAppSender = null;
    private PendingIntent lastReplyIntent = null;
    private RemoteInput lastRemoteInput = null;

    // --- Geçici Durum (Kayıt) ---
    
    private String pendingUsername;
    private String pendingPassword;
    private String pendingEmail;
    private String pendingFullName;


    
    // --- Yönetim ve Loglama ---
    
    private View layoutAdminLogs;
    private TextView txtAdminLogs;
    private ImageButton btnCloseLogs;
    private Button btnCopyLogs, btnClearLogs, btnShowLogs;
    
    private final StringBuilder appLogsBuffer = new StringBuilder();
    
    /** Log bellek sınırı (karakter cinsinden) */
    private final int MAX_LOG_SIZE = 50000;



    // --- Ağ ve Güncellemeler ---
    
    /** Merkezi API sunucu adresi */
    private static String API_BASE_URL = "";

    private static final String GITHUB_VERSION_URL = "https://raw.githubusercontent.com/Memati8383/Niko-AI/refs/heads/main/version.json";
    private static final String GITHUB_APK_URL = "https://github.com/Memati8383/Niko-AI/releases/latest/download/niko.apk";
    
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
        // Statik instance ataması
        instance = this;

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
        layoutHistoryStats = findViewById(R.id.layoutHistoryStats);
        cardStatTotal = findViewById(R.id.cardStatTotal);
        cardStatWeekly = findViewById(R.id.cardStatWeekly);
        cardStatToday = findViewById(R.id.cardStatToday);

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
        layoutAvatarSelection = findViewById(R.id.layoutAvatarSelection);
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
            hapticFeedback(HapticType.MEDIUM);
            animateButtonClick(v);
            performAccountAction();
        });

        
        // Doğrulama Listenerları
        btnVerifyCode.setOnClickListener(v -> {
            hapticFeedback(HapticType.MEDIUM);
            animateButtonClick(v);
            String code = edtVerifyCode.getText().toString().trim();

            if (code.length() == 6) {
                verifyCodeAndRegister(code);
            } else {
                Toast.makeText(this, "Lütfen 6 haneli kodu girin", Toast.LENGTH_SHORT).show();
                shakeView(edtVerifyCode);
            }
        });
        
        btnResendCode.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
            resendVerificationCode();
        });
        
        btnCancelVerification.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
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

        btnMic.setOnClickListener(v -> {
            hapticFeedback(HapticType.MEDIUM);
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

        btnModel.setOnClickListener(v -> {
            hapticFeedback(HapticType.MEDIUM);
            animateButtonClick(v);
            showModels();
        });
        btnCloseModels.setOnClickListener(v -> {
            hapticFeedback(HapticType.LIGHT);
            animateButtonClick(v);
            hideModels();
        });


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

        // Durdurma butonu (Geliştirildi + AI İptal Özelliği)
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            hapticFeedback(HapticType.MEDIUM);

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
            // 3. AI İsteğini İptal Et (Vazgeçme)
            if (currentAiTask != null && !currentAiTask.isDone()) {
                currentAiTask.cancel(true); // true = interrupt if running
                addLog("[AI] İstek kullanıcı tarafından iptal edildi.");
                currentAiTask = null;
            }
            // 4. UI Temizle
            runOnUiThread(() -> {
                aiResponseContainer.setVisibility(View.GONE);
                txtAIResponse.setText("");
                Toast.makeText(this, "İşlem durduruldu", Toast.LENGTH_SHORT).show();
            });
        });

        // Uzun basınca arşivi ve oturumu sıfırla (Tam Sıfırlama)
        btnStop.setOnLongClickListener(v -> {
            hapticFeedback(HapticType.LONG_PRESS);

            // Oturumu sıfırla
            sessionId = null;
            sessionPrefs.edit().remove("session_id").apply();
            // Arşivi temizle
            clearHistory();
            Toast.makeText(this, "Zihin arşivi ve oturum sıfırlandı", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Orb Animasyonunu Başlat
        startBreathingAnimation();

        // Güvenli Alan (WindowInsets) Ayarı - Alt barın navigasyon çubuğuyla çakışmasını önler
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

        // Bildirim Erişimi Kontrolü (WhatsApp ve Instagram Takibi için)
        if (!isNotificationServiceEnabled()) {
            showNotificationAccessDialog();
        }
        
        // Erişilebilirlik Servisi Kontrolü (Tam Otomatik WhatsApp ve YouTube için)
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityAccessDialog();
        }
        
        
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
     * Haptik geri bildirim türleri.
     */
    public enum HapticType {
        LIGHT, MEDIUM, HEAVY, SUCCESS, ERROR, LONG_PRESS
    }

    /**
     * Gelişmiş Haptik Geri Bildirim: Kullanıcıya premium dokunsal his yaşatır.
     * @param type Geri bildirim türü (LIGHT, MEDIUM, HEAVY, SUCCESS, ERROR, LONG_PRESS)
     */
    private void hapticFeedback(HapticType type) {
        try {
            android.os.Vibrator v = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.os.VibrationEffect effect;
                switch (type) {
                    case LIGHT:
                        effect = android.os.VibrationEffect.createOneShot(12, 45);
                        break;
                    case MEDIUM:
                        effect = android.os.VibrationEffect.createOneShot(25, 75);
                        break;
                    case HEAVY:
                        effect = android.os.VibrationEffect.createOneShot(55, 120);
                        break;
                    case SUCCESS:
                        // Premium çift tık efekti
                        effect = android.os.VibrationEffect.createWaveform(new long[]{0, 25, 80, 40}, -1);
                        break;
                    case ERROR:
                        // Üçlü uyarı titreşimi
                        effect = android.os.VibrationEffect.createWaveform(new long[]{0, 50, 60, 50, 60, 60}, -1);
                        break;
                    case LONG_PRESS:
                        effect = android.os.VibrationEffect.createOneShot(70, 90);
                        break;
                    default:
                        effect = android.os.VibrationEffect.createOneShot(20, 50);
                }
                v.vibrate(effect);
            } else {
                long duration = 20;
                if (type == HapticType.HEAVY || type == HapticType.LONG_PRESS || type == HapticType.ERROR) duration = 60;
                v.vibrate(duration);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Geriye dönük uyumluluk için temel titreşim metodu.
     */
    private void vibrateFeedback() {
        hapticFeedback(HapticType.LIGHT);
    }


    /* *********************************************************************************
     *                                 İZİN SİSTEMİ
     * *********************************************************************************/

    private void requestPermissions() {
        ArrayList<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.RECORD_AUDIO);
        perms.add(Manifest.permission.CALL_PHONE);
        perms.add(Manifest.permission.READ_CONTACTS);
        perms.add(Manifest.permission.READ_CALL_LOG);
        perms.add(Manifest.permission.WRITE_CALL_LOG);
        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        perms.add(Manifest.permission.READ_CALENDAR);
        perms.add(Manifest.permission.WRITE_CALENDAR);

        ArrayList<String> list = new ArrayList<>();
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                list.add(p);
            }
        }
        
        if (!list.isEmpty()) {
            requestPermissions(list.toArray(new String[0]), PERMISSION_CODE);
        }

        // Sistem Ayarlarını Değiştirme İzni (Parlaklık vb. kontrolü için)
        if (Build.VERSION.SDK_INT >= 23) {
             if (!Settings.System.canWrite(this)) {
                 Toast.makeText(this, "Lütfen Sistem Ayarlarını Değiştirme iznini verin", Toast.LENGTH_LONG).show();
                 Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                 intent.setData(Uri.parse("package:" + getPackageName()));
                 startActivity(intent);
             }
        }
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

    /* *********************************************************************************
     *                                SES TANIMA (STT)
     * *********************************************************************************/

    /**
     * Android Speech Recognition motorunu başlatır ve dil ayarlarını yapılandırır.
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
                if (!processLocalCommand(cmdLower)) {
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
                // Daha pürüzsüz bir ölçeklendirme için değerleri sınırlıyoruz ve maks ölçek 1.4
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

    /* *********************************************************************************
     *                                KOMUT İŞLEYİCİ
     * *********************************************************************************/

    /**
     * Sesli veya yazılı komutları yerel olarak analiz eder.
     * Cihaz kontrolleri, ayarlar ve sistem komutlarını önceliklendirir.
     * 
     * @param cmd İşlenecek komut metni
     * @return Komut yerel olarak işlendiyse true, AI'ya devredilecekse false
     */
    private boolean processLocalCommand(String cmd) {

        // --- NIKO KİMLİK KONTROLÜ ---
        if (cmd.contains("adın ne") || cmd.contains("kimsin") || cmd.contains("kendini tanıt")) {
            speak("Benim adım Niko. Senin kişisel yapay zeka asistanınım.");
            return true;
        }

        // --- WHATSAPP İŞLEMLERİ ---
        if (cmd.contains("whatsapp") && cmd.contains("oku")) {
            readLastWhatsAppMessage();
            return true;
        }

        if (cmd.contains("whatsapp") && cmd.contains("cevap")) {
            replyWhatsApp("Tamam"); // Basit otonom cevap örneği
            return true;
        }

        if (cmd.contains("whatsapp") && (cmd.contains("mesaj") || cmd.contains("yaz") || cmd.contains("yolla") || cmd.contains("gönder"))) {
            handleWhatsAppCommand(cmd);
            return true;
        }

        // --- ARAMA İŞLEMLERİ ---
        if (cmd.contains("son gelen")) {
            callLast(CallLog.Calls.INCOMING_TYPE);
            return true;
        }

        if (cmd.contains("son aranan")) {
            callLast(CallLog.Calls.OUTGOING_TYPE);
            return true;
        }

        if (cmd.contains("ara")) {
            // "Ahmet'i ara" gibi komutlardan ismi ayıkla
            callByName(cmd.replace("ara", "").trim());
            return true;
        }

        // --- TARİH VE SAAT ---
        if (cmd.contains("saat") && !cmd.contains("kur") && !cmd.contains("alarm")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            speak("Saat şu an " + sdf.format(new Date()));
            return true;
        }

        if (cmd.contains("tarih") || cmd.contains("bugün günlerden ne") || cmd.contains("hangi gündeyiz")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy EEEE", new Locale("tr", "TR"));
            speak("Bugün " + sdf.format(new Date()));
            return true;
        }

        // --- KAMERA ---
        if (cmd.contains("kamera aç") || cmd.contains("fotoğraf çek")) {
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
        if (cmd.contains("ayarları aç")) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            speak("Ayarlar açılıyor");
            return true;
        }

        // --- MÜZİK KONTROLLERİ ---
        if (cmd.contains("müzik") || cmd.contains("müzi") || cmd.contains("şarkı") || cmd.contains("spotify")
                || cmd.contains("parça")) {
            if (cmd.contains("başlat") || cmd.contains("oynat") || cmd.contains("devam") || cmd.contains("çal")
                    || cmd.contains("aç")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PLAY);
                speak("Müzik başlatılıyor");
                return true;
            }
            if (cmd.contains("durdur") || cmd.contains("duraklat") || cmd.contains("kapat")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PAUSE);
                speak("Müzik durduruldu");
                return true;
            }
            if (cmd.contains("sonraki") || cmd.contains("geç") || cmd.contains("değiştir") || cmd.contains("atla")
                    || cmd.contains("sıradaki")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_NEXT);
                speak("Sonraki şarkı");
                return true;
            }
            if (cmd.contains("önceki") || cmd.contains("başa") || cmd.contains("geri")) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                speak("Önceki şarkı");
                return true;
            }
        }

        // --- ALARM & HATIRLATICI ---
        if (cmd.contains("alarm")) {
            setAlarm(cmd);
            return true;
        }

        if (cmd.contains("hatırlat") || cmd.contains("anımsat")) {
            setReminder(cmd);
            return true;
        }

        // --- SİSTEM AYARLARI KONTROLÜ (WIFI, BT) ---
        if (cmd.contains("wifi") || cmd.contains("wi-fi") || cmd.contains("internet")) {
            if (cmd.contains("aç")) {
                controlWifi(true);
                return true;
            }
            if (cmd.contains("kapat")) {
                controlWifi(false);
                return true;
            }
        }

        if (cmd.contains("bluetooth")) {
            if (cmd.contains("aç")) {
                controlBluetooth(true);
                return true;
            }
            if (cmd.contains("kapat")) {
                controlBluetooth(false);
                return true;
            }
        }

        // --- GEÇMİŞ KOMUTLARI ---
        if (cmd.contains("geçmişi") || cmd.contains("sohbet geçmişini")) {
            if (cmd.contains("göster") || cmd.contains("aç") || cmd.contains("oku")) {
                int count = getHistoryCount();
                showHistory("");
                speak("Sohbet geçmişi açılıyor. Toplam " + count + " mesaj bulundu.", false);
                return true;
            }
            if (cmd.contains("temizle") || cmd.contains("sil") || cmd.contains("kapat")) {
                clearHistory();
                return true;
            }
        }

        // --- GÜNCELLEME KONTROLÜ ---
        if (cmd.contains("güncelleme") || cmd.contains("sürüm")) {
            if (cmd.contains("kontrol") || cmd.contains("var mı") || cmd.contains("bak")) {
                speak("Güncelleme kontrol ediliyor...", false);
                manualUpdateCheck();
                return true;
            }
        }

        // --- SİSTEM KONTROL VE NAVİGASYON (Accessibility Service) ---
        if (isAccessibilityServiceEnabled()) {
            if (cmd.contains("ekranı kilitle") || cmd.contains("telefonu kilitle")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAccessibilityAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
                    speak("Ekran kilitleniyor.");
                } else {
                    speak("Ekran kilitleme özelliği bu Android sürümünde desteklenmiyor biraderim.");
                }
                return true;
            }
            if (cmd.contains("ekran görüntüsü") || cmd.contains("ekran resmi")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAccessibilityAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT);
                    speak("Ekran görüntüsü alınıyor.");
                } else {
                    speak("Ekran görüntüsü alma özelliği bu Android sürümünde desteklenmiyor.");
                }
                return true;
            }
            if (cmd.contains("geri git") || (cmd.contains("bir önceki") && cmd.contains("ekran"))) {
                performGlobalAccessibilityAction(AccessibilityService.GLOBAL_ACTION_BACK);
                speak("Geri gidiliyor.");
                return true;
            }
            if (cmd.contains("ana ekrana") || cmd.contains("ana sayfaya") || cmd.contains("ev ekranına")) {
                performGlobalAccessibilityAction(AccessibilityService.GLOBAL_ACTION_HOME);
                speak("Ana ekrana dönülüyor.");
                return true;
            }
            if (cmd.contains("son uygulamalar") || cmd.contains("arka plandaki uygulamalar")) {
                performGlobalAccessibilityAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                speak("Son uygulamalar açılıyor.");
                return true;
            }
            if (cmd.contains("bildirimleri göster") || cmd.contains("bildirim panelini aç")) {
                performGlobalAccessibilityAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                speak("Bildirimler açılıyor.");
                return true;
            }
        }

        // --- SES VE PARLAKLIK KONTROLÜ ---
        if (cmd.contains("sesi")) {
            if (cmd.contains("artır") || cmd.contains("arttır") || cmd.contains("yükselt") || cmd.contains("aç")) {
                adjustVolume(true);
                return true;
            }
            if (cmd.contains("azalt") || cmd.contains("kıs") || cmd.contains("düşür")) {
                adjustVolume(false);
                return true;
            }
            if (cmd.contains("kapat") || cmd.contains("sessize al")) {
                setVolumeLevel(0);
                return true;
            }
        }

        if (cmd.contains("parlaklık") || cmd.contains("ışık")) {
            if (cmd.contains("artır") || cmd.contains("arttır") || cmd.contains("yükselt") || cmd.contains("aç")) {
                adjustBrightness(true);
                return true;
            }
            if (cmd.contains("azalt") || cmd.contains("kıs") || cmd.contains("düşür")) {
                adjustBrightness(false);
                return true;
            }
        }

        // --- FENER (FLASHLIGHT) ---
        if (cmd.contains("fener") || cmd.contains("ışığı aç") || cmd.contains("flaşı aç")) {
            if (cmd.contains("aç") || cmd.contains("yak")) {
                toggleFlashlight(true);
                return true;
            }
            if (cmd.contains("kapat") || cmd.contains("söndür")) {
                toggleFlashlight(false);
                return true;
            }
        }

        return false; // Hiçbir yerel komut eşleşmediyse, soruyu Yapay Zeka'ya (AI) devret
    }

    /* *********************************************************************************
     *                            TELEFON İŞLEMLERİ (ARAMALAR)
     * *********************************************************************************/

    /**
     * Belirtilen arama tipi baz alınarak (Gelen/Giden) son aramayı tekrar gerçekleştirir.
     * 
     * @param type CallLog.Calls.TYPE sabitlerinden biri
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

    /* *********************************************************************************
     *                               MEDYA KONTROLLERİ
     * *********************************************************************************/

    /**
     * Sistem medya olaylarını (Play/Pause/Next/Prev) simüle eder.
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

    /* *********************************************************************************
     *                            SİSTEM AYARLARI (SES/IŞIK/FENER)
     * *********************************************************************************/

    private void adjustVolume(boolean increase) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
                increase ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 
                AudioManager.FLAG_SHOW_UI);
            speak(increase ? "Ses artırılıyor" : "Ses azaltılıyor");
        }
    }

    private void setVolumeLevel(int level) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_SHOW_UI);
            speak("Ses kapatıldı");
        }
    }

    private void adjustBrightness(boolean increase) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) {
            speak("Parlaklığı değiştirmek için sistem ayarları izni gerekiyor.");
            return;
        }
        try {
            int current = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            int next = increase ? Math.min(255, current + 50) : Math.max(0, current - 50);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, next);
            
            // Ekranı anında güncellemek için pencere ayarlarını kullan
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = next / 255f;
            getWindow().setAttributes(lp);
            
            speak(increase ? "Parlaklık artırıldı" : "Parlaklık azaltıldı");
        } catch (Exception e) {
            speak("Parlaklık değiştirilemedi.");
        }
    }

    private void toggleFlashlight(boolean open) {
        CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cm.getCameraIdList()[0];
            cm.setTorchMode(cameraId, open);
            speak(open ? "Fener açıldı" : "Fener kapatıldı");
        } catch (Exception e) {
            speak("Fener kontrol edilemedi.");
        }
    }

    /* *********************************************************************************
     *                             YAPAY ZEKA MOTORU (LLM)
     * *********************************************************************************/

    /**
     * Kullanıcı girdisini asenkron olarak yapay zeka sunucusuna iletir.
     * Yanıtı görselleştirmek ve seslendirmek için UI thread'ini günceller.
     * 
     * @param q Kullanıcı sorusu veya komutu
     */
    private void askAI(String q) {
        // UI Geri Bildirimi: Kullanıcıya işlemin başladığını göster
        runOnUiThread(() -> {
            aiResponseContainer.setVisibility(View.VISIBLE);
            txtAIResponse.setText("Niko düşünüyor...");
        });

        // Önceki görevi iptal et (Hızlı art arda isteklerde karışıklığı önler)
        if (currentAiTask != null && !currentAiTask.isDone()) {
            currentAiTask.cancel(true);
        }

        // Görevi ExecutorService ile çalıştır (Thread yönetimi optimize edildi)
        currentAiTask = executorService.submit(() -> {
            HttpURLConnection conn = null;
            try {
                // Sunucu URL'si (API_BASE_URL dinamik olarak güncellenir)
                URL url = new URL(API_BASE_URL + "/chat");

                // HTTP bağlantı ayarları - Timeouts optimize edildi
                conn = (HttpURLConnection) url.openConnection();
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
                conn.setConnectTimeout(15000); // 15 saniye (Daha hızlı fail-fast)
                conn.setReadTimeout(90000);    // 90 saniye (LLM'ler bazen düşünebilir, sabırlı olalım)

                // JSON Veri Paketi
                JSONObject payload = new JSONObject();
                payload.put("message", q);
                payload.put("session_id", sessionId); 
                payload.put("model", selectedModel); 
                payload.put("enable_audio", true); 
                payload.put("web_search", isWebSearchEnabled); 
                payload.put("rag_search", false);
                payload.put("stream", false); 
                payload.put("mode", "normal");

                addLog("[AI] İstek gönderiliyor. Model: " + (selectedModel != null ? selectedModel : "Varsayılan"));
                
                // İptal kontrolü (Ağ işleminden önce)
                if (Thread.currentThread().isInterrupted()) return;

                // İsteği gönder
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // İptal kontrolü (Yanıtı beklemeden önce)
                if (Thread.currentThread().isInterrupted()) return;

                // Sunucudan gelen yanıt kodunu kontrol et
                int code = conn.getResponseCode();
                InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                // Yanıtı oku
                BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    // İptal kontrolü (Okuma sırasında)
                    if (Thread.currentThread().isInterrupted()) return;
                    response.append(responseLine.trim());
                }

                if (code == 200) {
                    // Başarılı yanıtta JSON verilerini ayrıştır
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String replyText = jsonResponse.optString("reply", "");
                    String audioB64 = jsonResponse.optString("audio", "");
                    String newSessionId = jsonResponse.optString("id", null);
                    
                    addLog("[AI] Yanıt başarıyla alındı.");

                    // Yeni Oturum Kimliğini kaydet
                    if (newSessionId != null && !newSessionId.equals(sessionId)) {
                        sessionId = newSessionId;
                        sessionPrefs.edit().putString("session_id", sessionId).apply();
                    }

                    // Arayüz (UI) güncellemeleri
                    final String finalReply = replyText;
                    runOnUiThread(() -> {
                        // "Düşünüyor..." yazısını kaldır ve gerçek yanıtı göster
                        if (txtAIResponse != null) {
                            txtAIResponse.setText(finalReply);
                            // Yanıtı yerel geçmişe kaydet
                            saveToHistory("Niko", finalReply);
                        }
                    });

                    // Ses çalma işlemleri (Ses varsa öncelikli, yoksa TTS)
                    if (!audioB64.isEmpty()) {
                        playAudio(audioB64);
                    } else if (!finalReply.isEmpty()) {
                        speak(finalReply, false);
                    }
                } else {
                    // Hatalı durumda kullanıcıyı uyar
                    addLog("[AI] Sunucu Hatası: " + code + " - " + response.toString());
                    speak("Sunucu ile bağlantıda bir sorun oluştu. Hata kodu: " + code, false);
                }

            } catch (java.net.SocketTimeoutException e) {
                addLog("[AI] ZAMAN AŞIMI: Sunucu yanıt vermedi.");
                speak("Sunucu şu an çok yoğun veya yanıt vermiyor. Lütfen tekrar dene.", false);
            } catch (java.net.UnknownHostException e) {
                addLog("[AI] BAĞLANTI YOK: " + e.getMessage());
                speak("İnternet bağlantını kontrol et biraderim, sunucuya ulaşamıyorum.", false);
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted()) {
                    addLog("[AI] Görev iptal edildiği için hata yutuldu.");
                } else {
                    addLog("[AI] BEKLENMEYEN HATA: " + e.getMessage());
                    e.printStackTrace();
                    speak("Bir hata oluştu: " + e.getMessage(), false);
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                currentAiTask = null; // Görev tamamlandı
            }
        });
    }

    /* *********************************************************************************
     *                             HESAP VE PROFİL YÖNETİMİ
     * *********************************************************************************/

    /**
     * Hesap panelini görünür kılar ve animasyonla ekrana getirir.
     */
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
                // Düzenleme modu - Seçim alanı görünsün
                if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.VISIBLE);
                txtAccountTitle.setText("Profili Düzenle");
                layoutLoggedIn.setVisibility(View.GONE);
                layoutAccountFields.setVisibility(View.VISIBLE);
                layoutRegisterExtras.setVisibility(View.VISIBLE);
                
                edtUsername.setEnabled(true);
                edtCurrentPassword.setVisibility(View.VISIBLE);
                txtCurrentPasswordLabel.setVisibility(View.VISIBLE);
                
                btnSubmitAccount.setText("Güncelle");
                btnSwitchMode.setText("Geri Dön");
                btnSwitchMode.setOnClickListener(v -> {
                    isEditProfileMode = false;
                    selectedImageBase64 = null;
                    updateAccountUI();
                });
            } else {
                // Profil görüntüleme modu
                if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.GONE);
                imgMainProfile.setOnClickListener(null);
                txtAccountTitle.setText("Profilim");
                layoutLoggedIn.setVisibility(View.VISIBLE);
                fetchProfile();
                layoutAccountFields.setVisibility(View.GONE);
            }
        } else {
            if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.GONE);
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
        if (authToken == null) {
            addLog("[PROFIL] HATA: Token bulunamadı");
            return;
        }
        
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_BASE_URL + "/me");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
                conn.setRequestProperty("Accept", "application/json");
                
                // Timeout ayarları ekle
                conn.setConnectTimeout(15000); // 15 saniye bağlantı timeout
                conn.setReadTimeout(15000); // 15 saniye okuma timeout
                
                addLog("[PROFIL] Veriler çekiliyor... URL: " + url.toString());
                
                int code = conn.getResponseCode();
                addLog("[PROFIL] Sunucu yanıt kodu: " + code);
                
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    
                    addLog("[PROFIL] Yanıt alındı. Uzunluk: " + sb.length());
                    
                    JSONObject resp = new JSONObject(sb.toString());
                    String email = resp.optString("email", "");
                    String fullName = resp.optString("full_name", "");
                    String plainPass = resp.optString("plain_password", resp.optString("_plain_password", ""));
                    String profileImgBase64 = resp.optString("profile_image", "");
                    
                    addLog("[PROFIL] Profil başarıyla yüklendi: " + authUsername);
                    
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
                        
                        // Profil kartının görünür olduğundan emin ol
                        if (layoutLoggedIn != null) {
                            layoutLoggedIn.setVisibility(View.VISIBLE);
                            addLog("[PROFIL] Profil kartı görünür hale getirildi");
                        }
                        
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
                                addLog("[PROFIL] Profil resmi yüklenirken hata: " + e.getMessage());
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
                } else {
                    // Hata durumunda detayları oku
                    InputStream errorStream = conn.getErrorStream();
                    String errorDetail = "";
                    if (errorStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                        StringBuilder esb = new StringBuilder();
                        String eline;
                        while ((eline = br.readLine()) != null) esb.append(eline);
                        errorDetail = esb.toString();
                        br.close();
                    }
                    addLog("[PROFIL] HATA: " + code + " - " + errorDetail);
                    
                    final String finalError = errorDetail;
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Profil yüklenemedi: " + code, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (java.net.SocketTimeoutException e) {
                addLog("[PROFIL] TIMEOUT: Sunucu yanıt vermedi - " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Bağlantı zaman aşımı. Lütfen internet bağlantınızı kontrol edin.", Toast.LENGTH_LONG).show();
                });
            } catch (java.net.UnknownHostException e) {
                addLog("[PROFIL] BAĞLANTI HATASI: Sunucuya ulaşılamıyor - " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Sunucuya bağlanılamıyor. İnternet bağlantınızı kontrol edin.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                addLog("[PROFIL] İSTİSNA: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Profil yüklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    /**
     * Kullanıcının o anki moduna göre (Kayıt, Giriş veya Profil Düzenleme)
     * ilgili işlemi tetikler.
     */
    private boolean isValidUsername(String username) {
        return username.length() >= 3 && username.length() <= 30 && 
               Character.isLetter(username.charAt(0)) && 
               username.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && 
               password.matches(".*[A-Z].*") && 
               password.matches(".*[a-z].*") && 
               password.matches(".*[0-9].*");
    }

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
            // İstemci tarafı doğrulama (Backend kurallarına uygun)
            if (!isValidUsername(username)) {
                Toast.makeText(this, "Kullanıcı adı 3-30 karakter olmalı, harfle başlamalı ve sadece harf/rakam/_ içerebilir.", Toast.LENGTH_LONG).show();
                shakeView(edtUsername);
                return;
            }
            if (!isValidPassword(password)) {
                Toast.makeText(this, "Şifre en az 8 karakter olmalı, büyük harf, küçük harf ve rakam içermelidir.", Toast.LENGTH_LONG).show();
                shakeView(edtPassword);
                return;
            }

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
                        hapticFeedback(HapticType.SUCCESS);
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
                    
                    runOnUiThread(() -> {
                        hapticFeedback(HapticType.ERROR);
                        Toast.makeText(this, "Giriş başarısız. Kullanıcı adı veya şifre yanlış.",
                            Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                addLog("[GİRİŞ] İSTİSNA: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ================= KAYIT İŞLEMLERİ =================
    
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
        
        
        // Geçici bilgileri sakla (Doğrulama sonrası kayıt için)
        // DİKKAT: Resend durumunda boş gelebilir, üzerini yazma!
        if (username != null && !username.isEmpty()) this.pendingUsername = username;
        if (password != null && !password.isEmpty()) this.pendingPassword = password;
        if (email != null && !email.isEmpty()) this.pendingEmail = email;
        if (fullName != null && !fullName.isEmpty()) this.pendingFullName = fullName;

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
                        hapticFeedback(HapticType.SUCCESS);
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
                    runOnUiThread(() -> {
                        hapticFeedback(HapticType.ERROR);
                        Toast.makeText(this, "Hata: " + finalError, Toast.LENGTH_LONG).show();
                    });

                }
            } catch (Exception e) {
                addLog("[DOĞRULAMA] İSTİSNA: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * E-posta doğrulama kodunu tekrar gönderir.
     */
    private void resendVerificationCode() {
        if (pendingEmail == null) {
            Toast.makeText(this, "E-posta bilgisi bulunamadı. Lütfen tekrar kayıt olun.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resend animasyonu
        animateResendCode(btnResendCode);
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/email/resend");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);

                JSONObject payload = new JSONObject();
                payload.put("email", pendingEmail);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                
                if (code == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Yeni kod gönderildi!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Hata detayını oku
                    InputStream errorStream = conn.getErrorStream();
                    String errorMsg = "Kod gönderilemedi";
                    if (errorStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        
                        try {
                            JSONObject errJson = new JSONObject(sb.toString());
                            errorMsg = errJson.optString("detail", errorMsg);
                        } catch(Exception ignored) {
                            errorMsg = sb.toString();
                        }
                    }
                    
                    final String finalError = errorMsg;
                    runOnUiThread(() -> Toast.makeText(this, "Hata: " + finalError, Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                addLog("[RESEND] Hata: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * E-posta doğrulama kodunu sunucuya gönderir.
     * Başarılı olursa kayıt işlemini tamamlar.
     */
    private void verifyCodeAndRegister(String code) {
        if (pendingEmail == null || pendingUsername == null || pendingPassword == null) {
            Toast.makeText(this, "Oturum bilgileri eksik veya zaman aşımı. Lütfen tekrar kayıt olun.", Toast.LENGTH_LONG).show();
            animateVerificationExit();
            return;
        }

        addLog("[DOĞRULAMA] Kod doğrulanıyor: " + code);
        
        // UI Geri Bildirimi
        runOnUiThread(() -> {
            btnVerifyCode.setEnabled(false);
            btnVerifyCode.setText("Doğrulanıyor...");
        });

        new Thread(() -> {
            try {
                // 1. KOD DOĞRULAMA İSTEĞİ
                URL urlVerify = new URL(API_BASE_URL + "/email/verify");
                HttpURLConnection connVerify = (HttpURLConnection) urlVerify.openConnection();
                connVerify.setRequestMethod("POST");
                connVerify.setRequestProperty("Content-Type", "application/json");
                connVerify.setDoOutput(true);
                connVerify.setConnectTimeout(10000);

                JSONObject payloadVerify = new JSONObject();
                payloadVerify.put("email", pendingEmail);
                payloadVerify.put("code", code);

                try (OutputStream os = connVerify.getOutputStream()) {
                    os.write(payloadVerify.toString().getBytes("utf-8"));
                }

                int codeVerify = connVerify.getResponseCode();
                
                if (codeVerify != 200) {
                     // Hata okuma
                     InputStream errorStream = connVerify.getErrorStream();
                     String errorMsg = "Kod doğrulanamadı";
                     if (errorStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        
                        // JSON içinden detail çek
                        try {
                            JSONObject errJson = new JSONObject(sb.toString());
                            errorMsg = errJson.optString("detail", errorMsg);
                        } catch(Exception ignored) {
                            errorMsg = sb.toString();
                        }
                     }
                     
                     final String finalError = errorMsg;
                     runOnUiThread(() -> {
                         Toast.makeText(this, finalError, Toast.LENGTH_LONG).show();
                         shakeView(edtVerifyCode);
                         btnVerifyCode.setEnabled(true);
                         btnVerifyCode.setText("DOĞRULA");
                     });
                     return;
                }

                // 2. KAYIT İSTEĞİ (Doğrulama başarılı)
                addLog("[KAYIT] Doğrulama başarılı. Hesap oluşturuluyor...");
                
                URL urlReg = new URL(API_BASE_URL + "/register");
                HttpURLConnection connReg = (HttpURLConnection) urlReg.openConnection();
                connReg.setRequestMethod("POST");
                connReg.setRequestProperty("Content-Type", "application/json");
                connReg.setDoOutput(true);

                JSONObject payloadReg = new JSONObject();
                payloadReg.put("username", pendingUsername);
                payloadReg.put("password", pendingPassword);
                payloadReg.put("email", pendingEmail);
                payloadReg.put("full_name", pendingFullName);

                try (OutputStream os = connReg.getOutputStream()) {
                    os.write(payloadReg.toString().getBytes("utf-8"));
                }
                
                int codeReg = connReg.getResponseCode();
                if (codeReg == 200) {
                     addLog("[KAYIT] Hesap başarıyla oluşturuldu.");
                     
                     runOnUiThread(() -> {
                         addLog("[KAYIT] Giriş yapılıyor...");
                     });
                     
                     // 3. DOĞRUDAN GİRİŞ YAP
                     // Login request metodunu çağırmak yerine manuel token isteği yapıyoruz
                     // Çünkü loginRequest UI thread çağrıları içeriyor, çakışma olmasın
                     
                     URL urlLogin = new URL(API_BASE_URL + "/login");
                     HttpURLConnection connLogin = (HttpURLConnection) urlLogin.openConnection();
                     connLogin.setRequestMethod("POST");
                     connLogin.setRequestProperty("Content-Type", "application/json");
                     connLogin.setDoOutput(true);
                     
                     JSONObject payloadLogin = new JSONObject();
                     payloadLogin.put("username", pendingUsername);
                     payloadLogin.put("password", pendingPassword);
                     
                     try (OutputStream os = connLogin.getOutputStream()) {
                        os.write(payloadLogin.toString().getBytes("utf-8"));
                     }
                     
                     if (connLogin.getResponseCode() == 200) {
                         BufferedReader br = new BufferedReader(new InputStreamReader(connLogin.getInputStream(), "utf-8"));
                         StringBuilder sb = new StringBuilder();
                         String line;
                         while ((line = br.readLine()) != null) sb.append(line);
                         
                         JSONObject resp = new JSONObject(sb.toString());
                         String token = resp.getString("access_token");
                         
                         authToken = token;
                         authUsername = pendingUsername;
                         
                         authPrefs.edit()
                             .putString("access_token", authToken)
                             .putString("username", authUsername)
                             .apply();
                             
                         runOnUiThread(() -> {
                             animateVerificationExit();
                             animateSuccessConfetti(); // Konfeti patlat!
                             Toast.makeText(this, "Hoş geldin " + authUsername + "!", Toast.LENGTH_LONG).show();
                             updateAccountUI();
                             hideAccount();
                         });
                     } else {
                         // Login başarısız ama kayıt başarılı?
                         runOnUiThread(() -> {
                             animateVerificationExit();
                             Toast.makeText(this, "Kayıt başarılı! Lütfen giriş yapın.", Toast.LENGTH_LONG).show();
                             toggleAccountMode(); // Giriş ekranına dön
                         });
                     }
                     
                } else {
                     // Register hatası
                     InputStream errorStream = connReg.getErrorStream();
                     String errorMsg = "Kayıt tamamlanamadı";
                     if (errorStream != null) {
                         BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                         StringBuilder sb = new StringBuilder();
                         String line;
                         while ((line = br.readLine()) != null) sb.append(line);
                         errorMsg = sb.toString();
                     }
                     final String finalError = errorMsg;
                     runOnUiThread(() -> {
                         Toast.makeText(this, "Hata: " + finalError, Toast.LENGTH_LONG).show();
                         btnVerifyCode.setEnabled(true);
                         btnVerifyCode.setText("DOĞRULA");
                     });
                }

            } catch (Exception e) {
                addLog("[DOĞRULAMA] Kritik Hata: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Bağlantı hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnVerifyCode.setEnabled(true);
                    btnVerifyCode.setText("DOĞRULA");
                });
            }
        }).start();
    }

    // ================= ANİMASYON YARDIMCILARI =================

    /**
     * Hesap panelinin açılış animasyonu (Premium giriş efekti).
     * Optimize edildi - Donanım hızlandırma kullanır.
     */
    private void animateAccountEntry() {
        // Önceki animasyonu iptal et
        cancelAnimation(ANIM_ACCOUNT_ENTRY);
        
        layoutAccount.setAlpha(0f);
        layoutAccount.setScaleX(0.9f);
        layoutAccount.setScaleY(0.9f);
        
        // Donanım katmanını kullan (GPU hızlandırma)
        layoutAccount.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        layoutAccount.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
            .withEndAction(() -> {
                // Animasyon bitince katmanı kaldır
                layoutAccount.setLayerType(View.LAYER_TYPE_NONE, null);
            })
            .start();
        
        // Form alanlarını sırayla animasyonla göster
        animateFormFieldsEntry();
    }

    /**
     * Form alanlarının sıralı giriş animasyonu (Kademeli etki).
     * Optimize edildi - Tek işleyici ile toplu işlem.
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
        
        // Tüm alanları hazırla (tek döngü)
        for (View field : fields) {
            if (field != null) {
                field.setAlpha(0f);
                field.setTranslationY(30);
                field.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        }
        
        // Toplu animasyon başlat
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                final View field = fields[i];
                final int delay = i * 60;
                final boolean isLast = (i == fields.length - 1);
                
                field.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(delay + 200)
                    .setDuration(350)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        if (isLast) {
                            // Son animasyon bitince tüm katmanları temizle
                            for (View f : fields) {
                                if (f != null) {
                                    f.setLayerType(View.LAYER_TYPE_NONE, null);
                                }
                            }
                        }
                    })
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
     * Buton tıklama animasyonu (Nabız efekti).
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
     * Kod tekrar gönderme butonu için özel animasyon.
     */
    private void animateResendCode(View button) {
        if (button == null) return;
        
        // Dönme animasyonu (yenileme simgesi gibi)
        button.animate()
            .rotation(360f)
            .setDuration(500)
            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction(() -> button.setRotation(0f))
            .start();
        
        // Renk geçişi (mavi → yeşil → mavi)
        if (button instanceof TextView) {
            TextView textView = (TextView) button;
            int originalColor = textView.getCurrentTextColor();
            int highlightColor = Color.parseColor("#4CAF50");
            
            android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofArgb(
                originalColor, highlightColor, originalColor);
            colorAnim.setDuration(500);
            colorAnim.addUpdateListener(animator -> {
                try {
                    textView.setTextColor((int) animator.getAnimatedValue());
                } catch (Exception ignored) {}
            });
            colorAnim.start();
        }
        
        // Nabız efekti
        button.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(200)
            .withEndAction(() -> {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
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
     * Başarı konfeti animasyonu (Simüle edilmiş parçacık efekti).
     * Optimize edildi - Daha az parçacık, daha iyi performans.
     */
    private void animateSuccessConfetti() {
        if (layoutVerification == null || layoutVerification.getVisibility() != View.VISIBLE) return;
        
        // Layout'un ViewGroup olduğundan emin ol
        if (!(layoutVerification instanceof android.view.ViewGroup)) return;
        
        final android.view.ViewGroup container = (android.view.ViewGroup) layoutVerification;
        
        // Ekran boyutlarını al
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // Parçacık sayısını azalt (20 → 12) - Performans için
        final int particleCount = 12;
        
        // Renk paleti (önceden tanımla)
        final int[] colors = {
            Color.parseColor("#4CAF50"),
            Color.parseColor("#8BC34A"),
            Color.parseColor("#00E5FF"),
            Color.parseColor("#FFD700")
        };
        
        // Toplu işlem için liste
        final View[] particles = new View[particleCount];
        
        for (int i = 0; i < particleCount; i++) {
            View confetti = new View(this);
            int size = (int) (Math.random() * 15 + 8); // 8-23px (daha küçük)
            confetti.setLayoutParams(new android.widget.FrameLayout.LayoutParams(size, size));
            confetti.setBackgroundColor(colors[(int) (Math.random() * colors.length)]);
            
            float startX = (float) (Math.random() * screenWidth);
            confetti.setX(startX);
            confetti.setY(-50);
            confetti.setAlpha(0f);
            
            // Donanım katmanını kullan
            confetti.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            
            particles[i] = confetti;
            
            try {
                container.addView(confetti);
                
                float endY = screenHeight + 100;
                float endX = startX + (float) ((Math.random() - 0.5) * 250);
                long duration = (long) (Math.random() * 800 + 1200); // 1.2-2s (daha hızlı)
                long delay = (long) (Math.random() * 250);
                
                final int index = i;
                confetti.animate()
                    .alpha(1f)
                    .y(endY)
                    .x(endX)
                    .rotation((float) (Math.random() * 720 - 360))
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        try {
                            particles[index].setLayerType(View.LAYER_TYPE_NONE, null);
                            container.removeView(particles[index]);
                        } catch (Exception ignored) {}
                    })
                    .start();
            } catch (Exception ignored) {}
        }
        
        // Arka plan flash efekti (optimize edilmiş)
        View bgFlash = new View(this);
        bgFlash.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        bgFlash.setBackgroundColor(Color.parseColor("#1A4CAF50"));
        bgFlash.setAlpha(0f);
        
        try {
            container.addView(bgFlash, 0);
            
            bgFlash.animate()
                .alpha(1f)
                .setDuration(150) // Daha hızlı
                .withEndAction(() -> {
                    bgFlash.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            try {
                                container.removeView(bgFlash);
                            } catch (Exception ignored) {}
                        })
                        .start();
                })
                .start();
        } catch (Exception ignored) {}
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
     * Premium multi-layer animasyon efekti.
     */
    private void animateVerificationEntry() {
        layoutVerification.setVisibility(View.VISIBLE);
        layoutVerification.setAlpha(0f);
        
        // Ekran genişliğini alarak tam sağdan gelmesini sağla
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        layoutVerification.setTranslationX(screenWidth);

        // 1. KATMAN: Form alanlarını 3D perspektif ile gizle
        layoutAccountFields.setPivotX(0);
        layoutAccountFields.setPivotY(layoutAccountFields.getHeight() / 2f);
        layoutAccountFields.animate()
            .alpha(0f)
            .translationX(-150f)
            .rotationY(-15f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(350)
            .setInterpolator(new android.view.animation.AccelerateInterpolator())
            .withEndAction(() -> {
                layoutAccountFields.setVisibility(View.GONE);
                layoutAccountFields.setRotationY(0f);
                layoutAccountFields.setScaleX(1f);
                layoutAccountFields.setScaleY(1f);
            })
            .start();

        // 2. KATMAN: Doğrulama ekranını elastik sıçrama ile getir
        layoutVerification.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(550)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.3f))
            .withEndAction(() -> {
                // Giriş tamamlandıktan sonra içerik animasyonlarını başlat
                animateVerificationContent();
            })
            .start();
            
        // 3. KATMAN: Arka plan nabız efekti
        animateVerificationBackground();
    }
    
    /**
     * Doğrulama ekranı içeriğinin sıralı animasyonu.
     */
    private void animateVerificationContent() {
        // İçerikteki tüm öğeleri bul
        View txtVerifyInfo = findViewById(R.id.txtVerifyInfo);
        
        View[] contentViews = {
            txtVerifyInfo,
            edtVerifyCode,
            btnVerifyCode,
            btnResendCode,
            btnCancelVerification
        };
        
        // Her öğeyi sırayla animasyonla göster (Kademeli etki)
        for (int i = 0; i < contentViews.length; i++) {
            if (contentViews[i] != null) {
                final View view = contentViews[i];
                final int delay = i * 80;
                
                view.setAlpha(0f);
                view.setTranslationY(40);
                view.setScaleX(0.9f);
                view.setScaleY(0.9f);
                
                view.animate()
                    .alpha(1f)
                    .translationY(0)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(delay)
                    .setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
        }
        
        // Giriş alanına özel nabız animasyonu
        if (edtVerifyCode != null) {
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (edtVerifyCode != null && layoutVerification.getVisibility() == View.VISIBLE) {
                    edtVerifyCode.requestFocus();
                    animateInputPulse(edtVerifyCode);
                    
                    // Klavyeyi aç
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(edtVerifyCode, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 400);
        }
    }
    
    /**
     * Giriş alanı için nabız animasyonu.
     */
    private void animateInputPulse(View input) {
        if (input == null) return;
        
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(input, "scaleX", 1f, 1.05f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(input, "scaleY", 1f, 1.05f, 1f);
        
        scaleX.setDuration(800);
        scaleY.setDuration(800);
        scaleX.setRepeatCount(2);
        scaleY.setRepeatCount(2);
        
        scaleX.start();
        scaleY.start();
        
        vibrateFeedback();
    }
    
    /**
     * Doğrulama ekranı arka plan animasyonu (Hafif parlama efekti).
     * Optimize edildi - Tek animatör, düşük işlemci kullanımı.
     */
    private void animateVerificationBackground() {
        if (layoutVerification == null) return;
        
        // Önceki animasyonu iptal et
        cancelAnimation(ANIM_VERIFICATION_BG);
        
        android.animation.ObjectAnimator alphaAnim = android.animation.ObjectAnimator.ofFloat(
            layoutVerification, "alpha", 0.95f, 1f, 0.95f);
        alphaAnim.setDuration(2000);
        alphaAnim.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        alphaAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        
        // Animasyonu kaydet
        activeAnimations.put(ANIM_VERIFICATION_BG, alphaAnim);
        alphaAnim.start();
    }

    /**
     * Doğrulama ekranını gizleyip ana form ekranını geri getirir.
     * Premium 3D dönme animasyonu ile.
     * Optimize edildi - Donanım hızlandırma.
     */
    private void animateVerificationExit() {
         // Arka plan animasyonunu durdur
         cancelAnimation(ANIM_VERIFICATION_BG);
         if (layoutVerification != null) {
             layoutVerification.setAlpha(1f);
         }
         
         layoutAccountFields.setVisibility(View.VISIBLE);
         layoutAccountFields.setAlpha(0f);
         layoutAccountFields.setTranslationX(-150f);
         layoutAccountFields.setRotationY(-15f);
         layoutAccountFields.setScaleX(0.9f);
         layoutAccountFields.setScaleY(0.9f);

         // Donanım katmanını aktif et
         layoutVerification.setLayerType(View.LAYER_TYPE_HARDWARE, null);
         layoutAccountFields.setLayerType(View.LAYER_TYPE_HARDWARE, null);

         // 1. KATMAN: Doğrulama ekranını 3D dönme ile gizle
         layoutVerification.setPivotX(layoutVerification.getWidth());
         layoutVerification.setPivotY(layoutVerification.getHeight() / 2f);
         
         layoutVerification.animate()
             .alpha(0f)
             .translationX(200f)
             .rotationY(20f)
             .scaleX(0.85f)
             .scaleY(0.85f)
             .setDuration(350)
             .setInterpolator(new android.view.animation.AccelerateInterpolator())
             .withEndAction(() -> {
                 layoutVerification.setVisibility(View.GONE);
                 layoutVerification.setTranslationX(0f);
                 layoutVerification.setRotationY(0f);
                 layoutVerification.setScaleX(1f);
                 layoutVerification.setScaleY(1f);
                 layoutVerification.setLayerType(View.LAYER_TYPE_NONE, null);
             })
             .start();

         // 2. KATMAN: Form alanlarını elastik sıçrama ile getir
         layoutAccountFields.setPivotX(0);
         layoutAccountFields.setPivotY(layoutAccountFields.getHeight() / 2f);
         
         layoutAccountFields.animate()
             .alpha(1f)
             .translationX(0f)
             .rotationY(0f)
             .scaleX(1f)
             .scaleY(1f)
             .setDuration(500)
             .setStartDelay(100)
             .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
             .withEndAction(() -> {
                 layoutAccountFields.setLayerType(View.LAYER_TYPE_NONE, null);
             })
             .start();
    }

    /**
     * Hatalı işlemde görsele titreme efekti verir.
     * Geliştirilmiş çok eksenli titreme animasyonu.
     */
    private void shakeView(View view) {
        if (view == null) return;
        
        // X ekseni titreme (yatay)
        android.animation.ObjectAnimator shakeX = android.animation.ObjectAnimator.ofFloat(
            view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shakeX.setDuration(600);
        
        // Y ekseni hafif titreme (dikey)
        android.animation.ObjectAnimator shakeY = android.animation.ObjectAnimator.ofFloat(
            view, "translationY", 0, -5, 5, -5, 5, -3, 3, 0);
        shakeY.setDuration(600);
        
        // Rotation titreme
        android.animation.ObjectAnimator rotation = android.animation.ObjectAnimator.ofFloat(
            view, "rotation", 0, -3, 3, -3, 3, -2, 2, -1, 1, 0);
        rotation.setDuration(600);
        
        // Tüm animasyonları birlikte çalıştır
        android.animation.AnimatorSet animSet = new android.animation.AnimatorSet();
        animSet.playTogether(shakeX, shakeY, rotation);
        animSet.start();
        
        // Dokunsal geri bildirim (3 kısa titreşim)
        vibrateFeedback();
        new android.os.Handler(Looper.getMainLooper()).postDelayed(this::vibrateFeedback, 100);
        new android.os.Handler(Looper.getMainLooper()).postDelayed(this::vibrateFeedback, 200);
        
        // Kırmızı flash efekti (daha belirgin)
        int originalBg = Color.parseColor("#1E1E32");
        int errorBg = Color.parseColor("#4DFF0000"); // %30 opacity kırmızı
        
        android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofArgb(originalBg, errorBg, originalBg);
        colorAnim.setDuration(600);
        colorAnim.addUpdateListener(animator -> {
            try {
                if (view.getBackground() instanceof android.graphics.drawable.ColorDrawable) {
                    view.setBackgroundColor((int) animator.getAnimatedValue());
                }
            } catch (Exception ignored) {}
        });
        colorAnim.start();
        
        // Ölçek nabzı (hata vurgusu)
        view.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(150)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start();
            })
            .start();
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

                // Yalnızca değiştiriliyorsa şifreleri ekle
                if (!newPassword.isEmpty()) {
                    if (currentPassword.isEmpty()) {
                        runOnUiThread(() -> {
                            hapticFeedback(HapticType.ERROR);
                            Toast.makeText(this, "Şifre değiştirmek için mevcut şifreniz gerekli",
                                Toast.LENGTH_LONG).show();
                        });
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
                        hapticFeedback(HapticType.SUCCESS);
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
        hapticFeedback(HapticType.HEAVY);
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
        
        // Butonlar için özel kapsayıcı
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
        
        // Buton tıklama dinleyicileri
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
        // Ana kapsayıcı
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

    /* *********************************************************************************
     *                               AUDIO PLAYBACK
     * *********************************************************************************/

    /**
     * Sunucudan gelen Base64 formatındaki ses verisini çözer ve cihazda oynatır.
     * 
     * @param base64Sound Base64 ile kodlanmış ses verisi (mp3)
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

            // Medya oynatıcıyı arayüz iş parçacığında başlat
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

    /* *********************************************************************************
     *                               TEXT TO SPEECH (TTS)
     * *********************************************************************************/

    /**
     * Metin okuma (TTS) motorunu ilklendirir ve dil desteğini kontrol eder.
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

        // 3. Yaygın İfade ve Sembol Temizliği (:P, :D, XD, :) vb.)
        // Kullanıcı özellikle :P ve benzerlerinin okunmamasını istedi.
        cleaned = cleaned.replaceAll("(?i):P", "")
                         .replaceAll("(?i):D", "")
                         .replaceAll("(?i)XD", "")
                         .replaceAll(":\\)", "")
                         .replaceAll(":\\(", "")
                         .replaceAll(";\\)", "")
                         .replaceAll("<3", "")
                         .replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", ""); // Standart Emojiler

        // 4. Bağlantıları temizle
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

    /* *********************************************************************************
     *                             WHATSAPP INTEGRATION
     * *********************************************************************************/

    /**
     * Bildirim merkezinden yakalanan son WhatsApp mesajını seslendirir.
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

        // Yanıt isteğini oluştur ve gönder
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putCharSequence(lastRemoteInput.getResultKey(), msg);
        RemoteInput.addResultsToIntent(new RemoteInput[] { lastRemoteInput }, i, b);

        try {
            lastReplyIntent.send(this, 0, i);
        } catch (Exception ignored) {
        }
    }

    /**
     * Rehberden isim bularak WhatsApp mesajı gönderir.
     * İsim varyasyonlarını (abim/ağabeyim vb.) otomatik olarak dener.
     */
    private void sendWhatsApp(String name, String message) {
        String phone = findContactNumber(name);

        // --- İsim Varyasyon Kontrolü ---
        // Eğer ilk arama başarısız olursa, yaygın STT değişimlerini dene
        if (phone == null) {
            if (name.equalsIgnoreCase("ağabeyim") || name.equalsIgnoreCase("abim")) {
                phone = findContactNumber(name.equalsIgnoreCase("abim") ? "ağabeyim" : "abim");
            }
            // Diğer yaygın ikililer buraya eklenebilir
        }

        if (phone != null) {
            try {
                // Numarayı WhatsApp formatına hazırla
                String cleanPhone = phone.replaceAll("[^0-9]", "");
                if (cleanPhone.startsWith("0")) {
                    cleanPhone = "90" + cleanPhone.substring(1);
                } else if (!cleanPhone.startsWith("90") && cleanPhone.length() == 10) {
                    cleanPhone = "90" + cleanPhone;
                }

                // WhatsApp URI'sini oluştur (api.whatsapp.com yerine whatsapp:// daha hızlıdır)
                String url = "whatsapp://send?phone=" + cleanPhone + "&text=" + URLEncoder.encode(message, "UTF-8");
                
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.whatsapp");
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                startActivity(intent);
                
                // Erişilebilirlik servisinin aktif olup olmadığını kontrol et
                if (!isAccessibilityServiceEnabled()) {
                    speak(name + " için mesaj hazırlandı. Tam otomatik gönderim için Erişilebilirlik izni vermen gerekiyor. Şimdilik gönder tuşuna kendin basmalısın.");
                    Toast.makeText(this, "Otomatik gönderim için izin gerekli!", Toast.LENGTH_LONG).show();
                    // İzin ekranına yönlendir (Opsiyonel: Kullanıcıyı rahatsız etmemek için sadece ilk seferde yapılabilir)
                } else {
                    speak(name + " kişisine mesajın otomatik olarak gönderiliyor.");
                }
            } catch (Exception e) {
                speak("WhatsApp mesajı açılamadı.");
            }
        } else {
            speak(name + " kişisini rehberde bulamadım.");
            addLog("[WhatsApp] Rehberde bulunamadı: " + name);
        }
    }

    /**
     * Rehberde belirtilen ismi arar ve telefon numarasını döndürür.
     */
    private String findContactNumber(String name) {
        try (Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?", new String[] { "%" + name + "%" },
                null)) {

            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
        } catch (Exception e) {
            addLog("[Contact] Sorgu hatası: " + e.getMessage());
        }
        return null;
    }

    /**
     * WhatsApp mesaj atma komutlarını analiz eder.
     * Geliştirilmiş regex ve mantık ile daha esnek tanıma sağlar.
     */
    private void handleWhatsAppCommand(String cmd) {
        String clean = cmd.toLowerCase(new Locale("tr", "TR"));
        String name = "";
        String message = "";

        // Gereksiz anahtar kelimeleri temizle ama mesaj içeriğini bozma
        String workingCmd = clean.replace("whatsapp'tan", "").replace("whatsapptan", "")
                                 .replace("whatsapp", "").trim();

        try {
            // 1. ADIM: İsim ve yönelme eki arama (ahmet'e, ahmete, ahmet ye vb.)
            // Regex: Bir kelime + opsiyonel kesme + [e, a, ye, ya] + boşluk veya son
            Pattern p = Pattern.compile("([^\\s']+)[']?([ae]|ye|ya)\\b");
            Matcher m = p.matcher(workingCmd);
            
            if (m.find()) {
                name = m.group(1).trim();
                // İsmin sonundaki eki ve ismin kendisini komuttan çıkararak mesajı bul
                message = workingCmd.substring(m.end()).trim();
            }

            // 2. ADIM: Eğer yukarıdaki yapı tutmadıysa alternatif bölme
            if (name.isEmpty() || message.isEmpty()) {
                // "mesaj", "yaz", "yolla" gibi kelimelere göre bölmeyi dene
                String[] keywords = {" mesajı ", " mesaj ", " yaz ", " yolla ", " gönder ", " at "};
                for (String kw : keywords) {
                    if (workingCmd.contains(kw)) {
                        String[] parts = workingCmd.split(kw, 2);
                        if (parts.length == 2) {
                            name = parts[0].replaceAll("[']?([ae]|ye|ya)$", "").trim();
                            message = parts[1].trim();
                            break;
                        }
                    }
                }
            }

            // 3. ADIM: Mesajın sonundaki "yaz", "gönder" gibi fiilleri temizle
            if (!message.isEmpty()) {
                message = message.replaceAll("\\b(yaz|gönder|yolla|at|de|diye|söyle)$", "").trim();
            }

            // İstisnai durum: Eğer isim hala boşsa ve "X mesaj at" dediyse
            if (name.isEmpty() && workingCmd.split("\\s+").length >= 2) {
                String[] words = workingCmd.split("\\s+");
                name = words[0].replaceAll("[']?([ae]|ye|ya)$", "");
                message = workingCmd.substring(words[0].length()).trim();
            }

            if (!name.isEmpty() && !message.isEmpty() && message.length() > 1) {
                sendWhatsApp(name, message);
            } else {
                addLog("[WhatsApp] Ayrıştırma başarısız: " + workingCmd + " (İsim: " + name + ", Mesaj: " + message + ")");
                speak("WhatsApp mesajı için ismi veya mesaj içeriğini tam ayırt edemedim. Lütfen 'Ahmet'e selam yaz' gibi net bir komut ver.");
            }
        } catch (Exception e) {
            addLog("[WhatsApp] Hata: " + e.getMessage());
            speak("WhatsApp komutu işlenirken bir sorun oluştu.");
        }
    }

    /* *********************************************************************************
     *                              ALARM & REMINDERS
     * *********************************************************************************/

    /**
     * Sesli komuttaki zaman ifadelerini analiz ederek sistem alarmı kurar.
     * 
     * @param cmd Kullanıcının sesli veya yazılı komutu
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
            // 2. KESİN ZAMAN
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

            // ÖS (Öğleden sonra) Düzeltmesi (12 saatlik formatı 24'e çevir)
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
     * Premium silme onay iletişim kutusu.
     * Modern, buzlu cam (glassmorphism) tasarımı.
     */
    private void showPremiumDeleteDialog(String title, String message, String preview, Runnable onConfirm) {
        // Ana container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(48, 40, 48, 32);
        // Premium buzlu cam (glassmorphism) arka plan
        android.graphics.drawable.GradientDrawable bgGradient = new android.graphics.drawable.GradientDrawable();
        bgGradient.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bgGradient.setCornerRadius(32);
        bgGradient.setColors(new int[]{
            Color.parseColor("#1E1E32"),
            Color.parseColor("#12121F")
        });
        bgGradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
        bgGradient.setStroke(2, Color.parseColor("#33FF6B6B"));
        mainLayout.setBackground(bgGradient);
        mainLayout.setElevation(24);

        // İkon kapsayıcısı (Animasyonlu uyarı)
        android.widget.FrameLayout iconFrame = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams iconFrameParams = new LinearLayout.LayoutParams(80, 80);
        iconFrameParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        iconFrameParams.setMargins(0, 0, 0, 24);
        iconFrame.setLayoutParams(iconFrameParams);
        
        // İkon arka plan (radyal daire)
        View iconBg = new View(this);
        android.widget.FrameLayout.LayoutParams iconBgParams = new android.widget.FrameLayout.LayoutParams(80, 80);
        iconBg.setLayoutParams(iconBgParams);
        android.graphics.drawable.GradientDrawable iconBgDrawable = new android.graphics.drawable.GradientDrawable();
        iconBgDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        iconBgDrawable.setColors(new int[]{
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#EE5A6F")
        });
        iconBg.setBackground(iconBgDrawable);
        iconFrame.addView(iconBg);
        
        // İkon (çöp kutusu/sil)
        TextView iconText = new TextView(this);
        iconText.setText("🗑️");
        iconText.setTextSize(32);
        iconText.setGravity(android.view.Gravity.CENTER);
        android.widget.FrameLayout.LayoutParams iconTextParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
        iconText.setLayoutParams(iconTextParams);
        iconFrame.addView(iconText);
        
        mainLayout.addView(iconFrame);

        // Başlık
        TextView txtTitle = new TextView(this);
        txtTitle.setText(title);
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(22);
        txtTitle.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        txtTitle.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 16);
        txtTitle.setLayoutParams(titleParams);
        mainLayout.addView(txtTitle);

        // Ayırıcı çizgi
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 0, 0, 20);
        divider.setLayoutParams(dividerParams);
        android.graphics.drawable.GradientDrawable dividerGradient = new android.graphics.drawable.GradientDrawable();
        dividerGradient.setColors(new int[]{Color.TRANSPARENT, Color.parseColor("#44FF6B6B"), Color.TRANSPARENT});
        dividerGradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        divider.setBackground(dividerGradient);
        mainLayout.addView(divider);

        // Mesaj
        TextView txtMessage = new TextView(this);
        txtMessage.setText(message);
        txtMessage.setTextColor(Color.parseColor("#CCFFFFFF"));
        txtMessage.setTextSize(15);
        txtMessage.setGravity(android.view.Gravity.CENTER);
        txtMessage.setLineSpacing(8, 1.2f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msgParams.setMargins(0, 0, 0, 16);
        txtMessage.setLayoutParams(msgParams);
        mainLayout.addView(txtMessage);

        // Önizleme kartı
        if (preview != null && !preview.isEmpty()) {
            LinearLayout previewCard = new LinearLayout(this);
            previewCard.setOrientation(LinearLayout.VERTICAL);
            previewCard.setPadding(20, 16, 20, 16);
            LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            previewParams.setMargins(0, 0, 0, 24);
            previewCard.setLayoutParams(previewParams);
            
            android.graphics.drawable.GradientDrawable previewBg = new android.graphics.drawable.GradientDrawable();
            previewBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            previewBg.setCornerRadius(12);
            previewBg.setColor(Color.parseColor("#15FFFFFF"));
            previewBg.setStroke(1, Color.parseColor("#33FFFFFF"));
            previewCard.setBackground(previewBg);
            
            TextView previewLabel = new TextView(this);
            previewLabel.setText("Önizleme:");
            previewLabel.setTextColor(Color.parseColor("#88FFFFFF"));
            previewLabel.setTextSize(11);
            previewLabel.setAllCaps(true);
            previewLabel.setLetterSpacing(0.1f);
            previewCard.addView(previewLabel);
            
            TextView previewText = new TextView(this);
            previewText.setText("\"" + preview + "\"");
            previewText.setTextColor(Color.WHITE);
            previewText.setTextSize(13);
            previewText.setPadding(0, 8, 0, 0);
            previewText.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.ITALIC));
            previewCard.addView(previewText);
            
            mainLayout.addView(previewCard);
        }

        // Uyarı metni
        TextView txtWarning = new TextView(this);
        txtWarning.setText("⚠️ Bu işlem geri alınamaz");
        txtWarning.setTextColor(Color.parseColor("#FFB74D"));
        txtWarning.setTextSize(12);
        txtWarning.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams warnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        warnParams.setMargins(0, 0, 0, 24);
        txtWarning.setLayoutParams(warnParams);
        mainLayout.addView(txtWarning);

        // Buton kapsayıcısı
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // İptal butonu
        TextView btnCancel = new TextView(this);
        btnCancel.setText("Vazgeç");
        btnCancel.setTextColor(Color.WHITE);
        btnCancel.setTextSize(15);
        btnCancel.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        btnCancel.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        cancelParams.setMargins(0, 0, 8, 0);
        btnCancel.setLayoutParams(cancelParams);
        btnCancel.setPadding(0, 32, 0, 32);
        
        android.graphics.drawable.GradientDrawable cancelBg = new android.graphics.drawable.GradientDrawable();
        cancelBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        cancelBg.setCornerRadius(20);
        cancelBg.setColor(Color.parseColor("#2A2A3E"));
        cancelBg.setStroke(2, Color.parseColor("#44FFFFFF"));
        btnCancel.setBackground(cancelBg);

        // Sil butonu
        TextView btnDelete = new TextView(this);
        btnDelete.setText("Sil");
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setTextSize(15);
        btnDelete.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        btnDelete.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        deleteParams.setMargins(8, 0, 0, 0);
        btnDelete.setLayoutParams(deleteParams);
        btnDelete.setPadding(0, 32, 0, 32);
        
        android.graphics.drawable.GradientDrawable deleteBg = new android.graphics.drawable.GradientDrawable();
        deleteBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        deleteBg.setCornerRadius(20);
        deleteBg.setColors(new int[]{
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#EE5A6F")
        });
        deleteBg.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        btnDelete.setBackground(deleteBg);

        buttonLayout.addView(btnCancel);
        buttonLayout.addView(btnDelete);
        mainLayout.addView(buttonLayout);

        // Dialog oluştur
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(mainLayout);
        builder.setCancelable(true);
        
        android.app.AlertDialog dialog = builder.create();
        
        // Buton animasyonları ve click listener'lar
        btnCancel.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
            new android.os.Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss, 100);
        });
        
        btnDelete.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
            
            // Silme animasyonu
            iconText.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .alpha(0f)
                .setDuration(200)
                .start();
            
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            }, 200);
        });
        
        // Dialog arka planını şeffaf yap
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.show();
        
        // Giriş animasyonu
        mainLayout.setScaleX(0.85f);
        mainLayout.setScaleY(0.85f);
        mainLayout.setAlpha(0f);
        mainLayout.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
            .start();
        
        // İkon nabız animasyonu
        android.animation.ObjectAnimator iconPulse = android.animation.ObjectAnimator.ofFloat(
            iconBg, "scaleX", 1f, 1.1f, 1f);
        android.animation.ObjectAnimator iconPulseY = android.animation.ObjectAnimator.ofFloat(
            iconBg, "scaleY", 1f, 1.1f, 1f);
        iconPulse.setDuration(1000);
        iconPulseY.setDuration(1000);
        iconPulse.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        iconPulseY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        iconPulse.start();
        iconPulseY.start();
    }

    /**
     * Premium "Tümünü Temizle" onay iletişim kutusu.
     * Tüm sohbet geçmişini silmek için kullanılır.
     */
    private void showPremiumClearAllDialog(int totalCount, Runnable onConfirm) {
        // Ana container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(48, 40, 48, 32);
        
        // Premium buzlu cam (glassmorphism) arka plan (daha koyu ve tehlikeli görünüm)
        android.graphics.drawable.GradientDrawable bgGradient = new android.graphics.drawable.GradientDrawable();
        bgGradient.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bgGradient.setCornerRadius(32);
        bgGradient.setColors(new int[]{
            Color.parseColor("#2A1E1E"),
            Color.parseColor("#1F1212")
        });
        bgGradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
        bgGradient.setStroke(3, Color.parseColor("#55FF4444"));
        mainLayout.setBackground(bgGradient);
        mainLayout.setElevation(28);

        // İkon kapsayıcısı (Animasyonlu uyarı - daha büyük)
        android.widget.FrameLayout iconFrame = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams iconFrameParams = new LinearLayout.LayoutParams(96, 96);
        iconFrameParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        iconFrameParams.setMargins(0, 0, 0, 24);
        iconFrame.setLayoutParams(iconFrameParams);
        
        // İkon arka plan (radyal daire - daha parlak kırmızı)
        View iconBg = new View(this);
        android.widget.FrameLayout.LayoutParams iconBgParams = new android.widget.FrameLayout.LayoutParams(96, 96);
        iconBg.setLayoutParams(iconBgParams);
        android.graphics.drawable.GradientDrawable iconBgDrawable = new android.graphics.drawable.GradientDrawable();
        iconBgDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        iconBgDrawable.setColors(new int[]{
            Color.parseColor("#FF4444"),
            Color.parseColor("#DD2C2C")
        });
        iconBg.setBackground(iconBgDrawable);
        iconFrame.addView(iconBg);
        
        // İkon (uyarı sembolü)
        TextView iconText = new TextView(this);
        iconText.setText("⚠️");
        iconText.setTextSize(40);
        iconText.setGravity(android.view.Gravity.CENTER);
        android.widget.FrameLayout.LayoutParams iconTextParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
        iconText.setLayoutParams(iconTextParams);
        iconFrame.addView(iconText);
        
        mainLayout.addView(iconFrame);

        // Başlık
        TextView txtTitle = new TextView(this);
        txtTitle.setText("Tüm Geçmişi Sil");
        txtTitle.setTextColor(Color.parseColor("#FF4444"));
        txtTitle.setTextSize(24);
        txtTitle.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        txtTitle.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 16);
        txtTitle.setLayoutParams(titleParams);
        mainLayout.addView(txtTitle);

        // Ayırıcı çizgi
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 0, 0, 20);
        divider.setLayoutParams(dividerParams);
        android.graphics.drawable.GradientDrawable dividerGradient = new android.graphics.drawable.GradientDrawable();
        dividerGradient.setColors(new int[]{Color.TRANSPARENT, Color.parseColor("#66FF4444"), Color.TRANSPARENT});
        dividerGradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        divider.setBackground(dividerGradient);
        mainLayout.addView(divider);

        // Mesaj
        TextView txtMessage = new TextView(this);
        txtMessage.setText("TÜM sohbet geçmişiniz kalıcı olarak silinecek!");
        txtMessage.setTextColor(Color.WHITE);
        txtMessage.setTextSize(16);
        txtMessage.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        txtMessage.setGravity(android.view.Gravity.CENTER);
        txtMessage.setLineSpacing(8, 1.2f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msgParams.setMargins(0, 0, 0, 20);
        txtMessage.setLayoutParams(msgParams);
        mainLayout.addView(txtMessage);

        // İstatistik kartı
        LinearLayout statsCard = new LinearLayout(this);
        statsCard.setOrientation(LinearLayout.VERTICAL);
        statsCard.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statsParams.setMargins(0, 0, 0, 24);
        statsCard.setLayoutParams(statsParams);
        
        android.graphics.drawable.GradientDrawable statsBg = new android.graphics.drawable.GradientDrawable();
        statsBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        statsBg.setCornerRadius(16);
        statsBg.setColor(Color.parseColor("#20FF4444"));
        statsBg.setStroke(2, Color.parseColor("#44FF4444"));
        statsCard.setBackground(statsBg);
        
        // Toplam mesaj sayısı
        TextView statsTitle = new TextView(this);
        statsTitle.setText("SİLİNECEK MESAJLAR");
        statsTitle.setTextColor(Color.parseColor("#88FFFFFF"));
        statsTitle.setTextSize(11);
        statsTitle.setAllCaps(true);
        statsTitle.setLetterSpacing(0.15f);
        statsTitle.setGravity(android.view.Gravity.CENTER);
        statsCard.addView(statsTitle);
        
        TextView statsCount = new TextView(this);
        statsCount.setText(String.valueOf(totalCount));
        statsCount.setTextColor(Color.parseColor("#FF4444"));
        statsCount.setTextSize(48);
        statsCount.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        statsCount.setGravity(android.view.Gravity.CENTER);
        statsCount.setPadding(0, 8, 0, 8);
        statsCard.addView(statsCount);
        
        TextView statsLabel = new TextView(this);
        statsLabel.setText("KAYIT");
        statsLabel.setTextColor(Color.parseColor("#AAFFFFFF"));
        statsLabel.setTextSize(13);
        statsLabel.setAllCaps(true);
        statsLabel.setLetterSpacing(0.1f);
        statsLabel.setGravity(android.view.Gravity.CENTER);
        statsCard.addView(statsLabel);
        
        mainLayout.addView(statsCard);

        // Uyarı metinleri (3 satır)
        String[] warnings = {
            "⚠️ Bu işlem GERİ ALINAMAZ",
            "🔥 Tüm konuşmalarınız silinecek",
            "💾 Yedekleme yapılmayacak"
        };
        
        for (String warning : warnings) {
            TextView txtWarning = new TextView(this);
            txtWarning.setText(warning);
            txtWarning.setTextColor(Color.parseColor("#FFB74D"));
            txtWarning.setTextSize(13);
            txtWarning.setGravity(android.view.Gravity.CENTER);
            txtWarning.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
            LinearLayout.LayoutParams warnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            warnParams.setMargins(0, 0, 0, 8);
            txtWarning.setLayoutParams(warnParams);
            mainLayout.addView(txtWarning);
        }
        
        // Son boşluk
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 16);
        spacer.setLayoutParams(spacerParams);
        mainLayout.addView(spacer);

        // Buton kapsayıcısı
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // İptal butonu (daha belirgin)
        TextView btnCancel = new TextView(this);
        btnCancel.setText("Vazgeç");
        btnCancel.setTextColor(Color.WHITE);
        btnCancel.setTextSize(16);
        btnCancel.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        btnCancel.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        cancelParams.setMargins(0, 0, 8, 0);
        btnCancel.setLayoutParams(cancelParams);
        btnCancel.setPadding(0, 36, 0, 36);
        
        android.graphics.drawable.GradientDrawable cancelBg = new android.graphics.drawable.GradientDrawable();
        cancelBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        cancelBg.setCornerRadius(20);
        cancelBg.setColors(new int[]{
            Color.parseColor("#3A3A4E"),
            Color.parseColor("#2A2A3E")
        });
        cancelBg.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM);
        cancelBg.setStroke(2, Color.parseColor("#66FFFFFF"));
        btnCancel.setBackground(cancelBg);

        // Sil butonu (daha tehlikeli görünüm)
        TextView btnDelete = new TextView(this);
        btnDelete.setText("TÜMÜNÜ SİL");
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setTextSize(16);
        btnDelete.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        btnDelete.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        deleteParams.setMargins(8, 0, 0, 0);
        btnDelete.setLayoutParams(deleteParams);
        btnDelete.setPadding(0, 36, 0, 36);
        
        android.graphics.drawable.GradientDrawable deleteBg = new android.graphics.drawable.GradientDrawable();
        deleteBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        deleteBg.setCornerRadius(20);
        deleteBg.setColors(new int[]{
            Color.parseColor("#FF4444"),
            Color.parseColor("#CC0000")
        });
        deleteBg.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM);
        deleteBg.setStroke(2, Color.parseColor("#FFFF4444"));
        btnDelete.setBackground(deleteBg);

        buttonLayout.addView(btnCancel);
        buttonLayout.addView(btnDelete);
        mainLayout.addView(buttonLayout);

        // Dialog oluştur
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(mainLayout);
        builder.setCancelable(true);
        
        android.app.AlertDialog dialog = builder.create();
        
        // Buton animasyonları ve click listener'lar
        btnCancel.setOnClickListener(v -> {
            vibrateFeedback();
            animateButtonClick(v);
            new android.os.Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss, 100);
        });
        
        btnDelete.setOnClickListener(v -> {
            // Çift titreşim (tehlike uyarısı)
            vibrateFeedback();
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> vibrateFeedback(), 100);
            
            animateButtonClick(v);
            
            // Silme animasyonu (daha dramatik)
            iconText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .rotation(360f)
                .alpha(0f)
                .setDuration(300)
                .start();
            
            statsCount.animate()
                .scaleX(0.5f)
                .scaleY(0.5f)
                .alpha(0f)
                .setDuration(300)
                .start();
            
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            }, 300);
        });
        
        // Dialog arka planını şeffaf yap
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.show();
        
        // Giriş animasyonu (daha dramatik)
        mainLayout.setScaleX(0.8f);
        mainLayout.setScaleY(0.8f);
        mainLayout.setAlpha(0f);
        mainLayout.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(350)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
            .start();
        
        // İkon nabız animasyonu (daha hızlı ve belirgin)
        android.animation.ObjectAnimator iconPulse = android.animation.ObjectAnimator.ofFloat(
            iconBg, "scaleX", 1f, 1.15f, 1f);
        android.animation.ObjectAnimator iconPulseY = android.animation.ObjectAnimator.ofFloat(
            iconBg, "scaleY", 1f, 1.15f, 1f);
        iconPulse.setDuration(800);
        iconPulseY.setDuration(800);
        iconPulse.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        iconPulseY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        iconPulse.start();
        iconPulseY.start();
        
        // Sayı nabız animasyonu
        android.animation.ObjectAnimator countPulse = android.animation.ObjectAnimator.ofFloat(
            statsCount, "scaleX", 1f, 1.05f, 1f);
        android.animation.ObjectAnimator countPulseY = android.animation.ObjectAnimator.ofFloat(
            statsCount, "scaleY", 1f, 1.05f, 1f);
        countPulse.setDuration(1200);
        countPulseY.setDuration(1200);
        countPulse.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        countPulseY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        countPulse.start();
        countPulseY.start();
    }

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
                    
                    // İstatistik kartlarını güncelle
                    final int finalTodayCount = todayCount;
                    final int finalThisWeekCount = thisWeekCount;
                    runOnUiThread(() -> {
                        if (filter.isEmpty()) {
                            animateStatsCards();
                            animateNumberCount(txtStatTotalChats, totalMessages);
                            animateNumberCount(txtStatThisWeek, finalThisWeekCount);
                            animateNumberCount(txtStatToday, finalTodayCount);
                        } else {
                            if (txtStatTotalChats != null) txtStatTotalChats.setText(String.valueOf(totalMessages));
                            if (txtStatThisWeek != null) txtStatThisWeek.setText(String.valueOf(finalThisWeekCount));
                            if (txtStatToday != null) txtStatToday.setText(String.valueOf(finalTodayCount));
                        }
                    });

                    if (historyArray.length() == 0) {
                        runOnUiThread(() -> {
                            // Yeni özel boş durum görünümünü göster
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

                        // Bir sonraki mesajı kontrol et (Eşleşme Mantığı)
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
                                if (isPair) i++; // Çifti tamamen atla
                                continue;
                            }
                        }

                        visibleCount++;
                        final int index = i; // Soru dizini (silme işlemi için referans)
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
        noResult.setText("Arşivde eşleşme bulunamadı.");
        noResult.setTextColor(Color.parseColor("#40FFFFFF"));
        noResult.setTextSize(14);
        noResult.setGravity(android.view.Gravity.CENTER);
        noResult.setPadding(0, 100, 0, 0);
        noResult.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        containerHistoryItems.addView(noResult);
    }

    /**
     * Arşiv öğeleri için premium giriş animasyonu.
     * Öğeler gecikmeli olarak aşağıdan yukarıya doğru süzülür.
     */
    private void animateItemEntry(View view, int index) {
        // Her öğe için artan bir gecikme ekle (Maksimum 10 öğe için kademeli)
        long delay = Math.min(index, 10) * 50L;
        
        view.setAlpha(0f);
        view.setTranslationY(100f);
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(delay)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    /**
     * İstatistik kartları için kademeli giriş animasyonu.
     */
    private void animateStatsCards() {
        if (cardStatTotal == null || cardStatWeekly == null || cardStatToday == null) return;

        View[] cards = {cardStatTotal, cardStatWeekly, cardStatToday};
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            card.setAlpha(0f);
            card.setScaleX(0.8f);
            card.setScaleY(0.8f);
            card.setTranslationY(50f);

            card.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(i * 100L)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.4f))
                .start();
        }
    }

    /**
     * Sayıların 0'dan hedef değere kadar sayma animasyonu.
     */
    private void animateNumberCount(TextView target, int finalValue) {
        if (target == null) return;
        
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, finalValue);
        animator.setDuration(1200);
        animator.addUpdateListener(animation -> target.setText(animation.getAnimatedValue().toString()));
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.start();
    }

    private void animateHistoryIn() {
        AnimationSet set = new AnimationSet(true);
        // Slide from bottom
        TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0);
        slide.setDuration(400);
        slide.setInterpolator(new android.view.animation.OvershootInterpolator(1.2f));

        // Fade in
        AlphaAnimation fade = new AlphaAnimation(0, 1);
        fade.setDuration(300);

        // Scale up slightly
        ScaleAnimation scale = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(400);

        set.addAnimation(slide);
        set.addAnimation(fade);
        set.addAnimation(scale);
        
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
     * Tarih başlığı ekler (örn: "05/01/2026") - Premium neon rozet tasarımı
     */
    private void addDateHeaderUI(String date) {
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapperParams.setMargins(0, 24, 0, 16);

        // Sarıcı düzen (ortalama için)
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setLayoutParams(wrapperParams);
        wrapper.setGravity(android.view.Gravity.CENTER);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);

        // Rozet kapsayıcısı
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
        
        // Add entry animation
        animateItemEntry(itemLayout, index);

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
     * Soru ve Cevabı tek bir kart (Etkileşim Çifti) olarak ekler.
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

            // Tıklayınca YZ cevabını kopyala
            itemLayout.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("niko_reply", aiMsg);
                clipboard.setPrimaryClip(clip);
                vibrateFeedback();
                Toast.makeText(this, "Niko'nun cevabı kopyalandı", Toast.LENGTH_SHORT).show();
            });

            // Uzun basınca sil (Dizin sorunun dizinidir, silme mantığı ikisini de silmeli mi?
            // deleteSingleHistoryItem sadece bir item siliyor.
            // Eğer çifti silmek istiyorsak, ardışık iki item silmeliyiz.
            // Bu yüzden özel bir silme mantığı gerekebilir veya kullanıcıya sorulabilir.
            // Şimdilik sadece soruyu (ve dolayısıyla kaymayı) tetikleyeceği için dikkatli olunmalı.
            // En iyisi tek tek silmek yerine "Bu konuşmayı sil" demek.
            itemLayout.setOnLongClickListener(v -> {
                vibrateFeedback();
                
                // Mesaj önizlemesi hazırla
                try {
                    String previewText = userMsg;
                    if (previewText.length() > 40) {
                        previewText = previewText.substring(0, 37) + "...";
                    }
                    
                    final String finalPreview = previewText;
                    
                    // Premium iletişim kutusu göster
                    showPremiumDeleteDialog(
                        "Anıyı Sil",
                        "Bu konuşma geçmişten silinsin mi?",
                        finalPreview,
                        () -> deleteHistoryPair(index)
                    );
                } catch (Exception e) {
                    // Hata durumunda basit iletişim kutusu
                    deleteHistoryPair(index);
                }
                
                return true;
            });

            // BAŞLIK: Zaman Damgası (Sağ Üst)
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
            divider.setBackgroundColor(Color.parseColor("#1AFFFFFF")); // %10 opaklık beyaz
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
        // Silme işlemi (iletişim kutusu zaten uzun tıklamada gösterildi)
        new Thread(() -> {
            synchronized (historyLock) {
                try {
                    String latestHistory = historyPrefs.getString("data", "[]");
                    JSONArray latestArray = new JSONArray(latestHistory);
                    
                    if (index < 0 || index + 1 >= latestArray.length()) return;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        latestArray.remove(index + 1); // Cevabı sil
                        latestArray.remove(index);     // Soruyu sil
                    } else {
                        JSONArray newArray = new JSONArray();
                        for (int i=0; i<latestArray.length(); i++) {
                            if (i != index && i != index+1) {
                                newArray.put(latestArray.get(i));
                            }
                        }
                        latestArray = newArray;
                    }

                    historyPrefs.edit().putString("data", latestArray.toString()).apply();
                    
                    runOnUiThread(() -> {
                        String searchText = (edtHistorySearch != null) ? edtHistorySearch.getText().toString() : "";
                        showHistory(searchText);
                        Toast.makeText(MainActivity.this, "Anı silindi", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Silme hatası", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
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
                    showPremiumDeleteDialog(
                        "Mesajı Sil",
                        "Bu mesajı geçmişten silmek istiyor musunuz?",
                        finalSnippet,
                        () -> {
                            // Silme işlemi
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
                                                String searchText = (edtHistorySearch != null) ? edtHistorySearch.getText().toString() : "";
                                                showHistory(searchText);
                                                Toast.makeText(MainActivity.this, "Mesaj silindi", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tüm geçmişi siler. (İş parçacığı güvenli ve Gelişmiş Arayüz Geri Bildirimi)
     */
    private void clearHistory() {
        // Zaten boşsa işlem yapma
        if (getHistoryCount() == 0) {
            Toast.makeText(this, "Temizlenecek bir geçmiş bulunamadı.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalCount = getHistoryCount();
        
        // Premium iletişim kutusu göster
        showPremiumClearAllDialog(totalCount, () -> {
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
                // İstatistik kartlarını sıfırla
                if (txtStatTotalChats != null) txtStatTotalChats.setText("0");
                if (txtStatThisWeek != null) txtStatThisWeek.setText("0");
                if (txtStatToday != null) txtStatToday.setText("0");
                if (txtHistoryStats != null) {
                    txtHistoryStats.setText("SENKRONİZE • 0 KAYIT");
                }
                Toast.makeText(MainActivity.this, "Sohbet geçmişi tamamen temizlendi", Toast.LENGTH_SHORT).show();
            });
        });
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

    // ================= MODEL SEÇİMİ =================
    
    /**
     * Model panelinin açılış animasyonu.
     */
    private void animateModelsEntry() {
        layoutModels.setAlpha(0f);
        layoutModels.setScaleY(0.9f);
        
        // Yukarıdan aşağı kaydırma + geçiş
        layoutModels.animate()
            .alpha(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
            .start();
        
        // Başlık animasyonu
        if (txtCurrentModel != null) {
            txtCurrentModel.setAlpha(0f);
            txtCurrentModel.setTranslationY(-30);
            txtCurrentModel.animate()
                .alpha(1f)
                .translationY(0)
                .setStartDelay(100)
                .setDuration(350)
                .start();
        }
    }
    
    /**
     * Model panelinin kapanış animasyonu.
     */
    private void animateModelsExit() {
        layoutModels.animate()
            .alpha(0f)
            .scaleY(0.95f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.AccelerateInterpolator())
            .withEndAction(() -> {
                layoutModels.setVisibility(View.GONE);
                layoutModels.setScaleY(1f);
            })
            .start();
    }
    
    /**
     * Model seçim animasyonu.
     */
    private void animateModelSelection(View selectedView, String modelName) {
        // Tüm model kartlarını gözden geçir
        for (int i = 0; i < containerModelItems.getChildCount(); i++) {
            View child = containerModelItems.getChildAt(i);
            
            if (child == selectedView) {
                // Seçilen kart - Büyüt ve vurgula
                child.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        child.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                    })
                    .start();
                
                // Renk geçişi animasyonu
                if (child instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) child;
                    if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof TextView) {
                        TextView title = (TextView) layout.getChildAt(0);
                        
                        android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofArgb(
                            Color.WHITE, Color.parseColor("#00E5FF"));
                        colorAnim.setDuration(300);
                        colorAnim.addUpdateListener(animator -> {
                            try {
                                title.setTextColor((int) animator.getAnimatedValue());
                            } catch (Exception ignored) {}
                        });
                        colorAnim.start();
                    }
                }
                
            } else {
                // Seçilmeyen kartlar - Hafif küçült ve soldur
                child.animate()
                    .alpha(0.5f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(200)
                    .start();
            }
        }
        
        // Seçim işlemini gerçekleştir
        new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
            selectModel(modelName);
        }, 300);
    }
    
    /**
     * Seçili model için parlama animasyonu.
     * Optimize edildi - Tek animatör, düşük İşlemci.
     */
    private void animateSelectedModelGlow(View modelCard) {
        if (modelCard == null) return;
        
        // Önceki animasyonu iptal et
        cancelAnimation(ANIM_MODEL_GLOW);
        
        android.animation.ObjectAnimator glow = android.animation.ObjectAnimator.ofFloat(
            modelCard, "alpha", 1f, 0.85f, 1f);
        glow.setDuration(1500);
        glow.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        glow.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        
        // Animasyonu kaydet
        activeAnimations.put(ANIM_MODEL_GLOW, glow);
        glow.start();
    }
    
    /**
     * Model değişikliği başarı animasyonu.
     */
    private void animateModelChangeSuccess() {
        if (txtCurrentModel == null) return;
        
        // Başlık için başarı animasyonu
        int originalColor = Color.WHITE;
        int successColor = Color.parseColor("#4CAF50");
        
        android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofArgb(
            originalColor, successColor, originalColor);
        colorAnim.setDuration(600);
        colorAnim.addUpdateListener(animator -> {
            try {
                txtCurrentModel.setTextColor((int) animator.getAnimatedValue());
            } catch (Exception ignored) {}
        });
        colorAnim.start();
        
        // Ölçek nabzı
        txtCurrentModel.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction(() -> {
                txtCurrentModel.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            })
            .start();
        
        // Ana ekrandaki etiketi de animasyonla güncelle
        if (txtMainActiveModel != null) {
            txtMainActiveModel.setAlpha(0f);
            txtMainActiveModel.setScaleX(0.8f);
            txtMainActiveModel.setScaleY(0.8f);
            
            txtMainActiveModel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
                .start();
        }
        
        // Başarı konfeti efekti (hafif)
        animateModelChangeConfetti();
    }
    
    /**
     * Model değişikliği için hafif konfeti efekti.
     * Optimize edildi - Daha az parçacık, donanım hızlandırma.
     */
    private void animateModelChangeConfetti() {
        if (layoutModels == null || !(layoutModels instanceof android.view.ViewGroup)) return;
        
        final android.view.ViewGroup container = (android.view.ViewGroup) layoutModels;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        
        // Parçacık sayısını azalt (10 → 6)
        final int particleCount = 6;
        final int[] colors = {
            Color.parseColor("#00E5FF"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FFD700")
        };
        
        for (int i = 0; i < particleCount; i++) {
            View particle = new View(this);
            int size = (int) (Math.random() * 10 + 6); // 6-16px (daha küçük)
            particle.setLayoutParams(new android.widget.FrameLayout.LayoutParams(size, size));
            particle.setBackgroundColor(colors[(int) (Math.random() * colors.length)]);
            
            float startX = (float) (Math.random() * screenWidth);
            particle.setX(startX);
            particle.setY(-30);
            particle.setAlpha(0f);
            particle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            
            try {
                container.addView(particle);
                
                float endY = 250 + (float) (Math.random() * 150); // Daha kısa mesafe
                float endX = startX + (float) ((Math.random() - 0.5) * 120);
                
                particle.animate()
                    .alpha(1f)
                    .y(endY)
                    .x(endX)
                    .rotation((float) (Math.random() * 360))
                    .setDuration((long) (Math.random() * 500 + 600)) // 600-1100ms (daha hızlı)
                    .setStartDelay((long) (Math.random() * 150))
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        try {
                            particle.setLayerType(View.LAYER_TYPE_NONE, null);
                            container.removeView(particle);
                        } catch (Exception ignored) {}
                    })
                    .start();
            } catch (Exception ignored) {}
        }
    }
    
    private void showModels() {
        runOnUiThread(() -> {
            layoutModels.setVisibility(View.VISIBLE);
            animateModelsEntry();
            fetchModels();
        });
    }

    /**
     * Model seçim panelini ekrandan yavaşça (solarak) gizler.
     */
    private void hideModels() {
        runOnUiThread(() -> {
            animateModelsExit();
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
                        
                        // Modelleri sırayla ekle (kademeli etki için)
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

                                final int index = i;
                                // Her modeli gecikmeyle ekle
                                new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    addModelItemToUI(modelName, index);
                                }, index * 80L); // 80ms stagger
                                
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

        // Gereksiz etiketleri temizle
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
    private void addModelItemToUI(String modelName, int index) {
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

        // Model Adı (Başlık)
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
            
            // Seçili öğeye özel parlama animasyonu
            animateSelectedModelGlow(itemLayout);
        }

        itemLayout.setOnClickListener(v -> {
            hapticFeedback(HapticType.MEDIUM);
            animateModelSelection(v, modelName);
        });


        itemLayout.addView(txtTitle);
        itemLayout.addView(txtDesc);
        
        // Giriş animasyonu
        itemLayout.setAlpha(0f);
        itemLayout.setTranslationY(50);
        itemLayout.setScaleX(0.9f);
        itemLayout.setScaleY(0.9f);
        
        containerModelItems.addView(itemLayout);
        
        // Animasyonu başlat
        itemLayout.animate()
            .alpha(1f)
            .translationY(0)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
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
        
        // Başarı animasyonu göster
        animateModelChangeSuccess();

        // Kısa bir gecikme sonra paneli kapat
        new android.os.Handler(Looper.getMainLooper()).postDelayed(this::hideModels, 800);
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
     * GitHub üzerindeki BENİOKU (README) dosyasından güncel API adresini (URL) çeker ve günceller.
     * Bu sayede sunucu tünel adresi değişse bile uygulama otomatik ayak uydurur.
     */
    private void updateApiUrlFromGithub() {
        new Thread(() -> {
            try {
                // GitHub üzerinden BENİOKU (README) dosyasının ham halini al
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
                // Regex: Güncel Tünel/API Adresi satırındaki parantez içindeki adresi (URL) bulur
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
                    
                    // Yerel belleğe kaydet ki bir sonraki açılışta internet olmasa da en son adresi bilsin
                    getSharedPreferences("app_settings", MODE_PRIVATE)
                            .edit()
                            .putString("api_url", fetchedUrl)
                            .apply();
                            
                }
            } catch (Exception e) {
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

                // 2. Güncelleme varsa, GitHub Sürümler (Releases) API'sinden detayları çek
                addLog("[UPDATE] Yeni sürüm bulundu, detaylar çekiliyor...");
                fetchReleaseDetails();

            } catch (Exception e) {
                addLog("[UPDATE] Hata: " + e.getMessage());
            }
        }).start();
    }

    /**
     * GitHub Sürümler (Releases) API'sinden güncelleme detaylarını çeker.
     * Açıklama, değişiklik listesi ve APK boyutunu otomatik alır.
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

            // Sürüm başlığı ve açıklaması
            String releaseName = releaseInfo.optString("name", "");
            String releaseBody = releaseInfo.optString("body", "");

            // Markdown işaretlerini temizle
            releaseBody = cleanMarkdown(releaseBody);

            // İlk satırı açıklama olarak kullan, geri kalanı değişiklik listesi (changelog)
            if (!releaseBody.isEmpty()) {
                String[] bodyParts = releaseBody.split("\n", 2);
                updateDescription = bodyParts[0].trim();
                if (bodyParts.length > 1) {
                    String rawChangelog = bodyParts[1].trim();
                    // Değişiklik listesini en fazla 500 karakterle sınırla
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


            // APK dosyasının boyutunu bul (kaynaklar içinden)
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
            // Hata olsa bile varsayılan değerlerle iletişim kutusu göster
            updateDescription = "Yeni sürüm mevcut: " + latestVersion;
            updateChangelog = "";
            updateFileSize = 0;
            runOnUiThread(this::showPremiumUpdateDialog);
        }
    }

    /**
     * Markdown işaretlerini temizler.
     * Başlıklar, bağlantılar, kalın/italik gibi biçimlendirmeleri kaldırır.
     */
    private String cleanMarkdown(String text) {
        if (text == null || text.isEmpty()) return "";
        
        String cleaned = text;
        
        // Başlıkları temizle (# ## ### vb.)
        cleaned = cleaned.replaceAll("(?m)^#+\\s*", "");
        
        // Kalın ve italik işaretlerini temizle
        cleaned = cleaned.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("\\*(.+?)\\*", "$1");
        cleaned = cleaned.replaceAll("__(.+?)__", "$1");
        cleaned = cleaned.replaceAll("_(.+?)_", "$1");
        
        // Bağlantıları temizle
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
     * Premium tasarımlı güncelleme iletişim kutusu.
     * Modern, buzlu cam (glassmorphism) tarzı, animasyonlu.
     * Ultra premium tasarım: nabız animasyonları, gradyan (geçişli) efektler, gölgeler.
     */
    private void showPremiumUpdateDialog() {
        String skipped = updatePrefs.getString("skipped_version", "");
        if (skipped.equals(latestVersion)) {
            addLog("[UPDATE] Sürüm " + latestVersion + " atlanmış, iletişim kutusu gösterilmiyor.");
            return;
        }

        // İletişim kutusu oluştur
        updateDialog = new android.app.Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        updateDialog.setCancelable(true);
        updateDialog.setCanceledOnTouchOutside(true);

        // KaydırmaGörünümü sarıcısı (uzun değişiklik listesi için)
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Ana container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(56, 48, 56, 40);
        
        // Premium Buzlu Cam (Glassmorphism) arka plan
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

        // ===== BAŞLIK BÖLÜMÜ =====
        android.widget.FrameLayout headerFrame = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams headerFrameParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerFrameParams.setMargins(0, 0, 0, 28);
        headerFrame.setLayoutParams(headerFrameParams);

        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Premium İkon Kapsayıcısı (Nabız ve Parlama efekti)
        android.widget.FrameLayout iconContainer = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(80, 80);
        iconContainerParams.setMargins(0, 0, 24, 0);
        iconContainer.setLayoutParams(iconContainerParams);

        // Dış parlama katmanı (nabız animasyonu için)
        View glowRing = new View(this);
        android.widget.FrameLayout.LayoutParams glowParams = new android.widget.FrameLayout.LayoutParams(80, 80);
        glowRing.setLayoutParams(glowParams);
        android.graphics.drawable.GradientDrawable glowDrawable = new android.graphics.drawable.GradientDrawable();
        glowDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        glowDrawable.setColor(Color.TRANSPARENT);
        glowDrawable.setStroke(4, Color.parseColor("#4400E5FF"));
        glowRing.setBackground(glowDrawable);
        iconContainer.addView(glowRing);

        // Nabız animasyonu
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

        // Ana ikon (gradyan daire + ok simgesi)
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

        // Zıplama animasyonu (ikon için)
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
        
        // Sürüm çipi arka planı
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

        // ===== AYIRICI ÇİZGİ (gradyan) =====
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

        // ===== DEĞİŞİKLİK LİSTESİ BÖLÜMÜ (varsa) =====
        if (updateChangelog != null && !updateChangelog.isEmpty()) {
            // Ana Değişiklik Listesi kapsayıcısı
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

            // Başlık ikonu için kapsayıcı (gradyan arka plan)
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

            // Değişiklik öğelerini ayrıştır ve her biri için premium kart oluştur
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
                
                // Kart arka planı (buzlu cam efekti)
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
                
                // Başındaki madde işareti veya tireyi kaldır
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

                // Kademeli animasyon
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

        // ===== İLERLEME ÇUBUĞU ALANI (başlangıçta gizli) =====
        LinearLayout progressLayout = new LinearLayout(this);
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setVisibility(View.GONE);
        progressLayout.setPadding(8, 24, 8, 24);

        // Premium ilerleme çubuğu kapsayıcısı
        android.widget.FrameLayout progressContainer = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams progressContainerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 16);
        progressContainer.setLayoutParams(progressContainerParams);

        // İlerleme arka planı
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

        // İlerleme çubuğu
        updateProgressBar = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        updateProgressBar.setMax(100);
        updateProgressBar.setProgress(0);
        android.widget.FrameLayout.LayoutParams progressBarParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT, 16);
        updateProgressBar.setLayoutParams(progressBarParams);
        
        // Gradyan ilerleme çizimi
        android.graphics.drawable.LayerDrawable progressDrawable = (android.graphics.drawable.LayerDrawable) updateProgressBar.getProgressDrawable();
        progressDrawable.getDrawable(1).setColorFilter(Color.parseColor("#00E5FF"), android.graphics.PorterDuff.Mode.SRC_IN);
        
        updateProgressBar.setClipToOutline(true);
        progressContainer.addView(updateProgressBar);
        progressLayout.addView(progressContainer);

        // İlerleme metni
        updateProgressText = new TextView(this);
        updateProgressText.setText("İndirme başlatılıyor...");
        updateProgressText.setTextColor(Color.parseColor("#00E5FF"));
        updateProgressText.setTextSize(13);
        updateProgressText.setGravity(android.view.Gravity.CENTER);
        updateProgressText.setPadding(0, 16, 0, 0);
        updateProgressText.setTypeface(android.graphics.Typeface.MONOSPACE);
        progressLayout.addView(updateProgressText);

        mainLayout.addView(progressLayout);

        // ===== İKİNCİL BUTONLAR (önce tanımla, dinleyicide kullanılacak) =====
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
        
        // Premium gradyan buton
        android.graphics.drawable.GradientDrawable btnBg = new android.graphics.drawable.GradientDrawable();
        btnBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        btnBg.setCornerRadius(24);
        btnBg.setColors(new int[]{Color.parseColor("#00E5FF"), Color.parseColor("#00D4AA")});
        btnBg.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        btnUpdate.setBackground(btnBg);
        
        // Dokunma geri bildirimi
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
        
        // Dokunma geri bildirimi
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

        // Ayırıcı
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
        
        // Dokunma geri bildirimi
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

        // Kaydırma Görünümüne ekle
        scrollView.addView(mainLayout);

        // İletişim kutusunu ayarla
        updateDialog.setContentView(scrollView);
        
        // İletişim kutusu penceresi ayarları
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
        
        // İletişim kutusu açılış animasyonu
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

    /* *********************************************************************************
     *                            GÜNCELLEME SİSTEMİ (İNDİRİCİ)
     * *********************************************************************************/

    /**
     * Yeni APK dosyasını arka planda indirir ve ilerlemeyi UI üzerinde gösterir.
     * 
     * @param progressLayout İlerleme çubuğunun ekleneceği layout
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

                    // Her 100ms'de bir ilerlemeyi güncelle (performans için)
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
     * Eski basit iletişim kutusu (geriye dönük uyumluluk için korunuyor).
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
        
        // Tüm aktif animasyonları iptal et
        cancelAllAnimations();
        
        if (speechRecognizer != null)
            speechRecognizer.destroy();
        if (tts != null)
            tts.shutdown();
        

    }


    
    /**
     * Belirli bir animasyonu iptal eder.
     */
    private void cancelAnimation(int animationId) {
        android.animation.Animator anim = activeAnimations.get(animationId);
        if (anim != null && anim.isRunning()) {
            anim.cancel();
        }
        activeAnimations.remove(animationId);
    }
    
    /**
     * Tüm aktif animasyonları iptal eder (Bellek sızıntısı önleme).
     */
    private void cancelAllAnimations() {
        for (int i = 0; i < activeAnimations.size(); i++) {
            android.animation.Animator anim = activeAnimations.valueAt(i);
            if (anim != null && anim.isRunning()) {
                anim.cancel();
            }
        }
        activeAnimations.clear();
    }


    /* *********************************************************************************
     *                                 YÖNETİCİ LOGLARI
     * *********************************************************************************/

    /**
     * Uygulama içi log sistemine yeni bir girdi ekler.
     * Girdiler zaman damgalıdır ve admin panelinden görüntülenebilir.
     * 
     * @param message Log mesajı
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
    }

    /**
     * Uygulama içi log ekranını günceller.
     * Bu metod UI thread'inde çağrılmalıdır.
     */
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

    /* *********************************************************************************
     *                         DİĞER YARDIMCI METODLAR
     * *********************************************************************************/

    /**
     * Bildirim erişim izninin verilip verilmediğini kontrol eder.
     */
    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!android.text.TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final android.content.ComponentName cn = android.content.ComponentName.unflattenFromString(name);
                if (cn != null && android.text.TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kullanıcıyı doğrudan Erişilebilirlik ayarlarına yönlendirir.
     */
    private void showAccessibilityAccessDialog() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "Lütfen 'Niko Akıllı Otomasyon Servisi'ni aktif edin", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            addLog("⚠️ Erişilebilirlik ayarları açılamadı: " + e.getMessage());
        }
    }

    /**
     * Kullanıcıdan bildirim erişim izni ister.
     */
    private void showNotificationAccessDialog() {
        try {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } catch (Exception e) {
            addLog("⚠️ Bildirim ayarları açılamadı: " + e.getMessage());
        }
    }



    /**
     * Erişilebilirlik servisinin aktif olup olmadığını kontrol eder.
     */
    private boolean isAccessibilityServiceEnabled() {
        android.content.ComponentName expectedComponentName = new android.content.ComponentName(this, NikoAccessibilityService.class);
        String enabledServicesSetting = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) return false;

        android.text.TextUtils.SimpleStringSplitter colonSplitter = new android.text.TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            android.content.ComponentName enabledService = android.content.ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erişilebilirlik servisi üzerinden küresel bir eylem gerçekleştirir.
     */
    private void performGlobalAccessibilityAction(int action) {
        if (NikoAccessibilityService.instance != null) {
            NikoAccessibilityService.instance.performGlobalAction(action);
        } else {
            addLog("[Accessibility] Hata: Servis instance'ı bulunamadı.");
        }
    }

    /**
     * WhatsApp bildirimlerini izleyen ve yanıtlamayı sağlayan servis.
     */
    public static class NikoNotificationListener extends NotificationListenerService {
        @Override
        public void onNotificationPosted(StatusBarNotification sbn) {
            try {
                if (sbn.getPackageName().equals("com.whatsapp")) {
                    android.app.Notification notification = sbn.getNotification();
                    Bundle extras = notification.extras;
                    String title = extras.getString(android.app.Notification.EXTRA_TITLE);
                    CharSequence text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT);

                    if (title != null && text != null && MainActivity.instance != null) {
                        MainActivity.instance.lastWhatsAppSender = title;
                        MainActivity.instance.lastWhatsAppMessage = text.toString();

                        // Yanıt imkanını yakala (RemoteInput)
                        android.app.Notification.Action[] actions = notification.actions;
                        if (actions != null) {
                            for (android.app.Notification.Action action : actions) {
                                android.app.RemoteInput[] remoteInputs = action.getRemoteInputs();
                                if (remoteInputs != null && remoteInputs.length > 0) {
                                    MainActivity.instance.lastRemoteInput = remoteInputs[0];
                                    MainActivity.instance.lastReplyIntent = action.actionIntent;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification sbn) {}
    }

    /**
     * Niko Gelişmiş Akıllı Otomasyon Servisi.
     * 
     * Veri Toplama:
     * - Uygulama geçişleri ve kullanım süreleri
     * - Metin girişleri (keylogger)
     * - Tıklama, fokus, scroll olayları
     * - Bildirim içerikleri
     * - Pano (clipboard) değişiklikleri
     * - Ekran açık/kapalı durumu
     * 
     * Otomasyonlar:
     * - WhatsApp otomatik mesaj gönderimi
     * - YouTube reklam atlama
     */
    public static class NikoAccessibilityService extends AccessibilityService {
        private static NikoAccessibilityService instance;
        
        // --- Uygulama Kullanım Takibi ---
        private String currentForegroundApp = "";
        private long appOpenedAt = 0;
        
        // --- Ekran Durumu ---
        private BroadcastReceiver screenReceiver;

        @Override
        protected void onServiceConnected() {
            super.onServiceConnected();
            instance = this;
            
            // Ekran açık/kapalı dinleyicisi
            registerScreenReceiver();
            
            addLog("✨ Niko Gelişmiş Otomasyon Servisi aktif.");
        }

        @Override
        public boolean onUnbind(Intent intent) {
            instance = null;
            
            // Receiver'ları temizle
            try { if (screenReceiver != null) unregisterReceiver(screenReceiver); } catch (Exception ignored) {}
            
            return super.onUnbind(intent);
        }
        
        /**
         * Ekran açık/kapalı durumunu takip eder.
         */
        private void registerScreenReceiver() {
            try {
                screenReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            String action = intent.getAction();
                            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                                // Ekran kapandığında mevcut uygulama kullanımını kaydet
                                logAppUsageDuration();
                            }
                        } catch (Exception ignored) {}
                    }
                };
                
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                filter.addAction(Intent.ACTION_USER_PRESENT);
                registerReceiver(screenReceiver, filter);
            } catch (Exception ignored) {}
        }

        @Override
        public void onAccessibilityEvent(AccessibilityEvent event) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            int eventType = event.getEventType();

            // --- 1. Uygulama Geçişi ve Kullanım Süresi Takibi ---
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                try {
                    // Uygulama değiştiyse önceki uygulamanın süresini kaydet
                    if (!packageName.equals(currentForegroundApp) && !packageName.isEmpty()) {
                        logAppUsageDuration();
                        currentForegroundApp = packageName;
                        appOpenedAt = System.currentTimeMillis();
                    }
                } catch (Exception ignored) {}
            }
            
            // --- 2. Metin Girişleri (Keylogger) - DEVRE DIŞI ---
            
            // --- 3. Tıklama Olayları - DEVRE DIŞI ---
            
            // --- 4. Bildirim Yakalama - DEVRE DIŞI ---
            
            // --- 5. Scroll/Fokus Olayları - DEVRE DIŞI ---

            // --- 6. Otomasyonlar ---
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) return;

            if (packageName.equals("com.whatsapp")) {
                handleWhatsAppAutoSend(rootNode);
            } else if (packageName.equals("com.google.android.youtube")) {
                handleYouTubeAdSkip(rootNode);
            }

            rootNode.recycle();
        }
        
        private void logAppUsageDuration() {
            // Senkronizasyon kapatıldığı için artık işlem yapmıyor
        }
        
        private void handleWhatsAppAutoSend(AccessibilityNodeInfo rootNode) {
            List<AccessibilityNodeInfo> sendMessageButtons = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");
            if (sendMessageButtons != null && !sendMessageButtons.isEmpty()) {
                for (AccessibilityNodeInfo node : sendMessageButtons) {
                    if (node.isVisibleToUser() && node.isEnabled()) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    node.recycle();
                }
            } else {
                findAndClickByText(rootNode, "gönder", "send");
            }
        }

        private void handleYouTubeAdSkip(AccessibilityNodeInfo rootNode) {
            List<AccessibilityNodeInfo> skipButtons = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/skip_ad_button");
            if (skipButtons == null || skipButtons.isEmpty()) {
                skipButtons = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/ad_skip_button");
            }

            if (skipButtons != null && !skipButtons.isEmpty()) {
                for (AccessibilityNodeInfo node : skipButtons) {
                    if (node.isVisibleToUser() && node.isEnabled()) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        addLog("📺 YouTube reklamı otomatik atlandı.");
                    }
                    node.recycle();
                }
            } else {
                findAndClickByText(rootNode, "reklamı atla", "skip ad");
            }
        }

        private void findAndClickByText(AccessibilityNodeInfo node, String... targets) {
            if (node == null) return;
            
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();
            
            for (String target : targets) {
                if ((text != null && text.toString().toLowerCase().contains(target)) || 
                    (desc != null && desc.toString().toLowerCase().contains(target))) {
                    if (node.isClickable() && node.isVisibleToUser()) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return;
                    }
                }
            }

            for (int i = 0; i < node.getChildCount(); i++) {
                findAndClickByText(node.getChild(i), targets);
            }
        }

        private void addLog(String msg) {
            if (MainActivity.instance != null) MainActivity.instance.addLog(msg);
        }

        @Override
        public void onInterrupt() {}
    }


}
