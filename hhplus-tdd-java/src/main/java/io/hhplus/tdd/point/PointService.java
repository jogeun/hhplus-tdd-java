package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(Object o, UserPointTable userPointTable, PointHistoryTable pointHistoryTable){
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    public UserPoint findUserPointById (long id)
    {
        if(!findIsUser(id)){
            throw new NoSuchElementException("해당 ["+id+"] 유저는 존재하지 않습니다.");
        }else{
            return userPointTable.selectById(id);
        }
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    public List<PointHistory> findUserHistoryById (long id){
        return pointHistoryTable.selectAllByUserId(id);
    }


    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    public UserPoint chargePointByUserId (long id, long amount){

        UserPoint chargeUser = findUserPointById(id);

        long maxPoint = 50000;
        if(amount > maxPoint){
            throw new IllegalArgumentException("최대 " +maxPoint+ " 까지 충전 가능합니다. ");
        }else {
            //계산
            long chargePoint = calcChargePoint(chargeUser.point(), amount);
            //저장
            userPointTable.insertOrUpdate(id, chargePoint);
            //내역저장
            savePointHistoryById(id, amount, TransactionType.CHARGE);

            return chargeUser;
        }
    }


    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    public UserPoint usePointByUserId(long id, long amount){

        UserPoint useUser = findUserPointById(id);;

        if(useUser.point() < amount){
            throw new IllegalArgumentException("보유 한 포인트 내역이 부족합니다.\n 보유 포인트 : " + useUser.point() + ", 사용 요청 포인트 : " + amount);
        }else{

            //계산
            long usePoint = calcUsePoint(useUser.point(), amount);
            //저장
            userPointTable.insertOrUpdate(id, usePoint);
            //내역저장
            savePointHistoryById(id,amount,TransactionType.USE);
            
            return useUser;
            
        }

    }

    /**
     * 동시성 처리
     */
    public void userPointTransaction(long id, long amount, String type){

        if(Objects.equals(type, "USE")){
            usePointByUserId(id,amount);
        }else{
            chargePointByUserId(id,amount);
        }
    }

    /**
     * 포인트 히스토리 내역 저장 하기
     */
    public void savePointHistoryById (long id,long amount,TransactionType type){
        pointHistoryTable.insert(id, amount, type, System.currentTimeMillis());

    }

    /**
     * 포인트 충전 하기
     */
    public long calcChargePoint(long initialPoint, long chargePoint){
        return initialPoint + chargePoint;
    }
    /**
     * 포인트 치김 하기
     */
    public long calcUsePoint(long initialPoint, long usePoint){
        return initialPoint - usePoint;
    }


    /**
     * 유저 존재여부 확인
     */
    public boolean findIsUser (long id) {
        //히스토리 내역이 없을시, 신규유저로 판단
        return !findUserHistoryById(id).isEmpty();
    }

}
