package com.gof.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

//import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntSceDetBiz;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.enums.EAfnsParamTypCd;
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.interfaces.IRateInput;
import com.gof.model.entity.SmithWilsonRslt;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class AFNelsonSiegel extends IrModel {		
	
//	protected String        mode;
	protected EIrModel      mode;
	protected double[]      inputParas;
	protected double[]      initParas;
	protected double[]      optParas;
	protected double[]      optLSC;	                      
	protected boolean       optParasFlag = false;
		
	protected SimpleMatrix  IntShock;
//	protected String[]      IntShockName;
//	protected Integer[]      IntDetSceList;
	
	protected int           nf;
	protected double        dt; 
	protected double        accuracy;
	protected int           itrMax;
	protected double        confInterval;	
	protected double        initSigma;
	                        
	protected double        ltfrL;
	protected double        ltfrA;
	protected int           ltfrT;
	protected double        liqPrem;
	protected double        term;	                        
	protected double        minLambda;
	protected double        maxLambda;	
	protected int           prjYear;
	                        
	protected double[]      coeffLt;
	protected double[]      coeffSt;
	protected double[]      coeffCt;
	protected double[]      residue;
	                        
	protected double        lambda;
	protected double        thetaL;
	protected double        thetaS;
	protected double        thetaC;	
	protected double        kappaL;
	protected double        kappaS;
	protected double        kappaC;	
	protected double        epsilon;
	
	protected List<IrDcntSceDetBiz> rsltList = new ArrayList<IrDcntSceDetBiz>();
	
	// 금리정보를 받아오는 경우.
	public AFNelsonSiegel(String bssd, List<IRateInput> iRateHisList, List<IRateInput> iRateBaseList, IrParamModel irModelMst, IrParamSw irParamSw, Map<String, String> argInDBMap) {
		this( bssd, iRateHisList, iRateBaseList, null , irModelMst,  irParamSw, argInDBMap);
	}
	// 금리 정보가 비어있는 경우. 사용할 테너정보를 받아야 함. 
	public AFNelsonSiegel(String bssd, double[] inTenor , IrParamModel irModelMst, IrParamSw irParamSw, Map<String, String> argInDBMap) {
		this( bssd, null, null , inTenor, irModelMst,  irParamSw, argInDBMap);
	}

	
	public AFNelsonSiegel( String bssd
					 	 , List<IRateInput> iRateHisList  // 금리내역 
					 	 , List<IRateInput> iRateBaseList // 기준일자 금리정보 
					 	 , double[]     inTenor           // 금리내역이 없을때 기본적으로 정의할 테너 (개별job에서 정의)  
					 	 , IrParamModel irModelMst 
					 	 , IrParamSw    irParamSw  
					 	 , Map<String, String>  argInDBMap ) 
	
{		
		// 모형에서 계산에 필요한 초기인수는 필요한 곳에서 풀어헤치기 
		double errorTolerance                 = irModelMst.getItrTol();
		double ltfr                           = irParamSw.getLtfr();
		int    ltfrT                          = irParamSw.getLtfrCp();
		
		double confInterval    = Double. valueOf((String) argInDBMap.getOrDefault("AFNS_CONF_INTERVAL"   , "0.995"));
		int    kalmanItrMax    = Integer.valueOf((String) argInDBMap.getOrDefault("AFNS_KALMAN_ITR_MAX"  , "100"));			
		double sigmaInit       = Double. valueOf((String) argInDBMap.getOrDefault("AFNS_SIGMA_INIT"      , "0.05"));
		double epsilonInit     = Double. valueOf((String) argInDBMap.getOrDefault("AFNS_EPSILON_INIT"    , "0.001"));	
		int    prjYear         = Integer.valueOf((String) argInDBMap.getOrDefault("PROJECTION_YEAR"      , "120"));
		
		
		this.baseDate      =  IrModel.stringToDate(bssd);		
		this.mode          =  irModelMst.getIrModelNm() ;
		this.inputParas    =  null;
		
//		this.setTermStructureHis(iRateHisList, iRateBaseList);
		
		this.isRealNumber  = true;
		this.cmpdType      = 'D';		
		this.dt            = 1.0 /52.0 ;	//weekly only
		this.initSigma     = sigmaInit;
		this.dayCountBasis = DCB_MON_DIF;
		this.ltfrL         = ltfr;
		this.ltfrA         = 0;
		this.ltfrT         = ltfrT;
		this.liqPrem       = 0.0;
		this.term          = 1.0 / 12;
		this.minLambda     = 0.05;
		this.maxLambda     = 2.0;
		this.nf            = 3;
		this.prjYear       = prjYear;
		this.accuracy      = errorTolerance;
		this.itrMax        = kalmanItrMax;
		this.confInterval  = confInterval;
		this.epsilon       = epsilonInit;
		
		if(iRateHisList != null && iRateBaseList != null) {	
		// 금리정보가 있으면 금리 세팅 ,tenor도 여기에서 설정함.  
			this.setTermStructureHis(iRateHisList, iRateBaseList);
			this.setIrmodelAttributes();
		} else 
		{ // 금리정보가 null 일 때 input 값으로 tenor 설정.
			this.tenor = inTenor;
		};

}

	//TODO:
	public void setTermStructureHis(List<IRateInput> iRateHisList, List<IRateInput> iRateBaseList) {		
				
		Map<String, Map<String, Double>> tsHisArg = new TreeMap<String, Map<String, Double>>();		
		tsHisArg = iRateHisList.stream().collect(Collectors.groupingBy(s -> s.getBaseDate(), TreeMap::new, Collectors.toMap(IRateInput::getMatCd, IRateInput::getRate)));
		this.setTermStructureHis(tsHisArg, iRateBaseList);		
		
//		iRateHisList.stream().filter(s -> Double.parseDouble(s.getMatCd().substring(1, 5)) <= 12 ).forEach(s -> log.info("{}, {}, {}", s.getBaseDate(), s.getMatCd(), s.getIntRate()));
	}	
	

	private void setTermStructureHis(Map<String, Map<String, Double>> iRateHisMap, List<IRateInput> iRateBaseList) {			

		Map<String, Map<Double, Double>> tsHis = new TreeMap<String, Map<Double, Double>>();
		
		for(Map.Entry<String, Map<String, Double>> hisArg : iRateHisMap.entrySet()) {					
			Map<Double, Double> ts = new TreeMap<Double, Double>();			
			
			for(Map.Entry<String, Double> arg : hisArg.getValue().entrySet()) {						
				ts.put(Double.parseDouble(arg.getKey().substring(1,5)) / MONTH_IN_YEAR, arg.getValue());				
				tsHis.put(hisArg.getKey(), ts);
			}
		}		
		this.irCurveNm = iRateBaseList.get(0).getIrCurveNm();
//		setTermStructureBase(iRateBaseList);
		setTermStructure(iRateBaseList);
		setTermStructureHis(tsHis, this.termStructureBase);		
	}		

	
	private void setTermStructureHis(Map<String, Map<Double, Double>> termStructureHis, Map<Double, Double> termStructureBase) {
		
		this.termStructureHis   = termStructureHis;
		this.termStructureBase  = termStructureBase;			
		int numObs              = termStructureHis.keySet().size();
		int numTenor            = ((TreeMap<String, Map<Double, Double>>) termStructureHis).firstEntry().getValue().size();
		                        
		this.iRateDateHis       = new LocalDate[numObs];			
		this.iRateHis           = new double   [numObs][numTenor];			
		this.tenor              = new double   [numTenor];
		this.iRateBase          = new double   [numTenor];			
		
		int tau = 0; 
//		log.info("size: {}, {}", this.tenor.length, termStructureBase.size()); 
		for(Map.Entry<Double, Double> base : termStructureBase.entrySet()) {
			this.tenor[tau]     = base.getKey();				
			this.iRateBase[tau] = base.getValue();
			tau++;
		}
		
		int obs = 0;			
		for(Map.Entry<String, Map<Double, Double>> ts : termStructureHis.entrySet()) {				
			int mat = 0;				
			this.iRateDateHis[obs] = stringToDate(ts.getKey());
			
			for(Map.Entry<Double, Double> pts : ts.getValue().entrySet()) {					
				this.iRateHis[obs][mat] = pts.getValue();
				mat++;
			}
			obs++;
		}
	}
	
	
//	public void setTermStructureHis(String[] date, double[] tenor, double[][] iRateHis, double[] iRateBase) {
//		
//		int numObs        = date .length;
//		int numTenor      = tenor.length;
//		
//		this.iRateDateHis = new LocalDate[numObs];		
//		this.iRateHis     = new double   [numObs][numTenor];
//		this.tenor        = tenor;
//		this.iRateBase    = iRateBase;		
//		
//		for(int i=0; i<numObs; i++) {					
//			this.iRateDateHis[i] = stringToDate(date[i]);			
//			for(int j=0; j<numTenor; j++) this.iRateHis[i][j] = iRateHis[i][j];
//		}	
//	}		

	// 230530 add initParam setting : IrParamAfnsCalc에 찍어둠 
	public List<IrParamAfnsCalc> setAfnsInitParamList() {

		List<IrParamAfnsCalc> paramList = new ArrayList<IrParamAfnsCalc>();
		
//		String[] paraNames = new String[] {"INIT_LAMBDA"  , "INIT_THETA_1" , "INIT_THETA_2" , "INIT_THETA_3" , "INIT_KAPPA_1" , "INIT_KAPPA_2" , "INIT_KAPPA_3" , 
//				                           "INIT_SIGMA_11", "INIT_SIGMA_21", "INIT_SIGMA_22", "INIT_SIGMA_31", "INIT_SIGMA_32", "INIT_SIGMA_33", "INIT_EPSILON" };		
		
		// enum으로 정의한 초기모수 목록 가져오기 
		List<EAfnsParamTypCd> initParamNames = EAfnsParamTypCd.getInitParamNames();
//		System.out.println(initParamNames);

		
		if(this.initParas != null) {			
			
			for(int i=0; i<this.initParas.length; i++) {
				
				IrParamAfnsCalc param = new IrParamAfnsCalc();
				param.setBaseYymm(dateToString(this.baseDate).substring(0,6));
				param.setIrModelNm(this.mode);
				param.setIrCurveNm(this.irCurveNm);				
				param.setParamTypCd(initParamNames.get(i));
				param.setParamVal(initParas[i]);
				param.setModifiedBy("INIT_" + this.getClass().getSimpleName());
				param.setUpdateDate(LocalDateTime.now());				
				paramList.add(param);
			}

		}
		return paramList;
	}
	
	
	public List<IrParamAfnsCalc> getAfnsParamList() {

		List<IrParamAfnsCalc> paramList = new ArrayList<IrParamAfnsCalc>();
		
//		String[] optParaNames = new String[] {"LAMBDA"  , "THETA_1" , "THETA_2" , "THETA_3" , "KAPPA_1" , "KAPPA_2" , "KAPPA_3" , 
//				                              "SIGMA_11", "SIGMA_21", "SIGMA_22", "SIGMA_31", "SIGMA_32", "SIGMA_33", "EPSILON" };		
		
		List<EAfnsParamTypCd> optParaNames = EAfnsParamTypCd.getOptParamNames("paras");
		
			
//		String[] optLSCNames  = new String[] {"L0", "S0", "C0"};
		
		List<EAfnsParamTypCd> optLSCNames = EAfnsParamTypCd.getOptParamNames("LSC");
				
		if(this.optParas != null && this.optLSC != null) {			
			
			for(int i=0; i<this.optParas.length; i++) {
				
				IrParamAfnsCalc param = new IrParamAfnsCalc();
				param.setBaseYymm(dateToString(this.baseDate).substring(0,6));
				param.setIrModelNm(this.mode);
				param.setIrCurveNm(this.irCurveNm);				
				param.setParamTypCd(optParaNames.get(i));
				param.setParamVal(optParas[i]);
//				param.setModifiedBy("GESG_" + this.getClass().getSimpleName());
				param.setUpdateDate(LocalDateTime.now());				
				paramList.add(param);
			}
			
			for(int i=0; i<this.optLSC.length; i++) {

				IrParamAfnsCalc param = new IrParamAfnsCalc();
				param.setBaseYymm(dateToString(this.baseDate).substring(0,6));
				param.setIrModelNm(this.mode);
				param.setIrCurveNm(this.irCurveNm);				
				param.setParamTypCd(optLSCNames.get(i));
				param.setParamVal(optLSC[i]);
//				param.setModifiedBy("GESG_" + this.getClass().getSimpleName());
				param.setUpdateDate(LocalDateTime.now());				
				paramList.add(param);				
			}
		}
		return paramList;
	}
	
	
	public List<IrSprdAfnsCalc> getAfnsShockList() {		
		
		List<IrSprdAfnsCalc> shockList = new ArrayList<IrSprdAfnsCalc>();			
        
		if(this.IntShock != null) {			
			
			for(int i=0; i<this.IntShock.numCols(); i++) {
				for(int j=0; j<this.IntShock.numRows(); j++) {
					
					IrSprdAfnsCalc shock = new IrSprdAfnsCalc();
					shock.setBaseYymm(dateToString(this.baseDate).substring(0,6));
					shock.setIrModelNm(this.mode);
					shock.setIrCurveNm(this.irCurveNm);				
//					shock.setIrCurveSceNo(this.IntShockName[i]);
					shock.setIrCurveSceNo(EDetSce.getEDetSce(i+1));
					shock.setMatCd(String.format("%s%04d", 'M', (int) round(this.tenor[j] * MONTH_IN_YEAR, 0)));
					//shock.setMatCd(String.valueOf((int) round(this.tenor[j] * MONTH_IN_YEAR, 0) ));
					shock.setShkSprdCont(this.IntShock.get(j,i));
					shock.setModifiedBy("GESG_" + this.getClass().getSimpleName());
					shock.setUpdateDate(LocalDateTime.now());				
					shockList.add(shock);			
				}
			}			
		}
		return shockList;
	}
	
// 작업 분리 23.05.30 
	
	// 1. Initializing AFNS Parameter
	public void getinitialAfnsParas() {
		if(!this.optParasFlag) {

			initializeAfnsParas();
		}
	}
	
	// 2. afns 모수 최적화 
	public void optimizationParas(List<IrParamAfnsCalc> initParam) {
		if(!this.optParasFlag) {

			// 초기모수 읽어온 값 담기 
			this.initParas = initParam.stream()
				    .mapToDouble(param -> param.getParamVal())
				    .toArray();
	        
			// Determine this.initParas 사용자 입력값을 받을지 산출된 초기모수를 사용할지 
			if(this.inputParas != null) this.initParas = this.inputParas;   

				
			// To set this.optParas, this.optLSC
			kalmanFiltering();
		}
	}
	
	// 3. afns 충격시나리오 생성 
	public void genAfnsShock(List<IrParamAfnsCalc> inOptParam, List<IrParamAfnsCalc> inOptLsc) {
		
		
//		// 최적화된 모수 읽어온 값 담기 
		this.optParas = inOptParam.stream()
			    .mapToDouble(param -> param.getParamVal())
			    .toArray();        
        
        // L,S,C 담기 
        this.optLSC = inOptLsc.stream()
        	    .mapToDouble(param -> param.getParamVal())
        	    .toArray();
		
        
		// To set this.IntShock
		afnsShockGenerating();	
	}
	
	
	
	public List<IrDcntSceDetBiz> getAfnsResultList() {

		if(!this.optParasFlag) {
			// Initializing AFNS Parameter
			initializeAfnsParas();
			
			// Determine this.initParas
			if(this.inputParas != null) this.initParas = this.inputParas;                       		
				
			// To set this.optParas, this.optLSC
			kalmanFiltering();
		}
		
		// To set this.IntShock
		afnsShockGenerating();	
		
		// Applying Smith-Wilson inter - extrapolation
//		this.rsltList.addAll(applySmithWilsonInterpoloation(this.ltfrA,  0.0         ,  "A"));		
//		this.rsltList.addAll(applySmithWilsonInterpoloation(this.ltfrL,  this.liqPrem,  "L"));		
		
//		this.rsltList.stream().filter(s -> Double.parseDouble(s.getMatCd().substring(1, 5)) <= 12 ).forEach(s -> log.info("{}, {}, {}", s));		
		
		return this.rsltList;
	}


	private void initializeAfnsParas() {	
		
//		setIrmodelAttributes();
		findInitialLambda();
		findInitailThetaKappa();		
		
		this.initParas = new double[14];
		
		this.initParas[0]  = this.lambda;
		this.initParas[1]  = this.thetaL;  
		this.initParas[2]  = this.thetaS;  
		this.initParas[3]  = this.thetaC;
		this.initParas[4]  = Math.max(this.kappaL, 1e-4); 
		this.initParas[5]  = Math.max(this.kappaS, 1e-4);		
		this.initParas[6]  = Math.max(this.kappaC, 1e-4);		
		this.initParas[7]  = this.initSigma; 
		this.initParas[8]  = 0.0; 
		this.initParas[9]  = this.initSigma;
		this.initParas[10] = 0.0;            
		this.initParas[11] = 0.0; 
		this.initParas[12] = this.initSigma;
		this.initParas[13] = this.epsilon * 1000;
	}	
	

	private void setIrmodelAttributes() {		

		double toRealScale = this.isRealNumber ? 1 : 0.01;		
		
		for(int i=0; i<this.iRateHis.length; i++) {
//			log.info("{}, {}, {}, {}, {}", this.iRateDateHis[i], this.iRateHis[i][0], this.iRateHis[i][1], this.iRateHis[i][2],this.iRateHis[i][3]);
			for(int j=0; j<this.iRateHis[i].length; j++) {		
				this.iRateHis[i][j] = (this.cmpdType == CMPD_MTD_DISC) ? irDiscToCont(toRealScale*this.iRateHis[i][j]) : toRealScale*this.iRateHis[i][j];				
			}
//			log.info("{}, {}, {}, {}, {}", this.iRateDateHis[i], this.iRateHis[i][0], this.iRateHis[i][1], this.iRateHis[i][2],this.iRateHis[i][3]);
		}	
		
		for(int j=0; j<this.iRateBase.length; j++) {
			this.iRateBase[j] = (this.cmpdType == CMPD_MTD_DISC) ? irDiscToCont(toRealScale*this.iRateBase[j]) : toRealScale*this.iRateBase[j];
//			log.info("{}, {}", this.iRateBase[j]);
		}
		
		coeffLt = new double[this.iRateHis.length];
		coeffSt = new double[this.iRateHis.length];
		coeffCt = new double[this.iRateHis.length];
		residue = new double[this.iRateHis.length];
	}		
		

	protected void findInitialLambda() {
		
		UnivariateFunction fp = new UnivariateFunction() {
			public double value(double lambda) {
				return residualSumOfSquares(lambda);
			}
		};
		
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
		this.lambda = optimizer.optimize(new MaxEval(10000)
				                       , new UnivariateObjectiveFunction(fp)
				                       , GoalType.MINIMIZE
				                       , new SearchInterval(this.minLambda, this.maxLambda)).getPoint();	
		log.info("find initialLamda:{},residual Sum Of Squares : {}", this.lambda, residualSumOfSquares(this.lambda));
	}
	
	
	private double residualSumOfSquares(double lambda) {
		
		double residualSum = 0.0;		
		double[][] xArray = factorLoad(lambda, this.tenor, false);		

		for(int i=0; i<this.iRateHis.length; i++) {		

			double[] yArray = this.iRateHis[i];			
			
			OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
			reg.newSampleData(yArray, xArray);				
			
			double[] rslt = reg.estimateRegressionParameters();			
			coeffLt[i] = rslt[0];
			coeffSt[i] = rslt[1];
			coeffCt[i] = rslt[2];
			residue[i] = reg.calculateResidualSumOfSquares();			
			
			residualSum += residue[i];
		}
//		log.info("residualSum, lambda :{}, {}", residualSum, lambda);
		return residualSum;		
	}	
	
	
	private void findInitailThetaKappa() {	
		
		SimpleRegression linRegL = new SimpleRegression(true);
		SimpleRegression linRegS = new SimpleRegression(true);
		SimpleRegression linRegC = new SimpleRegression(true);		
				
		for(int i=0; i<coeffLt.length-1; i++) {
			
			linRegL.addData(coeffLt[i], coeffLt[i+1]);
			linRegS.addData(coeffSt[i], coeffSt[i+1]);
			linRegC.addData(coeffCt[i], coeffCt[i+1]);			
		}				
		
		this.thetaL = linRegL.getIntercept() / (1.0 - linRegL.getSlope());
		this.thetaS = linRegS.getIntercept() / (1.0 - linRegS.getSlope());
		this.thetaC = linRegC.getIntercept() / (1.0 - linRegC.getSlope());
		
		this.kappaL = -Math.log(linRegL.getSlope()) / this.dt;
		this.kappaS = -Math.log(linRegS.getSlope()) / this.dt;
		this.kappaC = -Math.log(linRegC.getSlope()) / this.dt;
		
		log.info("findInitailThetaKappa :{}, {}, {}, {}, {}, {}", this.thetaL, this.thetaS, this.thetaC, this.kappaL, this.kappaS, this.kappaC);
	}		
	

	private void kalmanFiltering() {		
		kalmanFiltering(this.initParas);
	}
	

	private void kalmanFiltering(double[] paras) {
		
		MultivariateFunction fp = new MultivariateFunction() {			
			public double value(double[] inputParas) {
				return logLikelihood(inputParas);
			}
		};		
		
		double[] fpLower = new double[paras.length];
		double[] fpUpper = new double[paras.length];
		double[] fpScale = new double[paras.length];
		
		
		// 모수 추정 구간 정의 
		for(int i=0; i<paras.length; i++) {
			if(i == 0) {  //lambda
				fpLower[i] = this.minLambda;
				fpUpper[i] = this.maxLambda;				
			}
			else if(i < 4) {  // theta(no constraint)
				fpLower[i] = -100000.0;
				fpUpper[i] = 100000.0;				
			}
			else if(i < 7) {  // kappa 
				fpLower[i] = 1E-4;
				fpUpper[i] = 100000.0;
			}
			else if(i < 13) { // sigma(diagonal only)
				if(i == 7 || i == 9 || i == 12) {
					fpLower[i] = 1E-6;
					fpUpper[i] = 1.0;
				}
				else {
					fpLower[i] = -1.0;
					fpUpper[i] = 1.0;					
				}				
			}
			else { // epsilon
				fpLower[i] = 0.0;
				fpUpper[i] = 1000000;				
			}			
			fpScale[i] = 1000;
		}		
	    MultivariateFunctionPenaltyAdapter fpConstr = new MultivariateFunctionPenaltyAdapter(fp, fpLower, fpUpper, 1000, fpScale);
		
		double[] calibParas = paras;
		double   calibValue = 0.0;	
		
		log.info("{}, {}, {}", LocalDateTime.now(), paras);		
		try {			
			for(int i=0; i<this.itrMax; i++) {		
				
				SimplexOptimizer optimizer = new SimplexOptimizer(1e-12, 1e-12);
				AbstractSimplex  ndsimplex = new NelderMeadSimplex(nelderMeadStep(calibParas, 0.001));
				PointValuePair   result    = optimizer.optimize(new MaxEval(100000)
//						                                      , new ObjectiveFunction(fp)
						                                      , new ObjectiveFunction(fpConstr)
						                                      , ndsimplex
						                                      , GoalType.MINIMIZE
						                                      , new InitialGuess(calibParas));

				log.info("{}, {}, {}, {}", i, result.getValue(), LocalDateTime.now(), result.getPoint());			

				if(Math.abs(result.getValue() - calibValue) < this.accuracy) break;	
				calibParas   = constraints(result.getPoint());				
				calibValue   = result.getValue();				
			}
			
			this.optParas = calibParas;			
			this.optLSC   = new double[] {this.coeffLt[this.iRateHis.length-1], this.coeffSt[this.iRateHis.length-1], this.coeffCt[this.iRateHis.length-1]};
		}
		catch (Exception e) {
			log.error("Error in finding Kalman Gain in AFNS module");
			e.printStackTrace();
		}		
		log.info("{}, {}, {}", LocalDateTime.now(), this.optLSC, this.optParas);
	}		
	

    /**
     * TODO: 
     * Currently, To set init sigma as 1.0e-6 is too small...
     * [Sigma, Step] = [0.05, e-5] ok, However [0.001, E-5] failed, [0.001, E-6] is ok but unstable !
     * Mode: AFNS, Sigma: 0.001, dt: 0.003968253968253968, stepMin: 1.0E-5, -6, -7 failed!! -> initSigma is not to be set 0.001
     * Mode: AFNS, Sigma: 0.05,  dt: 0.003968253968253968, stepMin: 1.0E-5 Success(only),  if -6 then Failed!!		
     */	
	private double[] nelderMeadStep(double[] inputParas, double scale) {
		
		double[] step = new double[inputParas.length];
		for(int i=0; i<step.length; i++) {
			step[i] = Math.max(Math.abs(inputParas[i] * scale), SIMPLEX_STEP_MIN);
		}
//		log.info("step: {}", step);
		return step;		
	}
	
	
	private double logLikelihood(double[] inputParas) {
		
		double[] paras = constraints(inputParas).clone();
		
		double       Lambda = paras[0];
		SimpleMatrix Theta  = new SimpleMatrix(vecToMat(new double[] {paras[1], paras[2], paras[3]}));
		SimpleMatrix Kappa  = new SimpleMatrix(toDiagMatrix(paras[4], paras[5], paras[6]));
		SimpleMatrix Sigma  = new SimpleMatrix(toLowerTriangular3(new double[] {paras[7], paras[8], paras[9], paras[10], paras[11], paras[12]}));				
		
//		SimpleMatrix H      = new SimpleMatrix(toDiagMatrix(paras[13] * 1, this.tenor.length));
//		SimpleMatrix H      = new SimpleMatrix(toDiagMatrix(Math.pow((paras[13] * 1), 2), this.tenor.length));
		
		SimpleMatrix H      = new SimpleMatrix(toDiagMatrix(Math.pow((paras[13] * 0.001), 2), this.tenor.length));
		SimpleMatrix B      = new SimpleMatrix(factorLoad(Lambda, this.tenor, true));		
		SimpleMatrix C      = new SimpleMatrix(vecToMat(afnsC(Sigma, Lambda, this.tenor)));
		
		// Conditional and Unconditional covariance matrix : Q, Q0
		SimpleEVD<SimpleMatrix> eig    = new SimpleMatrix(Kappa).eig();		
		
		//if there is zero eigenvalue then Vmat, Vlim is not consistent
		double []    Eval   = eig.getEigenvalues().stream().map(s -> s.getReal()).mapToDouble(Double::doubleValue).toArray();		
		SimpleMatrix Evec   = new SimpleMatrix(eig.getEigenVector(0)).combine(0, 1, eig.getEigenVector(1)).combine(0,  2,  eig.getEigenVector(2));
		SimpleMatrix iEvec  = Evec.invert();
		SimpleMatrix Smat   = iEvec.mult(Sigma).mult(Sigma.transpose()).mult(iEvec.transpose());

		SimpleMatrix Vmat   = new SimpleMatrix(toDiagMatrix(0.0, 0.0, 0.0));
		SimpleMatrix Vlim   = new SimpleMatrix(toDiagMatrix(0.0, 0.0, 0.0));		
		
		for(int i=0; i<Smat.numRows(); i++) {
			for(int j=0; j<Smat.numCols(); j++) {
				Vmat.set(i, j, Smat.get(i,j) * (1.0 - Math.exp(-(Eval[i]+Eval[j]) * this.dt)) / (Eval[i] + Eval[j]));
				Vlim.set(i, j, Smat.get(i,j)                                                  / (Eval[i] + Eval[j]));
			}
		}
		
		// Analytical Covariance matrix
		SimpleMatrix Q  = Evec.mult(Vmat).mult(Evec.transpose());
		SimpleMatrix Q0 = Evec.mult(Vlim).mult(Evec.transpose());
		
		// Initialization of vector and matrix
		SimpleMatrix PrevX  = Theta;
		SimpleMatrix PrevV  = Q0;			
		SimpleMatrix Phi1   = new SimpleMatrix(toDiagMatrix(Math.exp(-Kappa.get(0,0) * this.dt)
				                                          , Math.exp(-Kappa.get(1,1) * this.dt)
				                                          , Math.exp(-Kappa.get(2,2) * this.dt)));
		SimpleMatrix Phi0   = new SimpleMatrix(toDiagMatrix(1.0, 1.0, 1.0)).minus(Phi1).mult(PrevX);
		
		double logLike = 0.0;		
		
		for(int i=0; i<this.iRateHis.length; i++) {
			
			SimpleMatrix Xhat  = Phi0.plus(Phi1.mult(PrevX));
			SimpleMatrix Vhat  = Phi1.mult(PrevV).mult(Phi1.transpose()).plus(Q);			
			
			// The model-implied yields
			SimpleMatrix Y     = new SimpleMatrix(vecToMat(this.iRateHis[i]));			
//			SimpleMatrix Yimp  = B.mult(Xhat).plus(C.scale(mode.toUpperCase().equals("AFNS") ? 1 : 0));			
			SimpleMatrix Yimp  = B.mult(Xhat).plus(C.scale(mode.equals(EIrModel.AFNS) ? 1 : 0));			
			SimpleMatrix Er    = Y.minus(Yimp);			
			
			// Updating
			SimpleMatrix Ev    = B.mult(Vhat).mult(B.transpose()).plus(H);
		    SimpleMatrix Evinv = Ev.invert();
			SimpleMatrix KG    = Vhat.mult(B.transpose()).mult(Evinv);			
			PrevX              = Xhat.plus(KG.mult(Er));
			PrevV              = Vhat.minus(KG.mult(B).mult(Vhat));
			
			// Log-Likelihood function
			logLike           += - 0.5 * this.tenor.length * Math.log(2 * Math.PI) - 0.5 * Math.log(Ev.determinant()) - 0.5 * Er.transpose().mult(Evinv).dot(Er);
			
			this.coeffLt[i] = PrevX.get(0,0);  this.coeffSt[i] = PrevX.get(1,0);  this.coeffCt[i] = PrevX.get(2,0);
		}		
		return -logLike;
	}
	
	
	private double[] constraints(double[] paras) {		
		
		double[] paraCon = paras.clone();
		
//		paraCon[0]  = Math.min(Math.max(paraCon[0] ,  this.minLambda), this.maxLambda);  // 0: lambda, 1/2/3: theta LSC 
//		paraCon[4]  = Math.min(Math.max(paraCon[4] ,  1e-4), 100000);                    // 4: kappaL
//		paraCon[5]  = Math.min(Math.max(paraCon[5] ,  1e-4), 100000);                    // 5: kappaS
//		paraCon[6]  = Math.min(Math.max(paraCon[6] ,  1e-4), 100000);                    // 6: kappaC
//		paraCon[7]  = Math.min(Math.max(paraCon[7] ,  0e-4), 1.0000);                    // 7: sigma11		
//		paraCon[9]  = Math.min(Math.max(paraCon[9] ,  0e-4), 1.0000);                    // 9: sigma22
//		paraCon[12] = Math.min(Math.max(paraCon[12],  0e-4), 1.0000);                    //12: sigma33

		return(paraCon);		
	}	


	private void afnsShockGenerating() {
		
		double       Lambda     = this.optParas[0];
		SimpleMatrix Theta      = new SimpleMatrix(vecToMat(new double[] {this.optParas[1], this.optParas[2], this.optParas[3]}));
		SimpleMatrix Kappa      = new SimpleMatrix(toDiagMatrix(this.optParas[4], this.optParas[5], this.optParas[6]));
		SimpleMatrix Sigma      = new SimpleMatrix(toLowerTriangular3(new double[] {this.optParas[7], this.optParas[8], this.optParas[9], this.optParas[10], this.optParas[11], this.optParas[12]}));
		SimpleMatrix X0         = new SimpleMatrix(vecToMat(new double[] {this.optLSC[0], this.optLSC[1], this.optLSC[2]}));		
		
		
		// AFNS factor loading matrix based on LLP weight
		double[]     tenorLLP   = new double[(int) (Math.round(this.tenor[this.tenor.length-1]))];
		for(int i=0; i<tenorLLP.length; i++) tenorLLP[i] = i+1;
		SimpleMatrix factorLLP  = new SimpleMatrix(factorLoad(Lambda, tenorLLP, true));

		
		// Declare M, N and Calculate NTN | eKappa ~ Kappa^-1 x (I-exp(-Kappa)) x Sigma  |  N ~ W.mat x M  |  NTN ~ t(N) x N
		SimpleMatrix eKappa     = new SimpleMatrix(toDiagMatrix(Math.exp(-Kappa.get(0,0)), Math.exp(-Kappa.get(1,1)), Math.exp(-Kappa.get(2,2))));
		
//		2023.05.30 주석처리 : for 엑셀과 로직 맞추기 변동성 행렬 산출 시 주 요인만을 고려 (level, slope)
//		SimpleMatrix IminusK    = new SimpleMatrix(toIdentityMatrix(this.nf)).minus(eKappa);
//		SimpleMatrix M          = Kappa.invert().mult(IminusK).mult(Sigma);		
		
        ////////////////////////////////////////////////////////////////////////////////	
//		2023.05.30 수정 : for 엑셀과 로직 맞추기 변동성 행렬 산출 시 모든 요인을 고려(level, slope, curvature)
		
		double[][] mTemp = new double[this.nf][this.nf];		
		for(int i=0; i<mTemp.length; i++) {
			for(int j=0; j<mTemp[i].length; j++) {
				mTemp[i][j]     = (1.0 - eKappa.get(i,i) * eKappa.get(j,j)) / (Kappa.get(i,i) + Kappa.get(j,j));								
			}
		}	
		SimpleMatrix M1         = Sigma.mult(Sigma.transpose()).elementMult(new SimpleMatrix(mTemp));
		
		CholeskyDecomposition_F64<DMatrixRMaj> chol = DecompositionFactory_DDRM.chol(true);
		
		if(!chol.decompose(M1.getDDRM())) {
			log.error("Cholesky Decomposition is failed in AFNS Process!");
			System.exit(0);
		}		
		SimpleMatrix M          = new SimpleMatrix(chol.getT(M1.getDDRM()));  //for IAIS Modified after 2017		
		for(int i=0; i<M.numRows(); i++) log.info("M matrix: {}, {}, {}", M.get(i,0),  M.get(i,1),  M.get(i,2));		

        ////////////////////////////////////////////////////////////////////////////////		

		SimpleMatrix N          = new SimpleMatrix(toDiagMatrix(factorLLP.extractMatrix(0, tenorLLP.length, 0, 1).elementSum()
				                                              , factorLLP.extractMatrix(0, tenorLLP.length, 1, 2).elementSum()
				                                              , factorLLP.extractMatrix(0, tenorLLP.length, 2, 3).elementSum())).mult(M);
//		2023.05.29 수정  : for 엑셀과 로직 맞추기 
//		SimpleMatrix NTN        = N.transpose().mult(N);   //for IAIS(International Association of Insurance Supervisors) 2017 Field Testing Tech. doc(page 201 of 326)
		SimpleMatrix NTN	    = N.mult(N.transpose());   //for KIDI Article(2018-2) (page 28)
		
		// Eigen Decomposition & get rotation angle
		Map<Integer, List<Double>> eigVec =  eigenValueUserDefined(NTN, 3);		
//		Map<Integer, List<Double>> eigVec =  eigenValueOrigin(NTN, 3);
		SimpleMatrix Me1        = M.mult(new SimpleMatrix(vecToMat(eigVec.get(0).stream().mapToDouble(Double::doubleValue).toArray())));
		SimpleMatrix Me2        = M.mult(new SimpleMatrix(vecToMat(eigVec.get(1).stream().mapToDouble(Double::doubleValue).toArray())));
		SimpleMatrix S1         = factorLLP.mult(Me1);
		SimpleMatrix S2         = factorLLP.mult(Me2);		
//		double rotation         = Math.atan(S2.elementSum() / S1.elementSum());
		double rotation         = Math.atan2(S2.elementSum(), S1.elementSum());
		
		// Mean-Reversion, Level and Twist Shock
		SimpleMatrix MeanR      = new SimpleMatrix(toIdentityMatrix(this.nf)).minus(eKappa).mult(Theta.minus(X0));
		SimpleMatrix Level      = new SimpleMatrix(Me1.scale( Math.cos(rotation)).plus(Me2.scale(Math.sin(rotation)))).scale(new NormalDistribution().inverseCumulativeProbability(this.confInterval));
		SimpleMatrix Twist      = new SimpleMatrix(Me1.scale(-Math.sin(rotation)).plus(Me2.scale(Math.cos(rotation)))).scale(new NormalDistribution().inverseCumulativeProbability(this.confInterval));		

		SimpleMatrix CoefInt    = new SimpleMatrix(factorLoad(Lambda, this.tenor, true));		                        		
		SimpleMatrix BaseShock  = CoefInt.mult(new SimpleMatrix(vecToMat(new double[] {0.0, 0.0, 0.0})));
		SimpleMatrix MeanRShock = CoefInt.mult(MeanR);
		SimpleMatrix LevelShock = CoefInt.mult(Level);
		SimpleMatrix TwistShock = CoefInt.mult(Twist);
		
//		for(int i=0; i<MeanRShock.numRows(); i++) log.info("{}",MeanRShock.get(i));
		
		//TODO: To adjust Shock Scale in case of daily history(only in AFNS)
		double levelScale = LevelShock.get(LevelShock.numRows()-1,0) > ZERO_DOUBLE ? 1.0 : -1.0;
		double twistScale = TwistShock.get(TwistShock.numRows()-1,0) > ZERO_DOUBLE ? 1.0 : -1.0;
						
		this.IntShock           = new SimpleMatrix(BaseShock).concatColumns(MeanRShock)
			                                                 .concatColumns(LevelShock.scale(+levelScale)).concatColumns(LevelShock.scale(-levelScale))
			                                                 .concatColumns(TwistShock.scale(-twistScale)).concatColumns(TwistShock.scale(+twistScale));
//				                                             .concatColumns(TwistShock.scale(+twistScale)).concatColumns(TwistShock.scale(-twistScale));
		
//		this.IntShockName       = new String[] {"BASE", "MEAN", "UP", "DOWN", "FLAT", "STEEP"};
//		this.IntShockName       = new String[] {"1", "2", "3", "4", "5", "6"};
//		this.IntDetSceList       = new Integer[] {1, 2, 3, 4, 5, 6};
	}		
	
@Deprecated
	protected List<IrDcntSceDetBiz> applySmithWilsonInterpoloation(double ltfr, double liqPrem, String type) {

		List<IrDcntSceDetBiz> curveList = new ArrayList<IrDcntSceDetBiz>();

		SimpleMatrix spotShock = toSpotShock(this.IntShock, liqPrem);		
//		log.info("{}, {}", this.IntShock, spotShock);
		
		for(int i=0; i<spotShock.numCols(); i++) {
			
			Map<Double, Double> ts = new TreeMap<Double, Double>();
			for(int j=0; j<this.tenor.length; j++) ts.put(this.tenor[j], spotShock.get(j,i));		
//			log.info("{}, {}", i+1, ts);
			
//			if(i==0) log.info("{}, {}, {}, {}", ts, ltfr, ltfrT, this.dayCountBasis);
			SmithWilsonKics sw = new SmithWilsonKics(this.baseDate, ts, CMPD_MTD_CONT, true, ltfr, this.ltfrT, this.prjYear, 1, 100, this.dayCountBasis);			
//			SmithWilsonKics sw = SmithWilsonKics.of()
//					.baseDate       (this.baseDate)
//					.irCurveHisList (ts)
//					.cmpdType       (CMPD_MTD_CONT)
//					.isRealNumber   (true)
//					.ltfr           (ltfr)
//					.ltfrT          (this.ltfrT)
//					.prjYear        (this.prjYear)
//					.prjInterval    (1)
//					.alphaItrNum    (100)
//					.dayCountBasis  (this.dayCountBasis)
//					.build();
			
			
			List<SmithWilsonRslt> swRslt = sw.getSmithWilsonResultList();			
				
			for(SmithWilsonRslt rslt : swRslt) {				
				
				IrDcntSceDetBiz ir  = new IrDcntSceDetBiz();
				
				ir.setBaseYymm(dateToString(this.baseDate).substring(0,6));
				ir.setIrModelId(this.mode);
				ir.setMatCd(rslt.getMatCd());
//				ir.setSceNo(type + String.valueOf(i+1) + "_" + this.IntShockName[i]);
//				ir.setIrCurveSceNo(String.valueOf((type.equals("L") ? 0 : 10) + (i+1)));
				ir.setIrCurveSceNo((type.equals("L") ? 0 : 10) + (i+1));
				ir.setIrCurveId(this.irCurveNm);
				ir.setSpotRate(rslt.getSpotDisc());
				ir.setFwdRate(0.0);
				ir.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
				ir.setLastUpdateDate(LocalDateTime.now());				
				curveList.add(ir);
			}						
//			log.info("scenNo: {}, swAlpha: {}", type + String.valueOf(i+1), sw.getAlphaApplied());
		}		
		return curveList;
	}
		
	
	private SimpleMatrix toSpotShock(SimpleMatrix intShock, double liqPrem) {
		
		SimpleMatrix baseRate = new SimpleMatrix(vecToMat(this.iRateBase));
		SimpleMatrix intBase  = baseRate;
		for(int i=1; i<intShock.numCols(); i++) intBase = intBase.concatColumns(baseRate);				

//		TODO:
		SimpleMatrix spotShock = intBase.plus(intShock).elementExp().plus(liqPrem).elementLog();   // to_cont(to_disc(intBase + intShock) + VA)
//		SimpleMatrix spotShock = intBase.plus(intShock).plus(liqPrem);		
		
		return spotShock;
	}	


	private double[][] factorLoad(double lambda, double[] tau, boolean isfull) {
		
		double[][] fLoadFull =  factorLoad(lambda, tau);		
		if(isfull) return fLoadFull;
		
		double[][] fLoad = new double[tau.length][2];			
		
		for(int i=0; i<fLoad.length; i++) {
			for(int j=0; j<2; j++) fLoad[i][j] = fLoadFull[i][j+1];
//			log.info("{}, {}, {}, {}, {}", lambda, tau[i], fLoad[i][0], fLoad[i][1]);
		}			
		return fLoad;
	}
	

	private double[][] factorLoad(double lambda, double[] tau) {
		
		double[][] fLoad = new double[tau.length][3];                                 // fLoad[i] = [L1, S1, C1], ... , [Ln, Sn, Cn]
		
		for(int i=0; i<fLoad.length; i++) {			
			fLoad[i][0] = 1.0;                                                        // L component in LSC
			fLoad[i][1] = (1.0 - Math.exp(-lambda * tau[i])) / (lambda * tau[i]);     // S component in LSC
			fLoad[i][2] = fLoad[i][1] - Math.exp(-lambda * tau[i]);			          // C component in LSC
//			log.info("{}, {}, {}, {}, {}", lambda, tau[i], fLoad[i][0], fLoad[i][1], fLoad[i][2]);
		}		
		return fLoad;
	}
	
	
	protected static double[] afnsC(SimpleMatrix sigma, double lambda, double[] tau) {
		
		double s11 = sigma.get(0,0),  s12 = sigma.get(0,1),  s13 = sigma.get(0,2);
		double s21 = sigma.get(1,0),  s22 = sigma.get(1,1),  s23 = sigma.get(1,2);
		double s31 = sigma.get(2,0),  s32 = sigma.get(2,1),  s33 = sigma.get(2,2);
				
		double A = s11*s11 + s12*s12 + s13*s13;  double D = s11*s21 + s12*s22 + s13*s23;		
		double B = s21*s21 + s22*s22 + s23*s23;  double E = s11*s31 + s12*s32 + s13*s33;
		double C = s31*s31 + s32*s32 + s33*s33;  double F = s21*s31 + s22*s32 + s23*s33;		
		
		double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0, r6 = 0;
		double la = lambda, la2 = Math.pow(lambda, 2), la3 = Math.pow(lambda, 3);
		
		double[] afnsC = new double[tau.length];
		
		for(int i=0; i<tau.length; i++) {			
						
			r1 = -A * tau[i]*tau[i]/6;
			r2 = -B * (  1/(2*la2) - (1-Math.exp(-la*tau[i]))/(la3*tau[i]) + (1-Math.exp(-2*la*tau[i]))/(4*la3*tau[i])  );
			r3 = -C * (  1/(2*la2) + Math.exp(-la*tau[i])/(la2) - tau[i]*Math.exp(-2*la*tau[i])/(4*la) 
			           - 3*Math.exp(-2*la*tau[i])/(4*la2) - 2*(1-Math.exp(-la*tau[i]))/(la3*tau[i])
			           + 5*(1-Math.exp(-2*la*tau[i]))/(8*la3*tau[i])  );
			r4 = -D * (  tau[i]/(2*la) + Math.exp(-la*tau[i])/(la2) - (1-Math.exp(-la*tau[i]))/(la3*tau[i])  );
			r5 = -E * (  3*Math.exp(-la*tau[i])/(la2) + tau[i]/(2*la) + tau[i]*Math.exp(-la*tau[i])/(la) - 3*(1-Math.exp(-la*tau[i]))/(la3*tau[i])  );
			r6 = -F * (  1/(la2) + Math.exp(-la*tau[i])/(la2) - Math.exp(-2*la*tau[i])/(2*la2)
				       - 3*(1-Math.exp(-la*tau[i]))/(la3*tau[i]) + 3*(1-Math.exp(-2*la*tau[i]))/(4*la3*tau[i])  );
			
			afnsC[i] = r1 + r2 + r3 + r4 + r5 + r6;
			//log.info("{}, {}, {}, {}, {}, {}, {}", r1, r2, r3, r4, r5, r6, afnsC[i]);
						
		}		
		return afnsC;		
	}
	
	
	protected static double[] afnsC(double[][] sigma, double lambda, double[] tau) {		
		return afnsC(new SimpleMatrix(sigma), lambda, tau);
	}

	
	protected static double[] nelsonSiegelTermStructure(double lambda, double[] tenor, double[] Lt, double[] St, double[] Ct) {
		
		double[] iRate = new double[tenor.length];
		for(int i=0; i<iRate.length; i++) iRate[i] = nelsonSiegelFn(lambda, tenor[i], Lt[i], St[i], Ct[i]);
		
		return iRate;
	}
	
	
	private static double nelsonSiegelFn(double lambda, double tenor, double Lt, double St, double Ct) {
		return nelsonSiegelFn(lambda, tenor, Lt, St, Ct, 0.0);
	}	
	
	
	private static double nelsonSiegelFn(double lambda, double tenor, double Lt, double St, double Ct, double epsilon) {
		
		double lamTau = lambda *  tenor;
		return Lt * 1.0 + St * ((1 - Math.exp(-lamTau)) / lamTau) + Ct * ((1 - Math.exp(-lamTau)) / lamTau - Math.exp(-lamTau)) + epsilon;
	}
	
}
