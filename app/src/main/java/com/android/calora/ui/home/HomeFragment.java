package com.android.calora.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.calora.Constants;
import com.android.calora.CreateMealActivity;
import com.android.calora.MealListActivity;
import com.android.calora.MealListAdapter;
import com.android.calora.MealListModel;
import com.android.calora.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment {

    @BindView( R.id.linearLayoutToday )
    LinearLayout linearLayout;
    @BindView( R.id.homeTvProtein ) TextView tvProtein;
    @BindView( R.id.homeTvCarbs ) TextView tvCarbs;
    @BindView( R.id.homeTvFats ) TextView tvFats;
    @BindView( R.id.tvHomeCalories ) TextView tvCalories;
    @BindView( R.id.homeTvProteinGoal ) TextView tvProteinGoal;
    @BindView( R.id.homeTvCarbsGoal ) TextView tvCarbsGoal;
    @BindView( R.id.homeTvFatsGoal ) TextView tvFatsGoal;
    @BindView( R.id.homeTvCaloriesGoal ) TextView tvCaloriesGoal;

    Query queryConsumedMealData, queryGoalData;
    private Float fbProtein, fbCarbs, fbFats, fbCalories,fbProteinGoal, fbCarbsGoal, fbFatsGoal, fbCaloriesGoal;
    private String date;
    private FirebaseAuth mAuth;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of( this ).get( HomeViewModel.class );
        View root = inflater.inflate( R.layout.fragment_home, container, false );
        ButterKnife.bind( this,root );
        mAuth = FirebaseAuth.getInstance();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        date = df.format(c);
        queryConsumedMealData = FirebaseDatabase.getInstance().getReference( Constants.FB_CONSUMED_MEAL ).child( mAuth.getUid() ).child( date );
        queryConsumedMealData.addValueEventListener( getConsumedMealData );

        queryGoalData = FirebaseDatabase.getInstance().getReference( Constants.FB_PROFILE_INFO ).child( mAuth.getUid() );
        queryGoalData.addValueEventListener( getGoalData );

        linearLayout.setOnClickListener( v -> {
            Intent intent= new Intent( getContext(), MealListActivity.class );
            startActivity( intent );
        } );

        return root;
    }
    private ValueEventListener getConsumedMealData = new ValueEventListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                fbProtein = Float.parseFloat(  dataSnapshot.child( Constants.FB_PROTEIN ).getValue() + "");
                fbFats = Float.parseFloat( dataSnapshot.child( Constants.FB_CARBS ).getValue() + "");
                fbCarbs = Float.parseFloat( dataSnapshot.child( Constants.FB_FATS ).getValue() + "");
                fbCalories = Float.parseFloat( dataSnapshot.child( Constants.FB_CALORIES ).getValue() + "");

                tvProtein.setText( String.format("%.0f",fbProtein));
                tvCarbs.setText( String.format("%.0f",fbCarbs));
                tvFats.setText( String.format("%.0f",fbFats));
                tvCalories.setText( String.format("%.0f",fbCalories));
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again"+e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private ValueEventListener getGoalData = new ValueEventListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                fbProteinGoal = Float.parseFloat(  dataSnapshot.child( Constants.FB_PROTEIN ).getValue() + "");
                fbFatsGoal = Float.parseFloat( dataSnapshot.child( Constants.FB_CARBS ).getValue() + "");
                fbCarbsGoal = Float.parseFloat( dataSnapshot.child( Constants.FB_FATS ).getValue() + "");
                fbCaloriesGoal = Float.parseFloat( dataSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_CALORIES_GOAL ).getValue() + "");

                tvProteinGoal.setText( String.format("%.0f",fbProteinGoal));
                tvCarbsGoal.setText( String.format("%.0f",fbCarbsGoal));
                tvFatsGoal.setText( String.format("%.0f",fbFatsGoal));
                tvCaloriesGoal.setText( String.format("%.0f",fbCaloriesGoal));
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again"+e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
