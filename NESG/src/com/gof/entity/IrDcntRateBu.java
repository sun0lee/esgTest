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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_DCNT_RATE_BU")
@NoArgsConstructor
@FilterDef(name="IR_FILTER", parameters= { @ParamDef(name="baseYymm", type="string"), @ParamDef(name="irCurveId", type="string") })
@Filters( { @Filter(name ="IR_FILTER", condition="BASE_YYMM = :baseYymm"),  @Filter(name ="IR_FILTER", condition="IR_CURVE_ID like :irCurveId") } )
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "IR_DCNT_RATE_BU_SEQ_GEN",sequenceName = "IR_DCNT_RATE_BU_SEQ",initialValue = 1, allocationSize = 1)
public class IrDcntRateBu implements Serializable {
	
	private static final long serialVersionUID = -4644199390958760035L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_DCNT_RATE_BU_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	private String applBizDv; 
	private String irCurveNm;
	private Integer irCurveSceNo;
	private String matCd;	
	
	private Double spotRateDisc;
	private Double spotRateCont;
	private Double liqPrem;
	private Double adjSpotRateDisc;
	private Double adjSpotRateCont;	
	private Double addSprd;
	private String modifiedBy;
	private LocalDateTime updateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	
	public IrCurveSpot convertAdj() {
		
		IrCurveSpot adjSpot = new IrCurveSpot();
		
		adjSpot.setBaseDate(this.baseYymm);		
		adjSpot.setIrCurve(this.irCurve);   // add		
		adjSpot.setIrCurveNm(this.irCurveNm);		
		adjSpot.setMatCd(this.matCd);			
		adjSpot.setSpotRate(this.adjSpotRateDisc);
		adjSpot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		adjSpot.setUpdateDate(LocalDateTime.now());
		
		return adjSpot;
	}
	
	
	public IrCurveSpot convertBase() {
		
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseYymm);		
		spot.setIrCurve(this.irCurve);  //add 		
		spot.setIrCurveNm(this.irCurveNm);		
		spot.setMatCd(this.matCd);			
		spot.setSpotRate(this.spotRateDisc);
		spot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		spot.setUpdateDate(LocalDateTime.now());
		
		return spot;
	}	

}