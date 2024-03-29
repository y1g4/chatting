package com.example.myfirstapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myfirstapp.adapters.ChatAdapter;
import com.example.myfirstapp.databinding.ActivityChatBinding;
import com.example.myfirstapp.models.ChatMessage;
import com.example.myfirstapp.models.User;
import com.example.myfirstapp.utilities.PreferenceManager;
import com.example.myfirstapp.utilities.constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        listenMesages();
    }
     private void  init(){
        preferenceManager =  new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,  getBitMapFromEncodedString(receiverUser.image),
                preferenceManager.getString(constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void sendMessage(){
        HashMap<String, Object>message = new HashMap<>();
        message.put(constants.KEY_SENDER_ID,preferenceManager.getString(constants.KEY_USER_ID));
        message.put(constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(constants.KEY_TIMESTAMP,new Date());
        database.collection(constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }
        else{
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID));
            conversion.put(constants.KEY_SENDER_NAME, preferenceManager.getString(constants.KEY_NAME));
            conversion.put(constants.KEY_SENDER_IMAGE, preferenceManager.getString(constants.KEY_IMAGE));
            conversion.put(constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);

        }
        binding.inputMessage.setText(null);
    }
    private void listenMesages(){
        database.collection(constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .whereEqualTo(constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                chatMessage.SenderId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                chatMessage.receiverId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                chatMessage.message = documentChange.getDocument().getString(constants.KEY_MESSAGE);
                chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(constants.KEY_TIMESTAMP));
                chatMessage.dateObject = documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1,obj2)-> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);

            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
            binding.ProgressBar.setVisibility(View.GONE);
        if (conversionId == null){
            checkForConversion();
        }
    };

    private Bitmap getBitMapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }
    public void setListeners(){

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
    public String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMM dd, yyyy - h:mm a" , Locale.getDefault()).format(date);

    }
    private void addConversion(HashMap<String, Object> conversion){
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversionId);
        documentReference.update(constants.KEY_LAST_MESSAGE, message, constants.KEY_TIMESTAMP, new Date());

    }
    private void checkForConversion(){
        if (chatMessages.size()!= 0){
            checkForConversionRemotely(preferenceManager.getString(constants.KEY_USER_ID),
                    receiverUser.id);
            checkForConversionRemotely(receiverUser.id, preferenceManager.getString(constants.KEY_USER_ID));
        }
    }
    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
        .whereEqualTo(constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(ConversionOnCompleteListener);

    }
    private final OnCompleteListener<QuerySnapshot> ConversionOnCompleteListener = task ->{
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() >0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }

};
}