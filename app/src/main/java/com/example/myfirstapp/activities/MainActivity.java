package com.example.myfirstapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.myfirstapp.activities.SigninActivity;
import com.example.myfirstapp.databinding.ActivityMainBinding;
import com.example.myfirstapp.utilities.PreferenceManager;
import com.example.myfirstapp.utilities.constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import android.util.Base64;
import android.widget.Toast;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;
private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }
    private void setListeners(){

        binding.imageSignout.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v-> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }
    public void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void getToken(){
       FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    public void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(constants.KEY_USER_ID)
                );
        documentReference.update(constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update Token"));
    }
    private void  signOut(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SigninActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to log out"));

    }
}