package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.enums.EJob;
import com.gof.model.SmithWilsonKicsBts;
import com.gof.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg150_YtmToSpotSw extends Process {	
	
	public static final Esg150_YtmToSpotSw INSTANCE = new Esg150_YtmToSpotSw();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	
	public static List<IrCurveSpot> createIrCurveSpot(String baseYmd, IrCurve irCurve, List<IrCurveYtm> ytmRst) {		
		return createIrCurveSpot(baseYmd, irCurve, ytmRst, 0.1, 2);
	}
	
	public static List<IrCurveSpot> createIrCurveSpot(String baseYmd, IrCurve irCurve, List<IrCurveYtm> ytmRst, Double alphaApplied, Integer freq) {		
		
		// SW bootstrapping 객체 통째로 넘겨줌 ! 
		SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of()
									 .baseDate(DateUtil.convertFrom(baseYmd))
									 .ytmCurveHisList(ytmRst)
									 .alphaApplied(alphaApplied)													 
									 .freq(freq)
									 .build();
		
        // 객체 안에 각각 생성된 값 (산출결과 : SmithWilsonRslt)은 rst에 있음.
		List<IrCurveSpot> rst = swBts.getSpotBtsRslt();
		
		for(IrCurveSpot crv : rst) {
			if(crv.getSpotRate().isNaN() || crv.getSpotRate().isInfinite()) {
//				double[] ytm = ytmRst.stream().filter(s -> s.getMatCd().equals(crv.getMatCd())).map(IrCurveYtm::getYtm).mapToDouble(Double::doubleValue).toArray();
//				crv.setSpotRate(ytm[0]);				
				log.error("YTM to SPOT BootStrapping is failed. Check YTM Data in [{}] Table for [ID: {} in {}]", Process.toPhysicalName(IrCurveYtm.class.getSimpleName()), irCurve.getIrCurveNm(), baseYmd);
				return new ArrayList<IrCurveSpot>();
			}
		}
		// 왜 각각 set 해줄까 ??
		rst.stream().forEach(s -> s.setIrCurveNm(irCurve.getIrCurveNm()));
		rst.stream().forEach(s -> s.setIrCurve(irCurve));
		rst.stream().forEach(s -> s.setBaseDate(baseYmd));
		rst.stream().forEach(s -> s.setModifiedBy(jobId));
		rst.stream().forEach(s -> s.setUpdateDate(LocalDateTime.now()));
		
		log.info("{}({}) creates [{}] results of [{}] in [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), irCurve.getIrCurveNm(), baseYmd, toPhysicalName(IrCurveSpot.class.getSimpleName()));
		
		return rst;
	}	
	
}

