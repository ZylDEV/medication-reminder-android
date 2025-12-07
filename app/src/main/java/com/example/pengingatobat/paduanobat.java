package com.example.pengingatobat;

import android.os.Bundle;
import android.text.SpannableString;
import android.widget.TextView;  // Import TextView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class paduanobat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_paduanobat);

        // Data yang akan ditampilkan
        String[] obatData = {
                "Minumlah obat sesuai dosis yang dianjurkan oleh dokter atau petunjuk kemasan. Jangan menambah atau mengurangi dosis tanpa saran medis.",
                "Perhatikan waktu minum obat, misalnya sebelum makan, sesudah makan, atau saat perut kosong. Ini mempengaruhi efektivitas obat.",
                "Simpan obat di tempat yang sejuk, kering, dan jauh dari jangkauan anak-anak. Hindari menyimpan obat di kamar mandi atau mobil.",
                "Jangan mencampur obat dengan makanan atau minuman tanpa saran dokter karena bisa memengaruhi penyerapan.",
                "Jika terjadi efek samping, hentikan penggunaan dan segera konsultasikan ke dokter.",
                "Jangan berbagi obat dengan orang lain meskipun gejalanya sama. Dosis dan kondisi tiap orang bisa berbeda.",
                "Catatan: Panduan ini bersifat umum. Untuk penggunaan obat tertentu, selalu ikuti saran dan resep dari profesional medis."
        };

        // Menggabungkan array menjadi satu teks
        StringBuilder sb = new StringBuilder();
        for (String item : obatData) {
            sb.append(item).append("\n\n");  // Menambahkan teks dan memberi jarak antar item
        }

        // Menambahkan penekanan pada teks tertentu
        SpannableString spannableText = new SpannableString(sb.toString());

        // Menampilkan teks ke dalam TextView
        TextView textView = findViewById(R.id.isiPanduan);  // Menemukan TextView berdasarkan ID
        textView.setText(spannableText);  // Menetapkan teks ke dalam TextView

        // Menambahkan padding untuk menyesuaikan dengan sistem status bar dan navigasi bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
