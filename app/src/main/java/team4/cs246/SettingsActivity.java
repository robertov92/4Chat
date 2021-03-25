package team4.cs246;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

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

    //Storage reference
    private StorageReference mImageStorage;


    //Progress bar
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mDisplayImage = (CircleImageView)findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);

        mStatusBtn = (Button)findViewById(R.id.setting_status_btn);
        mImageBtn = findViewById(R.id.settings_image_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();

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

                Picasso.get().load(image).into(mDisplayImage);




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

                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/





            }


            protected void onActivityResult(int requestCode,int resultCode,Intent data){
                SettingsActivity.super.onActivityResult(requestCode,resultCode,data);

                if(requestCode==GALLERY_PICK && resultCode== RESULT_OK) {

                    Uri imageUri = data.getData();



                    CropImage.activity(imageUri)
                            .setAspectRatio(1,1).start(SettingsActivity.this);


                    //Toast.makeText(SettingsActivity.this,imageUrl,Toast.LENGTH_LONG).show();


                }

                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {

                        mProgressDialog=new ProgressDialog(SettingsActivity.this);
                        mProgressDialog.setTitle("Uploading image...");
                        mProgressDialog.setMessage("Please wait while we upload and process the image.");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();

                        Uri resultUri = result.getUri();

                        String current_user_id = mCurrentUser.getUid();

                        StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id+".jpg");

                        filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if(task.isSuccessful()){
                                    String download_url = task.getResult().getStorage().getDownloadUrl().toString();

                                    mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Success uploading.",Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });


                                    //Toast.makeText(SettingsActivity.this,"Working",Toast.LENGTH_LONG).show();

                                } else{
                                    Toast.makeText(SettingsActivity.this,"Error in uploading.",Toast.LENGTH_LONG).show();
                                    mProgressDialog.dismiss();
                                }


                            }
                        });


                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                    }
                }





            }

            public String random() {
                Random generator = new Random();
                StringBuilder randomStringBuilder = new StringBuilder();
                int randomLength = generator.nextInt(10);
                char tempChar;
                for (int i = 0; i < randomLength; i++){
                    tempChar = (char) (generator.nextInt(96) + 32);
                    randomStringBuilder.append(tempChar);
                }
                return randomStringBuilder.toString();
            }


        });







    }
}