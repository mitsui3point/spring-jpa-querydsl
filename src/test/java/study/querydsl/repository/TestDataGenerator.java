package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@Transactional
public class TestDataGenerator {
    @PersistenceContext
    protected EntityManager em;

    /**
     * 메서드 안이 아닌 field 에 선언하면 multi thread 가 동시적으로 접근할 경우 문제가 되지 않을까?
     * EntityManager 가 Transaction 단위로 동작하기 때문에,
     * EntityManager 가 자신이 속한 Transaction 이외에서 동작하지 않게 설계되어 있어, 동시성 문제가 없다.
     */
    protected JPAQueryFactory queryFactory;
    protected Team teamA;
    protected Team teamB;
    protected Member member1;
    protected Member member2;
    protected Member member3;
    protected Member member4;

    @BeforeEach
    void setUp() {
        queryFactory = new JPAQueryFactory(em);

        teamA = Team.builder().name("teamA").build();
        teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        member1 = Member.builder().username("member1").age(10).team(teamA).build();
        member2 = Member.builder().username("member2").age(20).team(teamA).build();
        member3 = Member.builder().username("member3").age(30).team(teamB).build();
        member4 = Member.builder().username("member4").age(40).team(teamB).build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();
    }
}
