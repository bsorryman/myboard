package com.myboard.board.domain;

import lombok.Data;

@Data
public class FreeBoardPost {
    private int idx;
    private String title; 
    private String writer;
    private String password;
    private String contents;
    private char deleteYn;
    private String createdAt;
    private int hits;
    
    public static int seekOffset(int page, int size) {
        if (page > 0) {
            return (page - 1) * size;
        }
        return 0;
    }
}
