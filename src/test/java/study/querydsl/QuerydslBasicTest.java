package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.Arrays;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    private EntityManager em;
    @PersistenceUnit
    private EntityManagerFactory emf;

    /**
     * 메서드 안이 아닌 field 에 선언하면 multi thread 가 동시적으로 접근할 경우 문제가 되지 않을까?
     * EntityManager 가 Transaction 단위로 동작하기 때문에,
     * EntityManager 가 자신이 속한 Transaction 이외에서 동작하지 않게 설계되어 있어, 동시성 문제가 없다.
     */
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
        /* select member1 from Member member1 where member1.age = ?1
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

    @Test
    void pagingTest() {
        /* select from order by member0_.username desc limit 2 offset 1
         */
        //given
        List<Member> expected = Arrays.asList(member3, member2);

        //when
        List<Member> actual = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void pagingQueryResultsTest() {
        /* select from order by member0_.username desc limit 2 offset 1
         */
        //given
        List<Member> expectedResults = Arrays.asList(member3, member2);

        //when
        QueryResults<Member> actual = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        List<Member> actualResults = actual.getResults();
        long actualTotal = actual.getTotal();
        long actualOffset = actual.getOffset();
        long actualLimit = actual.getLimit();

        //then
        assertThat(actualResults).isEqualTo(expectedResults);
        assertThat(actualTotal).isEqualTo(4);
        assertThat(actualOffset).isEqualTo(1);
        assertThat(actualLimit).isEqualTo(2);
    }

    @Test
    void aggregationTest() {
        //given

        //when
        Tuple actual = queryFactory
                .select(member.count(),
                        member.age.max(),
                        member.age.min(),
                        member.age.avg(),
                        member.age.sum())
                .from(member)
                .fetchOne();

        //then
        assertThat(actual.get(member.count())).isEqualTo(4);
        assertThat(actual.get(member.age.max())).isEqualTo(40);
        assertThat(actual.get(member.age.min())).isEqualTo(10);
        assertThat(actual.get(member.age.avg())).isEqualTo(25);
        assertThat(actual.get(member.age.sum())).isEqualTo(100);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    void groupByTest() {
        //given

        //when
        List<Tuple> teamAgeAvgs = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple actualTeamA = teamAgeAvgs.get(0);
        Tuple actualTeamB = teamAgeAvgs.get(1);

        //then
        assertThat(actualTeamA.get(team.name)).isEqualTo("teamA");
        assertThat(actualTeamA.get(member.age.avg())).isEqualTo(15);

        assertThat(actualTeamB.get(team.name)).isEqualTo("teamB");
        assertThat(actualTeamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void groupByHavingTest() {
        //given

        //when
        List<Tuple> teamAgeAvgs = queryFactory
                .select(
                        team.name,
                        member.age.avg(),
                        member.age.max()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.max().eq(40))
                .fetch();

        Tuple actualTeamB = teamAgeAvgs.get(0);

        //then
        assertThat(actualTeamB.get(team.name)).isEqualTo("teamB");
        assertThat(actualTeamB.get(member.age.avg())).isEqualTo(35);
        assertThat(actualTeamB.get(member.age.max())).isEqualTo(40);
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    void joinTest() {
        //given

        //when
        List<Member> actual = queryFactory.select(member)
                .from(member)
                .join(member.team, team)//.leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        //then
        assertThat(actual)
                .containsExactlyInAnyOrder(member1, member2);
        assertThat(actual)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    void thetaJoinTest() {
        //given
        Member memberTeamA = Member.builder().username("teamA").build();
        Member memberTeamB = Member.builder().username("teamB").build();
        Member memberTeamC = Member.builder().username("teamC").build();
        em.persist(memberTeamA);
        em.persist(memberTeamB);
        em.persist(memberTeamC);

        //when
        List<Member> actual = queryFactory
                .select(member)
                .from(member, team)//cartesian join(cross join)
                .where(member.username.eq(team.name))
                .fetch();

        //then
        assertThat(actual)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 조인 대상 필터링
     * 예) 회원과 팀을 조인하면서,
     * 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
     * JPQL: select m from Member m left join m.team t on t.name = "teamA"
     */
    @Test
    void leftJoinOnFilteringTest() {
        //given
        Member member5 = Member.builder().username("member5").age(50).build();
        em.persist(member5);

        //when
        List<Tuple> actual = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))//.where(team.name.eq("teamA")) ;left join on 은 record 개수에 영향을 주지 않으므로 record 개수가 틀어짐
                .fetch();

        //then
        actual.forEach(System.out::println);

        assertThat(actual.size()).isEqualTo(5);
        //member 기준 left join 이므로 member record 는 모두 조회
        assertThat(actual.get(0).get(member)).isEqualTo(member1);
        assertThat(actual.get(1).get(member)).isEqualTo(member2);
        assertThat(actual.get(2).get(member)).isEqualTo(member3);
        assertThat(actual.get(3).get(member)).isEqualTo(member4);
        assertThat(actual.get(4).get(member)).isEqualTo(member5);
        //team name "teamA" 인 team data 값만 채움(나머지 teamB, null .. 등은 null)
        assertThat(actual.get(0).get(team)).isEqualTo(teamA);
        assertThat(actual.get(1).get(team)).isEqualTo(teamA);
        assertThat(actual.get(2).get(team)).isNull();
        assertThat(actual.get(3).get(team)).isNull();
        assertThat(actual.get(4).get(team)).isNull();
    }

    @Test
    void joinOnFilteringTest() {
        //given
        Member member5 = Member.builder().username("member5").age(50).build();
        em.persist(member5);

        //when
        List<Tuple> actual = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))//.on(team.name.eq("teamA")) ;record 추출이므로 결과 같음.
                .fetch();

        //then
        actual.forEach(System.out::println);

        assertThat(actual.size()).isEqualTo(2);
        //member 기준 inner join 이므로 team.name = "teamA" 인 record 만 조회
        assertThat(actual.get(0).get(member)).isEqualTo(member1);
        assertThat(actual.get(1).get(member)).isEqualTo(member2);
        //team name "teamA" 인 team data 만 조회해 옴(나머지 teamB, null 등은 inner join 이므로 조회하지 않음)
        assertThat(actual.get(0).get(team)).isEqualTo(teamA);
        assertThat(actual.get(1).get(team)).isEqualTo(teamA);
    }

    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    void leftJoinOnNoRelationTest() {
        //given
        Member memberTeamA = Member.builder().username("teamA").build();
        Member memberTeamB = Member.builder().username("teamB").build();
        Member memberTeamC = Member.builder().username("teamC").build();
        em.persist(memberTeamA);
        em.persist(memberTeamB);
        em.persist(memberTeamC);

        //when
        List<Tuple> actual = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();
        //then
        actual.forEach(System.out::println);

        assertThat(actual.size()).isEqualTo(7);

        assertThat(actual.get(0).get(member)).isEqualTo(member1);
        assertThat(actual.get(1).get(member)).isEqualTo(member2);
        assertThat(actual.get(2).get(member)).isEqualTo(member3);
        assertThat(actual.get(3).get(member)).isEqualTo(member4);
        assertThat(actual.get(4).get(member)).isEqualTo(memberTeamA);
        assertThat(actual.get(5).get(member)).isEqualTo(memberTeamB);
        assertThat(actual.get(6).get(member)).isEqualTo(memberTeamC);

        assertThat(actual.get(0).get(team)).isNull();
        assertThat(actual.get(1).get(team)).isNull();
        assertThat(actual.get(2).get(team)).isNull();
        assertThat(actual.get(3).get(team)).isNull();
        assertThat(actual.get(4).get(team)).isEqualTo(teamA);
        assertThat(actual.get(5).get(team)).isEqualTo(teamB);
        assertThat(actual.get(6).get(team)).isNull();
    }

    @Test
    void joinOnNoRelationTest() {
        //given
        Member memberTeamA = Member.builder().username("teamA").build();
        Member memberTeamB = Member.builder().username("teamB").build();
        Member memberTeamC = Member.builder().username("teamC").build();
        em.persist(memberTeamA);
        em.persist(memberTeamB);
        em.persist(memberTeamC);

        //when
        List<Tuple> actual = queryFactory
                .select(member, team)
                .from(member)
                .join(team)
                .on(member.username.eq(team.name))
                .fetch();
        //then
        actual.forEach(System.out::println);

        assertThat(actual.size()).isEqualTo(2);

        assertThat(actual.get(0).get(member)).isEqualTo(memberTeamA);
        assertThat(actual.get(1).get(member)).isEqualTo(memberTeamB);
    }

    /**
     * select
     * member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_
     * from member member0_
     * inner join team team1_ on member0_.team_id = team1_.id
     * where member0_.username = 'member1';
     */
    @Test
    void fetchJoinNoTest() {
        //given
        em.flush();
        em.clear();
        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isTeamLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());//초기화가 진행된 Entity 인지 확인

        //then
        assertThat(isTeamLoaded).as("fetch join 미적용").isFalse();
    }

    /**
     * select
     * member0_.member_id as member_i1_1_0_, team1_.id as id1_2_1_,
     * member0_.age as age2_1_0_, member0_.team_id as team_id4_1_0_, member0_.username as username3_1_0_, team1_.name as name2_2_1_
     * from member member0_
     * inner join team team1_ on member0_.team_id = team1_.id
     * where member0_.username = 'member1';
     */
    @Test
    void fetchJoinTest() {
        //given
        em.flush();
        em.clear();
        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        findMember.getTeam().getName();

        boolean isTeamLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());//초기화가 진행된 Entity 인지 확인

        //then
        assertThat(isTeamLoaded).as("fetch join 적용").isTrue();
    }

    /**
     * 나이가 가장 많은 회원을 조회
     */
    @Test
    void subQueryTest() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        Member actual = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub))
                )
                .fetchOne();
        //then
        assertThat(actual).extracting("age").isEqualTo(40);
        assertThat(actual).extracting("username").isEqualTo("member4");
    }

    /**
     * 나이가 10살 초과인 회원을 조회
     */
    @Test
    void subQueryGoeTest() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> actual = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                                select(memberSub.age)
                                        .from(memberSub)
                                        .where(member.age.gt(10))
                        )
                )
                .fetch();
        //then
        assertThat(actual).extracting("age").containsExactly(20, 30, 40);
        assertThat(actual).extracting("username").containsExactly("member2", "member3", "member4");
    }

    /**
     * DB에서 inline view(from subquery) 구현에 관해..
     *  : 최대한 사용하지 않는 방향으로 고민해 볼 필요가 있음; 보통 안좋은 이유가 많다.
     *  : DBMS SQL 에서 기능들을 너무 많이 제공하다 보니,
     *      application layer 관련 로직도 SQL 에 넣고,
     *      presentation layer formatting 로직도 SQL 에 넣고.. 불필요한 SQL 로직이 적재된다.
     *  : SQL 이 아닌 분리된 다른 각각의 계층에서 처리해야 한다.
     *  : SQL 은 최소한의 grouping, filter 으로 줄여진 rawdata 만 가져오는데에만 집중하고,
     *      필요하면 application layer 에서 로직을 처리하고,
     *      presentation layer formatting 이 필요하더라도 presentation layer 에서 처리하는 것이 올바른 책임분리.
     *  : 각각 layer 의 책임분리가 불명확하게 되면,
     *      다른 layer 에 종속적인 SQL 들을 작성하게 되어,
     *      SQL 재사용성이 떨어지게 된다.
     * 한방 쿼리
     *  : 복잡할 경우에 보통 한방 쿼리를 사용하게 되는데,
     *      application layer 에서 SQL 을 나누어 호출하는 방향으로 풀어가는 것이 유리하다.
     *  : SQL AntiPatterns(빌 카윈 저 / 윤성준 역)
     *
     * 불가피하게 사용해야 할 경우
     * 1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
     * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
     * 3. nativeSQL을 사용한다.
     */
    /**
     * 나이가 평균 이상인 회원을 조회
     */
    @Test
    void subQueryInTest() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> actual = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub))
                )
                .fetch();
        //then
        assertThat(actual).extracting("age").containsExactly(30, 40);
        assertThat(actual).extracting("username").containsExactly("member3", "member4");
    }

    @Test
    void selectSubQueryTest() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Tuple> actual = queryFactory
                .select(member.username,
                        select(memberSub.age.avg().subtract(member.age))
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        //then
        System.out.println("actual = " + actual);
        actual.forEach(System.out::println);
        assertThat(actual.size()).isEqualTo(4);
    }

    /**
     * DB에서 case when 문을 사용하면서 rawdata 를 converting 해야 할까..?
     *  : 지양할 필요가 있음. DB 조회는 최소한의 grouping, filter 으로 줄여진 rawdata 만 조회한 뒤,
     *  : 조건에 따라 가공해야 할 data(ex. 전환, 가공, 화면노출..)들은
     *      DB가 아닌
     *      application layer 혹은
     *      presentation layer 에서 처리하는 것이 낫다.
     */
    /**
     * select
     * case when member0_.age=? then ? when member0_.age=? then ? else '기타'
     * end as col_0_0_
     * from member member0_
     */
    @Test
    void basicCaseTest() {
        //given

        //when
        List<String> actual = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        //then
        assertThat(actual).containsExactly("열살", "스무살", "기타", "기타");
    }

    /**
     * select
     * case when member0_.age between ? and ? then ? when member0_.age between ? and ? then ? else '기타'
     * end as col_0_0_
     * from member member0_
     */
    @Test
    void complexCaseTest() {
        //given

        //when
        List<String> actual = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0 ~ 20")
                        .when(member.age.between(21, 30)).then("21 ~ 30")
                        .otherwise("기타"))
                .from(member).fetch();
        //then
        assertThat(actual).containsExactly("0 ~ 20", "0 ~ 20", "21 ~ 30", "기타");
    }

    /**
     * select member0_.username as col_0_0_, member0_.age as col_1_0_,
     * case when member0_.age between ? and ? then ? when member0_.age between ? and ? then ? else 3
     * end as col_2_0_
     * from member member0_
     * order by
     * case when member0_.age between ? and ? then ? when member0_.age between ? and ? then ? else 3
     * end desc
     */
    @Test
    void rankCaseTest() {
        //given
        //when
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        //then
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }

    /**
     * select member0_.username as col_0_0_ from member member0_ where member0_.username='member1';
     */
    @Test
    void constantsTest() {
        //given

        //when
        Tuple actual = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //then
        assertThat(actual.get(0, String.class)).isEqualTo("member1");
        assertThat(actual.get(1, String.class)).isEqualTo("A");
    }

    /**
     * select ((member0_.username||'_')||cast(member0_.age as character varying)) as col_0_0_ from member member0_ where member0_.username='member1';
     */
    @Test
    void constantsConcatTest() {
        //given

        //when
        String actual = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //then
        assertThat(actual).isEqualTo("member1_10");
    }
}
