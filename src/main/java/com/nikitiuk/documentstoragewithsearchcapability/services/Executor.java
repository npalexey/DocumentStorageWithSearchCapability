package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;

import javax.ws.rs.WebApplicationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor {

    /*private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void updateDbInfo(){
        Runnable getTask = () -> {
            try {
                HibernateUtil.getSessionFactory().openSession();
            } catch (Exception e) {
                throw new WebApplicationException("Error while indexing files with DB. Please, try again");
            }
        };
        executorService.execute(getTask);
    }*/
}
