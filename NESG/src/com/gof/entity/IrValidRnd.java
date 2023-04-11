package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.enums.EIrModel;
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_IR_VALID_RND")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrValidRnd implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4899640834780900414L;

	@Id
	private String baseYymm;
	
    @Id
    @Enumerated (EnumType.STRING)
	private EIrModel irModelId;	
	
	@Id
	private String irCurveId;
		
	@Id
	private String validDv;
	
	@Id
	private Integer validSeq;	
	
	private Double validVal1;
	private Double validVal2;
	private Double validVal3;
	private Double validVal4;	
	private Double validVal5;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	
	
}