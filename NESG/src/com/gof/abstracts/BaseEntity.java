package com.gof.abstracts;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;


@MappedSuperclass
@Getter
@Setter
public class BaseEntity implements Serializable{
	private static final long serialVersionUID = -8151467682976876533L;

//	@Id
//	@Column (name = "SID")
//	@GeneratedValue(strategy=GenerationType.IDENTITY)
//	private long id;
	
//	public BaseEntity(String lastModifiedBy, LocalDateTime lastModifiedDate) {
//		this.lastModifiedBy = lastModifiedBy;
//		this.lastModifiedDate = lastModifiedDate;
//	}
//	private String remark;
	
//	@Column(name = "MODIFIED_BY")
	private String modifiedBy;
	private LocalDateTime updateDate;

}
