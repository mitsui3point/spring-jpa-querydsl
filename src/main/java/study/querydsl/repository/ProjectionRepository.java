package study.querydsl.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.QMember;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ProjectionRepository {
    private final EntityManager em;

    /**
     * 메서드 안이 아닌 field 에 선언하면 multi thread 가 동시적으로 접근할 경우 문제가 되지 않을까?
     * EntityManager 가 Transaction 단위로 동작하기 때문에,
     * EntityManager 가 자신이 속한 Transaction 이외에서 동작하지 않게 설계되어 있어, 동시성 문제가 없다.
     */
    private final JPAQueryFactory queryFactory;

    public ProjectionRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<String> searchSimpleProjection() {
        return queryFactory
                .select(QMember.member.username)
                .from(QMember.member)
                .fetch();
    }

    public List<Tuple> searchTupleProjection() {
        return queryFactory
                .select(QMember.member.username, QMember.member.age)
                .from(QMember.member)
                .fetch();
    }
}
