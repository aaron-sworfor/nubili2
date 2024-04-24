package com.example.nubli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.util.ArrayList;
import java.util.Set;

import android.content.DialogInterface;

import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;


public class MainActivity3 extends AppCompatActivity {
    TextView btnbuscar, btnconectar, btndesconectar, tvtemperatura;
    Spinner deviceSpinner;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "temperature_notification";

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter<String> deviceAdapter;
    private BluetoothGatt bluetoothGatt;
    ArrayList<BluetoothDevice> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceSpinner = findViewById(R.id.deviceSpinner);
        btnbuscar = (TextView) findViewById(R.id.btnbuscar);
        btnconectar = (TextView) findViewById(R.id.btnconectar);
        btndesconectar = (TextView) findViewById(R.id.btndesconectar);
        tvtemperatura = (TextView) findViewById(R.id.tvtemperatura);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(deviceAdapter);

        btnbuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevices();
            }
        });

        btnconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDevice();
            }
        });
        btndesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectDevice();
            }
        });
        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void searchDevices() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            doSearchDevices();
        }
    }

    private void doSearchDevices() {
        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            devices.clear();
            deviceAdapter.clear();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    devices.add(device);
                    deviceAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
            deviceAdapter.notifyDataSetChanged();
        } catch (SecurityException e) {
            showLocationPermissionDeniedDialog();
        }
    }

    private void showLocationPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("La búsqueda de dispositivos Bluetooth requiere acceso a la ubicación. ¿Desea habilitar el acceso ahora?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doSearchDevices();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado. No se puede buscar dispositivos Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectToDevice() {
        int position = deviceSpinner.getSelectedItemPosition();
        if (position != AdapterView.INVALID_POSITION) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return;
            }

            BluetoothDevice selectedDevice = devices.get(position);
            bluetoothGatt = selectedDevice.connectGatt(this, false, bluetoothGattCallback);
        }
    }

    private void disconnectDevice() {
        if (bluetoothGatt != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            showToast("Desconectado del dispositivo");
            btndesconectar.setVisibility(View.GONE);
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                showToast("Conectado al dispositivo");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btndesconectar.setVisibility(View.VISIBLE);
                    }
                });
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                showToast("Conexión perdida con el dispositivo");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btndesconectar.setVisibility(View.GONE);
                    }
                });
            }
        }

        // Implementa otros métodos de BluetoothGattCallback aquí
    };

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity3.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Método para actualizar la temperatura y verificar si excede los 30 grados Celsius
    private void updateTemperature(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] parts = data.split("\n");
                if (parts.length >= 2) {
                    String humidity = parts[0].trim();
                    String temp = parts[1].trim();
                    tvtemperatura.setText("Humedad: " + humidity + "\nTemp: " + temp);

                    try {
                        double temperatureValue = Double.parseDouble(temp.substring(5, temp.length() - 3));
                        if (temperatureValue > 30) {
                            showTemperatureNotification();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Método para mostrar una notificación emergente cuando la temperatura excede los 30 grados Celsius
    private void showTemperatureNotification() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no se ha concedido el permiso, solicítalo
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            // No muestres la notificación en este punto, ya que aún no tienes permiso
            return;
        }

        // Creamos la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.nubli)
                .setContentTitle("¡Alerta de temperatura!")
                .setContentText("La temperatura ha superado los 30 grados Celsius.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        // Mostramos la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(123, builder.build());
    }
}