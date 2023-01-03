package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class DynamicQueryRepositoryTest {
    @PersistenceContext
    private EntityManager em;
    @PersistenceUnit
    private EntityManagerFactory emf;
    @Autowired
    private DynamicQueryRepository dynamicQueryRepository;

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

    /**
     * 동적 쿼리 - BooleanBuilder 사용
     * select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_
     * where member0_.age=10;
     */
    @Test
    void dynamicQueryBooleanBuilderTest() {
        //given
        String usernameParam = null;
        Integer ageParam = 10;

        //when
        List<Member> actual = dynamicQueryRepository.searchMemberBooleanBuilder(usernameParam, ageParam);

        //then
        assertThat(actual).containsExactly(member1);
    }

    private List<Member> searchMemberBooleanBuilder(String usernameCond, Integer ageCond) {

        return dynamicQueryRepository.searchMemberBooleanBuilder(usernameCond, ageCond);
    }

    /**
     * 동적 쿼리 - Where 다중 파라미터 사용
     * : where 조건에 null 값은 무시된다.
     * : 메서드를 다른 쿼리에서도 재활용 할 수 있다.
     * : 쿼리 자체의 가독성이 높아진다.
     * <p>
     * select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_
     * where member0_.username='member1';
     */
    @Test
    void dynamicQueryWhereParamTest() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        //when
        List<Member> actual = dynamicQueryRepository.searchMemberWhereParam(usernameParam, ageParam);

        //then
        assertThat(actual).containsExactly(member1);
    }
    @Test
    void dynamicQueryWhereParamAllTest() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        //when
        List<Member> actualAll = dynamicQueryRepository.searchMemberWhereParamAll(usernameParam, ageParam);

        //then
        assertThat(actualAll).containsExactly(member1);
    }
    @Test
    void dynamicQueryWhereParamReusableTest() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        //when
        List<MemberDto> actualReusable = dynamicQueryRepository.searchMemberWhereParamReusable(usernameParam, ageParam);

        //then
        assertThat(actualReusable).containsExactly(
                MemberDto
                        .builder()
                        .username(member1.getUsername())
                        .age(member1.getAge())
                        .build()
        );
    }
}
