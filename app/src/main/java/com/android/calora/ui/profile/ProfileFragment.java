package com.android.calora.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.android.calora.FBProfileInfo;
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
    private String sUserName, sUserEmail, sUserAge, sUserWeight, sUserHeight, sUserCaloriesGoal, sUserGender="",sUserDietType="", sUserFitnessGoal="";
    private String fbUserName,fbUserEmail, fbUserAge, fbUserWeight, fbUserHeight, fbUserCaloriesGoal, fbUserGender, fbUserDietType, fbUserFitnessGoal;
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
        Toast.makeText( getContext(), mAuth.getUid()+"", Toast.LENGTH_SHORT ).show();
        getSharedPreferenceData();
        //if (!mAuth.getUid().isEmpty()) {
            fetchUserName = FirebaseDatabase.getInstance().getReference( Constants.FB_ACC_INFO ).orderByChild( Constants.FB_ACC_INFO_CHILD_EMAIL ).equalTo( sUserEmail );
            fetchUserName.addValueEventListener( fetchUserNameVLE );

            fetchUserProfile = FirebaseDatabase.getInstance().getReference( Constants.FB_PROFILE_INFO ).orderByKey().equalTo( mAuth.getUid() );
            fetchUserProfile.addValueEventListener( fetchUserProfileVLE );
        btnUpdate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getProfileData())
                {
                    updateUserProfile();
                    if(!fbUserName.equals( sUserName ))
                    {
                       updateUserName();
                    }
                }
            }
        } );
        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            }
        });
        rgGoal.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            }
        });
        rgDietType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            }
        });

        return root;
    }

    private void updateUserName() {
        FbAccountInfo accountInfo = new FbAccountInfo( sUserName,fbUserEmail );
        myRefAccountInfo.child( Objects.requireNonNull( mAuth.getUid() ) ).setValue( accountInfo );
    }

    private void updateUserProfile() {
        FBProfileInfo fbProfileInfo = new FBProfileInfo( sUserAge, sUserGender, sUserWeight, sUserHeight, sUserFitnessGoal, sUserDietType, sUserCaloriesGoal );
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
                Toast.makeText( getContext(), "Error, please try again", Toast.LENGTH_SHORT ).show();
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
                }
                etAge.setText( fbUserAge );
                etHeight.setText( fbUserHeight );
                etWeight.setText( fbUserWeight );
                etCaloriesGoal.setText( fbUserCaloriesGoal );
                setRadioGroups();
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again", Toast.LENGTH_SHORT ).show();
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
        return getName() && getAge() && getGender() && getWeight() && getHeight() && getDietType() && getFitnessGoal() && getCalories();
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
        sUserAge = etAge.getText().toString();
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
        else if (Integer.parseInt( etWeight.getText().toString() ) > 400)
        {
            etWeight.setError( "Please enter a valid weight" );
            return false;
        }
        sUserWeight = etWeight.getText().toString();
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
        sUserHeight = etHeight.getText().toString();
        return true;
    }
    private boolean getDietType() {
        return !sUserDietType.isEmpty();
    }
    private boolean getFitnessGoal() {
        return !sUserFitnessGoal.isEmpty();
    }
    private boolean getCalories() {
        if (etCaloriesGoal.getText().toString().isEmpty())
        {
            etCaloriesGoal.setError( "Please enter calories goal per day" );
            return false;
        }
        else if (Integer.parseInt( etCaloriesGoal.getText().toString() ) > 10000)
        {
            etCaloriesGoal.setError( "Please enter a valid calories goal" );
            return false;
        }
        sUserCaloriesGoal = etCaloriesGoal.getText().toString();
        return true;
    }
    private void getSharedPreferenceData() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences( Constants.SP_LOGIN_CREDENTIALS, Context.MODE_PRIVATE);
        sUserEmail= sharedPreferences.getString(Constants.SP_EMAIL, "");
    }
}
