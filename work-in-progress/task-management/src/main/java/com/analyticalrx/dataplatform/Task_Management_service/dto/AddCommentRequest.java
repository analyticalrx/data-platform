package com.primerx.task.dto;

public class AddCommentRequest {

    private String commentText;

    public AddCommentRequest() {
    }

    public AddCommentRequest(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
}