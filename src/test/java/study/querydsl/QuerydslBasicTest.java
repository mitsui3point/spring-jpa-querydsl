package study.querydsl;

import com.querydsl.core.QueryResults;
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
import java.util.Arrays;
import java.util.List;

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

    @Test
    void resultFetchTest() {
        //given
        List<Member> expected = Arrays.asList(member2, member3, member4);
        //when
        List<Member> actual = queryFactory
                .selectFrom(member)
                .offset(1)
                .limit(3)
                .orderBy(member.id.asc())
                .fetch();
        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resultFetchOneTest() {
        //given
        Member expected = member1;
        //when
        /* select member1 from Member member1 where member1.username = ?1 */
        Member actual = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resultFetchFirstTest() {
        //given
        Member expected = member1;
        //when
        /* select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_ limit 1; */
        Member actual = queryFactory
                .selectFrom(member)
                .fetchFirst();

        //then
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * deprecated
     */
    @Test
    void resultFetchResultAndCountTest() {
        //given
        List<Member> expectedResults = Arrays.asList(member1, member2, member3, member4);
        int expectedTotal = expectedResults.size();
        int expectedCount = expectedTotal;

        //when
        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .fetchResults();
        List<Member> actualResults = fetchResults.getResults();/* select member1 from Member member1 */
        long actualTotal = fetchResults.getTotal();/* select count(member1) from Member member1 */

        //then
        assertThat(actualResults).isEqualTo(expectedResults);
        assertThat(actualTotal).isEqualTo(expectedTotal);

        //when
        long actualCount = queryFactory
                .selectFrom(member)
                .fetchCount();/* select count(member1) from Member member1 */

        //then
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원이름이 없을 경우 마지막 출력(nulls test)
     */
    @Test
    void sortTest() {
        /* select member1
        from Member member1
        where member1.age = ?1
        order by member1.age desc, member1.username asc nulls last */
        //given
        Member nullMember = Member.builder().username(null).age(100).build();
        Member member5 = Member.builder().username("member5").age(100).build();
        Member member6 = Member.builder().username("member6").age(100).build();
        em.persist(nullMember);
        em.persist(member5);
        em.persist(member6);
        List<Member> expected = Arrays.asList(member5, member6, nullMember);

        //when
        List<Member> actual = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        //then
        assertThat(actual).isEqualTo(expected);
    }
}
