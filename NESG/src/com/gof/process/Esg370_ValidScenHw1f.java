package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrParamHwDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrDcntSceStoBiz;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrValidSceSto;
import com.gof.entity.StdAsstIrSceSto;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.enums.EHwParamTypCd;
import com.gof.interfaces.IRateInput;
import com.gof.model.Hw1fSimulationKics;
import com.gof.model.entity.Hw1fCalibParas;
import com.gof.model.entity.IrModelSce;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg370_ValidScenHw1f extends Process {
	
	public static final Esg370_ValidScenHw1f INSTANCE = new Esg370_ValidScenHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static Map<String, List<?>> createValidInputHw1f
	        ( String bssd
			, EApplBizDv applBizDv
			, IrParamModel modelMst
			, Integer irCurveSceNo
			, Map<IrCurve, Map<EDetSce, IrParamSw>> paramSwMap
			, Integer projectionYear
			, Double targetDuration) 
	{
		
		Map<String, List<?>>  rst     = new TreeMap<String, List<?>>();
		List<IrDcntSceStoBiz> sceRst  = new ArrayList<IrDcntSceStoBiz>();
		List<StdAsstIrSceSto> yldRst  = new ArrayList<StdAsstIrSceSto>();
		
		EIrModel irModelNm = modelMst.getIrModelNm();
		String irCurveNm = modelMst.getIrCurveNm();
		
		for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
//				
				if(!StringUtil.objectToPrimitive(swSce.getValue().getStoSceGenYn(), "N").toUpperCase().equals("Y")) continue;
				
				if(!irCurveNm.equals(irCurveNm) || !swSce.getKey().getSceNo().equals(irCurveSceNo)) continue;				
//				log.info("IR_CURVE_ID: [{}], IR_CURVE_SCE_NO: [{}]", irCurveNm, swSce.getKey());
				
//				if(!modelMstMap.containsKey(irCurveNm)) {
//					log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
//					continue;
//				}
				
				List<IRateInput> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, irCurveNm, swSce.getKey());				
				if(adjSpotRate.isEmpty()) {
					log.warn("No Spot Rate Data [ID: {}, SCE_NO: {}] for [{}] in [{}] Table", irCurveNm, swSce.getKey(), bssd, Process.toPhysicalName(IrDcntRateBu.class.getSimpleName()));
					continue;
				}				
									
				List<IrParamHwBiz> paramHw = IrParamHwDao.getIrParamHwBizList(bssd, applBizDv, irModelNm, irCurveNm);					
				if(paramHw.isEmpty()) {
					log.warn("No HW1F Model Parameter exist in [MODEL: {}] [IR_CURVE_ID: {}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamHwBiz.class.getSimpleName()));
					continue;
				}
				List<Hw1fCalibParas> hwParasList = Hw1fCalibParas.convertFrom(paramHw);
//				log.info("{}, {}", hwParasList);
				
				int[] alphaPiece = paramHw.stream().filter(s->s.getParamTypCd()==EHwParamTypCd.ALPHA && s.getMatCd().equals("M0240"))
										  	       .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();
				int[] sigmaPiece = paramHw.stream().filter(s->s.getParamTypCd()==EHwParamTypCd.SIGMA && !s.getMatCd().equals("M1200") && !s.getMatCd().equals("M0240"))
												   .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();	
	//			log.info("{}, {}", alphaPiece, sigmaPiece);				
				
				boolean priceAdj      = false;
				int     randomGenType = 1;
				int     sceNum        = StringUtil.objectToPrimitive(Integer.valueOf(modelMst.getTotalSceNo()), SCEN_NUM);						
				int     seedNum       = StringUtil.objectToPrimitive(Integer.valueOf(modelMst.getRndSeed())   , RANDOM_SEED);
				double  ltfr          = swSce.getValue().getLtfr();
				int     ltfrCp        = swSce.getValue().getLtfrCp();
				log.info("seedNum: {}, {}", seedNum, bssd);		

				Hw1fSimulationKics hw1f = new Hw1fSimulationKics(bssd, adjSpotRate, hwParasList, alphaPiece, sigmaPiece, priceAdj, sceNum, ltfr, ltfrCp, projectionYear, randomGenType, seedNum);				
				
				List<IrModelSce>       hwResult    = hw1f.getIrModelHw1fList();
				List<IrDcntSceStoBiz>  stoBizList  = hwResult.stream().map(s -> s.convert(applBizDv, modelMst, swSce.getKey().getSceNo(), jobId)).collect(Collectors.toList());
//				List<IrDcntSceStoBiz>  stoBizList  = hwResult.stream().filter(s -> !s.getSceNo().equals("0")).map(s -> s.convert(applBizDv, irModelId, irCurveNm, swSce.getKey(), jobId)).collect(Collectors.toList());
				List<StdAsstIrSceSto>  stoYldList  = hw1f.getIrModelHw1fBondYield(hwResult, targetDuration).stream().map(s -> s.convert(applBizDv, irCurveNm, irCurveSceNo, jobId)).collect(Collectors.toList());			
				
				sceRst.addAll(stoBizList);
				yldRst.addAll(stoYldList);
			}
		}
		rst.put("SCE", sceRst);
		rst.put("YLD", yldRst);		
		
		return rst;		
	}
	
	
	public static List<IrValidSceSto> testMarketConsistency
	        ( String bssd
			, EApplBizDv applBizDv
			, IrParamModel modelMst
			, Integer irCurveSceNo
			, TreeMap<Integer, TreeMap<Integer, Double>> stoSceMap
			, TreeMap<Integer, TreeMap<Integer, Double>> stoYldMap
			, Double sigLevel) 
	{		
		
		List<IrValidSceSto> rst = new ArrayList<IrValidSceSto>();		
		EIrModel irModelNm = modelMst.getIrModelNm();
		String irCurveNm = modelMst.getIrCurveNm();
		
		if(stoSceMap.isEmpty()) {			
			log.warn("Martingale Test: No Stochastic Discount Rate Data of [{}] [BIZ: {}, ID: {}, SCE_NO: {}] for [{}]", irModelNm, applBizDv, irCurveNm, irCurveSceNo, bssd);
			return rst;		
		}		
			
		if(stoYldMap.isEmpty()) {
			log.warn("Martingale Test: No Stochastic Bond Yield Data of [{}] [BIZ: {}, ID: {}, SCE_NO: {}] for [{}]", irModelNm, applBizDv, irCurveNm, irCurveSceNo, bssd);
			return rst;		
		}		

		int[]      monthIdx        = stoSceMap.keySet().stream().mapToInt(Integer::intValue).toArray();
		double     dt              = 1.0 / MONTH_IN_YEAR;
		double     upper           = new NormalDistribution().inverseCumulativeProbability(1 - sigLevel / 2.0);
		double     lower           = new NormalDistribution().inverseCumulativeProbability(    sigLevel / 2.0);		

		double[]   detDcntRate     = new double[stoSceMap.keySet().size()];
		double[]   detPrice        = new double[stoSceMap.keySet().size()];

		double[][] stoDcntRate     = new double[stoSceMap.keySet().size()][stoSceMap.firstEntry().getValue().size() - 1];		
		double[][] stoPrice        = new double[stoSceMap.keySet().size()][stoSceMap.firstEntry().getValue().size() - 1];		

		double[]   stoPriceMean    = new double[stoSceMap.keySet().size()];
		double[]   stoPriceSe      = new double[stoSceMap.keySet().size()];		
		double[]   stoPriceUpper   = new double[stoSceMap.keySet().size()];
		double[]   stoPriceLower   = new double[stoSceMap.keySet().size()];
		
		double[]   stoFwdByZcbMean = new double[stoSceMap.keySet().size()];  //Convert ZCB Mean to FWD rate
		double[]   stoDcntRateMean = new double[stoSceMap.keySet().size()];  //Simple Average DiscountRate Group by Month
		
		
		double[]   detYield        = new double[stoYldMap.keySet().size()];
		double[]   detYieldCum     = new double[stoYldMap.keySet().size()];
		double[]   detYieldDotDcnt = new double[stoYldMap.keySet().size()];
		
		double[][] stoYield        = new double[stoYldMap.keySet().size()][stoYldMap.firstEntry().getValue().size()];
		double[][] stoYieldCum     = new double[stoYldMap.keySet().size()][stoYldMap.firstEntry().getValue().size()];
		double[][] stoYieldDotDcnt = new double[stoYldMap.keySet().size()][stoYldMap.firstEntry().getValue().size()];

		double[]   stoYieldDotDcntMean  = new double[stoYldMap.keySet().size()];  //Mean of stoYieldDotDcnt(stoYieldCum * stoDcntRate)
		double[]   stoYieldDotDcntSe    = new double[stoYldMap.keySet().size()];		
		double[]   stoYieldDotDcntUpper = new double[stoYldMap.keySet().size()];
		double[]   stoYieldDotDcntLower = new double[stoYldMap.keySet().size()];
		

		int mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> stoSce : stoSceMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> sto : stoSce.getValue().entrySet()) {
				
				if(sce == 0) {//Deterministic Scenario
					detDcntRate[mat] = sto.getValue();
					detPrice   [mat] = (mat > 0) ? detPrice[mat-1] / Math.pow(1.0 + detDcntRate[mat], dt) : 1.0 / Math.pow(1.0 + detDcntRate[mat], dt);
					
					detYield   [mat] = Math.pow(1.0 + detDcntRate[mat], dt) - 1.0;
					detYieldCum[mat] = (mat > 0) ? (1.0 + detYieldCum[mat-1]) * (1.0 + detYield[mat]) - 1.0 : detYield[mat];
					detYieldDotDcnt[mat] = (1.0 + detYieldCum[mat]) * detPrice[mat];					
				}
				else {
					stoDcntRate[mat][sce-1] = sto.getValue();
					stoPrice   [mat][sce-1] = (mat > 0) ? stoPrice[mat-1][sce-1] / Math.pow(1.0 + stoDcntRate[mat][sce-1], dt) : 1.0 / Math.pow(1.0 + stoDcntRate[mat][sce-1], dt);
				}				
				sce++;
			} 
			mat++;
		}		
		stoDcntRateMean = matToVecMean(stoDcntRate);
		stoPriceMean    = matToVecMean(stoPrice);
		stoPriceSe      = matToVecStdError(stoPrice, (double)stoPrice[0].length);
		
		for(int i=0; i<stoDcntRate.length; i++) {
			double stoPricePre = ((i==0) ? 1.0 : stoPriceMean[i-1]);
			
			stoFwdByZcbMean[i] = Math.pow(1.0 + (stoPricePre / stoPriceMean[i] - 1), MONTH_IN_YEAR) - 1;
			stoPriceUpper  [i] = stoPriceMean[i] + upper * stoPriceSe[i];
			stoPriceLower  [i] = stoPriceMean[i] + lower * stoPriceSe[i];			
		}		
		
		
		mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> stoYld : stoYldMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> sto : stoYld.getValue().entrySet()) {				
				stoYield       [mat][sce] = sto.getValue();
				stoYieldCum    [mat][sce] = (mat > 0) ? (1.0 + stoYieldCum[mat-1][sce]) * (1.0 + stoYield[mat][sce]) - 1.0 : stoYield[mat][sce];
				stoYieldDotDcnt[mat][sce] = (1.0 + stoYieldCum[mat][sce]) * stoPrice[mat][sce];
				sce++;			
			} 
			mat++;
		}		
		stoYieldDotDcntMean = matToVecMean(stoYieldDotDcnt);
		stoYieldDotDcntSe   = matToVecStdError(stoYieldDotDcnt, (double)stoYieldDotDcnt[0].length);
		
		for(int i=0; i<stoYieldDotDcnt.length; i++) {			
			stoYieldDotDcntUpper[i] = stoYieldDotDcntMean[i] + upper * stoYieldDotDcntSe[i];
			stoYieldDotDcntLower[i] = stoYieldDotDcntMean[i] + lower * stoYieldDotDcntSe[i];			
		}				
		
//		for(int i=0; i<stoDcntRate.length; i++) {
//			if(i==1199) {
//				log.info("Idx: {}, detDcntRate: {}, detPrice: {}", i+1, detDcntRate[i], detPrice[i]);
//				log.info("Idx: {}, detYield: {}, detYieldCum: {}, detPrice: {}, detYieldDotDcnt: {}, stoYieldDotDcntMean: {}", i+1, detYield[i], detYieldCum[i], detPrice[i], detYieldDotDcnt[i], stoYieldDotDcntMean[i]);
//			} 
//			for(int j=0; j<stoDcntRate[0].length; j++) {				
//				if(i==1199 && j<10) {
//					log.info("Idx: {},{}, stoDcntRate: {}, stoPrice: {}", i+1, j+1, stoDcntRate[i][j], stoPrice[i][j]);
//					log.info("Idx: {},{}, stoYield: {}, stoYieldCum: {}, stoPrice: {}, stoYieldDotDcnt: {}", i+1, j+1, stoYield[i][j], stoYieldCum[i][j], stoPrice[i][j], stoYieldDotDcnt[i][j], stoYieldDotDcntSe[i]);					
//				}
//			}			
//		}				
		
		//Martingale Test for Discount Rate
		for(int i=0; i<stoPriceMean.length; i++) {			
			
			IrValidSceSto dcnt = new IrValidSceSto();
			dcnt.setBaseYymm(bssd);
			dcnt.setApplBizDv(applBizDv);
			dcnt.setIrModelNm(irModelNm);
			dcnt.setIrCurveNm(irCurveNm);
			dcnt.setIrParamModel(modelMst);                 // add
			dcnt.setIrCurve(modelMst.getIrCurve());         // add
			dcnt.setIrCurveSceNo(irCurveSceNo);
			dcnt.setValidDv("DCNT_TEST");
			
			dcnt.setValidSeq(Integer.valueOf(monthIdx[i]));
			dcnt.setValidVal1(detPrice[i]);
			dcnt.setValidVal2(stoPriceMean[i]);						
			dcnt.setValidVal3(stoPriceLower[i]);
			dcnt.setValidVal4(stoPriceUpper[i]);
			dcnt.setValidVal5((i<=0) ? 1.0 : ((detPrice[i] >= stoPriceLower[i] && detPrice[i] <= stoPriceUpper[i]) ? 1.0 : 0.0));

			dcnt.setModifiedBy(jobId);
			dcnt.setUpdateDate(LocalDateTime.now());
			
			rst.add(dcnt);			
		}		
		
		//Martingale Test for Bond Yield
		for(int i=0; i<stoYieldDotDcntMean.length; i++) {			
			
			IrValidSceSto yld = new IrValidSceSto();
			yld.setBaseYymm(bssd);
			yld.setApplBizDv(applBizDv);
			yld.setIrModelNm(irModelNm);
			yld.setIrCurveNm(irCurveNm);
			yld.setIrParamModel(modelMst);                // add
			yld.setIrCurve(modelMst.getIrCurve());        // add
			yld.setIrCurveSceNo(irCurveSceNo);
			yld.setValidDv("YIELD_TEST");
			
			yld.setValidSeq(Integer.valueOf(monthIdx[i]));
			yld.setValidVal1(detYieldDotDcnt[i]);
			yld.setValidVal2(stoYieldDotDcntMean[i]);						
			yld.setValidVal3(stoYieldDotDcntLower[i]);
			yld.setValidVal4(stoYieldDotDcntUpper[i]);
			yld.setValidVal5((i==0) ? 1.0 : ((detYieldDotDcnt[i] >= stoYieldDotDcntLower[i] && detYieldDotDcnt[i] <= stoYieldDotDcntUpper[i]) ? 1.0 : 0.0));

			yld.setModifiedBy(jobId);
			yld.setUpdateDate(LocalDateTime.now());
			
			rst.add(yld);			
		}
		
		//Martingale Test for Discount Rate from ZCB price
		for(int i=0; i<stoFwdByZcbMean.length; i++) {
			
			IrValidSceSto fwd = new IrValidSceSto();
			fwd.setBaseYymm(bssd);
			fwd.setApplBizDv(applBizDv);
			fwd.setIrModelNm(irModelNm);
			fwd.setIrCurveNm(irCurveNm);
			fwd.setIrParamModel(modelMst);                 // add
			fwd.setIrCurve(modelMst.getIrCurve());         // add
			fwd.setIrCurveSceNo(irCurveSceNo);			
			fwd.setValidDv("FWD_TEST");
			
			fwd.setValidSeq(Integer.valueOf(monthIdx[i]));
			fwd.setValidVal1(detDcntRate[i]);
			fwd.setValidVal2(stoFwdByZcbMean[i]);
			fwd.setValidVal3(detDcntRate[i]-stoFwdByZcbMean[i]);
			fwd.setValidVal4(stoDcntRateMean[i]);						

			fwd.setModifiedBy(jobId);
			fwd.setUpdateDate(LocalDateTime.now());
			
			rst.add(fwd);			
		}		
		return rst;
	}

}