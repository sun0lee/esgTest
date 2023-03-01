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


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_CURVE_YTM")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "IR_CURVE_YTM_SEQ_GEN",sequenceName = "IR_CURVE_YTM_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveYtm implements Serializable {	
	
	private static final long serialVersionUID = 1340116167808300605L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_YTM_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseDate;	
	private String irCurveNm;	
	private String matCd;
	
	private Double ytm;	
	private String modifiedBy;	
	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	
	public IrCurveSpot convertSimple() {
		
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseDate);		
		spot.setIrCurve(this.irCurve);   // add		
		spot.setIrCurveNm(this.irCurveNm);		
		spot.setMatCd(this.matCd);
		spot.setSpotRate(this.ytm);		
		spot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		spot.setUpdateDate(LocalDateTime.now());
		
		return spot;
	}		
	
	
	public IrCurveYtm addSpread(double spread) {
		
		IrCurveYtm addYtm = new IrCurveYtm();
		
		addYtm.setBaseDate(this.baseDate);		
		addYtm.setIrCurve(this.irCurve);		//add
		addYtm.setIrCurveNm(this.irCurveNm);		
		addYtm.setMatCd(this.matCd);
		addYtm.setYtm(this.ytm+spread);		
		addYtm.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		addYtm.setUpdateDate(LocalDateTime.now());
		
		return addYtm;
	}		
}