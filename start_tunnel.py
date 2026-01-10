import subprocess
import re
import base64
import requests # pip install requests
import os

# --- .ENV YÜKLEME ---
if os.path.exists(".env"):
    with open(".env", "r", encoding="utf-8") as f:
        for line in f:
            if "=" in line and not line.startswith("#"):
                key, val = line.strip().split("=", 1)
                os.environ[key] = val
# --------------------

# --- AYARLAR ---
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
REPO_OWNER = "memati8383"
REPO_NAME = "niko-with-kiro"
FILE_PATH = "README.md"
JAVA_FILE_PATH = "Niko Mobile App/MainActivity.java"
# ---------------

def update_github_readme(new_url):
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/contents/{FILE_PATH}"
    headers = {"Authorization": f"token {GITHUB_TOKEN}"}

    # 1. Dosyanın mevcut halini ve 'sha' değerini al
    r = requests.get(url, headers=headers)
    if r.status_code == 200:
        content_data = r.json()
        sha = content_data['sha']
        # Mevcut içeriği decode et
        current_content = base64.b64decode(content_data['content']).decode('utf-8')
    else:
        print("[!] Dosya GitHub'da bulunamadı, yeni oluşturulacak.")
        sha = None
        current_content = ""

    # 2. İçeriği güncelle
    target_prefix = "> 🌐 **Güncel Tünel Adresi:**"
    new_line = f"{target_prefix} [{new_url}]({new_url})"
    
    if target_prefix in current_content:
        # Eski satırı yenisiyle değiştir (regex kullanarak)
        updated_content = re.sub(rf"{re.escape(target_prefix)}.*", new_line, current_content)
    else:
        # Yoksa en sona ekle
        updated_content = current_content + f"\n\n{new_line}\n"

    # 3. Güncellenmiş içeriği geri gönder
    message = "Tünel adresi otomatik güncellendi"
    encoded_content = base64.b64encode(updated_content.encode('utf-8')).decode('utf-8')
    
    data = {
        "message": message,
        "content": encoded_content,
        "sha": sha
    }

    r_put = requests.put(url, json=data, headers=headers)
    if r_put.status_code in [200, 201]:
        print(f"[+] GitHub README başarıyla güncellendi: {new_url}")
    else:
        print(f"[!] GitHub güncelleme hatası: {r_put.text}")

def update_github_java(new_url):
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/contents/{JAVA_FILE_PATH}"
    headers = {"Authorization": f"token {GITHUB_TOKEN}"}

    # 1. Dosyanın mevcut halini al
    r = requests.get(url, headers=headers)
    if r.status_code == 200:
        content_data = r.json()
        sha = content_data['sha']
        current_content = base64.b64decode(content_data['content']).decode('utf-8')
    else:
        print(f"[!] Java dosyası GitHub'da bulunamadı: {r.status_code}")
        return

    # 2. İçeriği güncelle
    pattern = r'(private static final String API_BASE_URL = ").*?(")'
    replacement = f'\\1{new_url}\\2'
    
    if re.search(pattern, current_content):
        updated_content = re.sub(pattern, replacement, current_content)
    else:
        print("[!] GitHub'daki Java dosyasında API_BASE_URL bulunamadı.")
        return

    if updated_content == current_content:
        return

    # 3. Güncellenmiş içeriği geri gönder
    message = "API URL otomatik güncellendi (Android)"
    encoded_content = base64.b64encode(updated_content.encode('utf-8')).decode('utf-8')
    
    data = {"message": message, "content": encoded_content, "sha": sha}
    r_put = requests.put(url, json=data, headers=headers)
    
    if r_put.status_code in [200, 201]:
        print(f"[+] GitHub Java dosyası güncellendi: {new_url}")
    else:
        print(f"[!] GitHub Java güncelleme hatası: {r_put.text}")

def update_local_java(new_url):
    local_path = os.path.join(os.getcwd(), "Niko Mobile App", "MainActivity.java")
    if not os.path.exists(local_path):
        print(f"[!] Yerel Java dosyası bulunamadı: {local_path}")
        return

    with open(local_path, "r", encoding="utf-8") as f:
        content = f.read()

    pattern = r'(private static final String API_BASE_URL = ").*?(")'
    replacement = f'\\1{new_url}\\2'
    
    if re.search(pattern, content):
        updated_content = re.sub(pattern, replacement, content)
        with open(local_path, "w", encoding="utf-8") as f:
            f.write(updated_content)
        print(f"[+] Yerel Java dosyası güncellendi: {new_url}")
    else:
        print("[!] Yerel Java dosyasında API_BASE_URL bulunamadı.")

def main():
    cmd = ["cloudflared", "tunnel", "--url", "http://127.0.0.1:8001"]
    print(f"[*] Cloudflared başlatılıyor...")

    try:
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        url_regex = re.compile(r"https://[a-zA-Z0-9-]+\.trycloudflare\.com")
        
        url_found = False
        while True:
            line = process.stdout.readline()
            if not line: break
            print(line.strip())
            
            if not url_found:
                match = url_regex.search(line)
                if match:
                    found_url = match.group(0)
                    print(f"\n[!] URL Yakalandı: {found_url}")
                    update_github_readme(found_url)
                    update_github_java(found_url)
                    update_local_java(found_url)
                    url_found = True
                    
    except KeyboardInterrupt:
        process.terminate()

if __name__ == "__main__":
    main()