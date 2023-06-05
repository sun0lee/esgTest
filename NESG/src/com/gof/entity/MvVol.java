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
@Table(name ="MV_VOL")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator (name = "MV_VOL_SEQ_GEN",sequenceName = "MV_VOL_SEQ",initialValue = 1, allocationSize = 1)
public class MvVol extends BaseEntity implements Serializable, EntityIdentifier {
		
	private static final long serialVersionUID = -564546080950002113L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MV_VOL_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseDate;
	private String volCalcId;
	private String mvId;
	private String mvTypCd;
	private String curCd;	

	private Double mvHisVol;	
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;
	
	public MvVol(String mvId, Double mvHisVol) {
		super();
		this.mvId = mvId;
		this.mvHisVol = mvHisVol;
	}	
	
}
