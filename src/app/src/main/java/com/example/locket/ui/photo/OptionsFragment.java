package com.example.locket.ui.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.locket.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptionsFragment extends Fragment {

    private static final String ARG_OPTION = "option";
    private String option;

    private static final int LOCATION_PERMISSION_CODE = 1001;
    private static final int REQUEST_PLACE_PICKER = 123;

    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<String[]> nearbyPlaces = new ArrayList<>();

    EditText messageInput;
    Button optionSongButton;
    Button optionLocationButton;
    private boolean hasFetchedLocation = false;

    public interface OnOptionSelectedListener {
        void onMessageEntered(String message);
        void onMusicSelected(String song);
        void onLocationSelected(String location);
    }

    private OnOptionSelectedListener listener;

    public static OptionsFragment newInstance(String option) {
        OptionsFragment fragment = new OptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OPTION, option);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionSelectedListener) {
            listener = (OnOptionSelectedListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            option = getArguments().getString(ARG_OPTION);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_message_option, container, false);

        messageInput = view.findViewById(R.id.message_input);
        optionSongButton = view.findViewById(R.id.option_button_song);
        optionLocationButton = view.findViewById(R.id.option_button_location);

        messageInput.setVisibility(View.GONE);
        optionSongButton.setVisibility(View.GONE);
        optionLocationButton.setVisibility(View.GONE);

        if (option.contains("tin nhắn")) {
            messageInput.setVisibility(View.VISIBLE);

            messageInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (listener != null) listener.onMessageEntered(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        } else if (option.contains("nhạc")) {
            optionSongButton.setVisibility(View.VISIBLE);
            optionSongButton.setText(option);

            optionSongButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đã chọn bài hát mẫu 🎵", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onMusicSelected("sample_song.mp3");
            });
        } else if (option.contains("vị trí")) {
            optionLocationButton.setVisibility(View.VISIBLE);
            optionLocationButton.setText(option);

            optionLocationButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("MapboxLocation", "Chưa có quyền, đang request...");
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_CODE);
                } else {
                    Log.d("MapboxLocation", "Đã có quyền, gọi getCurrentLocation()");
                    getCurrentLocation();
                }
            });
        }

        return view;
    }

    private void getCurrentLocation() {
        if (hasFetchedLocation) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("MapboxLocation", "Chưa có quyền vị trí");
            return;
        }
        hasFetchedLocation = true;

        LocationRequest request = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setNumUpdates(1);

        fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    Log.d("MapboxLocation", "Lấy từ requestLocationUpdates: " + location.getLatitude() + ", " + location.getLongitude());
                    nearbyPlaces.clear();
                    fetchNearbyWithOverpass(location.getLatitude(), location.getLongitude());
                }
            }
        }, Looper.getMainLooper());
    }

    private void fetchNearbyWithOverpass(double lat, double lon) {
        new Thread(() -> {
            try {
                String query = "[out:json];node(around:500," + lat + "," + lon + ")[amenity];out;";
                String overpassUrl = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, "UTF-8");

                HttpURLConnection conn = (HttpURLConnection) new URL(overpassUrl).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray elements = json.getJSONArray("elements");

                if (elements.length() == 0) {
                    Log.d("MapboxLocation", "Không tìm thấy địa điểm nào gần đó (Overpass)");
                }

                CountDownLatch latch = new CountDownLatch(elements.length());
                ExecutorService executor = Executors.newFixedThreadPool(5);

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject node = elements.getJSONObject(i);
                    JSONObject tags = node.optJSONObject("tags");
                    if (tags != null) {
                        String name = tags.optString("name", "").trim();
                        if (name.isEmpty()) {
                            latch.countDown();
                            continue;
                        }
                        double latPoi = node.getDouble("lat");
                        double lonPoi = node.getDouble("lon");

                        executor.execute(() -> {
                            try {
                                getAddressFromLatLonWithName(name, latPoi, lonPoi);
                            } finally {
                                latch.countDown();
                            }
                        });
                    } else {
                        latch.countDown();
                    }
                }

                executor.shutdown();
                latch.await();

                requireActivity().runOnUiThread(() -> {
                    if (!nearbyPlaces.isEmpty()) {
                        Intent intent = new Intent(getContext(), PlaceListActivity.class);
                        intent.putExtra("places", nearbyPlaces);
                        startActivityForResult(intent, REQUEST_PLACE_PICKER);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getAddressFromLatLonWithName(String name, double lat, double lon) {
        try {
            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&addressdetails=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MyAndroidApp/1.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject address = json.getJSONObject("address");

            String road = address.optString("road", "");
            String suburb = address.optString("suburb", "");
            String district = address.optString("city_district", address.optString("city", ""));
            String city = address.optString("state", "");

            List<String> parts = new ArrayList<>();
            if (!road.isEmpty()) parts.add(road);
            if (!suburb.isEmpty()) parts.add(suburb);
            if (!district.isEmpty()) parts.add(district);
            if (!city.isEmpty()) parts.add(city);

            String full = String.join(", ", parts);
            synchronized (nearbyPlaces) {
                nearbyPlaces.add(new String[]{name, full});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PLACE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            String selectedName = data.getStringExtra("selected_name");
            optionLocationButton.setText(selectedName);
            if (listener != null) listener.onLocationSelected(selectedName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!option.contains("vị trí")) return;
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.d("MapboxLocation", "Quyền vị trí đã được cấp, gọi lại getCurrentLocation()");
            getCurrentLocation();
        } else {
            Log.e("MapboxLocation", "Người dùng từ chối quyền vị trí");
        }
    }


}