package study.querydsl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@AutoConfigureMockMvc
@Profile("local")
public class MemberControllerTest {
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager em;

    @Autowired
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        init();
    }

    @Test
    @Transactional
    void initV1Test() throws Exception {
        //given
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "member1");

        List<MemberTeamDto> expected = queryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(member.username.eq("member1"))
                .fetch();
        String expectedJson = new ObjectMapper().writeValueAsString(expected);

        //when
        ResultActions perform = mvc.perform(get("/v1/members").params(params));

        //then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

    }

    @Test
    @Transactional
    void initV2Test() throws Exception {
        //given
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "member11");
        params.add("teamName", "teamB");
        params.add("ageGoe", "10");
        params.add("ageLoe", "30");
        params.add("size", "5");
        params.add("page", "0");
        
        List<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(member.username.eq("member11"), team.name.eq("teamB"), member.age.goe(10), member.age.loe(30))
                .offset(0).limit(5)
                .orderBy(member.id.asc())
                .fetch();

        Page<MemberTeamDto> expected = new PageImpl<>(results, PageRequest.of(0, 5), 1);
        String expectedJson = new ObjectMapper().writeValueAsString(expected);

        //when
        ResultActions perform = mvc.perform(get("/v2/members").params(params));

        //then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    private void init() {
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();

        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 100; i++) {
            Team selectedTeam = i % 2 == 0 ? teamA : teamB;
            em.persist(Member.builder()
                    .username("member" + i)
                    .age(i)
                    .team(selectedTeam)
                    .build());
        }
    }

}
