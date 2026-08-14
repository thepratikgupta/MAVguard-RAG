package com.prateek.ai_agent.Project.service;

import com.prateek.ai_agent.Project.dto.TelecomChunk;
import com.prateek.ai_agent.Project.dto.TelecomSearchResult;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Lucene3gppService {

    private final ByteBuffersDirectory directory = new ByteBuffersDirectory();
    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    @Value("${rag.search.max-results:7}")
    private int hardMaxResults;
    @Value("${rag.search.dynamic-threshold-multiplier:0.75}")
    private float dynamicThresholdMultiplier;

    public void indexChunks(List<TelecomChunk> chunks) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        try (IndexWriter writer = new IndexWriter(directory, config)) {
            writer.deleteAll();

            for (TelecomChunk chunk : chunks) {
                Document doc = new Document();

                doc.add(new StringField("docId", chunk.docId(), Field.Store.YES));
                doc.add(new StringField("sectionNumber", chunk.sectionNumber(), Field.Store.YES));

                doc.add(new TextField("content", chunk.content(), Field.Store.YES));

                writer.addDocument(doc);
            }
            writer.commit();
            System.out.println("Successfully indexed " + chunks.size() + " chunks into Apache Lucene.");
        } catch (IOException e) {
            System.err.println("Error indexing chunks: " + e.getMessage());
        }
    }

    public List<TelecomSearchResult> search(String queryStr, int topK) {
        List<TelecomSearchResult> results = new ArrayList<>();

        try {

            if (directory.listAll().length == 0) return results;

            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);

                QueryParser parser = new QueryParser("content", analyzer);

                Query query = parser.parse(QueryParser.escape(queryStr));

                TopDocs topDocs = searcher.search(query, topK);

                if (topDocs.scoreDocs.length == 0) return results;

                float bestScore = topDocs.scoreDocs[0].score;

                float dynamicThreshold = bestScore * dynamicThresholdMultiplier;

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

                    if (results.size() >= hardMaxResults) break;
                    if (scoreDoc.score < dynamicThreshold) break;

                    Document doc = searcher.storedFields().document(scoreDoc.doc);
                    results.add(new TelecomSearchResult(
                            doc.get("docId"),
                            doc.get("sectionNumber"),
                            doc.get("content"),
                            scoreDoc.score
                    ));
                }

            }
        } catch (Exception e) {
            System.err.println("Error searching index: " + e.getMessage());
        }
        return results;
    }
}
