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
	
	
	//Currently this method is only used for job110	
	public static List<IrParamSw> getIrParamSwList(String bssd, List<String> irCurveNmList) {
		return getIrParamSwUsrList(bssd, irCurveNmList).stream().map(s -> s.convert(bssd)).collect(Collectors.toList());
	}

	
	public static List<IrParamSwUsr> getIrParamSwUsrList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo) {
		
		String applStYymm = getAppliedYymm(bssd, applBizDv, irCurveNm, irCurveSceNo);
		
		String query = " select a from IrParamSwUsr a 						"
				 	 + "  where 1=1 										" 
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm "
				 	 + "    and a.applBizDv    = :applBizDv    				"
				 	 + "    and a.irCurveNm    = :irCurveNm    				"
				 	 + "    and a.irCurveSceNo = :irCurveSceNo 				"
				 	 + "    and a.applStYymm   = :applStYymm 				"
				 	 + "  order by a.irCurveNm, a.irCurveSceNo 				"
				 	 ;		
		
		return session.createQuery(query, IrParamSwUsr.class)
				      .setParameter("bssd", bssd)
				      .setParameter("applBizDv", applBizDv)
				      .setParameter("irCurveNm", irCurveNm)
				      .setParameter("irCurveSceNo", irCurveSceNo)
				      .setParameter("applStYymm", applStYymm)
				      .getResultList();
	}		
	
	
	public static List<IrParamSw> getIrParamSwList(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo) {
		return getIrParamSwUsrList(bssd, applBizDv, irCurveNm, irCurveSceNo)
				.stream()
				.map(s -> s.convert(bssd)).collect(Collectors.toList());		
	}


	private static String getAppliedYymm(String bssd, String applBizDv, String irCurveNm, Integer irCurveSceNo) {		
		
		String query = " select max(a.applStYymm) from IrParamSwUsr a 		"
			     	 + "  where 1=1											"
			     	 + "    and :bssd between a.applStYymm and a.applEdYymm "
			     	 + "	and a.applBizDv    = :applBizDv 				"
			     	 + "	and a.irCurveNm    = :irCurveNm 				"
			     	 + "	and a.irCurveSceNo = :irCurveSceNo 				"
			     	 ;
	
		Object appYymm = session.createQuery(query)					
								.setParameter("bssd", bssd)
								.setParameter("applBizDv"   , applBizDv)
								.setParameter("irCurveNm"   , irCurveNm)
								.setParameter("irCurveSceNo", irCurveSceNo)
								.uniqueResult();
	
//		log.info("{}, {}, {}, {}, {}", bssd, applBizDv, irCurveNm, irCurveSceNo, appYymm);
		if(appYymm == null) {
//			log.warn("Apply YYYYMM for IrParamSwUsr is not found [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] at {}", applBizDv, irCurveNm, irCurveSceNo, bssd);
			return bssd;
		}		
		return appYymm.toString();		
	}

}
