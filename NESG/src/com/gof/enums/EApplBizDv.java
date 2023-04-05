package com.gof.enums;
import java.util.List;

import static java.util.stream.Collectors.toList;
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

   private boolean isBizDv ;
   private EApplBizDv upperBizDv ;
   private String assetLiabDv ;
 

   private EApplBizDv(boolean isBizDv) {
	  this.isBizDv = isBizDv ;
   }
   
   // 생성자를 왜 만들어야 하지 ?
  private EApplBizDv( boolean isBizDv, EApplBizDv upperBizDv, String assetLiabDv) {
	  this.isBizDv = isBizDv ;
	  this.upperBizDv = upperBizDv;
	  this.assetLiabDv = assetLiabDv;
}

public static List<EApplBizDv> getUseBizList(){
	   List<EApplBizDv> rst = new ArrayList<EApplBizDv>();
	   rst = Stream.of(EApplBizDv.values()).filter(s->s.isBizDv ==true).collect(toList());
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
	   String aa ;
	   aa = upperBizDv.name() + "_" + assetLiabDv ;
	 
	  return valueOf(aa) ;
  }
   
   
}



