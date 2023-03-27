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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_CURVE_YTM_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
@SequenceGenerator (name = "IR_CURVE_YTM_USR_SEQ_GEN",sequenceName = "IR_CURVE_YTM_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveYtmUsr extends BaseEntity implements Serializable {	

	private static final long serialVersionUID = 8728364358808498458L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_YTM_USR_SEQ_GEN")
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

	
	public IrCurveSpot convertToHis() {
		
		IrCurveSpot rst = new IrCurveSpot();
		
		rst.setBaseDate(this.baseDate);
		rst.setIrCurve(this.irCurve);
		rst.setIrCurveNm(this.irCurveNm);
		rst.setMatCd(this.matCd);
		rst.setSpotRate(this.ytm);
		rst.setModifiedBy("ESG");
		rst.setUpdateDate(LocalDateTime.now());
		
		return rst;
	}
	
}