package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    /**
     * select member0_.member_id as col_0_0_, member0_.username as col_1_0_, member0_.age as col_2_0_, team1_.id as col_3_0_, team1_.name as col_4_0_
     * from member member0_
     * left outer join team team1_ on member0_.team_id=team1_.id
     * where
     * member0_.username=?
     * and team1_.name=?
     * and member0_.age>=?
     * and member0_.age<=?
     */
    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMembersV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMembersV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPage(condition, pageable);
    }
}
