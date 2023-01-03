package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class DynamicQueryRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public DynamicQueryRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Member> searchMemberBooleanBuilder(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder(/*member.username.eq(usernameCond)*/);
        if (usernameCond != null) {
            builder.and(QMember.member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(QMember.member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(QMember.member)
                .where(builder)
                .fetch();
    }

    public List<Member> searchMemberWhereParam(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(QMember.member)
                .where(usernameEq(usernameCond), ageEq(ageCond))//파라미터 null 일 경우 무시; ex) .where(null, ageEq(ageCond))
                .fetch();
    }

    public List<Member> searchMemberWhereParamAll(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(QMember.member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    public List<MemberDto> searchMemberWhereParamReusable(String usernameCond, Integer ageCond) {
        return queryFactory
                .select(new QMemberDto(QMember.member.username, QMember.member.age))
                .from(QMember.member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    /**
     * 메소드 추출로 재사용이 용이,
     * searchMemberWhereParam 외에 다른 where() 절에 사용 가능
     */
    public BooleanExpression usernameEq(String usernameCond) {
        if (usernameCond != null) {
            return QMember.member.username.eq(usernameCond);
        }
        return null;
    }

    public BooleanExpression ageEq(Integer ageCond) {
        if (ageCond != null) {
            return QMember.member.age.eq(ageCond);
        }
        return null;
    }

    public BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond)
                .and(ageEq(ageCond));
    }
}