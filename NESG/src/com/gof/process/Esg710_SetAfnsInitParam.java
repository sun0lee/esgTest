package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.model.AFNelsonSiegel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg710_SetAfnsInitParam extends Process {	
	
	public static final Esg710_SetAfnsInitParam INSTANCE = new Esg710_SetAfnsInitParam();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 충격시나리오 : 초기모수 산출 </br> 
	 * @param 
	 * */
	public static Map<String, List<?>> setAfnsInitParam(String bssd
												 	  , List<IRateInput> curveHisList
												 	  , List<IRateInput> curveBaseList
												 	  , IrParamModel irModelMst // add
												 	  , IrParamSw    irParamSw  // add
												 	  , Map<String, String>  argInDBMap // add 
														 	 )	
	{		
		Map<String, List<?>>  resultMap  = new TreeMap<String, List<?>>(); 
		List<IrParamAfnsCalc> irAfnsInitParam    = new ArrayList<IrParamAfnsCalc>();
		
		
		AFNelsonSiegel afns = new AFNelsonSiegel(bssd, curveHisList, curveBaseList, irModelMst, irParamSw, argInDBMap) ;
		
		// 초기 모수 산출
		afns.getinitialAfnsParas();
		
		// 산출한 초기모수를 IrParamAfnsCalc 에 담기
		irAfnsInitParam.  addAll(afns.setAfnsInitParamList());
		
		// fk 값 추가 
		irAfnsInitParam.stream().forEach(s -> s.setIrParamModel(irModelMst));
		irAfnsInitParam.stream().forEach(s -> s.setIrCurve(irModelMst.getIrCurve()));
		irAfnsInitParam.stream().forEach(s -> s.setModifiedBy(jobId));

		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irAfnsInitParam.size(), toPhysicalName(IrParamAfnsCalc.class.getSimpleName()));

		resultMap.put("PARAM", irAfnsInitParam);
		
		return resultMap;
	}
	
	
}
