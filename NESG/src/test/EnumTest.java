package test;

import java.util.Arrays;
import java.util.EnumSet;

import com.gof.enums.EApplBizDv;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EnumTest {
	public static void main(String[] args) {

	
//	Arrays.stream(EApplBizDv.values())
//	     .filter(s->s.isBizDv == true) // 바깥에서는 모름 왜냐 private로 정의했기 때문.
//	     .forEach(System.out::println);
		
//		for(EApplBizDv aa : EApplBizDv.values()) {
//
//			 log.info("aaa : {},{},{}"
//					 , aa.name()
//					 , aa.isBizDv()
//					 , aa.ordinal());
//		}
		
//		for(EApplBizDv aa : EApplBizDv.getUseBizList()) {
//
//			 log.info("aaa : {},{},{}"
//					 , aa.name()
//					 , aa.isBizDv()
//					 , aa.ordinal());
//		}
        
		log.info("aaa : {}" , EApplBizDv.getApplBizDetDv(EApplBizDv.KICS, "A"));
		
		EnumSet<EApplBizDv> bizDvs = EnumSet.of(EApplBizDv.KICS);
		log.info("aa : {}", bizDvs.size());
	}
	
}