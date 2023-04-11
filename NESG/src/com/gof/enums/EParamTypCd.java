package com.gof.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum EParamTypCd {
	
  // HW1F
	ALPHA  (EIrModel.HW1F)
  , SIGMA  (EIrModel.HW1F)
  , THETA  (EIrModel.HW1F)
  , COST   (EIrModel.HW1F)
  , R_ZERO (EIrModel.HW1F)
  
  // AFNS optParaNames
  , LAMBDA   (EIrModel.AFNS,"optParas") // 1e-1  0
  , THETA_1  (EIrModel.AFNS,"optParas") // 1e-2  1
  , THETA_2  (EIrModel.AFNS,"optParas") //-1e-3  2
  , THETA_3  (EIrModel.AFNS,"optParas") //-1e-3  3
  , KAPPA_1  (EIrModel.AFNS,"optParas") // 1e-1  4
  , KAPPA_2  (EIrModel.AFNS,"optParas") // 1e-1  5
  , KAPPA_3  (EIrModel.AFNS,"optParas") // 1e-1  6
  , SIGMA_11 (EIrModel.AFNS,"optParas") // 1e-2  7
  , SIGMA_21 (EIrModel.AFNS,"optParas") // 0e-2  8
  , SIGMA_22 (EIrModel.AFNS,"optParas") // 1e-2  9
  , SIGMA_31 (EIrModel.AFNS,"optParas") // 0e-2  10
  , SIGMA_32 (EIrModel.AFNS,"optParas") //-1e-2  11
  , SIGMA_33 (EIrModel.AFNS,"optParas") // 1e-2  12
  , EPSILON  (EIrModel.AFNS,"optParas") // 1e-1  13
    
  // AFNS optLSCNames
  , L0       (EIrModel.AFNS,"optLSC")
  , S0       (EIrModel.AFNS,"optLSC")
  , C0       (EIrModel.AFNS,"optLSC");

	
	private EIrModel irModel ; 
	private  String paramDv ;
	
	EParamTypCd(EIrModel irModel) {
		this.irModel = irModel ;
	}

	EParamTypCd(EIrModel inIrModel, String inParaDv) {
		this.irModel = inIrModel ;
		this.paramDv = inParaDv ; 
	}

	
	public static List<EParamTypCd> getParamList( EIrModel inIrModel) {
		List<EParamTypCd> paramList = new ArrayList<EParamTypCd>();
		paramList = Stream.of(EParamTypCd.values()).filter(s->s.irModel==inIrModel).collect(Collectors.toList());
		return paramList ;
	}
	
	public static List<EParamTypCd> getParamList( EIrModel inIrModel , String inParamDv) {
		List<EParamTypCd> paramList = new ArrayList<EParamTypCd>();
		paramList = Stream.of(EParamTypCd.values()).filter(s->s.irModel==inIrModel && s.paramDv == inParamDv).collect(Collectors.toList());
		return paramList ;
	}

}


/*
			this.optParas[0]  = 0.4397764671040283;
			this.optParas[1]  = 0.03238093323059146;
			this.optParas[2]  = -0.01816932435509963;
			this.optParas[3]  = -0.0012340100084967927;
			this.optParas[4]  =  0.07011881997655274;
			this.optParas[5]  =  0.31428423540308786;
			this.optParas[6]  =  0.41032947646397744;
			this.optParas[7]  =  0.004594675150093352;
			this.optParas[8]  = -0.004372406977548432;
			this.optParas[9]  =  0.0027771993513785245;
			this.optParas[10] =  6.773607124233114E-4;
			this.optParas[11] = -5.426876995115856E-4;
			this.optParas[12] =  0.00976325443053842;
			this.optParas[13] =  0.38292135421347995;
			
			this.optLSC[0]    =  0.01935128249313093;
			this.optLSC[1]    = -0.00667992698106652;
			this.optLSC[2]    = -0.004801227043622508;

 
"EPSILON1", "EPSILON2", "EPSILON3" , "EPSILON4" , "EPSILON5" , "EPSILON6" , "EPSILON7" ,
"EPSILON8", "EPSILON9", "EPSILON10", "EPSILON11", "EPSILON12", "EPSILON13", "EPSILON14" };
*/
 