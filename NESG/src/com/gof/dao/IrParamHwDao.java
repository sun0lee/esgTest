package com.gof.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwCalc;
import com.gof.entity.IrParamHwUsr;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;
import com.gof.enums.EHwParamTypCd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

public class IrParamHwDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<IrParamHwUsr> getIrParamHwUsrList(String bssd) {
		
		String query = " select a from IrParamHwUsr a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd    "				 
				 	 ;		
		
		return session.createQuery(query, IrParamHwUsr.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	
	
	
	public static List<IrParamHwUsr> getIrParamHwUsrList(String bssd, EApplBizDv applBizDv, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamHwUsr a    "
				 	 + "  where 1=1                      " 
				 	 + "    and a.baseYymm  = :bssd      "
//				 	 + "    and a.applBizDv = :applBizDv "
//				 	 + "    and a.irModelNm = :irModelNm "
//				 	 + "    and a.irCurveNm = :irCurveNm "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwUsr.class)
				      .setParameter("bssd", bssd)
//				      .setParameter("applBizDv", applBizDv)
//				      .setParameter("irModelNm", irModelNm)
//				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();
	}		
	
@Deprecated
	public static List<IrParamHwBiz> getIrParamHwBizFromUsrList(String bssd) {		
		return getIrParamHwUsrList(bssd).stream().map(s -> s.convert()).collect(Collectors.toList());
	}		

	@Deprecated
	public static List<IrParamHwCalc> getIrParamHwCalcList(String bssd) {
		
		String query = " select a from IrParamHwCalc a "
				 	 + "  where 1=1                    "
				 	 + "    and a.baseYymm = :bssd     "				 
				 	 ;		
		
		return session.createQuery(query, IrParamHwCalc.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	
	
	
	//both HW1F_NSP and HW1F_SP(just Counting in JOB: Esg330)
	public static List<IrParamHwCalc> getIrParamHwCalcList(String bssd, String irCurveNm) {
		
		String query = " select a from IrParamHwCalc a      "
				 	 + "  where 1=1                         " 
				 	 + "    and a.baseYymm  = :bssd         "
				 	 + "    and a.irCurveNm = :irCurveNm    "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwCalc.class)
				      .setParameter("bssd", bssd)			
				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();
	}
	
	
	public static List<IrParamHwCalc> getIrParamHwCalcList(String bssd, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamHwCalc a      "
				 	 + "  where 1=1                         " 
				 	 + "    and a.baseYymm  = :bssd         "
				 	 + "    and a.irModelNm  = :irModelNm   "
//				 	 + "    and a.irModelId like :irModelId "
				 	 + "    and a.irCurveNm = :irCurveNm    "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwCalc.class)
				      .setParameter("bssd", bssd)			
				      .setParameter("irModelNm", irModelNm)
//				      .setParameter("irModelId", "%"+irModelId+"%")
				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();
	}
	
	
	public static List<IrParamHwCalc> getIrParamHwCalcHisList(String bssd, EIrModel irModelNm, String irCurveNm, EHwParamTypCd paramTypCd, int monthNum, String matCd) {
		
		String query = "select a from IrParamHwCalc a      "
					 + " where 1=1                         "
					 + "   and a.baseYymm   > :stDate      "
					 + "   and a.baseYymm  <= :endDate     "
					 + "   and a.irModelNm  = :irModelNm   "
					 + "   and a.irCurveNm  = :irCurveNm   "
					 + "   and a.paramTypCd = :paramTypCd  "					 
					 + "   and a.matCd      = :matCd       " 
					 ;
		
		return session.createQuery(query, IrParamHwCalc.class)
					  .setParameter("stDate", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("endDate", bssd)
					  .setParameter("irModelNm", irModelNm)
//					  .setParameter("irModelId", "%"+irModelId+"%")
					  .setParameter("irCurveNm", irCurveNm)
					  .setParameter("paramTypCd", paramTypCd)
					  .setParameter("matCd", matCd)
					  .getResultList();
	}	
	
	
	public static List<IrParamHwBiz> getIrParamHwBizList(String bssd, EApplBizDv applBizDv, EIrModel irModelNm, String irCurveNm) {
		
		String query = " select a from IrParamHwBiz a    "
				 	 + "  where 1=1                      " 
				 	 + "    and a.baseYymm  = :bssd      "
				 	 + "    and a.applBizDv = :applBizDv "
				 	 + "    and a.irModelNm = :irModelNm "
				 	 + "    and a.irCurveNm = :irCurveNm "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwBiz.class)
				      .setParameter("bssd", bssd)
				      .setParameter("applBizDv", applBizDv)
				      .setParameter("irModelNm", irModelNm)
				      .setParameter("irCurveNm", irCurveNm)
				      .getResultList();
	}
	
}
