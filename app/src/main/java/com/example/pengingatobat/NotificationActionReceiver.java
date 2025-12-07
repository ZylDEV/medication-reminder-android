package com.example.pengingatobat;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = intent.getStringExtra("status");
        String alarmId = intent.getStringExtra("alarmId");

        if (status != null && alarmId != null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                DatabaseReference alarmRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("alarm")
                        .child(alarmId);

                if (status.equals("Sudah")) {
                    alarmRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String namaObat = snapshot.child("namaObat").getValue(String.class);
                                if (namaObat == null) return;

                                // Simpan status dan waktu ditekan
                                alarmRef.child("status").setValue("Sudah");
                                String waktuSekarang = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                alarmRef.child("waktuDitekan").setValue(waktuSekarang);

                                // Update stok di path 'obat'
                                DatabaseReference obatRef = FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(userId)
                                        .child("obat");

                                obatRef.orderByChild("namaObat").equalTo(namaObat)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                boolean stokDitemukan = false;
                                                for (DataSnapshot obatSnapshot : dataSnapshot.getChildren()) {
                                                    Long stok = obatSnapshot.child("stok").getValue(Long.class);
                                                    String namaObatDb = obatSnapshot.child("namaObat").getValue(String.class);

                                                    if (namaObatDb != null && namaObatDb.equals(namaObat) && stok != null && stok > 0) {
                                                        long stokBaru = stok - 1;
                                                        obatSnapshot.getRef().child("stok").setValue(stokBaru);
                                                        alarmRef.child("stokAkhir").setValue(stokBaru);
                                                        stokDitemukan = true;
                                                        break;
                                                    }
                                                }

                                                if (!stokDitemukan) {
                                                    Log.w("NotifReceiver", "Obat tidak ditemukan atau stok habis.");
                                                }

                                                // Setelah status diperbarui dan stok diproses, hentikan alarm
                                                stopAlarmSound(context);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e("NotifReceiver", "Gagal update stok: " + error.getMessage());
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("NotifReceiver", "Gagal ambil data alarm: " + error.getMessage());
                        }
                    });

                } else if (status.equals("Belum")) {
                    // Jika pengguna membatalkan alarm
                    alarmRef.child("status").setValue("Belum");
                    alarmRef.child("waktuDitekan").setValue("Dibatalkan");
                    Log.d("NotifReceiver", "Pengguna membatalkan alarm.");

                    // Hentikan alarm segera setelah membatalkan
                    stopAlarmSound(context);
                }
            }
        }
    }

    private void stopAlarmSound(Context context) {
        if (AlarmPlayer.mediaPlayer != null && AlarmPlayer.mediaPlayer.isPlaying()) {
            AlarmPlayer.stop();
            Log.d("NotifReceiver", "Alarm dihentikan.");
        } else {
            Log.d("NotifReceiver", "MediaPlayer tidak aktif atau sudah berhenti.");
        }

        // Hapus notifikasi
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1001);
        }
    }
}



