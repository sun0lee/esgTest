package test;

import com.gof.enums.EApplBizDv;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EnumTest {
	public static void main(String[] args) {

	
		
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
		
	}
	
}
