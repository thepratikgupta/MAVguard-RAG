package com.prateek.ai_agent;

import com.prateek.ai_agent.Project.dto.TelecomChunk;
import com.prateek.ai_agent.Project.dto.TelecomSearchResult;
import com.prateek.ai_agent.Project.service.Lucene3gppService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import com.prateek.ai_agent.Project.service.TelecomDocumentParser;
import java.io.File;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class AiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiAgentApplication.class, args);
	}

	@Bean
	CommandLineRunner initEngine(TelecomDocumentParser parser, Lucene3gppService luceneService) {
		return args -> {
			System.out.println("Booting Telecom RAG Engine...");

			ClassPathResource pdfResource = new ClassPathResource("3gpp-specs/TS_23501.pdf");

			try (InputStream inputStream = pdfResource.getInputStream()) {

			List<TelecomChunk> chunks = parser.parse3gppPdf(inputStream, "TS 23.501");

			luceneService.indexChunks(chunks);

			System.out.println("\n🔍 Running internal test query for: 'AMF Architecture'");
			List<TelecomSearchResult> testResults = luceneService.search("AMF Architecture", 3);

			for (int i = 0; i < testResults.size(); i++) {
				TelecomSearchResult res = testResults.get(i);
				System.out.println("--- Result " + (i+1) + " (Score: " + res.score() + ") ---");
				System.out.println("Section: " + res.sectionNumber());
				System.out.println("Snippet: " + res.content().substring(0, Math.min(100, res.content().length())) + "...");
			}
			System.out.println("\nBackend RAG Engine is READY.");
			} catch (Exception e) {
                System.err.println("❌ Failed to parse PDF on startup: " + e.getMessage());
            }
		};
	}
}

