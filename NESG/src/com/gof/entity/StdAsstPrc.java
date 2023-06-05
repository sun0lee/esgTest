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
@Table(name ="STD_ASST_PRC")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator (name = "STD_ASST_PRC_SEQ_GEN",sequenceName = "STD_ASST_PRC_SEQ",initialValue = 1, allocationSize = 1)
public class StdAsstPrc extends BaseEntity implements Serializable, EntityIdentifier { 
	
	private static final long serialVersionUID = 8822660082355727894L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STD_ASST_PRC_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseDate;
	private String stdAsstCd;	

	private Double stdAsstPrice;	
//	private String lastModifiedBy;	
//	private LocalDateTime lastUpdateDate;
	
}
