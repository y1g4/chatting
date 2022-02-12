package com.example.myfirstapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myfirstapp.Listeners.UserLIstener;
import com.example.myfirstapp.R;
import com.example.myfirstapp.databinding.ActivityUsersBinding;
import com.example.myfirstapp.models.User;
import com.example.myfirstapp.adapters.userAdapter;
import com.example.myfirstapp.utilities.PreferenceManager;
import com.example.myfirstapp.utilities.constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserLIstener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v-> onBackPressed());
    }
    private void getUsers(){
        loading (true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String CurrentUserID =  preferenceManager.getString(constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (CurrentUserID.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size()>0 ){
                            userAdapter usersAdapter = new userAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }}
                        else{
                            showErrorMessage();
                        }

                });

    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.ProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.ProgressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}