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
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_PARAM_MODEL_RND")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@SequenceGenerator (name = "IR_PARAM_MODEL_RND_SEQ_GEN",sequenceName = "IR_PARAM_MODEL_RND_SEQ",initialValue = 1, allocationSize = 1)
public class IrParamModelRnd extends BaseEntity implements Serializable, EntityIdentifier {
		
	private static final long serialVersionUID = -1452217231888509501L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_PARAM_MODEL_RND_SEQ")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String baseYymm;
	private String irModelNm;
	private String irCurveNm;
	private Integer sceNo;
	private String matCd;
	
	private Double rndNum;
//	private String lastModifiedBy;
//	private LocalDateTime lastUpdateDate;	

}



