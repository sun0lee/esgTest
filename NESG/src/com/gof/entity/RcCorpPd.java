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
@Table(name ="RC_CORP_PD")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SequenceGenerator (name = "RC_CORP_PD_SEQ_GEN",sequenceName = "RC_CORP_PD_SEQ",initialValue = 1, allocationSize = 1)
public class RcCorpPd extends BaseEntity implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -3833361109526416019L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RC_CORP_PD_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private String baseYymm;
	private String crdEvalAgncyCd;
	private String crdGrdCd;	
	private String matCd;	
	
	private Double cumPd;	
	private Double fwdPd;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
}


