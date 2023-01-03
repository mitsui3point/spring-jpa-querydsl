package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	/**
	 * 동시성(멀티쓰레드)문제가 있지 않을까?<br>
	 * : 없다.<br>
	 * : JPAQueryFactory 는 EntityManager 에 의존하는데,<br>
	 * 		EntityManager 는 Spring 사용 시<br>
	 * 		Spring framework 가 EntityManager Proxy 객체를 등록한 후<br>
	 * 		Transactional 단위로 바인딩 되도록 routing 만 해줌.<br>
	 * 		그러므로 EntityManager 가 Transactional 단위로 동작하게 되면서,<br>
	 * 		JPAQueryFactory 또한 멀티쓰레드 문제가 발생하지 않게 된다.<br>
	 *  <p>
	 * 장점<br>
	 * : @{@link lombok.RequiredArgsConstructor} 사용가능<br>
	 * <p>
	 * 단점
	 * : 테스트 코드 작성시 @Bean 등록을 하게 되면 테스트 코드에 두가지 의존성을 넣게 된다.
	 */
	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
		return new JPAQueryFactory(entityManager);
	}
}
