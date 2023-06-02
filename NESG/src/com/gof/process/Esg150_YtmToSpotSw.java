package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrParamSw;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.model.SmithWilsonKicsBts;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg150_YtmToSpotSw extends Process {	
	
	public static final Esg150_YtmToSpotSw INSTANCE = new Esg150_YtmToSpotSw();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
//	23.03.31 매개변수 줄이기 
//	public static List<IrCurveSpot> createIrCurveSpot(String baseYmd, String irCurveNm, List<IrCurveYtm> ytmRst, Double alphaApplied, Integer freq) {
	/** ytm -> spot
	 * @param 
	 * */
	public static List<IrCurveSpot> createIrCurveSpot(List<IRateInput> ytmRst, IrParamSw irparamSw ){		
		
		// 내부 변수로 정의 irCurveSwMap.get(irCurveNm)
		String  baseYmd      = ytmRst.get(0).getBaseDate();
		IrCurve irCurve      = ytmRst.get(0).getIrCurve();
		Double  alphaApplied = irparamSw.getSwAlphaYtm();
		Integer freq         = irparamSw.getFreq();
		
		
		SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of()
								  .baseDate(DateUtil.convertFrom(baseYmd))
								  .ytmCurveHisList(ytmRst)
								  .alphaApplied(alphaApplied)													 
								  .freq(freq)
								  .build();

		//  IRateInput -> IrCurveSpot  SmithWilsonKicsBts에서는 타입변환 말고 율 변환에만 집중하기 
//		List<IrCurveSpot> spotRst = swBts.getSpotBtsRslt();
		
//		23.04.03 타입변환 여기에서 하기 
		List<IrCurveSpot> spotRst = new ArrayList<IrCurveSpot>() ;
			IrCurveSpot tempSpot ;
		
		// sw 결과를 spot rate형태로 변환하기 (한줄씩 처리되므로 만기별 변환처리를 반복함.)
		for (SmithWilsonRslt swRst : swBts.getSmithWilsonResultList()) {
			tempSpot = new IrCurveSpot( baseYmd, irCurve, swRst.getMatCd(), 1, swRst.getSpotDisc()) ;
			spotRst.add(tempSpot) ;
		}
		
		for(IrCurveSpot crv : spotRst) {
			if(crv.getSpotRate().isNaN() || crv.getSpotRate().isInfinite()) {
//				double[] ytm = ytmRst.stream().filter(s -> s.getMatCd().equals(crv.getMatCd())).map(IrCurveYtm::getYtm).mapToDouble(Double::doubleValue).toArray();
//				crv.setSpotRate(ytm[0]);				
				log.error("YTM to SPOT BootStrapping is failed. Check YTM Data in [{}] Table for [ID: {} in {}]"
						, Process.toPhysicalName(IrCurveYtm.class.getSimpleName())
						, irCurve.getIrCurveNm()
						, baseYmd);
				return new ArrayList<IrCurveSpot>();
			}
		}
//		spotRst.stream().forEach(s -> s.setIrCurveNm(irCurveNm));
//		spotRst.stream().forEach(s -> s.setBaseDate(baseYmd));
		 
		spotRst.stream().forEach(s -> s.setModifiedBy("ESG150")); 			 // TODO : 작업마다 공통적으로 찍어주는 로그 처리
		spotRst.stream().forEach(s -> s.setUpdateDate(LocalDateTime.now())); // TODO : trigger 로 처리하기 
//		
		log.info("{}({}) creates [{}] results of [{}] in [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), spotRst.size(), irCurve.getIrCurveNm(),baseYmd, toPhysicalName(IrCurveSpot.class.getSimpleName()));
		
		return spotRst;
	}
	
}

