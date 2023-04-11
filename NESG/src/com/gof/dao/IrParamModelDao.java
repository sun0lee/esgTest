package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamModelBiz;
import com.gof.entity.IrParamModelCalc;
import com.gof.entity.IrParamModelUsr;
import com.gof.enums.EBoolean;
import com.gof.enums.EIrModel;
import com.gof.util.HibernateUtil;

public class IrParamModelDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<IrParamModel> getParamModelList(EIrModel irModelNm) {
		
		String q = " select a from IrParamModel a     "
				+ "  where 1=1                       "
//				+ "    and irModelNm like :irModelNm "
				+ "    and irModelNm = :irModelNm "
				+ "    and a.useYn = :useYn          "
;		
		
		return session.createQuery(q, IrParamModel.class)
//				.setParameter("irModelNm" , "%"+irModelNm+"%") 
// 23.04.10 ENUM으로 정의한 값은 like 쿼리 사용 불가. 의미상으로 like를 쓸게 아니라 열거타입에 추가하고 구분자를 생성해서 사용하기  
				.setParameter("irModelNm" , irModelNm)
				.setParameter("useYn"     , EBoolean.Y)
				.getResultList();
	}
	
	@Deprecated
	public static List<IrParamModelBiz> getParamModelBizList(String bssd, EIrModel irModelNm, String irCurveNm) {
		
		String q = " select a from IrParamModelBiz a  "
				 + "  where 1=1                       "
				 + "    and baseYymm  = :baseYymm     "
				 + "    and irModelNm = :irModelNm    "
				 + "    and irCurveNm = :irCurveNm    "
				 ;		
		
		return session.createQuery(q, IrParamModelBiz.class)
				      .setParameter("baseYymm"  , bssd     )
				      .setParameter("irModelNm" , irModelNm)
				      .setParameter("irCurveNm" , irCurveNm)
					  .getResultList();
	}	
	
	@Deprecated
	public static List<IrParamModelUsr> getIrParamModelUsrList(String bssd, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamModelUsr a                    "
				 	 + "  where 1=1                                         " 
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm "				 	 
				 	 + "    and a.irModelNm = :irModelNm                    "
				 	 + "    and a.irCurveNm = :irCurveNm                    "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamModelUsr.class)
				      .setParameter("bssd", bssd)				      
				      .setParameter("irModelNm", irModelNm)
				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();
	}	
	
	@Deprecated
	public static List<IrParamModelCalc> getIrParamModelCalcList(String bssd, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamModelCalc a   "
				 	 + "  where 1=1                         " 
				 	 + "    and a.baseYymm  = :bssd         "
				 	 + "    and a.irModelNm  = :irModelNm   "
				 	 + "    and a.irCurveNm = :irCurveNm    "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamModelCalc.class)
				      .setParameter("bssd", bssd)			
				      .setParameter("irModelNm", irModelNm)
				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();
	}		
	
}
