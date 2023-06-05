package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;
import com.gof.enums.EHwParamTypCd;
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_HW_USR")
@FilterDef(name="paramApplyEqBaseYymm", parameters= { @ParamDef(name="baseYymm", type="string") })
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_HW_USR_SEQ_GEN",sequenceName = "IR_PARAM_HW_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamHwUsr extends BaseEntity implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1524655691890282755L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_HW_USR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseYymm;	
	@Enumerated(EnumType.STRING)
	private EApplBizDv applBizDv;
	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
	private String irCurveNm;
	private String matCd;
	@Enumerated(EnumType.STRING)
	private EHwParamTypCd paramTypCd;	
	
	private Double paramVal;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;
	
	public IrParamHwBiz convert() {		
		
		IrParamHwBiz paramHwBiz = new IrParamHwBiz();			
		
		paramHwBiz.setBaseYymm(this.baseYymm);
		paramHwBiz.setApplBizDv(this.applBizDv);
		paramHwBiz.setIrModelNm(this.irModelNm);
		paramHwBiz.setIrCurveNm(this.irCurveNm);
		paramHwBiz.setIrParamModel(this.irParamModel);
		paramHwBiz.setIrCurve(this.irCurve);
		paramHwBiz.setMatCd(this.matCd);
		paramHwBiz.setParamTypCd(this.paramTypCd);
		paramHwBiz.setParamVal(this.paramVal);
		paramHwBiz.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		paramHwBiz.setUpdateDate(LocalDateTime.now());		
		
		return paramHwBiz;
	}	
		
}


