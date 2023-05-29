package com.gof.enums;

import lombok.Getter;

@Getter
public enum EDetSce {
	 SCE00 (0) // 현재 미사용 base는 보통 0번 아닌가? 일단 esg에서는 1번이 기본임. 
	,SCE01 (1)
	,SCE02 (2)
	,SCE03 (3)
	,SCE04 (4)
	,SCE05 (5)
	,SCE06 (6)
	,SCE07 (7)
	,SCE08 (8)
	,SCE09 (9)
	,SCE10 (10)
	,SCE11 (11)
	,SCE12 (12)
	,SCE13 (13);
	
	private final Integer sceNo ;
	
	private EDetSce(Integer sceNo ) {
		this.sceNo = sceNo;
	}
	// enum -> int
	public Integer getSceNo() {
		return sceNo ;
	}
	
	// base 시나리오 알려주기 
//	public Integer getBaseScenNo() {
//	    return EDetSce.SCE01.sceNo;
//	}
	
	// int -> enum
    public static EDetSce getEDetSce(Integer sceNo) {
        for (EDetSce aa : EDetSce.values()) {
            if (aa.sceNo == sceNo) {
                return aa;
            }
        }
        throw new IllegalArgumentException("Invalid EDetSce value: " + sceNo);
    }
	
}
 