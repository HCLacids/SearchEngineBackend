import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class App {
    public static void main(String[] args) throws Exception {
        // testCreateIndex();
        // testMatchAllDocsQuery();
        // testSearchIndex();
        // testBooleanQuery();
    }
    public static void testCreateIndex() throws IOException{
        Directory indexDirectory = FSDirectory.open(new File("indexDir").toPath());
        Analyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

        File file = new File("../web/webSrc");
        File[] fileList = file.listFiles();
        for (File file2 : fileList) {
            Document document = new Document();
            try (Scanner sc = new Scanner(new FileReader(file2))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] parts = line.trim().split("\t");
                    String text = parts[0].replace("text : ", "");
                    Field textField = new TextField("text", text, Store.YES);
                    String link = parts[1].replace("link : ", "");
                    Field linkField = new StoredField("link", link);
                    String keyword = parts[2].replace("keyword: ", "");
                    Field keywordFeild = new TextField("keyword", keyword, Store.NO);
                    String description = parts[3].replace("description:", "");
                    Field descriptionField = new TextField("description", description, Store.YES);
                    document.add(textField);
                    document.add(linkField);
                    document.add(keywordFeild);
                    document.add(descriptionField);
                    indexWriter.addDocument(document);
                    System.out.println(text);
                }
             }
        }
        indexWriter.close();
    }
    
    public static JSONObject testMatchAllDocsQuery() throws Exception {
        Directory indexDirectory = FSDirectory.open(new File("./indexDir").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query = new MatchAllDocsQuery();
        TopDocs topDocs = indexSearcher.search(query, 30);
        List<JSONObject> list = returnJsonAndPrintln(indexSearcher, topDocs);
        JSONObject object = new JSONObject();
        object.put("list", list);
        indexReader.close();
        return object;
    }

    public static JSONObject testSearchIndex(String input) throws IOException{
        Directory indexDirectory = FSDirectory.open(new File("./indexDir").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query = new TermQuery(new Term("text", input));
        TopDocs topDocs = indexSearcher.search(query, 30);
        List<JSONObject> list = returnJsonAndPrintln(indexSearcher, topDocs);
        JSONObject object = new JSONObject();
        object.put("list", list);
        indexReader.close();
        return object;
    }
    public static JSONObject testBooleanQuery() throws Exception {
        Directory indexDirectory = FSDirectory.open(new File("./indexDir").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        Query query1 = new TermQuery(new Term("text", "白癜风"));
        Query query2 = new TermQuery(new Term("keyword", "沈阳"));
        builder.add(query1, Occur.MUST);
        builder.add(query2, Occur.MUST);
        BooleanQuery query = builder.build();
        TopDocs topDocs = indexSearcher.search(query, 30);
        List<JSONObject> list = returnJsonAndPrintln(indexSearcher, topDocs);
        JSONObject object = new JSONObject();
        object.put("list", list);
        indexReader.close();
        return object;
    }
    public static JSONObject testQueryParser() throws Exception {
        Directory indexDirectory = FSDirectory.open(new File("./indexDir").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("text", new SmartChineseAnalyzer());
        Query query = queryParser.parse("白癜风");
        TopDocs topDocs = indexSearcher.search(query, 10);
        List<JSONObject> list = returnJsonAndPrintln(indexSearcher, topDocs);
        JSONObject object = new JSONObject();
        object.put("list", list);
        indexReader.close();
        return object;
    }
    public static JSONObject testMultiFiledQueryParser() throws Exception {
        Directory indexDirectory = FSDirectory.open(new File("./indexDir").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        String[] fields = {"fileName", "fileContent"};
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, new SmartChineseAnalyzer());
        Query query = queryParser.parse("apache");
        TopDocs topDocs = indexSearcher.search(query, 10);
        List<JSONObject> list = returnJsonAndPrintln(indexSearcher, topDocs);
        JSONObject object = new JSONObject();
        object.put("list", list);
        indexReader.close();
        return object;
    }

    public static List<JSONObject> returnJsonAndPrintln(IndexSearcher indexSearcher, TopDocs topDocs) throws IOException {
        System.out.println("查询结果的总条数："+ topDocs.totalHits);
        List<JSONObject> list = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            JSONObject object = new JSONObject();
            object.put("text", document.get("text"));
            object.put("link", document.get("link"));
            object.put("keyword", document.get("keyword"));
            object.put("description", document.get("description"));
            list.add(object);
            System.out.println(document.get("text"));
            System.out.println(document.get("link"));
            System.out.println(document.get("keyword"));
            System.out.println(document.get("description"));
            System.out.println("----------------------------------");
        }
        return list;
    }
}
