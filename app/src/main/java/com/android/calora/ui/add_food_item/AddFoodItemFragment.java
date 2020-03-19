package com.android.calora.ui.add_food_item;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.calora.Constants;
import com.android.calora.firebase.FbAddFoodItem;
import com.android.calora.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddFoodItemFragment extends Fragment {
    @BindView( R.id.addMealSpinUnit) Spinner spinnerUnit;
    @BindView( R.id.addMealEtName ) EditText etName;
    @BindView( R.id.addMealProtein ) EditText etProtein;
    @BindView( R.id.addMealCarbs ) EditText etCarbs;
    @BindView( R.id.addMealFats ) EditText etFats;
    @BindView( R.id.addMealCalories ) EditText etCalories;
    @BindView( R.id.btnAddMeal ) Button btnAddMeal;

    private String sName, sUnit;
    private Double dProtien=0.0, dCarbs=0.0, dFats=0.0, dCalories=0.0;
    private AddFoodItemViewModel slideshowViewModel;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference refAddFoodItem = database.getReference( Constants.FB_USER_FOOD_ITEM_LIST );
    private String [] arrayValues = {"Select unit of measure","30g","100g","100ml","1pc"};
    private String [] arrayMeasure = {"30","100","100","1"};
    private String [] arrayUnits = {"g","g","ml","pc"};
    private int spinnerPosition ;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel = ViewModelProviders.of( this ).get( AddFoodItemViewModel.class );
        View root = inflater.inflate( R.layout.fragment_add_food_item, container, false );
        ButterKnife.bind( this,root );
        setSpinner();
        mAuth = FirebaseAuth.getInstance();

        btnAddMeal.setOnClickListener( view -> {
            if(getMealData())
            {
                if (checkInternet())
                {
                    saveMeal();
                    clearFields();
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
        spinnerUnit.setSelection( 0 );
    }

    private void saveMeal() {
        String unit = arrayUnits[spinnerPosition];
        Double measure = Double.parseDouble(  arrayMeasure[spinnerPosition]);
        dProtien = (double) Math.round( dProtien * 100 )/100;
        dCarbs = (double) Math.round( dCarbs * 100 )/100;
        dFats = (double) Math.round( dFats * 100 )/100;
        dCalories = (double) Math.round( dCalories * 100 )/100;

        FbAddFoodItem fbAddFoodItem = new FbAddFoodItem( sName, unit , dProtien,dCarbs, dFats,dCalories, measure);
        refAddFoodItem.child( mAuth.getUid() ).push().setValue( fbAddFoodItem );
        Toast.makeText( getActivity(), getString( R.string.added ), Toast.LENGTH_SHORT ).show();
    }

    private boolean checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull( getActivity() ).getSystemService( Context.CONNECTIVITY_SERVICE );
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean getMealData() {
        return getName() && getUnit() && getProtein() && getCarbs() && getFats() && getCalories();
    }

    private boolean getUnit() {
        if (spinnerUnit.getSelectedItem().equals( "Select unit of measure" ) ) {
            Toast.makeText( getActivity(), getString( R.string.select_measure_for_this_food_item ), Toast.LENGTH_SHORT ).show();
            return false;
        }
        spinnerPosition = spinnerUnit.getSelectedItemPosition() - 1;
        sUnit = spinnerUnit.getSelectedItem().toString();
        Toast.makeText( getActivity(), spinnerPosition, Toast.LENGTH_SHORT ).show();

        return true;
    }

    private boolean getName() {
        if(etName.getText().toString().isEmpty())
        {
            etName.setError( getString( R.string.enter_food_item) );
            return false;
        }
        sName = etName.getText().toString();
        return true;
    }

    private boolean getProtein() {
        if(etProtein.getText().toString().isEmpty())
        {
            etProtein.setError( getString( R.string.enter_protein) );
            return false;
        }
        dProtien = Double.parseDouble( etProtein.getText().toString() );
        return true;
    }

    private boolean getCarbs() {
        if(etCarbs.getText().toString().isEmpty())
        {
            etCarbs.setError( getString( R.string.enter_carbs) );
            return false;
        }
        dCarbs = Double.parseDouble(etCarbs.getText().toString());
        return true;
    }

    private boolean getFats() {
        if(etFats.getText().toString().isEmpty())
        {
            etFats.setError(getString( R.string.enter_fats)  );
            return false;
        }
        dFats = Double.parseDouble(etFats.getText().toString());
        return true;
    }

    private boolean getCalories() {
        if(etCalories.getText().toString().isEmpty())
        {
            etCalories.setError(getString( R.string.enter_calories)  );
            return false;
        }
        dCalories = Double.parseDouble( etCalories.getText().toString());
        return true;
    }

    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>( Objects.requireNonNull( this.getActivity() ), android.R.layout.simple_spinner_item, arrayValues );
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerUnit.setAdapter(adapter);
    }
}
