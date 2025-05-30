# PandaCare  – 💬 Fitur Chat antara Pacillians and CareGiver

- BeChat adalah microservice untuk menangani fitur percakapan (chat) antara dua pengguna, seperti pasien (Pacillians) dan dokter (CareGiver)
---
#### Fitur Chat antara Pacillians and CareGiver
*Developed by: Cleo Excellen Iskandar*
- Component Diagram
  ![Rating Component Diagram](src/main/resources/assets/component_diagram.jpg)
- Code Diagram
  ![Rating Code Diagram](src/main/resources/assets/code_diagram.png)

## Deployment

Link: [BE-Chat Deployment](http://54.158.239.145/)
Link: [Postman API](https://www.postman.com/api-ristek/workspace/adprochat/collection/38268031-7f8b0afc-a091-4e9d-9439-8d1736072df7?action=share&creator=38268031)

## 🚀 Fitur Utama

Melakukan chat antara pengguna dan dokter,  terdiri dari use cases sebagai berikut.
- User dan dokter dapat saling mengirim pesan (C)
- dokter dapat memberikan saran medis melalui chat (C)
- Melihat Riwayat Chat: User dan Dokter dapat melihat riwayat pesan yang pernah dikirim dan diterima sebelumnya. (R)
- Mengubah Pesan: User dan Dokter dapat mengubah isi pesan yang sudah dikirim, dan pesan tersebut akan diberi label bahwa telah diedit. (U)
- Menghapus Pesan: User dan Dokter dapat menghapus pesan yang sudah dikirim. Pesan yang dihapus tetap ada di database namun diberi tanda "Pesan telah dihapus". (D)

---

## 💡 Design Pattern yang Digunakan

### 🧠 State Pattern
