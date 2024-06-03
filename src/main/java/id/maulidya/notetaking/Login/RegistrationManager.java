package id.maulidya.notetaking.Login; // Mendefinisikan package tempat class ini berada

import id.maulidya.notetaking.Model.NoteController; // Mengimpor kelas NoteController dari package Model
import javafx.fxml.FXML; // Mengimpor anotasi FXML dari JavaFX
import javafx.scene.control.*; // Mengimpor semua kontrol dari JavaFX
import javafx.application.Platform; // Mengimpor kelas Platform dari JavaFX
import javafx.stage.Stage; // Mengimpor kelas Stage dari JavaFX
import javafx.scene.Scene; // Mengimpor kelas Scene dari JavaFX
import javafx.fxml.FXMLLoader; // Mengimpor kelas FXMLLoader dari JavaFX
import id.maulidya.notetaking.Model.DataManager; // Mengimpor kelas DataManager dari package Model
import java.io.IOException; // Mengimpor kelas IOException
import java.util.regex.Pattern; // Mengimpor kelas Pattern untuk validasi regex

public class RegistrationManager { // Mendefinisikan kelas RegistrationManager
    @FXML private TextField emailField, fullNameField, phoneNumberField; // Mendeklarasikan field untuk input email, nama lengkap, dan nomor telepon
    @FXML private PasswordField passwordField; // Mendeklarasikan field untuk input kata sandi
    @FXML private Label statusLabel; // Mendeklarasikan label untuk menampilkan status

    @FXML
    protected void onRegisterButtonClick() { // Metode yang dipanggil saat tombol registrasi diklik
        String email = emailField.getText().trim().toLowerCase(); // Mendapatkan dan membersihkan input email
        String password = passwordField.getText(); // Mendapatkan input kata sandi
        String fullName = fullNameField.getText().trim(); // Mendapatkan dan membersihkan input nama lengkap
        String phoneNumber = phoneNumberField.getText().trim(); // Mendapatkan dan membersihkan input nomor telepon

        if (!isValidEmail(email)) { // Memeriksa validitas email
            statusLabel.setText("Format email tidak falid."); // Menampilkan pesan kesalahan jika email tidak valid
            return; // Keluar dari metode
        }

        if (!isStrongPassword(password)) { // Memeriksa kekuatan kata sandi
            statusLabel.setText("Kata sandi setidaknya 8 karakter, berisi huruf dan angka."); // Menampilkan pesan kesalahan jika kata sandi tidak kuat
            return; // Keluar dari metode
        }

        if (!isValidPhoneNumber(phoneNumber)) { // Memeriksa validitas nomor telepon
            statusLabel.setText("Nomor telepon harus berupa angka dan minimal 12 angka."); // Menampilkan pesan kesalahan jika nomor telepon tidak valid
            return; // Keluar dari metode
        }

        // Mendaftarkan pengguna dengan DataManager
        DataManager.registerUser(email, password, fullName, phoneNumber, success -> Platform.runLater(() -> {
            if (success) { // Jika registrasi berhasil
                statusLabel.setText("Registrasi berhasil!."); // Menampilkan pesan sukses
                try {
                    transitionToMainApp(fullName); // Beralih ke aplikasi utama
                } catch (IOException e) { // Menangkap pengecualian IO
                    e.printStackTrace();
                    statusLabel.setText("Gagal untuk menampilkan aplikasi."); // Menampilkan pesan kesalahan
                }
            } else { // Jika registrasi gagal
                statusLabel.setText("Registrasi gagal! Pengguna sudah ada, silakan login."); // Menampilkan pesan kesalahan
            }
        }));
    }

    @FXML
    protected void onLoginButtonClick() { // Metode yang dipanggil saat tombol login diklik
        DataManager.authenticateUser(
                emailField.getText().trim().toLowerCase(), // Mendapatkan dan membersihkan input email
                passwordField.getText(), // Mendapatkan input kata sandi
                user -> Platform.runLater(() -> {
                    if (user != null) { // Jika autentikasi berhasil
                        statusLabel.setText("Login berhasil!"); // Menampilkan pesan sukses
                        try {
                            transitionToMainApp(user.getFullName()); // Beralih ke aplikasi utama dengan nama pengguna
                        } catch (IOException e) { // Menangkap pengecualian IO
                            e.printStackTrace();
                            statusLabel.setText("Gagal untuk menampilkan aplikasi."); // Menampilkan pesan kesalahan
                        }
                    } else { // Jika autentikasi gagal
                        statusLabel.setText("Login gagal. Silakan coba lagi."); // Menampilkan pesan kesalahan
                    }
                })
        );
    }

    // Mengalihkan ke tampilan utama aplikasi
    private void transitionToMainApp(String userId) throws IOException { // Menambahkan parameter userId
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/id.maulidya.notetaking/main.fxml")); // Memuat file FXML utama
        Stage stage = (Stage) statusLabel.getScene().getWindow(); // Mendapatkan stage saat ini
        Scene scene = new Scene(loader.load()); // Membuat scene baru dari file FXML
        NoteController noteController = loader.getController(); // Mendapatkan controller dari main.fxml
        noteController.setUserId(userId); // Mengatur ID pengguna di NoteController
        stage.setScene(scene); // Mengatur scene baru
        stage.show(); // Menampilkan scene
    }

    @FXML
    protected void onShowLoginView() throws IOException { // Metode untuk menampilkan tampilan login
        changeScene("/id.maulidya.notetaking/login.fxml"); // Mengganti scene ke login.fxml
    }

    @FXML
    protected void onShowRegisterView() throws IOException { // Metode untuk menampilkan tampilan registrasi
        changeScene("/id.maulidya.notetaking/register.fxml"); // Mengganti scene ke register.fxml
    }

    private void changeScene(String fxmlPath) throws IOException { // Metode untuk mengganti scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); // Memuat file FXML
        Stage stage = (Stage) emailField.getScene().getWindow(); // Mendapatkan stage saat ini
        Scene scene = new Scene(loader.load()); // Membuat scene baru dari file FXML
        stage.setScene(scene); // Mengatur scene baru
        stage.show(); // Menampilkan scene
    }

    private boolean isValidEmail(String email) { // Metode untuk memvalidasi email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"; // Regex untuk email
        Pattern pat = Pattern.compile(emailRegex); // Membuat pola regex
        return email != null && pat.matcher(email).matches(); // Memeriksa kecocokan email dengan pola
    }

    private boolean isStrongPassword(String password) { // Metode untuk memeriksa kekuatan kata sandi
        return password.length() >= 8 && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(Character::isLetter); // Memeriksa panjang dan karakter dalam kata sandi
    }

    private boolean isValidPhoneNumber(String phoneNumber) { // Metode untuk memvalidasi nomor telepon
        String phoneRegex = "\\d{12,}"; // Regex untuk nomor telepon
        Pattern pat = Pattern.compile(phoneRegex); // Membuat pola regex
        return phoneNumber != null && pat.matcher(phoneNumber).matches(); // Memeriksa kecocokan nomor telepon dengan pola
    }
}
