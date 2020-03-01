package com.example.loginapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    //view
    EditText mEmailEt,mpasswordEt;
    TextView notHaveAccountTv , mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //progress dialog

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ActionBar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //before mAuth
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mpasswordEt = findViewById(R.id.passwordEt);
        notHaveAccountTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverpassTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);

        //login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email =mEmailEt.getText().toString();
                String passw = mpasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //invalid email pattern set error
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else {
                    //valid email pattern
                    loginUser(email,passw);
                }

            }
        });
        //not have account textview cllick
        notHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        //recover pass textView click
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //handle google login btn click
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin google login process
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //init progress dialog
        pd = new ProgressDialog(this);
    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover password");

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        // view to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        /* sets tejh min  width of a editview to fit a text of n 'M' letters regardless of the actual text
        * extension and text size */
        emailEt.setMinEms(19);

        linearLayout.addView(emailEt);

        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);


            }
        });

        //buttons cancel
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismmis dialog
                dialog.dismiss();

            }
        });

        //show dialog
        builder.create().show();

    }

    private void beginRecovery(String email) {
        // show progress dialog
        pd.setMessage("Sending email .....");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //get5 and show proper error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });
    }


    private void loginUser(String email, String passw) {
        // show progress dialog
        pd.setMessage("Login .....");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // dismiss pr5ogress dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // user is login , so start login acitivity
                            startActivity( new Intent(LoginActivity.this, Dashboard.class));
                            finish();
                        } else {
                            // dismiss pr5ogress dialog
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // error , get5 and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();


                            //if user is sing in in  first time then get and show user
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                //get user email and uid from auth
                                String email = user.getEmail();
                                String uid = user.getUid();
                                //when use4r is register store user info in firebase real time database too
                                //using HashMap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put info in hashMap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", ""); // will aADD LATER( e.g. edit profile)
                                hashMap.put("phone", ""); // will aADD LATER( e.g. edit profile)
                                hashMap.put("image", ""); // will aADD LATER( e.g. edit profile)
                                //firebase database isntance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //path to store user data named "users"
                                DatabaseReference reference = database.getReference("Users");
                                //put data within hasmap in database
                                reference.child(uid).setValue(hashMap);


                            }





                            //show user email ion toast
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            //go to profile activity
                            startActivity(new Intent(LoginActivity.this, Dashboard.class));
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login Failed....", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    
    
    
}
