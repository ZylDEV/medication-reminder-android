package com.example.pengingatobat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class kelolaobat extends AppCompatActivity {

    private Spinner edtNamaObatManajemen;
    private EditText edtJumlahStok;
    private Button btnTambahObat;
    private LinearLayout daftarObatLayout;
    private LinearLayout btnHome, btnHistory, btnProfil;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private List<String> daftarNamaObat;

    private TextView namaObatTextView, stokTextView;  // TextView yang ada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelolaobat);

        edtNamaObatManajemen = findViewById(R.id.edtNamaObatManajemen);
        edtJumlahStok = findViewById(R.id.edtJumlahStok);
        btnTambahObat = findViewById(R.id.btnTambahObat);
        daftarObatLayout = findViewById(R.id.linearLayoutObat);  // Layout untuk menampilkan daftar obat

        btnHome = findViewById(R.id.btnHome);
        btnHistory = findViewById(R.id.btnHistory);
        btnProfil = findViewById(R.id.btnProfil);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        daftarNamaObat = Arrays.asList("Metformin", "Glibenclamide", "Pioglitazone", "Insulin");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daftarNamaObat);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edtNamaObatManajemen.setAdapter(adapter);

        // Tombol Home
        btnHome.setOnClickListener(v -> {
            // Intent untuk pindah ke HomeActivity dari kelolaobat
            Intent intent = new Intent(kelolaobat.this, home.class);
            startActivity(intent);
        });

        // Tombol History
        btnHistory.setOnClickListener(v -> {
            // Intent untuk pindah ke RiwayatActivity dari kelolaobat
            Intent intent = new Intent(kelolaobat.this, riwayat.class);
            startActivity(intent);
        });

        // Tombol Profil
        btnProfil.setOnClickListener(v -> {
            // Intent untuk pindah ke AkunActivity dari kelolaobat
            Intent intent = new Intent(kelolaobat.this, akun.class);
            startActivity(intent);
        });

        // Load data obat dari Firebase saat aplikasi dijalankan
        loadDataObatFromFirebase();

        btnTambahObat.setOnClickListener(v -> {
            String namaObat = edtNamaObatManajemen.getSelectedItem().toString();
            String jumlahStokStr = edtJumlahStok.getText().toString().trim();

            if (jumlahStokStr.isEmpty()) {
                Toast.makeText(kelolaobat.this, "Masukkan jumlah stok", Toast.LENGTH_SHORT).show();
                return;
            }

            int jumlahStok;
            try {
                jumlahStok = Integer.parseInt(jumlahStokStr);
            } catch (NumberFormatException e) {
                Toast.makeText(kelolaobat.this, "Jumlah stok harus berupa angka", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(kelolaobat.this, "User belum login", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference obatRef = databaseReference.child(userId).child("obat");

            obatRef.get().addOnSuccessListener(dataSnapshot -> {
                boolean found = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nama = snapshot.child("namaObat").getValue(String.class);
                    if (nama != null && nama.equalsIgnoreCase(namaObat)) {
                        Long stokLama = snapshot.child("stok").getValue(Long.class);
                        if (stokLama == null) stokLama = 0L;
                        long stokBaru = stokLama + jumlahStok;

                        snapshot.getRef().child("stok").setValue(stokBaru)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(kelolaobat.this, "Stok obat berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                    edtJumlahStok.setText("");

                                    // Update layout untuk menampilkan stok terbaru
                                    loadDataObatFromFirebase();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(kelolaobat.this, "Gagal memperbarui stok: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        found = true;
                        break;
                    }
                }

                if (!found) {
                    String idObat = obatRef.push().getKey();
                    Map<String, Object> dataObat = new HashMap<>();
                    dataObat.put("namaObat", namaObat);
                    dataObat.put("stok", jumlahStok);

                    obatRef.child(idObat).setValue(dataObat)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(kelolaobat.this, "Obat baru berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                edtJumlahStok.setText("");

                                // Update layout untuk menampilkan stok terbaru
                                loadDataObatFromFirebase();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(kelolaobat.this, "Gagal menambahkan obat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            });
        });
    }

    private void loadDataObatFromFirebase() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference obatRef = databaseReference.child(userId).child("obat");

        obatRef.get().addOnSuccessListener(dataSnapshot -> {
            daftarObatLayout.removeAllViews();  // Menghapus semua tampilan lama sebelum menambahkan yang baru

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                String namaObat = snapshot.child("namaObat").getValue(String.class);
                Long stok = snapshot.child("stok").getValue(Long.class);
                String obatId = snapshot.getKey();  // Dapatkan ID unik obat

                if (namaObat != null && stok != null) {
                    // Membuat tampilan baru untuk setiap obat
                    LinearLayout itemLayout = new LinearLayout(kelolaobat.this);
                    itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                    itemLayout.setPadding(16, 16, 16, 16);

                    TextView namaObatTextView = new TextView(kelolaobat.this);
                    namaObatTextView.setText(namaObat);
                    namaObatTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    TextView stokTextView = new TextView(kelolaobat.this);
                    stokTextView.setText("Stok: " + stok);
                    stokTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    Button btnKurangiObat = new Button(kelolaobat.this);
                    btnKurangiObat.setText("Kurangi obat");
                    btnKurangiObat.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    // Set onClickListener untuk tombol Kurangi Obat
                    btnKurangiObat.setOnClickListener(v -> {
                        if (stok > 0) {
                            // Kurangi stok
                            long stokBaru = stok - 1;
                            snapshot.getRef().child("stok").setValue(stokBaru)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(kelolaobat.this, "Stok berhasil dikurangi", Toast.LENGTH_SHORT).show();
                                        loadDataObatFromFirebase();  // Reload data setelah pengurangan
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(kelolaobat.this, "Gagal mengurangi stok: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(kelolaobat.this, "Stok sudah mencapai 0", Toast.LENGTH_SHORT).show();
                        }
                    });

                    itemLayout.addView(namaObatTextView);
                    itemLayout.addView(stokTextView);
                    itemLayout.addView(btnKurangiObat);

                    daftarObatLayout.addView(itemLayout);
                }
            }
        });
    }
}
