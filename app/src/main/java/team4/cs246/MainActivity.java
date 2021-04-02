package team4.cs246;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The user is redirected to the StartActivity if he hasn't logged in
 * MainActivity is where a list of existing chats for the current user are displayed
 * When an item from the list is tapped, the user is redirected to the ChatActivity
 * It also has a menu to access SettingsActivity, UsersActivity, and an option to logout
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    // Chats
    private RecyclerView mConversationList;
    private String mCurrentUserId;
    private DatabaseReference mConversationDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    /**
     * Overrides onCreate
     * Initializes all the objects used in the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Toolbar
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chats");

        // build view if the user exists in the Authentication list
        if(mAuth.getCurrentUser() != null){
            // Initiate database references
            mConversationList = findViewById(R.id.conversation_list);
            mCurrentUserId = mAuth.getCurrentUser().getUid();
            mConversationDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
            mConversationDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);
            mUsersDatabase.keepSynced(true);

            // Initialize LinearLayoutManager to display chats
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);

            mConversationList.setHasFixedSize(true);
            mConversationList.setLayoutManager(linearLayoutManager);
        }

    }

    /**
     * Overrides onStart
     * Populates RecyclerView
     */
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStart();
        }

        // build view if the user exists in the Authentication list
        if(mAuth.getCurrentUser() != null){
            // this query object is used later to order the chats by it
            Query conversationQuery = mConversationDatabase.orderByChild("timestamp");

            // initiates FirebaseRecyclerAdapter to display chats
            // it uses the static class MainActivity.ConversationViewHolder which is written at the bottom of this activity
            FirebaseRecyclerAdapter<Conversation, MainActivity.ConversationViewHolder> firebaseConversationAdapter = new FirebaseRecyclerAdapter<Conversation, MainActivity.ConversationViewHolder>(
                    Conversation.class, R.layout.users_single_layout, MainActivity.ConversationViewHolder.class, conversationQuery
            ) {
                @Override
                protected void populateViewHolder(MainActivity.ConversationViewHolder conversationViewHolder, Conversation conversation, int i) {
                    // gets the id of the other user with whom we will have a conversation
                    final String listUserId = getRef(i).getKey();
                    // Query to retrieve the last message we have had with other users
                    Query lastMessageQuery = mMessageDatabase.child(listUserId).limitToLast(1);

                    // takes the last message and sets it to be display in the layout
                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String data = snapshot.child("message").getValue().toString();
                            conversationViewHolder.setMessage(data);
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

                    // sets the username and image in each conversation item from the list
                    mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            final String userName = snapshot.child("name").getValue().toString();
                            String userImage = snapshot.child("image").getValue().toString();
                            conversationViewHolder.setName(userName);
                            conversationViewHolder.setImage(userImage);

                            // OnClickListener to open the ChatActivity when a chat from the RecyclerView is tapped
                            conversationViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // sends the user to ChatActivity
                                    Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                                    chatIntent.putExtra("user_id", listUserId);
                                    startActivity(chatIntent);

                                    // here I'm updating the the Chat timestamp so the chat goes to the beginning of the list when tapped
                                    HashMap<String, Object> updateTime = new HashMap<>();
                                    updateTime.put("timestamp", ServerValue.TIMESTAMP);
                                    mConversationDatabase.child(listUserId).updateChildren(updateTime).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "Let's Chat!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            };
            // mConversationList is the RecyclerView, firebaseConversationAdapter is the final adapter we just built
            mConversationList.setAdapter(firebaseConversationAdapter);
        }

    }

    /**
     * Send to start used when we log out
     */
    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    /**
     * Creating main menu
     * @param menu a menu
     * @return bool
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    /**
     * Main-menu options
     * @param item a menu
     * @return a bool
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        // if the first item is tapped, go to SettingsActivity
        if (item.getItemId() == R.id.main_settings_btn) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        // if the second item is tapped, go to SettingsUsers
        if (item.getItemId() == R.id.main_users_btn) {
            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);
        }
        // if the third item is tapped, logout and go invoke send to start
        if (item.getItemId() == R.id.main_logout_btn) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        return true;
    }

    /**
     * Helper class used to initialize the items from the user_single_layout.xml
     * Objects form this class are used in the FirebaseRecyclerAdapter above
     */
    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);
        }

        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setImage(String image){
            CircleImageView circleImageView = mView.findViewById(R.id.user_single_picture);
            // show default image if there is not user image
            if(!image.equals("default")){
                Picasso.get().load(image).into(circleImageView);
            }
        }
    }
}
