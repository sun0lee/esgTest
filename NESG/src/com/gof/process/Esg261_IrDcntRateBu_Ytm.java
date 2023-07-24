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
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;

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
	public static List<IrDcntRateBu> setIrDcntRateBu(String bssd, EIrModel irModelNm, EApplBizDv applBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap) {	
		
		List<IrDcntRateBu> rst = new ArrayList<IrDcntRateBu>();
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {	
			IrCurve irCurve  = curveSwMap.getKey();
			String irCurveNm = irCurve.getIrCurveNm();
			
			// ori ytm  : IRateInput 형태로 가져올 수 없었던 이유는 IRateInput interface값을 read only로 정의함. 
			List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd, irCurveNm);
//			ytmList.forEach(s-> log.info("ytm : {},{}", s.toString()));

			
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				EDetSce  irCurveSce = swSce.getKey();  
				IrParamSw irParamSw = swSce.getValue();
				
				// 1. ytm -> spot 변환 (ytm에 직접 스프레드를 반영, 10.0 추가된 up down 시나리오 산출 부분 확인)
				List<IRateInput> ytmAddList = ytmList.stream().map(s->s.addSpread(irParamSw.getYtmSpread())).collect(Collectors.toList());
//				ytmAddList.forEach(s-> log.info("ytm1 : {},{}", s.toString()));
				
				List<IrCurveSpot> spotList = Esg150_YtmToSpotSw.createIrCurveSpot(ytmAddList, irParamSw)
											.stream().map(s-> s.convertToCont()).collect(Collectors.toList());
		
				spotList.forEach(s-> s.setIrCurve(irCurve));
				spotList.forEach(s-> log.info("zzzz : {},{}", irCurveSce.getSceNo(), s.toString()));
				
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
				Map<String, Double> irSprdLpMap = IrSprdDao.getIrSprdLpBizList(bssd, applBizDv, irCurveNm, irCurveSce).stream()
						                                   .collect(Collectors.toMap(IrSprdLpBiz::getMatCd, IrSprdLpBiz::getLiqPrem));

				// 3. 금리 충격스프레드 가져오기 시나리오번호도 디폴트 처리가 필요할까? 없으면 에러인데.
				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelNm, irCurveNm, irParamSw.getShkSprdSceNo()).stream()
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
				// 23.04.17 sy 260과 결과가 같게 만들기 
//				double pvtRate  = spotMap.getOrDefault(pvtMatCd, 0.0);				
				double pvtRate  = irContToDisc(spotMap.getOrDefault(pvtMatCd, 0.0)); 				
				double pvtMult  = irParamSw.getMultPvtRate();
				double addSprd  = irParamSw.getAddSprd();
				int    llp      = irParamSw.getLlp();				
				
				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, irCurveNm, irCurveSce, pvtMatCd, pvtRate, pvtMult, addSprd, llp);
//				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, curveSwMap.getKey(),irCurveSce , pvtMatCd, pvtRate, pvtMult, addSprd, llp);
				for(IrCurveSpot spot : spotSceList) {				
					if(Integer.valueOf(spot.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrDcntRateBu dcntRateBu = new IrDcntRateBu();						
						
					// 충격 시나리오 적용은 fwd로 변환해서 적용해야 하는지 확인하기 !!	
						// 23.04.17 sy 260과 결과가 같게 만들기  baseSpot 조정하기 전의 spot은 disc 를 기준으로 조정 후 cont로 변환함. 
//						double baseSpot = pvtMult * (spot.getSpotRate() - pvtRate) + pvtRate + addSprd  ;  
						double baseSpot = pvtMult * (irContToDisc(spot.getSpotRate()) - pvtRate) + pvtRate + addSprd  ;  // pvtRate doesn't have an effect on parallel shift(only addSprd)
						
						// 23.04.17 sy 260과 결과가 같게 만들기 
						double baseSpotCont = irDiscToCont(baseSpot);	
						
						// 261 KICS면 irSprdShkMap 만기별 충격수준 적용 / KICS 가 아니면 0 
//						double shkCont      = applBizDv.equals(EApplBizDv.KICS) ? 
//											    irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) 
//											  : 0.0; 
						
						// 260 KICS면 irSprdShkMap 만기별 충격수준 적용 + addSprd / KICS 가 아니면 irSprdShkMap
//						double shkCont      = (applBizDv.equals(EApplBizDv.KICS) && irCurveSce.getSceNo() <= kicsAddSprdContSceNo) ? 
//							    irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) + addSprd 
//							  : irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0);
						
						
						// 23.04.17 biz.KICS여부에 관계 없이 만기별 irSprdShkMap 를 적용 !
						double shkCont = irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) ;

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

