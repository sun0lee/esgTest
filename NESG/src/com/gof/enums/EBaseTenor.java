package com.gof.enums;

import java.util.Comparator;
import java.util.List;

public enum EBaseTenor {
	
    M0003(0.25),
    M0006(0.5),
    M0009(0.75),
    M0012(1),
    M0018(1.5),
    M0024(2),
    M0030(2.5),
    M0036(3),
    M0048(4),
    M0060(5),
    M0084(7),
    M0120(10),
    M0180(15),
    M0240(20);

    private final double yearFrac;

    EBaseTenor(double yearFrac) {
        this.yearFrac = yearFrac;
    }

    public double getYearFrac() {
        return yearFrac;
    }

    public static double[] getTenorArray(List<String> matCdList) {
        return matCdList.stream()
                .map(EBaseTenor::valueOf)  // string -> Enum 매핑 
                .sorted(Comparator.comparingDouble(EBaseTenor::getYearFrac)) // 정렬 추가
                .mapToDouble(EBaseTenor::getYearFrac) //Enum -> tenor (double) 매핑 
                .toArray();
    }
}
