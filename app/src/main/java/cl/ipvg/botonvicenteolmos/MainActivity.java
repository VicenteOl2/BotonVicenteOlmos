package cl.ipvg.botonvicenteolmos;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvLat, tvLon, tvDir, tvAccel, tvLight;
    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor;
    private ToneGenerator toneGen;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular TextViews
        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        ;




        // Firebase
        db = FirebaseDatabase.getInstance().getReference("Registros");

        // Permisos GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startGPS();
        }
    }

    private void startGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                tvLat.setText("Lat: " + lat);
                tvLon.setText("Lon: " + lon);

                // Dirección
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> list = geocoder.getFromLocation(lat, lon, 1);
                    String dir = list != null && list.size() > 0 ? list.get(0).getAddressLine(0) : "No disponible";


                    // Guardar en Firebase
                    String id = UUID.randomUUID().toString();
                    db.child(id).setValue(new Registro(lat, lon));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            tvAccel.setText("Acelerómetro: x=" + event.values[0] + ", y=" + event.values[1] + ", z=" + event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            tvLight.setText("Luz: " + event.values[0] + " lx");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Clase para guardar en Firebase
    public static class Registro {
        public double lat, lon;


        public Registro() {} // Constructor vacío requerido por Firebase

        public Registro(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;

        }
    }
}