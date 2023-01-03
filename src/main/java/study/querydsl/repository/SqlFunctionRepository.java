package study.querydsl.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.entity.QMember.member;

@Repository
public class SqlFunctionRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public SqlFunctionRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * function {@link org.hibernate.dialect.H2Dialect} 에 등록되어있어야 함
     * : registerFunction( "replace", new StandardSQLFunction( "replace", StandardBasicTypes.STRING ) );
     */
    public List<String> sqlFunctionSelectReplace() {
        List<String> result = queryFactory.select(
                        Expressions.stringTemplate(
                                "function('replace', {0}, {1}, {2})",
                                member.username, "member", "M"))
                .from(member)
                .fetch();
        return result;
    }

    public List<String> sqlFunctionWhereLower() {
        List<String> result = queryFactory.select(member.username)
                .from(member)
//                .where(member.username.eq(Expressions.stringTemplate(
//                        "function('lower', {0})",
//                        member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();
        return result;
    }
}
