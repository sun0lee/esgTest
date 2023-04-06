package com.gof.enums;
import java.util.List;

//import static java.util.stream.Collectors.toList;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum EApplBizDv {
		
     KICS   (true)
   , KICS_A (false, EApplBizDv.KICS, "A")  
   , KICS_L (false, EApplBizDv.KICS, "L") 
   , IFRS   (true)
   , IFRS_A (false, EApplBizDv.IFRS,"A")
   , IFRS_L (false, EApplBizDv.IFRS,"L")
   , IBIZ   (true)
   , IBIZ_A (false, EApplBizDv.IBIZ,"A")
   , IBIZ_L (false, EApplBizDv.IBIZ,"L")
   , SAAS   (true)
   , SAAS_A (false, EApplBizDv.SAAS,"A")
   , SAAS_L (false, EApplBizDv.SAAS,"L")
   ;

   private boolean isUpperBizDv ;
   private EApplBizDv upperBizDv ;
   private String assetLiabDv ;
 

   private EApplBizDv(boolean isUpper) {
	  this.isUpperBizDv = isUpper ;
   }
   
   // enum에 private생성자를 무조건 만들어야 함 -> enum에 정의한 항목 1개씩 인스턴스를 생성해서 그것을 상수처럼 쓰는 방식이라 그렇게 해야 함. 
   // public이 아닌 이유는 바깥에서 enum 을 통제하지 못하도록, 밖에서는 정의된 enum을 받아서 믿고 쓰는 것.
  private EApplBizDv( boolean isUpper, EApplBizDv upperBizDv, String assetLiabDv) {
	  this.isUpperBizDv = isUpper ;
	  this.upperBizDv = upperBizDv;
	  this.assetLiabDv = assetLiabDv;
}

  // static (정적) 메서드 : 인스턴스화 없이 클래스이름으로 직접 호출 가능함.
  // 객체와 무관하게 정의되므로 전역함수처럼 쓸 수 있음. 
public static List<EApplBizDv> getUseBizList(){
	   List<EApplBizDv> rst = new ArrayList<EApplBizDv>();
//	   rst = Stream.of(EApplBizDv.values()).filter(s->s.isUpperBizDv ==true).collect(toList());
	   rst = Stream.of(EApplBizDv.values()).filter(s->s.isUpperBizDv ==true).collect(Collectors.toList());
	   return rst ;
   }
      
// true 만 쉽게 가져오는 방법 !! 이게 최선인가 ??
//   public static List<EApplBizDv> getUseBizList(){
//	   
//	   List<EApplBizDv> rst = new ArrayList<EApplBizDv>();
//	   
//	   for (EApplBizDv tmp : EApplBizDv.values()) {
//		   if(tmp.isBizDv == true) {
//			   rst.add(tmp);
//		   }
//	   }
//	   return rst ;
//   }

  public static EApplBizDv getApplBizDetDv(EApplBizDv upperBizDv, String assetLiabDv) {
	   String testString ;
	   testString = upperBizDv.name() + "_" + assetLiabDv ;
	 
	  return valueOf(testString) ;
  }
   
   
}



