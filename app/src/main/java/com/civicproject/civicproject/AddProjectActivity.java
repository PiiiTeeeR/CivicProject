package com.civicproject.civicproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddProjectActivity extends AppCompatActivity {

    Button buttonAddProject;
    ImageButton buttonCamera;
    TextView textViewLocation, textViewDate, textViewAuthor;
    LocationManager locationManager;
    LocationListener locationListener;
    EditText editTextSubject, editTextDesctiption;
    ImageView imageViewPicture;
    String tempAuthorKey;
    Double locationX = Double.NaN, locationY = Double.NaN;
    Uri file;
    private Camera camera = null;
    private MyFTPClientFunctions ftpclient = null;
    private static final String TAG = "AddProjectActivity";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrolling_addproject);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        init();
        events();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_addproject);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        DateFormat df = new SimpleDateFormat("d.MM.yyyy, HH:mm");
        textViewDate.setText(df.format(Calendar.getInstance().getTime()));

        SharedPreferences myprefs = getSharedPreferences("user", MODE_PRIVATE);

        String name = myprefs.getString("name", null);
        String surname = myprefs.getString("surname", null);
        tempAuthorKey = myprefs.getString("author_key", null);
        textViewAuthor.setText(name + " " + surname);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                textViewLocation.setText(location.getLatitude() + " " + location.getLongitude());
                locationX = location.getLatitude();
                locationY = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                buttonCamera.setEnabled(false);
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
            }
            return;
        } else {
            configureButton();
        }
    }

    public void init() {
        buttonAddProject = (Button) findViewById(R.id.buttonAddProject);
        buttonCamera = (ImageButton) findViewById(R.id.buttonCamera);
        textViewLocation = (TextView) findViewById(R.id.textViewLocation);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewAuthor = (TextView) findViewById(R.id.textViewAuthor);
        editTextSubject = (EditText) findViewById(R.id.editTextSubject);
        editTextDesctiption = (EditText) findViewById(R.id.editTextDesctiption);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        imageViewPicture = (ImageView) findViewById(R.id.imageViewPicture);

        camera = new Camera();
        ftpclient = new MyFTPClientFunctions();
    }

    public void events() {
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file = Uri.fromFile(camera.getOutputMediaFile());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
                startActivityForResult(intent, 100);
            }
        });

        buttonAddProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locationX.isNaN() && !locationY.isNaN()) {
                    if (locationX <= 51.843678 && locationX >= 51.690382 && locationY <= 19.619980 && locationY >= 19.324036) {
                        String subject = editTextSubject.getText().toString();
                        String description = editTextDesctiption.getText().toString();
                        String author = textViewAuthor.getText().toString();
                        String date = textViewDate.getText().toString();
                        String location = textViewLocation.getText().toString();
                        String type = "addProject";
                        String image = ftpUploadImage();
                        BackgroundWorker backgroundWorker = new BackgroundWorker(AddProjectActivity.this);
                        backgroundWorker.execute(type, author, subject, description, location, date, tempAuthorKey, image);
                        editTextSubject.setText("");
                        editTextDesctiption.setText("");
                        if (textViewLocation == null) {
                            Toast.makeText(getApplicationContext(), "Twój projekt został dodany bez lokalizacji, nie wyświetli się na mapie...", Toast.LENGTH_LONG).show();
                        }
                        Toast.makeText(getApplicationContext(), "Dodano projekt. Bedzie on widoczny po ponownym zalogowaniu ; )", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Znajdujesz się poza Łodzią twój projekt nie może zostać dodany...", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Musisz poczekać na znalezienie twojej lokalizacji...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Civic Project/IMG_CP" + ".jpg");

                imageViewPicture.setImageResource(0);

                String path = file.toString();
                String pathCompressed = camera.compressImage(path);
                imageViewPicture.setImageURI(Uri.parse(pathCompressed));

                //File image = new File(path);
                //image.delete();
                //File imageCompressed = new File(pathCompressed);
                //imageCompressed.delete();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                buttonCamera.setEnabled(true);
                configureButton();
                break;
            default:
                break;
        }
    }

    private void configureButton() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    }

    public String ftpUploadImage() {
        final String desFileName = tempAuthorKey + "_" + textViewDate.getText() + ".jpg";

        new Thread(new Runnable() {
            public void run() {
                boolean status = false;
                status = ftpclient.ftpConnect("serwer1633804.home.pl", "serwer1633804", "33murs0tKiby", 21);
                if (status == true) {
                    Log.d(TAG, "Połączenie udane");
                } else {
                    Log.d(TAG, "Połączenie nieudane");
                }
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Civic Project/IMG_CP_COMPRESSED" + ".jpg");
                String srcFilePath = file.toString();
                ftpclient.ftpChangeDirectory("/images/");
                ftpclient.ftpUpload(srcFilePath, desFileName);

                status = ftpclient.ftpDisconnect();
                if (status == true) {
                    Log.d(TAG, "Połączenie zakończone");
                } else {
                    Log.d(TAG, "Połączenie nie mogło zostać zakończone");
                }
            }
        }).start();
        return desFileName;
    }
}
