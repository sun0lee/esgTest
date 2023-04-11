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
import com.gof.enums.EIrModel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_SPRD_AFNS_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_SPRD_AFNS_BIZ_SEQ_GEN",sequenceName = "IR_SPRD_AFNS_BIZ_SEQ",initialValue = 1, allocationSize = 1)
public class IrSprdAfnsBiz extends BaseEntity  implements Serializable {
	
	private static final long serialVersionUID = -7783664746646277314L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_SPRD_AFNS_BIZ_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm; 
	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
	private String irCurveNm;	
	private Integer irCurveSceNo;
	private String matCd;	
	
	private Double shkSprdCont;	
//	private String modifiedBy;	
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;

}
