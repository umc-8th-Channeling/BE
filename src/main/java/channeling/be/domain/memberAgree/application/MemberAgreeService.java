package channeling.be.domain.memberAgree.application;

import channeling.be.domain.member.domain.Member;
import channeling.be.domain.memberAgree.domain.MemberAgree;
import channeling.be.domain.memberAgree.presentation.MemberAgreeReqDto;

public interface MemberAgreeService {
    /*
     * 회원의 약관 동의 정보를 수정합니다.
     */
    MemberAgree editMemberAgree(MemberAgreeReqDto.Edit dto, Member member);
}
