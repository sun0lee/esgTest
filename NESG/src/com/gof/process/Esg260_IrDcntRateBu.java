package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdAfnsBiz;
import com.gof.entity.IrSprdLpBiz;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg260_IrDcntRateBu extends Process {	
	
	public static final Esg260_IrDcntRateBu INSTANCE = new Esg260_IrDcntRateBu();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrDcntRateBu> setIrDcntRateBu(String bssd, EIrModel irModelNm, EApplBizDv applBizDv, Map<IrCurve, Map<Integer, IrParamSw>> map) {	
		
		List<IrDcntRateBu> rst = new ArrayList<IrDcntRateBu>();
		
		for(Entry<IrCurve, Map<Integer, IrParamSw>> curveSwMap : map.entrySet()) {		
			String irCurveNm = curveSwMap.getKey().getIrCurveNm();
			IrCurve irCurve = curveSwMap.getKey();
			List<IrCurveSpot> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm);
//			List<IrCurveSpot> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm, 20);	//for special purpose to fix llp to 20yrs(temp)
//			log.info("{}", spotList);
			
			TreeMap<String, Double> spotMap = spotList.stream().collect(Collectors.toMap(IrCurveSpot::getMatCd, IrCurveSpot::getSpotRate, (k, v) -> k, TreeMap::new));
			
			if(spotList.isEmpty()) {
				log.warn("No IR Curve Spot Data [BIZ: {}, IR_CURVE_NM: {}] in [{}] for [{}]", applBizDv, irCurveNm, toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}

			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				
				// (biz, irCurveNm) 만기별 유동성프리미엄 
				Map<String, Double> irSprdLpMap = IrSprdDao.getIrSprdLpBizList(bssd, applBizDv, irCurveNm, swSce.getKey()).stream()
						                                   .collect(Collectors.toMap(IrSprdLpBiz::getMatCd, IrSprdLpBiz::getLiqPrem));

//				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelId, irCurveNm, swSce.getKey()).stream()
//															.collect(Collectors.toMap(IrSprdAfnsBiz::getMatCd, IrSprdAfnsBiz::getShkSprdCont));				
				// (biz, irCurveNm) 만기별 충격스프레드 
				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelNm, irCurveNm, StringUtil.objectToPrimitive(swSce.getValue().getShkSprdSceNo(), 1)).stream()
															.collect(Collectors.toMap(IrSprdAfnsBiz::getMatCd, IrSprdAfnsBiz::getShkSprdCont));				
				
				//TODO: shallow copy vs deep copy
//				List<IrCurveSpot> spotSceList = spotList;
				List<IrCurveSpot> spotSceList = spotList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());
				
				String fwdMatCd = swSce.getValue().getFwdMatCd();				
				if(!fwdMatCd.equals("M0000")) {					
					Map<String, Double> fwdSpotMap = irSpotDiscToFwdMap(bssd, spotMap, fwdMatCd);					
					spotSceList.stream().forEach(s -> s.setSpotRate(fwdSpotMap.get(s.getMatCd())));					
				}				

				String pvtMatCd = swSce.getValue().getPvtRateMatCd();
//				double pvtRate  = StringUtil.objectToPrimitive(spotMap.getOrDefault(pvtMatCd, 0.0), 0.0    );				
				double pvtRate  = spotMap.getOrDefault(pvtMatCd, 0.0);				
				double pvtMult  = swSce.getValue().getMultPvtRate();				
//				double intMult  = swSce.getValue().getMultIntRate();				
				double addSprd  = swSce.getValue().getAddSprd();
				int    llp      = swSce.getValue().getLlp();				
				
//				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, irCurveNm, swSce.getKey(), pvtMatCd, pvtRate, pvtMult, intMult, addSprd, llp);
				for(IrCurveSpot spot : spotSceList) {				
					if(Integer.valueOf(spot.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrDcntRateBu dcntRateBu = new IrDcntRateBu();						
						//TODO:
						int kicsAddSprdContSceNo = 12;
						if(bssd.equals("202012")) kicsAddSprdContSceNo =  6;
						if(bssd.equals("202112")) kicsAddSprdContSceNo = 12;						
						
//						double addSprd2 = addSprd;                                                                              //Parallel Shift Rate of KICS 6.0 Sensitivity Scenario is Discrete(Below is Continuous) 
//						double addSprd2 = (applBizDv.equals("KICS") && swSce.getKey() <= kicsAddSprdContSceNo) ? 0.0 : addSprd;						
//						double baseSpot = intMult * (StringUtil.objectToPrimitive(spot.getSpotRate()) + pvtMult * pvtRate) + addSprd2 + pvtRate * 0;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
//						double baseSpotCont = irDiscToCont(baseSpot);
						
//						double baseSpot = pvtMult * (StringUtil.objectToPrimitive(spot.getSpotRate()) - pvtRate) +  pvtRate + addSprd  ;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
						double baseSpot = pvtMult * (spot.getSpotRate() - pvtRate) +  pvtRate + addSprd  ;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
//						double baseSpotCont = baseSpot;					
						double baseSpotCont = irDiscToCont(baseSpot);
//						if(swSce.getValue().getApplBizDv().equals("SAAS")) {
//							log.info("{}, {}, {}, {}, {}, {}, {}, {}", llp, spot.getMatCd(), spot.getSpotRate(), addSprd2, intMult, baseSpot, baseSpotCont, irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0));
//						}		
						
//						double shkCont      = irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0);
//						double shkCont      = applBizDv.equals("KICS") ? irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) : 0.0;  //Parallel Shift is Discrete Rate (Below is Continuous)
						double shkCont      = (applBizDv.equals(EApplBizDv.KICS) && swSce.getKey() <= kicsAddSprdContSceNo) ? irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) + addSprd : irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0);
						double lpDisc       = irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0);
						
						double spotCont     = baseSpotCont + shkCont;
						double spotDisc     = irContToDisc(spotCont);						
						double adjSpotDisc  = spotDisc + lpDisc;
						double adjSpotCont  = irDiscToCont(adjSpotDisc);						
						
						dcntRateBu.setBaseYymm(bssd);
						dcntRateBu.setApplBizDv(applBizDv);
						dcntRateBu.setIrCurveNm(irCurveNm);
						dcntRateBu.setIrCurve(irCurve); 
						dcntRateBu.setIrCurveSceNo(swSce.getKey());
						dcntRateBu.setMatCd(spot.getMatCd());						
						dcntRateBu.setSpotRateDisc(spotDisc);
						dcntRateBu.setSpotRateCont(spotCont);
						dcntRateBu.setLiqPrem(lpDisc);
						dcntRateBu.setAdjSpotRateDisc(adjSpotDisc);
						dcntRateBu.setAdjSpotRateCont(adjSpotCont);
						dcntRateBu.setAddSprd(addSprd);
						dcntRateBu.setModifiedBy(jobId);						
						dcntRateBu.setUpdateDate(LocalDateTime.now());						
						
						rst.add(dcntRateBu);
					}					
				}
			}
		}		
		log.info("{}({}) creates [{}] results of [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrDcntRateBu.class.getSimpleName()));
		
		return rst;		
	}		

}

