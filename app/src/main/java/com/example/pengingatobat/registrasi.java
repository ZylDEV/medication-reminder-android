package com.example.pengingatobat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class registrasi extends AppCompatActivity {

    private EditText inputNama, inputUsia, inputEmail, inputPassword;
    private Button btnRegister;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        inputNama = findViewById(R.id.inputNama);
        inputUsia = findViewById(R.id.inputUsia);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Inisialisasi Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String nama = inputNama.getText().toString().trim();
        String usia = inputUsia.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (nama.isEmpty() || usia.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication: Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User successfully created in Firebase Authentication
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid(); // Get user ID from FirebaseAuth

                        // Membuat HashMap untuk menyimpan data user
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nama", nama);
                        userData.put("usia", usia);
                        userData.put("email", email);
                        userData.put("password", password); // You can keep password or store a hashed one for security

                        // Simpan data user di Firebase Realtime Database
                        mDatabase.child(userId).setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                                    // Arahkan ke MainActivity setelah registrasi berhasil
                                    Intent intent = new Intent(registrasi.this, login.class);
                                    intent.putExtra("fromRegister", true); // <-- kirim flag
                                    startActivity(intent);
                                    finish();

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        // If Firebase Authentication fails
                        Toast.makeText(registrasi.this, "Gagal registrasi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
