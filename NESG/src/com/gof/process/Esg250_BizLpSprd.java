package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdLp;
import com.gof.entity.IrSprdLpBiz;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg250_BizLpSprd extends Process {	
	
	public static final Esg250_BizLpSprd INSTANCE = new Esg250_BizLpSprd();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static List<IrSprdLpBiz> setLpSprdBiz(String bssd, EApplBizDv applBizDv, Map<IrCurve, Map<Integer, IrParamSw>> paramSwMap) {
		
		List<IrSprdLpBiz> rst = new ArrayList<IrSprdLpBiz>();
		
		for(Map.Entry<IrCurve, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {	
			IrCurve irCurve  = curveSwMap.getKey();
			String irCurveNm = curveSwMap.getKey().getIrCurveNm();

			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {				
				
				// 최종 적용 기준 : IR_PARAM_SW 의 liqPremApplDv (유동성프리미엄적용구분) 설정에 따라 결정함.  
				String dcntApplModelCd = "BU" + swSce.getValue().getLiqPremApplDv();
				
				List<IrSprdLp> sprdLpList = IrSprdDao.getIrSprdLpList(bssd, dcntApplModelCd, applBizDv, irCurveNm, swSce.getKey());
				if(sprdLpList.isEmpty()) {
					log.warn("No IR Spread Data [IR_CURVE_NM: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", irCurveNm, swSce.getKey(), toPhysicalName(IrSprdLp.class.getSimpleName()), bssd);
					continue;
				}

				for(IrSprdLp sprdLp : sprdLpList) {
					IrSprdLpBiz sprdLpBiz = new IrSprdLpBiz();
					
					sprdLpBiz.setBaseYymm(bssd);						
					sprdLpBiz.setApplBizDv(applBizDv);
					sprdLpBiz.setIrCurveNm(irCurveNm);
					sprdLpBiz.setIrCurve(irCurve);
					sprdLpBiz.setIrCurveSceNo(swSce.getKey());
					sprdLpBiz.setMatCd(sprdLp.getMatCd());
					sprdLpBiz.setLiqPrem(sprdLp.getLiqPrem());
					sprdLpBiz.setModifiedBy(jobId);						
					sprdLpBiz.setUpdateDate(LocalDateTime.now());
					
					rst.add(sprdLpBiz);
				}
			}
		}		
		log.info("{}({}) creates [{}] results of {}. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLpBiz.class.getSimpleName()));
		
		return rst;		
	}	

}

