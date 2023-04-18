package com.gof.enums;

import lombok.Getter;

@Getter
public enum EIrModel {
	  AFNS ()
	, DNS  ()
	, HW1F ()
	, CIR  ()
	, CIR_Y5 ()
	, CIR_Y10 ()

	/* 민감도 분석용 */
	, HW1F_NSP (EIrModel.HW1F) //
	, HW1F_SP  (EIrModel.HW1F)  //
//	,HW1F_NSP_INIT_0.001
//	,HW1F_NSP_INIT_0.01
//	,HW1F_NSP_INIT_0.02
//	,HW1F_NSP_INIT_0.03
//	,HW1F_NSP_INIT_0.04
//	,HW1F_NSP_INIT_0.05
	,HW1F_NSP_SPOT_DN (EIrModel.HW1F)
	,HW1F_NSP_SPOT_UP (EIrModel.HW1F)
	,HW1F_NSP_SWPN_DN (EIrModel.HW1F)
	,HW1F_NSP_SWPN_UP (EIrModel.HW1F);

	private final EIrModel upperIrModel ; 
	
	private EIrModel(EIrModel aa) {
		this.upperIrModel = aa;
	}
	
	private EIrModel() {
		this.upperIrModel = null;
	}
	
	// (HW1F_NSP, HW1F_SP) -> HW1F 
	public EIrModel getIrModel(EIrModel inIrModel) {
		return upperIrModel ;
	}
	
}
