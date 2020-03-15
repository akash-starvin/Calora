package com.android.calora.ui.home;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.calora.Constants;
import com.android.calora.DetailActivity;
import com.android.calora.DetailedWidget;
import com.android.calora.HomeAdapter;
import com.android.calora.HomeModel;
import com.android.calora.MealListActivity;
import com.android.calora.R;
import com.android.calora.RecyclerTouchListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    @BindView( R.id.layoutToday )
    ConstraintLayout layout;
    @BindView( R.id.homeTvProtein ) TextView tvProtein;
    @BindView( R.id.homeTvCarbs ) TextView tvCarbs;
    @BindView( R.id.homeTvFats ) TextView tvFats;
    @BindView( R.id.tvHomeCalories ) TextView tvCalories;
    @BindView( R.id.homeTvProteinGoal ) TextView tvProteinGoal;
    @BindView( R.id.homeTvCarbsGoal ) TextView tvCarbsGoal;
    @BindView( R.id.homeTvFatsGoal ) TextView tvFatsGoal;
    @BindView( R.id.homeTvCaloriesGoal ) TextView tvCaloriesGoal;
    @BindView( R.id.homeProgressBarProtein ) ProgressBar progressBarProtein;
    @BindView( R.id.homeProgressBarCarbs ) ProgressBar progressBarCarbs;
    @BindView( R.id.homeProgressBarFats ) ProgressBar progressBarFats;
    @BindView( R.id.homeProgressBarCalories ) ProgressBar progressBarCalories;
    @BindView( R.id.homeRecycleView ) RecyclerView recyclerView;
    Query queryConsumedMealData, queryGoalData, queryGetAllConsumedMeal;
    private Float fbProtein, fbCarbs, fbFats, fbCalories,fbProteinGoal, fbCarbsGoal, fbFatsGoal, fbCaloriesGoal;
    private Float fbProteinList, fbCarbsList, fbFatsList, fbCaloriesList;
    private Boolean fbBreakfast, fbSnack1, fbLunch, fbSnack2,fbSnack3,fbDinner;
    //private float fbProteinList=0, fbCarbsList=0, fbFatsList=0, fbCaloriesList;
    //private Boolean fbBreakfast = false, fbSnack1 = false, fbLunch = false, fbSnack2 = false,fbSnack3 = false,fbDinner =false;
    private HomeAdapter homeAdapter;
    private HomeModel homeModel;
    private ArrayList<HomeModel> myList = new ArrayList<>(  );
    private String date, fbDate;
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

        queryGetAllConsumedMeal = FirebaseDatabase.getInstance().getReference( Constants.FB_CONSUMED_MEAL ).child( mAuth.getUid() );
        queryGetAllConsumedMeal.addValueEventListener( getAllConsumedMealData );




        layout.setOnClickListener( v -> {
            Intent intent= new Intent( getContext(), MealListActivity.class );
            startActivity( intent );
        } );

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                homeModel = myList.get( position );
                Intent intent = new Intent( getContext(), DetailActivity.class );
                intent.putExtra( "data", (Serializable) homeModel );
                startActivity( intent );
                Log.e( "========date", homeModel.getDate() );
                Log.e( "========Pro", homeModel.getProtein()+"" );
                Log.e( "========Cal", homeModel.getCalories()+"" );
                Log.e( "========BF", homeModel.getBreakfast()+"" );
                Log.e( "========Din", homeModel.getDinner()+"" );


            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        return root;
    }
    private ValueEventListener getAllConsumedMealData = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    fbDate = String.valueOf( postSnapshot.getKey() );
                    if(fbDate.endsWith( date.substring( 3 ) )) {
                        fbProteinList = Float.parseFloat( postSnapshot.child( Constants.FB_PROTEIN ).getValue() + "" );
                        fbCarbsList = Float.parseFloat( postSnapshot.child( Constants.FB_CARBS ).getValue() + "" );
                        fbFatsList = Float.parseFloat( postSnapshot.child( Constants.FB_FATS ).getValue() + "" );
                        fbCaloriesList = Float.parseFloat( postSnapshot.child( Constants.FB_CALORIES ).getValue() + "" );

                        fbBreakfast = Boolean.parseBoolean( postSnapshot.child( Constants.FB_BREAKFAST ).getValue() + "" );
                        fbSnack1 = Boolean.parseBoolean( postSnapshot.child( Constants.FB_SNACK1 ).getValue() + "" );
                        fbLunch = Boolean.parseBoolean( postSnapshot.child( Constants.FB_LUNCH ).getValue() + "" );
                        fbSnack2 = Boolean.parseBoolean( postSnapshot.child( Constants.FB_SNACK2 ).getValue() + "" );
                        fbSnack3 = Boolean.parseBoolean( postSnapshot.child( Constants.FB_SNACK3 ).getValue() + "" );
                        fbDinner = Boolean.parseBoolean( postSnapshot.child( Constants.FB_DINNER ).getValue() + "" );

                        homeModel = new HomeModel( fbDate, fbProteinList, fbCarbsList, fbFatsList, fbCaloriesList, fbBreakfast, fbSnack1, fbLunch, fbSnack2, fbSnack3, fbDinner );
                        myList.add( homeModel );
                    }
                }
                homeAdapter = new HomeAdapter( myList );
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager( getContext());
                recyclerView.setLayoutManager( mLayoutManager );
                recyclerView.setItemAnimator( new DefaultItemAnimator() );
                recyclerView.setAdapter( homeAdapter );
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again"+e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private ValueEventListener getConsumedMealData = new ValueEventListener() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                fbProtein = Float.parseFloat(  dataSnapshot.child( Constants.FB_PROTEIN ).getValue() + "");
                fbFats = Float.parseFloat( dataSnapshot.child( Constants.FB_CARBS ).getValue() + "");
                fbCarbs = Float.parseFloat( dataSnapshot.child( Constants.FB_FATS ).getValue() + "");
                fbCalories = Float.parseFloat( dataSnapshot.child( Constants.FB_CALORIES ).getValue() + "");

                tvProtein.setText( String.format("%.0f",fbProtein)+"g");
                tvCarbs.setText( String.format("%.0f",fbCarbs)+"g");
                tvFats.setText( String.format("%.0f",fbFats)+"g");
                tvCalories.setText( String.format("%.0f",fbCalories)+"cal");
                serProgressMax();
                setProgressStart();
                updateMyWidget(String.valueOf(  fbProtein),String.valueOf(  fbCarbs),String.valueOf(  fbFats),String.valueOf(  fbCalories));
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again"+e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void updateMyWidget(String protein, String carbs, String fats, String calories) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        Bundle bundle = new Bundle();
        int appWidgetId = bundle.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        Toast.makeText(getContext(), "Added Widget", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(Constants.SP_WIDGET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String data = "Protein - "+protein+"\nCarbs - "+carbs+"\nFats - "+fats+"\nCalories - "+calories;
        editor.putString( Constants.SP_DATA,  data);
        editor.commit();

        DetailedWidget.updateAppWidget(getContext(), appWidgetManager, appWidgetId);
    }

    private ValueEventListener getGoalData = new ValueEventListener() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                fbProteinGoal = Float.parseFloat(  dataSnapshot.child( Constants.FB_PROTEIN ).getValue() + "");
                fbFatsGoal = Float.parseFloat( dataSnapshot.child( Constants.FB_CARBS ).getValue() + "");
                fbCarbsGoal = Float.parseFloat( dataSnapshot.child( Constants.FB_FATS ).getValue() + "");
                fbCaloriesGoal = Float.parseFloat( dataSnapshot.child( Constants.FB_PROFILE_INFO_CHILD_CALORIES_GOAL ).getValue() + "");

                tvProteinGoal.setText( String.format("%.0f",fbProteinGoal)+"g");
                tvCarbsGoal.setText( String.format("%.0f",fbCarbsGoal)+"g");
                tvFatsGoal.setText( String.format("%.0f",fbFatsGoal)+"g");
                tvCaloriesGoal.setText( String.format("%.0f",fbCaloriesGoal)+"cal");

                serProgressMax();
                setProgressStart();
            }
            catch (Exception e) {
                Toast.makeText( getContext(), "Error, please try again", Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setProgressStart() {
        progressBarProtein.setProgress( Integer.parseInt( String.format("%.0f",fbProtein) ) );
        progressBarCarbs.setProgress( Integer.parseInt( String.format("%.0f",fbCarbs) ) );
        progressBarFats.setProgress( Integer.parseInt( String.format("%.0f",fbFats) ) );
        progressBarCalories.setProgress( Integer.parseInt( String.format("%.0f",fbCalories) ) );
    }
    private void serProgressMax() {
        progressBarProtein.setMax( Integer.parseInt( String.format("%.0f",fbProteinGoal) ) );
        progressBarCarbs.setMax( Integer.parseInt( String.format("%.0f",fbCarbsGoal) ) );
        progressBarFats.setMax( Integer.parseInt( String.format("%.0f",fbFatsGoal) ) );
        progressBarCalories.setMax( Integer.parseInt( String.format("%.0f",fbCaloriesGoal) ) );
    }
}
