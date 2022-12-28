package study.querydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class HelloTest {
    @PersistenceContext
    private EntityManager em;

    private Hello hello;

    @BeforeEach
    void setUp() {
        //given
        hello = Hello.builder().build();
        em.persist(hello);
    }

    @Test
    void helloEntityTest() {
        //given
        //when
        Hello findHello = em.find(Hello.class, hello.getId());
        //then
        assertThat(hello).isEqualTo(findHello);
    }

    @Test
    void qHelloEntityTest() {
        //given
        //when
        JPAQueryFactory query = new JPAQueryFactory(em);
//        QHello qHello = new QHello("h");//parameter: alias
        QHello qHello = QHello.hello;

        Hello findHello = query.selectFrom(qHello)
                .fetchOne();
        //then
        assertThat(hello).isEqualTo(findHello);
        assertThat(hello.getId()).isEqualTo(findHello.getId());
    }
}
