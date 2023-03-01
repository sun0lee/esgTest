package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrVolSwpn;
import com.gof.entity.IrVolSwpnUsr;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrVolSwpnDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<IrVolSwpnUsr> getSwpnVolUsr(String bssd, String irCurveNm, List<String> swpnMatList) {
		
		String baseDate = getMaxBaseDate(bssd, irCurveNm);
//		23.02.27 왜 여기서 가만히 있지 ???
//		String baseDate = "20210331";
		
		String query = " select a from IrVolSwpnUsr a       " 
		 		 	 + "  where a.baseDate  = :baseDate     "
		 		 	 + "    and a.irCurveNm= :irCurveNm    "
		 		 	 + "    and a.swpnMat in (:swpnMatList) "
		 		 	 + "  order by a.swpnMat                "		 		 
		 		 	 ;
		
		return session.createQuery(query, IrVolSwpnUsr.class)
					  .setParameter("baseDate", baseDate)
					  .setParameter("irCurveNm", irCurveNm)
					  .setParameterList("swpnMatList", swpnMatList)
					  .getResultList()
					  ;
	}
	
	
	public static String getMaxBaseDate(String bssd, String irCurveNm) {
		
		String query = "select max(a.baseDate)               "
					 + "from IrVolSwpnUsr a                  "
					 + "where 1=1                            "
					 + "and substr(a.baseDate, 1, 6) = :bssd "
					 + "and a.irCurveNm = :irCurveNm         "					 
					 ;		
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", bssd)
				 			 	.setParameter("irCurveNm", irCurveNm)
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("Swaption Volatility is not found [IR_CURVE_NM: {}] at [{}]" , irCurveNm, bssd);
			return bssd;
		}		
		
		return maxDate.toString();
	}	
	
	
	public static List<IrVolSwpn> getSwpnVol(String bssd, String irCurveNm) {
		
		String query = " select a from IrVolSwpn a       	  " 
		 		 	 + "  where a.baseYymm = :bssd       	  "
		 		 	 + "    and a.irCurveNm = :irCurveNm      "
		 		 	 + "  order by a.swpnMatNum, a.swapTenNum "		 		 
		 		 	 ;
		
		return session.createQuery(query, IrVolSwpn.class)
					  .setParameter("bssd", bssd)
					  .setParameter("irCurveNm", irCurveNm)
					  .getResultList()
					  ;
	}
	
	
	public static List<IrVolSwpn> getSwpnVol(String bssd, int monthNum, String irCurveNm) {
		
		String query = " select a from IrVolSwpn a            "
				 	 + "  where a.baseYymm = :bssd            "
		 		 	 + "    and a.irCurveNm = :irCurveNm      "
		 		 	 + "  order by a.swpnMatNum, a.swapTenNum "				
				 	 ;
		
		return session.createQuery(query, IrVolSwpn.class)
					  .setParameter("bssd", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("irCurveNm", irCurveNm)
					  .getResultList()
					  ;
	}
	
	
	public static List<IrVolSwpn> getSwpnVol(String bssd, int monthNum) {
		
		String query = " select a from IrVolSwpn a            "
				 	 + "  where a.baseYymm = :bssd            "
		 		 	 + "  order by a.swpnMatNum, a.swapTenNum "				
				 	 ;
		
		return session.createQuery(query, IrVolSwpn.class)
					  .setParameter("bssd", FinUtils.addMonth(bssd, monthNum))
					  .getResultList()
					  ;
	}	
	

	
	
}
