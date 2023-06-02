package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import com.gof.dao.IrSprdDao;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.entity.IrSprdAfnsUsr;
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.entity.IrSprdAfnsBiz;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg230_BizSprdAfns extends Process {	
	
	public static final Esg230_BizSprdAfns INSTANCE = new Esg230_BizSprdAfns();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	

	public static List<IrSprdAfnsBiz> createBizAfnsShockScenario(String bssd, EIrModel irModelNm, String irCurveNm) {
				
		List<IrSprdAfnsBiz>  irShockBiz  = new ArrayList<IrSprdAfnsBiz>();
		List<IrSprdAfnsCalc> irShockCalc = IrSprdDao.getIrSprdAfnsCalcList(bssd, irModelNm, irCurveNm);		
		List<IrSprdAfnsUsr>  irShockUsr  = IrSprdDao.getIrSprdAfnsUsrList(bssd, irModelNm, irCurveNm);
	
		// 둘 다 없으면 경고 때리고 비어있는 채로 리턴 
		if (irShockUsr.isEmpty()&&irShockCalc.isEmpty()) {
			log.warn("{}({}) No Shock Spread from Model in [Model:{}, ID:{}]", jobId, EJob.valueOf(jobId).getJobName(), irModelNm, irCurveNm);
//			return irShockBiz; 
		}
		// irShockUsr있으면 우선 적용 
		else if(!irShockUsr.isEmpty()) {
			
			for(IrSprdAfnsUsr usr : irShockUsr) {				
				for(int i=0; i<6; i++) {
					IrSprdAfnsBiz biz = new IrSprdAfnsBiz();			
					
					biz.setBaseYymm    (usr.getBaseYymm());
					biz.setIrParamModel(usr.getIrParamModel());
					biz.setIrModelNm   (usr.getIrModelNm());
					biz.setIrCurve     (usr.getIrCurve());
					biz.setIrCurveNm   (usr.getIrCurveNm());
					biz.setIrCurveSceNo(EDetSce.getEDetSce(i+1));
					biz.setMatCd       (usr.getMatCd());
					
					if(i==0) { 	   biz.setShkSprdCont(0.0);}
					else if(i==1) {biz.setShkSprdCont(usr.getMeanSprd() );}
					else if(i==2) {biz.setShkSprdCont(usr.getUpSprd()   );}
					else if(i==3) {biz.setShkSprdCont(usr.getDownSprd() );}
					else if(i==4) {biz.setShkSprdCont(usr.getFlatSprd() );}
					else 		  {biz.setShkSprdCont(usr.getSteepSprd());}
					
					biz.setModifiedBy(jobId);
					biz.setUpdateDate(LocalDateTime.now());
										
					irShockBiz.add(biz);
				}				
			}
			
			log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table"
				, jobId
				, EJob.valueOf(jobId).getJobName()
				, irShockBiz.size()
				, toPhysicalName(IrSprdAfnsUsr.class.getSimpleName())
				, toPhysicalName(IrSprdAfnsBiz.class.getSimpleName()));
		}
		// irShockUsr 없고 irShockCalc 있으면 
		else { 
				irShockBiz = irShockCalc.stream().map(s -> s.convert()).collect(Collectors.toList());
				
			log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table"
				, jobId
				, EJob.valueOf(jobId).getJobName()
				, irShockBiz.size()
				, toPhysicalName(IrSprdAfnsCalc.class.getSimpleName())
				, toPhysicalName(IrSprdAfnsBiz.class.getSimpleName()));			
		}
		return irShockBiz;
	}	
	
}
