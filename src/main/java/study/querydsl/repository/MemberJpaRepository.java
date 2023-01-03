package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

//    public MemberJpaRepository(EntityManager em) {
//        this.em = em;
//        this.queryFactory = new JPAQueryFactory(em);
//    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m " +
                                "from Member m " +
                                "where m.username = :username ",
                        Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m ",
                        Member.class)
                .getResultList();

    }

    public List<Member> findByUsernameQueryDsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<Member> findAllQueryDsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }
}