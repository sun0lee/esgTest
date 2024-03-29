package com.gof.entity;

import java.io.Serializable;
//import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EDetSce;
import com.gof.interfaces.IRateDcnt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_DCNT_RATE_BU")
@NoArgsConstructor
@FilterDef(name="IR_FILTER", parameters= { @ParamDef(name="baseYymm", type="string"), @ParamDef(name="irCurveNm", type="string") })
@Filters( { @Filter(name ="IR_FILTER", condition="BASE_YYMM = :baseYymm"),  @Filter(name ="IR_FILTER", condition="IR_CURVE_NM like :irCurveNm") } )
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
@SequenceGenerator (name = "IR_DCNT_RATE_BU_SEQ_GEN",sequenceName = "IR_DCNT_RATE_BU_SEQ",initialValue = 1, allocationSize = 1)
public class IrDcntRateBu extends BaseEntity implements Serializable, IRateDcnt {
	
	private static final long serialVersionUID = -4644199390958760035L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_DCNT_RATE_BU_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EApplBizDv applBizDv; 
	private String irCurveNm;
	@Enumerated(EnumType.ORDINAL)
	private EDetSce irCurveSceNo;
	private String matCd;	
	
	private Double spotRateDisc;
	private Double spotRateCont;
	private Double liqPrem;
	private Double adjSpotRateDisc;
	private Double adjSpotRateCont;	
	private Double addSprd;
//	private String modifiedBy;
//	private LocalDateTime updateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	
//	23.03.31 add
	public Double getSpotRate() {
		return getSpotRateCont();
	};
	
	public IrCurveSpot convertAdj() {
		
		IrCurveSpot adjSpot = new IrCurveSpot();
		
		adjSpot.setBaseDate(this.baseYymm);		
		adjSpot.setIrCurve(this.irCurve);   // add		
		adjSpot.setIrCurveNm(this.irCurveNm);		
		adjSpot.setMatCd(this.matCd);			
		adjSpot.setSpotRate(this.adjSpotRateDisc);
//		adjSpot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		adjSpot.setUpdateDate(LocalDateTime.now());
		
		return adjSpot;
	}
	
	
	public IrCurveSpot convertBase() {
		
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseYymm);		
		spot.setIrCurve(this.irCurve);  //add 		
		spot.setIrCurveNm(this.irCurveNm);		
		spot.setMatCd(this.matCd);			
		spot.setSpotRate(this.spotRateDisc);
//		spot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		spot.setUpdateDate(LocalDateTime.now());
		
		return spot;
	}	

}