package team4.cs246;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ChatActivity can be invoked when the user taps a chat in MainActivity or when he taps a user in UsersActivity
 * This activity serves three main purposes:
 * 1) Saves conversations into the RealtimeDatabase to be retrieved in the ChatsActivity
 * 2) Saves messages into the Realtime Database
 * 3) Retrieves messages to be displayed on the screen using instances of the Messages and MessageAdapter class
 */
public class ChatActivity extends AppCompatActivity {
    // declare activity content
    private String mOtherUserId;
    private Toolbar mToolbar;
    private DatabaseReference mDatabaseRef; // to retrieve the other user id
    private FirebaseAuth mAuth;             // to retrieve my user id
    private String mCurrentUserId;
    private CircleImageView mDisplayImage;

    private Button mSendMessageBtn;
    private EditText mMessageText;
    private ImageButton mSendMediaBtn;


    // retrieving messages
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    // to bring up the one result, gallery
    private static final int GALLERY_PICK = 1;

    // storage Firebase (brett added for images)
    private StorageReference mImageStorage;

    /**
     * Initialize activity content. Set content view activity_chat.xml.
     * @param savedInstanceState used to pass data between activities
     */
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
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // initialize and get CircleImageView instance to display the other user's image
        // there might be a better way to do this, but this is just a solution
        mDisplayImage = findViewById(R.id.chat_image_view2);
        mDatabaseRef.child("Users").child(mOtherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imagePath = snapshot.child("image").getValue().toString();
                // show default image if there is not user image
                if(!imagePath.equals("default")){
                    Picasso.get().load(imagePath).into(mDisplayImage);
                }
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
        mSendMediaBtn = (ImageButton) findViewById(R.id.chat_send_media_btn);
        mMessageText = findViewById(R.id.chat_message_text);

        // image storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        // creates Chats data structures in realtime database for new conversations
        // this data is used latter in the ChatsActivity view to retrieve existing conversations
        mDatabaseRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(mOtherUserId)){

                    // metadata "timestamp" for the conversations saved to a map
                    Map<String, Object> chatAddMap = new HashMap<>();
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    // creates two data structures, one to save the conversation for the sender and one for the receiver
                    Map<String, Object> chatUserMap = new HashMap<>();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mOtherUserId, chatAddMap);
                    chatUserMap.put("Chat/" + mOtherUserId + "/" + mCurrentUserId, chatAddMap);

                    mDatabaseRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null){
                                // Shows a toast if there was an error writing into the database
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

        // retrieving messages
        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        loadMessages();

        //------- SEND TEXT ---------
        // calls sendMessage each time the send button is clicked
        mSendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        //------- GET IMAGE ---------
        mSendMediaBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1) // needs API < 22, for createChooser
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

    }

    /**
     * Sending images!
     * Image upload from devices and store in database.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData(); // image download id

            // creates Db path references for the sender and the receiver inside the "messages" data structure
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mOtherUserId;
            String other_user_ref = "messages/" + mOtherUserId + "/" + mCurrentUserId;

            // Creates a reference to an autogenerated child location to store the actual message
            DatabaseReference userMessagePush = mDatabaseRef.child("messages").child(mCurrentUserId).child(mOtherUserId).push();

            // Saves the userMessagePush key to a String
            String pushId = userMessagePush.getKey();

            // path to store image upload uri to Firebase
            StorageReference filepath = mImageStorage.child("message_images").child( pushId + ".jpg");

            // Resource used:
            // https://stackoverflow.com/questions/54009384/task-getresult-getdownloadurl-method-not-working
            // as reference. First answer.
            final UploadTask uploadTask = filepath.putFile(imageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                String download_url = uri.toString();

                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put( "message", download_url);
                messageMap.put( "type", "image");               // type is image
                messageMap.put( "time", ServerValue.TIMESTAMP);
                messageMap.put( "from", mCurrentUserId);

                Map<String, Object> messageUserMap = new HashMap<>();
                messageUserMap.put(current_user_ref + "/" + pushId, messageMap);
                messageUserMap.put(other_user_ref + "/" + pushId, messageMap);

                mMessageText.setText("");

                mDatabaseRef.updateChildren(messageUserMap, (error, ref) -> {
                    if (error != null){
                        Toast.makeText(ChatActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }));
        }
    }

    /**
     * Load messages. This method is called every time data in the conversation changes.
     */
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

    /**
     * Creates message data structure in realtime database.
     */
    private void sendMessage() {
        // converts EditText content to a string
        String message = mMessageText.getText().toString();

        // if the content of the message is not empty...
        if (!TextUtils.isEmpty(message)){

            // creates Db path references for the sender and the receiver inside the "messages" data structure
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mOtherUserId;
            String other_user_ref = "messages/" + mOtherUserId + "/" + mCurrentUserId;

            // Creates a reference to an autogenerated child location to store the actual message
            DatabaseReference userMessagePush = mDatabaseRef.child("messages").child(mCurrentUserId).child(mOtherUserId).push();
            // Saves the userMessagePush key to a String
            String pushId = userMessagePush.getKey();

            // save message and metadata to a HashMap
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("type", "text"); // type = text
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId); // adds who is sending the message to change color of background

            // save the message in two locations into the database for the two users, the pushId and the message data
            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_user_ref + "/" + pushId, messageMap);
            messageUserMap.put(other_user_ref + "/" + pushId, messageMap);

            mMessageText.setText(""); // sets the EditText to blank

            mDatabaseRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null){
                        // show an error toast if the data wasn't written in the database
                        Toast.makeText(ChatActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}