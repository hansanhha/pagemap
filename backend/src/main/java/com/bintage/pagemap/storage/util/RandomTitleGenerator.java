package com.bintage.pagemap.storage.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RandomTitleGenerator {

    private static final List<String> titles = new LinkedList<>();

    static {
        titles.add("임시 너구리");
        titles.add("터진 타이어");
        titles.add("오랜만에 영화");
        titles.add("초스피드 암기");
        titles.add("음메");
    }

    public static String generate() {
        var random = new Random();
        return titles.get(random.nextInt(titles.size()));
    }
}
