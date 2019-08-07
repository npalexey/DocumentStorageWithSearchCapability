package com.nikitiuk.documentstoragewithsearchcapability.serverpart;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.extraction.ExtractingParams;
import org.eclipse.jetty.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolrService {

    public static void main(String[] args) throws IOException, SolrServerException {
        /*SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
        String docName = "Chapter13.pdf";
        req.addFile(new File("/home/npalexey/Downloads/" + docName), "application/pdf"); //application/octet-stream text/plain pdf
        //req.setParam(ExtractingParams.EXTRACT_ONLY, "true");
        req.setParam("literal.docname", docName);
        //System.out.println("Request before commit: " + req);
        req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        NamedList<Object> result = client.request(req);

        //client.commit();
        System.out.println("Result after commit: " + result);*/
        //System.out.println(req);
        //System.out.println("Request after commit: " + req.toString());
        query();
        //queryWithText();
        //listCoresInSolr();
    }

    public static void indexDocumentWithSolr(String docName, String contentType) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
        req.addFile(new File("/home/npalexey/workenv/DOWNLOADED/" + docName), contentType); //application/octet-stream text/plain pdf
        req.setParam("literal.docname", docName);
        req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        client.request(req);
    }

    public static void deletDocumentFromSolrIndex(String docName) throws IOException, SolrServerException {
        //Preparing the Solr client
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        //Delete by id
        //solrClinet.deleteByQuery("id:1");
        //Delete by other field
        //solrClinet.deleteByQuery("city:Washington");
        //Deleting all documents
        //solrClinent.deleteByQuery("*:*");

        client.deleteByQuery("docname:" + docName);

        //Saving the document
        client.commit();
    }

    public static void query() throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/").build();
        Map<String, String> queryParamMap = new HashMap<String, String>();
        queryParamMap.put("q", "*:*");
        //queryParamMap.put("fl", "id, name");
        //queryParamMap.put("sort", "id asc");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);

        QueryResponse response = client.query("mycoll", queryParams);
        SolrDocumentList documents = response.getResults();

        System.out.println("\nFound " + documents.getNumFound() + " documents");
        for (int i = 0; i < documents.size(); ++i) {
            System.out.println(documents.get(i));
        }
    }

    public static StringBuilder searchAndReturnDocsAndHighlightedText(String queryString) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        SolrQuery query = new SolrQuery();
        //String queryString = "\"Key classes\"";
        query.setQuery(queryString);
        query.setHighlight(true);
        //query.setParam("hl", "on");
        query.setParam("hl.fl", "*");
        query.setParam("hl.encoder", "");
        //query.setStart(0);
        //query.setRows(10);

        QueryResponse response = client.query(query);
        //NamedList<Object> items = response.getResponse();

        Map<String, Map<String, List<String>>> hitHighlightedMap = response.getHighlighting();
        SolrDocumentList doclist = response.getResults();
        SolrDocumentList documents = response.getResults();
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder
                .append("\nQuery request: ").append(queryString)
                .append("\nFound ").append(documents.getNumFound()).append(" documents\n")
                .append("Highlighting documents: ").append(hitHighlightedMap);
        System.out.println("Query request: " + queryString);
        System.out.println("\nFound " + documents.getNumFound() + " documents\n");
        //System.out.println("Items: " + response.getResponse());
        System.out.println("Highlighting documents: " + hitHighlightedMap);
        return contentBuilder;
    }

    public static void listCoresInSolr() throws IOException, SolrServerException {
        SolrClient client2 = new HttpSolrClient.Builder("http://localhost:8983/solr/").build();
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = request.process(client2);

        // List of the cores
        List<String> coreList = new ArrayList<String>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            coreList.add(cores.getCoreStatus().getName(i));
        }

        System.out.println(coreList.toString());
    }
}
