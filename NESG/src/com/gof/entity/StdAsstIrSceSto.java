package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.enums.EApplBizDv;
import com.gof.interfaces.EntityIdentifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="STD_ASST_IR_SCE_STO")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "STD_ASST_IR_SCE_STO_SEQ_GEN",sequenceName = "STD_ASST_IR_SCE_STO_SEQ",initialValue = 1, allocationSize = 1)
public class StdAsstIrSceSto implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4432625052946670655L;
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STD_ASST_IR_SCE_STO_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private String baseYymm;
	@Enumerated(EnumType.STRING)
	private EApplBizDv applBizDv;
	private String stdAsstCd;
	private Integer sceTypCd;
	private Integer sceNo;
	private String matCd;

	private Double asstYield;	
	private String modifiedBy;
	private LocalDate updateDate;	
	
}


