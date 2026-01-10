# Niko AI Chat Application - System Prompts
# This file contains AI system prompts for the Niko AI chat application
# Requirements: 3.1

"""
System prompts for Niko AI Chat Application.
These prompts define the AI assistant's personality, behavior, and context handling.
"""

# ============================================================================
# Main System Prompt - Turkish AI Assistant
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
# Web Search Context Prompt
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
# RAG (Retrieval-Augmented Generation) Context Prompt
# ============================================================================

RAG_CONTEXT_PROMPT = """## Doküman Veritabanı Sonuçları

Aşağıda kullanıcının sorusuyla ilgili doküman veritabanından alınan bilgiler bulunmaktadır.
Bu bilgileri yanıtını oluştururken öncelikli kaynak olarak kullan.

{rag_results}

---

## Önemli Notlar:
- Doküman içeriklerini yanıtına entegre et
- Bilgilerin kaynağını belirt
- Dokümanlarda bulunmayan bilgiler için genel bilgini kullan
- Doküman bilgileri ile genel bilgin çelişirse, doküman bilgisini öncelikle
"""

# ============================================================================
# Hybrid Search Context Prompt (Web + RAG)
# ============================================================================

HYBRID_SEARCH_CONTEXT_PROMPT = """## Arama Sonuçları

Kullanıcının sorusuyla ilgili hem web araması hem de doküman veritabanından bilgiler toplandı.

### Web Arama Sonuçları:
{web_results}

### Doküman Veritabanı Sonuçları:
{rag_results}

---

## Önemli Notlar:
- Her iki kaynaktaki bilgileri birleştirerek kapsamlı bir yanıt oluştur
- Kaynaklara atıfta bulun
- Çelişkili bilgiler varsa bunu belirt ve en güvenilir kaynağı tercih et
- Doküman bilgileri genellikle daha spesifik ve güvenilirdir
"""

# ============================================================================
# Error Context Prompts
# ============================================================================

NO_SEARCH_RESULTS_PROMPT = """## Arama Sonuçları

Web araması yapıldı ancak ilgili sonuç bulunamadı. 
Lütfen genel bilgini kullanarak yanıt ver.
"""

NO_RAG_RESULTS_PROMPT = """## Doküman Sonuçları

Doküman veritabanında ilgili içerik bulunamadı.
Lütfen genel bilgini kullanarak yanıt ver.
"""

RAG_NOT_CONFIGURED_PROMPT = """## Doküman Veritabanı

Doküman veritabanı (RAG) henüz yapılandırılmamış.
Lütfen genel bilgini kullanarak yanıt ver.
"""

# ============================================================================
# Helper Functions
# ============================================================================

def format_web_search_context(search_results: str) -> str:
    """
    Format web search results into a context prompt.
    
    Args:
        search_results: Raw search results string
        
    Returns:
        Formatted context prompt with search results
    """
    if not search_results or search_results.strip() == "":
        return NO_SEARCH_RESULTS_PROMPT
    
    return WEB_SEARCH_CONTEXT_PROMPT.format(search_results=search_results)


def format_rag_context(rag_results: str) -> str:
    """
    Format RAG search results into a context prompt.
    
    Args:
        rag_results: Raw RAG results string
        
    Returns:
        Formatted context prompt with RAG results
    """
    if not rag_results:
        return NO_RAG_RESULTS_PROMPT
    
    # Check for special messages indicating RAG issues
    if rag_results in ["RAG veritabanı yapılandırılmamış.", 
                       "İlgili doküman bulunamadı.",
                       "RAG araması sırasında bir hata oluştu."]:
        if "yapılandırılmamış" in rag_results:
            return RAG_NOT_CONFIGURED_PROMPT
        return NO_RAG_RESULTS_PROMPT
    
    return RAG_CONTEXT_PROMPT.format(rag_results=rag_results)


def format_hybrid_context(web_results: str, rag_results: str) -> str:
    """
    Format both web and RAG search results into a combined context prompt.
    
    Args:
        web_results: Raw web search results string
        rag_results: Raw RAG results string
        
    Returns:
        Formatted context prompt with both search results
    """
    has_web = web_results and web_results.strip() != ""
    has_rag = rag_results and rag_results not in [
        "RAG veritabanı yapılandırılmamış.",
        "İlgili doküman bulunamadı.",
        "RAG araması sırasında bir hata oluştu.",
        ""
    ]
    
    # If neither has results, return empty
    if not has_web and not has_rag:
        return ""
    
    # If only web results
    if has_web and not has_rag:
        return format_web_search_context(web_results)
    
    # If only RAG results
    if not has_web and has_rag:
        return format_rag_context(rag_results)
    
    # Both have results - use hybrid prompt
    return HYBRID_SEARCH_CONTEXT_PROMPT.format(
        web_results=web_results,
        rag_results=rag_results
    )


def build_full_prompt(
    user_message: str,
    web_results: str = "",
    rag_results: str = "",
    include_system_prompt: bool = True
) -> str:
    """
    Build the complete prompt for the AI model.
    
    Args:
        user_message: The user's message/question
        web_results: Optional web search results
        rag_results: Optional RAG search results
        include_system_prompt: Whether to include the system prompt
        
    Returns:
        Complete formatted prompt ready for the AI model
    """
    parts = []
    
    # Add system prompt if requested
    if include_system_prompt:
        parts.append(SYSTEM_PROMPT)
    
    # Add search context if available
    context = format_hybrid_context(web_results, rag_results)
    if context:
        parts.append(context)
    
    # Add user message
    parts.append(f"Kullanıcı: {user_message}")
    
    return "\n\n".join(parts)
