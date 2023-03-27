package com.gof.entity;

import java.io.Serializable;
//import java.time.LocalDateTime;

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
@Table(name ="IR_SPRD_CRD")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@SequenceGenerator (name = "IR_SPRD_CRD_SEQ_GEN",sequenceName = "IR_SPRD_CRD_SEQ",initialValue = 1, allocationSize = 1)
public class IrSprdCrd extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 76762312409390492L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_SPRD_CRD_SEQ_GEN")
	@Column (name = "SID")
	@Id	
	private long id;

	private String baseYymm;
	private String crdGrdCd;
	private String crdGrdNm;
	private String matCd;	
	
	private Double crdSprd;
//	private String modifiedBy;	
//	private LocalDateTime updateDate;	
	
}


