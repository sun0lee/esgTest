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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="CO_ESG_META")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@SequenceGenerator (name = "CO_ESG_META_SEQ_GEN",sequenceName = "CO_ESG_META_SEQ",initialValue = 1, allocationSize = 1)
public class CoEsgMeta  extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -474699114755027684L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CO_ESG_META_SEQ_GEN")
	@Column (name = "SID")
	@Id	
	private long id;

	private String groupId;
	
	private String paramKey;	
	private String paramValue;
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;
	
//	private String modifiedBy;
//	private LocalDateTime updateDate;
	

	@Override
	public String toString() {
		return toString(",");
	}

	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		builder.append(groupId).append(delimeter)
				.append(paramKey).append(delimeter)
				.append(paramValue).append(delimeter)
				.append(useYn)
				;

		return builder.toString();
	}	
}


