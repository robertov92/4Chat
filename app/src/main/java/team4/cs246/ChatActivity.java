package team4.cs246;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    private String mOtherUserId;
    private Toolbar mToolbar;
    private DatabaseReference mDatabaseRef; // to retrieve the other user id
    private FirebaseAuth mAuth; // to retrieve my user id
    private String mCurrentUserId;

    private Button mSendMessageBtn;
    private EditText mMessageText;

    // retrieving messages
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // retrieve the other user id from intent
        mOtherUserId = getIntent().getStringExtra("user_id");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // retrieve the other user name from database using id and put it in the toolbar
        mDatabaseRef.child("Users").child(mOtherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nameAsString = snapshot.child("name").getValue().toString();
                mToolbar = findViewById(R.id.chat_toolbar);
                setSupportActionBar(mToolbar);
                getSupportActionBar().setTitle(nameAsString);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // retrieve current user id and save it to a string
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        // message input
        mSendMessageBtn = findViewById(R.id.chat_send_msg_btn);
        mMessageText = findViewById(R.id.chat_message_text);

        // creates Chats data structure in realtime database
        mDatabaseRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(mOtherUserId)){
                    Map<String, Object> chatAddMap = new HashMap<>();
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map<String, Object> chatUserMap = new HashMap<>();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mOtherUserId, chatAddMap);
                    chatUserMap.put("Chat/" + mOtherUserId + "/" + mCurrentUserId, chatAddMap);

                    mDatabaseRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null){
                                Toast.makeText(ChatActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // retrieving messages
        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        loadMessages();

    }
    // load messages
    private void loadMessages() {
        mDatabaseRef.child("messages").child(mCurrentUserId).child(mOtherUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() -1); // shows the last message
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // creates message data structure in realtime database
    private void sendMessage() {
        String message = mMessageText.getText().toString();
        if (!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mOtherUserId;
            String other_user_ref = "messages/" + mOtherUserId + "/" + mCurrentUserId;

            DatabaseReference userMessagePush = mDatabaseRef.child("messages").child(mCurrentUserId).child(mOtherUserId).push();
            String pushId = userMessagePush.getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId); // adds who is sending the message to change color of background

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_user_ref + "/" + pushId, messageMap);
            messageUserMap.put(other_user_ref + "/" + pushId, messageMap);

            mMessageText.setText(""); // sets the EditText blank

            mDatabaseRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null){
                        Toast.makeText(ChatActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}