package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrSprdCurve;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.interfaces.IRateInput;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveSpotDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	
	public static String getMaxBaseDate(String bssd, String irCurveNm) {
		
		String query = "select max(a.baseDate) "
					 + "from IrCurveSpot a "
					 + "where 1=1 "
					 + "and a.baseDate <= :bssd	"
					 + "and substr(a.baseDate,1,6) = substr(:bssd,1,6) "
					 + "and a.irCurveNm = :irCurveNm "					 
					 ;
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				 			 	.setParameter("irCurveNm", irCurveNm)
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("IR Curve History Data is not found {} at {}" , irCurveNm, FinUtils.toEndOfMonth(bssd));
			return bssd;
		}		
		
		return maxDate.toString();
	}

	public static List<IrCurveSpot> getIrCurveSpotListHis(String bssd, String stBssd, IrCurve irCurve, List<String> tenorList) {
		
		String query = " select a from IrCurveSpot a    " 
					 + "  where a.irCurveNm =:irCurveNm "			
					 + "    and a.baseDate >= :stBssd   "
					 + "    and a.baseDate <= :bssd     "
					 + "    and a.matCd in (:matCdList) "
					 + "  order by a.baseDate, a.matCd  "
					 ;	
		
		return session.createQuery(query, IrCurveSpot.class)
					  .setParameter("irCurveNm", irCurve.getIrCurveNm())
					  .setParameter("stBssd", stBssd)
					  .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
					  .setParameterList("matCdList", tenorList)
					  .getResultList()
					  ;
	}
	

	public static List<IRateInput> getIrCurveSpot(String bssd, String irCurveNm, List<String> tenorList) {
		
		session.clear();
		
		String query = "select a from IrCurveSpot a    "
					 + " where 1=1                     "
					 + "   and a.irCurveNm =:irCurveNm "
					 + "   and a.baseDate  = :bssd	   "
					 + "   and a.matCd in (:matCdList) "
					 + " order by a.matCd              "
					 ;
		
		List<IRateInput> curveRst = session.createQuery(query, IRateInput.class)
											.setParameter("irCurveNm", irCurveNm)
											.setParameter("bssd", getMaxBaseDate(bssd, irCurveNm))
											.setParameterList("matCdList", tenorList)
											.getResultList()
											;
		return curveRst;
	}	
	
	// 23.04.07 add 
	public static List<IRateInput> getIrCurveSpot(String bssd, String irCurveNm, List<String> tenorList, Double adjSpred) {
		
		session.clear();
		
		String query = "select new com.gof.entity.IRateInput a.baseDate, a.irCurveNm, a.irCurveSid, a.matCd , a.spotRate + :adjSpread as spotRate  " // 이게 될까? 
				+ "from IrCurveSpot a    "
				+ " where 1=1                     "
				+ "   and a.irCurveNm =:irCurveNm "
				+ "   and a.baseDate  = :bssd	   "
				+ "   and a.matCd in (:matCdList) "
				+ " order by a.matCd              "
				;
		
		List<IRateInput> curveRst = session.createQuery(query, IRateInput.class)
				.setParameter("irCurveNm", irCurveNm)
				.setParameter("bssd", getMaxBaseDate(bssd, irCurveNm))
				.setParameterList("matCdList", tenorList)
				.setParameter("adjSpread", adjSpred)
				.getResultList()
				;
		return curveRst;
	}	
	
	//Warning for delete command...  It resolve to append "'" ?  (.append("'")) 
	public static void deleteIrCurveSpotMonth(String bssd, String irCurveNm) {		
		
		StringBuilder sb = new StringBuilder();		
		sb.append("delete IrCurveSpot a where 1=1 ")
		  .append(" and ").append(" substr(a.baseDate,1,6) ").append(" = ").append("'").append(bssd).append("'")
		  .append(" and ").append(" a.irCurveNm ").append(" = ").append("'").append(irCurveNm).append("'")
		  ;
	
//		log.info("Delete Qry: {}", sb);
	    session.beginTransaction();		
		session.createQuery(sb.toString()).executeUpdate();		
		
		session.getTransaction().commit();
		log.info("{} have been Deleted: [BASE_YYMM: {}, IR_CURVE_NM: {}]", log.getName(), bssd, irCurveNm);		
	}

	
	public static List<String> getIrCurveTenorList(String bssd, String irCurveNm){
		
		String query = "select a.matCd from IrCurveSpot a "
					 + " where 1=1                        "
					 + "   and a.baseDate  =:baseYmd      "
					 + "   and a.irCurveNm =:irCurveNm    "
					 ;
		
		return session.createQuery(query, String.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveNm))
				 	  .setParameter("irCurveNm", irCurveNm)
					  .getResultList();
	}
	
	
	public static List<String> getIrCurveTenorList(String bssd, String irCurveNm, Integer llp){
		
		String query = "select a.matCd from IrCurveSpot a                 "
					 + " where 1=1                                        "
					 + "   and a.baseDate  =:baseYmd                      "
					 + "   and a.irCurveNm =:irCurveNm                    "
					 + "   and to_number(substr(a.matCd, 2)) <= :llp * 12 "
					 ;
		
		return session.createQuery(query, String.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveNm))
				 	  .setParameter("irCurveNm", irCurveNm)
				 	  .setParameter("llp", llp)
					  .getResultList();
	}

	
	public static List<IrCurveSpot> getIrCurveSpot(String bssd, String irCurveNm) {
		
		String query = "select a from IrCurveSpot a     "
					 + " where 1=1                      "
					 + "   and a.baseDate  = :baseYmd	"
					 + "   and a.irCurveNm = :irCurveNm "
					 + " order by a.matCd               "
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveNm))
				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();		
	}	
		
	
	public static List<IrSprdCurve> getIrSprdCurve(String bssd, String irCurveNm){
		
		String query = "select a from IrSprdCurve a "
					 + " where 1=1 "
					 + "   and a.baseYymm  =:bssd "
					 + "   and a.irCurveNm =:irCurveNm "
					 + "   and a.irTypDvCd =:irTypDvCd "
					 ;
		
		return session.createQuery(query, IrSprdCurve.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irCurveNm", irCurveNm)
			      	  .setParameter("irTypDvCd", "1")
					  .getResultList();
	}	
	
	
}
