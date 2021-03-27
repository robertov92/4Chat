package team4.cs246;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private FirebaseAuth mAuth;
    private List<Messages> mMessageList;
    private DatabaseReference mDatabaseRef;
   // private CircleImageView mDisplayImage;

    //mDisplayImage = (CircleImageView)findViewById(R.id.settings_image);



    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        //String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        // is it image, text, or video?
        String message_type = c.getType();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumbnail").getValue().toString();

                holder.messageText.setText(name);

                //Picasso.get().load(image)
                //       .placeholder(R.drawable.picture).into(holder.profileImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // if the current user is the one who sent the message...
        /*if (from_user.equals(current_user_id)){
            holder.messageText.setBackgroundColor(Color.rgb(227,227,227));
            holder.messageText.setTextColor(Color.BLACK);
        }*/

        //holder.messageText.setText(c.getMessage());

        // add when ready for message send
        if (message_type.equals("text")) {
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }
        else {
            holder.messageText.setVisibility(View.INVISIBLE);

            // hide message text
            //Picasso.get(holder.profileImage.getContext()).load(c.getMessage())
            //        .placeholder(R.drawable.default_avatar.into(holder.messageImage));

        }
    }

    @Override
    public int getItemCount() {

        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text_layout);
            messageImage = (ImageView) itemView.findViewById(R.id.message_image_layout);
        }
    }

}
