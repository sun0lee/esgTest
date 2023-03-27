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

@Entity
@Table(name ="IR_CURVE_FWD")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@SequenceGenerator (name = "IR_CURVE_FWD_SEQ_GEN",sequenceName = "IR_CURVE_FWD_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveFwd extends BaseEntity  implements Serializable {
	
	private static final long serialVersionUID = -2709553445772419321L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_FWD_SEQ_GEN")
	@Column (name = "SID")
	@Id	
	private long id;

	private String baseDate; 
	private String irCurveNm;
	private String fwdMatCd;
	private String matCd;	
	
	private Double intRate;	
//	private String modifiedBy;	
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID")
	private IrCurve irCurve ;
	
}
