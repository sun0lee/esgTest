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
import lombok.ToString;

@Entity
@Table(name ="IR_QVAL_SCE")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SequenceGenerator (name = "IR_QVAL_SCE_SEQ_GEN",sequenceName = "IR_QVAL_SCE_SEQ",initialValue = 1, allocationSize = 1)
public class IrQvalSce extends BaseEntity implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1660055914228437117L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_QVAL_SCE_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EApplBizDv applBizDv;
    @Enumerated(EnumType.STRING)
	private EIrModel irModelNm;	
	private String irCurveNm;
	private Integer irCurveSceNo;	
	private String qvalDv;
	private Integer qvalSeq;	
	
	private Double qval1;
	private Double qval2;
	private Double qval3;
	private Double qval4;
	private Double qval5;	
	private Double qval6;
	private Double qval7;
	private Double qval8;
	private Double qval9;
	private Double qval10;	
	private Double qval11;
	private Double qval12;
	private Double qval13;
	private Double qval14;
	private Double qval15;
	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;
}