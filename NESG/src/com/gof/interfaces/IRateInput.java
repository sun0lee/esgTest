package com.gof.interfaces;

import com.gof.entity.IrCurve;
/**
 * <p>input으로 받는 금리 데이터</p> 
 * IrCurveSpot,IrCurveSpotWeek, IrCurveYtm, IrCurveYtmUsr</br>
 * 기준일자별 데이터를 가져오며, input 이므로 시나리오 구분이 없음 </br>
 *  */
public interface IRateInput {
	
	public String  getBaseDate(); 
	public IrCurve getIrCurve();
	public String  getIrCurveNm();
	public String  getMatCd();
	public Double  getRate(); 
	// Integer irCurveSceNo
	// String applBizDv
	
	public default String getBaseYymm() {  // IrDcntRateBu, IrDcntRateUsr 
		return getBaseDate().substring(0,6);
	}
	
}
