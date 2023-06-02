package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;
import com.gof.enums.ECrdGrd;
import com.gof.interfaces.EntityIdentifier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="RC_CORP_TM")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SequenceGenerator (name = "RC_CORP_TM_SEQ_GEN",sequenceName = "RC_CORP_TM_SEQ",initialValue = 1, allocationSize = 1)
public class RcCorpTm extends BaseEntity implements Serializable, EntityIdentifier, Comparable<RcCorpTm> {
	
	private static final long serialVersionUID = -4080286022399238155L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RC_CORP_TM_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private String baseYymm;
	private String crdEvalAgncyCd;
	private String fromCrdGrdCd;
	private String toCrdGrdCd;
	
	private double transProb;
//	private String lastModifiedBy;	
//	private LocalDateTime lastUpdateDate;
	
	public ECrdGrd getFromGradeEnum() {
		return ECrdGrd.getECrdGrd(fromCrdGrdCd) ;
	}
	
	public ECrdGrd getToGradeEnum() {
		return ECrdGrd.getECrdGrd(toCrdGrdCd) ;
	}	
	
	@Override
	public int compareTo(RcCorpTm other) {
		return 100 * ( this.getFromGradeEnum().getOrder() - other.getFromGradeEnum().getOrder()) 
				   + ( this.getToGradeEnum().getOrder()  - other.getToGradeEnum().getOrder())
				   ;		
	}		
	
}
