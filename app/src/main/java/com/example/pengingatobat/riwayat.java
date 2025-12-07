package com.example.pengingatobat;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class riwayat extends AppCompatActivity {

    private LinearLayout riwayatLayout;
    private Button btnDownloadPdf;
    private DatabaseReference databaseReference;
    private LinearLayout btnHome, btnStockObat, btnProfil;
    private FirebaseAuth mAuth;

    // Create a List to store the riwayat data
    private List<String[]> riwayatData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        btnHome = findViewById(R.id.btnHome);
        btnStockObat = findViewById(R.id.btnStockObat);
        btnProfil = findViewById(R.id.btnProfil);

        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        riwayatLayout = findViewById(R.id.riwayatlayout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        btnHome.setOnClickListener(v -> {
            // Aksi tombol Home
            Intent intent = new Intent(riwayat.this, home.class);
            startActivity(intent);
        });

        btnStockObat.setOnClickListener(v -> {
            // Aksi tombol Stock Obat
            Intent intent = new Intent(riwayat.this, kelolaobat.class);
            startActivity(intent);
        });

        btnProfil.setOnClickListener(v -> {
            // Aksi tombol Profil
            Intent intent = new Intent(riwayat.this, akun.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("alarm");
            getRiwayatData();
        } else {
            TextView errorView = new TextView(this);
            errorView.setText("User belum login.");
            riwayatLayout.addView(errorView);
        }

        btnDownloadPdf.setOnClickListener(v -> generatePdf(riwayatData));
    }

    private void getRiwayatData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                riwayatLayout.removeAllViews();
                riwayatData.clear();  // Clear previous data

                List<DataSnapshot> snapshotList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshotList.add(snapshot);
                }

                // Tampilkan data mulai dari yang terakhir
                for (int i = snapshotList.size() - 1; i >= 0; i--) {
                    DataSnapshot snapshot = snapshotList.get(i);

                    String namaObat = snapshot.child("namaObat").getValue(String.class);
                    String dosis = snapshot.child("dosis").getValue(String.class);
                    String waktu = snapshot.child("waktu").getValue(String.class);
                    String startDate = snapshot.child("startDate").getValue(String.class);
                    String endDate = snapshot.child("endDate").getValue(String.class);
                    Long stokAkhirLong = snapshot.child("stokAkhir").getValue(Long.class);
                    String stokAkhir = (stokAkhirLong != null) ? String.valueOf(stokAkhirLong) : null;
                    String status = snapshot.child("status").getValue(String.class);
                    String waktuDitekan = snapshot.child("waktuDitekan").getValue(String.class);

                    addRiwayatItem(namaObat, dosis, waktu, startDate, endDate, stokAkhir, status, waktuDitekan);

                    // Add the data to riwayatData list
                    riwayatData.add(new String[] { namaObat, waktu, dosis, status, startDate, endDate, waktuDitekan });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                TextView errorView = new TextView(riwayat.this);
                errorView.setText("Gagal memuat data: " + databaseError.getMessage());
                riwayatLayout.addView(errorView);
            }
        });
    }

    private void addRiwayatItem(String namaObat, String dosis, String waktu,
                                String startDate, String endDate, String stokAkhir, String status, String waktuDitekan) {
        LinearLayout itemRiwayat = new LinearLayout(this);
        itemRiwayat.setOrientation(LinearLayout.VERTICAL);
        itemRiwayat.setPadding(16, 16, 16, 12);

        itemRiwayat.addView(makeTextView("Nama Obat: " + namaObat));
        itemRiwayat.addView(makeTextView("Dosis: " + dosis));
        itemRiwayat.addView(makeTextView("Waktu Alarm: " + waktu));
        itemRiwayat.addView(makeTextView("Tanggal Mulai: " + startDate));
        itemRiwayat.addView(makeTextView("Tanggal Akhir: " + endDate));
        if (stokAkhir != null && !stokAkhir.isEmpty()) {
            itemRiwayat.addView(makeTextView("Stok Akhir: " + stokAkhir));
        }
        itemRiwayat.addView(makeTextView("Status: " + status));
        itemRiwayat.addView(makeTextView("Waktu Ditekan: " + waktuDitekan));

        riwayatLayout.addView(itemRiwayat);
    }

    private TextView makeTextView(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(16);
        view.setTextColor(getResources().getColor(R.color.black));
        return view;
    }

    private void generatePdf(List<String[]> riwayatData) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setAntiAlias(true);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int startX = 40;
        int startY = 50;
        int tableWidth = 515;
        int rowHeight = 30;

        // Header Judul (judul di atas tabel)
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Diabetes Buddy", startX, startY, paint);
        startY += 30;
        canvas.drawText("Riwayat Penggunaan Obat", startX, startY, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        startY += 30;

        // Garis horizontal untuk judul
        canvas.drawLine(startX, startY, startX + tableWidth, startY, paint);
        startY += 5;

        // Tabel Baru di atas Data Riwayat
        String[] tableHeader = {"Nama Obat", "Waktu", "Dosis", "Status", "Tanggal Mulai", "Tanggal Akhir", "Waktu Ditekan"};
        int[] columnWidths = {100, 50, 60, 60, 90, 90, 80}; // Lebar kolom yang lebih sesuai
        int colX = startX; // Mendeklarasikan colX di sini

        // Header tabel (judul untuk kolom)
        paint.setFakeBoldText(true); // Membuat header tabel menjadi tebal
        for (int i = 0; i < tableHeader.length; i++) {
            canvas.drawText(tableHeader[i], colX + 5, startY + 20, paint);
            colX += columnWidths[i];
        }

        // Garis bawah header tabel
        startY += rowHeight;
        canvas.drawLine(startX, startY, startX + tableWidth, startY, paint);
        startY += 5;

        // Data tabel riwayat
        paint.setFakeBoldText(false); // Reset ke teks biasa setelah header tabel
        for (String[] data : riwayatData) {
            if (startY + rowHeight > 800) {
                // Tambah halaman baru jika halaman penuh
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                startY = 50;
            }

            // Gambar isi baris langsung
            colX = startX; // Reset colX untuk setiap baris baru
            for (int i = 0; i < data.length; i++) {
                String textToDraw = (data[i] != null) ? data[i] : "";
                canvas.drawText(textToDraw, colX + 5, startY + 20, paint);
                colX += columnWidths[i];
            }

            // Garis bawah baris
            startY += rowHeight;
            canvas.drawLine(startX, startY, startX + tableWidth, startY, paint);
        }

        pdfDocument.finishPage(page);

        // Simpan file ke folder Download
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RiwayatObat.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(this, "PDF berhasil disimpan di: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        pdfDocument.close();
    }



}

