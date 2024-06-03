package id.maulidya.notetaking.Model;

import java.io.Serializable;

public class Note implements Serializable { // Mendefinisikan kelas Note yang mengimplementasikan Serializable
    private static final long serialVersionUID = 1L; // Menambahkan serialVersionUID untuk versi serialisasi

    private String title; // Deklarasi variabel untuk judul catatan
    private String content; // Deklarasi variabel untuk konten catatan

    public Note() { // Konstruktor tanpa argumen
    }

    public Note(String title, String content) { // Konstruktor dengan argumen untuk menginisialisasi judul dan konten
        this.title = title; // Mengatur judul catatan
        this.content = content; // Mengatur konten catatan
    }

    // Getter untuk mendapatkan judul catatan
    public String getTitle() {
        return title;
    }

    // Setter untuk mengatur judul catatan
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter untuk mendapatkan konten catatan
    public String getContent() {
        return content;
    }

    // Setter untuk mengatur konten catatan
    public void setContent(String content) {
        this.content = content;
    }
}
