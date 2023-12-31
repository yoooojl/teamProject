package com.green.GreenClassRoom.board.controller;

import com.green.GreenClassRoom.board.service.FreeBoardService;
import com.green.GreenClassRoom.board.vo.FreeBoardVO;
import com.green.GreenClassRoom.board.vo.FreeBookMarkVO;
import com.green.GreenClassRoom.board.vo.ReplyVO;
import com.green.GreenClassRoom.member.service.MemberService;
import com.green.GreenClassRoom.member.vo.MemberVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class FreeBoardController {
    private final FreeBoardService freeBoardService;
    private final MemberService memberService;

    // 게시글 목록 페이지 이동
    @RequestMapping("/freeBoardList")
    public String freeBoardList(FreeBoardVO freeBoardVO, Model model){
        System.out.println("///////////////////////"+freeBoardVO);
        List<FreeBoardVO> boardList = freeBoardService.selectFreeBoardList(freeBoardVO);
        model.addAttribute("boardList",boardList);
        //페이징 처리
        int totalDataCnt = freeBoardService.pagingFreeBoard();
        freeBoardVO.setTotalDataCnt(totalDataCnt);
        freeBoardVO.setPageInfo();

        model.addAttribute("totalDataCnt", totalDataCnt);

        //각각의 게시물의 댓글 갯수를 담은 리스트
        for (FreeBoardVO freeBoard : boardList){
            int pk = freeBoard.getBoardNum();
            int cnt = freeBoardService.totalReply(pk);
            freeBoardVO.setBoardNum(pk);
            freeBoardVO.setFreeReplyCnt(cnt);
            freeBoardService.updateFreeReplyCnt(freeBoardVO);
        }

        return "/content/board/free_board_list";
    }

    // 게시글 작성 페이지 이동
    @GetMapping("/freeBoardWrite")
    public String boardWrite(){

        return "/content/board/free_board_write";
    }

    // 게시글 작성
    @PostMapping("/insertFreeBoard")
    public String insertFreeBoard(FreeBoardVO freeBoardVO, Authentication authentication){
        User user = (User) authentication.getPrincipal();

        freeBoardVO.setWriter(user.getUsername());
//        writer 값 임시로 지정
//        freeBoardVO.setWriter("test2");
        freeBoardService.insertFreeBoard(freeBoardVO);
        System.out.println("@@@@@@@@@@@@@"+freeBoardVO);
        return "redirect:/board/freeBoardList";
    }

    // 게시글 상세 페이지 이동, 게시글 조회수 증가 기능
    @GetMapping("/freeBoardDetail")
    public String freeBoardDetail(int boardNum, Model model, ReplyVO replyVO,
                                  @RequestParam(required = false, defaultValue = "false") boolean noCount
                                    , FreeBoardVO freeBoardVO, FreeBookMarkVO freeBookMarkVO, Authentication authentication){
        // @RequestParam(required = false, defaultValue = "false") boolean noCount
        // 댓글 등록이 실행되지 않으면 noCount가 넘어오지 않고, 그 값은 false가 된다.
        // 댓글 등록 시 게시글 목록 페이지로 오면 카운터 무효!
        // noCount 가 true가 아니라면 페이지의 조회수가 올라간다.
        User user = (User) authentication.getPrincipal();

        System.out.println("@@!!!!!!!!!!!!!!!!이겁니다@@@@@" + replyVO.getLimit());
        if(!noCount){
            freeBoardService.readCntUp(boardNum);
        }
        freeBoardVO.setWriter(user.getUsername());
        model.addAttribute("loginInfo", memberService.selectLoginInfo(user.getUsername()));

        FreeBoardVO freeBoardDetail=freeBoardService.selectFreeBoardDetail(boardNum);
        model.addAttribute("freeBoardDetail",freeBoardDetail);

        List<ReplyVO> replyList = freeBoardService.selectReply(replyVO);
        model.addAttribute("replyList",replyList);

        model.addAttribute("limit", replyVO.getLimit());

        model.addAttribute("totalReply", freeBoardService.totalReply(boardNum));

        freeBookMarkVO.setMemberId(user.getUsername());
        model.addAttribute("insertFreeBookMark", freeBoardService.selectInsertFreeBookMark(freeBookMarkVO));

        return "/content/board/free_board_detail";
    }
    // 게시글 수정 페이지 이동
    @GetMapping("/updateBoardForm")
    public String updateBoardForm(int boardNum,Model model){
        FreeBoardVO freeBoardDetail=freeBoardService.selectFreeBoardDetail(boardNum);
        model.addAttribute("freeBoardDetail",freeBoardDetail);
        return "/content/board/free_board_update_page";
    }

    // 게시글 수정 기능
    @PostMapping("/updateFreeBoard")
    public String updateFreeBoard(FreeBoardVO freeBoardVO){
        freeBoardService.updateFreeBoard(freeBoardVO);
        return "redirect:/board/freeBoardDetail?boardNum="+freeBoardVO.getBoardNum();
    }

    // 게시글 삭제 기능
    @GetMapping("/deleteBoard")
    public String deleteBoard(int boardNum){
        freeBoardService.deleteFreeBoard(boardNum);
        return "redirect:/board/freeBoardList";
    }

    // 댓글 작성
    @PostMapping("/insertReply")
    public String insertReply(ReplyVO replyVO){
        freeBoardService.insertReply(replyVO);
        //+ "&noCount=true" :  댓글 작성시 조회수가 오르지 않게 하기 위해 true인 값인 noCount를 보낸다.
        return "redirect:/board/freeBoardDetail?boardNum="+replyVO.getBoardNum()+"&replyer="+replyVO.getReplyer() + "&noCount=true";
    }

    // 댓글 삭제 기능
    @GetMapping("/deleteReply")
    public String deleteReply(int replyNum,ReplyVO replyVO){
        //replyVO.setReplyNumList(replyNums);
        freeBoardService.deleteReply(replyNum);
        return "redirect:/board/freeBoardDetail?replyNum="+replyVO.getReplyNum()+"&boardNum="+replyVO.getBoardNum()+"&replyer="+replyVO.getReplyer() + "&noCount=true";
    }

    // 댓글 더보기 기능
    @GetMapping("/showReplyMore")
    public String showReplyMore(ReplyVO replyVO, int limit){
        replyVO.setLimit(limit);
        return "redirect:/board/freeBoardDetail?boardNum=" + replyVO.getBoardNum() + "&limit=" + replyVO.getLimit();
    }

}
