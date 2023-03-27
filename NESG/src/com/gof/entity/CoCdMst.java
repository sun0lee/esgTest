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

//import com.gof.interfaces.EntityIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="CO_CD_MST")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper=false)
@SequenceGenerator (name = "CO_CD_MST_SEQ_GEN",sequenceName = "CO_CD_MST_SEQ",initialValue = 1, allocationSize = 1)
public class CoCdMst extends BaseEntity implements Serializable {	
	
	private static final long serialVersionUID = 7049237275411088860L;

	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CO_CD_MST_SEQ_GEN")
	@Column (name = "SID")
	private long id;

	private String grpCd;	
	private String grpNm;	
	
	private String cd;	
	private String cdNm;	
	private Integer codeOrd;		
//	private String modifiedBy;
//	private LocalDateTime updateDate;
	
}
