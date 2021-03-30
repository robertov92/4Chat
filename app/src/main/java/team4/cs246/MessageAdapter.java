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

import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private FirebaseAuth mAuth;
    private List<Messages> mMessageList;
    private DatabaseReference mDatabase;


    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        Messages c = mMessageList.get(position);
        String message_type = c.getType();

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
        Date d = new Date(c.getTime());
        holder.mSentTime.setText(d.toString());


        if (message_type.equals("text")) {
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }
        else if (message_type.equals("image")){
            Picasso.get().load(c.getMessage()).into(holder.messageImage);
            holder.messageText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public TextView mSentByName;
        public TextView mSentTime;
        public ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text_layout);
            mSentByName = itemView.findViewById(R.id.messages_text_name);
            mSentTime = itemView.findViewById(R.id.messages_timestamp);
            messageImage = itemView.findViewById(R.id.message_image_layout);
        }
    }

}
