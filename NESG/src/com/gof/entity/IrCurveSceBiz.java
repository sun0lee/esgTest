package com.gof.entity;

import java.io.Serializable;

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
import com.gof.enums.EIrModel;
import com.gof.interfaces.EntityIdentifier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="IR_CURVE_SCE_BIZ")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator (name = "IR_CURVE_SCE_BIZ_SEQ_GEN",sequenceName = "IR_CURVE_SCE_BIZ_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveSceBiz extends BaseEntity implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 4458482460359847563L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_SCE_BIZ_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EApplBizDv applBizDv;
    @Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
    private String irCurveNm;	
    private Integer irCurveSceNo;	
    private Integer sceNo;
    private String matCd;    

	private Double spotRate;
	private Double fwdRate;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;

	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;
}


