package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")//INFO 10652 --- [           main] study.querydsl.QuerydslApplication       : The following 1 profile is active: "local"
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    //@Transactional => 스프링 lifecycle 동시사용 안되기 때문에 @PostConstruct, @Transactional 분리하여 구현
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {

        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = Team.builder().name("teamA").build();
            Team teamB = Team.builder().name("teamB").build();

            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(Member.builder()
                        .username("member" + i)
                        .age(i)
                        .team(selectedTeam)
                        .build());
            }
        }
    }
}
