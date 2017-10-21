package com.hackharvard.petsafeandroid;

/**
 * Created by ayushranjan on 20/10/17.
 */

public class Post {
    public String userEmail;
    public double latitude;
    public double longitude;
    public String image;

    public Post() {

    }

    public Post(String userEmail, double xCord, double yCord, String image) {
        this.userEmail = userEmail;
        this.latitude = xCord;
        this.longitude = yCord;
        this.image = image;
    }
}
