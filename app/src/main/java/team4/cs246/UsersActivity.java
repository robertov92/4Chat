package team4.cs246;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * UsersActivity is invoked from MainActivity
 * It displays a list, using a RecycleView, of all the existing app uses
 * When a user from the list is tapped, a new conversation is created and the ChatActivity is invoked
 */
public class UsersActivity extends AppCompatActivity {
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private Toolbar mToolbar;

    /**
     * Overrides onCreate
     * Initializes all the objects used in the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // toolbar
        mToolbar = (Toolbar)findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Tap to Start Chatting!");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // back button on toolbar

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    /**
     * Overrides onStart
     * Populates RecyclerView
     */
    @Override
    protected void onStart() {
        super.onStart();

        // initiates FirebaseRecyclerAdapter to display users
        // similar to what happens in MainActivity, this FirebaseRecyclerVie uses the static class
        // UsersActivity.UsersViewHolder which is written at the bottom of this activity
        FirebaseRecyclerAdapter<Users, UsersActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class, R.layout.users_single_layout, UsersActivity.UsersViewHolder.class, mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersActivity.UsersViewHolder usersViewHolder, Users users, int i) {
                // sets each user's name, status, image and gets the a user's reference
                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setImage(users.getImage());
                String list_user_id = getRef(i).getKey(); // getting ref to click on it

                // listens for data changes on the database reference
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    // send user to chat activity when a user is tapped
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(UsersActivity.this, ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                startActivity(chatIntent);
                                Toast.makeText(getApplicationContext(), "Let's Chat!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        // mUsersList is our RecyclerView, firebaseRecyclerAdapter is the adapter we just built
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    /**
     * Helper class used to initialize the items from the user_single_layout.xml
     * Objects form this class are used in the FirebaseRecyclerAdapter above
     */
    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
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