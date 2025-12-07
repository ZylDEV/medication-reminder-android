package com.example.pengingatobat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi komponen UI
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi referensi ke Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Set listener untuk tombol login
        btnLogin.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication: sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Autentikasi berhasil
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        // Ambil nama pengguna dari database berdasarkan UID
                        databaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String nama = snapshot.child("nama").getValue(String.class);

                                Toast.makeText(login.this, "Login berhasil! Selamat datang, " + nama, Toast.LENGTH_SHORT).show();

                                // Arahkan ke halaman home
                                Intent intent = new Intent(login.this, home.class);
                                intent.putExtra("uid", uid); // Kirim UID ke activity berikutnya jika diperlukan
                                intent.putExtra("nama", nama);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(login.this, "Terjadi kesalahan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Autentikasi gagal
                        Toast.makeText(login.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
