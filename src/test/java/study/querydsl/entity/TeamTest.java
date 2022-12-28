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
public class TeamTest {
    @PersistenceContext
    private EntityManager em;

    private Team team;

    @BeforeEach
    void setUp() {
        //given
        team = Team.builder().name("team").build();
        em.persist(team);
    }

    @Test
    void qTeamEntityTest() {
        //given
        //when
        JPAQueryFactory query = new JPAQueryFactory(em);
        QTeam qTeam = QTeam.team;

        Team findTeam = query.selectFrom(qTeam)
                .fetchOne();
        //then
        assertThat(team).isEqualTo(findTeam);
        assertThat(team.getId()).isEqualTo(findTeam.getId());
    }
}
