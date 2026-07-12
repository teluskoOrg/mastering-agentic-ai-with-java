package com.telusko.teluskoai1.model;

public class Movie
{
    private String movieName;
    private String director;
    private String leadActor;
    private int year;

    // Getters and Setters
    public String getMovieName() {
        return movieName;
    }
    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
    public String getDirector() {
        return director;
    }
    public void setDirector(String director) {
        this.director = director;
    }
    public String getLeadActor() {
        return leadActor;
    }
    public void setLeadActor(String leadActor) {
        this.leadActor = leadActor;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
}
