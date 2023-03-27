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
@Table(name ="IR_SPRD_CURVE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode (callSuper = false)
@ToString
@SequenceGenerator (name = "IR_SPRD_CURVE_SEQ_GEN",sequenceName = "IR_SPRD_CURVE_SEQ",initialValue = 1, allocationSize = 1)
public class IrSprdCurve extends BaseEntity implements Serializable {	
	
	private static final long serialVersionUID = 8770367862233153559L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_SPRD_CURVE_SEQ_GEN")
	@Column (name = "SID")
	@Id	
	private long id;

	private String baseYymm; 
	private String irCurveNm;
	private String irTypDvCd;
	private String matCd;	
	
	private Double intRate;	
	private Double crdSprd;	
//	private String modifiedBy;	
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID")
	private IrCurve irCurve ;

}
