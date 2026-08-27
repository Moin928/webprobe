package com.webprobe.output;

import java.io.IOException;

public interface CrawlResultWriter {
    void write(CrawlResult result) throws IOException;
    void close() throws IOException;
}
