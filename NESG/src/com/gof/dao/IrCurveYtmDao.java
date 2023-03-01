package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrCurveYtmUsr;
import com.gof.entity.IrCurveYtmUsrHis;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveYtmDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static String getMaxBaseDate(String bssd, String irCurveNm) {
		
		String query = "select max(a.baseDate) "
					 + "  from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and a.irCurveNm = :irCurveNm                   "
					 + "   and a.baseDate <= :bssd                        "	
					 + "   and substr(a.baseDate,1,6) = substr(:bssd,1,6) "
					 ;
		
		Object maxDate =  session.createQuery(query)
				 				 .setParameter("irCurveNm", irCurveNm)			
								 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
								 .uniqueResult();
		
		if(maxDate==null) {
			log.warn("IR Curve YTM History Data is not found {} at {}" , irCurveNm, FinUtils.toEndOfMonth(bssd));
			return FinUtils.toEndOfMonth(bssd);
		}
		
		return maxDate.toString();
	}
	
	//Used in job ESG270(SW BootStrapping Interpolation of Asset Discount Rate) 
	public static List<IrCurveYtm> getIrCurveYtm(String bssd, String irCurveNm) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and a.irCurveNm = :irCurveNm "
					 + "   and a.baseDate  = :bssd	    "
					 + "   and a.ytm is not null        "
					 + "   order by a.matCd             "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
										   .setParameter("irCurveNm", irCurveNm)
										   .setParameter("bssd", getMaxBaseDate(bssd, irCurveNm))
										   .getResultList();
		
//		log.info("maxDate : {}, curveSize : {}", getMaxBaseDate(bssd, irCurveNm),curveRst.size());
		return curveRst;
	}
	
	
	public static List<IrCurveYtm> getIrCurveYtm(String bssd, String irCurveNm, List<String> tenorList) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and a.irCurveNm = :irCurveNm "
					 + "   and a.baseDate  = :bssd	    "
					 + "   and a.ytm is not null        "
					 + "   and a.matCd in (:matCdList)  "
					 + " order by a.matCd               "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
										   .setParameter("irCurveNm", irCurveNm)
										   .setParameter("bssd", getMaxBaseDate(bssd, irCurveNm))
										   .setParameterList("matCdList", tenorList)
										   .getResultList();		

		return curveRst;
	}	
	
	
	public static List<IrCurveYtm> getIrCurveYtmHis(String bssd, String irCurveNm, int monthNum, String matCd) {
		
		String query = "select a from IrCurveYtm a                    "
					 + " where 1=1                                    "
					 + "   and substr(a.baseDate, 1, 6) >  :stYymm    "
					 + "   and substr(a.baseDate, 1, 6) <= :endYymm   "
					 + "   and a.irCurveNm               = :irCurveNm "					 
					 + "   and a.matCd                   = :matCd     "
					 + " order by a.baseDate                          " 
					 ;
		
		return session.createQuery(query, IrCurveYtm.class)
					  .setParameter("stYymm", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("endYymm", bssd)
					  .setParameter("irCurveNm", irCurveNm)
					  .setParameter("matCd", matCd)
					  .getResultList();
	}		
	
	
	public static List<IrCurveYtm> getIrCurveYtmMonth(String bssd, String irCurveNm) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and substr(a.baseDate,1,6) = :bssd "
					 + "   and a.irCurveNm = :irCurveNm       "
					 + "   and a.ytm is not null              "
					 + " order by a.matCd                     "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
									 	   .setParameter("irCurveNm", irCurveNm)
										   .setParameter("bssd", bssd)
										   .getResultList();
		
		return curveRst;
	}	
	
	
	public static List<IrCurveYtm> getIrCurveYtmMonth(String bssd, String irCurveNm, List<String> tenorList) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "					 
					 + "   and substr(a.baseDate,1,6)  = :bssd "
					 + "   and a.irCurveNm = :irCurveNm        "
					 + "   and a.ytm is not null               "
					 + "   and a.matCd in (:matCdList)         "
					 + " order by a.matCd                      "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
										   .setParameter("irCurveNm", irCurveNm)
										   .setParameter("bssd", bssd)
										   .setParameterList("matCdList", tenorList)
										   .getResultList();		
		
		return curveRst;
	}	
	
	
	public static List<IrCurveYtmUsrHis> getIrCurveYtmUsrHis(String bssd, String irCurveNm) {
		
		String query = " select a from IrCurveYtmUsrHis a      " 
					 + "  where 1=1                            "
					 + "    and substr(a.baseDate,1,6) = :bssd "
					 + "    and a.irCurveNm = :irCurveNm       "
					 + "  order by a.baseDate                  "				
		 		 	 ;
		
		return session.createQuery(query, IrCurveYtmUsrHis.class)
					  .setParameter("bssd", bssd)
					  .setParameter("irCurveNm", irCurveNm)
					  .getResultList()
					  ;
	}	

	
	public static List<IrCurveYtmUsr> getIrCurveYtmUsr(String bssd, String irCurveNm) {
		
		String query = " select a from IrCurveYtmUsr a         " 
					 + "  where 1=1                            "
					 + "    and substr(a.baseDate,1,6) = :bssd "
					 + "    and a.irCurveNm = :irCurveNm       "
					 + "  order by a.baseDate                  "				
		 		 	 ;
		
		return session.createQuery(query, IrCurveYtmUsr.class)
					  .setParameter("bssd", bssd)
					  .setParameter("irCurveNm", irCurveNm)
					  .getResultList()
					  ;
	}	
	
}
