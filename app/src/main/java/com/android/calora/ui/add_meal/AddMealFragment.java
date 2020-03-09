package com.android.calora.ui.add_meal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.calora.Constants;
import com.android.calora.FbAddMeal;
import com.android.calora.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddMealFragment extends Fragment {
    @BindView( R.id.addMealspinner) Spinner spinnerUnitMeasure;
    @BindView( R.id.addMealEtName ) EditText etName;
    @BindView( R.id.addMealProtein ) EditText etProtein;
    @BindView( R.id.addMealCarbs ) EditText etCarbs;
    @BindView( R.id.addMealFats ) EditText etFats;
    @BindView( R.id.addMealCalories ) EditText etCalories;
    @BindView( R.id.btnAddMeal ) Button btnAddMeal;

    private String sName, sUnit;
    private int iProtien, iCarbs, iFats, iCalories;
    private AddMealViewModel slideshowViewModel;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference refAddMeal = database.getReference( Constants.FB_MEAL_LIST );

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel = ViewModelProviders.of( this ).get( AddMealViewModel.class );
        View root = inflater.inflate( R.layout.fragment_add_meal, container, false );
        ButterKnife.bind( this,root );
        setProductSpinner();

        btnAddMeal.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getMealData())
                {
                    if (checkInternet())
                    {
                        saveMeal();
                        clearFields();
                    }
                }
            }
        } );

        return root;
    }

    private void clearFields() {
        etName.setText( "" );
        etProtein.setText( "" );
        etCarbs.setText( "" );
        etFats.setText( "" );
        etCalories.setText( "" );
        spinnerUnitMeasure.setSelection( 0 );
    }

    private void saveMeal() {
        FbAddMeal fbAddMeal = new FbAddMeal( sName, sUnit, iProtien,iCarbs, iFats,iCalories );
        refAddMeal.push().setValue( fbAddMeal );
        Toast.makeText( getActivity(), "Saved", Toast.LENGTH_SHORT ).show();
    }

    private boolean checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull( getActivity() ).getSystemService( Context.CONNECTIVITY_SERVICE );
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private boolean getMealData() {
        return getName() && getUnit() && getProtein() && getCarbs() && getFats() && getCalories();
    }

    private boolean getUnit() {
        if (spinnerUnitMeasure.getSelectedItem().equals( "Select unit of measure" ) ) {
            Toast.makeText( getActivity(), "Please select unit of measure for this food item", Toast.LENGTH_SHORT ).show();
            return false;
        }
        sUnit = spinnerUnitMeasure.getSelectedItem().toString();
        return true;
    }

    private boolean getName() {
        if(etName.getText().toString().isEmpty())
        {
            etName.setError( "Enter food item name" );
            return false;
        }
        sName = etName.getText().toString();
        return true;
    }

    private boolean getProtein() {
        if(etProtein.getText().toString().isEmpty())
        {
            etProtein.setError( "Enter protein" );
            return false;
        }
        iProtien = Integer.parseInt(  etProtein.getText().toString());
        return true;
    }

    private boolean getCarbs() {
        if(etCarbs.getText().toString().isEmpty())
        {
            etCarbs.setError( "Enter carbs" );
            return false;
        }
        iCarbs = Integer.parseInt(etCarbs.getText().toString());
        return true;
    }

    private boolean getFats() {
        if(etFats.getText().toString().isEmpty())
        {
            etFats.setError( "Enter fats" );
            return false;
        }
        iFats = Integer.parseInt(etFats.getText().toString());
        return true;
    }

    private boolean getCalories() {
        if(etCalories.getText().toString().isEmpty())
        {
            etCalories.setError( "Enter calories" );
            return false;
        }
        iCalories =Integer.parseInt( etCalories.getText().toString());
        return true;
    }

    private void setProductSpinner() {
        String [] values = {"Select unit of measure","g","ml"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>( Objects.requireNonNull( this.getActivity() ), android.R.layout.simple_spinner_item, values );
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerUnitMeasure.setAdapter(adapter);
    }
}
