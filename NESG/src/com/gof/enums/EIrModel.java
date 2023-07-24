package com.gof.enums;

import lombok.Getter;

@Getter
public enum EIrModel {
	  AFNS ()
	, AFNS_IM ()
	, DNS  ()
	, HW1F ()
	, CIR  ()
	, CIR_Y5 ()
	, CIR_Y10 ()

	/* 내부모형에서 TVOG 산출용 */
	, AFNS_STO(EIrModel.AFNS_IM)
	
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

	private EIrModel upperIrModel ; 
	
	private EIrModel() {
		this.upperIrModel = null;
	}
	
	private EIrModel(EIrModel upperIrModel) {
		this.upperIrModel = upperIrModel;
	}
	
	public EIrModel getIrModel() {
		return upperIrModel ;
	}	
}
