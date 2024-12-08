package com.example.jaime_lopez_feedback_6_novelas.model;

public class Review {

    private String id;
    private String novelId;
    private String reviewer;
    private String comment;
    private int rating;
    private String novelName;

    public Review() {
    }

    public Review(String novelId, String reviewer, String comment, int rating, String novelName) {
        this.novelId = novelId;
        this.reviewer = reviewer;
        this.comment = comment;
        this.rating = rating;
        this.novelName = novelName;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNovelId() {
        return novelId;
    }

    public void setNovelId(String novelId) {
        this.novelId = novelId;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getNovelName() {
        return novelName;
    }

    public void setNovelName(String novelName) {
        this.novelName = novelName;
    }
}

