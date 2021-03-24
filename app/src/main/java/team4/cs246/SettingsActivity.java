package team4.cs246;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private Toolbar mToolbar;


    //android layout

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;

    private static final int GALLERY_PICK =1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mDisplayImage = (CircleImageView)findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);

        mStatusBtn = (Button)findViewById(R.id.setting_status_btn);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String thumb_image = snapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mStatusBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                String status_value = mStatus.getText().toString();



                Intent status_intent = new Intent(SettingsActivity.this,StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });


        mImageBtn.setOnClickListener(new View.OnClickListener() {






            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);


                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

    }
}