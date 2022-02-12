package com.example.myfirstapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfirstapp.Listeners.UserLIstener;
import com.example.myfirstapp.databinding.ItemContainerUserBinding;
import com.example.myfirstapp.models.User;

import java.util.List;


public class userAdapter extends RecyclerView.Adapter<userAdapter.userViewHolder>{
    private final UserLIstener userLIstener;
    public userAdapter(List<User> users, UserLIstener userLIstener) {
        this.users = users;
        this.userLIstener =  userLIstener;
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding
                .inflate(LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new userViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private final List<User> users;

    class userViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserBinding binding;
        userViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }
        void setUserData (User users){
            binding.textName.setText(users.name);
            binding.textEmail.setText(users.email);
            binding.imageProfile.setImageBitmap(getUserimages(users.image));
            binding.getRoot().setOnClickListener(v-> userLIstener.onClicked(users));
        }
    }

    private Bitmap getUserimages(String encodedImage){
        byte[]  bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

