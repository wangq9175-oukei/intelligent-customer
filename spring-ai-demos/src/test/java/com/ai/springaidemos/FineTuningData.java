package com.ai.springaidemos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.reader.TextReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootTest
public class FineTuningData {

    @Test
    public void generateData(@Autowired ChatClient.Builder chatClientBuilder,
                             @Value("classpath:rag/terms-of-service.txt") Resource resource) throws JSONException, JsonProcessingException {
        ChatClient chatClient = chatClientBuilder
                .defaultSystem("""
                        你是一个数据处理专家，需要根据语义分隔成不同的问答片段，转换为Alpaca格式作为fine-turning使用:
                        格式：
                        [
                          {
                            "instruction": "[片段内容指令问题]",
                            "input": "[片段内容的简短问题]",
                            "output": "[片段内容的回复]",
                          },
                        ]
                        """)
                .build();
        TextReader textReader = new TextReader(resource);
        String text = textReader.read().get(0).getText();


        List<JsonData> list = chatClient.prompt().user(text).call()
                .entity(new ParameterizedTypeReference<List<JsonData>>() {});

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new java.io.File("demo.json"), list);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
