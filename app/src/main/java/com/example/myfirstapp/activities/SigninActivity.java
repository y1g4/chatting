package com.example.myfirstapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myfirstapp.databinding.ActivitySigninBinding;
import com.example.myfirstapp.utilities.PreferenceManager;
import com.example.myfirstapp.utilities.constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SigninActivity extends AppCompatActivity {

    private ActivitySigninBinding binding;
    private PreferenceManager preferenceManager;
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignupActivity.class)));
        binding.buttonSignIn.setOnClickListener(v-> {
            if(isValiSignInDetails()){
                signIn();
            }
        });
    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USERS)
                .whereEqualTo(constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() >0){
                        DocumentSnapshot DocumentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.PutString(constants.KEY_USER_ID, DocumentSnapshot.getId());
                        preferenceManager.PutString(constants.KEY_NAME, DocumentSnapshot.getString(constants.KEY_NAME));
                        preferenceManager.PutString(constants.KEY_IMAGE, DocumentSnapshot.getString(constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        loading(false);
                        showToast("Unable TO Login");
                    }
                });

    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.ProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.ProgressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);

        }
    }
        private void showToast(String message){
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
        public Boolean isValiSignInDetails(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid Email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }
        else{
            return true;
        }
        }
}
