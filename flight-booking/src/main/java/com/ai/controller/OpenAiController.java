package com.ai.controller;

import com.ai.services.BookingTools;
import com.ai.services.LoggingAdvisor;
import com.ai.services.ToolsService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;


/**
 * @author 智能助手
 * @version 1.0
 * @description: 智能助手
 */
@RestController
@CrossOrigin
public class OpenAiController {

    private static final String CONVERSATION_ID = "flight-booking";

    private final ChatClient chatClient;

    public OpenAiController(ChatClient.Builder chatClientBuilder,
                            VectorStore vectorStore,
                            ChatMemory chatMemory,
                            BookingTools bookingTools,
                            ToolsService toolsService,
                            // mcp tools
                            ToolCallbackProvider mcpTools) {
        this.chatClient = chatClientBuilder
                // 系统角色
                .defaultSystem("""
                        ##角色
                        您是“智能”航空公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
                       您正在通过在线聊天系统与客户互动。
                       ##要求
                            1.在涉及增删改（除了查询）function-call前，必须等用户回复“确认”后再调用tool。
                            2.请讲中文。
                       在提供有关预订或取消预订的信息之前，您必须始终从用户处获取以下信息：预订号、客户姓名。
                       请讲中文。
                       今天的日期是 {current_date}.
                        在更改或退订function-call前，请先获取预订信息并且一定要等用户回复"确定"之后才进行更改或退订的function-call。 
                    """)
                // 对话记忆
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 日志记录
                        new LoggingAdvisor())
                // .defaultTools(bookingTools)
                .defaultTools(toolsService)
                .defaultToolCallbacks(mcpTools)
                 //.defaultFunctions("getBookingDetails", "changeBooking", "cancelBooking") // FUNCTION CALLING
                .build();
    }

    @Autowired
    private VectorStore vectorStore;

    @CrossOrigin
    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {

        Flux<String> content = chatClient.prompt()
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
                .user(message)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().query(message)
                                .similarityThreshold(0.6)
                                .build())
                        .build())
                .options(DeepSeekChatOptions.builder().temperature(0.2).build())
                .stream()
                .content();

        return  content
                .concatWith(Flux.just("[complete]"));

    }
}
