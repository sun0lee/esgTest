package com.gof.enums;

import lombok.Getter;

@Getter
public enum EDetSceNo {
	 SCE00 (0) // 현재 미사용 base는 보통 0번 아닌가? 일단 esg에서는 1번이 기본임. 
	,SCE01 (1)
	,SCE02 (2)
	,SCE03 (3)
	,SCE04 (4)
	,SCE05 (5)
	,SCE06 (6);
	
	private final Integer sceNo ;
	
	private EDetSceNo(Integer sceNo ) {
		this.sceNo = sceNo;
	}
	public Integer getSceNo() {
		return sceNo ;
	}
	
	public Integer getBaseScenNo() {
	    return EDetSceNo.SCE01.sceNo;
	}
	
}
 