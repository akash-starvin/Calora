package com.android.calora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calora.Constants;
import com.android.calora.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView( R.id.etLogEmail ) EditText etEmail;
    @BindView( R.id.etLogPassword ) EditText etPassword;
    @BindView( R.id.btnLogin ) Button btnLogin;
    @BindView( R.id.btnNewUser ) Button btnNewUser;
    @BindView( R.id.tvForgotPassword ) TextView tvForgot;
    private String sUserEmail, sUserPassword;
    private Query checkEmailExistQuery;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );
        ButterKnife.bind( this );
        Objects.requireNonNull( getSupportActionBar() ).hide();
        FirebaseAuth.getInstance().signOut();
        mAuth = FirebaseAuth.getInstance();

        getSharedPreferenceData();
        btnLogin.setEnabled( true );
        btnLogin.setOnClickListener( view -> {
            if (getUserData())
            {
                if(checkInternet())
                {
                    btnLogin.setEnabled( false );
                    checkEmailExistQuery = FirebaseDatabase.getInstance().getReference( Constants.FB_ACC_INFO ).orderByChild( Constants.FB_ACC_INFO_CHILD_EMAIL ).equalTo( sUserEmail );
                    checkEmailExistQuery.addValueEventListener( checkEmailExistVLE );
                }
                else
                    Snackbar.make( view,getString( R.string.no_internet ), Snackbar.LENGTH_LONG ).show();
            }
        } );
        btnNewUser.setOnClickListener( view -> {
            Intent intent = new Intent( getApplicationContext(), CreateAccountActivity.class );
            startActivity( intent );
        } );
        tvForgot.setOnClickListener( view -> {
            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            final EditText input = new EditText(LoginActivity.this);
            input.setInputType( InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
            input.setHint(getString( R.string.email_id));
            FrameLayout container = new FrameLayout(LoginActivity.this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dp_19);
            params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dp_19);
            input.setLayoutParams(params);
            container.addView(input);
            builder.setTitle(getString( R.string.reset_password));
            builder.setMessage(getString( R.string.email_will_be_sent));
            builder.setView(container);
            builder.setPositiveButton(getString( R.string.ok), (dialog, which) -> {
                if (input.getText().toString().isEmpty()) {
                    Toast.makeText( getApplicationContext(), getString( R.string.enter_registred_email ), Toast.LENGTH_LONG ).show();
                }
                else {
                    String email = input.getText().toString();
                    FirebaseAuth.getInstance().sendPasswordResetEmail( email ).addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText( getApplicationContext(), getString( R.string.reset_email_sent), Toast.LENGTH_LONG ).show();
                        } else
                            Toast.makeText( getApplicationContext(), getString( R.string.something_went_wrong), Toast.LENGTH_LONG ).show();
                    } );
                }
            } );
            builder.setNegativeButton(getString( R.string.cancel), (dialog, which) -> dialog.cancel() );
            builder.show();
        } );

    }

    final ValueEventListener checkEmailExistVLE = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                if (!dataSnapshot.hasChildren()) {
                    etEmail.setError( getString( R.string.email_isnt_registered ));
                    btnLogin.setEnabled( true );
                } else {
                    signInUser();
                }
            } catch (Exception ignored) {

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void signInUser() {
        mAuth.signInWithEmailAndPassword(sUserEmail,sUserPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        btnLogin.setEnabled( true );
                        saveUserDataInSharedPreference();
                    } else {
                        Toast.makeText( getApplicationContext(), getString( R.string.error_loging_in ), Toast.LENGTH_SHORT ).show();
                    }
                } );
    }

    private boolean getUserData() {
        return getEmail() && getPassword();
    }

    private boolean getEmail() {
        if(etEmail.getText().toString().isEmpty())
        {
            etEmail.setError( getString( R.string.enter_valid_email) );
            return false;
        }
        else {
            sUserEmail = etEmail.getText().toString().toLowerCase();
            return true;
        }
    }

    private boolean getPassword() {
        if(etPassword.getText().toString().isEmpty())
        {
            etPassword.setError( getString( R.string.enter_password) );
            return false;
        }
        else {
            sUserPassword = etPassword.getText().toString();
            return true;
        }
    }
    private void getSharedPreferenceData() {
        SharedPreferences sharedPreferences = getSharedPreferences( Constants.SP_LOGIN_CREDENTIALS, MODE_PRIVATE);
        sUserEmail= sharedPreferences.getString(Constants.SP_EMAIL, "");
        sUserPassword = sharedPreferences.getString(Constants.SP_PASSWORD, "");
        etEmail.setText( sUserEmail );
        etPassword.setText( sUserPassword );
    }
    private void saveUserDataInSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SP_LOGIN_CREDENTIALS ,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString( Constants.SP_EMAIL, sUserEmail);
        myEdit.putString( Constants.SP_PASSWORD, sUserPassword);
        myEdit.apply();
        Intent intent =  new Intent( getApplicationContext(), HomeActivity.class );
        startActivity( intent );
        Toast.makeText( LoginActivity.this, getString( R.string.logged_in), Toast.LENGTH_SHORT ).show();
    }
    private boolean checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService( CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
