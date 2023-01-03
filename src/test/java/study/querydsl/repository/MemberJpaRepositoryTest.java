package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * JPQL -> QueryDsl
     * : 문자열 vs java code => run time error vs compile time error check
     * : 간결함
     * : parameter binding
     */
    @Test
    void basicTest() {
        //given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member2").age(20).build();

        //when
        memberJpaRepository.save(member1);//insert into member (age, team_id, username, member_id) values (10, NULL, 'member1', 1);
        memberJpaRepository.save(member2);//insert into member (age, team_id, username, member_id) values (20, NULL, 'member2', 2);
        Member findMember = memberJpaRepository
                .findById(member1.getId())
                .orElseGet(() -> {
                    return Member.builder()
                            .username("nomember")
                            .build();
                });
        //then
        assertThat(findMember).isEqualTo(member1);

        //when
        List<Member> findMembersByUsername = memberJpaRepository
                .findByUsernameQueryDsl(member1.getUsername());//.findByUsername(member1.getUsername());
        //then
        assertThat(findMembersByUsername).containsExactly(member1);//select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_ where member0_.username=?

        //when
        List<Member> findAllMembers = memberJpaRepository
                .findAllQueryDsl();//.findAll();
        //then
        assertThat(findAllMembers).containsExactly(member1, member2);//select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_;
    }

    /**
     * select member0_.member_id as col_0_0_,member0_.username as col_1_0_,member0_.age as col_2_0_,team1_.id as col_3_0_,team1_.name as col_4_0_
     * from member member0_
     * left outer join team team1_ on member0_.team_id=team1_.id
     * where member0_.age<=35
     * and member0_.age>=40
     * and team1_.name='teamB'
     */
    @Test
    void searchByBuilderTest() {
        //given
        searchTestData();

        //조회조건이 다빠지게 되면 전체를 조회하기 때문에, 기본 조회조건이라던지, 페이징을 이용하는 게 장애를 대응하기 수월하다.
        MemberSearchCondition condition = MemberSearchCondition.builder().ageGoe(35).ageLoe(40).teamName("teamB").build();

        //when
        List<MemberTeamDto> actual = memberJpaRepository.searchByBuilder(condition);
        actual.forEach(System.out::println);

        //then
        assertThat(actual).extracting("username").containsExactly("member4");
    }

    /**
     * select member0_.member_id as col_0_0_,member0_.username as col_1_0_,member0_.age as col_2_0_,team1_.id as col_3_0_,team1_.name as col_4_0_
     * from member member0_
     * left outer join team team1_ on member0_.team_id=team1_.id
     * where member0_.age<=35
     * and member0_.age>=40
     * and team1_.name='teamB'
     */
    @Test
    void searchWhereParamTest() {
        //given
        searchTestData();
        MemberSearchCondition condition = MemberSearchCondition.builder().ageGoe(35).ageLoe(40).teamName("teamB").build();

        //when
        List<MemberTeamDto> actual = memberJpaRepository.search(condition);
        actual.forEach(System.out::println);

        //then
        assertThat(actual).extracting("username").containsExactly("member4");
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

}
