package com.example.pengingatobat;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.widget.Spinner;

public class home extends AppCompatActivity {
    private Spinner NamaObat, Dosis;
    private EditText edtStartDate, edtEndDate, edtWaktu, edtNamaObatLain, edtDosisLain;
    private Button btnSimpan;
    private TextView countdownTimer;
    private String currentAlarmId;
    private ImageView menuIcon;
    private LinearLayout bar1, bar2;
    private View obatABar, obatBBar;
    private TextView obatAText, obatBText;

    private LinearLayout btnStockObat, btnHistory, btnProfil;


    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;  // Menambahkan FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);  // Pastikan nama layout sudah benar

        // Inisialisasi komponen UI
        edtNamaObatLain = findViewById(R.id.edtNamaObatLain);
        NamaObat = findViewById(R.id.NamaObat);
        Dosis = findViewById(R.id.Dosis);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        edtWaktu = findViewById(R.id.edtWaktu);
        btnSimpan = findViewById(R.id.btnSimpan);
        countdownTimer = findViewById(R.id.countdownTimer);
        edtDosisLain = findViewById(R.id.edtDosisLain);
        btnStockObat = findViewById(R.id.btnStockObat);
        btnHistory = findViewById(R.id.btnHistory);
        btnProfil = findViewById(R.id.btnProfil);
        menuIcon = findViewById(R.id.menuIcon);

        bar1 = findViewById(R.id.bar1);
        bar2 = findViewById(R.id.bar2);
        obatABar = findViewById(R.id.obatABar);
        obatBBar = findViewById(R.id.obatBBar);
        obatAText = findViewById(R.id.obatA);
        obatBText = findViewById(R.id.obatB);

        // Inisialisasi Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();  // Inisialisasi FirebaseAuth
        fetchNextAlarmFromFirebase();
        loadData();

        // Set data ke Spinner (Kategori Obat)
        String[] kategoriObat = {"Metformin", "Glibenclamide", "Pioglitazone", "Insulin", "Lainnya..."};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kategoriObat);

        Map<String, List<String>> dosisMap = new HashMap<>();
        dosisMap.put("Metformin", Arrays.asList("500 mg", "800 mg", "1000 mg"));
        dosisMap.put("Glibenclamide", Arrays.asList("2.5 mg", "5 mg", "10 mg"));
        dosisMap.put("Pioglitazone", Arrays.asList("15 mg", "30 mg", "45 mg"));
        dosisMap.put("Insulin", Arrays.asList("10 unit", "20 unit", "30 unit"));

        // ✅ TAMBAHAN: Listener NamaObat untuk atur dosis otomatis
        // ✅ TAMBAHAN: Listener NamaObat untuk atur dosis otomatis
        NamaObat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals("Lainnya...")) {
                    edtNamaObatLain.setVisibility(View.VISIBLE); // Menampilkan input nama obat lainnya
                    edtDosisLain.setVisibility(View.VISIBLE); // Menampilkan input dosis lainnya
                    Dosis.setVisibility(View.GONE); // Menyembunyikan spinner dosis
                } else {
                    edtNamaObatLain.setVisibility(View.GONE); // Sembunyikan input nama obat lainnya
                    edtDosisLain.setVisibility(View.GONE); // Sembunyikan input dosis lainnya
                    Dosis.setVisibility(View.VISIBLE); // Menampilkan spinner dosis

                    // Sesuaikan daftar dosis sesuai dengan obat yang dipilih
                    List<String> dosisList = dosisMap.get(selectedItem);
                    if (dosisList != null) {
                        ArrayAdapter<String> dosisAdapter = new ArrayAdapter<>(home.this, android.R.layout.simple_spinner_dropdown_item, dosisList);
                        Dosis.setAdapter(dosisAdapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                edtNamaObatLain.setVisibility(View.GONE);
                edtDosisLain.setVisibility(View.GONE); // Sembunyikan dosis manual
                Dosis.setVisibility(View.VISIBLE); // Menampilkan spinner dosis
            }
        });
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(home.this, menuIcon);
            popup.getMenu().add("Panduan Aplikasi");
            popup.getMenu().add("Panduan Obat");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Panduan Aplikasi")) {
                    Intent intent = new Intent(home.this, paduanapk.class);
                    startActivity(intent);
                } else if (title.equals("Panduan Obat")) {
                    Intent intent = new Intent(home.this, paduanobat.class);
                    startActivity(intent);
                }
                return true;
            });

            popup.show();
        });

        NamaObat.setAdapter(adapter);
        // Menyimpan data ketika tombol "Simpan Pengingat" diklik
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarmData();
            }
        });

        // Set listener untuk memilih tanggal mulai
        edtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(edtStartDate);
            }
        });

        // Set listener untuk memilih tanggal selesai
        edtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(edtEndDate);
            }
        });

        // Set listener untuk memilih waktu
        edtWaktu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(edtWaktu);  // Panggil fungsi untuk memilih waktu
            }
        });
        btnStockObat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, kelolaobat.class);
                startActivity(intent);
            }
        });

        // Set listener tombol History
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, riwayat.class);
                startActivity(intent);
            }
        });

        // Set listener tombol Profil
        btnProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, akun.class);
                startActivity(intent);
            }
        });
    }
    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            mDatabase.child("users").child(userId).child("alarm")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int sudahCount = 0;
                            int belumCount = 0;

                            for (DataSnapshot alarmSnapshot : dataSnapshot.getChildren()) {
                                String status = alarmSnapshot.child("status").getValue(String.class);
                                String stokStr = alarmSnapshot.child("stok").getValue(String.class);

                                if (status == null || stokStr == null) continue;

                                int stok;
                                try {
                                    stok = Integer.parseInt(stokStr);
                                } catch (NumberFormatException e) {
                                    continue;
                                }

                                if ("Sudah".equals(status)) {
                                    sudahCount += stok;
                                } else if ("Belum".equals(status)) {
                                    belumCount += stok;
                                }
                            }

                            // Update bar Sudah & Belum
                            updateGraph(obatABar, obatAText, "Sudah Diminum", sudahCount);
                            updateGraph(obatBBar, obatBText, "Belum Diminum", belumCount);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("loadData", "Gagal ambil data: " + databaseError.getMessage());
                        }
                    });
        } else {
            Log.e("loadData", "User belum login.");
        }
    }

    private void updateGraph(View bar, TextView textView, String label, int count) {
        // Update teks label dengan jumlah
        textView.setText(label + " (" + count + ")");

        // Hitung lebar bar (skala bisa disesuaikan, misalnya: 1 stok = 20px)
        int barWidth = count * 20;
        bar.getLayoutParams().width = barWidth;
        bar.requestLayout();
    }

    private void saveAlarmData() {
        String namaObat = NamaObat.getSelectedItem().toString();
        String dosis = Dosis.getSelectedItem() != null ? Dosis.getSelectedItem().toString() : "";
        String startDate = edtStartDate.getText().toString().trim();
        String endDate = edtEndDate.getText().toString().trim();
        String waktu = edtWaktu.getText().toString().trim();

        // Validasi data
        if (namaObat.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || waktu.isEmpty()) {
            Toast.makeText(home.this, "Harap lengkapi semua kolom!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (namaObat.equals("Lainnya...")) {
            namaObat = edtNamaObatLain.getText().toString().trim();
            dosis = edtDosisLain.getText().toString().trim();

            if (namaObat.isEmpty()) {
                Toast.makeText(home.this, "Masukkan nama obat", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dosis.isEmpty()) {
                Toast.makeText(home.this, "Masukkan dosis obat", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (dosis.isEmpty()) {
            Toast.makeText(home.this, "Harap pilih dosis!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Set stok awal 1
            String stok = "1"; // Stok pertama kali alarm dibuat

            HashMap<String, String> alarmData = new HashMap<>();
            alarmData.put("namaObat", namaObat);
            alarmData.put("dosis", dosis);
            alarmData.put("startDate", startDate); // Simpan startDate sebagai String
            alarmData.put("endDate", endDate); // Simpan endDate sebagai String
            alarmData.put("waktu", waktu); // Simpan waktu sebagai String
            alarmData.put("status", "Belum");
            alarmData.put("stok", stok); // Tambahkan stok

            // Ambil alarmId dari intent jika ada (untuk edit)
            String alarmId = getIntent().getStringExtra("alarmId");

            DatabaseReference alarmRef;
            if (alarmId != null && !alarmId.isEmpty()) {
                // Edit alarm yang sudah ada
                alarmRef = mDatabase.child("users").child(userId).child("alarm").child(alarmId);
            } else {
                // Tambah alarm baru
                alarmRef = mDatabase.child("users").child(userId).child("alarm").push();
            }

            alarmRef.setValue(alarmData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(home.this, "Pengingat berhasil disimpan!", Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            Toast.makeText(home.this, "Gagal menyimpan pengingat. Coba lagi.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(home.this, "User belum login!", Toast.LENGTH_SHORT).show();
        }
    }





    private void showDatePickerDialog(final EditText targetEditText) {
        // Ambil tanggal saat ini
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Membuka dialog DatePicker untuk memilih tanggal
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            // Format tanggal yang dipilih menjadi yyyy-MM-dd (contoh: 2025-05-01)
            String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth);
            targetEditText.setText(date);  // Set tanggal ke EditText
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText targetEditText) {
        // Ambil waktu saat ini
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // Jam dalam format 24 jam
        int minute = calendar.get(Calendar.MINUTE);  // Menit

        // Membuka dialog TimePicker untuk memilih waktu
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            // Format waktu yang dipilih menjadi HH:mm (contoh: 14:30)
            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
            targetEditText.setText(time);  // Set waktu ke EditText
        }, hour, minute, true);  // true untuk format 24 jam

        timePickerDialog.show();
    }

    private void fetchNextAlarmFromFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            countdownTimer.setText("User belum login.");
            return;
        }

        String userId = user.getUid();
        DatabaseReference alarmRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("alarm");

        alarmRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                long now = System.currentTimeMillis();
                Date nextAlarmTime = null;
                long shortestDiff = Long.MAX_VALUE;

                for (DataSnapshot alarmSnapshot : snapshot.getChildren()) {
                    String startDateStr = alarmSnapshot.child("startDate").getValue(String.class);
                    String endDateStr = alarmSnapshot.child("endDate").getValue(String.class);
                    String waktu = alarmSnapshot.child("waktu").getValue(String.class);

                    if (startDateStr != null && endDateStr != null && waktu != null) {
                        try {
                            Date startDate = dateFormat.parse(startDateStr);
                            Date endDate = dateFormat.parse(endDateStr);

                            Calendar current = Calendar.getInstance();
                            Calendar start = Calendar.getInstance();
                            Calendar end = Calendar.getInstance();

                            start.setTime(startDate);
                            end.setTime(endDate);

                            // Cek tanggal dari hari ini sampai tanggal akhir
                            for (Calendar iter = (Calendar) start.clone(); !iter.after(end); iter.add(Calendar.DATE, 1)) {
                                String currentDateStr = dateFormat.format(iter.getTime());
                                Date alarmDateTime = dateTimeFormat.parse(currentDateStr + " " + waktu);

                                if (alarmDateTime != null) {
                                    long diff = alarmDateTime.getTime() - now;

                                    if (diff > 0 && diff < shortestDiff) {
                                        shortestDiff = diff;
                                        nextAlarmTime = alarmDateTime;
                                        currentAlarmId = alarmSnapshot.getKey();
                                    }
                                }
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (nextAlarmTime != null) {
                    startCountdownTo(nextAlarmTime.getTime());
                } else {
                    countdownTimer.setText("Tidak ada alarm berikutnya.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                countdownTimer.setText("Gagal mengambil alarm.");
            }
        });
    }

    private void showNotification(String alarmId) {
        Log.d("Notifikasi", "showNotification dipanggil");

        String channelId = "alarm_channel";
        String channelName = "Alarm Notification";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        DatabaseReference alarmRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("alarm")
                .child(alarmId);

        alarmRef.get().addOnSuccessListener(snapshot -> {
            String namaObat = snapshot.child("namaObat").getValue(String.class);
            String dosis = snapshot.child("dosis").getValue(String.class);

            if (namaObat == null) namaObat = "Obat";
            if (dosis == null) dosis = "-";

            // Tombol Sudah
            Intent intentSudah = new Intent(this, NotificationActionReceiver.class);
            intentSudah.putExtra("status", "Sudah");
            intentSudah.putExtra("alarmId", alarmId);
            PendingIntent pendingIntentSudah = PendingIntent.getBroadcast(
                    this,
                    0,
                    intentSudah,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Tombol Belum (hanya matikan alarm, tidak ubah data)
            Intent intentBelum = new Intent(this, NotificationActionReceiver.class);
            intentBelum.putExtra("status", "Belum");
            intentBelum.putExtra("alarmId", alarmId);
            PendingIntent pendingIntentBelum = PendingIntent.getBroadcast(
                    this,
                    1,
                    intentBelum,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            String judul = "⏰ Minum Obat: " + namaObat;
            String pesan = "Waktunya minum " + namaObat + " dengan dosis " + dosis + ". Jangan sampai lupa yaa!";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle(judul)
                    .setContentText(pesan)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(pesan))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .addAction(android.R.drawable.ic_menu_today, "Sudah", pendingIntentSudah)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Belum", pendingIntentBelum);

            notificationManager.notify(1001, builder.build());

            // Mulai alarm suara
            AlarmPlayer.play(this, R.raw.alarm1);

            if (AlarmPlayer.mediaPlayer != null) {
                AlarmPlayer.mediaPlayer.setLooping(true);
                AlarmPlayer.mediaPlayer.start();
            } else {
                Log.e("MediaPlayer", "Gagal memuat audio dari raw.");
            }

        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Gagal mengambil data alarm", e);
        });
    }




    private void startCountdownTo(long targetTimeInMillis) {
        new CountDownTimer(targetTimeInMillis - System.currentTimeMillis(), 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                String time = String.format(Locale.getDefault(),
                        "Waktu selanjutnya: %02d:%02d:%02d", hours, minutes, secs);
                countdownTimer.setText(time);
            }

            public void onFinish() {
                countdownTimer.setText("Waktu alarm tercapai!");
                showNotification(currentAlarmId); // 🔔 gunakan alarmId dari Firebase
            }

        }.start();
    }




    // Membersihkan form setelah berhasil menyimpan
    private void clearForm() {
        NamaObat.setSelection(0);  // Mengatur Spinner ke pilihan pertama
        Dosis.setSelection(0);
        edtStartDate.setText("");
        edtEndDate.setText("");
        edtWaktu.setText("");
    }
}

