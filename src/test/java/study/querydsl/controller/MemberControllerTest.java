package study.querydsl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Profile("local")
public class MemberControllerTest {
    private MockMvc mvc;

    @Autowired
    private MemberController memberController;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @Test
    @Transactional
    void initTest() throws Exception {
        //given
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "member1");

        //when
        ResultActions perform = mvc.perform(get("/v1/members").params(params));

        //then
        perform.andDo(print())
                .andExpect(status().isOk());

    }

}
