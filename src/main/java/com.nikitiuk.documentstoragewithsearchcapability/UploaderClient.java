package com.nikitiuk.documentstoragewithsearchcapability;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

public class UploaderClient {

    public static void main(String[] args) throws IOException {
        final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();

        final FileDataBodyPart filePart = new FileDataBodyPart("file", new File("/home/npalexey/Downloads/codeconventions-150003.pdf"));
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar").bodyPart(filePart);

        final WebTarget target = client.target("http://localhost:9999/DocumentStorageWithSearchCapability/rest/doc/somenewpdf.pdf");
        final Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));

        //Use response object to verify upload success

        formDataMultiPart.close();
        multipart.close();
    }
}
