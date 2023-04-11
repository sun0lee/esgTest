package com.gof.enums;

import lombok.Getter;

@Getter
public enum EDetSceNo {
	 SCE01 (1  )
	,SCE02 (2  )
	,SCE03 (3  )
	,SCE04 (4  )
	,SCE05 (5  )
	,SCE06 (6  )
;
	private final Integer SceNo ;
	

	EDetSceNo(Integer SceNo ) {
		this.SceNo = SceNo;
			}
	public static Integer getBaseScenNo() {
	    return EDetSceNo.SCE01.SceNo;
	}
	
//	public static Integer getScenNo(sceNo) {
//		return EDetSceNo(sceNo);
//	}
}
 