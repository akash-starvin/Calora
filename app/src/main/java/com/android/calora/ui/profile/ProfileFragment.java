package com.android.calora.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.calora.Constants;
import com.android.calora.firebase.FBProfileInfo;
import com.android.calora.R;
import com.android.calora.firebase.FbAccountInfo;
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

public class ProfileFragment extends Fragment {
    @BindView( R.id.profileEtName ) EditText etName;
    @BindView( R.id.profileEtEmail ) EditText etEmail;
    @BindView( R.id.profileEtAge ) EditText etAge;
    @BindView( R.id.profileEtWeight ) EditText etWeight;
    @BindView( R.id.profileEtHeight ) EditText etHeight;
    @BindView( R.id.profileEtProtein ) EditText etProteinGoal;
    @BindView( R.id.profileEtCarbs ) EditText etCarbsGoal;
    @BindView( R.id.profileEtFats ) EditText etFatsGoal;
    @BindView( R.id.profileEtGoal ) EditText etCaloriesGoal;
    @BindView( R.id.profileRgGender ) RadioGroup rgGender;
    @BindView( R.id.profileRgGoal ) RadioGroup rgGoal;
    @BindView( R.id.profileRgDietType ) RadioGroup rgDietType;
    @BindView( R.id.btnProfile ) Button btnUpdate;
    @BindView( R.id.rbMale ) RadioButton rbMale;
    @BindView( R.id.rbFemale ) RadioButton rbFemale;
    @BindView( R.id.rbOther ) RadioButton rbOther;
    @BindView( R.id.rbVeg ) RadioButton rbVeg;
    @BindView( R.id.rbNonVeg ) RadioButton rbNonveg;
    @BindView( R.id.rbBoth ) RadioButton rbBoth;
    @BindView( R.id.rbBuilding ) RadioButton rbBuilding;
    @BindView( R.id.rbLoss ) RadioButton rbLos;
    @BindView( R.id.rbFitness ) RadioButton rbFitness;

    private ProfileViewModel profileViewModel;
    private String sUserName, sUserEmail, sUserGender="",sUserDietType="", sUserFitnessGoal="";
    private float  fUserAge, fUserWeight, fUserHeight, fProtein=0, fCarbs=0,fFats=0, fBMR;
    private String fbUserName,fbUserEmail, fbUserAge, fbUserWeight, fbUserHeight, fbUserCaloriesGoal, fbUserGender, fbUserDietType, fbUserFitnessGoal, fbUserProteinGoal,fbUserCarbsGoal, fbUserFatsGoal;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRefProfileInfo = database.getReference( Constants.FB_PROFILE_INFO );
    private DatabaseReference myRefAccountInfo = database.getReference( Constants.FB_ACC_INFO );
    private Query fetchUserName, fetchUserProfile;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = ViewModelProviders.of( this ).get( ProfileViewModel.class );
        View root = inflater.inflate( R.layout.fragment_profile, container, false );

        ButterKnife.bind( this,root );
        mAuth = FirebaseAuth.getInstance();
        getSharedPreferenceData();
            fetchUserName = FirebaseDatabase.getInstance().getReference( Constants.FB_ACC_INFO ).orderByChild( Constants.FB_ACC_INFO_CHILD_EMAIL ).equalTo( sUserEmail );
            fetchUserName.addValueEventListener( fetchUserNameVLE );

            fetchUserProfile = FirebaseDatabase.getInstance().getReference( Constants.FB_PROFILE_INFO ).orderByKey().equalTo( mAuth.getUid() );
            fetchUserProfile.addValueEventListener( fetchUserProfileVLE );
        btnUpdate.setOnClickListener( view -> {
            if (getProfileData())
            {
                calculate();
                updateUserProfile();
                if (!fbUserName.equals( sUserName )) {
                    updateUserName();
                }
            }
        } );
        rgGender.setOnCheckedChangeListener( (group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbMale:
                    sUserGender = "Male";
                    break;
                case R.id.rbFemale:
                    sUserGender = "Female";
                    break;
                case R.id.rbOther:
                    sUserGender = "Other";
                    break;
            }
        } );
        rgGoal.setOnCheckedChangeListener( (group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbBuilding:
                    sUserFitnessGoal = "Muscle Building";
                    break;
                case R.id.rbLoss:
                    sUserFitnessGoal = "Fat Loss";
                    break;
                case R.id.rbFitness:
                    sUserFitnessGoal = "Fitness";
                    break;
            }
        } );
        rgDietType.setOnCheckedChangeListener( (group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbNonVeg:
                    sUserDietType = "Non-vegetarian";
                    break;
                case R.id.rbVeg:
                    sUserDietType = "Vegetarian";
                    break;
                case R.id.rbBoth:
                    sUserDietType = "Both";
                    break;
            }
        } );

        return root;
    }

    private void calculate() {
        if (sUserGender.equals( "Male" ))
            fBMR = (float) ((10 * fUserWeight) + (6.25 * fUserHeight) - (5 * fUserAge ) + 5);
        else
            fBMR = (float) ((10*fUserWeight) + (6.25 * fUserHeight) - (5 * fUserAge ) -161);
        switch (sUserFitnessGoal)
        {
            case "Muscle Building":
                fBMR *= 2.25;
                break;
            case "Fat Loss":
                fBMR *= 0.75;
                break;
            case "Fitness":
                fBMR *= 1.76;
                break;
        }
        //etCaloriesGoal.setText(  String.valueOf( fBMR ) );

        fProtein = (fBMR/4)/4;
        fCarbs = (fBMR/3)/4;
        fFats = (fBMR/3)/9;
        fProtein = Float.parseFloat(String.format("%.0f",fProtein));
        fCarbs = Float.parseFloat(String.format("%.0f",fCarbs));
        fFats = Float.parseFloat(String.format("%.0f",fFats));
        fBMR = Float.parseFloat(String.format("%.0f",fBMR));
    }

    private void updateUserName() {
        FbAccountInfo accountInfo = new FbAccountInfo( sUserName,fbUserEmail );
        myRefAccountInfo.child( Objects.requireNonNull( mAuth.getUid() ) ).setValue( accountInfo );
    }

    private void updateUserProfile() {
        FBProfileInfo fbProfileInfo = new FBProfileInfo( fUserAge, sUserGender, sUserFitnessGoal, sUserDietType,fUserWeight, fUserHeight, fBMR, fProtein,fCarbs,fFats );
        myRefProfileInfo.child( Objects.requireNonNull( mAuth.getUid() ) ).setValue( fbProfileInfo );
        Toast.makeText( getContext(), "Profile updated", Toast.LENGTH_SHORT ).show();
    }

    private ValueEventListener fetchUserNameVLE = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    fbUserName = postSnapshot.child( Constants.FB_ACC_INFO_CHILD_USER_NAME ).getValue()+"";
                    fbUserEmail = postSnapshot.child( Constants.FB_ACC_INFO_CHILD_EMAIL ).getValue()+"";
                }
                etName.setText( fbUserName );
                etEmail.setText( fbUserEmail );
                }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again"+e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener fetchUserProfileVLE = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    fbUserAge = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_AGE ).getValue()+"";
                    fbUserGender = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_GENDER ).getValue()+"";
                    fbUserWeight = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_WEIGHT ).getValue()+"";
                    fbUserHeight = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_HEIGHT ).getValue()+"";
                    fbUserFitnessGoal = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_FITNESS_GOAL ).getValue()+"";
                    fbUserDietType = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_DIET_TYPE ).getValue()+"";
                    fbUserCaloriesGoal = postSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_CALORIES_GOAL ).getValue()+"";
                    fbUserProteinGoal = postSnapshot.child( Constants.FB_PROTEIN ).getValue()+"";
                    fbUserCarbsGoal = postSnapshot.child( Constants.FB_CARBS ).getValue()+"";
                    fbUserFatsGoal = postSnapshot.child( Constants.FB_FATS ).getValue()+"";
                }
                etAge.setText( fbUserAge );
                etHeight.setText( fbUserHeight );
                etWeight.setText( fbUserWeight );
                etCaloriesGoal.setText( fbUserCaloriesGoal );
                etProteinGoal.setText( fbUserProteinGoal );
                etCarbsGoal.setText( fbUserCarbsGoal );
                etFatsGoal.setText( fbUserFatsGoal );
                setRadioGroups();
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again"+e.toString(), Toast.LENGTH_SHORT ).show();
                Log.e( "=======PROFILE", e.toString() );
                Log.e( "=======PROFILE_MESSAGE", e.getMessage() );
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setRadioGroups() {
        setRadioGroupGender();
        setRadioGroupDietType();
        setRadioGroupFitnessGoalType();
    }

    private void setRadioGroupFitnessGoalType() {
        switch (fbUserFitnessGoal)
        {
            case "Muscle Building":
                rbBuilding.setChecked( true );
                break;
            case "Fat Loss":
                rbLos.setChecked( true );
                break;
            case "Fitness":
                rbFitness.setChecked( true );
                break;
        }
    }

    private void setRadioGroupDietType() {
        switch (fbUserDietType) {
            case "Vegetarian":
                rbVeg.setChecked( true );
                break;
            case "Non-vegetarian":
                rbNonveg.setChecked( true );
                break;
            case "Both":
                rbBoth.setChecked( true );
                break;
        }
    }

    private void setRadioGroupGender() {
        switch (fbUserGender) {
            case "Male":
                rbMale.setChecked( true );
                break;
            case "Female":
                rbFemale.setChecked( true );
                break;
            case "Other":
                rbOther.setChecked( true );
                break;
        }
    }

    private boolean getProfileData() {
        return getName() && getAge() && getGender() && getWeight() && getHeight() && getDietType() && getFitnessGoal() ;
    }
    private boolean getName() {
        if(etName.getText().toString().isEmpty())
        {
            etName.setError( "Please enter name" );
            return false;
        }
        sUserName = etName.getText().toString();
        return true;
    }
    private boolean getAge() {
        if(etAge.getText().toString().isEmpty())
        {
            etAge.setError( "Please enter age" );
            return false;
        }
        fUserAge = Float.parseFloat(  etAge.getText().toString());
        return true;
    }
    private boolean getGender() {
        return !sUserGender.isEmpty();
    }
    private boolean getWeight() {
        if (etWeight.getText().toString().isEmpty())
        {
            etWeight.setError( "Please enter weight" );
            return false;
        }
        else if (Float.parseFloat( etWeight.getText().toString() ) > 400)
        {
            etWeight.setError( "Please enter a valid weight" );
            return false;
        }
        fUserWeight = Float.parseFloat(  etWeight.getText().toString());
        return true;
    }
    private boolean getHeight() {
        if (etHeight.getText().toString().isEmpty())
        {
            etHeight.setError( "Please enter height" );
            return false;
        }
        else if (Integer.parseInt( etHeight.getText().toString() ) > 250)
        {
            etHeight.setError( "Please enter a valid height" );
            return false;
        }
        fUserHeight = Float.parseFloat( etHeight.getText().toString());
        return true;
    }
    private boolean getDietType() {
        return !sUserDietType.isEmpty();
    }
    private boolean getFitnessGoal() {
        return !sUserFitnessGoal.isEmpty();
    }

    private void getSharedPreferenceData() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences( Constants.SP_LOGIN_CREDENTIALS, Context.MODE_PRIVATE);
        sUserEmail= sharedPreferences.getString(Constants.SP_EMAIL, "");
    }
}
