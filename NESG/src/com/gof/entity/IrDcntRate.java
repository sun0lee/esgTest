package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;
import com.gof.interfaces.IRateDcnt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_DCNT_RATE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
@SequenceGenerator (name = "IR_DCNT_RATE_SEQ_GEN",sequenceName = "IR_DCNT_RATE_SEQ",initialValue = 1, allocationSize = 1)
public class IrDcntRate extends BaseEntity implements Serializable, IRateDcnt {

	private static final long serialVersionUID = -4252300668894647002L;

	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_DCNT_RATE_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	
	private String baseYymm;
	private String applBizDv;
	private String irCurveNm;
	private Integer irCurveSceNo;
	private String matCd;
	
	private Double spotRate;
	private Double fwdRate;	
	private Double adjSpotRate;
	private Double adjFwdRate;	
//	private String modifiedBy;
//	private LocalDateTime updateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	public IrDcntRateBiz convertAdj() {
		
		IrDcntRateBiz adjDcnt = new IrDcntRateBiz();
		
		adjDcnt.setBaseYymm(this.baseYymm);		
		adjDcnt.setApplBizDv(this.applBizDv  + "_L");
		adjDcnt.setIrCurveNm(this.irCurveNm);
		adjDcnt.setIrCurve(this.irCurve);
		adjDcnt.setIrCurveSceNo(this.irCurveSceNo);
		adjDcnt.setMatCd(this.matCd);			
		adjDcnt.setSpotRate(this.adjSpotRate);
		adjDcnt.setFwdRate(this.adjFwdRate);
		adjDcnt.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		adjDcnt.setUpdateDate(LocalDateTime.now());
		
		return adjDcnt;
	}
	
	
	public IrDcntRateBiz convertBase() {
		
		IrDcntRateBiz baseDcnt = new IrDcntRateBiz();
		
		baseDcnt.setBaseYymm(this.baseYymm);		
		baseDcnt.setApplBizDv(this.applBizDv + "_A");
		baseDcnt.setIrCurveNm(this.irCurveNm);
		baseDcnt.setIrCurve(this.irCurve);
		baseDcnt.setIrCurveSceNo(this.irCurveSceNo);		
		baseDcnt.setMatCd(this.matCd);			
		baseDcnt.setSpotRate(this.spotRate);
		baseDcnt.setFwdRate(this.fwdRate);
		baseDcnt.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		baseDcnt.setUpdateDate(LocalDateTime.now());
		
		return baseDcnt;
	}	
	
	
	public IrDcntRate deepCopy(IrDcntRate org) {
		IrDcntRate copy = new IrDcntRate();
		
		copy.setBaseYymm(org.getBaseYymm());
		copy.setApplBizDv(org.getApplBizDv());
		copy.setIrCurve(org.getIrCurve());
		copy.setIrCurveNm(org.getIrCurveNm());
		copy.setIrCurveSceNo(org.getIrCurveSceNo());
		copy.setMatCd(org.getMatCd());
		copy.setSpotRate(org.getSpotRate());
		copy.setFwdRate(org.getFwdRate());
		copy.setAdjSpotRate(org.getAdjSpotRate());
		copy.setAdjFwdRate(org.getAdjFwdRate());
		
		return copy;
	}	
	
	
	public IrCurveSpot convertAdjSpot() {
		
		IrCurveSpot adjSpot = new IrCurveSpot();
		
		adjSpot.setBaseDate(this.baseYymm);		
		adjSpot.setIrCurve(this.irCurve);		
		adjSpot.setIrCurveNm(this.irCurveNm);		
		adjSpot.setMatCd(this.matCd);			
		adjSpot.setSpotRate(this.adjSpotRate);
//		adjSpot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		adjSpot.setUpdateDate(LocalDateTime.now());
		
		return adjSpot;
	}
	
	
	public IrCurveSpot convertBaseSpot() {
		
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseYymm);		
		spot.setIrCurve(this.irCurve);		
		spot.setIrCurveNm(this.irCurveNm);		
		spot.setMatCd(this.matCd);			
		spot.setSpotRate(this.spotRate);
//		spot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		spot.setUpdateDate(LocalDateTime.now());
		
		return spot;
	}		

}