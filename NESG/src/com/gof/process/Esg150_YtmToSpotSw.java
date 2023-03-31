package com.gof.process;

import java.util.ArrayList;
import java.util.List;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.model.SmithWilsonKicsBts;
import com.gof.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg150_YtmToSpotSw extends Process {	
	
	public static final Esg150_YtmToSpotSw INSTANCE = new Esg150_YtmToSpotSw();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
//	23.03.31 매개변수 줄이기 
//	public static List<IrCurveSpot> createIrCurveSpot(String baseYmd, String irCurveNm, List<IrCurveYtm> ytmRst, Double alphaApplied, Integer freq) {
	/** ytm -> spot
	 * @param ytmRst
	 * @param alphaApplied
	 * @param freq
	 * */
	public static List<IrCurveSpot> createIrCurveSpot(List<IRateInput> ytmRst, Double alphaApplied, Integer freq) {		
		
		// 내부 변수로 정의 
		String baseYmd   = ytmRst.get(0).getBaseDate();
		String irCurveNm = ytmRst.get(0).getIrCurveNm();
		
		
		SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of()
									 .baseDate(DateUtil.convertFrom(baseYmd))
									 .ytmCurveHisList(ytmRst)
									 .alphaApplied(alphaApplied)													 
									 .freq(freq)
									 .build();
		
		List<IrCurveSpot> rst = swBts.getSpotBtsRslt();
		
		for(IrCurveSpot crv : rst) {
			if(crv.getSpotRate().isNaN() || crv.getSpotRate().isInfinite()) {
//				double[] ytm = ytmRst.stream().filter(s -> s.getMatCd().equals(crv.getMatCd())).map(IrCurveYtm::getYtm).mapToDouble(Double::doubleValue).toArray();
//				crv.setSpotRate(ytm[0]);				
				log.error("YTM to SPOT BootStrapping is failed. Check YTM Data in [{}] Table for [ID: {} in {}]", Process.toPhysicalName(IrCurveYtm.class.getSimpleName()), irCurveNm, baseYmd);
				return new ArrayList<IrCurveSpot>();
			}
		}
		rst.stream().forEach(s -> s.setIrCurveNm(irCurveNm));
		rst.stream().forEach(s -> s.setBaseDate(baseYmd));
		
		log.info("{}({}) creates [{}] results of [{}] in [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), baseYmd, toPhysicalName(IrCurveSpot.class.getSimpleName()));
		
		return rst;
	}
	
}

