package id.maulidya.notetaking.Firebase; // Mendefinisikan package tempat class ini berada

import com.google.auth.oauth2.GoogleCredentials; // Mengimpor kelas untuk mengelola kredensial Google
import com.google.firebase.FirebaseApp; // Mengimpor kelas untuk mengelola aplikasi Firebase
import com.google.firebase.FirebaseOptions; // Mengimpor kelas untuk mengatur opsi Firebase

import java.io.FileInputStream; // Mengimpor kelas untuk membaca file input sebagai stream
import java.io.IOException; // Mengimpor kelas untuk menangani pengecualian IO

public class Firebase { // Mendefinisikan kelas utama Firebase
    public static class FirebaseUtil { // Mendefinisikan kelas utilitas sebagai inner class
        private static final String FIREBASE_KEY_PATH = "C:/Users/isnan/IdeaProjects/earlybirdNeena/notetaking-b0bee-firebase-adminsdk-5yk64-dd555d8a4e.json"; // Lokasi file kunci Firebase

        static { // Blok statis untuk inisialisasi awal
            initializeFirebase(); // Memanggil metode inisialisasi Firebase
        }

        public static void initializeFirebase() { // Metode untuk menginisialisasi Firebase
            try {
                FileInputStream serviceAccount = new FileInputStream(FIREBASE_KEY_PATH); // Membaca file kunci Firebase
                FirebaseOptions options = FirebaseOptions.builder() // Membuat opsi konfigurasi Firebase
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount)) // Mengatur kredensial dari file
                        .setDatabaseUrl("https://notetaking-b0bee-default-rtdb.firebaseio.com/") // Mengatur URL database Firebase
                        .build(); // Membangun objek opsi

                if (FirebaseApp.getApps().isEmpty()) { // Mengecek apakah belum ada instance FirebaseApp yang aktif
                    FirebaseApp.initializeApp(options); // Menginisialisasi aplikasi Firebase dengan opsi yang sudah dibuat
                }
            } catch (IOException e) { // Menangkap pengecualian IO
                throw new RuntimeException("Firebase initialization failed.", e); // Melempar runtime exception jika inisialisasi gagal
            }
        }
    }
}
