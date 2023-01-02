package study.querydsl.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"username", "age"})
public class MemberDto {
    private String username;
    private int age;

    @Builder
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberDto memberDto = (MemberDto) o;
        return getAge() == memberDto.getAge() && Objects.equals(getUsername(), memberDto.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getAge());
    }
}
