package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrParamAfnsCalc;
import com.gof.enums.EIrModel;
import com.gof.util.HibernateUtil;

public class IrParamAfnsDao extends DaoUtil {
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	// 초기모수 읽어오기
	public static List<IrParamAfnsCalc> getIrParamAfnsCalcInitList(String bssd, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamAfnsCalc a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd    "				
				 	 + "    and a.irModelNm =:param2 "
				 	 + "    and a.irCurveNm =:param3 "
				 	 + "    and a.modifiedBy like :param4 "
				 	 ;		
		
		return session.createQuery(query, IrParamAfnsCalc.class)
				      .setParameter("bssd", bssd)
         			  .setParameter("param2", irModelNm)
         			  .setParameter("param3", irCurveNm)
         			  .setParameter("param4", "%"+"710"+"%")

				      .getResultList();
	}	
	
	// 최적화 모수 읽어오기
	public static List<IrParamAfnsCalc> getIrParamAfnsCalcList(String bssd, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamAfnsCalc a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd    "				
				 	 + "    and a.irModelNm =:param2 "
				 	 + "    and a.irCurveNm =:param3 "
				 	 + "    and a.modifiedBy like :param4 "
				 	 ;		
		
		return session.createQuery(query, IrParamAfnsCalc.class)
				      .setParameter("bssd", bssd)
         			  .setParameter("param2", irModelNm)
         			  .setParameter("param3", irCurveNm)
         			  .setParameter("param4", "%"+"720"+"%")

				      .getResultList();
	}
}
