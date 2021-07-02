package com.solution.goncharova;

import org.apache.logging.log4j.LogManager;

public class LogTest {
    private static final org.apache.logging.log4j.Logger LOG4j2 = LogManager.getLogger(LogTest.class);
    public static void main(String[] args) {
       LOG4j2.info("!!!!!!");
    }
}
