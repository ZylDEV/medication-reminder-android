package com.example.pengingatobat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class akun extends AppCompatActivity {

    private EditText editNama, editUsia, editEmail;
    private Button btnSimpan, btnKeluarAkun;
    private LinearLayout btnStockObat, btnHistory, btnHome;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akun);

        // Inisialisasi EditText
        editNama = findViewById(R.id.editNama);
        editUsia = findViewById(R.id.editUsia);
        editEmail = findViewById(R.id.editEmail);

        btnStockObat = findViewById(R.id.btnStockObat);
        btnHistory = findViewById(R.id.btnHistory);
        btnHome = findViewById(R.id.btnHome);

        // Inisialisasi Tombol
        btnSimpan = findViewById(R.id.btnSimpan);
        btnKeluarAkun = findViewById(R.id.btnKeluarAkun);

        // Inisialisasi Firebase Auth dan Database
        mAuth = FirebaseAuth.getInstance();

        // Mendapatkan data pengguna yang sedang login
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Mengambil referensi data pengguna dari Firebase Realtime Database
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Mengambil data pengguna
            getUserData();
        } else {
            // Handle jika user belum login
            TextView errorView = new TextView(akun.this);
            errorView.setText("User belum login.");
            setContentView(errorView);
        }

        // Menangani aksi tombol Simpan
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        // Menangani aksi tombol Keluar Akun
        btnKeluarAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });



        btnStockObat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(akun.this, kelolaobat.class);
                startActivity(intent);
            }
        });

        // Set listener tombol History
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(akun.this, riwayat.class);
                startActivity(intent);
            }
        });

        // Set listener tombol Profil
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(akun.this, home.class);
                startActivity(intent);
            }
        });
    }

    private void getUserData() {
        // Mengambil data pengguna dari Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Mendapatkan data pengguna
                String nama = dataSnapshot.child("nama").getValue(String.class);  // Menggunakan field "nama"
                String usia = dataSnapshot.child("usia").getValue(String.class);  // Asumsi ada field usia
                String email = dataSnapshot.child("email").getValue(String.class);

                // Menambahkan teks penjelas sebelum menampilkan data di EditText
                if (nama != null) {
                    editNama.setText("Nama: " + nama); // Menampilkan "Nama: [nama]"
                }
                if (usia != null) {
                    editUsia.setText("Usia: " + usia); // Menampilkan "Usia: [usia]"
                }
                if (email != null) {
                    editEmail.setText("Email: " + email); // Menampilkan "Email: [email]"
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Menangani error jika terjadi
                TextView errorView = new TextView(akun.this);
                errorView.setText("Gagal memuat data: " + databaseError.getMessage());
                setContentView(errorView);
            }
        });
    }

    private void saveUserData() {
        String nama = editNama.getText().toString().replace("Nama: ", "");
        String usia = editUsia.getText().toString().replace("Usia: ", "");
        String email = editEmail.getText().toString().replace("Email: ", "");

        // Menyimpan data pengguna yang telah diperbarui ke Firebase Realtime Database
        databaseReference.child("nama").setValue(nama);
        databaseReference.child("usia").setValue(usia);
        databaseReference.child("email").setValue(email);

        // Menampilkan Toast untuk log perubahan
        Toast.makeText(akun.this, "Data berhasil disimpan.", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        // Keluar dari akun Firebase
        mAuth.signOut();

        // Kembali ke MainActivity dan membersihkan stack Activity
        Intent intent = new Intent(akun.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear semua activity dan buat aktivitas baru
        startActivity(intent);
        finish();  // Menutup Activity ini

        // Menampilkan Toast untuk log keluar akun
        Toast.makeText(akun.this, "Anda telah keluar dari akun.", Toast.LENGTH_SHORT).show();
    }


    private void deleteAccount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Menghapus akun dari Firebase Authentication
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Menghapus data pengguna dari Firebase Realtime Database
                    databaseReference.removeValue();

                    // Keluar dari aplikasi dan kembali ke MainActivity
                    Intent intent = new Intent(akun.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);  // Clear stack
                    startActivity(intent);
                    finish();

                    // Menampilkan Toast untuk log hapus akun
                    Toast.makeText(akun.this, "Akun berhasil dihapus.", Toast.LENGTH_SHORT).show();
                } else {
                    // Menangani error jika gagal menghapus akun
                    Toast.makeText(akun.this, "Gagal menghapus akun: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
