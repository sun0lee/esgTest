package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
import com.gof.interfaces.EntityIdentifier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_HW_RND")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SequenceGenerator (name = "IR_PARAM_HW_RND_SEQ_GEN",sequenceName = "IR_PARAM_HW_RND_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamHwRnd extends BaseEntity implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -4252300668894647002L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_HW_RND_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EIrModel irModelNm;
	private String irCurveNm;
	private Integer sceNo;
	private String matCd;
	
	private Double rndNum;
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	@ManyToOne
	@JoinColumn(name = "IR_MODEL_SID" , referencedColumnName ="SID")
	private IrParamModel irParamModel ;
	
	public IrParamHwRnd setKeys(EIrModel irModelNm, String irCurveNm, String jobId) {		
		IrParamHwRnd rst = new IrParamHwRnd();
		
		rst.setBaseYymm(this.baseYymm);		
		rst.setIrModelNm(irModelNm);
		rst.setIrParamModel(this.irParamModel);
		rst.setIrCurveNm(irCurveNm);		
		rst.setIrCurve(irCurve);		
		rst.setSceNo(this.sceNo);
		rst.setMatCd(this.matCd);		
		rst.setRndNum(this.rndNum);			
		rst.setModifiedBy(jobId);
		rst.setUpdateDate(LocalDateTime.now());

		return rst;
	}	

}