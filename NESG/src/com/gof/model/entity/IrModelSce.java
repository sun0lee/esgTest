package com.gof.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gof.entity.IrDcntSceStoBiz;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrModelSce implements Serializable {
	
	private static final long serialVersionUID = -5971256119173516419L;

	private String  baseDate;	
	private Integer sceNo;	
	private String  matCd;	
	private Integer monthSeq;	
	
	private Double  spotRateDisc;	
	private Double  spotRateCont;	
	private Double  fwdRateDisc;	
	private Double  fwdRateCont;	
	private Double  dcntFactor;	
	private double  theta;
	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
	public IrDcntSceStoBiz convert(EApplBizDv applBizDv, EIrModel irModelNm, String irCurveNm, Integer irCurveSceNo, String jobId) {
		IrDcntSceStoBiz rst = new IrDcntSceStoBiz();
		
		rst.setBaseYymm(this.baseDate.substring(0,6));		
		rst.setApplBizDv(applBizDv);
		rst.setIrModelNm(irModelNm);
		rst.setIrCurveNm(irCurveNm);
		rst.setIrCurveSceNo(irCurveSceNo);
		rst.setSceNo(this.sceNo);
		rst.setMatCd(this.matCd);		
		rst.setSpotRate(this.spotRateDisc);
		rst.setFwdRate(this.fwdRateDisc);	
		rst.setModifiedBy(jobId);
		rst.setUpdateDate(LocalDateTime.now());

		return rst;
	}

}
