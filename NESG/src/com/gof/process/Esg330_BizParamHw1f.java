package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gof.dao.IrParamHwDao;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwCalc;
import com.gof.entity.IrParamHwUsr;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.enums.EParamTypCd;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg330_BizParamHw1f extends Process {
	
	public static final Esg330_BizParamHw1f INSTANCE = new Esg330_BizParamHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrParamHwBiz> createBizHw1fParam(String bssd, EApplBizDv applBizDv, EIrModel irModelNm, String irCurveNm, int hwAlphaAvgNum, String hwAlphaAvgMatCd, int hwSigmaAvgNum, String hwSigmaAvgMatCd) {
		
		List<IrParamHwBiz>  paramHwBiz  = new ArrayList<IrParamHwBiz>();
		List<IrParamHwUsr>  paramHwUsr  = IrParamHwDao.getIrParamHwUsrList(bssd, applBizDv,irModelNm.name(), irCurveNm);		
		List<IrParamHwCalc> paramHwCalc = IrParamHwDao.getIrParamHwCalcList(bssd, irCurveNm);   //just counting in E_IR_PARAM_HW_CALC
		
		if(!paramHwUsr.isEmpty()) {			
			paramHwBiz = paramHwUsr.stream().map(s -> s.convert()).collect(Collectors.toList());
			log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramHwBiz.size(), toPhysicalName(IrParamHwUsr.class.getSimpleName()), toPhysicalName(IrParamHwBiz.class.getSimpleName()));			
		}
		else if(applBizDv.equals(EApplBizDv.KICS) && !paramHwCalc.isEmpty()) {			
			paramHwBiz = calcBizHw1fParam(bssd, applBizDv, irModelNm, irCurveNm, hwAlphaAvgNum, hwAlphaAvgMatCd, hwSigmaAvgNum, hwSigmaAvgMatCd);			
			log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramHwBiz.size(), toPhysicalName(IrParamHwCalc.class.getSimpleName()), toPhysicalName(IrParamHwBiz.class.getSimpleName()));
		}
		else {
			if(!paramHwCalc.isEmpty()) {				
				paramHwBiz = calcBizHw1fParam(bssd, applBizDv, irModelNm, irCurveNm, hwAlphaAvgNum, hwAlphaAvgMatCd, hwSigmaAvgNum, hwSigmaAvgMatCd);				
				log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramHwBiz.size(), toPhysicalName(IrParamHwCalc.class.getSimpleName()), toPhysicalName(IrParamHwBiz.class.getSimpleName()));				
			}
			else {
				log.warn("{}({}) No Model Parameter from Hull-White 1 Factor Model in [Model:{}, ID:{}]", jobId, EJob.valueOf(jobId).getJobName(), irModelNm, irCurveNm);
			}
		}		
		return paramHwBiz;
	}
	
	
	private static List<IrParamHwBiz> calcBizHw1fParam(String bssd, EApplBizDv applBizDv, EIrModel irModelNm, String irCurveNm, int hwAlphaAvgNum, String hwAlphaAvgMatCd, int hwSigmaAvgNum, String hwSigmaAvgMatCd) {		
		
		List<IrParamHwBiz>  paramHwBiz  = new ArrayList<IrParamHwBiz>();
		List<IrParamHwCalc> paramHwCalc = IrParamHwDao.getIrParamHwCalcList(bssd,  EIrModel.valueOf(irModelNm + "_NSP"), irCurveNm);

		for(IrParamHwCalc calc : paramHwCalc) {
//			if(calc.getParamTypCd().equals("COST")) continue;
			if(calc.getParamTypCd()==EParamTypCd.COST) continue;
			
			IrParamHwBiz biz = new IrParamHwBiz();			
			biz.setBaseYymm(bssd);
			biz.setApplBizDv(applBizDv);
			biz.setIrModelNm(irModelNm);
			biz.setIrCurveNm(irCurveNm);
			biz.setMatCd(calc.getMatCd());
			biz.setParamTypCd(calc.getParamTypCd());			
			biz.setParamVal(calc.getParamVal());
			biz.setModifiedBy(jobId);
			biz.setUpdateDate(LocalDateTime.now());
			
			paramHwBiz.add(biz);
		}		
						
		paramHwBiz.addAll(createBizAppliedParameterOuter(bssd, applBizDv, irModelNm, irCurveNm, EParamTypCd.ALPHA, hwAlphaAvgNum, hwAlphaAvgMatCd));
		paramHwBiz.addAll(createBizAppliedParameterOuter(bssd, applBizDv, irModelNm, irCurveNm, EParamTypCd.SIGMA, hwSigmaAvgNum, hwSigmaAvgMatCd));		
		
		if(applBizDv.equals(EApplBizDv.KICS)) paramHwBiz.stream().forEach(s -> log.info("PARAM BIZ from CALC: [{}, {}, {}, {}], {}", s.getIrModelNm(), s.getApplBizDv(), s.getParamTypCd(), s.getMatCd(), s.getParamVal()));

		return paramHwBiz;
	}
	
	
	private static List<IrParamHwBiz> createBizAppliedParameterOuter(String bssd, EApplBizDv applBizDv, EIrModel irModelNm, String irCurveNm, EParamTypCd paramTypCd, int monthNum, String matCd) {
		
		List<IrParamHwCalc> paramCalcHisList = new ArrayList<IrParamHwCalc>();
		if(paramTypCd==EParamTypCd.ALPHA) {
			paramCalcHisList = IrParamHwDao.getIrParamHwCalcHisList(bssd, EIrModel.valueOf( irModelNm + "_SP"), irCurveNm, paramTypCd, monthNum, matCd);
			paramCalcHisList.forEach(s ->log.info("aaa : {}, {}", s.getBaseYymm(), s.getParamVal()));
		}
		else {
			paramCalcHisList = IrParamHwDao.getIrParamHwCalcHisList(bssd, EIrModel.valueOf(irModelNm + "_NSP"), irCurveNm, paramTypCd, monthNum, matCd);
		}		 
		
		List<IrParamHwBiz> paramHwBiz = new ArrayList<IrParamHwBiz>();
		IrParamHwBiz biz = new IrParamHwBiz();
		
		
		biz.setBaseYymm(bssd);
		biz.setApplBizDv(applBizDv);
		biz.setIrModelNm(irModelNm);
		biz.setIrCurveNm(irCurveNm);
		biz.setParamTypCd(paramTypCd);
		biz.setMatCd("M1200");
		biz.setParamVal(paramCalcHisList.stream().collect(Collectors.averagingDouble(s -> s.getParamVal())));
		biz.setModifiedBy(jobId);
		biz.setUpdateDate(LocalDateTime.now());
		paramHwBiz.add(biz);
		
		log.info("{}({}) ", biz.getParamTypCd(), biz.getParamVal() );
		return paramHwBiz;
	}
	
	
//	private static List<IrParamHwBiz> calcBizHw1fParam(String bssd, String applBizDv, String irModelId, String irCurveId, int hwAlphaAvgNum, String hwAlphaAvgMatCd, int hwSigmaAvgNum, String hwSigmaAvgMatCd) {
//
//		List<IrParamHwBiz>  paramHwBiz  = new ArrayList<IrParamHwBiz>();
//		List<IrParamHwCalc> paramHwCalc = IrParamHwDao.getIrParamHwCalcList(bssd, irModelId, irCurveId);
//			
//		for(IrParamHwCalc calc : paramHwCalc) {
//			
//			IrParamHwBiz biz = new IrParamHwBiz();			
//			biz.setBaseYymm(bssd);
//			biz.setApplBizDv(applBizDv);
//			biz.setIrModelId(irModelId);
//			biz.setIrCurveId(irCurveId);
//			biz.setMatCd(calc.getMatCd());
//			biz.setParamTypCd(calc.getParamTypCd());			
//			biz.setParamVal(calc.getParamVal());
//			biz.setLastModifiedBy(jobId);
//			biz.setLastUpdateDate(LocalDateTime.now());
//			
//			paramHwBiz.add(biz);
//		}		
//						
//		paramHwBiz.addAll(createBizAppliedParameterOuter(bssd, applBizDv, irModelId, irCurveId, "ALPHA", hwAlphaAvgNum, hwAlphaAvgMatCd));
//		paramHwBiz.addAll(createBizAppliedParameterOuter(bssd, applBizDv, irModelId, irCurveId, "SIGMA", hwSigmaAvgNum, hwSigmaAvgMatCd));
//		
//		double alpha1 = 0.0001;
//		//cloning ALPHA@M0120 to ALPHA@M0240
//		for(IrParamHwBiz biz : paramHwBiz) {
//			if(biz.getParamTypCd().equals("ALPHA") && biz.getMatCd().equals("M0120")) {
//				alpha1 = biz.getParamVal();
//				break;
//			}				
//		}						
//		
//		//update ALPHA@M0120 to ALPHA@M0240
//		for(IrParamHwBiz biz : paramHwBiz) {
//			if(biz.getParamTypCd().equals("ALPHA") && biz.getMatCd().equals(hwAlphaAvgMatCd)) {
//				biz.setParamVal(alpha1);
//				break;
//			}				
//		}		
//		
//		//delete ALPHA@M0120 and SIGMA@M0240
//		List<IrParamHwBiz> bizList = new ArrayList<IrParamHwBiz>();			
//		for(IrParamHwBiz biz : paramHwBiz) {
//			if(biz.getParamTypCd().equals("ALPHA") && biz.getMatCd().equals("M0120")) bizList.add(biz);
//			if(biz.getParamTypCd().equals("SIGMA") && biz.getMatCd().equals("M0240")) bizList.add(biz);				
//		}			
//		for(IrParamHwBiz rm : bizList) {
//			paramHwBiz.remove(rm);				
//		}
//		
//		paramHwBiz.stream().forEach(s -> log.info("{}, {}, {}, {}", s.getApplBizDv(), s.getParamTypCd(), s.getMatCd(), s.getParamVal()));
//
//		return paramHwBiz;
//	}
//
//	
//	private static List<IrParamHwBiz> createBizAppliedParameterOuter(String bssd, String applBizDv, String irModelId, String irCurveId, String paramTypCd, int monthNum, String matCd) {
//		
//		List<IrParamHwCalc> paramCalcHisList = IrParamHwDao.getIrParamHwCalcHisList(bssd, irModelId, irCurveId, paramTypCd, monthNum, matCd);
//		
//		List<IrParamHwBiz> paramHwBiz = new ArrayList<IrParamHwBiz>();
//		IrParamHwBiz biz = new IrParamHwBiz();
//		
//		biz.setBaseYymm(bssd);
//		biz.setApplBizDv(applBizDv);
//		biz.setIrModelId(irModelId);
//		biz.setIrCurveId(irCurveId);
//		biz.setParamTypCd(paramTypCd);
//		biz.setMatCd("M1200");
//		biz.setParamVal(paramCalcHisList.stream().collect(Collectors.averagingDouble(s -> s.getParamVal())));
//		biz.setLastModifiedBy(jobId);
//		biz.setLastUpdateDate(LocalDateTime.now());
//		paramHwBiz.add(biz);
//		
//		return paramHwBiz;
//	}

}