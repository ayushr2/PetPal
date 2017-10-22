package com.hackharvard.petsafeandroid;

/**
 * Created by ayushranjan on 20/10/17.
 */

public class Post {
    public String imageLocation;
    public String userEmail;
    public double latitude;
    public double longitude;
    public String image;
    public String name;
    public String date;

    public Post() {

    }

    public Post(String userEmail, double xCord, double yCord, String downloadUrl, String imageLocation, String name, String strDate) {
        this.userEmail = userEmail;
        this.latitude = xCord;
        this.longitude = yCord;
        this.image = downloadUrl;
        this.imageLocation = imageLocation;
        this.name = name;
        this.date = strDate;
    }
}
