package com.mycompany.app;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Finals {
    public static final Logger logger = LogManager.getLogger(ModelTrainer.class);

    public static final String BUCKET_NAME = System.getProperty("BUCKET_NAME");
    public static final String SECRET_KEY = System.getProperty("SECRET_KEY");

    public static final String ACCESS_KEY_ID = System.getProperty("ACCESS_KEY_ID");


}
