package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SolrService {

    public static void indexDocumentWithSolr(String docName, String contentType) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
        req.addFile(new File("/home/npalexey/workenv/DOWNLOADED/" + docName), contentType); //application/octet-stream text/plain pdf
        req.setParam("literal.docpath", "/home/npalexey/workenv/DOWNLOADED/" + docName);
        req.setParam("literal.docname", docName);
        req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        client.request(req);
    }

    public static void deleteDocumentFromSolrIndex(String docName) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        client.deleteByQuery("docname:" + docName);
        client.commit();
    }

    public static StringBuilder searchAndReturnDocsAndHighlightedText(String queryString, List<DocBean> docBeanList) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        SolrQuery query = new SolrQuery();
        if(docBeanList.isEmpty()) {
            return new StringBuilder("You Don't Have Rights For Any Document.");
        }
        Iterator<DocBean> iterator = docBeanList.iterator();
        StringBuilder contentBuilder = new StringBuilder();
        if(iterator.hasNext()){
            contentBuilder.append("docname:(\"").append(iterator.next().getName()).append("\"");
            while (iterator.hasNext()){
                contentBuilder.append(" OR ").append("\"").append(iterator.next().getName()).append("\"");
            }
            contentBuilder.append(") AND ");
        }
        contentBuilder.append("\"").append(queryString).append("\"");
        query.setQuery(contentBuilder.toString());
        //query.setQuery("\"" + queryString + "\"");
        query.setHighlight(true);
        //query.setParam("hl", "on");                                    //same as .setHighlight(true);
        query.setParam("hl.method", "original");          //default original, other: unified, fastVector
        //query.setParam("hl.bs.type", "LINE");                          //if unified method was chosen
        //query.setParam("hl.mergeContiguous", "true");
        query.setParam("hl.simple.pre", " <span style=\"color: #011DFE; font-weight:bold;\">");  //default <em>, hl.tag.pre for other than original
        query.setParam("hl.simple.post", "</span> ");      //default </em>, hl.tag.post for other
        query.setParam("hl.fl", "*");                     //default *, fields to search in
        query.setParam("hl.encoder", "");                 //default html
        query.setParam("hl.snippets", "10");              //default 1
        query.setParam("hl.maxAnalyzedChars", "1000000"); //default 50000
        query.setParam("hl.fragsize", "100");             //default 100
        //query.setParam("hl.usePhraseHighlighter", "false");            //default true
        //query.setStart(0);
        query.setRows(50);
        //NamedList<Object> items = response.getResponse();
        QueryResponse response = client.query(query);
        return formDocsAndHighlightText(response, queryString);
    }


    private static StringBuilder formDocsAndHighlightText(QueryResponse response, String queryString) {
        Map<String, Map<String, List<String>>> hitHighlightedMap = response.getHighlighting();
        SolrDocumentList documents = response.getResults();
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder
                .append("Query request: ").append(queryString)
                .append("\nFound ").append(documents.getNumFound()).append(" document(s)");
        for(Map.Entry<String, Map<String, List<String>>> entry: hitHighlightedMap.entrySet()){
            contentBuilder.append("\n\n\n").append("-------------------------------------------------")
                    .append("In document: ").append(entry.getKey()).append("\n\n\n")
                    .append(entry.getValue().get("_text_"));
        }
        return contentBuilder;
    }

    public static void listCoresInSolr() throws IOException, SolrServerException {
        SolrClient client2 = new HttpSolrClient.Builder("http://localhost:8983/solr/").build();
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = request.process(client2);
        // List of the cores
        List<String> coreList = new ArrayList<>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            coreList.add(cores.getCoreStatus().getName(i));
        }
        System.out.println(coreList.toString());
    }
}
