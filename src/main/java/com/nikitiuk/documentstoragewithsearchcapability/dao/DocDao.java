package com.nikitiuk.documentstoragewithsearchcapability.dao;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;

public class DocDao {

    public static void populateTableWithDocs(List<DocBean> docBeanList, Session session) {
        for (DocBean docBean : docBeanList) {
            Transaction transaction = null;
            try {
                // start a transaction
                transaction = session.beginTransaction();
                // save the document object
                session.save(docBean);
                // commit transaction
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
            }
        }
    }

    public static void saveDocument(DocBean document) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<DocBean> beanList = session.createQuery("FROM DocBean", DocBean.class).list();
            if(!beanList.isEmpty()){
                for (DocBean docBean : beanList) {
                    if(!docBean.equals(document)){
                        // start a transaction
                        transaction = session.beginTransaction();
                        // save the document object
                        session.save(document);
                        // commit transaction
                        transaction.commit();
                    }
                }
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static List<DocBean> getDocuments() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DocBean", DocBean.class).list();
        }
    }

    public static void deleteDocument(DocBean docBean){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("DELETE FROM DocBean WHERE name = '"
                    + docBean.getName() + "' AND path = '" + docBean.getPath() + "' ");
        }
    }
}