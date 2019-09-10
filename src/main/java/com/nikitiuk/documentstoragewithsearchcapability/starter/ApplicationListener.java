package com.nikitiuk.documentstoragewithsearchcapability.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationListener implements javax.servlet.ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            Properties prop = new Properties();
            String propFileName = "appconfig.properties";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException(String.format("Property file '%s' is not found in the classpath", propFileName));
            }
            System.setProperty("local.path.to.storage", prop.getProperty("LOCAL_PATH"));
            System.setProperty("current.authorization.property", prop.getProperty("AUTHORIZATION_PROPERTY"));
            System.setProperty("current.authentication.scheme", prop.getProperty("AUTHENTICATION_SCHEME"));
            System.setProperty("tika.config", prop.getProperty("TIKA_CONFIG"));
            System.setProperty("default.folder", prop.getProperty("DEFAULT_FOLDER"));
            inputStream.close();
        } catch (Exception e) {
            logger.error("Error at ApplicationListener contextInitialized: ", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}