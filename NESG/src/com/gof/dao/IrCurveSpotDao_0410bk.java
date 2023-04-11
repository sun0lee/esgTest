package com.gof.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrSprdCurve;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.enums.EBoolean;
import com.gof.interfaces.IRateInput;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveSpotDao_0410bk extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	@Deprecated
	public static List<IrCurve> getIrCurveByCrdGrdCd(String crdCrdCd) {
		
		String query = "select a from IrCurve a "
					 + "where 1=1 "
					 + "and a.creditGrate = :crdGrdCd "
					 + "and a.useYn		  = :useYn "
					 + "and a.applMethDv  <> '6' "					
					 + "and a.refCurveId  is null "
					 ;
		
		return   session.createQuery(query, IrCurve.class)
								 .setParameter("crdGrdCd",crdCrdCd )				
								 .setParameter("useYn", EBoolean.Y )				
								 .getResultList();
								 
	}
	
	@Deprecated
	public static List<IrCurve> getBottomUpIrCurve() {
		return getIrCurveByGenMethod("4");
	}
	
	@Deprecated
	public static List<IrCurve> getIrCurveByGenMethod(String applMethDv) {
		
		String query = "select a from IrCurve a "
					 + "where 1=1 "
					 + "and a.applMethDv = :applMethDv "
					 + "and a.useYn = :useYn"
					 ;
		
		return   session.createQuery(query, IrCurve.class)
								 .setParameter("applMethDv",applMethDv)			// Bond Gen : 3, BottomUp : 4 , TopDown : 6, KICS : 5 SwapRate : 7
								 .setParameter("useYn", EBoolean.Y)				
								 .getResultList();
	}
	
	@Deprecated
	public static Map<String, String> getEomMap(String bssd, String irCurveNm) {
		
		String query = "select substring(a.baseDate, 0, 6), max(a.baseDate) "
					 + "from IrCurveSpot a "
					 + "where 1=1 "
					 + "and a.irCurveNm = :irCurveNm "
					 + "and a.baseDate <= :bssd	"
					 + "group by substring(a.baseDate, 0, 6)"
					 ;
		
		@SuppressWarnings("unchecked")
		List<Object[]> maxDate = session.createQuery(query)
				 						.setParameter("irCurveNm", irCurveNm)			
				 						.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				 						.getResultList();
		
//		if(maxDate == null) {
//			log.warn("IR Curve History Data is not found {} at {}" , irCurveNm, FinUtils.toEndOfMonth(bssd));
//			return new hashMap<String, String>;
//		}
		
		Map<String, String> rstMap = new HashMap<String, String>();
		for(Object[] aa : maxDate) {
			rstMap.put(aa[0].toString(), aa[1].toString());
		}
		return rstMap;
	}
	
	@Deprecated
	public static String getEomDate(String bssd, String irCurveNm) {
		
		String query = "select max(a.baseDate) "
				     + "from IrCurveSpot a "
				     + "where 1=1 "
				     + "and a.irCurveNm = :irCurveNm "
				     + "and a.baseDate >= :bom	"
				     + "and a.baseDate <= :eom	"
				     ;
		
		Object maxDate = session.createQuery(query)
				 				.setParameter("irCurveNm", irCurveNm)			
				 				.setParameter("bom", bssd)
								.setParameter("eom", FinUtils.toEndOfMonth(bssd))
								.uniqueResult();
		if(maxDate == null) {
			log.warn("IR Curve History Data is not found {} at {}" , irCurveNm, bssd);
			return bssd;
		}
		return maxDate.toString();
	}
	
	
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
	
	@Deprecated
	public static List<IrCurveSpot> getIrCurveSpotHis(String bssd, String irCurveNm, int monthNum, String matCd) {
		
		String query = "select a from IrCurveSpot a                   "
					 + " where 1=1                                    "
					 + "   and substr(a.baseDate, 1, 6) >  :stYymm    "
					 + "   and substr(a.baseDate, 1, 6) <= :endYymm   "
					 + "   and a.irCurveNm               = :irCurveNm "					 
					 + "   and a.matCd                   = :matCd     "
					 + " order by a.baseDate                          " 
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
					  .setParameter("stYymm", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("endYymm", bssd)
					  .setParameter("irCurveNm", irCurveNm)
					  .setParameter("matCd", matCd)
					  .getResultList();
	}		
	
    @Deprecated
	public static List<IrCurveSpot> getCurveHisBetween(String bssd, String stBssd, String curveId){
		String query = "select a from IrCurveSpot a "
				+ "where 1=1 "
				+ "and a.baseDate <= :bssd	"
				+ "and a.baseDate >= :stBssd "
				+ "and a.irCurveNm =:param1 "
//				+ "and a.matCd not in (:matCd1, :matCd2, :matCd3) "
				+ "order by a.baseDate"
				;
		
		List<IrCurveSpot> curveRst =  session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", curveId)
				.setParameter("bssd", FinUtils.addMonth(bssd, 1))
				.setParameter("stBssd", stBssd)
//				.setParameter("matCd1", "M0018")
//				.setParameter("matCd2", "M0030")
//				.setParameter("matCd3", "M0048")
				.getResultList();		
		
//		Map<String, Map<String, IrCurveHis>> curveMap = curveRst.stream().collect(Collectors.groupingBy(s -> s.getMatCd()
//				, Collectors.toMap(s-> s.getBaseYymm(), Function.identity(), (s,u)->u)));
//		curveMap.entrySet().forEach(s -> log.info("aaa : {},{},{}", s.getKey(), s.getValue()));
		return curveRst;
	}
	
	@Deprecated
	public static List<IrCurveSpot> getShortRateBtw(String stBssd, String bssd, String curveId){
		String query = "select a from IrCurveSpot a "
				+ "where 1=1 "
				+ "and a.baseDate <= :bssd	"
				+ "and a.baseDate >= :stBssd "
				+ "and a.irCurveNm =:param1 "
				+ "and a.matCd = :matCd "
				+ "order by a.baseDate desc"
				;
		
		List<IrCurveSpot> curveRst =  session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", curveId)
				.setParameter("stBssd", stBssd+"01")
				.setParameter("bssd", bssd+"31")
				.setParameter("matCd", "M0003")
				.getResultList();		
		
//		Map<String, Map<String, IrCurveHis>> curveMap = curveRst.stream().collect(Collectors.groupingBy(s -> s.getMatCd()
//				, Collectors.toMap(s-> s.getBaseYymm(), Function.identity(), (s,u)->u)));
//		curveMap.entrySet().forEach(s -> log.info("aaa : {},{},{}", s.getKey(), s.getValue()));
		return curveRst;
	}
	
	@Deprecated
	public static IrCurveSpot getShortRateHis(String bssd, String irCurveNm){
		String query = "select a from IrCurveSpot a "
				+ "where 1=1 "
				+ "and a.baseDate = :bssd	"
				+ "and a.irCurveNm =:param1 "
				+ "and a.matCd = :matCd "
				+ "order by a.baseDate"
				;
		
		IrCurveSpot curveRst =  session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", irCurveNm)
				.setParameter("bssd", getMaxBaseDate(bssd, irCurveNm))
				.setParameter("matCd", "M0003")
				.getSingleResult()
				;		
		

		return curveRst;
	}
	@Deprecated
	public static List<IrCurveSpot> getIrCurveHisByMaturityHis(String bssd, int monthNum, String irCurveNm, String matCd) {
		
		String query = "select a from IrCurveSpot a "
					 + "where 1=1 "
					 + "and a.irCurveNm =:param1 "
					 + "and a.baseDate >=:stBssd "
					 + "and a.baseDate <=:bssd "
					 + "and a.matCd =:param2 ";
					 ;
		
		return   session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", irCurveNm)
				.setParameter("stBssd", FinUtils.addMonth(bssd, monthNum)+"01")
				.setParameter("bssd", bssd+"31")
				.setParameter("param2", matCd)				
				.getResultList();
	}
	
	@Deprecated
	public static List<IrCurveSpot> getKTBMaturityHis(String bssd, String matCds){
//	public static List<IrCurveHis> getKTBMaturityHis(String bssd, String matCd1, String matCd2){
		String matCd1 = matCds.split(",")[0].trim();
		String matCd2 ="";
		if(matCds.split(",").length==2) {
			matCd2 =matCds.split(",")[1].trim();
		}
		
		String query = 	"select new com.gof.entity.IrCurveSpot (substr(a.baseDate,1,6), a.matCd, avg(a.intRate)) "
					+ "from IrCurveSpot a "
					+ "where 1=1 "
					+ "and a.baseDate <= :bssd	"
					+ "and a.irCurveNm =:param1 "
					+ "and a.matCd in (:param2, :param3) "
					+ "group by substr(a.baseDate,1,6), a.matCd "
					;
		
		List<IrCurveSpot> curveRst = session.createQuery(query, IrCurveSpot.class)
											.setParameter("param1", "A100")
											.setParameter("param2", matCd1)
											.setParameter("param3", matCd2)
											.setParameter("bssd", FinUtils.addMonth(bssd, 1))
											.getResultList();
		
		return curveRst;
	}
	
	@Deprecated
	public static List<IrCurveSpot> getKTBMaturityHis(String bssd, String matCd1, String matCd2) {
			
			String query = "select new com.gof.entity.IrCurveSpot (substr(a.baseDate,1,6), a.matCd, avg(a.intRate)) "
						+ "from IrCurveSpot a "
						+ "where 1=1 "
						+ "and a.baseDate <= :bssd	"
						+ "and a.irCurveNm =:param1 "
						+ "and a.matCd in (:param2, :param3) "
						+ "group by substr(a.baseDate,1,6), a.matCd "
						;
			
			List<IrCurveSpot> curveRst =  session.createQuery(query, IrCurveSpot.class)
					.setParameter("param1", "A100")
					.setParameter("param2", matCd1)
					.setParameter("param3", matCd2)
					.setParameter("bssd", FinUtils.addMonth(bssd, 1))
					.getResultList();		
			return curveRst;
	}
	
	@Deprecated
	public static Map<String, List<IrCurveSpot>> getIrCurveListTermStructure(String bssd, String stBssd, String irCurveNm) {
		
		String query =" select a from IrCurveSpot a " 
					+ "where a.irCurveNm =:irCurveNm "			
					+ "and a.baseDate >= :stBssd "
					+ "and a.baseDate <= :bssd "
					+ "and a.matCd in (:matCdList)"
					+ "order by a.baseDate, a.matCd "
					;
		
		return session.createQuery(query, IrCurveSpot.class)
				.setParameter("irCurveNm", irCurveNm)
				.setParameter("stBssd", stBssd)
				.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				.stream()
//				.collect(Collectors.groupingBy(s ->s.getBaseDate(), TreeMap::new, Collectors.toList()))
				.collect(Collectors.groupingBy(s ->s.getMatCd(), TreeMap::new, Collectors.toList()))
				;
	}
	

//	public static List<_IrSce> getIrCurveSce(String bssd, String irCurveNm) {
//		
//		String query ="select a from IrSce a " 
//					+ "where a.irCurveNm =:irCurveNm "			
//					+ "and a.baseDate = :bssd "
//				;
//		
//		return session.createQuery(query, _IrSce.class)
//				.setParameter("irCurveNm", irCurveNm)
//				.setParameter("bssd", bssd)
//				.setHint(QueryHints.HINT_READONLY, true)
//				.getResultList()
//				;
//	}


	@Deprecated
	public static List<IrCurveSpot> getEomTimeSeries(String bssd, String irCurveNm, String matCd, int monNum) {
		
		Collection<String> eomList = getEomMap(bssd, irCurveNm).values(); 

		String query = "select a from IrCurveSpot a "
				 	 + "where a.irCurveNm = :irCurveNm "
				 	 + "and a.baseDate > :stBssd "
				 	 + "and a.baseDate < :bssd "
				 	 + "and a.baseDate in :eomList "
				 	 + "and a.matCd = :matCd "
				 	 + "order by a.baseDate desc "
				 	 ;
		
		return   session.createQuery(query, IrCurveSpot.class)
				 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))			
				 .setParameter("stBssd", FinUtils.toEndOfMonth( FinUtils.addMonth(bssd, monNum)))				
				 .setParameter("irCurveNm", irCurveNm)				
				 .setParameter("matCd", matCd)				
				 .setParameter("eomList", eomList)	
				 .getResultList();
	}	

	@Deprecated
	public static List<IrCurveSpot> getIrCurveSpotListHis(String bssd, String stBssd, String irCurveNm) {
		
		String query = " select a from IrCurveSpot a    " 
					 + "  where a.irCurveNm =:irCurveNm "			
					 + "    and a.baseDate >= :stBssd   "
					 + "    and a.baseDate <= :bssd     "
					 + "  order by a.baseDate, a.matCd  "
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
					  .setParameter("irCurveNm", irCurveNm)
					  .setParameter("stBssd", stBssd)
					  .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
					  .getResultList()
					  ;
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
	
	@Deprecated
	public static void deleteIrCurveSpot(String baseYmd, IrCurve irCurve) {		
		
		StringBuilder sb = new StringBuilder();		
		sb.append("delete IrCurveSpot a where 1=1 ")
		  .append(" and ").append(" a.baseDate ").append(" = ").append("'").append(baseYmd).append("'")
		  .append(" and ").append(" a.irCurveNm ").append(" = ").append("'").append(irCurve.getIrCurveNm()).append("'")
		  ;
	
	    session.beginTransaction();		
		session.createQuery(sb.toString()).executeUpdate();		
		
		session.getTransaction().commit();
		log.info("{} have been Deleted: [BASE_DATE: {}, IR_CURVE_NM: {}]", log.getName(), baseYmd, irCurve.getIrCurveNm());		
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
	
	@Deprecated
	public static List<IrCurveSpot> getIrCurveSpot(String bssd, String irCurveNm, Integer llp){
		
		String query = "select a from IrCurveSpot a                       "
					 + " where 1=1                                        "
					 + "   and a.baseDate  = :baseYmd                     "
					 + "   and a.irCurveNm = :irCurveNm                   "
					 + "   and to_number(substr(a.matCd, 2)) <= :llp * 12 "
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
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
	

//	public static String getMaxBaseDateEom (String bssd, String irCurveNm) {
//		String query = "select max(a.baseDate) "
//					 + "from IrCurveSpot a "
//					 + "where 1=1 "
//					 + "and a.irCurveNm = :irCurveNm "
//					 + "and a.baseDate <= :bssd	"
//					 + "and substr(a.baseDate,1,6) = :bssd "
//					 ;
//		Object maxDate =  session.createQuery(query)
//				 				 .setParameter("irCurveNm", irCurveNm)			
//								 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
//								 .uniqueResult();
//		if(maxDate==null) {
//			log.warn("IR Curve History Data is not found {} at {}" , irCurveNm, FinUtils.toEndOfMonth(bssd));
//			return FinUtils.toEndOfMonth(bssd);
//		}		
//		
//		return maxDate.toString();
//	}	
	
}
