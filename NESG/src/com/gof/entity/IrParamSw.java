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
@Table(name ="IR_PARAM_SW")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "IR_PARAM_SW_SEQ_GEN",sequenceName = "IR_PARAM_SW_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamSw implements Serializable {
	
	private static final long serialVersionUID = -4175243759288891655L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_SW_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	private String applBizDv;	
	private String irCurveNm;	
	private Integer irCurveSceNo;
	
	private String irCurveSceNm;
	private String curCd;
	private Integer freq;
	private Integer llp;
	private Double ltfr;
	private Integer ltfrCp;
	private Double liqPrem;
	private String liqPremApplDv;
	private Integer shkSprdSceNo;
	private Double swAlphaYtm;		
	private String stoSceGenYn;	
	private String fwdMatCd;	
	
	@Column(name = "MULT_INT_RATE")
	private Double multIntRate;   //ytmSpread
	
	private Double addSprd;
	private String pvtRateMatCd;	
	private Double multPvtRate;	
	private String modifiedBy;
	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID")
	private IrCurve irCurve ;
	
	public double getYtmSpread() {
		return multIntRate == null ? 0.0 : multIntRate.doubleValue();
	}
	


}


