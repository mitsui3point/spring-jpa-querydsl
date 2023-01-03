package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    /**
     * JPQL -> QueryDsl
     *  : 문자열 vs java code => run time error vs compile time error check
     *  : 간결함
     *  : parameter binding
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
}
