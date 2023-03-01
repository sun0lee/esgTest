package test;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.dao.CoEsgMetaDao;
import com.gof.dao.CoJobListDao;
import com.gof.dao.IrCurveDao;
import com.gof.entity.CoEsgMeta;
import com.gof.entity.CoJobInfo;
import com.gof.entity.CoJobList;
import com.gof.entity.IrCurve;
import com.gof.enums.EBoolean;
import com.gof.enums.ERunArgument;
import com.gof.process.Process;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class dbtest {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	private static Map<String, String>       azaz   = new LinkedHashMap<>();	
	
	public static void main(String[] args) {
	
		
		
//		List<IrCurve> irList= IrCurveDao.getIrCurveList();
//		
//		for( IrCurve aa : irList) {
//			System.out.println("aaa :" + aa.getIrCurveNm());
//		}

//		List<CoJobList> CoJobInfo= CoJobListDao.getCoJobList();
//		
//		for( CoJobList aa : CoJobInfo) {
//			System.out.println("aaa :" + aa.getJobNm() + " : " + aa.getJobName());
//		}
		
		List<CoEsgMeta> CoEsgMetaInfo = CoEsgMetaDao.getCoEsgMeta("PROPERTIES");
		
		for( CoEsgMeta aa : CoEsgMetaInfo) {
			System.out.println("aaa :" + aa.getGroupId() + " : " + aa.getParamKey() + aa.getParamValue());
		}
		
		azaz = CoEsgMetaDao.getCoEsgMeta("PROPERTIES")
				           .stream()
				           .collect(toMap(s->s.getParamKey(), s->s.getParamValue()));	
		
		log.info("azaz: {}", azaz);	
		
		
	}

}
