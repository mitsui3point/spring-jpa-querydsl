package study.querydsl.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class HelloControllerTest {
    private MockMvc mvc;
    @Autowired
    private HelloController helloController;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
//        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mvc = MockMvcBuilders.standaloneSetup(helloController).build();
    }

    @Test
    void helloTest() throws Exception {
        //given

        //when
        ResultActions perform = mvc.perform(get("/hello"));
        //then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("hello"));
    }
}
