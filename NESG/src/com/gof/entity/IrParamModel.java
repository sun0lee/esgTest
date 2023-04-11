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
//import javax.persistence.Transient;

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EBoolean;
import com.gof.enums.EIrModel;
import com.gof.util.StringUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_MODEL")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_MODEL_SEQ_GEN",sequenceName = "IR_PARAM_MODEL_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamModel  extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = -3967105002517595201L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_MODEL_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;		
	private String irModelName;
	
	private String irCurveNm;
	private Integer totalSceNo;	
	private Integer rndSeed;	
	private Double itrTol;
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;
	
//	private String modifiedBy;
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID")
	private IrCurve irCurve ;

	
		public Double getItrTol() {
			return itrTol = StringUtil.objectToPrimitive(itrTol, 1E-8) ;	
		}

	
}
