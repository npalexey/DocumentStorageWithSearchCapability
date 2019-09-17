package com.nikitiuk.documentstoragewithsearchcapability.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

public class ApplicationListener implements javax.servlet.ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            System.setProperty("org.apache.commons.logging.Log",
                    "org.apache.commons.logging.impl.NoOpLog");
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

            String[] loggers = { "org.apache.pdfbox.util.PDFStreamEngine",
                    "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                    "org.apache.pdfbox.pdmodel.font.PDFont",
                    "org.apache.pdfbox.pdmodel.font.FontManager",
                    "org.apache.pdfbox.pdfparser.PDFObjectStreamParser",
                    "org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap",
                    "org.apache.pdfbox"};
            for (String logger : loggers) {
                org.apache.log4j.Logger logpdfengine = org.apache.log4j.Logger
                        .getLogger(logger);
                logpdfengine.setLevel(org.apache.log4j.Level.OFF);
            }
            /*String[] loggers = { "org.apache.pdfbox.util.PDFStreamEngine",
                    "org.apache.pdfbox.pdmodel.font.PDSimpleFont, org.apache.pdfbox"};
            for (String ln : loggers) {
                // Try java.util.logging as backend
                java.util.logging.Logger.getLogger(ln).setLevel(java.util.logging.Level.WARNING);

                // Try Log4J as backend
                org.apache.log4j.Logger.getLogger(ln).setLevel(org.apache.log4j.Level.WARN);

                // Try another backend
                //Log4JLoggerFactory.getInstance().getLogger(ln).setLevel(java.util.logging.Level.WARNING);
            }*/
            /*java.util.logging.Logger
                    .getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);*/
        } catch (Exception e) {
            logger.error("Error at ApplicationListener contextInitialized: ", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}