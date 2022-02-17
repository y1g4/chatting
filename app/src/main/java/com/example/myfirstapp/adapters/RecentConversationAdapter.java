package com.example.myfirstapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfirstapp.Listeners.ConversionListener;
import com.example.myfirstapp.databinding.ItemContainerRecentConversationBinding;
import com.example.myfirstapp.models.ChatMessage;
import com.example.myfirstapp.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationAdapter (List<ChatMessage> chatMessage, ConversionListener conversionListener) {
        this.chatMessages = chatMessage;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                                        )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{

    ItemContainerRecentConversationBinding binding;

    ConversionViewHolder(ItemContainerRecentConversationBinding itemContainerReceivedMessageBinding){
        super(itemContainerReceivedMessageBinding.getRoot());
        binding = itemContainerReceivedMessageBinding;
    }

    void setData(ChatMessage chatMessage){
        binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
        binding.textName.setText(chatMessage.conversionName);
        binding.textRecentMessage.setText(chatMessage.message);
        binding.getRoot().setOnClickListener(v->{
                User user = new User();
                user.id =  chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.OnConversionClicked(user);
    });
    }
}

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
