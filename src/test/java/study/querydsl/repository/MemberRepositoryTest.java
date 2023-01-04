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
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * Jpa -> SpringDataJpa
     */
    @Test
    void basicTest() {
        //given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member2").age(20).build();

        //when
        memberRepository.save(member1);//insert into member (age, team_id, username, member_id) values (10, NULL, 'member1', 1);
        memberRepository.save(member2);//insert into member (age, team_id, username, member_id) values (20, NULL, 'member2', 2);
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
        assertThat(findMembersByUsername).containsExactly(member1);//select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_ where member0_.username=?

        //when
        List<Member> findAllMembers = memberRepository
                .findAll();
        //then
        assertThat(findAllMembers).containsExactly(member1, member2);//select member0_.member_id as member_i1_1_, member0_.age as age2_1_, member0_.team_id as team_id4_1_, member0_.username as username3_1_ from member member0_;
    }
}
