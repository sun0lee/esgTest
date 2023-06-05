package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrParamHwDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrDcntSceStoBiz;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwRnd;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrValidSceSto;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.enums.EHwParamTypCd;
import com.gof.interfaces.IRateInput;
import com.gof.model.Hw1fSimulationKics;
import com.gof.model.entity.Hw1fCalibParas;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg340_BizScenHw1f extends Process {
	
	public static final Esg340_BizScenHw1f INSTANCE = new Esg340_BizScenHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);

	public static Map<String, List<?>> createScenHw1f(String bssd
													, EApplBizDv applBizDv
													, IrParamModel modelMst
													, Integer irCurveSceNo
													, Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap
//													, Map<String, IrParamModel> modelMstMap
													, Integer projectionYear) 
	{
		Map<String, List<?>>  rst     = new TreeMap<String, List<?>>();
		List<IrDcntSceStoBiz> sceRst  = new ArrayList<IrDcntSceStoBiz>();
		List<IrParamHwRnd>    randRst = new ArrayList<IrParamHwRnd>();
		
		EIrModel irModelNm = modelMst.getIrModelNm();
		String irCurveNm = modelMst.getIrCurveNm();
		
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
//			String irCurveNm = curveSwMap.getKey().getIrCurveNm();
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
			// secnario 단위로 처리하는 경우 
				EDetSce detSceNo = swSce.getKey() ;
				
//				
				if(!StringUtil.objectToPrimitive(swSce.getValue().getStoSceGenYn(), "N").toUpperCase().equals("Y")) continue;				
//				if(!applBizDv.equals("KICS") || !detSceNo.equals(1)) continue;
				
				if(!irCurveNm.equals(irCurveNm) || !detSceNo.getSceNo().equals(irCurveSceNo)) continue;		// 이걸 왜 체크 하는건지??		
//				log.info("IR_CURVE_ID: [{}], IR_CURVE_SCE_NO: [{}]", curveSwMap.getKey(), detSceNo);
				
//				if(!modelMstMap.containsKey(irCurveNm)) {
//					log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
//					continue;
//				}
				
				// 확률론 시나리오 생성에 사용할 기준시나리오 금리 커브 가져오기 
				List<IRateInput> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, irCurveNm, detSceNo);				
//				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateToAdjSpotList(bssd, applBizDv, curveSwMap.getKey(), detSceNo);     //Do Not USE this Huge Array(SW reslt itself). Its Accuracy also have problems.				
//				adjSpotRate.stream().forEach(s -> log.info("{}", s));
				
				if(adjSpotRate.isEmpty()) {
					log.warn("No Spot Rate Data [ID: {}, SCE_NO: {}] for [{}] in [{}] Table", irCurveNm, detSceNo, bssd, Process.toPhysicalName(IrDcntRateBu.class.getSimpleName()));
					continue;
				}				
									
				// 시나리오 생성에 사용할 파라메타 가져오기 
				List<IrParamHwBiz> paramHw = IrParamHwDao.getIrParamHwBizList(bssd, applBizDv, irModelNm, irCurveNm);					
				if(paramHw.isEmpty()) {
					log.warn("No HW1F Model Parameter exist in [MODEL: {}] [IR_CURVE_ID: {}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamHwBiz.class.getSimpleName()));
					continue;
				}
				
				int[] alphaPiece = paramHw.stream().filter(s->s.getParamTypCd()==EHwParamTypCd.ALPHA && s.getMatCd().equals("M0240"))
										  	       .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();				
				
				int[] sigmaPiece = paramHw.stream().filter(s->s.getParamTypCd()==EHwParamTypCd.SIGMA && !s.getMatCd().equals("M1200") && !s.getMatCd().equals("M0240"))
												   .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();	
				log.info("{}, {}", alphaPiece, sigmaPiece);				

				List<Hw1fCalibParas> hwParasList = Hw1fCalibParas.convertFrom(paramHw);
//				hwParasList.stream().forEach(s -> log.info("hwParasList: {}", s));
				
				boolean priceAdj      = false;
				int     randomGenType = 1;
				int     sceNum        = StringUtil.objectToPrimitive(Integer.valueOf(modelMst.getTotalSceNo()), SCEN_NUM);						
				int     seedNum       = StringUtil.objectToPrimitive(Integer.valueOf(modelMst.getRndSeed())   , RANDOM_SEED);
				double  ltfr          = swSce.getValue().getLtfr();
				int     ltfrCp        = swSce.getValue().getLtfrCp();
				log.info("seedNum: {}, {}", seedNum, bssd);
				
				Hw1fSimulationKics hw1f = new Hw1fSimulationKics(bssd, adjSpotRate, hwParasList, alphaPiece, sigmaPiece, priceAdj, sceNum, ltfr, ltfrCp, projectionYear, randomGenType, seedNum);
				List<IrDcntSceStoBiz> stoBizList  = hw1f.getIrModelHw1fList().stream().map(s -> s.convert(applBizDv,modelMst , detSceNo.getSceNo(), jobId)).collect(Collectors.toList());
				List<IrParamHwRnd>    randNumList = new ArrayList<IrParamHwRnd>();				
				
				//TODO:
				if(applBizDv.equals("1KICS") && irCurveNm.equals("1010000") && detSceNo.equals(1)) {
					
//					String pathDir = "C:/Users/NHfire.DESKTOP-J5J0BJV/Desktop/";
					String pathDir = "C:/Users/gof/Desktop/";
					String path0 = pathDir + "SW_FWD_"        + irCurveNm + "_" + detSceNo + ".csv";
					String path1 = pathDir + "HW_FWD_DISC_"   + irCurveNm + "_" + detSceNo + ".csv";
					String path2 = pathDir + "HW_RANDOM_"     + irCurveNm + "_" + detSceNo + ".csv";
					String path3 = pathDir + "HW_YIELD_DISC_" + irCurveNm + "_" + detSceNo + ".csv";
					
					try {
						double[][] sw = new double[hw1f.getFwdDiscBase().length][3];
						for(int i=0; i<sw.length; i++) {
							sw[i][0] = i+1;
							sw[i][1] = hw1f.getSpotDiscBase()[i];
							sw[i][2] = hw1f.getFwdDiscBase()[i];
						}			
						writeArraytoCSV(sw, path0);  //matTranspose(sw)
						writeArraytoCSV(hw1f.getFwdDiscScen(), path1);
//						writeArraytoCSV(matTranspose(hw1f.getFwdDiscScen()), path1);
						if(detSceNo.equals(1)) writeArraytoCSV(hw1f.getRandNum(), path2);
						
						hw1f.getIrModelHw1fBondYield(hw1f.getIrModelHw1fList(), 3.0);
						writeArraytoCSV(hw1f.getBondYieldDisc(), path3);
											
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
				
				if(detSceNo.equals(1)) {
					randNumList = hw1f.getRandomScenList().stream().map(s -> s.setKeys(irModelNm, irCurveNm, jobId)).collect(Collectors.toList());	
				}				
				sceRst.addAll(stoBizList);
				randRst.addAll(randNumList);
			}
		}
		rst.put("SCE", sceRst);
		rst.put("RND", randRst);
		
		log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.get("SCE").size(), applBizDv, irCurveNm, irCurveSceNo, toPhysicalName(IrDcntSceStoBiz.class.getSimpleName()));
		if(applBizDv.equals(EApplBizDv.KICS) && rst.get("RND").size() > 0) {
			log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.get("RND").size(), applBizDv, irCurveNm, irCurveSceNo, toPhysicalName(IrParamHwRnd.class.getSimpleName()));	
		}
		
		return rst;		
	}		
	
	
	public static List<IrValidSceSto> createQuantileValue(
			  String bssd
			, EApplBizDv applBizDv
			, IrParamModel modelMst
//			, EIrModel irModelNm
//			, String irCurveNm
			, Integer irCurveSceNo
			, TreeMap<Integer, TreeMap<Integer, Double>> stoSceMap) {		
		
		List<IrValidSceSto> rst = new ArrayList<IrValidSceSto>();
		
		EIrModel irModelNm = modelMst.getIrModelNm();
		String irCurveNm = modelMst.getIrCurveNm();
		
		if(stoSceMap.isEmpty()) {
			log.warn("Quantile Value: No Stochastic Discount Rate Data of [{}] [BIZ: {}, ID: {}, SCE: {}] for [{}]", irModelNm, applBizDv, irCurveNm, irCurveSceNo, bssd);
			return rst;		
		}

		int[]      monthIdx        = stoSceMap.keySet().stream().mapToInt(Integer::intValue).toArray();
		double     dt              = 1.0 / MONTH_IN_YEAR;

		double[][] stoDcntRate     = new double[stoSceMap.keySet().size()][stoSceMap.firstEntry().getValue().size() - 1];		
		double[][] stoPrice        = new double[stoSceMap.keySet().size()][stoSceMap.firstEntry().getValue().size() - 1];
		double[]   stoDcntRateMean = new double[stoSceMap.keySet().size()];  //Simple Average DiscountRate Group by Month		

		int mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> stoSce : stoSceMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> sto : stoSce.getValue().entrySet()) {				
				if(sce > 0) {
					stoDcntRate[mat][sce-1] = sto.getValue();
					stoPrice   [mat][sce-1] = (mat > 0) ? stoPrice[mat-1][sce-1] / Math.pow(1.0 + stoDcntRate[mat][sce-1], dt) : 1.0 / Math.pow(1.0 + stoDcntRate[mat][sce-1], dt);
				}				
				sce++;
			} 
			mat++;
		}		
		stoDcntRateMean = matToVecMean(stoDcntRate);		
		
		//TODO: for specific maturity, finding sce_no for corresponding quantile of fwd dcntRate 
//		Map<Integer, Double> stoQuantile = stoSceMap.lastEntry().getValue();
//		Map<Integer, Double> stoQuantile = stoSceMap.get(20);
//		stoQuantile.remove(0);		//remove detDcntRate		
//		double[] quantile = stoQuantile.values().stream().mapToDouble(Double::doubleValue).toArray();
				
		Map<Integer, Double> stoQuantile = new TreeMap<Integer, Double>();
		double[] quantile = stoPrice[stoPrice.length-1];		
//		double[] quantile = stoPrice[1199];
		for(int i=0; i<quantile.length; i++) stoQuantile.put(i+1, quantile[i]);
		
		double p000 = new Percentile().evaluate(quantile,  0.01);
		double p025 = new Percentile().evaluate(quantile,  25.0);
		double p050 = new Percentile().evaluate(quantile,  50.0);
		double p075 = new Percentile().evaluate(quantile,  75.0);
		double p100 = new Percentile().evaluate(quantile, 100.0);
		
		double v000 = nearValue(quantile, p000);
		double v025 = nearValue(quantile, p025);
		double v050 = nearValue(quantile, p050);
		double v075 = nearValue(quantile, p075);
		double v100 = nearValue(quantile, p100);

		int    q000 = 1;
		int    q025 = 1;
		int    q050 = 1;
		int    q075 = 1;
		int    q100 = 1;

		for(Map.Entry<Integer, Double> sto : stoQuantile.entrySet()) {
			if(Math.abs(sto.getValue() - v000) < ZERO_DOUBLE / 1.00) q000 = sto.getKey();
			if(Math.abs(sto.getValue() - v025) < ZERO_DOUBLE / 1.00) q025 = sto.getKey();
			if(Math.abs(sto.getValue() - v050) < ZERO_DOUBLE / 1.00) q050 = sto.getKey();
			if(Math.abs(sto.getValue() - v075) < ZERO_DOUBLE / 1.00) q075 = sto.getKey();
			if(Math.abs(sto.getValue() - v100) < ZERO_DOUBLE / 1.00) q100 = sto.getKey();			
		}		

//		log.info("quantile: {}, {}, {}, {}, {}", p000, p025, p050, p075, p100);
//		log.info("quantile: {}, {}, {}, {}, {}", v000, v025, v050, v075, v100);
		log.info("[{}, {}, {}, {}], [QUANTILE SCE_NO: 0%: {}, 25%: {}, 50%: {}, 75%: {}, 100%: {}]", bssd, applBizDv, irCurveNm, irCurveSceNo, q000, q025, q050, q075, q100);		
		

		for(int i=0; i<stoDcntRate.length; i++) {
			
			IrValidSceSto fwd = new IrValidSceSto();
			fwd.setBaseYymm(bssd);
			fwd.setApplBizDv(applBizDv);
			fwd.setIrModelNm(irModelNm);
			fwd.setIrParamModel(modelMst);                   // add )
			fwd.setIrCurve(modelMst.getIrCurve());           // add 
			fwd.setIrCurveNm(irCurveNm);
			fwd.setIrCurveSceNo(irCurveSceNo);			
			fwd.setValidDv("FWD_QUANTILE");
			
			fwd.setValidSeq(Integer.valueOf(monthIdx[i]));

			// stoDcntRate Idx
//			fwd.setValidVal1(stoDcntRate[i][q000-1]);
//			fwd.setValidVal2(stoDcntRate[i][q025-1]);
//			fwd.setValidVal3(stoDcntRate[i][q050-1]);
//			fwd.setValidVal4(stoDcntRate[i][q075-1]);
//			fwd.setValidVal5(stoDcntRate[i][q100-1]);			
			
			// ZcbPrice Idx asc -> DcntRate Idx desc 
			fwd.setValidVal1(stoDcntRate[i][q100-1]);
			fwd.setValidVal2(stoDcntRate[i][q075-1]);
//			fwd.setValidVal3(stoDcntRate[i][q050-1]);
			fwd.setValidVal3(stoDcntRateMean[i]);
			fwd.setValidVal4(stoDcntRate[i][q025-1]);
			fwd.setValidVal5(stoDcntRate[i][q000-1]);			
			
//			fwd.setValidVal1(stoPrice[i][q000-1]);
//			fwd.setValidVal2(stoPrice[i][q025-1]);
//			fwd.setValidVal3(stoPrice[i][q050-1]);
//			fwd.setValidVal4(stoPrice[i][q075-1]);
//			fwd.setValidVal5(stoPrice[i][q100-1]);						

			fwd.setModifiedBy(jobId);
			fwd.setUpdateDate(LocalDateTime.now());
			
			rst.add(fwd);			
		}
		
		
		for(int i=0; i<stoDcntRate.length; i++) {
			
			IrValidSceSto fwd = new IrValidSceSto();
			fwd.setBaseYymm(bssd);
			fwd.setApplBizDv(applBizDv);
			fwd.setIrModelNm(irModelNm);
			fwd.setIrCurveNm(irCurveNm);
			fwd.setIrParamModel(modelMst);                   // add 
			fwd.setIrCurve(modelMst.getIrCurve());           // add
			fwd.setIrCurveSceNo(irCurveSceNo);			
			fwd.setValidDv("FWD_QUANTILE2");
			
			fwd.setValidSeq(Integer.valueOf(monthIdx[i]));			
			fwd.setValidVal1(new Percentile().evaluate(stoDcntRate[i],  0.01));
			fwd.setValidVal2(new Percentile().evaluate(stoDcntRate[i],  25.0));
			fwd.setValidVal3(new Percentile().evaluate(stoDcntRate[i],  50.0));
			fwd.setValidVal4(new Percentile().evaluate(stoDcntRate[i],  75.0));
			fwd.setValidVal5(new Percentile().evaluate(stoDcntRate[i], 100.0));			

			fwd.setModifiedBy(jobId);
			fwd.setUpdateDate(LocalDateTime.now());
			
			rst.add(fwd);			
		}
		log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, irCurveNm, irCurveSceNo, toPhysicalName(IrValidSceSto.class.getSimpleName()));

		return rst;
	}

}