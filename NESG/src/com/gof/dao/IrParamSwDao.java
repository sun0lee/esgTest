package com.gof.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrParamSw;
import com.gof.entity.IrParamSwUsr;
import com.gof.util.HibernateUtil;

public class IrParamSwDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();	
	
	public static List<IrParamSwUsr> getIrParamSwUsrList(String bssd) {
		
		String query = " select a from IrParamSwUsr a 						"
				 	 + "  where 1=1 										"
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm "
				 	 + "  order by a.irCurveNm, a.irCurveSceNo 				"
				 	 ;		
		
		return session.createQuery(query, IrParamSwUsr.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	
	

	public static List<IrParamSw> getIrParamSwList(String bssd) {		
		return getIrParamSwUsrList(bssd).stream().map(s -> s.convert(bssd)).collect(Collectors.toList());
	}		

	
	public static List<IrParamSwUsr> getIrParamSwUsrList(String bssd, List<String> irCurveNmList) {
		
		String query = " select a from IrParamSwUsr a 						              "
				 	 + "  where 1=1 										              " 
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm               "
				 	 + "    and a.irCurveNm in (:irCurveNmList) 			              "
				 	 + "  order by a.applStYymm, a.applBizDv, a.irCurveNm, a.irCurveSceNo "
				 	 ;		
		
		return session.createQuery(query, IrParamSwUsr.class)
				      .setParameter("bssd", bssd)
				      .setParameterList("irCurveNmList", irCurveNmList)
				      .getResultList();
	}	
	

}
