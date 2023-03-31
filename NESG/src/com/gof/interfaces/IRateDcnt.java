package com.gof.interfaces;

import com.gof.entity.IrCurve;
/**
 * <p> 이전 작업의 rst로 받는 금리 데이터</p> 
 * IrDcntRate, IrDcntRateBiz, IrDcntRateBu, IrDcntRateUsr </br>
 * 기준년월별 데이터를 가져오며,irCurveSceNo, applBizDv   </br>
 * (고민) 엔진의 산출결과로 생성되는 금리 결과와 합칠 것인가 ?? 구분할 것인가 ?? </br>
 *  */
public interface IRateDcnt {
	
	public String  getBaseYymm();
	public String  getApplBizDv();
	public IrCurve getIrCurve();
	public String  getIrCurveNm();
	public Integer getIrCurveSceNo();
	public String  getMatCd();
	public Double  getSpotRate(); 

}
