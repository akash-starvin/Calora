package com.android.calora.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.calora.Constants;
import com.android.calora.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class HomeActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    private String sUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        SharedPreferences sharedPreferences = getSharedPreferences( Constants.SP_LOGIN_CREDENTIALS, MODE_PRIVATE);
        sUserEmail = sharedPreferences.getString(Constants.SP_EMAIL, "");

        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        NavigationView navigationView = findViewById( R.id.nav_view );
        View headerView = navigationView.getHeaderView( 0 );
        TextView navUserEmail = headerView.findViewById( R.id.navEmail );
        Button btnLogout = headerView.findViewById( R.id.navBtnLogout );
        Button btnSettings = headerView.findViewById( R.id.navBtnSettings );
        navUserEmail.setText( sUserEmail );
        mAppBarConfiguration = new AppBarConfiguration.Builder( R.id.nav_home, R.id.nav_profile, R.id.nav_add_meal, R.id.nav_how_to_use ).setDrawerLayout( drawer ).build();
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );
        navigationView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        
        btnLogout.setOnClickListener( view -> {
            FirebaseAuth.getInstance().signOut();
            removeSharedPreferenceValues();
            Toast.makeText( this, getString( R.string.logged_out ), Toast.LENGTH_SHORT ).show();
            Intent intent =  new Intent( getApplicationContext(), LoginActivity.class );
            startActivity( intent );
        } );
        btnSettings.setOnClickListener( view -> {
            Intent intent = new Intent( getApplicationContext(), SettingsActivity.class );
            startActivity( intent );

        } );
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        return NavigationUI.navigateUp( navController, mAppBarConfiguration ) || super.onSupportNavigateUp();
    }
    private void removeSharedPreferenceValues() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SP_LOGIN_CREDENTIALS ,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString( Constants.SP_EMAIL, "");
        myEdit.putString( Constants.SP_PASSWORD, "");
        myEdit.apply();
    }
}
