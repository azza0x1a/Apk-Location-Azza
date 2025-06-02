package com.example.a08_azzaaa_apklocationn; // Sesuaikan dengan nama package Anda

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority; // Import Priority untuk setPriority
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // Kode request permission
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView locationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi UI
        locationTextView = findViewById(R.id.locationTextView);

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Mulai cek dan minta izin lokasi
        checkLocationPermission();
    }

    // --- Metode untuk Cek dan Minta Izin Lokasi ---
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Izin belum diberikan, minta izin kepada pengguna
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Izin sudah diberikan, bisa langsung ambil lokasi
            Toast.makeText(this, "Izin lokasi sudah diberikan.", Toast.LENGTH_SHORT).show();
            getAndStartLocationUpdates();
        }
    }

    // --- Callback setelah pengguna merespon permintaan izin ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan
                Toast.makeText(this, "Izin lokasi diberikan!", Toast.LENGTH_SHORT).show();
                getAndStartLocationUpdates();
            } else {
                // Izin ditolak
                Toast.makeText(this, "Izin lokasi ditolak. Aplikasi mungkin tidak berfungsi dengan baik.", Toast.LENGTH_LONG).show();
                locationTextView.setText("Izin lokasi ditolak.");
            }
        }
    }

    // --- Metode untuk mendapatkan lokasi terakhir dan memulai pembaruan berkelanjutan ---
    private void getAndStartLocationUpdates() {
        // Periksa lagi izin sebelum mengambil lokasi (penting untuk safety)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Seharusnya tidak terjadi jika checkLocationPermission() sudah dipanggil
            return;
        }

        // 1. Mendapatkan lokasi terakhir yang diketahui
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Lokasi terakhir tersedia
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String message = "Lokasi Terakhir:\nLat: " + String.format("%.6f", latitude) + "\nLon: " + String.format("%.6f", longitude);
                            locationTextView.setText(message);
                            Toast.makeText(MainActivity.this, "Berhasil mendapatkan lokasi terakhir.", Toast.LENGTH_SHORT).show();
                        } else {
                            locationTextView.setText("Tidak dapat menemukan lokasi terakhir.");
                            Toast.makeText(MainActivity.this, "Tidak dapat menemukan lokasi terakhir yang diketahui.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // 2. Menyiapkan dan memulai pembaruan lokasi berkelanjutan
        createLocationCallback(); // Inisialisasi callback
        startLocationUpdates();   // Mulai pembaruan
    }

    // --- Metode untuk membuat LocationCallback ---
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Lokasi baru diterima
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String message = "Update Lokasi:\nLat: " + String.format("%.6f", latitude) + "\nLon: " + String.format("%.6f", longitude);
                        locationTextView.setText(message);
                        Toast.makeText(MainActivity.this, "Lokasi Diperbarui!", Toast.LENGTH_SHORT).show();
                        // Anda bisa menambahkan logika lain di sini, seperti mengirim lokasi ke server
                    }
                }
            }
        };
    }

    // --- Metode untuk memulai pembaruan lokasi ---
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // Akurasi tinggi, update setiap 10 detik
                .setMinUpdateIntervalMillis(5000) // Paling cepat update setiap 5 detik
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Harusnya sudah ditangani oleh checkLocationPermission()
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // --- Metode untuk menghentikan pembaruan lokasi saat Activity dijeda ---
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    // --- Metode untuk melanjutkan pembaruan lokasi saat Activity dilanjutkan ---
    @Override
    protected void onResume() {
        super.onResume();
        // Cek kembali izin dan mulai pembaruan jika sudah diberikan
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    // --- Metode untuk menghentikan pembaruan lokasi ---
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Toast.makeText(this, "Pembaruan lokasi dihentikan.", Toast.LENGTH_SHORT).show();
        }
    }
}