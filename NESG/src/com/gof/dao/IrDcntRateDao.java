package com.gof.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRate;
import com.gof.entity.IrDcntRateBiz;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrDcntRateUsr;
import com.gof.util.HibernateUtil;

public class IrDcntRateDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<IrDcntRateBu> getIrDcntRateBuList(String bssd) {
		
		String query = " select a from IrDcntRateBu a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd "
				 	 ;		
		
		return session.createQuery(query, IrDcntRateBu.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	

	
	public static List<IrDcntRateBu> getIrDcntRateBuList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo){
		
		String query = "select a from IrDcntRateBu a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.applBizDv    = :applBizDv    "
					 + "   and a.irCurveNm    = :irCurveNm    "
					 + "   and a.irCurveSceNo = :irCurveSceNo "
					 ;
		
		return session.createQuery(query, IrDcntRateBu.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveNm", irCurveNm)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
					  .getResultList();
	}
	
	
	public static List<IrCurveSpot> getIrDcntRateBuToAdjSpotList(String bssd) {		
		return getIrDcntRateBuList(bssd).stream().map(s -> s.convertAdj()).collect(Collectors.toList());
	}
	
	
	public static List<IrCurveSpot> getIrDcntRateBuToAdjSpotList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo) {		
		return getIrDcntRateBuList(bssd, applBizDv, irCurveNm, irCurveSceNo).stream().map(s -> s.convertAdj()).collect(Collectors.toList());
	}	


	public static List<IrCurveSpot> getIrDcntRateBuToBaseSpotList(String bssd) {		
		return getIrDcntRateBuList(bssd).stream().map(s -> s.convertBase()).collect(Collectors.toList());
	}
	
	
	public static List<IrCurveSpot> getIrDcntRateBuToBaseSpotList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo) {		
		return getIrDcntRateBuList(bssd, applBizDv, irCurveNm, irCurveSceNo).stream().map(s -> s.convertBase()).collect(Collectors.toList());
	}	
	
	
	/////////////////////////////////////////////////////////////////////////
	
	public static List<IrCurveSpot> getIrDcntRateToAdjSpotList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo) {		
		return getIrDcntRateList(bssd, applBizDv, irCurveNm, irCurveSceNo).stream().map(s -> s.convertAdjSpot()).collect(Collectors.toList());
	}	
	
	
	public static List<IrDcntRate> getIrDcntRateList(String bssd) {
		
		String query = " select a from IrDcntRate a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd "
				 	 ;		
		
		return session.createQuery(query, IrDcntRate.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	

	
	public static List<IrDcntRate> getIrDcntRateList(String bssd, String applBizDv) {
		
		String query = " select a from IrDcntRate a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd       "
				 	 + "    and a.applBizDv = :applBizDv "
				 	 ;		
		
		return session.createQuery(query, IrDcntRate.class)
				      .setParameter("bssd", bssd)
				      .setParameter("applBizDv", applBizDv)
				      .getResultList();
	}	
	
	
	public static List<IrDcntRate> getIrDcntRateList(String bssd, String applBizDv, String irCurveNm){
		
		String query = "select a from IrDcntRate a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd      "
					 + "   and a.applBizDv    = :applBizDv "
					 + "   and a.irCurveNm    = :irCurveNm "
					 ;
		
		return session.createQuery(query, IrDcntRate.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveNm", irCurveNm)			      	  
					  .getResultList();
	}
	
	
	public static List<IrDcntRate> getIrDcntRateList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo){
		
		String query = "select a from IrDcntRate a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.applBizDv    = :applBizDv    "
					 + "   and a.irCurveNm    = :irCurveNm    "
					 + "   and a.irCurveSceNo = :irCurveSceNo "
					 ;
		
		return session.createQuery(query, IrDcntRate.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveNm", irCurveNm)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
					  .getResultList();
	}
	

	public static List<IrDcntRate> getIrDcntRateList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo, List<String> tenorList){
		
		String query = "select a from IrDcntRate a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.applBizDv    = :applBizDv    "
					 + "   and a.irCurveNm    = :irCurveNm    "
					 + "   and a.irCurveSceNo = :irCurveSceNo "
					 + "   and a.matCd in (:matCdList)        "
					 ;
		
		return session.createQuery(query, IrDcntRate.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveNm", irCurveNm)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
			      	  .setParameterList("matCdList", tenorList)
					  .getResultList();
	}
	
	
	public static List<IrDcntRateUsr> getIrDcntRateUsrList(String bssd) {
		
		String query = " select a from IrDcntRateUsr a "
				 	 + "  where 1=1                    "
				 	 + "    and a.baseYymm = :bssd     "
				 	 ;		
		
		return session.createQuery(query, IrDcntRateUsr.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}
	
	
	public static List<IrDcntRateBiz> getIrDcntRateBizAdjSpotList(String bssd) {		
		return getIrDcntRateList(bssd).stream().map(s -> s.convertAdj()).collect(Collectors.toList());
	}
	
	
	public static List<IrDcntRateBiz> getIrDcntRateBizAdjSpotList(String bssd, String applBizDv) {		
		return getIrDcntRateList(bssd, applBizDv).stream().map(s -> s.convertAdj()).collect(Collectors.toList());
	}	
	

	public static List<IrDcntRateBiz> getIrDcntRateBizAdjSpotList(String bssd, String applBizDv, String irCurveNm) {		
		return getIrDcntRateList(bssd, applBizDv, irCurveNm).stream().map(s -> s.convertAdj()).collect(Collectors.toList());
	}	

	
	public static List<IrDcntRateBiz> getIrDcntRateBizBaseSpotList(String bssd) {		
		return getIrDcntRateList(bssd).stream().map(s -> s.convertBase()).collect(Collectors.toList());
	}
	
	
	public static List<IrDcntRateBiz> getIrDcntRateBizBaseSpotList(String bssd, String applBizDv) {		
		return getIrDcntRateList(bssd, applBizDv).stream().map(s -> s.convertBase()).collect(Collectors.toList());
	}
	
	
	public static List<IrDcntRateBiz> getIrDcntRateBizBaseSpotList(String bssd, String applBizDv, String irCurveNm) {		
		return getIrDcntRateList(bssd, applBizDv, irCurveNm).stream().map(s -> s.convertBase()).collect(Collectors.toList());
	}	
	
}
