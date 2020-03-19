package com.android.calora.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

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
        navUserEmail.setText( sUserEmail );
        mAppBarConfiguration = new AppBarConfiguration.Builder( R.id.nav_home, R.id.nav_profile, R.id.nav_add_meal, R.id.nav_how_to_use ).setDrawerLayout( drawer ).build();
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );
        navigationView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        return NavigationUI.navigateUp( navController, mAppBarConfiguration ) || super.onSupportNavigateUp();
    }
}
