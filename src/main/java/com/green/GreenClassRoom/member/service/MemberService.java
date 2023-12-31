package com.green.GreenClassRoom.member.service;

import com.green.GreenClassRoom.member.vo.MemberVO;

import java.util.List;

public interface MemberService {

    //회원가입
    public int join(MemberVO memberVO);

    //아이디 중복 확인
    public boolean checkId(String memberId);

    // 로그인
    public MemberVO selectLoginInfo(String memberId);

    // 모든 학생 출력
    public List<MemberVO> selectMemberList();

    // 선생님 출력
    public MemberVO selectAdmin();

    //로그인 정보 출력
    public MemberVO loginInfo(String memberId);

}
