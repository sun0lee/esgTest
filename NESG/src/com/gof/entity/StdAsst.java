package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
import com.gof.enums.EBoolean;
import com.gof.interfaces.EntityIdentifier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="STD_ASST")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator (name = "STD_ASST_SEQ_GEN",sequenceName = "STD_ASST_SEQ",initialValue = 1, allocationSize = 1)
public class StdAsst extends BaseEntity implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -2698064271365825157L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STD_ASST_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;
	
	private String stdAsstCd;
	
	private String stdAsstNm;	
	private int seq;
	private String stdAsstTypCd;
	private String curCd;
	private String hisTable;	
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;
//	private String lastModifiedBy;	
//	private LocalDateTime lastUpdateDate;	

}
