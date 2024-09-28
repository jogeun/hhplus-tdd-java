package io.hhplus.tdd;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;


import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    PointService pointService;
    UserPointTable userPointTable;
    PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp(){

        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();

        for(int i =1; i <=5; i++){
            userPointTable.insertOrUpdate((long)i, (long)i*100);
            pointHistoryTable.insert((long)i, (long)i*100, TransactionType.CHARGE,System.currentTimeMillis());
        }

        for(int i =1; i <=5; i++){
            pointHistoryTable.insert(3L, (long)i*100, TransactionType.CHARGE,System.currentTimeMillis());
        }
        pointService = new PointService(null, userPointTable, pointHistoryTable); // 서비스에 Mock 주입

    }

    @Test
    @DisplayName("id로 유저 검색")
    public void getUserPointById(){

        long findUserId = 2L;

        UserPoint result = pointService.findUserPointById(findUserId);

        assertThat(result.id()).isEqualTo(2L);

    }

    @Test
    @DisplayName("id로 유저의 히스토리 확인")
    public void getPointHistoryByUserId(){

        long findUserId = 3L;

        List<PointHistory> result = pointService.findUserHistoryById(findUserId);

        System.out.println(result.toString());

    }

    @Test
    @DisplayName("유저 존재 여부 확인 - 존재안할시")
    void getFindIsUserNot(){

        long userId = 847999L;

        Boolean isUser =  pointService.findIsUser(userId);

        assertThat(isUser).isEqualTo(false);

    }

    @Test
    @DisplayName("유저 존재 여부 확인 - 존재시")
    void getFindIsUserIn(){

        long userId = 1L;

        Boolean isUser =  pointService.findIsUser(userId);

        assertThat(isUser).isEqualTo(true);

    }

    @Test
    @DisplayName("금액 더하기")
    void plusPointByUserId(){

        long initalPoint = 100L;
        long plusPoint = 2000L;

        long result = pointService.calcChargePoint(initalPoint,plusPoint);

        assertThat(result).isEqualTo(2100L);
    }

    @Test
    @DisplayName("금액 충전기능")
    void chargePointByUserId(){

        long userId = 1L;
        long chargePoint = 2400L;

        pointService.chargePointByUserId(userId,chargePoint);

        //저장 확인
        UserPoint result = userPointTable.selectById(userId);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(2500L);

        //내역 저장 확인
        List<PointHistory> resultHistory = pointHistoryTable.selectAllByUserId(userId);

        assertThat(resultHistory.get(resultHistory.size()-1 ).userId()).isEqualTo(userId);
        assertThat(resultHistory.get(resultHistory.size()-1 ).amount()).isEqualTo(chargePoint);
        assertThat(resultHistory.get(resultHistory.size()-1 ).type()).isEqualTo(TransactionType.CHARGE);

    }

    @Test
    @DisplayName("금액 빼기")
    void minusPointByUserId(){

        long initalPoint = 500L;
        long minusPoint = 100L;

        long result = pointService.calcUsePoint(initalPoint,minusPoint);

        assertThat(result).isEqualTo(400L);
    }


    @Test
    @DisplayName("금액 히스토리 저장 확인")
    void usePointByUserId(){
        //내역 저장 확인
        long userId = 20L;
        long saveAmount = 20L;
        pointService.savePointHistoryById(userId, saveAmount, TransactionType.CHARGE);

        List<PointHistory> resultHistory = pointHistoryTable.selectAllByUserId(userId);

        assertThat(resultHistory.get(resultHistory.size()-1 ).userId()).isEqualTo(userId);
        assertThat(resultHistory.get(resultHistory.size()-1 ).amount()).isEqualTo(saveAmount);
        assertThat(resultHistory.get(resultHistory.size()-1 ).type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("금액 사용기능 - 금액 없을시")
    void usePointByUserIdFail(){

        long userId = 1L;
        long usePoint = 20000L;

        pointService.usePointByUserId(userId,usePoint);

        //저장 결과값 확인
        UserPoint result = userPointTable.selectById(userId);

        //에러발생 비교는 테스트코드로 못 하나요??
    }



    @Test
    @DisplayName("금액 충전시 ")
    void chargePointByUserIdMax(){

        long userId = 1L;
        long maxPoint = 50001L;

        pointService.chargePointByUserId(userId,maxPoint);

        //에러발생 비교는 테스트코드로 못 하나요??
    }

    @Test
    @DisplayName("금액 사용기능 - 금액 있을시")
    void usePointByUserIdSuccess(){

        long userId = 5L;
        long usePoint = 400L;

        pointService.usePointByUserId(userId,usePoint);

        //저장 결과값 확인
        UserPoint result = userPointTable.selectById(userId);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(100L);


        //내역 저장 확인
        List<PointHistory> resultHistory = pointHistoryTable.selectAllByUserId(userId);

        assertThat(resultHistory.get(resultHistory.size()-1 ).userId()).isEqualTo(userId);
        assertThat(resultHistory.get(resultHistory.size()-1 ).amount()).isEqualTo(usePoint);
        assertThat(resultHistory.get(resultHistory.size()-1 ).type()).isEqualTo(TransactionType.USE);

    }
}
