package study.querydsl.entity;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class Hello {
    @Id
    @GeneratedValue
    private Long id;

    @Builder
    public Hello() {
    }
}
