package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpotWeek;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveSpotWeekDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	

	public static List<IrCurveSpotWeek> getIrCurveSpotWeekHis(String bssd, String stBssd, IrCurve irCurve, List<String> tenorList, Integer weekDay, Boolean bizDayOnly){
		
		String weekName = getWeekDayName(weekDay); 
		String query = null;
		
		if(bizDayOnly) {		
			query       = " select a from IrCurveSpotWeek a " 
						+ "  where 1=1					    "			
						+ "    and a.baseDate <= :bssd      "
						+ "    and a.baseDate >= :stBssd    "
						+ "    and a.irCurveNm = :irCurveNm "
						+ "    and a.matCd in (:matCdList)  "
						+ "    and a.dayOfWeek = :weekName  "
						+ "    and a.bizDayType = 'Y'       "
						+ "  order by a.baseDate, a.matCd   "
						;
		}
		else {
			query       = " select a from IrCurveSpotWeek a " 
						+ "  where 1=1					    "			
						+ "    and a.baseDate <= :bssd      "
						+ "    and a.baseDate >= :stBssd    "
						+ "    and a.irCurveNm = :irCurveNm "
						+ "    and a.matCd in (:matCdList)  "
						+ "    and a.dayOfWeek = :weekName  "
						+ "  order by a.baseDate, a.matCd   "
						;
		}		
		
		return session.createQuery(query, IrCurveSpotWeek.class)
					  .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
					  .setParameter("stBssd", stBssd)
					  .setParameter("irCurveNm", irCurve.getIrCurveNm())								
					  .setParameterList("matCdList", tenorList)
					  .setParameter("weekName", weekName)
					  .getResultList()
					  ;
	}
	
	
	private static String getWeekDayName(int weekDay) {
		
		switch(weekDay) {
			case 1:  return "MONDAY";
			case 2:  return "TUESDAY";
			case 3:  return "WEDNESDAY";
			case 4:  return "THURSDAY";
			case 5:  return "FRIDAY";
			case 6:  return "SATURDAY";
			case 7:  return "SUNDAY";
			default: return "FRIDAY";			
		}
	}
	
}
