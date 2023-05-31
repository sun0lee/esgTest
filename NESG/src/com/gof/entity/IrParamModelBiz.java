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
@Table(name ="IR_PARAM_MODEL_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_MODEL_BIZ_SEQ_GEN",sequenceName = "IR_PARAM_MODEL_BIZ_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamModelBiz  extends BaseEntity implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -1938042374180430559L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_MODEL_BIZ_SEQ")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
	private String irCurveNm;
	@Enumerated(EnumType.STRING)
	private EHwParamTypCd paramTypCd;	
	
	private Double paramVal;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;	

}


