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

public class LoginActivity extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginBtn;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // toolbar
        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");

        //back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get inputs
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mLoginBtn = findViewById(R.id.login_btn);

        // Login button calls loginUser function
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get inputs from EditText's
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                // if the inputs aren't empty, call loginUser
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    loginUser(email, password);

                    // else, show toast
                } else {
                    Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Login user
     * @param email from TextView
     * @param password from TextView
     */
    private void loginUser(String email, String password) {
        // This method from authentication instance does the magic!!!
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If task is successful, send user to main
                if (task.isSuccessful()){

                    // Use intent to send user to main
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);

                    // This is so the user can't go back to this activity from MainActivity
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // send the user to MainActivity
                    startActivity(mainIntent);
                    finish();

                    //else, show error toast
                } else {
                    Toast.makeText(LoginActivity.this, "Please check email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
