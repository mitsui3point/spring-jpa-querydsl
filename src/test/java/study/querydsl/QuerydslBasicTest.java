package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    private EntityManager em;

    //메서드 안이 아닌 field 에 선언하면 multi thread 가 동시적으로 접근할 경우 문제가 되지 않을까?
    //EntityManager 가 Transaction 단위로 동작하기 때문에,
    //EntityManager 가 자신이 속한 Transaction 이외에서 동작하지 않게 설계되어 있어, 동시성 문제가 없다.
    private JPAQueryFactory queryFactory;

    private Team teamA;
    private Team teamB;
    private Member member1;
    private Member member2;
    private Member member3;
    private Member member4;


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

    @Test
    void startJPQLTest() {
        //when
        //find member1
        String qlString = "select m from Member m " +
                "where m.username = :username ";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * JPQL
     * : user 가 API, JPQL 을 사용하는 시점에 오류를 발견한다
     * QueryDSL
     * : compile time error.
     */
    @Test
    void startQuerydslTest() {
        //given
        QMember m = new QMember("m1");//constructor variable(별칭): jpql alias(사용시기 ex. 같은테이블 조인 조회시 alias 가 필요할 경우)
        //when
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))//parameter binding //.where(m.username.eq("' or 1=1--")) ==> where m.username=' or 1=1--'; : sql injection 방지
                .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydslRefactorStaticImportTest() {
        //when
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchTest() {
        //given
        Member expected = member1;

        //when
        Member actual = queryFactory.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10, 30)))
                .fetchOne();

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void searchAndParamTest() {
        //given
        Member expected = member1;

        //when
        Member actual = queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.between(10, 30))
                .fetchOne();
        //then
        assertThat(actual).isEqualTo(expected);
    }
}
