package com.gof.entity;

import java.io.Serializable;
//import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gof.abstracts.BaseEntity;
import com.gof.util.StringUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="IR_CURVE_YTM_USR_HIS")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
@SequenceGenerator (name = "IR_CURVE_YTM_USR_HIS_SEQ_GEN",sequenceName = "IR_CURVE_YTM_USR_HIS_SEQ",initialValue = 1, allocationSize = 1)
public class IrCurveYtmUsrHis extends BaseEntity implements Serializable {	

	private static final long serialVersionUID = 6426252298359459167L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IR_CURVE_YTM_USR_HIS_SEQ_GEN")
	@Column (name = "SID")
	@Id
	private long id;

	private String baseDate;	
	private String irCurveNm;	
	
	@Column(name = "YTM_M0003") private Double ytmM0003;
	@Column(name = "YTM_M0006") private Double ytmM0006;
	@Column(name = "YTM_M0009") private Double ytmM0009;
	@Column(name = "YTM_M0012") private Double ytmM0012;
	@Column(name = "YTM_M0018") private Double ytmM0018;
	@Column(name = "YTM_M0024") private Double ytmM0024;
	@Column(name = "YTM_M0030") private Double ytmM0030;
	@Column(name = "YTM_M0036") private Double ytmM0036;
	@Column(name = "YTM_M0048") private Double ytmM0048;
	@Column(name = "YTM_M0060") private Double ytmM0060;
	@Column(name = "YTM_M0084") private Double ytmM0084;
	@Column(name = "YTM_M0120") private Double ytmM0120;
	@Column(name = "YTM_M0180") private Double ytmM0180;
	@Column(name = "YTM_M0240") private Double ytmM0240;
	@Column(name = "YTM_M0360") private Double ytmM0360;
	@Column(name = "YTM_M0600") private Double ytmM0600;
	
//	private String modifiedBy;	
//	private LocalDateTime updateDate;	
	
	@ManyToOne
	@JoinColumn(name = "IR_CURVE_SID" , referencedColumnName ="SID")
	private IrCurve irCurve ;
	
	public Double getYtmM0003() {
		return ytmM0003 = StringUtil.objectToPrimitive(ytmM0003, 0.0) ;
		
	}
	public Double getYtmM0006() {
		return ytmM0006 = StringUtil.objectToPrimitive(ytmM0006, 0.0) ;
		
	}
	public Double getYtmM0009() {
		return ytmM0009 = StringUtil.objectToPrimitive(ytmM0009, 0.0) ;
		
	}
	public Double getYtmM0012() {
		return ytmM0012 = StringUtil.objectToPrimitive(ytmM0012, 0.0) ;
		
	}
	public Double getYtmM0018() {
		return ytmM0018 = StringUtil.objectToPrimitive(ytmM0018, 0.0) ;
		
	}
	public Double getYtmM0024() {
		return ytmM0024 = StringUtil.objectToPrimitive(ytmM0024, 0.0) ;
		
	}
	public Double getYtmM0030() {
		return ytmM0030 = StringUtil.objectToPrimitive(ytmM0030, 0.0) ;
		
	}
	public Double getYtmM0036() {
		return ytmM0036 = StringUtil.objectToPrimitive(ytmM0036, 0.0) ;
		
	}
	public Double getYtmM0048() {
		return ytmM0048 = StringUtil.objectToPrimitive(ytmM0048, 0.0) ;
		
	}
	public Double getYtmM0060() {
		return ytmM0060 = StringUtil.objectToPrimitive(ytmM0060, 0.0) ;
		
	}
	public Double getYtmM0084() {
		return ytmM0084 = StringUtil.objectToPrimitive(ytmM0084, 0.0) ;
		
	}
	public Double getYtmM0120() {
		return ytmM0120 = StringUtil.objectToPrimitive(ytmM0120, 0.0) ;
		
	}
	public Double getYtmM0180() {
		return ytmM0180 = StringUtil.objectToPrimitive(ytmM0180, 0.0) ;
		
	}
	public Double getYtmM0240() {
		return ytmM0240 = StringUtil.objectToPrimitive(ytmM0240, 0.0) ;
		
	}
	public Double getYtmM0360() {
		return ytmM0360 = StringUtil.objectToPrimitive(ytmM0360, 0.0) ;
		
	}
	public Double getYtmM0600() {
		return ytmM0600 = StringUtil.objectToPrimitive(ytmM0600, 0.0) ;
		
	}
}