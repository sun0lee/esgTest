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

//import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="CO_JOB_LIST")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "CO_JOB_LIST_SEQ_GEN",sequenceName = "CO_JOB_LIST_SEQ",initialValue = 1, allocationSize = 1)
public class CoJobList  extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -8427725540244046466L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CO_JOB_LIST_SEQ_GEN")
	@Column (name = "SID")
	@Id 
	private long id;

	private String jobNm;	
	private String jobName;	
	private String useYn;
	
//	private String modifiedBy;
//	private LocalDateTime updateDate;	

}