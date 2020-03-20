package com.android.calora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calora.Constants;
import com.android.calora.R;
import com.android.calora.firebase.FbAccountInfo;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateAccountActivity extends AppCompatActivity {
    @BindView(R.id.btnCreateAccount) Button btnCreateAccount;
    @BindView(R.id.etCaName) EditText etName;
    @BindView(R.id.etCaEmail) EditText etEmail;
    @BindView(R.id.etCaPassword) EditText etPassword;
    @BindView(R.id.etCaConfirmPassword) EditText etConfirmPassword;

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

        Objects.requireNonNull( getSupportActionBar() ).setTitle(getString( R.string.create_account ));
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        btnCreateAccount.setOnClickListener( view -> {
            if (getUserCredentials()) {
                if(checkInternet())
                {
                    checkEmailExistQuery = FirebaseDatabase.getInstance().getReference( Constants.FB_ACC_INFO ).orderByChild( Constants.FB_ACC_INFO_CHILD_EMAIL ).equalTo( sUserEmail );
                    checkEmailExistQuery.addValueEventListener( checkEmailExistVLE );
                } else {
                    Snackbar.make( view,getString( R.string.no_internet ), Snackbar.LENGTH_LONG ).show();
                }
            }
        } );
    }
    ValueEventListener checkEmailExistVLE = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                if (dataSnapshot.hasChildren()) {
                    Toast.makeText( getApplicationContext(), getString( R.string.email_already_registered ), Toast.LENGTH_LONG ).show();
                    etEmail.setError(getString( R.string.email_already_registered  ));
                }
                else {
                    createNewUserAccount();
                    checkEmailExistQuery.removeEventListener( checkEmailExistVLE );
                }
            } catch (Exception ignored) {

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void createNewUserAccount() {
        mAuth.createUserWithEmailAndPassword(sUserEmail, sUserPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserData();
                        saveUserDataInSharedPreference();
                        Toast.makeText(getApplicationContext(), getString( R.string.account_created ), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent( getApplicationContext(),  LoginActivity.class );
                        startActivity( intent );

                    } else {
                        Toast.makeText( getApplicationContext(), getString( R.string.error_creating_account ), Toast.LENGTH_SHORT ).show();

                    }
                } );
    }

    private void saveUserDataInSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SP_LOGIN_CREDENTIALS ,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString( Constants.SP_EMAIL, sUserEmail);
        myEdit.putString( Constants.SP_PASSWORD, sUserPassword);
        myEdit.apply();
    }

    private void saveUserData() {
        FbAccountInfo fbAccountInfo = new FbAccountInfo( sUserName,sUserEmail );
        myRefAccInfo.child( Objects.requireNonNull( mAuth.getUid() ) ).setValue( fbAccountInfo );
    }
    private boolean getUserCredentials() {
        return getName() && getEmail() && getPassword();
    }
    private boolean getName() {
        if(etName.getText().toString().isEmpty() )
        {
            etName.setError( getString( R.string.enter_full_name ) );
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
            etEmail.setError( getString( R.string.enter_valid_email ) );
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
            etPassword.setError( getString( R.string.enter_password ) );
            return false;
        }
        else if(etConfirmPassword.getText().toString().isEmpty() )
        {
            etConfirmPassword.setError( getString( R.string.enter_confirm_password ) );
            return false;
        }
        else if(etPassword.getText().toString().length() < 6 )
        {
            etPassword.setError( getString( R.string.minimum_6_chars ) );
            return false;
        }
        else if(!etPassword.getText().toString().equals( etConfirmPassword.getText().toString() ) )
        {
            etConfirmPassword.setError( getString( R.string.password_doesnt_match ) );
            return false;
        }
        else {
            sUserPassword = etPassword.getText().toString();
            return true;
        }
    }
    private boolean checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService( CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
