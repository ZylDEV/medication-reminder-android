package com.example.pengingatobat;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.TextView; // Import TextView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class paduanapk extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Mengaktifkan fitur edge to edge
        setContentView(R.layout.activity_paduanapk);

        // Data yang akan ditampilkan
        String[] panduanData = {
                "Diabetes Buddy adalah aplikasi Android untuk membantu pengguna diabetes mengatur pengobatan, mulai dari pengingat obat, riwayat konsumsi, hingga panduan penggunaan obat.",
                "Fitur Utama aplikasi:",
                "1. Pengingat Otomatis: Notifikasi & alarm sesuai jadwal.",
                "2. Riwayat Obat: Catatan konsumsi obat harian.",
                "3. Stok Obat: Cek jumlah obat yang tersedia.",
                "4. Panduan Obat: Info jenis dan penggunaan obat.",
                "5. Profil: Informasi akun pengguna.",
                "6. Panduan Aplikasi: Petunjuk penggunaan aplikasi.",
                "7. Export PDF: Riwayat konsumsi bisa diunduh dalam bentuk PDF.",
                "Panduan Penggunaan:",
                "1. Stok Obat: Isi nama & jumlah obat sebelum mengatur pengingat.",
                "2. Pengingat: Atur jadwal minum obat, alarm akan aktif otomatis.",
                "3. Riwayat Obat: Lihat data konsumsi, unduh PDF menggunakan aplikasi ZArchiver.",
                "4. Panduan Obat: Lihat informasi dan efek samping dari obat yang digunakan.",
                "5. Profil: Cek data pengguna dan opsi logout."
        };

        // Menyiapkan StringBuilder untuk menggabungkan data
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < panduanData.length; i++) {
            sb.append(panduanData[i]).append("\n\n");  // Menambahkan nomor dan memberi jarak antar item
        }

        // Menampilkan teks ke dalam TextView
        TextView textView = findViewById(R.id.isiPanduan);  // Menemukan TextView berdasarkan ID

        // Menambahkan penebalan untuk beberapa teks
        SpannableString spannableString = new SpannableString(sb.toString());

        // Menebalkan "Diabetes Buddy"
        int start = spannableString.toString().indexOf("Diabetes Buddy");
        int end = start + "Diabetes Buddy".length();
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, 0);

        // Menebalkan "Fitur Utama aplikasi"
        start = spannableString.toString().indexOf("Fitur Utama aplikasi");
        end = start + "Fitur Utama aplikasi".length();
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, 0);

        // Menebalkan "Panduan Penggunaan"
        start = spannableString.toString().indexOf("Panduan Penggunaan");
        end = start + "Panduan Penggunaan".length();
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, 0);

        // Menetapkan teks ke dalam TextView
        textView.setText(spannableString);

        // Menambahkan padding untuk menyesuaikan dengan sistem status bar dan navigasi bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
