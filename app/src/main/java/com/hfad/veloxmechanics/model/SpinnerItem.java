package com.hfad.veloxmechanics.model;

public class SpinnerItem {

    private String spinnerText;
    private int spinnerImage;

    public SpinnerItem(){

    }

    public SpinnerItem(String spinnerText, int spinnerImage) {
        this.spinnerText = spinnerText;
        this.spinnerImage = spinnerImage;
    }

    public String getSpinnerText() {
        return spinnerText;
    }

    public void setSpinnerText(String spinnerText) {
        this.spinnerText = spinnerText;
    }

    public int getSpinnerImage() {
        return spinnerImage;
    }

    public void setSpinnerImage(int spinnerImage) {
        this.spinnerImage = spinnerImage;
    }
}
