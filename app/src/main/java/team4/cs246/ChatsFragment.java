package team4.cs246;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChatsFragment extends Fragment {
    private RecyclerView mConversationList;

    private DatabaseReference mConversationDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConversationList = mMainView.findViewById(R.id.conversation_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mConversationDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);

        mConversationDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConversationList.setHasFixedSize(true);
        mConversationList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConversationDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> firebaseConversationAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(
                Conversation.class, R.layout.users_single_layout, ConversationViewHolder.class, conversationQuery
        ) {
            @Override
            protected void populateViewHolder(ConversationViewHolder conversationViewHolder, Conversation conversation, int i) {

                final String listUserId = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(listUserId).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String data = snapshot.child("message").getValue().toString();
                        conversationViewHolder.setMessage(data, conversation.isSeen());
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

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String userName = snapshot.child("name").getValue().toString();
                        // String userThumb = snapshot.child("thumb_image").getValue().toString();
                        conversationViewHolder.setName(userName);
                        conversationViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", listUserId);
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        mConversationList.setAdapter(firebaseConversationAdapter);
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setMessage(String message, boolean isSeen){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            // is seen, bold text if the message has not been seen yet
//            if (!isSeen){
//                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
//            } else {
//                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
//            }
        }

        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
    }
}