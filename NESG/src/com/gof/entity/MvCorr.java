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

@Entity
@Table(name ="MV_CORR")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator (name = "MV_CORR_SEQ_GEN",sequenceName = "MV_CORR_SEQ",initialValue = 1, allocationSize = 1)
public class MvCorr extends BaseEntity implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -2507101178171773843L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MV_CORR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseDate;
	private String volCalcId;
	private String mvId;
	private String refMvId;	
	
	private Double mvHisCov;
	private Double mvHisCorr;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	public MvCorr(String mvId, String refMvId, Double mvHisCov,Double mvHisCorr) {
		this.mvId = mvId;
		this.refMvId = refMvId;
		this.mvHisCov = mvHisCov;
		this.mvHisCorr = mvHisCorr;
	}
		
}
