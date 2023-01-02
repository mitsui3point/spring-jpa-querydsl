package study.querydsl.dto;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class UserDtoTest {
    @PersistenceContext
    private EntityManager em;

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
     * member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     */
    @Test
    void findUserDtoTest() {
        //given
        List<UserDto> expected = getUserMaxAgeDtos();

        //when
        QMember memberSub = new QMember("memberSub");
        List<UserDto> actual = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),//Entity Dto 간 field 이름이 다른 경우 매핑방법
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub),
                                "age")//age 별칭에 다른 value 매핑
                ))
                .from(member)
                .fetch();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    private List<UserDto> getUserMaxAgeDtos() {
        return Arrays.asList(member1, member2, member3, member4)
                .stream()
                .map(member -> {
                    return UserDto.builder()
                            .name(member.getUsername())
                            .age(member4.getAge())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
