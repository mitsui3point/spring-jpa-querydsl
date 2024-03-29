package study.querydsl.repository.study;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import study.querydsl.entity.Member;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

//@SpringBootTest
//@Transactional
public class BulkUpdateRepositoryTest extends TestDataGenerator {
    @Autowired
    private BulkUpdateRepository bulkUpdateRepository;

    /**
     * update Member member1
     * set member1.username = '비회원'
     * where member1.age < 28
     * <p>
     * 벌크 연산의 문제점
     * : JPQL 배치와 마찬가지로,
     * 영속성 컨텍스트에 있는 엔티티를 무시하고 실행되기 때문에,
     * 배치 쿼리를 실행하고 나면 영속성 컨텍스트를 초기화 하는 것이 안전하다.
     * 벌크 연산 후 PersistenceContext flow
     * 1 member1 = 10 -> 1 DB 비회원  -> pc member1 (벌크연산 결과 무시)
     * 2 member2 = 20 -> 2 DB 비회원  -> pc member2 (벌크연산 결과 무시)
     * 3 member3 = 30 -> 3 DB member3-> pc member3
     * 4 member4 = 40 -> 4 DB member4-> pc member4
     */
    @Test
    void bulkUpdateTest() {
        //when
        long count = bulkUpdateRepository.memberBulkUpdate("비회원", 28);

        List<Member> actual = queryFactory
                .selectFrom(member)
                .fetch();

        //then
        assertThat(count).isEqualTo(2);

        /* 업데이트 확인
        1 member1 = 10 -> 1 DB 비회원
        2 member2 = 20 -> 2 DB 비회원
        3 member3 = 30 -> 3 DB member3
        4 member4 = 40 -> 4 DB member4
        */
        actual.forEach(System.out::println);
        actual.stream()
                .filter(member -> member.getAge() < 28)
                .forEach(member -> assertThat(member.getUsername()).isEqualTo("비회원"));
        actual.stream()
                .filter(member -> !(member.getAge() < 28))
                .forEach(member -> assertThat(member.getUsername()).isNotEqualTo("비회원"));

        /* 영속성 컨텍스트 벌크연산 무시 확인
        1 member1 = 10 -> 1 DB 비회원  -> pc member1 (DB 결과가 변했어도 벌크연산 결과 무시)
        2 member2 = 20 -> 2 DB 비회원  -> pc member2 (DB 결과가 변했어도 벌크연산 결과 무시)
        3 member3 = 30 -> 3 DB member3-> pc member3
        4 member4 = 40 -> 4 DB member4-> pc member4
        ==> 영속성 컨텍스트가 우선권을 가짐
        ==> jpa application level Repeatable read
        */
        System.out.println("========persistence context========");
        Arrays.asList(member1, member2, member3, member4)
                .forEach(System.out::println);
        assertThat(member1.getUsername()).isEqualTo("member1");
        assertThat(member2.getUsername()).isEqualTo("member2");
        assertThat(member3.getUsername()).isEqualTo("member3");
        assertThat(member4.getUsername()).isEqualTo("member4");
    }

    /**
     * update Member member1
     * set member1.age = member1.age + 1
     */
    @Test
    void bulkAddAgeTest() {
        //given
        List<Integer> expectedAges = Arrays.asList(
                member1.getAge() + 1,
                member2.getAge() + 1,
                member3.getAge() + 1,
                member4.getAge() + 1);

        //when
        long count = bulkUpdateRepository.bulkAddAge(1);
        List<Member> actual = queryFactory
                .selectFrom(member)
                .fetch();
        List<Integer> actualAges = actual.stream()
                .map(member -> member.getAge())
                .collect(Collectors.toList());

        //then
        actualAges.forEach(System.out::println);
        assertThat(count).isEqualTo(4);
        assertThat(actualAges).isEqualTo(expectedAges);
    }

    /**
     * delete from Member member1
     * where member1.age < 1
     */
    @Test
    void bulkDeleteTest() {
        //when
        long count = bulkUpdateRepository.bulkDelete(18);
        List<Member> actual = queryFactory
                .selectFrom(member)
                .where(member.age.lt(18))
                .fetch();

        //then
        actual.forEach(System.out::println);
        assertThat(count).isEqualTo(1);
        assertThat(actual).isEmpty();
    }
}
