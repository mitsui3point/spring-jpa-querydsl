package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static study.querydsl.entity.QMember.member;

@Repository
public class BulkUpdateRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public BulkUpdateRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public long memberBulkUpdate(String changeName, int ageCond) {
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
