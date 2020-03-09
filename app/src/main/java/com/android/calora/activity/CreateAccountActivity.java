package com.android.calora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calora.Constants;
import com.android.calora.R;
import com.android.calora.firebase.FbAccountInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateAccountActivity extends AppCompatActivity {
    @BindView(R.id.btnCreateAccount) Button btnCreateAccount;
    @BindView(R.id.etCaName) EditText etName;
    @BindView(R.id.etCaEmail) EditText etEmail;
    @BindView(R.id.etCaPassword) EditText etPassword;
    @BindView(R.id.etCaConfirmPassword) EditText etConfirmPassword;
    @BindView(R.id.tvCaHaveAccount) TextView tvHaveAccount;

    private String sUserName, sUserEmail, sUserPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRefAccInfo = database.getReference( Constants.FB_ACC_INFO );
    private Query checkEmailExistQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_create_account );
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("Create Account");
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        btnCreateAccount.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getUserCredentials()) {
                    if(checkInternet())
                    {
                        checkEmailExistQuery = FirebaseDatabase.getInstance().getReference( Constants.FB_ACC_INFO ).orderByChild( Constants.FB_ACC_INFO_CHILD_EMAIL ).equalTo( sUserEmail );
                        checkEmailExistQuery.addValueEventListener( checkEmailExistVLE );
                    } else {
                        Snackbar.make( view,"No internet connection", Snackbar.LENGTH_LONG ).show();
                    }
                }
            }
        } );
        tvHaveAccount.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getApplicationContext(),  LoginActivity.class );
                startActivity( intent );
            }
        } );

    }
    ValueEventListener checkEmailExistVLE = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {
                if (dataSnapshot.hasChildren()) {
                    Toast.makeText( getApplicationContext(), "Email address is already registered", Toast.LENGTH_LONG ).show();
                    etEmail.setError( "Email address is already registered, login using this email" );
                }
                else {
                    createNewUserAccount();
                    checkEmailExistQuery.removeEventListener( checkEmailExistVLE );
                }
            } catch (Exception e) {
                Toast.makeText( getApplicationContext(), "Error, please try again", Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void createNewUserAccount() {
        mAuth.createUserWithEmailAndPassword(sUserEmail, sUserPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserData();
                            saveUserDataInSharedPrefernce();
                            Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent( getApplicationContext(),  LoginActivity.class );
                            startActivity( intent );

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText( getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT ).show();

                        }
                    }
                });
    }

    private void saveUserDataInSharedPrefernce() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SP_LOGIN_CREDENTIALS ,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString( Constants.SP_EMAIL, sUserEmail);
        myEdit.putString( Constants.SP_PASSWORD, sUserPassword);
        myEdit.commit();
    }

    private void saveUserData() {
        FbAccountInfo fbAccountInfo = new FbAccountInfo( sUserName,sUserEmail );
        myRefAccInfo.child( mAuth.getUid() ).setValue( fbAccountInfo );
    }
    private boolean getUserCredentials() {
        return getName() && getEmail() && getPassword();
    }
    private boolean getName() {
        if(etName.getText().toString().isEmpty() )
        {
            etName.setError( "Please enter full name" );
            return false;
        }
        else {
            sUserName = etName.getText().toString();
            return true;
        }
    }
    private boolean getEmail() {
        if(etEmail.getText().toString().isEmpty() ||  !etEmail.getText().toString().contains( "@" ) || !etEmail.getText().toString().contains( ".com" ))
        {
            etEmail.setError( "Please enter a vaild email address" );
            return false;
        }
        else {
            sUserEmail = etEmail.getText().toString().toLowerCase();
            return true;
        }
    }
    private boolean getPassword() {
        if(etPassword.getText().toString().isEmpty() )
        {
            etPassword.setError( "Please enter password" );
            return false;
        }
        else if(etConfirmPassword.getText().toString().isEmpty() )
        {
            etConfirmPassword.setError( "Please enter confirm password" );
            return false;
        }
        else if(etPassword.getText().toString().length() < 6 )
        {
            etPassword.setError( "Minimum 6 characters" );
            return false;
        }
        else if(!etPassword.getText().toString().equals( etConfirmPassword.getText().toString() ) )
        {
            etConfirmPassword.setError( "Please enter password doesn't match" );
            return false;
        }
        else {
            sUserPassword = etPassword.getText().toString();
            return true;
        }
    }
    private boolean checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the VisualizerActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }


}
