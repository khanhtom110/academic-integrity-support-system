# Deploy backend lên VPS qua domain lms-haui.fit

Kien truc: `Trinh duyet -> HTTPS -> Cloudflare -> VPS:443 (Caddy) -> localhost:8080 (Spring Boot)`.

## 1. Cloudflare: DNS

Nameserver da chuyen sang Cloudflare. Vao **DNS**, them:

| Type | Name | Content    | Proxy status |
| ---- | ---- | ---------- | ------------ |
| A    | `@`  | IP cua VPS | Proxied      |

Dung `@` (root domain), khong dung subdomain, vi `frontend/vercel.json` va
`cors.allowed-origins` trong `application.properties` da tro thang
`https://lms-haui.fit`.

## 2. Cloudflare: SSL/TLS

- **SSL/TLS -> Overview**: chon **Full (strict)**. Khong dung Flexible, vi
  Flexible chi ma hoa doan Cloudflare-trinh duyet, con doan Cloudflare-VPS
  van la HTTP tran va de dinh redirect loop voi Spring Security.
- **SSL/TLS -> Origin Server -> Create Certificate**: sinh cap chung chi
  mien phi, han 15 nam, chi Cloudflare tin tuong. Tai ve 2 file, dat ten
  `origin.pem` va `origin-key.pem`.

## 3. VPS: cai Caddy

```bash
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https curl
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update
sudo apt install caddy
```

Copy 2 file chung chi tu buoc 2 vao `/etc/caddy/certs/` tren VPS, roi copy
`deploy/Caddyfile` trong repo nay vao `/etc/caddy/Caddyfile`:

```bash
sudo mkdir -p /etc/caddy/certs
sudo cp origin.pem origin-key.pem /etc/caddy/certs/
sudo cp deploy/Caddyfile /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

## 4. VPS: chay Spring Boot nhu mot service

App **khong duoc** chay bang `java -jar` tay qua SSH: phien SSH dong hoac
VPS restart la app chet theo. Dung systemd.

```bash
sudo useradd -r -s /usr/sbin/nologin lms
sudo mkdir -p /opt/lms
sudo cp target/lms-*.jar /opt/lms/lms.jar
sudo cp .env /opt/lms/.env          # doi gia tri dev sang gia tri production truoc khi copy
sudo chown -R lms:lms /opt/lms

sudo cp deploy/lms.service /etc/systemd/system/lms.service
sudo systemctl daemon-reload
sudo systemctl enable --now lms
sudo systemctl status lms
```

`EnvironmentFile=/opt/lms/.env` trong `lms.service` doc dung dinh dang
`.env` da co san trong repo (`KEY=value`, khong can dau ngoac kep).

## 5. VPS: tuong lua

`server.address=127.0.0.1` (da them trong `application.properties`) khien
Spring Boot chi lang nghe localhost, khong the goi thang tu ngoai internet
du firewall co mo hay khong. Van nen dong han port 8080 de phong ho, chi
mo 22 (SSH), 80 va 443:

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

## 6. Kiem thu

```bash
curl -I https://lms-haui.fit/api/v1/journals/search?q=cell
```

Ra `HTTP/2 200` (hoac 400/401 tuy endpoint co yeu cau dang nhap) la thong
duong tu Cloudflare toi Spring Boot. Kiem tra tiep tren frontend Vercel:
cac request `/api/**` duoc `vercel.json` proxy toi domain nay.

## Cap nhat phien ban moi

```bash
sudo systemctl stop lms
sudo cp target/lms-*.jar /opt/lms/lms.jar
sudo systemctl start lms
```
