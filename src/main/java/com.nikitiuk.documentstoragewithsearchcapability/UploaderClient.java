package com.nikitiuk.documentstoragewithsearchcapability;

import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.*;

public class UploaderClient {
    public static void main(String[] args) throws IOException
    {
        final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();

        final FileDataBodyPart filePart = new FileDataBodyPart("file", new File("/home/npalexey/Downloads/codeconventions-150003.pdf"));
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar").bodyPart(filePart);

        final WebTarget target = client.target("http://localhost:9999/DocumentStorageWithSearchCapability/rest/testservice/pdf");
        final Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));

        //Use response object to verify upload success

        formDataMultiPart.close();
        multipart.close();
    }
}
