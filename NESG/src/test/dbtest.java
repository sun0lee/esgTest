package test;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;

import com.gof.dao.CoEsgMetaDao;
import com.gof.dao.CoJobListDao;
import com.gof.dao.IrCurveDao;
import com.gof.dao.IrCurveYtmDao;
import com.gof.entity.CoEsgMeta;
import com.gof.entity.CoJobInfo;
import com.gof.entity.CoJobList;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveYtm;
import com.gof.enums.EBoolean;
import com.gof.enums.ERunArgument;
import com.gof.process.Esg130_SetYtm;
import com.gof.process.Process;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class dbtest {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	private static Map<String, String>       azaz   = new LinkedHashMap<>();	
	
	public static void main(String[] args) {
	
		
		Stream<String> stream = Stream.of("aa","bb","cc","dd") ;
		stream.map(String::toUpperCase).forEach(System.out::println); 
		
		Stream<String> stream2 =
	    Stream.generate(()-> "gen").limit(10) ;
	
		Stream<String> favorateFood = Stream.ofNullable(System.getProperty("food")) ; 
//		stream.map(favorateFood::toUpperCase).forEach(System.out::println); 
		
		Stream<String> value 
		= Stream.of("aa","bb","cc").flatMap(key-> Stream.ofNullable(System.getProperty(key)));
		
		int[] numbers = {1,2,3,4,5,6,7,8};
		int sum = Arrays.stream(numbers).sum(); 
			
		
		Stream.iterate(1, n->n*2)
	    	  .limit(10)
	    	  .forEach(System.out::println) ;
		
		Stream.iterate(1, n-> n< 100, n->n*2) // 1은 초기값 
			  .forEach(System.out::println) ; //각각의 요소들을 출력해줘 
	}

}
