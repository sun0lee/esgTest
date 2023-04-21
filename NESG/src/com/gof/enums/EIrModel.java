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
	, HW1F_NSP_INIT_0 (EIrModel.HW1F)
	, HW1F_NSP_INIT_1 (EIrModel.HW1F)
	, HW1F_NSP_INIT_2 (EIrModel.HW1F)
	, HW1F_NSP_INIT_3 (EIrModel.HW1F)
	, HW1F_NSP_INIT_4 (EIrModel.HW1F)
	, HW1F_NSP_INIT_5 (EIrModel.HW1F)
	, HW1F_NSP_SPOT_DN (EIrModel.HW1F)
	, HW1F_NSP_SPOT_UP (EIrModel.HW1F)
	, HW1F_NSP_SWPN_DN (EIrModel.HW1F)
	, HW1F_NSP_SWPN_UP (EIrModel.HW1F);

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
