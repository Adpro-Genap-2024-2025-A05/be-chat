# PandaCare  – 💬 Fitur Chat antara Pacillians and CareGiver

- BeChat adalah microservice untuk menangani fitur percakapan (chat) antara dua pengguna, seperti pasien (Pacillians) dan dokter (CareGiver)
---

## Deployment

Link: [BE-Chat Deployment](https://protective-shalna-be-chat-bccf0946.koyeb.app/)
Link: [Postman API](https://www.postman.com/api-ristek/workspace/adprochat/collection/38268031-7f8b0afc-a091-4e9d-9439-8d1736072df7?action=share&creator=38268031)

## 🚀 Fitur Utama

Melakukan chat antara pengguna dan dokter,  terdiri dari use cases sebagai berikut.
- User dan dokter dapat saling mengirim pesan (C)
- dokter dapat memberikan saran medis melalui chat (C)
- Melihat Riwayat Chat: User dan Dokter dapat melihat riwayat pesan yang pernah dikirim dan diterima sebelumnya. (R)
- Mengubah Pesan: User dan Dokter dapat mengubah isi pesan yang sudah dikirim, dan pesan tersebut akan diberi label bahwa telah diedit. (U)
- Menghapus Pesan: User dan Dokter dapat menghapus pesan yang sudah dikirim. Pesan yang dihapus tetap ada di database namun diberi tanda "Pesan telah dihapus". (D)

---

## 📁 Arsitektur Proyek (Layered Architecture Pattern)

Aplikasi ini menggunakan pendekatan **Layered Architecture** yang memisahkan tanggung jawab menjadi beberapa lapisan:

1. **Controller Layer**
   - Mengatur route dan menerima HTTP request
   - `ChatController`, `ChatSessionController`

2. **Service Layer**
   - Menangani business logic
   - `ChatService`, `ChatServiceImpl`, `ChatSessionService`, `ChatSessionServiceImpl`

3. **Repository Layer**
   - Abstraksi akses ke database via Spring Data JPA
   - Contoh: `ChatMessageRepository`, `ChatSessionRepository`

4. **Model Layer (Entity)**
   - Representasi entitas dalam database
   - Contoh: `ChatMessage`, `ChatSession`

5. **DTO Layer**
   - Data Transfer Object untuk validasi input
   - Contoh: `SendMessageRequest`, `CreateSessionRequest`, `EditMessageRequest`

---

## 💡 Design Pattern yang Digunakan

### 🧠 Strategy Pattern

**Alasan Penggunaan:**  
Strategy Pattern digunakan untuk menghindari logic `if-else` panjang dalam pengolahan pesan (misalnya: edit atau hapus). Dengan pola ini, setiap aksi terhadap pesan diwakili oleh strategi berbeda.

**Implementasi:**
- Interface: `MessageActionStrategy`
- Strategi:
  - `EditMessageStrategy` – mengubah konten & set `edited = true`
  - `DeleteMessageStrategy` – ubah konten ke “Pesan telah dihapus” & set `deleted = true`

**Manfaat:**
1. Memisahkan logika bisnis setiap aksi
- Setiap aksi (seperti mengirim, mengedit, atau menghapus pesan) diimplementasikan secara terpisah agar tidak tercampur dalam ChatServiceImpl, sehingga kode menjadi lebih terorganisir dan mudah dipelihara.

2. Meningkatkan fleksibilitas dan skalabilitas kode
- Pola ini memudahkan penambahan aksi atau fitur baru tanpa perlu mengubah kode yang sudah ada, sehingga meminimalkan risiko bug dan konflik.

3. Menerapkan prinsip SOLID, khususnya:

- Single Responsibility Principle (SRP):
Setiap strategi bertanggung jawab hanya untuk satu jenis aksi, sehingga kode menjadi lebih modular dan mudah dipahami.

- Open/Closed Principle (OCP):
Struktur kode terbuka untuk perluasan (penambahan fitur baru), namun tertutup untuk modifikasi terhadap bagian yang sudah berjalan dengan baik.
- contoh: 
Mudah menambahkan strategi baru, seperti PinMessageStrategy untuk fitur "Pin Message", tanpa harus mengubah kode utama di ChatServiceImpl.
---

## 📦 API Endpoint

### 🔗 Session
| Method | Endpoint                                                              | Description                                         |
|--------|-----------------------------------------------------------------------|-----------------------------------------------------|
| POST   | `/chat/session/create`                                                | Membuat sesi chat antara dua user                   |
| POST   | `/chat/send`                                                          | Mengirim atau membuat pesan baru                    |
| GET    | `/chat/session/find?user1={userId1}&user2={userId2}`                 | Mencari sesi antara dua pengguna                    |
| GET    | `/chat/session/user/{userId}`                                         | Mencari semua sesi chat yang dimiliki seorang user  |
| GET    | `/chat/session/{sessionId}`                                           | Mendapatkan semua pesan dalam satu sesi chat        |
| PUT    | `/chat/message/{messageId}`                                           | Mengedit isi pesan berdasarkan `messageId`          |
| DELETE | `/chat/message/{messageId}`                                           | Menghapus pesan berdasarkan `messageId`             |
