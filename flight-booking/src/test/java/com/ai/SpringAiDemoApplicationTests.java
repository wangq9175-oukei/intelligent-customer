package com.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootTest
class SpringAiDemoApplicationTests {

    @Test
    void contextLoads(@Value("classpath:rag/terms-of-service.txt") Resource resource) {
        TextReader textReader = new TextReader(resource);

        List<Document> documents = textReader.read();
        for (Document document : documents) {
            System.out.println(document.getText());
        }
    }

}
