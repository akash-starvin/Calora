package com.android.calora.firebase;

public class FBProfileInfo {

    String userGender, userFitnessGoal, userDietType;
    float userWeight, userHeight, userCaloriesGoal, protein, carbs, fats,userAge;

    public float getUserAge() {
        return userAge;
    }

    public void setUserAge(float userAge) {
        this.userAge = userAge;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
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

    public float getUserWeight() {
        return userWeight;
    }

    public void setUserWeight(float userWeight) {
        this.userWeight = userWeight;
    }

    public float getUserHeight() {
        return userHeight;
    }

    public void setUserHeight(float userHeight) {
        this.userHeight = userHeight;
    }

    public float getUserCaloriesGoal() {
        return userCaloriesGoal;
    }

    public void setUserCaloriesGoal(float userCaloriesGoal) {
        this.userCaloriesGoal = userCaloriesGoal;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getCarbs() {
        return carbs;
    }

    public void setCarbs(float carbs) {
        this.carbs = carbs;
    }

    public float getFats() {
        return fats;
    }

    public void setFats(float fats) {
        this.fats = fats;
    }

    public FBProfileInfo(float userAge, String userGender, String userFitnessGoal, String userDietType, float userWeight, float userHeight, float userCaloriesGoal, float protein, float carbs, float fats) {
        this.userAge = userAge;
        this.userGender = userGender;
        this.userFitnessGoal = userFitnessGoal;
        this.userDietType = userDietType;
        this.userWeight = userWeight;
        this.userHeight = userHeight;
        this.userCaloriesGoal = userCaloriesGoal;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }
}
