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
import com.gof.interfaces.IRateInput;

//import lombok.AllArgsConstructor;
import lombok.Builder;
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
@EqualsAndHashCode(callSuper=false)
@ToString
//@AllArgsConstructor // add 23.03.06 
//@Builder            // add 23.03.06 
@SequenceGenerator (name = "IR_CURVE_YTM_SEQ_GEN",sequenceName = "IR_CURVE_YTM_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveYtm extends BaseEntity implements Serializable, IRateInput {	
	
	private static final long serialVersionUID = 1340116167808300605L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_YTM_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseDate;	
	private String irCurveNm;	
	private String matCd;
	
	private Double ytm;	
//	private String modifiedBy;	
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	
//	23.03.31 add
	public Double getRate() {
		return getYtm();
	};
	
	public IrCurveSpot convertSimple() {
		 // 데이터 타입  // 메서드 
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseDate);		
		spot.setIrCurve(this.irCurve);   // add		
		spot.setIrCurveNm(this.irCurveNm);		
		spot.setMatCd(this.matCd);
		spot.setSpotRate(this.ytm);		
//		spot.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		spot.setUpdateDate(LocalDateTime.now());
		
		return spot;
	}		
	
	// 나머지 IrCurveYtm 정보는 그대로 전달하되 ytm 에 스프레드만 가산해서 return 
	public IrCurveYtm addSpread(double spread) {
		
		IrCurveYtm addYtm = new IrCurveYtm();
		
		addYtm.setBaseDate(this.baseDate);		
		addYtm.setIrCurve(this.irCurve);		//add
		addYtm.setIrCurveNm(this.irCurveNm);		
		addYtm.setMatCd(this.matCd);
		addYtm.setYtm(this.ytm+spread);		
//		addYtm.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		addYtm.setUpdateDate(LocalDateTime.now());
		
		return addYtm;
	}

@Builder // add 23.03.06 
	public IrCurveYtm(long id, String baseDate, String irCurveNm, String matCd, Double ytm
//			, String modifiedBy,LocalDateTime updateDate
			, IrCurve irCurve) {
			super();
			this.id = id;
			this.baseDate = baseDate;
			this.irCurveNm = irCurveNm;
			this.matCd = matCd;
			this.ytm = ytm;
//			this.modifiedBy = modifiedBy;
//			this.updateDate = updateDate;
			this.irCurve = irCurve;
	}

//@Override
//public String toString() {
//	StringBuilder builder = new StringBuilder();
//	builder.append("IrCurveYtm [id=").append(id).append(", baseDate=").append(baseDate).append(", irCurveNm=")
//			.append(irCurveNm).append(", matCd=").append(matCd).append(", ytm=").append(ytm).append(", modifiedBy=")
//			.append(modifiedBy).append(", updateDate=").append(updateDate).append(", irCurve=").append(irCurve)
//			.append("]");
//	return builder.toString();
//}		
}