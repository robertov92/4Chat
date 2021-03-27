package team4.cs246;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
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
    private Compressor thumbImage;
    //Storage reference
    private StorageReference mImageStorage;
    //Progress bar
    private ProgressDialog mProgressDialog;
    /*
    Main activity for Settings page
     */
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
        mUserDatabase.keepSynced(true); // Offline capabilities
        /*
        Reads User's data from database
         */
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String thumb_image = snapshot.child("thumb_image").getValue().toString();
                mName.setText(name);
                mStatus.setText(status);

                // show default image if there is not user image
                if(!image.equals("default")){
                    Picasso.get().load(image).into(mDisplayImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        /*
        Adds functionality to Status button
         */
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent status_intent = new Intent(SettingsActivity.this,StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });
        /*
        Adds functionality to Image button
         */
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
    }
    /*
    Retrieves image that user picks and crops it
     */
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        SettingsActivity.super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==GALLERY_PICK && resultCode== RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1).setMinCropWindowSize(300, 300).start(SettingsActivity.this);
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
                //File thumb_filePath = new File(resultUri.getPath());

                String current_user_id = mCurrentUser.getUid();

//                Bitmap thumb_bitmap = new Compressor(this)
//                        .setMaxWidth(200)
//                        .setMaxHeight(200)
//                        .setQuality(75)
//                        .compressToBitmap(thumb_filePath);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                //StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + "jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            String download_url = resultUri.toString();

                            //UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            //uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
//                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
//                                    if(thumb_task.isSuccessful()){
//                                        Map update_hashMap = new HashMap();
//                                        update_hashMap.put("image", download_url);
//                                        update_hashMap.put("thumb_image", thumb_downloadUrl);
//                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if(task.isSuccessful()){
//                                                    mProgressDialog.dismiss();
//                                                    Toast.makeText(SettingsActivity.this,"Success uploading.",Toast.LENGTH_LONG).show();
//                                                }
//                                            }
//                                        });
//                                    } else {
//                                        Toast.makeText(SettingsActivity.this,"Error in uploading thumbnail.",Toast.LENGTH_LONG).show();
//                                        mProgressDialog.dismiss();
//                                    }
//                                }
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
}