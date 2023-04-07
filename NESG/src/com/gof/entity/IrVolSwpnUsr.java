package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.gof.util.StringUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_VOL_SWPN_USR")
@FilterDef(name="eqBaseDate", parameters= @ParamDef(name ="bssd",  type="string"))
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator (name = "IR_VOL_SWPN_USR_SEQ_GEN",sequenceName = "IR_VOL_SWPN_USR_SEQ",initialValue = 1, allocationSize = 1)
public class IrVolSwpnUsr implements Serializable {
	
	private static final long serialVersionUID = -4010423589739075634L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_VOL_SWPN_USR_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseDate; 
	private String irCurveNm;
	private String swpnMat;
	
	@Column(name = "VOL_SWPN_Y1") 
	private Double volSwpnY1;
	@Column(name = "VOL_SWPN_Y2") 
	private Double volSwpnY2;
	@Column(name = "VOL_SWPN_Y3") 
	private Double volSwpnY3;
	@Column(name = "VOL_SWPN_Y4") 
	private Double volSwpnY4;
	@Column(name = "VOL_SWPN_Y5") 
	private Double volSwpnY5;
	@Column(name = "VOL_SWPN_Y7") 
	private Double volSwpnY7;
	@Column(name = "VOL_SWPN_Y10") 
	private Double volSwpnY10;
	@Column(name = "VOL_SWPN_Y12") 
	private Double volSwpnY12;
	@Column(name = "VOL_SWPN_Y15") 
	private Double volSwpnY15;
	@Column(name = "VOL_SWPN_Y20") 
	private Double volSwpnY20;
	@Column(name = "VOL_SWPN_Y25") 
	private Double volSwpnY25;
	@Column(name = "VOL_SWPN_Y30") 
	private Double volSwpnY30;	
	
	private String modifiedBy;	
	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;

	//user input  entity 값에 대한 default 처리 
	public Double getVolSwpnY1() {
	return volSwpnY1 = StringUtil.objectToPrimitive(volSwpnY1, 25.0) ;
	}
	public Double getVolSwpnY2() {
	return volSwpnY2 = StringUtil.objectToPrimitive(volSwpnY2, 25.0) ;
	}
	public Double getVolSwpnY3() {
	return volSwpnY3 = StringUtil.objectToPrimitive(volSwpnY3, 25.0) ;
	}
	public Double getVolSwpnY4() {
	return volSwpnY4 = StringUtil.objectToPrimitive(volSwpnY4, 25.0) ;
	}
	public Double getVolSwpnY5() {
	return volSwpnY5 = StringUtil.objectToPrimitive(volSwpnY5, 25.0) ;
	}
	public Double getVolSwpnY7() {
	return volSwpnY7 = StringUtil.objectToPrimitive(volSwpnY7, 25.0) ;
	}
	public Double getVolSwpnY10() {
	return volSwpnY10 = StringUtil.objectToPrimitive(volSwpnY10, 25.0) ;
	}
	public Double getVolSwpnY12() {
	return volSwpnY12 = StringUtil.objectToPrimitive(volSwpnY12, 25.0) ;
	}
	public Double getVolSwpnY15() {
	return volSwpnY15 = StringUtil.objectToPrimitive(volSwpnY15, 25.0) ;
	}
	public Double getVolSwpnY20() {
	return volSwpnY20 = StringUtil.objectToPrimitive(volSwpnY20, 25.0) ;
	}
	public Double getVolSwpnY25() {
	return volSwpnY25 = StringUtil.objectToPrimitive(volSwpnY25, 25.0) ;
	}
	public Double getVolSwpnY30() {
	return volSwpnY30 = StringUtil.objectToPrimitive(volSwpnY30, 25.0) ;
	}
}
