package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static study.querydsl.entity.QMember.member;

@Repository
@RequiredArgsConstructor
@Transactional
public class BulkUpdateRepository {
    private final EntityManager em;
    private JPAQueryFactory queryFactory;

    public long memberBulkUpdate(String changeName, int ageCond) {
        queryFactory = new JPAQueryFactory(em);

        long updateCount = queryFactory.update(member)
                .set(member.username, changeName)
                .where(member.age.lt(ageCond))
                .execute();

        /* bulk update 시 DBMS 와 PersistenceContext 가 맞지 않으므로 초기화.
        application level Repeatable read */
        em.flush();
        em.clear();

        return updateCount;
    }

    public long bulkAddAge(int addAge) {
        queryFactory = new JPAQueryFactory(em);

        long updateCount = queryFactory.update(member)
                .set(member.age, member.age.add(addAge))
                .execute();

        /* bulk update 시 DBMS 와 PersistenceContext 가 맞지 않으므로 초기화.
        application level Repeatable read */
        em.flush();
        em.clear();

        return updateCount;
    }

    public long bulkDelete(int deleteAgeCond) {
        queryFactory = new JPAQueryFactory(em);

        long updateCount = queryFactory.delete(member)
                .where(member.age.lt(deleteAgeCond))
                .execute();

        /* bulk update 시 DBMS 와 PersistenceContext 가 맞지 않으므로 초기화.
        application level Repeatable read */
        em.flush();
        em.clear();

        return updateCount;
    }
}
