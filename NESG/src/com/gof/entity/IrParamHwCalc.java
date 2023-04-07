package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import com.gof.enums.EApplBizDv;
import com.gof.enums.EParamTypCd;
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table( name ="E_IR_PARAM_HW_CALC")
@FilterDef(name="FILTER", parameters= { @ParamDef(name="baseYymm", type="string") })
@Filters( { @Filter(name ="FILTER", condition="BASE_YYMM = :baseYymm") } )
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamHwCalc implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -3199922647182076353L;

	private static final String String = null;

	@Id
	private String baseYymm;
	
	@Id	
	private String irModelId;
	
	@Id	
	private String irCurveId;
	
	@Id
	private String matCd;	

	@Id
	@Enumerated (EnumType.STRING)
//	private String paramTypCd;
	private EParamTypCd paramTypCd;
	
	private Double paramVal;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public IrParamHwBiz convert(EApplBizDv applBizDv) {		
		
		IrParamHwBiz paramHwBiz = new IrParamHwBiz();			
		
		paramHwBiz.setBaseYymm(this.baseYymm);
		paramHwBiz.setApplBizDv(applBizDv);
		paramHwBiz.setIrModelId(this.irModelId);
		paramHwBiz.setIrCurveId(this.irCurveId);
		paramHwBiz.setMatCd(this.matCd);
		paramHwBiz.setParamTypCd(this.paramTypCd);
		paramHwBiz.setParamVal(this.paramVal);
		paramHwBiz.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		paramHwBiz.setLastUpdateDate(LocalDateTime.now());		
		
		return paramHwBiz;
	}	
	
}


