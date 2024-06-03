package id.maulidya.notetaking.Model;

import com.google.firebase.database.*;
import org.mindrot.jbcrypt.BCrypt;

public class DataManager { // Mendefinisikan kelas DataManager

    public interface Callback<T> { // Mendefinisikan interface Callback dengan tipe generik T
        void onCallback(T result); // Metode callback yang akan dipanggil dengan hasil
    }

    private static final DatabaseReference USERS_REF = FirebaseDatabase.getInstance().getReference("users"); // Referensi ke node "users" di Firebase Realtime Database

    public static void registerUser(String email, String password, String fullName, String phoneNumber, DataManager.Callback<Boolean> callback) {
        email = email.toLowerCase().trim();  // Pastikan email disimpan dalam lowercase dan tanpa spasi berlebih
        Query query = USERS_REF.orderByChild("email").equalTo(email); // Membuat query untuk mencari pengguna berdasarkan email
        String finalEmail = email; // Menyimpan email final untuk digunakan dalam inner class
        query.addListenerForSingleValueEvent(new ValueEventListener() { // Menambahkan listener untuk menangani hasil query
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // Dipanggil saat data berubah
                if (dataSnapshot.exists()) { // Jika pengguna dengan email yang sama sudah ada
                    callback.onCallback(false); // Callback dengan hasil false
                } else { // Jika pengguna dengan email tersebut belum ada
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // Meng-hash kata sandi menggunakan BCrypt
                    DataUser newDataUser = new DataUser(finalEmail, hashedPassword, fullName, phoneNumber); // Membuat objek DataUser baru
                    USERS_REF.push().setValue(newDataUser, (databaseError, databaseReference) -> { // Menyimpan pengguna baru ke database
                        callback.onCallback(databaseError == null); // Callback dengan hasil true jika tidak ada error
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { // Dipanggil saat terjadi kesalahan database
                System.err.println("Kesalahan database: " + databaseError.getMessage()); // Mencetak pesan kesalahan
                callback.onCallback(false); // Callback dengan hasil false
            }
        });
    }

    public static void authenticateUser(String email, String password, DataManager.Callback<DataUser> callback) {
        email = email.toLowerCase().trim();  // Pastikan email dalam lowercase untuk pencocokan
        Query query = USERS_REF.orderByChild("email").equalTo(email); // Membuat query untuk mencari pengguna berdasarkan email
        query.addListenerForSingleValueEvent(new ValueEventListener() { // Menambahkan listener untuk menangani hasil query
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // Dipanggil saat data berubah
                if (dataSnapshot.exists()) { // Jika pengguna dengan email tersebut ditemukan
                    for (DataSnapshot child : dataSnapshot.getChildren()) { // Iterasi melalui hasil query
                        DataUser dataUser = child.getValue(DataUser.class); // Mengambil objek DataUser dari snapshot
                        if (dataUser != null && BCrypt.checkpw(password, dataUser.getPassword())) { // Memeriksa kata sandi yang di-hash
                            callback.onCallback(dataUser); // Callback dengan objek DataUser jika autentikasi berhasil
                            return;
                        }
                    }
                }
                callback.onCallback(null); // Callback dengan null jika autentikasi gagal
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { // Dipanggil saat terjadi kesalahan database
                System.err.println("Kesalahan database: " + databaseError.getMessage()); // Mencetak pesan kesalahan
                callback.onCallback(null); // Callback dengan null
            }
        });
    }
}
