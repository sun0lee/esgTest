package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;
import com.gof.enums.EBoolean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="IR_CURVE")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper=false)
@SequenceGenerator (name = "IR_CURVE_SEQ_GEN",sequenceName = "IR_CURVE_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurve  extends BaseEntity implements Serializable {	
	
	private static final long serialVersionUID = -7079607534247603390L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_SEQ_GEN")
	@Column (name = "SID")
	private long id;
	
	private String irCurveNm;	
	private String irCurveName;	
	private String curCd;	
	private String applMethDv;
	private String crdGrdCd;	
	private String intpMethCd;
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IrCurve [id=").append(id).append(", irCurveNm=").append(irCurveNm).append(", irCurveName=")
				.append(irCurveName).append(", curCd=").append(curCd).append(", applMethDv=").append(applMethDv)
				.append(", crdGrdCd=").append(crdGrdCd).append(", intpMethCd=").append(intpMethCd).append(", useYn=")
				.append(useYn).append("]");
		return builder.toString();
	}
	
}
