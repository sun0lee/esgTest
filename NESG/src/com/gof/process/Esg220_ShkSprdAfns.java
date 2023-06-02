package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrDcntSceDetBiz;
import com.gof.model.AFNelsonSiegel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg220_ShkSprdAfns extends Process {	
	
	public static final Esg220_ShkSprdAfns INSTANCE = new Esg220_ShkSprdAfns();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 충격시나리오 생성 </br> 
	 * @param 
	 * @See getAfnsResultList
	 * */
	public static Map<String, List<?>> createAfnsShockScenario(String bssd
														 	 , List<IRateInput> curveHisList
														 	 , List<IRateInput> curveBaseList
														 	 , IrParamModel irModelMst // add
														 	 , IrParamSw    irParamSw  // add
														 	 , Map<String, String>  argInDBMap // add 
														 	 )	
	{		
		Map<String, List<?>>  irShockSenario  = new TreeMap<String, List<?>>();
		List<IrParamAfnsCalc> irShockParam    = new ArrayList<IrParamAfnsCalc>();
		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
		List<IrDcntSceDetBiz> irScenarioList  = new ArrayList<IrDcntSceDetBiz>();
		
		
		AFNelsonSiegel afns = new AFNelsonSiegel(bssd, curveHisList, curveBaseList, irModelMst, irParamSw, argInDBMap) ;
		
//		AFNelsonSiegelHetero afns = new AFNelsonSiegelHetero(IrModel.stringToDate(bssd), mode, null, curveHisList, curveBaseList,
//                								 	   		 true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
//                								 	   		 0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);
		
		irScenarioList.addAll(afns.getAfnsResultList());
		irShockParam.  addAll(afns.getAfnsParamList());
		irShock.       addAll(afns.getAfnsShockList());
		
		// fk 값 추가 
		irShockParam.stream().forEach(s -> s.setIrParamModel(irModelMst));
		irShock.     stream().forEach(s -> s.setIrParamModel(irModelMst));
		irShockParam.stream().forEach(s -> s.setIrCurve(irModelMst.getIrCurve()));
		irShock.     stream().forEach(s -> s.setIrCurve(irModelMst.getIrCurve()));
		irShockParam.stream().forEach(s -> s.setModifiedBy(jobId));
		irShock.     stream().forEach(s -> s.setModifiedBy(jobId));

//		irShockSenario.put("CURVE",  irScenarioList);
		irShockSenario.put("PARAM",  irShockParam);
		irShockSenario.put("SHOCK",  irShock);
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShockParam.size(), toPhysicalName(IrParamAfnsCalc.class.getSimpleName()));
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
		
		return irShockSenario;
	}
	
	//for inputParas
//	public static Map<String, List<?>> createAfnsShockScenarioByParam(String bssd
//			 														, List<IrParamAfnsBiz> inputParas
//			 														, List<IRateInput> curveBaseList
//			 														, List<String> tenorList
//			 														, List<IrParamModel> modelMst // add 
//													           	    ,  double dt, double initSigma, double ltfr, double ltfrT, int prjYear, double errorTolerance, int itrMax, double confInterval, double epsilon)	
//	{		
//		Map<String, List<?>>  irShockSenario  = new TreeMap<String, List<?>>();
//		List<IrParamAfnsCalc> irShockParam    = new ArrayList<IrParamAfnsCalc>();
//		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
//		List<IrDcntSceDetBiz> irScenarioList  = new ArrayList<IrDcntSceDetBiz>();				
//		
//		AFNelsonSiegel afns = new AFNelsonSiegel(IrModel.stringToDate(bssd), modelMst.get(0).getIrModelNm(), inputParas, curveBaseList, 
//				                                 true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
//				                                 0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);
//
//		irScenarioList.addAll(afns.getAfnsResultList());
//		irShockParam.  addAll(afns.getAfnsParamList());
//		irShock.       addAll(afns.getAfnsShockList());
//		
//		// fk 값 추가 
//		irShockParam.stream().forEach(s -> s.setIrParamModel(modelMst.get(0)));
//		irShock.     stream().forEach(s -> s.setIrParamModel(modelMst.get(0)));
//		irShockParam.stream().forEach(s -> s.setIrCurve(modelMst.get(0).getIrCurve()));
//		irShock.     stream().forEach(s -> s.setIrCurve(modelMst.get(0).getIrCurve()));
//		
////		log.info("{}", irShockParam.toString());
////		log.info("{}", irShock.toString());
//		
//		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShockParam.size(), toPhysicalName(IrParamAfnsBiz.class.getSimpleName()));
//		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
////		irScenarioList.stream().filter(s -> s.getSceNo().equals(1)).filter(s -> Integer.valueOf(s.getMatCd().substring(1, 5)) <= 12).forEach(s->log.warn("Arbitrage Free Nelson Siegle Scenario Result : {}", s.toString()));		
//
////		irShockSenario.put("CURVE",  irScenarioList);
//		irShockSenario.put("PARAM",  irShockParam);
//		irShockSenario.put("SHOCK",  irShock);
//		
//		return irShockSenario;
//	}	
	
}
