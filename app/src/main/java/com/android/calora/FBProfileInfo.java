package com.android.calora;

public class FBProfileInfo {

    String userAge, userGender, userWeight, userHeight, userFitnessGoal, userDietType, userCaloriesGoal;

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserWeight() {
        return userWeight;
    }

    public void setUserWeight(String userWeight) {
        this.userWeight = userWeight;
    }

    public String getUserHeight() {
        return userHeight;
    }

    public void setUserHeight(String userHeight) {
        this.userHeight = userHeight;
    }

    public String getUserFitnessGoal() {
        return userFitnessGoal;
    }

    public void setUserFitnessGoal(String userFitnessGoal) {
        this.userFitnessGoal = userFitnessGoal;
    }

    public String getUserDietType() {
        return userDietType;
    }

    public void setUserDietType(String userDietType) {
        this.userDietType = userDietType;
    }

    public String getUserCaloriesGoal() {
        return userCaloriesGoal;
    }

    public void setUserCaloriesGoal(String userCaloriesGoal) {
        this.userCaloriesGoal = userCaloriesGoal;
    }

    public FBProfileInfo(String userAge, String userGender, String userWeight, String userHeight, String userFitnessGoal, String userDietType, String userCaloriesGoal) {
        this.userAge = userAge;
        this.userGender = userGender;
        this.userWeight = userWeight;
        this.userHeight = userHeight;
        this.userFitnessGoal = userFitnessGoal;
        this.userDietType = userDietType;
        this.userCaloriesGoal = userCaloriesGoal;
    }
}
