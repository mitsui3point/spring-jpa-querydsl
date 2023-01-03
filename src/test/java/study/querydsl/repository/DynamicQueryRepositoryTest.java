package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import study.querydsl.dto.MemberDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest
//@Transactional
public class DynamicQueryRepositoryTest extends TestDataGenerator {
    @Autowired
    private DynamicQueryRepository dynamicQueryRepository;

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
