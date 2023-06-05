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
import com.gof.enums.EApplBizDv;
import com.gof.interfaces.EntityIdentifier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="RC_CORP_PD_BIZ")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SequenceGenerator (name = "RC_CORP_PD_BIZ_SEQ_GEN",sequenceName = "RC_CORP_PD_BIZ_SEQ",initialValue = 1, allocationSize = 1)
public class RcCorpPdBiz extends BaseEntity implements Serializable, EntityIdentifier {
		
	private static final long serialVersionUID = -5770104017516099415L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RC_CORP_PD_BIZ_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EApplBizDv applBizDv;
	private String crdGrdCd;
	private String matCd;

	private Double cumPd;
	private Double fwdPd;
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;	
	
}
