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
import lombok.ToString;

@Entity
@Table(name ="IR_DCNT_RATE_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "IR_DCNT_RATE_BIZ_SEQ_GEN",sequenceName = "IR_DCNT_RATE_BIZ_SEQ",initialValue = 1, allocationSize = 1)
public class IrDcntRateBiz implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 9213714569868056834L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_YTM_USR_HIS_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseYymm; 
	private String applBizDv; 
	private String irCurveNm;
	private Integer irCurveSceNo;
	private String matCd;
	
	private Double spotRate;	
	private Double fwdRate;	
	private String modifiedBy;
	private LocalDateTime updateDate;
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;


	public int getMatNum() {
		return Integer.parseInt(matCd.substring(1));
	}
	
	public double getDf() {
		return Math.pow(1+spotRate, -1.0 * getMatNum()/12);
	}
	
	public double getContForwardRate() {
		return Math.log(1+fwdRate);
	}
	
}
