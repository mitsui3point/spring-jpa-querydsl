package study.querydsl.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

/**
 * 사용자 정의 인터페이스 구현체 implements 사용자 정의 인터페이스
 * <p>
 * : MemberRepositoryCustom (사용자 정의 인터페이스); 구현 대상<br/>
 * : MemberRepository (SpringDataJpa extends 인터페이스); 최종 사용처<br/>
 * : naming rule(legacy)<br/>
 * -> MemberRepositoryImpl(O), MemberRepositoryCustomImpl(X)<br/>
 * : naming rule(spring 2.2 ~ current)<br/>
 * -> MemberRepositoryImpl(O), MemberRepositoryCustomImpl(O)<br/>
 * -> https://inflearn.com/questions/328823<br/>
 */
@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    /**
     * whereParam 장점<br/>
     * 1.projection 이 바뀌더라도 where 조건 methods 재사용하여 조립이 가능
     */
    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable) {
//        /** {@link PageImpl} implements {@link Page} */
//        return new PageImpl<>(
//                searchPageContent(condition, pageable),
//                pageable,
//
        /**
         * 스프링 데이터 라이브러리가 제공<br/>
         * count 쿼리가 생략 가능한 경우 생략해서 처리<br/>
         * : 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때<br/>
         * : 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)<br/>
         */
        return PageableExecutionUtils.getPage(
                searchPageContent(condition, pageable),
                pageable,
                searchPageTotal(condition)::fetchOne);
    }

    private List<MemberTeamDto> searchPageContent(MemberSearchCondition condition, Pageable pageable) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .orderBy(member.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

//    private Long searchPageTotal(MemberSearchCondition condition) {
    private JPAQuery<Long> searchPageTotal(MemberSearchCondition condition) {
        return queryFactory
                .select(member.id.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()));
    }

    /**
     * {@link Predicate} vs {@link BooleanExpression}<br/>
     * {@link BooleanExpression} 권장<br/>
     * : composition 가능
     */
    private BooleanExpression usernameEq(String username) {
        if (hasText(username)) {
            return member.username.eq(username);
        }
        return null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        if (hasText(teamName)) {
            return team.name.eq(teamName);
        }
        return null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        if (ageGoe != null) {
            return member.age.goe(ageGoe);
        }
        return null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        if (ageLoe != null) {
            return member.age.loe(ageLoe);
        }
        return null;
    }
}
