package com.nikitiuk.documentstoragewithsearchcapability;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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

public class SolrCellRequestDemo {

    public static void main(String[] args) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
        req.addFile(new File("/home/npalexey/Downloads/rfc2616.txt"), "application/text/plain"); //application/octet-stream
        //req.setParam(ExtractingParams.EXTRACT_ONLY, "true");
        NamedList<Object> result = client.request(req);

        System.out.println("Result: " + result);
        //client.commit();
        query();
        queryWithText();
        listCoresInSolr();
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
    }

    public static void queryWithText() throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        SolrQuery query = new SolrQuery();
        query.setQuery("Java");
        query.setHighlight(true);
        //query.setStart(0);
        //query.setRows(10);

        QueryResponse response = client.query(query);
        //NamedList<Object> items = response.getResponse();

        Map<String, Map<String, List<String>>> hitHighlightedMap = response.getHighlighting();
        SolrDocumentList doclist = response.getResults();
        SolrDocumentList documents = response.getResults();
        System.out.println("\nFound " + documents.getNumFound() + " documents");
        System.out.println("Items: " + response.getResponse());
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
