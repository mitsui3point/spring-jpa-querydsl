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

/**
 * Jpa -> SpringDataJpa
 */
@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void basicTest() {
        //given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member2").age(20).build();

        //when
        memberRepository.save(member1);
        memberRepository.save(member2);
        Member findMember = memberRepository
                .findById(member1.getId())
                .orElseGet(() -> {
                    return Member.builder()
                            .username("nomember")
                            .build();
                });
        //then
        assertThat(findMember).isEqualTo(member1);

        //when
        List<Member> findMembersByUsername = memberRepository
                .findByUsername(member1.getUsername());
        //then
        assertThat(findMembersByUsername).containsExactly(member1);

        //when
        List<Member> findAllMembers = memberRepository
                .findAll();
        //then
        assertThat(findAllMembers).containsExactly(member1, member2);
    }

    @Test
    void searchWhereParamTest() {
        //given
        searchTestData();

        MemberSearchCondition condition = MemberSearchCondition.builder().ageGoe(35).ageLoe(40).teamName("teamB").build();

        //when
        List<MemberTeamDto> actual = memberRepository.search(condition);
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
