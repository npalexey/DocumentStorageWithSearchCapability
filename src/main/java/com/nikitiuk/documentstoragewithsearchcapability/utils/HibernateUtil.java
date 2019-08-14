package com.nikitiuk.documentstoragewithsearchcapability.utils;

import com.nikitiuk.documentstoragewithsearchcapability.dao.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(DocDao.class);

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                // Hibernate settings equivalent to hibernate.cfg.xml's properties
                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
                settings.put(Environment.URL, "jdbc:mysql://localhost:3306/document_tracker?useSSL=false&serverTimezone=Europe/Moscow");
                settings.put(Environment.USER, "root");
                settings.put(Environment.PASS, "deranbor8989");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
                settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "create-drop");
                //settings.put(Environment.STORAGE_ENGINE, "");
                configuration.setProperties(settings);
                configuration.addAnnotatedClass(DocBean.class);
                configuration.addAnnotatedClass(UserBean.class);
                configuration.addAnnotatedClass(GroupBean.class);
                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                populateTable(sessionFactory);
            } catch (Exception e) {
                logger.error("Exception caught while working with DB. " + e);
            }
        }
        return sessionFactory;
    }

    private static void populateTable(SessionFactory sessionFactory) throws Exception {
        DocDao.populateTableWithDocs(LocalStorageService.listDocumentsInPath(), sessionFactory.openSession());
        UserDao.populateTableWithUsers(sessionFactory.openSession());
        GroupDao.populateTableWithGroups(sessionFactory.openSession());
    }
}
