package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdCurve;
import com.gof.entity.IrSprdLp;
import com.gof.entity.IrSprdLpUsr;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EDetSce;
import com.gof.enums.EJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg240_LpSprd extends Process {	
	
	public static final Esg240_LpSprd INSTANCE = new Esg240_LpSprd();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	

	public static List<IrSprdLp> setLpFromSwMap(String bssd
											  , EApplBizDv applBizDv
											  , Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap) {
		 										// irCurve , ScenNo , IrParamSw
		
		List<IrSprdLp> rst = new ArrayList<IrSprdLp>();
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			
			List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, curveSwMap.getKey().getIrCurveNm());
			if(tenorList.isEmpty()) {
				log.warn("No IR Curve Data [IR_CURVE_NM: {}] in [{}] for [{}]", curveSwMap.getKey(), toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}
			
			// swSce , IrParamSw
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				EDetSce irCurveSce = swSce.getKey();
				IrParamSw irParamSw = swSce.getValue();

				
				int llp = irParamSw.getLlp();
				for(String tenor : tenorList) {					
//					log.info("tenor: {}, {}, {}", tenor, tenor.substring(1), irParamSw.getLlp());					
					
					if(Integer.valueOf(tenor.substring(1)) <=  llp * MONTH_IN_YEAR) {						
						
						IrSprdLp lp1 = new IrSprdLp();
						
						lp1.setBaseYymm(bssd);
						lp1.setDcntApplModelCd("BU1");
						lp1.setApplBizDv(applBizDv);
						lp1.setIrCurveNm(curveSwMap.getKey().getIrCurveNm());
						lp1.setIrCurve(curveSwMap.getKey());
						lp1.setIrCurveSceNo(irCurveSce);
						lp1.setMatCd(tenor);
						lp1.setLiqPrem(irParamSw.getLiqPrem());
						lp1.setModifiedBy(jobId);						
						lp1.setUpdateDate(LocalDateTime.now());
						
						rst.add(lp1);
					}					
				}
			}
		}
		log.info("{}({}) creates [{}] results of [{}] (from SW Param). They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLp.class.getSimpleName()));
		
		return rst;
	}
	
	
	public static List<IrSprdLp> setLpFromCrdSprd(String bssd, EApplBizDv applBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap, String lpCurveId) {
		
		List<IrSprdLp> rst = new ArrayList<IrSprdLp>();
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {			
			
			List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, curveSwMap.getKey().getIrCurveNm());
			if(tenorList.isEmpty()) {
				log.warn("No IR Curve Data [IR_CURVE_NM: {}] in [{}] for [{}]", curveSwMap.getKey(), toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}
			
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				EDetSce irCurveSce = swSce.getKey();
				IrParamSw irParamSw = swSce.getValue();


				int llp = irParamSw.getLlp();				
				for(IrSprdCurve lpCrv : IrCurveSpotDao.getIrSprdCurve(bssd, lpCurveId)) {
					if(Integer.valueOf(lpCrv.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrSprdLp lp2 = new IrSprdLp();
						
						lp2.setBaseYymm(bssd);
						lp2.setDcntApplModelCd("BU2");
						lp2.setApplBizDv(applBizDv);
						lp2.setIrCurveNm(curveSwMap.getKey().getIrCurveNm());
						lp2.setIrCurve(curveSwMap.getKey());
						lp2.setIrCurveSceNo(irCurveSce);
						lp2.setMatCd(lpCrv.getMatCd());
						lp2.setLiqPrem(lpCrv.getCrdSprd());
						lp2.setModifiedBy(jobId);						
						lp2.setUpdateDate(LocalDateTime.now());
						
						rst.add(lp2);
					}					
				}
			}
		}
		log.info("{}({}) creates [{}] results of [{}] (from Credit Spread). They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLp.class.getSimpleName()));
		
		return rst;
	}
	
	
	public static List<IrSprdLp> setLpFromUsr(String bssd, EApplBizDv applBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap) {
		
		List<IrSprdLp> rst = new ArrayList<IrSprdLp>();
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {	
			
			IrCurve irCurve = curveSwMap.getKey() ;
			String irCurveNm = curveSwMap.getKey().getIrCurveNm() ;
			
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				EDetSce  irCurveSce = swSce.getKey();
				IrParamSw irParamSw = swSce.getValue();


				int llp = irParamSw.getLlp();				
				List<IrSprdLpUsr> lpUsr = IrSprdDao.getIrSprdLpUsrList(bssd, applBizDv, irCurveNm, irCurveSce);
				
				for(IrSprdLpUsr usr : lpUsr) {
					if(Integer.valueOf(usr.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrSprdLp lp3 = new IrSprdLp();
						
						lp3.setBaseYymm(bssd);
						lp3.setDcntApplModelCd("BU3");
						lp3.setApplBizDv(applBizDv);
						lp3.setIrCurveNm(irCurveNm);
						lp3.setIrCurve(irCurve);
						lp3.setIrCurveSceNo(irCurveSce);
						lp3.setMatCd(usr.getMatCd());
						lp3.setLiqPrem(usr.getLiqPrem());
						lp3.setModifiedBy(jobId);						
						lp3.setUpdateDate(LocalDateTime.now());
						
						rst.add(lp3);
					}					
				}
			}
		}
		log.info("{}({}) creates [{}] results of [{}] (from User Defined). They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLp.class.getSimpleName()));
		
		return rst;
	}	

}

