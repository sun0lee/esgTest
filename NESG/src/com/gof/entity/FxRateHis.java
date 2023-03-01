package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="FX_RATE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@SequenceGenerator (name = "FX_RATE_SEQ_GEN",sequenceName = "FX_RATE_SEQ",initialValue = 1, allocationSize = 1)
public class FxRateHis  extends BaseEntity implements Serializable {	
	
	private static final long serialVersionUID = 7749489060564117278L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FX_RATE_SEQ_GEN")
	@Column (name = "SID")
	@Id 
	private long id;

	private String baseDate;	
	
	private String curCd;
	
	private Double bslBseRt;
//	private String modifiedBy;
//	private LocalDateTime updateDate;	
	
}
