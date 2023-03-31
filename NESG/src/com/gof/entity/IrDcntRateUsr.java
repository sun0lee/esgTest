package com.gof.entity;

import java.io.Serializable;
//import java.time.LocalDateTime;

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
@Table(name ="IR_DCNT_RATE_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_DCNT_RATE_USR_SEQ_GEN",sequenceName = "IR_DCNT_RATE_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrDcntRateUsr extends BaseEntity implements Serializable, IRateDcnt {
	
	private static final long serialVersionUID = -4252300668894647002L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_SPOT_SEQ_GEN")
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

	
	public IrDcntRate convert() {		
		
		IrDcntRate dcnt = new IrDcntRate();			
		
		dcnt.setBaseYymm(this.baseYymm);
		dcnt.setApplBizDv(this.applBizDv);
		dcnt.setIrCurve(this.irCurve);
		dcnt.setIrCurveNm(this.irCurveNm);
		dcnt.setIrCurveSceNo(this.irCurveSceNo);
		dcnt.setMatCd(this.matCd);
		dcnt.setSpotRate(this.spotRate);
		dcnt.setFwdRate(this.fwdRate);
		dcnt.setAdjSpotRate(this.adjSpotRate);
		dcnt.setAdjFwdRate(this.adjFwdRate);

//		dcnt.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		dcnt.setUpdateDate(LocalDateTime.now());		
		
		return dcnt;
	}	
		
}