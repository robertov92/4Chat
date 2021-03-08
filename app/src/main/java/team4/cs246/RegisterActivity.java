package team4.cs246;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText mDisplayName, mEmail, mPassword;
    private Button mCreateBtn;

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // authentication instance
        mAuth = FirebaseAuth.getInstance();

        // toolbar
        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");

        // back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // getting inputs
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateBtn = findViewById(R.id.reg_create_account_btn);



        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                // if inputs aren't empty
                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    // Calling function to register a new user
                    registerUser(display_name, email, password);

                    // else, show toast
                } else {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * User registration functionality
     * @param display_name name taken from EditText
     * @param email email taken from EditText
     * @param password password taken from EditText
     */
    private void registerUser(String display_name, String email, String password) {
        // This method from authentication instance does the magic!!!
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If successful, send user to the main activity
                if (task.isSuccessful()){
                    Intent mainIntent = new Intent(RegisterActivity.this,
                            MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                    // else, show error toast!
                } else {
                    Toast.makeText(RegisterActivity.this, "You can't create an " +
                            "account with the provided username, email and password. Please try " +
                            "again using different inputs!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}