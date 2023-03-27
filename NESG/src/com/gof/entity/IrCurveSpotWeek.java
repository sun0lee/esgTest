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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_CURVE_SPOT_WEEK")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString(exclude="irCurve")
@SequenceGenerator (name = "IR_CURVE_SPOT_WEEK_SEQ_GEN",sequenceName = "IR_CURVE_SPOT_WEEK_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveSpotWeek  extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 8687612876394929135L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_SPOT_WEEK_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseDate;	
//	private long irCurveSid;	
	private String irCurveNm;	
	private String matCd;	
	
	private Double spotRate;	
	private String dayOfWeek;
	private String bizDayType;	
//	private String modifiedBy;
//	private LocalDateTime updateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	
	public IrCurveSpotWeek(String baseDate, String matCd, Double intRate) {
		
		this.baseDate = baseDate;
		this.matCd = matCd;
		this.spotRate = intRate;
	}
	
	public IrCurveSpotWeek(String bssd, IrCurveSpotWeek curveHis) {
		
		this.baseDate = curveHis.getBaseDate();
		this.irCurveNm = curveHis.getIrCurveNm();
//		this.irCurveSid = curveHis.getIrCurveSid();
		this.irCurve = curveHis.getIrCurve();
		this.matCd = curveHis.getMatCd();
		this.spotRate = curveHis.getSpotRate();				
	}
	
	public IrCurveSpotWeek(IrCurveSpotWeek curveHis) {
		
		this.baseDate = curveHis.baseDate;
		this.irCurveNm = curveHis.irCurveNm;
//		this.irCurveSid = curveHis.irCurveSid;
		this.irCurve = curveHis.irCurve;
		this.matCd = curveHis.matCd;		
		this.spotRate = curveHis.spotRate;
		this.dayOfWeek = curveHis.dayOfWeek;
		this.bizDayType = curveHis.bizDayType;
//		this.modifiedBy = curveHis.modifiedBy;
//		this.updateDate = curveHis.updateDate;
	}
	
	public IrCurveSpot convertToHis() {
		IrCurveSpot rst = new IrCurveSpot();
		
		rst.setBaseDate(this.baseDate);
		rst.setIrCurveNm(this.irCurveNm);
		rst.setIrCurve(this.irCurve);
		rst.setMatCd(this.matCd);
		rst.setSpotRate(this.spotRate);
//		rst.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		rst.setUpdateDate(LocalDateTime.now());
		return rst;
	}	
	
//	@Override
//	public IrCurveWeek clone() throws CloneNotSupportedException {
//		return (IrCurveWeek) super.clone();		
//	}
	
}
