package team4.cs246;

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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper class used to format the Messages
 * It contains a list of formatted messages
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private FirebaseAuth mAuth;
    private List<Messages> mMessageList;
    private DatabaseReference mDatabase;

    /**
     * Constructor. One object of this class is used in the ChatActivity
     * @param mMessageList a list of Messages objects
     */
    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    /**
     * Creates every message using the message_single_layout
     * @param parent a ViewGroup
     * @param viewType an integer
     * @return a new MessageViewHolder
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    /**
     * Sets the name, time, and message for each message
     * @param holder a MessageViewHolder
     * @param position an integer
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();

        Messages c = mMessageList.get(position);
        String message_type = c.getType();

        // sets the user's name for each message
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(c.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                holder.mSentByName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // sets teh date and time for each message
        Date d = new Date(c.getTime());
        holder.mSentTime.setText(d.toString());

        // sets the message according to it's type
        if (message_type.equals("text")) {
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }
        else if (message_type.equals("image")){
            Picasso.get().load(c.getMessage()).into(holder.messageImage);
            holder.messageText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Used in the ChatActivity to go to automatically show the last message at the bottom of the
     * list of messages
     * @return an integer
     */
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    /**
     * Helper class used to initialize the items from the message_single_layout.xml
     * Objects form this class are used above
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public TextView mSentByName;
        public TextView mSentTime;
        public ImageView messageImage;

        /**
         * Initialized the items from message_single_layout
         * @param itemView takes a View
         */
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text_layout);
            mSentByName = itemView.findViewById(R.id.messages_text_name);
            mSentTime = itemView.findViewById(R.id.messages_timestamp);
            messageImage = itemView.findViewById(R.id.message_image_layout);
        }
    }

}
