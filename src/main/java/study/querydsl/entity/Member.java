package study.querydsl.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@ToString(of = {"id", "username", "age"})
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    //==연관관계 메서드==//
    public void changeTeam(Team team) {
        if (this.team != null){
            this.team.getMembers().remove(this);
        }
        this.team = team;
        team.getMembers().add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return getAge() == member.getAge() && Objects.equals(getId(), member.getId()) && Objects.equals(getUsername(), member.getUsername())/* && Objects.equals(getTeam(), member.getTeam())*/;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getAge()/*, getTeam()*/);
    }
}
