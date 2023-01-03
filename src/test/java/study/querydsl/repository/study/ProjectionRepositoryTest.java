package study.querydsl.repository.study;

import com.querydsl.core.Tuple;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import study.querydsl.entity.QMember;

import java.util.Arrays;
import java.util.List;

//@SpringBootTest
//@Transactional
public class ProjectionRepositoryTest extends TestDataGenerator {

    @Autowired
    private ProjectionRepository projectionRepository;

    /**
     * select member0_.username as col_0_0_, member0_.age as col_1_0_ from member member0_;
     */
    /**
     * select member0_.username as col_0_0_ --select 절 나열하는 구문: Projection
     * from member member0_;
     */
    @Test
    void simpleProjectionTest() {
        //given

        //when
        List<String> actual = projectionRepository.searchSimpleProjection();
        //then
        Assertions.assertThat(actual).containsExactly("member1", "member2", "member3", "member4");
    }

    /**
     * {@link Tuple}: com.querydsl.core
     * Tuple 이 Repository 계층에서 사용하는것은 괜찮으나,
     * Service, Controller 계층에서 사용하는 것은 좋은 설계가 아니다
     * : 하부 구현기술(Jpa, QueryDsl ...) 사용을 다른 계층(ex. Service, Controller 같은 핵심 비즈니스 로직 계층)에 노출하는 것은 좋지 않다.
     * 다른 계층이 하부 구현기술에 의존하게 되는 구조가 되므로 좋지 않은 설계.
     * : 하부 구현기술을 다른것으로 바꿀 때,
     * 다른 계층이 의존하고 있게 되면 하부 구현기술을 유연하게 바꿀 수 없다.
     * ==> parameter return 을 DTO 로 변환하여 다른계층과 데이터를 주고 받을 것.
     */
    @Test
    void tupleProjectionTest() {
        //given

        //when
        List<Tuple> actual = projectionRepository.searchTupleProjection();
        //then
        actual.forEach(tuple -> {
            String username = tuple.get(QMember.member.username);
            Integer age = tuple.get(QMember.member.age);

            System.out.println("username = " + username);
            System.out.println("age = " + age);

            Assertions.assertThat(Arrays.asList("member1", "member2", "member3", "member4")).contains(username);
            Assertions.assertThat(Arrays.asList(10, 20, 30, 40)).contains(age);
        });
    }
}