package com.prateek.ai_agent.Project.service;

import com.prateek.ai_agent.Project.dto.TelecomChunk;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelecomDocumentParser {

    public List<TelecomChunk> parse3gppPdf(InputStream inputStream, String docId) throws IOException {
        List<TelecomChunk> chunks = new ArrayList<>();

        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setStartPage(15);

            String fullText = stripper.getText(document);
            Pattern sectionPattern = Pattern.compile("(?m)^(\\d+(\\.\\d+)+)\\s+([^\\n]+)");
            String[] lines = fullText.split("\\r?\\n");

            String currentSection = "General Introduction";
            StringBuilder currentContent = new StringBuilder();

            for (String line : lines) {
                Matcher matcher = sectionPattern.matcher(line);

                if (matcher.find()) {

                    if (currentContent.length() > 150) {
                        chunks.add(new TelecomChunk(docId, currentSection, currentContent.toString().trim()));
                        currentContent.setLength(0);
                    }
                    currentSection = matcher.group(1) + " " + matcher.group(3).trim();
                }
                currentContent.append(line).append(" ");
            }
            if (currentContent.length() > 0) {
                chunks.add(new TelecomChunk(docId, currentSection, currentContent.toString().trim()));
            }
        }

        System.out.println("Successfully parsed " + chunks.size() + " logical chunks from " + docId);
        return chunks;
    }
}

