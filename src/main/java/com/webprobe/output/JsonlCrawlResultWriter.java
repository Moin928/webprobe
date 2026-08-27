package com.webprobe.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonlCrawlResultWriter implements CrawlResultWriter{
    private final ObjectMapper objectMapper;
    private final BufferedWriter writer;

    public JsonlCrawlResultWriter (Path outputPath) throws IOException {
        // create the parent folder if not there
        Path parent = outputPath.getParent();

        if(parent != null) {
            Files.createDirectories(parent);
        }

        // jackson tunrns the crawl result into proper json
        this.objectMapper = new ObjectMapper();

        this.objectMapper.findAndRegisterModules();

        this.writer = Files.newBufferedWriter(
            outputPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND
            
        );
    }


    @Override
    public synchronized void write (CrawlResult result) throws IOException {

        //turn one cral result into one json line
        String json = objectMapper.writeValueAsString(result);

        // one workier at a time
        writer.write(json);
        writer.newLine();

        // no sitting in buffer for you result
        writer.flush();
    }

    @Override

     public synchronized void close () throws IOException {
        writer.close();
     }

}