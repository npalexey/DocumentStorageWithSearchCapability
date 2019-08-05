package com.nikitiuk.documentstoragewithsearchcapability;

import org.glassfish.jersey.media.multipart.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;

@Path("/doc")
public class TestService {

    /*@GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTestService() {

        return "Hello World! This is coming from webservice";

    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello Jersey" + "</title>"
                + "<body><h1>" + "Hello Jersey HTML" + "</h1></body>" + "</html> ";
    }*/

    @POST
    @Path("/{parentid}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response uploadPdfFile(  @FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                    @PathParam("parentid") String parentid) throws Exception
    {
        String UPLOAD_PATH = "/home/npalexey/workenv/DOWNLOADED/";
        try
        {
            int read = 0;
            byte[] bytes = new byte[1024];

            OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileMetaData.getFileName()));//fileMetaData.getFileName()
            while ((read = fileInputStream.read(bytes)) != -1)
            {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e)
        {
            throw new WebApplicationException("Error while uploading file. Please try again");
        }
        return Response.ok("Data uploaded successfully").build();
    }

}
