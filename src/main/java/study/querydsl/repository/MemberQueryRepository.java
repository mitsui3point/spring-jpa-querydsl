package study.querydsl.repository;

import org.springframework.stereotype.Repository;

/**
 * 조회가 복잡한 경우(or MemberRepositoryCustom 의 역할이 중구난방해 지거나, 종속성이 복잡해 질 때)<br>
 * 1.순수 Entity 전용, 재사용성이 높은 핵심 비즈니스 Repository 인터페이스 (extends JPARepository<Type,ID>)<br>
 * 2.복잡한 조회로직, 특정 상황에서만 사용하는 Repository 구현체 (DTO 조회, 특정 화면 조회, 부가적인 조건사항이 동적으로 변동하는 조회쿼리 등..)<br>
 * <p>
 * => 이와 같이 설계적 분리로 유연하게 해소하는것도 좋은 방법.
 */
@Repository
public class MemberQueryRepository {
}
