package com.soni.RandomWorkout;

public class Exercise {

    public String name;
    public String URL;
    public  String location;
    public  float minutes;
    public  String bodyPart;
    public  String skip;

    public Exercise(){

    }

    public Exercise(String name,
                    String URL,
                    String location,
                    float minutes,
                    String bodyPart,
                    String skip) {

        name = name;
        URL = URL;
        location=location;
        minutes=minutes;
        bodyPart=bodyPart;
        skip=skip;
    }

    public float getMinutes() {
        return minutes;
    }
    public String getName() {
        return name;
    }
    public String getURL() {
        return URL;
    }
    public String getlocation() {
        return location;
    }
    public String getBodyPart() {
        return bodyPart;
    }
    public String getSkip() {
        return skip;
    }

    public String toString() {
        return name;

        //String msg;
        //msg = "Name=" + name + "|Location=" + location + "|Mins=" + Float.toString(minutes) + "|BodyPart=" + bodyPart + "|Skip=" + skip + "|URL=" + URL;
        //return msg;
    }
}



