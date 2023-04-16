package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

//import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrCurveYtmDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdAfnsBiz;
import com.gof.entity.IrSprdLpBiz;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg261_IrDcntRateBu_Ytm extends Process {	
	
	public static final Esg261_IrDcntRateBu_Ytm INSTANCE = new Esg261_IrDcntRateBu_Ytm();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/**
	 * @param bssd
	 * @param irModelNm
	 * @param applBizDv
	 * @param paramSwMap*/
	public static List<IrDcntRateBu> setIrDcntRateBu(String bssd, EIrModel irModelNm, EApplBizDv applBizDv, Map<IrCurve, Map<Integer, IrParamSw>> paramSwMap) {	
		
		List<IrDcntRateBu> rst = new ArrayList<IrDcntRateBu>();
		
		for(Map.Entry<IrCurve, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {	
			IrCurve irCurve  = curveSwMap.getKey();
			String irCurveNm = irCurve.getIrCurveNm();
			
			// ori ytm 
			List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd, irCurveNm);
//			ytmList.forEach(s-> log.info("ytm : {},{}", s.toString()));

			
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				Integer irCurveSceNo = swSce.getKey();
				IrParamSw irParamSw = swSce.getValue();
				
	// 1. ytm -> spot 변환 (ytm에 직접 스프레드를 반영, 10.0 추가된 up down 시나리오 산출 부분 확인)
				List<IRateInput> ytmAddList = ytmList.stream().map(s->s.addSpread(irParamSw.getYtmSpread())).collect(Collectors.toList());
//				ytmAddList.forEach(s-> log.info("ytm1 : {},{}", s.toString()));
				
				List<IrCurveSpot> spotList = Esg150_YtmToSpotSw.createIrCurveSpot(ytmAddList, irParamSw)
											.stream().map(s-> s.convertToCont()).collect(Collectors.toList());
		
				spotList.forEach(s-> s.setIrCurve(irCurve));
				spotList.forEach(s-> log.info("zzzz : {},{}", irCurveSceNo, s.toString()));
				
				TreeMap<String, Double> spotMap = spotList.stream()
														  .collect(Collectors.toMap(IrCurveSpot::getMatCd
																  				  , IrCurveSpot::getSpotRate
																  				  , (k, v) -> k
																  				  , TreeMap::new));
				
				if(spotList.isEmpty()) {
					log.warn("No IR Curve Spot Data [BIZ: {}, IR_CURVE_NM: {}] in [{}] for [{}]", applBizDv, irCurveNm, toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
					continue;
				}
	// 2. 유동성 프리미엄 가져오기 			
				Map<String, Double> irSprdLpMap = IrSprdDao.getIrSprdLpBizList(bssd, applBizDv, irCurveNm, irCurveSceNo).stream()
						                                   .collect(Collectors.toMap(IrSprdLpBiz::getMatCd, IrSprdLpBiz::getLiqPrem));
	// 3. 금리 충격스프레드 가져오기 시나리오번호도 디폴트 처리가 필요할까? 없으면 에러인데.
				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelNm, irCurveNm, StringUtil.objectToPrimitive(irParamSw.getShkSprdSceNo(), 1)).stream()
															.collect(Collectors.toMap(IrSprdAfnsBiz::getMatCd, IrSprdAfnsBiz::getShkSprdCont));				
	// 4. 시나리오 적용할 준비 : spotSceList copy 			
				List<IrCurveSpot> spotSceList = spotList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());
				
				
				String fwdMatCd = irParamSw.getFwdMatCd();				
				if(!fwdMatCd.equals("M0000")) {
					// spot -> fwd 변환 
					Map<String, Double> fwdSpotMap = irSpotDiscToFwdMap(bssd, spotMap, fwdMatCd);
					// spotSceList : 변환한 fwd로 대체  
					spotSceList.stream().forEach(s -> s.setSpotRate(fwdSpotMap.get(s.getMatCd())));					
				}				

				String pvtMatCd = irParamSw.getPvtRateMatCd();
				// 23.04.06 spotRate entity에서 값을 가져올때 이미 null 인 경우 에러를 return 하기 때문에 null인 채로 여기까지 올 수 없을텐데 또 default 처리가 된 이유가 뭘까.
				double pvtRate  = spotMap.getOrDefault(pvtMatCd, 0.0);				
				double pvtMult  = irParamSw.getMultPvtRate();		
				double addSprd  = irParamSw.getAddSprd();
				int    llp      = irParamSw.getLlp();				
				
//				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, curveSwMap.getKey(),irCurveSceNo , pvtMatCd, pvtRate, pvtMult, intMult, addSprd, llp);
				for(IrCurveSpot spot : spotSceList) {				
					if(Integer.valueOf(spot.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrDcntRateBu dcntRateBu = new IrDcntRateBu();						
						
					// 충격 시나리오 적용은 fwd로 변환해서 적용해야 하는지 확인하기 !!	
//						double baseSpot = pvtMult * (StringUtil.objectToPrimitive(spot.getSpotRate()) - pvtRate) +  pvtRate + addSprd  ;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
						double baseSpot = pvtMult * (spot.getSpotRate() - pvtRate) +  pvtRate + addSprd  ;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
						double baseSpotCont = baseSpot;	
						
						double shkCont      = applBizDv.equals(EApplBizDv.KICS) ? irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) : 0.0; 	
						double lpDisc       = irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0);
						
						double spotCont     = baseSpotCont + shkCont;
						double spotDisc     = irContToDisc(spotCont);						
						double adjSpotDisc  = spotDisc + lpDisc;
						double adjSpotCont  = irDiscToCont(adjSpotDisc);						
						
						dcntRateBu.setBaseYymm(bssd);
						dcntRateBu.setApplBizDv(applBizDv);
						dcntRateBu.setIrCurveNm(irCurveNm);
						dcntRateBu.setIrCurve(irCurve);
						dcntRateBu.setIrCurveSceNo(irCurveSceNo);
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

