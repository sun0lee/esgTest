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
import com.gof.interfaces.EntityIdentifier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="RC_CORP_TM_USR")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SequenceGenerator (name = "RC_CORP_TM_USR_SEQ_GEN",sequenceName = "RC_CORP_TM_USR_SEQ",initialValue = 1, allocationSize = 1)
public class RcCorpTmUsr extends BaseEntity implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1874314758316989856L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RC_CORP_TM_USR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private String baseYymm;
	private String crdEvalAgncyCd;
	private String fromCrdGrdCd;	

	@Column(name = "TRANS_PROB_1") private Double transProb1;
	@Column(name = "TRANS_PROB_2") private Double transProb2;
	@Column(name = "TRANS_PROB_3") private Double transProb3;
	@Column(name = "TRANS_PROB_4") private Double transProb4;
	@Column(name = "TRANS_PROB_5") private Double transProb5;
	@Column(name = "TRANS_PROB_6") private Double transProb6;
	@Column(name = "TRANS_PROB_7") private Double transProb7;

//	private String lastModifiedBy;	
//	private LocalDateTime lastUpdateDate;
	
}
