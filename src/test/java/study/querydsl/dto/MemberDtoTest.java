package study.querydsl.dto;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
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
public class MemberDtoTest {
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
     * select member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     */
    @Test
    void findDtoByJPQL() {
        //given
        List<MemberDto> expected = getMemberDtos();

        //when
        List<MemberDto> actual = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                        " from Member m ",
                MemberDto.class).getResultList();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * select member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     * <p>
     * Projections.bean()
     * -> getter/setter javabean 을 활용한 projections; setter 필수
     */
    @Test
    void findDtoBySetterTest() {
        //given
        List<MemberDto> expected = getMemberDtos();

        //when
        List<MemberDto> actual = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * select member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     * <p>
     * Projections.fields()
     * -> MemberDto fields 들을 활용한 projections; setter 없어도 접근가능
     */
    @Test
    void findDtoByFieldTest() {
        //given
        List<MemberDto> expected = getMemberDtos();

        //when
        List<MemberDto> actual = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * select member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     * <p>
     * Projections.constructor()
     * -> MemberDto constructor 를 활용한 projections; String username(타입), int age(타입); 타입 맞춰주어야 한다.
     */
    @Test
    void findDtoByConstructorTest() {
        //given
        List<MemberDto> expected = getMemberDtos();

        //when
        List<MemberDto> actual = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * select member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     * <p>
     * {@link com.querydsl.core.annotations.QueryProjection}
     * <p>
     * 1. 장점<br/>
     *  : 컴파일 타임 오류 확인 ex) 생성자 파라미터 잘못 입력한 경우<br/>
     *  ->  {@link Projections}(런타임 오류 확인)<br/>
     *  ->  {@link com.querydsl.core.annotations.QueryProjection}(컴파일타임 오류 확인)<br/>
     * <p>
     * 2. 단점<br/>
     *  : QMemberDto 생성<br/>
     *  : QueryDsl library(@QueryProjection) 의존성이 생겨버림.<br/>
     *      => ex. QueryDsl 교체 등.. 문제가 생길 수 있음<br/>
     *      => DTO 여러 layer(repository, service, controller.. )에서 사용하는데,<br/>
     *      {@link com.querydsl.core.annotations.QueryProjection} 이 들어가게 되면 앞에 언급한 layer 들에 MemberDto 의 의존성이 같이 흘러들어감.<br/>
     */
    @Test
    void findDtoByQueryProjectionTest() {
        //given
        List<MemberDto> expected = getMemberDtos();
        //when
        List<MemberDto> actual = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    private List<MemberDto> getMemberDtos() {
        return Arrays.asList(member1, member2, member3, member4)
                .stream()
                .map(member -> {
                    return MemberDto.builder()
                            .username(member.getUsername())
                            .age(member.getAge())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
