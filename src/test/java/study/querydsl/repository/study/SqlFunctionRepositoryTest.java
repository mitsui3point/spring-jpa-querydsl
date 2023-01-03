package study.querydsl.repository.study;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlFunctionRepositoryTest extends TestDataGenerator {

    @Autowired
    private SqlFunctionRepository sqlFunctionRepository;

    /**
     * select function('replace', member1.username, 'member', 'M')
     * from Member member1
     * <p>
     * select replace(member0_.username, 'member', 'M') as col_0_0_
     * from member member0_;
     * <p>
     * function {@link org.hibernate.dialect.H2Dialect} 에 등록되어있어야 함
     * : registerFunction( "replace", new StandardSQLFunction( "replace", StandardBasicTypes.STRING ) );
     */
    @Test
    void sqlFunctionSelectReplaceTest() {
        //given
        List<String> expected = Arrays.asList("M1", "M2", "M3", "M4");

        //when
        List<String> actual = sqlFunctionRepository.sqlFunctionSelectReplace();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * select member0_.username as col_0_0_
     * from member member0_
     * where member0_.username=lower(member0_.username);
     */
    @Test
    void sqlFunctionWhereLowerTest() {
        //given
        List<String> expected = Arrays.asList("member1", "member2", "member3", "member4");

        //when
        List<String> actual = sqlFunctionRepository.sqlFunctionWhereLower();

        //then
        actual.forEach(System.out::println);
        assertThat(actual).isEqualTo(expected);
    }
}
