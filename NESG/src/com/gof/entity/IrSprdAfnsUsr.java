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

import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="IR_SPRD_AFNS_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@SequenceGenerator (name = "IR_SPRD_AFNS_USR_SEQ_GEN",sequenceName = "IR_SPRD_AFNS_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrSprdAfnsUsr implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -8160719685730683413L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_SPRD_AFNS_USR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	
	private String baseYymm; 
	private String irModelNm;
	private String irCurveNm;	
	private String matCd;	

	private Double meanSprd;
	private Double upSprd;
	private Double downSprd;
	private Double flatSprd;
	private Double steepSprd;		
	private String modifiedBy;	
	private LocalDateTime updateDate;
	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;

	
}
