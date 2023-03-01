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
import javax.persistence.Transient;

import com.gof.abstracts.BaseEntity;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="CO_JOB_INFO")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@SequenceGenerator (name = "CO_JOB_INFO_SEQ_GEN",sequenceName = "CO_JOB_INFO_SEQ",initialValue = 1, allocationSize = 1)
public class CoJobInfo  extends BaseEntity implements Serializable {	
	
	private static final long serialVersionUID = 7781683831757307417L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CO_JOB_INFO_SEQ_GEN")
	@Column (name = "SID")
	@Id 
	private long id;

	private String jobId;	
	private String jobNm;
	
	private String baseYymm;
	private String calcDate;	
	private String calcStart;
	
	@Transient
	private LocalDateTime jobStart;
	
	private String calcEnd;
	private String calcElps;
	private String calcScd;	
	
//	private String modifiedBy;
//	private LocalDateTime updateDate;

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CoJobInfo [id=").append(id).append(", jobId=").append(jobId).append(", jobNm=").append(jobNm)
				.append(", baseYymm=").append(baseYymm).append(", calcDate=").append(calcDate).append(", calcStart=")
				.append(calcStart).append(", jobStart=").append(jobStart).append(", calcEnd=").append(calcEnd)
				.append(", calcElps=").append(calcElps).append(", calcScd=").append(calcScd).append(", modifiedBy=")
//				.append(modifiedBy).append(", updateDate=").append(updateDate)
				.append("]");
		return builder.toString();
	}	
	
	
	
}
