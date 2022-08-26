package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import set.BoardSet;
import set.ReplySet;
import util.JDBCUtil;
import vo.BoardVO;
import vo.NovelVO;
import vo.ReplyVO;
import vo.Reply_reVO;

public class BoardDAO {
	Connection conn;
	PreparedStatement pstmt;
    final String sql_selectAll_BoardSearch="SELECT * FROM BOARD WHERE MID LIKE '%'||?||'%' AND BCONTENT LIKE '%'||?||'%' AND BTITLE LIKE '%'||?||'%' ORDER BY BID DESC";
    							// 게시글 검색
	final String sql_selectOne_BoardOne="SELECT * FROM BOARD LEFT OUTER JOIN MEMBER ON BOARD.MID=MEMBER.MID WHERE MID=?";
								// 게시글 하나만 뽑는 것
	final String sql_selectAll_BoardAll="SELECT * FROM BOARD LEFT OUTER JOIN MEMBER ON BOARD.MID=MEMBER.MID ORDER BY BID DESC";
								// 게시글 전체 뽑는 것
	final String sql_selectAll_BoardAll_ADMIN="SELECT * FROM BOARD LEFT OUTER JOIN MEMBER ON BOARD.MID=MEMBER.MID ORDER BY BID DESC WHERE MEMBER.MID=?";
								// 게시글 전체 뽑는 것 = 내가 쓴 게시글 
	final String sql_selectOne_ReplyOne="SELECT * FROM REPLY LEFT OUTER JOIN MEMBER ON REPLY.MID=MEMBER.MID WHRER MID=?";
								// 댓글 하나만 뽑는 것
	final String sql_selectAll_ReplyAll="SELECT * FROM REPLY LEFT OUTER JOIN MEMBER ON REPLY.MID=MEMBER.MID ORDER BY RID DESC WHERE BOARD.MID=?";
								// 댓글 전체 뽑는 것 = 내가 쓴 댓글 전체
	final String sql_selectAll_ReplyAll_Board="SELECT * FROM REPLY LEFT OUTER JOIN BOARD ON REPLY.BID=BOARD.BID ORDER BY RID DESC";
								// 해당 게시글의 댓글만 뽑는 것 = 게시글에 맞는 댓글
	final String sql_selectAll_Reply_re="SELECT * FROM REPLY_RE LEFT OUTER JOIN REPLY ON REPLY_RE.RID=REPLY.RID ORDER BY RRID DESC";
								// 대댓글 전체 출력 = 해당 댓글과 맞는 대댓글
	
	final String sql_insert_B="INSERT INTO BOARD VALUES((SELECT NVL(MAX(BID),1000)+1 FROM BOARD),?,?,TO_DATE(sysdate,'yyyy.mm.dd hh24:mi'),?,?)";
	final String sql_update_B="UPDATE BOARD SET TITLE=?,CONTENT=? WHERE BID=?";
	final String sql_delete_B="DELETE FROM BOARD WHERE BID=?";



	// 게시글 상세 누르면 그 아래 댓글 all이 보여야하니까

	
	public ArrayList<BoardSet> sql_selectAll_BoardAll(BoardVO bvo){
		ArrayList<BoardSet> datas = new ArrayList<BoardSet>();
		conn=JDBCUtil.connect();
		System.out.println("시작로그");
		try {
			pstmt=conn.prepareStatement(sql_selectAll_BoardAll);
			ResultSet rs=pstmt.executeQuery();
			System.out.println("시작한다?!");
			while(rs.next()) {
				BoardSet bs = new BoardSet();
				BoardVO boardVO = new BoardVO();
				boardVO.setBid(rs.getInt("BID"));
				boardVO.setBcontent(rs.getString("BCONTENT"));
				boardVO.setBtitle(rs.getString("BTITLT"));
				boardVO.setBdate(rs.getString("BDATE"));
				if(rs.getString("NICKNAME")==null) {
					boardVO.setMid("[이름없음]");
				} else {
					// WRITER대신 MNAME을 담아서 WRITER를 뽑으면 MNAME이 출력된다.
					boardVO.setMid(rs.getString("NICKNAME"));
				}
				bs.setBoardVO(boardVO);

				ArrayList<ReplySet> replySet = new ArrayList<ReplySet>();
				System.out.println("댓글 all로그");
				pstmt=conn.prepareStatement(sql_selectAll_ReplyAll);
				pstmt.setString(1, bvo.getMid());
				ResultSet rs2 =pstmt.executeQuery();
				System.out.println("댓글 all 로그 2");
				while(rs2.next()) {
					ReplyVO rvo = new ReplyVO();
					ReplySet rSet = new ReplySet();
					rvo.setRid(rs2.getInt("RID"));
					rvo.setLid(rs2.getInt("LID"));
					rvo.setBid(rs2.getInt("BID"));
					rvo.setRcontent(rs2.getString("RCONTENT"));
					rvo.setRdate(rs2.getString("RDATE"));
					if(rs.getString("NICKNAME")==null) {
						rvo.setMid("[이름없음]");
					} else {
						// WRITER대신 MNAME을 담아서 WRITER를 뽑으면 MNAME이 출력된다.
						rvo.setMid(rs.getString("NICKNAME"));
					}
					rSet.setReplyVO(rvo);


					ArrayList<Reply_reVO> rrList=new ArrayList<Reply_reVO>();
					pstmt=conn.prepareStatement(sql_selectAll_Reply_re);
					ResultSet rs3 = pstmt.executeQuery();
					while(rs3.next()) {
						Reply_reVO rrvo = new Reply_reVO();
						rrvo.setBid(rs3.getInt("BID"));
						rrvo.setLid(rs3.getInt("LID"));
						rrvo.setRid(rs3.getInt("RID"));
						rrvo.setRrcontent(rs3.getString("RRCONTENT"));
						rrvo.setRrdate(rs3.getString("RRDATE"));
						rrvo.setRrid(rs3.getInt("RRID"));
						if(rs.getString("NICKNAME")==null) {
							rrvo.setMid("[이름없음]");
						} else {
							// WRITER대신 MNAME을 담아서 WRITER를 뽑으면 MNAME이 출력된다.
							rrvo.setMid(rs.getString("NICKNAME"));
						}
						rrList.add(rrvo);		
					}
					rSet.setrrList(rrList);
					replySet.add(rSet);
					bs.setReplySet(replySet);

				}
				datas.add(bs);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datas;
	}


	   public ArrayList<BoardVO> sql_selectAll_BoardSearch(BoardVO bvo){
		      ArrayList<BoardVO> datas=new ArrayList<BoardVO>();
		      conn=JDBCUtil.connect();
		      try {
		            pstmt=conn.prepareStatement(sql_selectAll_BoardSearch);
		            pstmt.setString(1, bvo.getMid());
		            pstmt.setString(2, bvo.getBcontent());
		            pstmt.setString(3, bvo.getBtitle());
		            ResultSet rs=pstmt.executeQuery();
		         while(rs.next()) {
		        	BoardVO data=new BoardVO();
					data.setBid(rs.getInt("BID"));
					data.setBcontent(rs.getString("BCONTENT"));
					data.setBtitle(rs.getString("BTITLT"));
					data.setBdate(rs.getString("BDATE"));
		            datas.add(data);
		         }
		      } catch (SQLException e) {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      } finally {
		         JDBCUtil.disconnect(pstmt, conn);
		      }      
		      return datas;
		   }





	public boolean insert_B(BoardVO bvo) {
		conn=JDBCUtil.connect();
		try {
			pstmt=conn.prepareStatement(sql_insert_B);
			pstmt.setInt(1, bvo.getBid());
			pstmt.setString(2, bvo.getBtitle());
			pstmt.setString(3, bvo.getBcontent());
			pstmt.setString(4, bvo.getBdate());
			pstmt.setString(5, bvo.getMid());
			pstmt.setInt(6, bvo.getLid());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
		return true;
	}
	public boolean update_B(BoardVO bvo) {
		conn=JDBCUtil.connect();
		try {
			pstmt=conn.prepareStatement(sql_update_B);
			pstmt.setString(1, bvo.getBtitle());
			pstmt.setString(2, bvo.getBcontent());
			pstmt.setInt(3,bvo.getBid());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
		return true;
	}
	public boolean delete_B(BoardVO bvo) {
		conn=JDBCUtil.connect();
		try {
			pstmt=conn.prepareStatement(sql_delete_B);
			pstmt.setInt(1,bvo.getBid());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
		return true;
	}

}
