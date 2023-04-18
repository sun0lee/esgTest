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
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Deprecated
public class Esg260_IrDcntRateBu extends Process {	
	
	public static final Esg260_IrDcntRateBu INSTANCE = new Esg260_IrDcntRateBu();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrDcntRateBu> setIrDcntRateBu(String bssd, EIrModel irModelNm, EApplBizDv applBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>> map) {	
		
		List<IrDcntRateBu> rst = new ArrayList<IrDcntRateBu>();
		
		for(Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : map.entrySet()) {		
			IrCurve irCurve = curveSwMap.getKey();
			String irCurveNm = irCurve.getIrCurveNm();
			
			List<IrCurveSpot> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm);
			
			spotList.forEach(s-> log.info("zzzz : {},{}",  s.toString()));
			
			TreeMap<String, Double> spotMap = spotList.stream().collect(Collectors.toMap(IrCurveSpot::getMatCd, IrCurveSpot::getSpotRate, (k, v) -> k, TreeMap::new));
			
			if(spotList.isEmpty()) {
				log.warn("No IR Curve Spot Data [BIZ: {}, IR_CURVE_NM: {}] in [{}] for [{}]", applBizDv, irCurveNm, toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}

			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				EDetSce irCurveSce = swSce.getKey();
				IrParamSw irParamSw = swSce.getValue();

				// (biz, irCurveNm) 만기별 유동성프리미엄 
				Map<String, Double> irSprdLpMap = IrSprdDao.getIrSprdLpBizList(bssd, applBizDv, irCurveNm, irCurveSce).stream()
						                                   .collect(Collectors.toMap(IrSprdLpBiz::getMatCd, IrSprdLpBiz::getLiqPrem));

//				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelId, irCurveNm, irCurveSceNo).stream()
//															.collect(Collectors.toMap(IrSprdAfnsBiz::getMatCd, IrSprdAfnsBiz::getShkSprdCont));				
				// (biz, irCurveNm) 만기별 충격스프레드 
				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelNm, irCurveNm, irParamSw.getShkSprdSceNo()).stream()
															.collect(Collectors.toMap(IrSprdAfnsBiz::getMatCd, IrSprdAfnsBiz::getShkSprdCont));				
				
				//TODO: shallow copy vs deep copy
//				List<IrCurveSpot> spotSceList = spotList;
				List<IrCurveSpot> spotSceList = spotList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());
				
				String fwdMatCd = irParamSw.getFwdMatCd();				
				if(!fwdMatCd.equals("M0000")) {					
					Map<String, Double> fwdSpotMap = irSpotDiscToFwdMap(bssd, spotMap, fwdMatCd);					
					spotSceList.stream().forEach(s -> s.setSpotRate(fwdSpotMap.get(s.getMatCd())));					
				}				

				String pvtMatCd = irParamSw.getPvtRateMatCd();
//				double pvtRate  = StringUtil.objectToPrimitive(spotMap.getOrDefault(pvtMatCd, 0.0), 0.0    );				
				double pvtRate  = spotMap.getOrDefault(pvtMatCd, 0.0);				
				double pvtMult  = irParamSw.getMultPvtRate();				
//				double intMult  = irParamSw.getMultIntRate();				
				double addSprd  = irParamSw.getAddSprd();
				int    llp      = irParamSw.getLlp();				
				
//				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, irCurveNm, irCurveSceNo, pvtMatCd, pvtRate, pvtMult, intMult, addSprd, llp);
				for(IrCurveSpot spot : spotSceList) {				
					if(Integer.valueOf(spot.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrDcntRateBu dcntRateBu = new IrDcntRateBu();						
						//TODO:
						int kicsAddSprdContSceNo = 12;
						
						double baseSpot = pvtMult * (spot.getSpotRate() - pvtRate) +  pvtRate + addSprd  ;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
						double baseSpotCont = irDiscToCont(baseSpot);
//						if(irParamSw.getApplBizDv().equals("SAAS")) {
//							log.info("{}, {}, {}, {}, {}, {}, {}, {}", llp, spot.getMatCd(), spot.getSpotRate(), addSprd2, intMult, baseSpot, baseSpotCont, irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0));
//						}		
						
						double shkCont      = (applBizDv.equals(EApplBizDv.KICS) && irCurveSce.getSceNo() <= kicsAddSprdContSceNo) ? 
											    irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) + addSprd 
											  : irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0);
						double lpDisc       = irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0);
						
						double spotCont     = baseSpotCont + shkCont;
						double spotDisc     = irContToDisc(spotCont);						
						double adjSpotDisc  = spotDisc + lpDisc;
						double adjSpotCont  = irDiscToCont(adjSpotDisc);						
						
						dcntRateBu.setBaseYymm(bssd);
						dcntRateBu.setApplBizDv(applBizDv);
						dcntRateBu.setIrCurveNm(irCurveNm);
						dcntRateBu.setIrCurve(irCurve); 
						dcntRateBu.setIrCurveSceNo(irCurveSce);
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

