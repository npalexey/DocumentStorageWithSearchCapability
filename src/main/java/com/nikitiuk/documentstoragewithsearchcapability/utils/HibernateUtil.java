package com.nikitiuk.documentstoragewithsearchcapability.utils;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
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
                //settings.put(Environment.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
                //settings.put(Environment.SHOW_SQL, "true");
                //settings.put(Environment.FORMAT_SQL, "true");
                //settings.put(Environment.USE_SQL_COMMENTS, "true");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "create");
                //settings.put(Environment.UNIQUE_CONSTRAINT_SCHEMA_UPDATE_STRATEGY, "RECREATE_QUIETLY");
                //settings.put(Environment.STORAGE_ENGINE, "");
                configuration.setProperties(settings);
                configuration.addAnnotatedClass(DocBean.class);
                configuration.addAnnotatedClass(GroupBean.class);
                configuration.addAnnotatedClass(UserBean.class);
                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                populateTables();
            } catch (Exception e) {
                logger.error("Exception caught while creating Session Factory. " + e);
            }
        }
        return sessionFactory;
    }

    /*private static void deleteAllDataFromDb() throws Exception {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {

            // start a transaction
            transaction = session.beginTransaction();
            // save the document object
            session.createSQLQuery("DROP TABLE IF EXISTS Documents, User_groups_binding, Users, Permission_groups").executeUpdate();
            // commit transaction
            transaction.commit();
            //session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error while dropping DB: ", e);
        }
    }*/

    private static void populateTables() throws Exception {
        //deleteAllDataFromDb();
        GroupDao.populateTableWithGroups();
        UserDao.populateTableWithUsers();
        DocDao.populateTableWithDocs(LocalStorageService.listDocumentsInPath());
    }
}
