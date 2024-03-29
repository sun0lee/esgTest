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

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EDetSce;
import com.gof.util.StringUtil;

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
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_SW_SEQ_GEN",sequenceName = "IR_PARAM_SW_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamSw extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = -4175243759288891655L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_SW_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	
	@Enumerated(EnumType.STRING)
//	private String applBizDv;	
	private EApplBizDv applBizDv;	
	private String irCurveNm;	
	@Enumerated(EnumType.ORDINAL)
	private EDetSce irCurveSceNo;
//	private Integer irCurveSceNo;
	
	private String  irCurveSceNm;
	private String  curCd;
	private Integer freq;
	private Integer llp;
	private Double  ltfr;
	private Integer ltfrCp;
	private Double  liqPrem;
	private String  liqPremApplDv;
	@Enumerated(EnumType.ORDINAL)
	private EDetSce shkSprdSceNo;
	private Double  swAlphaYtm;		
	private String  stoSceGenYn;	
	private String  fwdMatCd;	
	
	// 23.04.17 add 
    private Double ytmSpread ;
    
	private Double multIntRate;   //ytmSpread 컬럼 별도 생성 
	private Double addSprd;
	private String pvtRateMatCd;	
	private Double multPvtRate;	
//	private String modifiedBy;
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID")
	private IrCurve irCurve ;
	
	public double getYtmSpread() {
		return ytmSpread == null ? 0.0 : ytmSpread.doubleValue();
	}
	
	
// 23.04.06 프로그램마다 default 값 처리하는 부분을 여기에서 처리함.
	
	public Integer getFreq() {
		return freq = StringUtil.objectToPrimitive(freq, 2) ;
	}
	
	public String getFwdMatCd() {
		return fwdMatCd = StringUtil.objectToPrimitive(fwdMatCd, "M0000");
	}
	public Integer getLlp() {
		return llp = StringUtil.objectToPrimitive(llp, 20);
	}
	public String getLiqPremApplDv() {
		return liqPremApplDv = StringUtil.objectToPrimitive(liqPremApplDv, "1");
	}
	public EDetSce getShkSprdSceNo() {
		if (shkSprdSceNo == null) return EDetSce.SCE01 ;
		else return shkSprdSceNo ;
//		return shkSprdSceNo = objectToPrimitive(shkSprdSceNo,1);
	}
	public Double getSwAlphaYtm() {
		return swAlphaYtm = StringUtil.objectToPrimitive(swAlphaYtm, 0.1) ; 
	}
	
	
	// 23.04.17 어디에서도 사용하지 않음!!  이것과 getMultPvtRate와 차이는 ????
	public Double getMultIntRate() {
		return multIntRate = StringUtil.objectToPrimitive(multIntRate,1.0);
	}
	public Double getAddSprd() {
		return addSprd =  StringUtil.objectToPrimitive(addSprd, 0.0);
	}
	public String getPvtRateMatCd() {
		return pvtRateMatCd = StringUtil.objectToPrimitive(pvtRateMatCd , "M0000");
	}
	public Double getMultPvtRate() {
		return multPvtRate = StringUtil.objectToPrimitive(multPvtRate , 1.0  );
	}

}


