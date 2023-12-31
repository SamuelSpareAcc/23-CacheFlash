package sg.edu.np.mad.madasgcacheflash;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;

public class Login extends AppCompatActivity {

    //String username;
    String title = "Main activity";
    private FirebaseAuth mAuth;

    public ProgressDialog loginprogress;
    String usernameprofile;
    private TextInputEditText etPassword;
    private boolean showpassword;
    /*
    private String GLOBAL_PREF = "MyPrefs";
    private String MY_USERNAME = "MyUserName";
    private String MY_PASSWORD = "MyPassword";
    SharedPreferences sharedPreferences;
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);
        TextView privacyPolicyTextView = findViewById(R.id.privacy_policy_text);
        Intent intent=getIntent();
        usernameprofile=intent.getStringExtra("Username");
        Log.v("At first","username profile"+usernameprofile);

        /*
        else
        {
            //usernameprofile=null;
        }

         */

        privacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://westwq.github.io/MADPrivacy/"));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(title, "Starting App Login Page");
        TextView newUser = findViewById(R.id.textView4);
        mAuth = FirebaseAuth.getInstance();
        EditText etUsername = findViewById(R.id.editTextText);
        etPassword = findViewById(R.id.editTextText2);
        TextView Forgetpassword=findViewById(R.id.textView7);
        ImageView showpasswordicon=findViewById(R.id.imageView11);
        newUser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(Login.this, Signup.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return false;
            }
        });
        //Button forgetpass = findViewById(R.id.button6);
        Forgetpassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }


        });
        
        Button loginButton = findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString();

                if (username.isEmpty() && password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else if (username.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(username, password);

                }
            }
        });
        showpasswordicon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(showpassword)
                        {
                            togglePassVisability();
                            showpasswordicon.setImageResource(R.drawable.hidepassword);
                            showpassword=false;
                        }
                        else {
                            togglePassVisability();
                            showpasswordicon.setImageResource(R.drawable.showpasswordicon);
                            showpassword=true;
                        }

                    }
                });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        String[] email1=email.split("@");
                        //String frontend=email1[0];
                        String standardizedUsername = email1[0];
                        Log.d("debug username profile","Name"+usernameprofile);
                        Log.d("debug username profile","Name"+standardizedUsername);
                        if(usernameprofile!=null)
                        {
                            updateUserprofile(usernameprofile);
                        } else if (mAuth.getCurrentUser().getDisplayName() == null) {
                            updateUserprofile(standardizedUsername);
                        }
                        else {
                            usernameprofile = standardizedUsername;
                            Log.v("After username profile","username"+usernameprofile);
                        }

                        Toast.makeText(Login.this, "Login successful! Welcome " + standardizedUsername, Toast.LENGTH_SHORT).show();
                        // Start the MainActivity
                        Intent intent = new Intent(Login.this, WalkThrough.class);
                        intent.putExtra("Username", standardizedUsername);
                        Log.d("Login", "Username: " + standardizedUsername);
                        startActivity(intent);
                    } else {
                        // Login failed
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // Invalid username
                            Toast.makeText(Login.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid password
                            Toast.makeText(Login.this, "Invalid password", Toast.LENGTH_SHORT).show();
                        } else {
                            // Other login error
                            Toast.makeText(Login.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailet = new EditText(this);

        // write the email using which you registered
        emailet.setHint("Email");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);

        // Click on Recover and a email will be sent to your registered email id
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailet.getText().toString().trim();
                beginRecovery(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    ProgressDialog loadingBar;

    private void beginRecovery(String email) {
        loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // calling sendPasswordResetEmail
        // open your email and write the new
        // password and then you can login
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    // if isSuccessful then done message will be shown
                    // and you can change the password
                    Toast.makeText(Login.this, "Done sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Login.this, "Error Occurred", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(Login.this, "Error Failed", Toast.LENGTH_LONG).show();
            }
        });

    }
    private void updateUserprofile(String username){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        assert user != null;
        user.updateProfile(profileUpdates)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                   if (task.isSuccessful()) {
                       Log.d(TAG, "User profile updated.");
                       Toast.makeText(Login.this, user.getEmail()+" profile updated", Toast.LENGTH_SHORT).show();
                       mAuth.signOut();
                   }
               }
        });
    }
    private void togglePassVisability() {
        if (showpassword) {
            String pass = etPassword.getText().toString();
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setText(pass);
            etPassword.setSelection(pass.length());
        } else {
            String pass = etPassword.getText().toString();
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            etPassword.setText(pass);
            etPassword.setSelection(pass.length());
        }

    }
//        private void resetPasswordWithPhoneNumber(String phoneNumber) {
//            FirebaseAuth.getInstance().signInWithPhoneNumber(phoneNumber, true)
//                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(Login.this, "Password reset code sent. Check your phone.", Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent(Login.this, VerifyPasswordResetCodeActivity.class);
//                                intent.putExtra("verificationId", task.getResult().getSignInResult().getVerificationId());
//                                startActivity(intent);
//                            } else {
//                                Toast.makeText(Login.this, "Failed to send password reset code. Make sure the phone number is correct.", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//        }


    }

/*
import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.appcompat.widget.Toolbar;

        import android.app.AlertDialog;
        import android.app.ProgressDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Bundle;
        import android.text.InputType;
        import android.text.TextUtils;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText memail;
    private EditText mpass;
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private Button login;
    TextView forgetpass;
    public ProgressDialog loginprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mtoolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mtoolbar);

        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setTitle("Login");

        forgetpass=findViewById(R.id.forgetpass);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loginprogress=new ProgressDialog(this);
        memail=(EditText)findViewById(R.id.logemail);
        mpass=(EditText)findViewById(R.id.logpass);

        // click on forget password text
        forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        login=(Button)findViewById(R.id.logbut);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=memail.getText().toString();
                String password =mpass.getText().toString();
                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
                    loginprogress.setTitle("Logging In");
                    loginprogress.setMessage("Please Wait ");
                    loginprogress.setCanceledOnTouchOutside(false);
                    loginprogress.show();
                    loginUser(email,password);
                }
            }
        });
    }

    ProgressDialog loadingBar;

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout=new LinearLayout(this);
        final EditText emailet= new EditText(this);

        // write the email using which you registered
        emailet.setText("Email");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        // Click on Recover and a email will be sent to your registered email id
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email=emailet.getText().toString().trim();
                beginRecovery(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        loadingBar=new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // calling sendPasswordResetEmail
        // open your email and write the new
        // password and then you can login
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if(task.isSuccessful())
                {
                    // if isSuccessful then done message will be shown
                    // and you can change the password
                    Toast.makeText(LoginActivity.this,"Done sent",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(LoginActivity.this,"Error Occurred",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this,"Error Failed",Toast.LENGTH_LONG).show();
            }
        });
    }
    public void loginUser(String email,String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    loginprogress.dismiss();
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                } else {
                    loginprogress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot Sign In..Please Try Again",Toast.LENGTH_LONG);
                }
            }
        });
    }
}
*/
