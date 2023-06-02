package com.gof.entity;

import java.io.Serializable;
//import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_AFNS_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_AFNS_USR_SEQ_GEN",sequenceName = "IR_PARAM_AFNS_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamAfnsUsr extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = -4846720080900879865L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_AFNS_USR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm; 
	private String irModelNm;
	private String irCurveNm;
//	@Enumerated(EnumType.STRING)
//	private EParamTypCd paramTypCd;
	private String paramTypCd;
	
	private Double paramVal;
	
//	private String modifiedBy;
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;


}
