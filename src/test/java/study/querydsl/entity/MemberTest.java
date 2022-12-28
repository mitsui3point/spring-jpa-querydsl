package study.querydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberTest {
    @PersistenceContext
    private EntityManager em;

    private Team teamA;
    private Team teamB;
    private Member memberA;
    private Member memberB;
    private Member memberC;

    @BeforeEach
    void setUp() {
        //given
        teamA = Team.builder().name("teamA").build();
        teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        memberA = Member.builder().username("memberA").age(10).team(teamB).build();
        memberA.changeTeam(teamA);
        memberB = Member.builder().username("memberB").age(20).team(teamB).build();
        memberC = Member.builder().username("memberC").age(30).team(teamB).build();
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);

        em.flush();
        em.clear();
    }

    @Test
    void qMemberEntityTest() {
        //given
        List<Member> expected = Arrays.asList(memberA, memberB, memberC);
        //when
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember qMember = QMember.member;

        List<Member> findMembers = query.selectFrom(qMember)
                .fetch();
        //then
        for (int i = 0; i < findMembers.size(); i++) {
            Team findTeam = findMembers.get(i).getTeam();
            Team expectedTeam = expected.get(i).getTeam();
            assertThat(findTeam).isEqualTo(expectedTeam);
        }
        assertThat(findMembers).isEqualTo(expected);
    }

    @Test
    void qMemberChangeTeamTest() {
        assertThat(teamA.getMembers()).containsExactlyElementsOf(Arrays.asList(memberA));
        assertThat(teamB.getMembers()).containsExactlyElementsOf(Arrays.asList(memberB, memberC));
    }
}
