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
import com.gof.enums.EDetSceNo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_SW_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "irCurve") //양방향 참조시에 순환참조를 막기위해 toString에서 제외시킴. 단방향은 상관 없지 않을까 ?
@SequenceGenerator (name = "IR_PARAM_SW_USR_SEQ_GEN",sequenceName = "IR_PARAM_SW_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamSwUsr extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 4818870209511307188L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_SW_USR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String applStYymm;
	private String applEdYymm;	

	@Enumerated (EnumType.STRING)
	private EApplBizDv applBizDv;	
	
//	private long irCurveSid;
	private String irCurveNm;
//	@Enumerated (EnumType.ORDINAL)
//	private EDetSceNo irCurveSceNo;
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
	private Double multIntRate;
	private Double addSprd;	
	private String pvtRateMatCd;	
	private Double multPvtRate;	
//	private String modifiedBy;
//	private LocalDateTime updateDate;		
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;
	
	public IrParamSw convert(String bssd) {		
			
		IrParamSw paramSw = new IrParamSw();			
		
		paramSw.setBaseYymm(bssd);
		paramSw.setApplBizDv(this.applBizDv);
		paramSw.setIrCurve(this.irCurve);		
		paramSw.setIrCurveNm(this.irCurveNm);
		paramSw.setIrCurveSceNo(this.irCurveSceNo);
		paramSw.setIrCurveSceNm(this.irCurveSceNm);
		paramSw.setCurCd(this.curCd);
		paramSw.setFreq(this.freq);
		paramSw.setLlp(this.llp);
		paramSw.setLtfr(this.ltfr);
		paramSw.setLtfrCp(this.ltfrCp);
		paramSw.setLiqPrem(this.liqPrem); //23.03.03 add  맞나 확인!
		paramSw.setLiqPremApplDv(this.liqPremApplDv);
		paramSw.setShkSprdSceNo(this.shkSprdSceNo);
		paramSw.setSwAlphaYtm(this.swAlphaYtm);
		paramSw.setStoSceGenYn(this.stoSceGenYn);
		paramSw.setFwdMatCd(this.fwdMatCd);
		paramSw.setMultIntRate(this.multIntRate);		
		paramSw.setAddSprd(this.addSprd);
		paramSw.setPvtRateMatCd(this.pvtRateMatCd);
		paramSw.setMultPvtRate(this.multPvtRate);		
//		paramSw.setModifiedBy("GESG_" + this.getClass().getSimpleName());
//		paramSw.setUpdateDate(LocalDateTime.now());		
		
		return paramSw;
	}	

}


