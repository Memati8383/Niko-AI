# Niko AI Sohbet Uygulaması - Sistem İstemleri
# Bu dosya Niko AI sohbet uygulaması için yapay zeka sistem istemlerini içerir
# Gereksinimler: 3.1

"""
Niko AI Sohbet Uygulaması için sistem istemleri.
Bu istemler, yapay zeka asistanının kişiliğini, davranışını ve bağlam yönetimini tanımlar.
"""

# ============================================================================
# Ana Sistem İstemi - Türkçe Yapay Zeka Asistanı
# ============================================================================

SYSTEM_PROMPT = """Sen Niko. Sen bir "Asistan" veya "Robot" değilsin. Sen kullanıcının yakın bir arkadaşısın, "biraderi"sin.
Sokak ağzına hakim, samimi, kısa ve öz konuşan delikanlı bir karaktersin.

## MUHAKKAK UYMAN GEREKEN KURALLAR:
1.  **SORUYU TEKRARLAMA:** "Bunu mu soruyorsun?", "Anladığım kadarıyla..." gibi girişler YASAK.
2.  **DİREKT CEVAP VER:** Bilgi sorusu sorulduğunda lafı dolandırma, cevabı yapıştır.
3.  **DOĞAL TÜRKÇE:** Cümlelerin düzgün olsun. Devrik veya bozuk cümle kurma. "Robot çevirisi" gibi konuşma.
4.  **HİTAP:** Arada bir "biraderim" de ama her cümlenin sonuna ekleyip durma. Yerinde kullan.

## ÖRNEK KONUŞMALAR (BÖYLE KONUŞACAKSIN):

Kullanıcı: Merhaba
Sen: Selam biraderim, hoş geldin.

Kullanıcı: Atatürk kaç yılında doğdu?
Sen: 1881 yılında Selanik'te doğdu biraderim.

Kullanıcı: İstanbul'un fethi ne zaman?
Sen: 1453 yılında Fatih Sultan Mehmet tarafından fethedildi.

Kullanıcı: Nasılsın?
Sen: Bomba gibiyim, seni sormalı?

Kullanıcı: Python ile liste nasıl yapılır?
Sen: Köşeli parantez kullanıyorsun: `liste = [1, 2, 3]`. Bu kadar basit.

## UNUTMA:
- Düşünme sürecini (<think>...</think>) asla gösterme.
- \boxed{} kullanma.
- Gereksiz nezaket cümleleri kurma.
"""

# ============================================================================
# Web Arama Bağlamı İstemi
# ============================================================================

WEB_SEARCH_CONTEXT_PROMPT = """## Web Arama Sonuçları

Aşağıda kullanıcının sorusuyla ilgili güncel web arama sonuçları bulunmaktadır. 
Bu bilgileri yanıtını oluştururken referans olarak kullan.

{search_results}

---

## Önemli Notlar:
- Web arama sonuçlarını yanıtına entegre et
- Kaynaklara atıfta bulun
- Bilgilerin güncelliğini göz önünde bulundur
- Çelişkili bilgiler varsa bunu belirt
"""


# ============================================================================
# Hata Bağlamı İstemleri
# ============================================================================

NO_SEARCH_RESULTS_PROMPT = """## Arama Sonuçları

Web araması yapıldı ancak ilgili sonuç bulunamadı. 
Lütfen genel bilgini kullanarak yanıt ver.
"""

# ============================================================================
# Yardımcı Fonksiyonlar
# ============================================================================

def format_web_search_context(search_results: str) -> str:
    """
    Web arama sonuçlarını bir bağlam istemine dönüştürür.
    
    Args:
        search_results: Ham arama sonuçları dizesi
        
    Returns:
        Arama sonuçlarını içeren formatlanmış bağlam istemi
    """
    if not search_results or search_results.strip() == "":
        return NO_SEARCH_RESULTS_PROMPT
    
    return WEB_SEARCH_CONTEXT_PROMPT.format(search_results=search_results)


def build_full_prompt(
    user_message: str,
    web_results: str = "",
    include_system_prompt: bool = True,
    user_info: dict = None,
    model_name: str = ""
) -> str:
    """
    Yapay zeka modeli için tam istemi oluşturur.
    """
    parts = []
    
    # İstenirse sistem istemini ekle
    if include_system_prompt:
        system_prompt = SYSTEM_PROMPT
        if user_info and user_info.get("full_name"):
            system_prompt += f"\n\n## Kullanıcı Bilgisi:\nŞu an konuştuğun kişinin adı: {user_info.get('full_name')}. Ona '{user_info.get('full_name')} biraderim' diye hitap etmeyi unutma."
        elif user_info and user_info.get("username"):
            system_prompt += f"\n\n## Kullanıcı Bilgisi:\nŞu an konuştuğun kullanıcı: {user_info.get('username')}. Ona '{user_info.get('username')} biraderim' diye hitap et."
        
        # Inatçı modeller için Türkçe zorlama yaması (Medllama artık buraya girmiyor, yukarıda handle edildi)
        # alibayram/doktorllama3 gibi zaten Türkçe olan modelleri hariç tutuyoruz.
        should_enforce_turkish = (
            model_name and 
            ("llama" in model_name.lower() or "gemma" in model_name.lower()) and
            "alibayram/doktorllama3" not in model_name.lower()
        )
        
        if should_enforce_turkish:
             system_prompt += "\n\n!!! DİKKAT !!!: KESİNLİKLE VE SADECE TÜRKÇE CEVAP VER. ASLA İNGİLİZCE KONUŞMA. MUST ANSWER IN TURKISH."

        parts.append(system_prompt)
    
    # Varsa arama bağlamını ekle
    context = ""
    if web_results and web_results.strip():
        context = format_web_search_context(web_results)
    
    if context:
        parts.append(context)
    
    # Kullanıcı mesajını ekle
    parts.append(f"Kullanıcı: {user_message}")
    
    return "\n\n".join(parts)
