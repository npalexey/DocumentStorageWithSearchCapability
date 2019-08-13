package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import org.apache.commons.io.FilenameUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorageService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";

    public static List<DocBean> getDocsInPath() throws IOException {
        List<DocBean> docBeanList = new ArrayList<>();
        Stream<Path> walk = Files.walk(Paths.get(PATH));
        Set<String> allowedFormats = Stream.of("doc", "docx", "pdf", "txt", "html", "xml")
                .collect(Collectors.toCollection(HashSet::new));
        List<String> result = walk.map(x -> x.toString())
                .filter(f -> allowedFormats.contains(FilenameUtils.getExtension(f)))
                .collect(Collectors.toList());
        if (!result.isEmpty()) {
            for (String docPath : result) {
                docBeanList.add(new DocBean(docPath.substring(docPath.lastIndexOf("/") + 1), docPath));
            }
        }
        return docBeanList;
    }
}
