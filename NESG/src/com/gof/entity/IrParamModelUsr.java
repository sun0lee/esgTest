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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EIrModel;
import com.gof.enums.EHwParamTypCd;
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_MODEL_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_MODEL_USR_SEQ_GEN",sequenceName = "IR_PARAM_MODEL_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamModelUsr extends BaseEntity  implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 6515137062709711605L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_MODEL_USR_SEQ")
	@Column (name = "SID")
	@Id
	private long id;

	private String applStYymm;
	private String applEdYymm;
	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
	private String irCurveNm;
	@Enumerated(EnumType.STRING)
	private EHwParamTypCd paramTypCd;	
	
	private Double paramVal;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	public IrParamModelBiz convert(String bssd) {
				
		IrParamModelBiz paramBiz = new IrParamModelBiz();			
		
		paramBiz.setBaseYymm(bssd);
		paramBiz.setIrModelNm(this.irModelNm);
		paramBiz.setIrCurveNm(this.irCurveNm);
		paramBiz.setParamTypCd(this.paramTypCd);
		paramBiz.setParamVal(this.paramVal);
//		paramBiz.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
//		paramBiz.setLastUpdateDate(LocalDateTime.now());		
		
		return paramBiz;
	}		

}


