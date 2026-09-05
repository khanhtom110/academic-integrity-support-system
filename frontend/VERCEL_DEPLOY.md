# Deploy frontend lên Vercel

## 1. Import repository

- Tạo Vercel Project từ GitHub repository.
- Chọn `frontend` làm **Root Directory**.
- Framework Preset: **Vite**.
- Build Command: `npm run build`.
- Output Directory: `dist`.

## 2. Deploy lần đầu

Deploy để nhận domain production cố định, ví dụ:

```text
https://academic-integrity-support-system.vercel.app
```

Không dùng Preview URL thay đổi theo từng commit làm OAuth callback.

## 3. Environment Variables

Thêm các biến sau cho môi trường Production trên Vercel:

```env
VITE_API_BASE_URL=
VITE_APP_ORIGIN=https://academic-integrity-support-system.vercel.app
VITE_GOOGLE_CLIENT_ID=930372998229-hvum0n94hf69l7u7po0vc5rncage449s.apps.googleusercontent.com
VITE_OUTLOOK_CLIENT_ID=f2fbd8d6-479a-4444-ae64-d40b9ebce928
```

Thay domain ví dụ bằng domain production thực tế. Không thêm client secret vào frontend.

## 4. Đồng bộ OAuth

Gửi domain production cho backend để cấu hình hai redirect URI:

```text
https://academic-integrity-support-system.vercel.app/oauth2/callback/google
https://academic-integrity-support-system.vercel.app/oauth2/callback/outlook
```

Đăng ký đúng các URI này trên Google Cloud và Microsoft Entra.

## 5. Redeploy và kiểm thử

Sau khi thêm hoặc đổi biến môi trường, tạo deployment mới. Kiểm tra trực tiếp các route:

```text
/login
/forgot-password
/oauth2/callback/google
/oauth2/callback/outlook
```

Các request `/api/**` được `vercel.json` proxy tới `https://lms-haui.fit`.
