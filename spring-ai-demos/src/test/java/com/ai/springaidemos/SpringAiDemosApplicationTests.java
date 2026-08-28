package com.ai.springaidemos;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.*;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootTest
class SpringAiDemosApplicationTests {

    @Test
      void contextLoads(@Value("classpath:rag/terms-of-service.txt") Resource resource,
       @Autowired EmbeddingModel embeddingModel) {
        // 1. 读取
        TextReader textReader = new TextReader(resource);

        List<Document> documents = textReader.read();
//        for (Document document : documents) {
//            System.out.println(document.getText());
//        }

        /**
         * chunkSize: 每块字数
         * minChunkSizeChars:  为了保证不断句，建议设置0
         * minChunkLengthToEmbed:    最小片段的限制，大于该数字的片段才会返回（大文本下可以过滤一些噪声，小文本建议设置0）
         * maxNumChunks: 最大块数，如果超过该数字，会自动忽略掉一些片段.
         * keepSeparator: 是否保留要将换行转为空格
         *
         */
        MyTokenTextSplitter splitter = new MyTokenTextSplitter(120, 0, 0, 100, true);

        //SentenceSplitter splitter = new SentenceSplitter(10);
        List<Document> splitDocuments = splitter.apply(documents);
        // 2. 分词
//        for (Document document : splitDocuments) {
//            System.out.println(document.getText());
//        }


        // 3.向量化
        DashScopeEmbeddingOptions options = DashScopeEmbeddingOptions.builder().build();
        TokenCountBatchingStrategy tokenCountBatchingStrategy = new TokenCountBatchingStrategy();
        List<float[]> embeds = embeddingModel.embed(splitDocuments, options, tokenCountBatchingStrategy);
//        for (float[] embed : embeds) {
//            // 输出embed
//            System.out.println(Arrays.toString(embed));
//            System.out.println(embed.length);
//        }

        // 4. 检索增强
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(splitDocuments);


        SearchRequest searchRequest = SearchRequest.builder()
                .query("我要退票")      // 内部会将文本向量化
                .topK(3)
                .build();

        List<Document> search = vectorStore.similaritySearch(searchRequest);
        for (Document document : search) {
            System.out.println(document.getText());
            System.out.println(document.getScore());
        }


    }

}
