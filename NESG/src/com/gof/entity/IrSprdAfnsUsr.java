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
import com.gof.interfaces.EntityIdentifier;
import com.gof.util.StringUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="IR_SPRD_AFNS_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@SequenceGenerator (name = "IR_SPRD_AFNS_USR_SEQ_GEN",sequenceName = "IR_SPRD_AFNS_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrSprdAfnsUsr  extends BaseEntity implements Serializable, EntityIdentifier {	
	
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
//	private String modifiedBy;	
//	private LocalDateTime updateDate;
	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;

	// 23.04.07 input 값에 대한 default 처리 
	public Double genMeanSprd() {
		return meanSprd = StringUtil.objectToPrimitive(meanSprd , 0.0) ;
	}
	public Double getUpSprd() {
		return upSprd = StringUtil.objectToPrimitive(upSprd , 0.0) ;
	}
	public Double genDownSprd() {
		return downSprd = StringUtil.objectToPrimitive(downSprd , 0.0) ;
	}
	public Double genFlatSprd() {
		return flatSprd = StringUtil.objectToPrimitive(flatSprd , 0.0) ;
	}
	public Double genSteepSprd() {
		return steepSprd = StringUtil.objectToPrimitive(steepSprd , 0.0) ;
	}

}
