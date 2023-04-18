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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EIrModel;
import com.gof.enums.EParamTypCd;
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table( name ="IR_PARAM_HW_CALC")
@FilterDef(name="FILTER", parameters= { @ParamDef(name="baseYymm", type="string") })
@Filters( { @Filter(name ="FILTER", condition="BASE_YYMM = :baseYymm") } )
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_HW_CALC_SEQ_GEN",sequenceName = "IR_PARAM_HW_CALC_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamHwCalc extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -3199922647182076353L;

	private static final String String = null;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_HW_CALC_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
	private String irCurveNm;
	private String matCd;	
	@Enumerated (EnumType.STRING)
	private EParamTypCd paramTypCd;
	
	private Double paramVal;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;
	
	public IrParamHwBiz convert(EApplBizDv applBizDv) {		
		
		IrParamHwBiz paramHwBiz = new IrParamHwBiz();			
		
		paramHwBiz.setBaseYymm(this.baseYymm);
		paramHwBiz.setApplBizDv(applBizDv);
		paramHwBiz.setIrModelNm(this.irModelNm);
		paramHwBiz.setIrCurveNm(this.irCurveNm);
		paramHwBiz.setMatCd(this.matCd);
		paramHwBiz.setParamTypCd(this.paramTypCd);
		paramHwBiz.setParamVal(this.paramVal);
		paramHwBiz.setModifiedBy("GESG_" + this.getClass().getSimpleName());
		paramHwBiz.setUpdateDate(LocalDateTime.now());		
		
		return paramHwBiz;
	}	
	
}


