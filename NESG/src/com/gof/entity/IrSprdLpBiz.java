package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_SPRD_LP_BIZ")
@FilterDef(name="eqBaseYymm", parameters= @ParamDef(name ="bssd",  type="string"))
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "IR_SPRD_LP_BIZ_SEQ_GEN",sequenceName = "IR_SPRD_LP_BIZ_SEQ",initialValue = 1, allocationSize = 1)
public class IrSprdLpBiz implements Serializable {	
	
	private static final long serialVersionUID = -2721793562033729088L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_SPRD_LP_BIZ_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm;
	private String applBizDv;
	private String irCurveNm;
	private Integer irCurveSceNo;	
	private String matCd;	
	
	private Double liqPrem;
	private String modifiedBy;
	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

}
