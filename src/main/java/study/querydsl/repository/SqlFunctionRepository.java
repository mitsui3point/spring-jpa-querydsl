package study.querydsl.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class SqlFunctionRepository {

    private final EntityManager em;

    private JPAQueryFactory queryFactory;

    /**
     * function {@link org.hibernate.dialect.H2Dialect} 에 등록되어있어야 함
     * : registerFunction( "replace", new StandardSQLFunction( "replace", StandardBasicTypes.STRING ) );
     */
    public List<String> sqlFunctionSelectReplace() {
        queryFactory = new JPAQueryFactory(em);
        List<String> result = queryFactory.select(
                        Expressions.stringTemplate(
                                "function('replace', {0}, {1}, {2})",
                                member.username, "member", "M"))
                .from(member)
                .fetch();
        return result;
    }

    public List<String> sqlFunctionWhereLower() {
        queryFactory = new JPAQueryFactory(em);
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
