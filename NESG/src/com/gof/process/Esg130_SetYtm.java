package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.gof.dao.IrCurveYtmDao;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrCurveYtmUsr;
import com.gof.entity.IrCurveYtmUsrHis;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg130_SetYtm extends Process {	
	
	public static final Esg130_SetYtm INSTANCE = new Esg130_SetYtm();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	

	public static List<IRateInput> createYtmFromUsrHis(String bssd, String irCurveNm) {
				
		List<IRateInput>       ytmList    = new ArrayList<IRateInput>();		
		List<String>           ytmTen     = Arrays.asList("M0003", "M0006", "M0009", "M0012", "M0018", "M0024", "M0030", "M0036", "M0048", "M0060", "M0084", "M0120", "M0180", "M0240", "M0360", "M0600");		
		List<IrCurveYtmUsrHis> ytmUsrList = IrCurveYtmDao.getIrCurveYtmUsrHis(bssd, irCurveNm);
		
		//Using Round Method: for avoiding truncation error in converting toReal Dimension
		double toReal = 0.01;
		int    digit  = 7;    
//		ytmUsrList.stream().forEach(s -> log.info("{}", s));		
			
		for(IrCurveYtmUsrHis usr : ytmUsrList) {
			
			for(int i=0; i<16; i++) {
				IrCurveYtm ytm = new IrCurveYtm();			
				
				ytm.setBaseDate(usr.getBaseDate());				
				ytm.setIrCurveNm(irCurveNm);				
				ytm.setIrCurve(usr.getIrCurve());				
				ytm.setMatCd(ytmTen.get(i));
					
				if     (i==0)  {ytm.setYtm(round(usr.getYtmM0003() * toReal, digit)); }
				else if(i==1)  {ytm.setYtm(round(usr.getYtmM0006() * toReal, digit)); }
				else if(i==2)  {ytm.setYtm(round(usr.getYtmM0009() * toReal, digit)); }
				else if(i==3)  {ytm.setYtm(round(usr.getYtmM0012() * toReal, digit)); }
				else if(i==4)  {ytm.setYtm(round(usr.getYtmM0018() * toReal, digit)); }
				else if(i==5)  {ytm.setYtm(round(usr.getYtmM0024() * toReal, digit)); }
				else if(i==6)  {ytm.setYtm(round(usr.getYtmM0030() * toReal, digit)); }
				else if(i==7)  {ytm.setYtm(round(usr.getYtmM0036() * toReal, digit)); }
				else if(i==8)  {ytm.setYtm(round(usr.getYtmM0048() * toReal, digit)); }
				else if(i==9)  {ytm.setYtm(round(usr.getYtmM0060() * toReal, digit)); }
				else if(i==10) {ytm.setYtm(round(usr.getYtmM0084() * toReal, digit)); }
				else if(i==11) {ytm.setYtm(round(usr.getYtmM0120() * toReal, digit)); }
				else if(i==12) {ytm.setYtm(round(usr.getYtmM0180() * toReal, digit)); }
				else if(i==13) {ytm.setYtm(round(usr.getYtmM0240() * toReal, digit)); }
				else if(i==14) {ytm.setYtm(round(usr.getYtmM0360() * toReal, digit)); }
				else {			ytm.setYtm(round(usr.getYtmM0600() * toReal, digit)); }
				
				ytm.setModifiedBy(jobId);
				ytm.setUpdateDate(LocalDateTime.now());
									
				ytmList.add(ytm);
			}				
		}
		log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), ytmList.size(), toPhysicalName(IrCurveYtmUsrHis.class.getSimpleName()), toPhysicalName(IrCurveYtm.class.getSimpleName()));
		
		return ytmList;
	}	
	
/* 23.03.06 기존로직 주석 처리 :setter 처리  
	public static List<IrCurveYtm> createYtmFromUsr(String bssd, String irCurveNm) {
		
		List<IrCurveYtm>    ytmList    = new ArrayList<IrCurveYtm>();		
		List<IrCurveYtmUsr> ytmUsrList = IrCurveYtmDao.getIrCurveYtmUsr(bssd, irCurveNm);
		
		//Using Round Method: for avoiding truncation error in converting toReal Dimension
		double toReal = 1;
		int    digit  = 7;    
//		ytmUsrList.stream().forEach(s -> log.info("{}", s));		
			
		for(IrCurveYtmUsr usr : ytmUsrList) {
			
			IrCurveYtm ytm = new IrCurveYtm();				
			ytm.setBaseDate(usr.getBaseDate());				
			ytm.setIrCurve(usr.getIrCurve());				
			ytm.setIrCurveNm(usr.getIrCurveNm());				
			ytm.setMatCd(usr.getMatCd());
			ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtm(), 0.0) * toReal, digit));

			ytm.setModifiedBy(jobId);
			ytm.setUpdateDate(LocalDateTime.now());
								
			ytmList.add(ytm);
		}
		log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), ytmList.size(), toPhysicalName(IrCurveYtmUsr.class.getSimpleName()), toPhysicalName(IrCurveYtm.class.getSimpleName()));
		
		return ytmList;
	}		
*/
	// 23.03.06 builder test -> loop 처리를 어떻게 할것인가 ??
	public static Stream<IrCurveYtm> createYtmFromUsrHisIdx(String bssd, String irCurveNm, int idx) {
		
		 return IrCurveYtmDao.getIrCurveYtmUsrHis(bssd)
				  .filter(s->s.getIrCurveNm().equals(irCurveNm))
				  .map(s->Esg130_SetYtm.buildFromYtmUsrHis(s, idx));
	}

	
	// 23.03.06 builder test
	public static Stream<IRateInput> createYtmFromUsr(String bssd, String irCurveNm) {

		return IrCurveYtmDao.getIrCurveYtmUsr(bssd).filter(s->s.getIrCurveNm().equals(irCurveNm))
												   .map(s->Esg130_SetYtm.buildFromYtmUsr(s));
	}
	

	private static IrCurveYtm buildFromYtmUsrHis (IrCurveYtmUsrHis ytmUsrHis, int idx) {
		
		List<String> ytmTen = Arrays.asList("M0003", "M0006", "M0009", "M0012", "M0018", "M0024", "M0030", "M0036", "M0048", "M0060", "M0084", "M0120", "M0180", "M0240", "M0360", "M0600");		
		List<String> TenList = new ArrayList<>() ; 
		List<Double> ytmList = new ArrayList<>() ;  

		//Using Round Method: for avoiding truncation error in converting toReal Dimension
		double toReal = 0.01;
		int    digit  = 7;    
		
		// TODO : List로 처리한 값을 순서대로 builder에 찍어주는 방법 찾기 .
		for (int i = 0 ; i < ytmTen.size(); i++) {
			TenList.add (ytmTen.get(i));
			
			if      (i==0)  { ytmList.add (ytmUsrHis.getYtmM0003()); }
			else if (i==1)  { ytmList.add (ytmUsrHis.getYtmM0006()); }
			else if (i==2)  { ytmList.add (ytmUsrHis.getYtmM0009()); }
			else if (i==3)  { ytmList.add (ytmUsrHis.getYtmM0012()); }
			else if (i==4)  { ytmList.add (ytmUsrHis.getYtmM0018()); }
			else if (i==5)  { ytmList.add (ytmUsrHis.getYtmM0024()); }
			else if (i==6)  { ytmList.add (ytmUsrHis.getYtmM0030()); }
			else if (i==7)  { ytmList.add (ytmUsrHis.getYtmM0036()); }
			else if (i==8)  { ytmList.add (ytmUsrHis.getYtmM0048()); }
			else if (i==9)  { ytmList.add (ytmUsrHis.getYtmM0060()); }
			else if (i==10) { ytmList.add (ytmUsrHis.getYtmM0084()); }
			else if (i==11) { ytmList.add (ytmUsrHis.getYtmM0120()); }
			else if (i==12) { ytmList.add (ytmUsrHis.getYtmM0180()); }
			else if (i==13) { ytmList.add (ytmUsrHis.getYtmM0240()); }
			else if (i==14) { ytmList.add (ytmUsrHis.getYtmM0360()); }
			else {            ytmList.add (ytmUsrHis.getYtmM0600()); }
		}
			
		return IrCurveYtm.builder()
				.baseDate(ytmUsrHis.getBaseDate())
                .irCurve(ytmUsrHis.getIrCurve())
                .irCurveNm(ytmUsrHis.getIrCurveNm())
                .matCd(TenList.get(idx)) // 0~14 까지 반복 ?? 
                .ytm(round(StringUtil.objectToPrimitive(ytmList.get(idx), 0.0) * toReal, digit))
//                .modifiedBy(jobId + "Builder")
//                .updateDate(LocalDateTime.now())
				.build();
	}
	
	
	private static IrCurveYtm buildFromYtmUsr(IrCurveYtmUsr ytmUsr) {

		double toReal = 1;
		int    digit  = 7;    

		return IrCurveYtm.builder()
                .baseDate(ytmUsr.getBaseDate())
                .irCurve(ytmUsr.getIrCurve())
                .irCurveNm(ytmUsr.getIrCurveNm())
                .matCd(ytmUsr.getMatCd())
                .ytm(round(StringUtil.objectToPrimitive(ytmUsr.getYtm(), 0.0) * toReal, digit))
//                .modifiedBy(jobId + "Builder")
//                .updateDate(LocalDateTime.now())
				.build();

	}
}
