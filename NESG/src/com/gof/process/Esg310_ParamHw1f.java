package com.gof.process;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gof.dao.IrParamModelDao;
import com.gof.entity.IrCurve;
//import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrParamHwCalc;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrValidParamHw;
import com.gof.entity.IrVolSwpn;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.interfaces.IRateInput;
import com.gof.model.Hw1fCalibrationKics;
import com.gof.model.entity.SwpnVolInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg310_ParamHw1f extends Process {
	
	public static final Esg310_ParamHw1f INSTANCE = new Esg310_ParamHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static Map<String, List<?>> createParamHw1fNonSplitMap(
			  String bssd
			, EIrModel irModelNm 
			, IrParamModel modelMst
			, List<IRateInput> spotList
			, List<IrVolSwpn> swpnVolList
			, double[] initParas
			, Integer freq
			, int[] alphaPiece
			, int[] sigmaPiece) 
	{
		double errTol = modelMst.getItrTol();
		
		Map<String, List<?>>  irParamHw1fMap  = new TreeMap<String, List<?>>();
		List<IrParamHwCalc>   paramCalc       = new ArrayList<IrParamHwCalc>();
		List<IrValidParamHw>  validParam      = new ArrayList<IrValidParamHw>();		
				
		freq = Math.max(freq, 1);		
		List<SwpnVolInfo> volInfo  = swpnVolList.stream().map(s-> SwpnVolInfo.convertFrom(s)).collect(toList());		
		
		
		Hw1fCalibrationKics calib = new Hw1fCalibrationKics(bssd, spotList, volInfo, alphaPiece, sigmaPiece, initParas, freq, errTol);
//		Hw1fCalibrationKicsNs calib = new Hw1fCalibrationKicsNs(bssd, spotList, volInfo, alphaPiece, sigmaPiece, initParas, freq, errTol);
		paramCalc                 = calib.getHw1fCalibrationResultList().stream().map(s -> s.convertNonSplit(irModelNm, modelMst.getIrCurveNm()))
																			     .flatMap(s-> s.stream())
																			     .collect(toList());

		paramCalc.stream().forEach(s -> s.setIrParamModel(modelMst));
		paramCalc.stream().forEach(s -> s.setIrCurve(modelMst.getIrCurve()));
		paramCalc.stream().forEach(s -> s.setModifiedBy(jobId));
		paramCalc.stream().forEach(s -> s.setUpdateDate(LocalDateTime.now()));
		
		validParam = calib.getValidationResult();
		validParam.stream().forEach(s -> s.setIrModelNm(irModelNm));
		validParam.stream().forEach(s -> s.setIrCurveNm(modelMst.getIrCurveNm()));
		validParam.stream().forEach(s -> s.setIrParamModel(modelMst));
		validParam.stream().forEach(s -> s.setIrCurve(modelMst.getIrCurve()));
		validParam.stream().forEach(s -> s.setModifiedBy(jobId));
		validParam.stream().forEach(s -> s.setUpdateDate(LocalDateTime.now()));
		
		irParamHw1fMap.put("PARAM",  paramCalc);
		irParamHw1fMap.put("VALID",  validParam);
		
//		paramCalc.stream().forEach(s-> log.info("Calibration Result: {}", s.toString()));
//		validParam.stream().forEach(s-> log.info("Validation Result: {}", s.toString()));
		
		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramCalc.size(), irModelNm, toPhysicalName(IrParamHwCalc.class.getSimpleName()));
		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), validParam.size(), irModelNm, toPhysicalName(IrValidParamHw.class.getSimpleName()));
		
		return irParamHw1fMap;
	}
	
	
	public static Map<String, List<?>> createParamHw1fSplitMap(
			  String bssd
			, EIrModel irModelNm
			, IrParamModel modelMst
			, List<IRateInput> spotList
			, List<IrVolSwpn> swpnVolList
			, double[] initParas
			, Integer freq
			, int[] alphaPiece
			, int[] sigmaPiece) 
	{
		
		double errTol = modelMst.getItrTol();
		Map<String, List<?>>  irParamHw1fMap  = new TreeMap<String, List<?>>();
		List<IrParamHwCalc>   paramCalc       = new ArrayList<IrParamHwCalc>();
//		List<IrValidParamHw>  validParam      = new ArrayList<IrValidParamHw>();		
				
		freq = Math.max(freq, 1);		
		List<SwpnVolInfo> volInfo  = swpnVolList.stream().map(s-> SwpnVolInfo.convertFrom(s)).collect(toList());		
		
		Hw1fCalibrationKics calib = new Hw1fCalibrationKics(bssd, spotList, volInfo, alphaPiece, sigmaPiece, initParas, freq, errTol);
		paramCalc                 = calib.getHw1fCalibrationResultList().stream().map(s -> s.convertSplit(irModelNm, modelMst.getIrCurveNm()))
																			     .flatMap(s-> s.stream())
																			     .collect(toList());

		paramCalc.stream().forEach(s -> s.setIrParamModel(modelMst));
		paramCalc.stream().forEach(s -> s.setIrCurve(modelMst.getIrCurve()));
		paramCalc.stream().forEach(s -> s.setModifiedBy(jobId));
		paramCalc.stream().forEach(s -> s.setUpdateDate(LocalDateTime.now()));	

		
		irParamHw1fMap.put("PARAM",  paramCalc);
		
//		paramCalc.stream().forEach(s-> log.info("Calibration Result: {}", s.toString()));
//		validParam.stream().forEach(s-> log.info("Validation Result: {}", s.toString()));
		
		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramCalc.size(), irModelNm, toPhysicalName(IrParamHwCalc.class.getSimpleName()));
//		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), validParam.size(), irModelId, toPhysicalName(IrValidParamHw.class.getSimpleName()));
		
		return irParamHw1fMap;
	}	

}