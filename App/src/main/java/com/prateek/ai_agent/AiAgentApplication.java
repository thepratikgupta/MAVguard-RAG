package com.prateek.ai_agent;

import com.prateek.ai_agent.Project.dto.TelecomChunk;
import com.prateek.ai_agent.Project.dto.TelecomSearchResult;
import com.prateek.ai_agent.Project.service.Lucene3gppService;
import com.prateek.ai_agent.Project.service.TelecomDocumentParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class AiAgentApplication {

    // Read the pattern from application.properties, default to the 3gpp-specs folder
    @Value("${rag.document.pattern:classpath*:3gpp-specs/*.pdf}")
    private String documentPattern;

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }

    @Bean
    CommandLineRunner initEngine(TelecomDocumentParser parser, Lucene3gppService luceneService) {
        return args -> {
            System.out.println("Booting Telecom RAG Engine...");
            System.out.println("Scanning for PDFs using pattern: " + documentPattern);

            try {
                // Use Spring's resolver to find all files matching the pattern
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources(documentPattern);

                if (resources.length == 0) {
                    System.err.println("CRITICAL: No PDF documents found at " + documentPattern);
                    System.err.println("The RAG system will be empty. Please check your documents folder.");
                    return; // Stop initialization safely
                }

                List<TelecomChunk> allChunks = new ArrayList<>();

                // Loop through every PDF found
                for (Resource resource : resources) {
                    String filename = resource.getFilename();
                    // Dynamically set the docId based on the filename (e.g., TS_23501)
                    String docId = filename != null ? filename.replace(".pdf", "") : "Unknown_Doc";
                    
                    System.out.println("Parsing document: " + filename);

                    try (InputStream inputStream = resource.getInputStream()) {
                        List<TelecomChunk> chunks = parser.parse3gppPdf(inputStream, docId);
                        allChunks.addAll(chunks); // Add to the master list
                    } catch (Exception e) {
                        // Catch error for a specific file, but don't crash the whole app
                        System.err.println("Failed to parse " + filename + ": " + e.getMessage());
                    }
                }

                // Index all chunks together
                if (!allChunks.isEmpty()) {
                    luceneService.indexChunks(allChunks);
                    System.out.println("Successfully indexed a total of " + allChunks.size() + " chunks across " + resources.length + " documents.");
                } else {
                    System.err.println("CRITICAL: Documents were found, but no text was extracted.");
                    return;
                }

                // Run test query
                System.out.println("\nRunning internal test query for: 'AMF Architecture'");
                List<TelecomSearchResult> testResults = luceneService.search("AMF Architecture", 3);

                for (int i = 0; i < testResults.size(); i++) {
                    TelecomSearchResult res = testResults.get(i);
                    System.out.println("--- Result " + (i + 1) + " (Score: " + res.score() + ") ---");
                    System.out.println("Document ID: " + res.docId()); // Output the dynamic DocId
                    System.out.println("Section: " + res.sectionNumber());
                    System.out.println("Snippet: " + res.content().substring(0, Math.min(100, res.content().length())) + "...");
                }
                
                System.out.println("\nBackend RAG Engine is READY.");
                
            } catch (Exception e) {
                System.err.println("CRITICAL: Failed to initialize RAG Engine: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}

// @SpringBootApplication
// public class AiAgentApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(AiAgentApplication.class, args);
// 	}

// 	@Bean
// 	CommandLineRunner initEngine(TelecomDocumentParser parser, Lucene3gppService luceneService) {
// 		return args -> {
// 			System.out.println("Booting Telecom RAG Engine...");

// 			ClassPathResource pdfResource = new ClassPathResource("3gpp-specs/TS_23501.pdf");

// 			try (InputStream inputStream = pdfResource.getInputStream()) {

// 			List<TelecomChunk> chunks = parser.parse3gppPdf(inputStream, "TS 23.501");

// 			luceneService.indexChunks(chunks);

// 			System.out.println("\nRunning internal test query for: 'AMF Architecture'");
// 			List<TelecomSearchResult> testResults = luceneService.search("AMF Architecture", 3);

// 			for (int i = 0; i < testResults.size(); i++) {
// 				TelecomSearchResult res = testResults.get(i);
// 				System.out.println("--- Result " + (i+1) + " (Score: " + res.score() + ") ---");
// 				System.out.println("Section: " + res.sectionNumber());
// 				System.out.println("Snippet: " + res.content().substring(0, Math.min(100, res.content().length())) + "...");
// 			}
// 			System.out.println("\nBackend RAG Engine is READY.");
// 			} catch (Exception e) {
//                 System.err.println("Failed to parse PDF on startup: " + e.getMessage());
//             }
// 		};
// 	}
// }

