package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@SpringBootTest
@Transactional
public class MemberQuerydsl4RepositoryTest {

    @Autowired
    private MemberQuerydsl4Repository memberQuerydsl4Repository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void basicSelectTest() {
        //given
        searchTestData();
        //when
        List<Member> actual = memberQuerydsl4Repository.basicSelect();
        //then
        Assertions.assertThat(actual).extracting("username").containsExactly("member1", "member2", "member3", "member4");
        Assertions.assertThat(actual).extracting("age").containsExactly(10, 20, 30, 40);
    }

    @Test
    void basicSelectFromTest() {
        //given
        searchTestData();
        //when
        List<Member> actual = memberQuerydsl4Repository.basicSelectFrom();
        //then
        Assertions.assertThat(actual).extracting("username").containsExactly("member1", "member2", "member3", "member4");
        Assertions.assertThat(actual).extracting("age").containsExactly(10, 20, 30, 40);
    }

    @Test
    void searchPageByApplyTest() {
        //given
        searchPageTestData();
        //when
        Page<Member> actual = memberQuerydsl4Repository.searchPageByApply(
                MemberSearchCondition.builder()
                        .ageGoe(1)
                        .ageLoe(15)
                        .build(),
                PageRequest.of(0, 5)
        );
        //then
        Assertions.assertThat(actual)
                .extracting("username")
                .containsExactly("member1", "member2", "member3", "member4", "member5");
        Assertions.assertThat(actual)
                .extracting("age")
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void applyPaginationTest() {
        //given
        searchPageTestData();
        //when
        Page<Member> actual = memberQuerydsl4Repository.applyPagination(
                MemberSearchCondition.builder()
                        .ageGoe(1)
                        .ageLoe(15)
                        .build(),
                PageRequest.of(0, 5)
        );
        //then
        Assertions.assertThat(actual)
                .extracting("username")
                .containsExactly("member1", "member2", "member3", "member4", "member5");
        Assertions.assertThat(actual)
                .extracting("age")
                .containsExactly(1, 2, 3, 4, 5);
    }

    private void searchTestData() {
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        Member member2 = Member.builder().username("member2").age(20).team(teamA).build();
        Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(40).team(teamB).build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();
    }

    private void searchPageTestData() {
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 30; i++) {
            Member member = Member.builder()
                    .username("member" + (i + 1))
                    .age(i + 1)
                    .team(i % 2 == 0 ? teamA : teamB).build();
            em.persist(member);
        }

        em.flush();
        em.clear();
    }
}
