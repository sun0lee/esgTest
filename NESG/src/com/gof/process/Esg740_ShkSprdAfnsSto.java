package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

//import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.model.AFNelsonSiegel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg740_ShkSprdAfnsSto extends Process {	
	
	public static final Esg740_ShkSprdAfnsSto INSTANCE = new Esg740_ShkSprdAfnsSto();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 충격시나리 스프레드 생성 for TVOG 산출  </br> 
	 * @param 
	 * @See getAfnsResultList
	 * */
	public static Map<String, List<?>> createAfnsShockScenario(String bssd
														 	 , double[]          inTenor
														 	 , IrParamModel     irModelMst
														 	 , IrParamSw        irParamSw  
														 	 , Map<String, String>  argInDBMap 
														 	 , List<IrParamAfnsCalc> optInput  // add 
														 	 , EIrModel irModelNm // add AFNS_STO 구분을 위해서 가져왔음. 
														 	 )	
	{		
		Map<String, List<?>>  resultMap  = new TreeMap<String, List<?>>();
		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
		
		List<IrParamAfnsCalc> inOptParam = optInput.stream().filter(param -> param.getParamTypCd().getParamDv().equals("paras"))
			                               .collect(Collectors.toList());
		
		List<IrParamAfnsCalc> inOptLsc  = optInput.stream().filter(param -> param.getParamTypCd().getParamDv().equals("LSC"))
                .collect(Collectors.toList());
		
		AFNelsonSiegel afns = new AFNelsonSiegel(bssd , inTenor, irModelMst,irModelNm, irParamSw, argInDBMap, 1 ,470) ;
		
		// tvog 생성용 시나리오 만들기 
		afns.genAfnsStoShock(inOptParam , inOptLsc);
		irShock.       addAll(afns.getAfnsShockList());
		
		// fk 값 추가 
		irShock.     stream().forEach(s -> s.setIrParamModel(irModelMst));
		irShock.     stream().forEach(s -> s.setIrCurve(irModelMst.getIrCurve()));
		irShock.     stream().forEach(s -> s.setIrCurveNm(irModelMst.getIrCurveNm())); // 금리 정보가 비어있으면 irCurveNm이 null임 
		irShock.     stream().forEach(s -> s.setModifiedBy(jobId));

		resultMap.put("SHOCK",  irShock);
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
		
		return resultMap;
	}
	
	
}
