package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

@Entity
@Table(name ="IR_CURVE_SPOT")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@SequenceGenerator (name = "IR_CURVE_SPOT_SEQ_GEN",sequenceName = "IR_CURVE_SPOT_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveSpot implements Serializable {	
	
	private static final long serialVersionUID = 8405894865559378104L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_SPOT_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseDate; 
	private String irCurveNm;
	private String matCd;	
	
	private Double spotRate;		
	private String modifiedBy;	
	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;
	
	
	public IrCurveSpot(String baseDate, String irCurveNm, String matCd, Integer sceNo, Double intRate) {
		this.baseDate = baseDate;
		this.irCurveNm = irCurveNm;
		this.matCd = matCd;
		this.spotRate = intRate;
	}
	public IrCurveSpot(String baseDate, IrCurve irCurve, String matCd, Integer sceNo, Double intRate) {
		this.baseDate = baseDate;
		this.irCurve = irCurve;
		this.matCd = matCd;
		this.spotRate = intRate;
	}
	public IrCurveSpot(String baseDate, String matCd, Double intRate) {
		this.baseDate = baseDate;
		this.matCd = matCd;
		this.spotRate = intRate;
	}
	
	public IrCurveSpot(String bssd, IrCurveSpot curveHis) {
		this.baseDate = curveHis.getBaseDate();
		this.irCurve = curveHis.getIrCurve();
		this.irCurveNm = curveHis.getIrCurveNm();
		this.matCd = curveHis.getMatCd();
		this.spotRate = curveHis.getSpotRate();				
	}
	
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseDate).append(delimeter)
			   .append(irCurve.getId()).append(delimeter) //irCurveSid
			   .append(irCurveNm).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(spotRate).append(delimeter)
			   ;

		return builder.toString();
	}
	
	public IrCurveSpot deepCopy(IrCurveSpot org) {
		IrCurveSpot copy = new IrCurveSpot();
		
		copy.setBaseDate(org.getBaseDate());
		copy.setIrCurve(org.getIrCurve());   //add
		copy.setIrCurveNm(org.getIrCurveNm());
		copy.setMatCd(org.getMatCd());
		copy.setSpotRate(org.getSpotRate());
		
		return copy;
//		return org;
	}
	
	
	public IrCurveSpot convertToCont() {
		IrCurveSpot copy = new IrCurveSpot();
		
		copy.setBaseDate(this.getBaseDate());
		copy.setIrCurveNm(this.getIrCurveNm());
		copy.setMatCd(this.getMatCd());
		copy.setSpotRate( Math.log(1.0 + this.getSpotRate()));
		
		return copy;
//		return org;
	}
	

//******************************************************Biz Method**************************************
//	@Transient
	public int getMatNum() {
		return Integer.parseInt(matCd.substring(1));
	}
	public IrCurveSpot addForwardTerm(String bssd) {
		return new IrCurveSpot(bssd, this);
	}
	
	public String getBaseYymm() {
		return getBaseDate().substring(0,6);
	}
	public boolean isBaseTerm() {
		if(matCd.equals("M0003") 
				|| matCd.equals("M0006") 
				|| matCd.equals("M0009")
				|| matCd.equals("M0012")
				|| matCd.equals("M0024")
				|| matCd.equals("M0036")
				|| matCd.equals("M0060")
				|| matCd.equals("M0084")
				|| matCd.equals("M0120")
				|| matCd.equals("M0240")
				) {
			return true;
		}
		return false;	
			
	}
	
	
	public IrCurveYtm convertSimpleYtm() {
		
		IrCurveYtm ytm = new IrCurveYtm();
		
		ytm.setBaseDate(this.baseDate);		
		ytm.setIrCurveNm(this.irCurveNm);		
		ytm.setMatCd(this.matCd);
		ytm.setYtm(this.spotRate);		
		ytm.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		ytm.setUpdateDate(LocalDateTime.now());
		
		return ytm;
	}		
	

	public IrCurveSpotWeek convertToWeek() {
		IrCurveSpotWeek rst = new IrCurveSpotWeek();
		
		String dayOfWeek = LocalDate.parse(baseDate, DateTimeFormatter.BASIC_ISO_DATE).getDayOfWeek().name();
		rst.setBaseDate(this.baseDate);
		rst.setIrCurveNm(this.irCurveNm);
		rst.setMatCd(this.matCd);
		rst.setSpotRate(this.spotRate);
		rst.setDayOfWeek(dayOfWeek);
		rst.setBizDayType("Y");
//		rst.setIrCurve(this.irCurve);
		rst.setModifiedBy("ESG");
		rst.setUpdateDate(LocalDateTime.now());
		
		return rst;		
	}		
	
}
