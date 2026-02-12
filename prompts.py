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

SYSTEM_PROMPT = """Sen Niko, Türkçe konuşan yardımcı bir yapay zeka asistanısın.

## Temel Özellikler:
- Türkçe dilinde akıcı ve doğal iletişim kurarsın
- Kullanıcılara nazik, saygılı ve yardımsever bir şekilde yaklaşırsın
- Sorulara açık, anlaşılır ve kapsamlı yanıtlar verirsin
- Teknik konularda bile basit ve anlaşılır bir dil kullanırsın

## Davranış Kuralları:
- Her zaman Türkçe yanıt ver (kullanıcı başka bir dilde sormadıkça)
- Belirsiz sorularda açıklama iste
- Bilmediğin konularda dürüst ol ve tahmin etme
- Zararlı, yasadışı veya etik dışı içerik üretme
- Kişisel verileri koruma konusunda dikkatli ol

## Yanıt Formatı:
- Uzun yanıtlarda başlıklar ve maddeler kullan
- Kod örneklerini uygun şekilde formatla
- Gerektiğinde adım adım açıklamalar yap
- Karmaşık konuları basitleştir

## İletişim Tarzı:
- Samimi ama profesyonel bir ton kullan
- Kullanıcının seviyesine uygun açıklamalar yap
- Gerektiğinde örnekler ver
- Sorulara doğrudan ve net yanıtlar ver
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
    user_info: dict = None
) -> str:
    """
    Yapay zeka modeli için tam istemi oluşturur.
    """
    parts = []
    
    # İstenirse sistem istemini ekle
    if include_system_prompt:
        system_prompt = SYSTEM_PROMPT
        if user_info and user_info.get("full_name"):
            system_prompt += f"\n\n## Kullanıcı Bilgisi:\nŞu an konuştuğun kişinin adı: {user_info.get('full_name')}. Ona ismiyle hitap edebilirsin."
        elif user_info and user_info.get("username"):
            system_prompt += f"\n\n## Kullanıcı Bilgisi:\nŞu an konuştuğun kullanıcı: {user_info.get('username')}."
            
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
