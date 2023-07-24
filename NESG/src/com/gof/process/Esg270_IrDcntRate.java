package com.gof.process;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gof.dao.IrCurveYtmDao;
import com.gof.dao.IrDcntRateDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrDcntRate;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamSw;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EDetSce;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.model.SmithWilsonKics;
import com.gof.model.SmithWilsonKicsBts;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg270_IrDcntRate extends Process {	
	
	public static final Esg270_IrDcntRate INSTANCE = new Esg270_IrDcntRate();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/**
	 * @param bssd
	 * @param applBizDv
	 * @param paramSwMap
	 * @param projectionYear
	 * */
	public static List<IrDcntRate> createIrDcntRate(String bssd, EApplBizDv applBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap, Integer projectionYear) {	
		
		List<IrDcntRate> rst = new ArrayList<IrDcntRate>();
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {	
			IrCurve irCurve = curveSwMap.getKey();
			String irCurveNm = curveSwMap.getKey().getIrCurveNm();

			Map<String, IrDcntRate> adjRateSce1Map       = new TreeMap<String, IrDcntRate>();
			Map<String, SmithWilsonRslt> baseRateSce1Map = new TreeMap<String, SmithWilsonRslt>();  			//for using SmithWilsonKicsBts not SmithWilsonKics
//			List<IrDcntRate> adjRateSce1List             = new ArrayList<IrDcntRate>();			                //hereafter for KICS SCE_NO 7 and 8(totalShift)
//			double ltfr1 = 0.0;
//			double shift = 0.0;
			
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {	
				EDetSce irCurveSce = swSce.getKey();
				IrParamSw irParamSw = swSce.getValue();

				
				log.info("BIZ: [{}], IR_CURVE_NM: [{}], IR_CURVE_SCE_NO: [{}]", applBizDv, irCurveNm, irCurveSce.getSceNo());
				List<IRateInput>  irCurveSpotList = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, irCurveNm, irCurveSce);
				
				if(irCurveSpotList.size()==0) {
					log.warn("No IR Dcnt Rate Data [BIZ: {}, IR_CURVE_NM: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", applBizDv, irCurveNm, irCurveSce.getSceNo(), toPhysicalName(IrDcntRateBu.class.getSimpleName()), bssd);
					continue;
				}				
				
				LocalDate baseDate = DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth());
//				log.info("{}, {}, {}", irParamSw.getLtfr(), irParamSw.getLtfrCp(), projectionYear);
				
				// smith-wilson 변환 생성 (조정 금리커브)  
//				SmithWilsonKics swKics = new SmithWilsonKics(baseDate, irCurveSpotList, CMPD_MTD_DISC, true, irParamSw.getLtfr(), irParamSw.getLtfrCp(), projectionYear, 1, 100, DCB_MON_DIF);	
				
				SmithWilsonKics swKics = SmithWilsonKics.of()
										.baseDate       (baseDate)
										.irCurveHisList (irCurveSpotList)
										.cmpdType       (CMPD_MTD_DISC)
										.isRealNumber   (true)
										.ltfr           (irParamSw.getLtfr())
										.ltfrT          (irParamSw.getLtfrCp())
										.prjYear        (projectionYear)
										.prjInterval    (1)
										.alphaItrNum    (100)
										.dayCountBasis  (DCB_MON_DIF)
										.build();
				
				// dcntRate 들고 들어가서 변환함.
				List<IrDcntRate> adjRateList = swKics.getSmithWilsonResultList().stream().map(s -> s.convert()).collect(Collectors.toList());
				
				
				// 만기별 map 
				Map<String, IrDcntRate> adjRateMap = adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd, Function.identity(), (k, v) -> k, TreeMap::new));				
				TreeSet<Double> tenorList = adjRateList.stream().map(s -> Double.valueOf(1.0 * Integer.valueOf(s.getMatCd().substring(1)) / MONTH_IN_YEAR)).collect(Collectors.toCollection(TreeSet::new));
				double[] prjTenor = tenorList.stream().mapToDouble(Double::doubleValue).toArray();				
				
				// 23.07.24 KICS 여부와 관계없이 동일한 로직으로 처리되는지 확인 
//				if(applBizDv==EApplBizDv.KICS) {
					
	  // KICS 기준시나리오 1 or 기타 별도 정의 6,7,8,9 
//					if( irCurveSce.getSceNo()==EDetSce.SCE01.getSceNo() || irCurveSce.getSceNo() >EDetSce.SCE06.getSceNo()) {
//					if( irCurveSce==EDetSce.SCE01 || irCurveSce.compareTo(EDetSce.SCE06) > 0) {
	  // 기준시나리오 
					if( irCurveSce==EDetSce.SCE01 ) {
						adjRateSce1Map = adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd, Function.identity(), (k, v) -> k, TreeMap::new));										
						
//						List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd, curveSwMap.getKey());
						List<IRateInput> ytmList = IrCurveYtmDao.getIrCurveYtm2(bssd, irCurveNm);
						
						if(ytmList.size()==0) {
							log.warn("No Historical YTM Data exist for [{}, {}] in [{}]", bssd, irCurveNm, jobId);
							continue;
						}				
						// 자산 할인율 산출을 위한 ytm smith-wilson 변환 
						SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of()
													.baseDate(baseDate)					
													.ytmCurveHisList(ytmList)
													.alphaApplied(irParamSw.getSwAlphaYtm())													 
													.freq(irParamSw.getFreq())
													.build();						
						
						baseRateSce1Map = swBts.getSmithWilsonResultList(prjTenor).stream().collect(Collectors.toMap(SmithWilsonRslt::getMatCd, Function.identity()));
						
						// smith-wilson 변환 결과 (기본 금리커브)  
						for(IrDcntRate rslt : adjRateList) {	
							// 자산 할인율 보간결과 넣어주기 
							rslt.setSpotRate(baseRateSce1Map.get(rslt.getMatCd()).getSpotDisc());
							rslt.setFwdRate (baseRateSce1Map.get(rslt.getMatCd()).getFwdDisc());
						}					
						
					}
	 // KICS 결정론적 시나리오 2~5 				
					//for KICS: Asset Discount Rate Scenario after scen#1 is generated from Above Insurance Discount Rate + Difference Rate of Insurance - Asset at SCE#1
//					else if(irCurveSce.getSceNo() <= EDetSce.SCE06.getSceNo()) {
//					else if (irCurveSce.compareTo(EDetSce.SCE06) <= 0) {
	  // 기준 시나리오 외 
					else  { 
						TreeMap<String, Double> spotRateMap = new TreeMap<String, Double>();
						TreeMap<String, Double> fwdRateMap  = new TreeMap<String, Double>();
						
						//부채 할인용 : 조정 후 금리커브는 swKics에서 변환한값을 그대로 사용하고  
						for(IrDcntRate rslt : adjRateList) {						
							String matCd   = rslt.getMatCd();
							// 조정 충격후 
							double adjRate = adjRateMap.get(matCd).getAdjSpotRate();
							// 기본 충격전 - 조정 충격전 
							double adjDiff = baseRateSce1Map.get(matCd).getSpotDisc() - adjRateSce1Map.get(matCd).getAdjSpotRate();
							
							// 기본 충격후 : = (기본 충격전) + (조정 충격후 - 조정 충격전) => 엑셀 로직 (기본금리커브 + (충격 스프레드))
							// 기본 충격후 : = (조정 충격후) + (기본 충격전 - 조정 충격전) => 코드 로직 
							rslt.setSpotRate(adjRate + adjDiff);						
							spotRateMap.put(matCd, adjRate + adjDiff);
						}	
						// 기본 충격후 spot -> fwd 
						fwdRateMap = irSpotDiscToFwdM1Map(spotRateMap);					

						for(IrDcntRate rslt : adjRateList) {
							rslt.setFwdRate(fwdRateMap.get(rslt.getMatCd()).doubleValue());
						}		
					}
					
//				}
	//KICS 외 			
////				else if(!applBizDv.equals("KICS")) {
//				else  {
//					// 부채평가용 (조정 할인율 커브 :연속복리 spot rate사용 )
//					adjRateSce1Map = adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd, Function.identity(), (k, v) -> k, TreeMap::new));		
//					
//					List<IRateInput> ytmList = IrDcntRateDao.getIrDcntRateBuToBaseSpotList(bssd, applBizDv, irCurveNm, irCurveSce)
//											  .stream().map(s -> s.convertSimpleYtm()).collect(Collectors.toList());					
//					if(ytmList.size()==0) {
//						log.warn("No IR Dcnt Rate Data [BIZ: {}, IR_CURVE_NM: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", applBizDv, irCurveNm, irCurveSce.getSceNo() , toPhysicalName(IrDcntRateBu.class.getSimpleName()), bssd);
//						continue;
//					}
//					
//					// 자산평가용 (기본 할인율 커브 : YTM 사용 ) 
//					SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of()
//							 									 .baseDate(baseDate)					
//							 									 .ytmCurveHisList(ytmList)
//							 									 .alphaApplied(irParamSw.getSwAlphaYtm())													 
//							 									 .freq(0)
//							 									 .build();						
//
////				    swBts.getSmithWilsonResultList(prjTenor).stream().filter(s -> Double.parseDouble(s.getMatCd().substring(1, 5)) <= 240).forEach(s -> log.info("{}, {}, {}", s.getMatCd(), s.getSpotDisc(), s.getFwdDisc()));
//					// 자산평가용 (기본 할인율 커브 : YTM 사용 ) 
//					baseRateSce1Map = swBts.getSmithWilsonResultList(prjTenor).stream().collect(Collectors.toMap(SmithWilsonRslt::getMatCd, Function.identity()));
//
//					for(IrDcntRate rslt : adjRateList) {						
//						rslt.setSpotRate(baseRateSce1Map.get(rslt.getMatCd()).getSpotDisc());
//						rslt.setFwdRate (baseRateSce1Map.get(rslt.getMatCd()).getFwdDisc());
//					}					
//				}

				for(IrDcntRate rslt : adjRateList) {
					rslt.setBaseYymm(bssd);
					rslt.setApplBizDv(applBizDv);
					rslt.setIrCurveNm(irCurveNm);
					rslt.setIrCurve(irCurve);  
					rslt.setIrCurveSceNo(irCurveSce);
					rslt.setModifiedBy(jobId);
					rslt.setUpdateDate(LocalDateTime.now());
				}				
				
				
				for(IrDcntRate dcnt : adjRateList) {
					if(dcnt.getSpotRate().isNaN() || dcnt.getSpotRate().isInfinite() || dcnt.getAdjSpotRate().isNaN() || dcnt.getAdjSpotRate().isInfinite()) {
//						log.info("{}, {}, {}", curveSwMap.getKey(), irCurveSceNo, dcnt);
						log.error("Smith-Wilson Interpolation is failed. Check Shock Spread Data in [{}] Table for [BIZ: {}, IR_CURVE_NM: {}, IR_CURVE_SCE_NO: {}] in [{}]", Process.toPhysicalName(IrCurveYtm.class.getSimpleName()), applBizDv, irCurveNm, irCurveSce.getSceNo(), bssd);
						try {
							throw new Exception();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						return new ArrayList<IrDcntRate>();
					}
				}
				
				rst.addAll(adjRateList);
			}
		}		
		log.info("{}({}) creates [{}] results of [{}] {}. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, paramSwMap.keySet(), toPhysicalName(IrDcntRate.class.getSimpleName()));
		
		return rst;		
	}	

}

