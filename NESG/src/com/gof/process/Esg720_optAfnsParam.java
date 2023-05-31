package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.entity.IrParamAfnsBiz;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrDcntSceDetBiz;
import com.gof.model.AFNelsonSiegel;
import com.gof.model.IrModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg720_optAfnsParam extends Process {	
	
	public static final Esg720_optAfnsParam INSTANCE = new Esg720_optAfnsParam();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 모수 최적화 by Kalman filter </br> 
	 * @param 
	 * @See getAfnsResultList
	 * */
	public static Map<String, List<?>> optimizationParas(String bssd
														 	 , List<IRateInput> curveHisList
														 	 , List<IRateInput> curveBaseList
														 	 , IrParamModel irModelMst // add
														 	 , IrParamSw    irParamSw  // add
														 	 , Map<String, String>  argInDBMap // add 
														 	 , List<IrParamAfnsCalc> initParam // add input 
														 	 )	
	{		
		Map<String, List<?>>  resultMap  = new TreeMap<String, List<?>>();
		List<IrParamAfnsCalc> irOptParam    = new ArrayList<IrParamAfnsCalc>();
		
		
		AFNelsonSiegel afns = new AFNelsonSiegel(bssd, curveHisList, curveBaseList, irModelMst, irParamSw, argInDBMap) ;
		
		// 모수 최적화
		afns.optimizationParas(initParam);
		
		// 최적화한 모수를 IrParamAfnsCalc 에 담기
		irOptParam.  addAll(afns.getAfnsParamList());

		
		// fk 값 추가 
		irOptParam.stream().forEach(s -> s.setIrParamModel(irModelMst));
		irOptParam.stream().forEach(s -> s.setIrCurve(irModelMst.getIrCurve()));
		irOptParam.stream().forEach(s -> s.setModifiedBy(jobId));

		resultMap.put("PARAM",  irOptParam);
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irOptParam.size(), toPhysicalName(IrParamAfnsCalc.class.getSimpleName()));
		
		return resultMap;
	}
	
	
}
