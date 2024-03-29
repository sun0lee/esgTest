package com.gof.process;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.function.Function;
//import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;

import com.gof.dao.CoEsgMetaDao;
import com.gof.dao.CoJobListDao;
import com.gof.dao.IrCurveDao;
import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrCurveSpotWeekDao;
import com.gof.dao.IrCurveYtmDao;
import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrParamAfnsDao;
import com.gof.dao.IrParamModelDao;
import com.gof.dao.IrParamSwDao;
import com.gof.dao.IrVolSwpnDao;
import com.gof.dao.RcCorpPdDao;
import com.gof.entity.IrDcntRate;
import com.gof.entity.IrDcntRateBiz;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrDcntSceStoBiz;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwCalc;
import com.gof.entity.StdAsstIrSceSto;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrParamSwUsr;
import com.gof.entity.IrSprdAfnsBiz;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.entity.IrSprdLp;
import com.gof.entity.IrSprdLpBiz;
import com.gof.entity.IrValidParamHw;
import com.gof.entity.IrValidRnd;
import com.gof.entity.IrValidSceSto;
import com.gof.entity.IrParamHwRnd;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveSpotWeek;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.CoEsgMeta;
import com.gof.entity.CoJobInfo;
import com.gof.entity.CoJobList;
import com.gof.entity.IrVolSwpn;
import com.gof.entity.RcCorpPd;
import com.gof.entity.RcCorpPdBiz;
import com.gof.entity.RcCorpTm;
import com.gof.enums.EApplBizDv;
import com.gof.enums.EBaseTenor;
import com.gof.enums.EDetSce;
import com.gof.enums.EIrModel;
import com.gof.enums.EJob;
import com.gof.enums.ERunArgument;
import com.gof.interfaces.IRateInput;
import com.gof.util.AesCrypto;
import com.gof.util.EsgConstant;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	
	private static Map<ERunArgument, String> argInputMap  = new LinkedHashMap<>();
	private static Map<String, String>       argInDBMap   = new LinkedHashMap<>();	
	private static List<String>              jobList      = new ArrayList<String>();

	private static Session   session;
	private static String    bssd;		
	
	private static int       projectionYear              = 120;                                            
	private static long      cnt                         = 0;	                 
	private static int       flushSize                   = 10000;
	private static int       logSize                     = 100000;	                                                     
		
	private static List<IrCurve>          irCurveList    = new ArrayList<IrCurve>();	
//	private static Map<String, IrParamSw> irCurveSwMap   = new TreeMap<String, IrParamSw>();
	private static List<IrParamSw>        paramSwList    = new ArrayList<IrParamSw>();
	private static Map<String,IrParamSw>  commIrParamSw   = new TreeMap<String,IrParamSw>();
	private static Map<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>>  bizIrParamSw    = new TreeMap<EApplBizDv, Map<IrCurve,Map<EDetSce, IrParamSw>>>() ;
	
//	private static List<IrParamModel>        modelMstList  = new ArrayList<IrParamModel>();
//	private static Map<String, IrParamModel> modelMstMap    = new TreeMap<String, IrParamModel>();

	private static double    hw1fInitAlpha               = 0.05;
	private static double    hw1fInitSigma               = 0.007;		
	private static double    targetDuration              = 3.0;
	private static int[]     hwAlphaPieceSplit           = new int[] {10};
	private static int[]     hwAlphaPieceNonSplit        = new int[] {20};
	private static int[]     hwSigmaPiece                = new int[] {1, 2, 3, 5, 7, 10};
	private static double    significanceLevel           = 0.05;	
//	private static int       cirAvgMonth                 = 36;	
//	private static int       cirPrjYear                  = 30;
	private static String    iRateHisStBaseDate          = "20100101";	
	

	public static void main(String[] args) {		

// ****************************************************************** Run Argument &  Common Data Setting ********************************
		
		init(args);		// Input Data Setting
		
// ****************************************************************** Pre-Process and Input Setting      ********************************

		job110();       // Job 110: Set Smith-Wilson Attribute
		job120();       // Job 120: Set Swaption Volatility
		job130();       // Job 130: Set YTM TermStructure : ( IR_CURVE_YTM_USR -> IR_CURVE_YTM )
		
		job150();       // Job 110: YTM to SPOT by Smith-Wilson Method ( IR_CURVE_YTM -> IR_CURVE_SPOT )
//		job151();       // Job 111: YTM to SPOT by Smith-Wilson Method Migration
		
// ****************************************************************** Deterministic Scenario with LP      ********************************		

		job210();       // Job 210: AFNS Weekly Input TermStructure Setup
		job220();       // Job 220: AFNS Shock Spread		
		job230();       // Job 230: Biz Applied AFNS Shock Spread
		
		job240();		// job 240: Set Liquidity Premium
		job250();		// job 250: Biz Applied Liquidity Premium
		
		job260();		// job 260: BottomUp Risk Free TermStructure with Liquidity Premium
		job270();		// job 270: Interpolated TermStructure by SW		
		job280();		// job 280: Biz Applied TermStructure by SW		
		
// ****************************************************************** Stochastic Scenario with LP         ********************************	
		
		job310();		// Job 310: Calibration and Validation of HW1F Model Parameter
		job320();		// job 320: Calibration Stress Test of HW1F Model Parameter		
		job330();       // job 330: Biz Applied HW1F Model Parameter
	
		job340();       // job 340: HW1F Discount Rate Scenario of Biz TermStructure
		job350();       // job 350: HW1F Bond Yield Scenario of Biz TermStructure
		job360();       // job 360: Validation for Random number of HW1F Scenario
		job370();       // job 370: Validation for Market Consistency of HW1F Scenario		
		
// ******************************************************************AFNS Shock Spread	   ********************************		
		
		job710(); 
		job720();
		job730(); 
		job740();  
		
// ****************************************************************** RC Job                              ********************************

		job810();		// Job 810: Set Transition Matrix
		job820();		// Job 820: Corporate PD from Transition Matrix	

// ****************************************************************** End Job                             ********************************
		
		hold();
		HibernateUtil.shutdown();
		System.exit(0);
	}
	

	private static void init(String[] args) {
		
		for (String aa : args) {			
			for (ERunArgument bb : ERunArgument.values()) {
				if (aa.split("=")[0].toLowerCase().contains(bb.name())) {
					argInputMap.put(bb, aa.split("=")[1]);
					break;
				}
			}
		}		
		
		try {
			if(argInputMap.containsKey(ERunArgument.encrypt) && argInputMap.get(ERunArgument.encrypt).toString().trim().toUpperCase().equals("Y")) {
				
				Scanner sc = new Scanner(System.in);			
				
				System.out.println("=======================================================");
				System.out.println("==========  [GESG Text Encryption Process]   ==========");
				System.out.println("=======================================================");
				System.out.print("Enter Plain Text To Encrypt: ");
				String plainPwd = sc.next();
				
				AesCrypto aes128 = new AesCrypto();		
				String encryptPwd = aes128.AesCBCEncode(plainPwd);				
//				log.info("Aes128 Encode: {}, Decode: {}", plainPwd, encryptPwd);
				
				System.out.println("");
				System.out.println("Encrypted Text:  " + encryptPwd);				
				System.out.println("=======================================================");
				
				sc.close();
				System.exit(0);
			}
		}
		catch (Exception e) {
			log.error("Argument Error: [-Dencrypt] in Text Encryption Process");
			System.exit(0);
		}
		
		
		for (String aa : args) log.info("Input Arguments : {}", aa);
//		log.info("argInputMap: {}", argInputMap);
		
		try {
			bssd = argInputMap.get(ERunArgument.time).replace("-", "").replace("/", "").substring(0, 6);
		} catch (Exception e) {
			log.error("Argument Error: [-Dtime]" );
			System.exit(0);
		}
		
		
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(argInputMap.get(ERunArgument.properties));
			properties.load(new BufferedInputStream(fis));			
			EsgConstant.TABLE_SCHEMA = properties.getOrDefault("schema", "PUBLIC").toString().trim().toUpperCase();
			
			if(properties.containsKey("encrypt") && properties.getProperty("encrypt").toString().trim().toUpperCase().equals("Y")) {
				
				AesCrypto aes128 = new AesCrypto();
				String decodePwd = aes128.AesCBCDecode(properties.getProperty("password"));
				properties.setProperty("password", decodePwd);
			}
			
		} catch (Exception e) {
			log.error("Error in Properties Loading : {}", e);
			System.exit(0);
		}
		
//		session = HibernateUtil.getSessionFactory(properties).openSession();
		session = HibernateUtil.getSessionFactory().openSession();
		log.info("End of session call");		
		
		
		argInDBMap = CoEsgMetaDao.getCoEsgMeta("PROPERTIES").stream().collect(toMap(s->s.getParamKey(), s->s.getParamValue()));		
		log.info("argInDBMap: {}", argInDBMap);		
		
		try {			
			if(argInputMap.containsKey(ERunArgument.job) && argInputMap.get(ERunArgument.job).toUpperCase().equals("CIR")) {				
				CoJobListDao.getCoJobList("CIR").stream().forEach(s -> log.info("JOB LIST: {}, {}", s.getJobNm().trim(), s.getJobName().trim()));
				jobList = CoJobListDao.getCoJobList("CIR").stream().map(s -> s.getJobNm().trim()).collect(Collectors.toList());
			}				
			else {
//				Map<String, String> jobListMap = CoJobListDao.getCoJobList().stream().collect(Collectors.toMap(CoJobList::getJobId, CoJobList::getJobNm, (k, v) -> k, TreeMap::new));			
				CoJobListDao.getCoJobList().stream().forEach(s -> log.info("JOB LIST: {}, {}", s.getJobNm().trim(), s.getJobName().trim()));
				jobList    = CoJobListDao.getCoJobList().stream().map(s -> s.getJobNm().trim()).collect(Collectors.toList());				
			}
			
			hw1fInitAlpha                = Double.parseDouble(argInDBMap.getOrDefault("HW1F_ALPHA_INIT", "0.05" ).toString());
			hw1fInitSigma                = Double.parseDouble(argInDBMap.getOrDefault("HW1F_SIGMA_INIT", "0.007").toString());			
		
			String hwAlphaPieceStr       = argInDBMap.getOrDefault("HW1F_ALPHA_PIECE", "10").toString();
			String hwSigmaPieceStr       = argInDBMap.getOrDefault("HW1F_SIGMA_PIECE", "1, 2, 3, 5, 7, 10").toString();
			hwAlphaPieceSplit            = Arrays.stream(hwAlphaPieceStr.split(",")).map(s -> s.trim()).map(Integer::parseInt).mapToInt(Integer::intValue).toArray();
			hwSigmaPiece                 = Arrays.stream(hwSigmaPieceStr.split(",")).map(s -> s.trim()).map(Integer::parseInt).mapToInt(Integer::intValue).toArray();				

			iRateHisStBaseDate           = argInDBMap.getOrDefault("IR_HIS_START_DATE", "20100101").toString().trim().toUpperCase();
			projectionYear 	             = Integer.parseInt(argInDBMap.getOrDefault("PROJECTION_YEAR", "120").toString());

			targetDuration	             = Double.parseDouble(argInDBMap.getOrDefault("BOND_YIELD_TGT_DURATION", "3.0").toString());		
			significanceLevel            = Double.parseDouble(argInDBMap.getOrDefault("SIGNIFICANCE_LEVEL", "0.05").toString());
			
//			cirAvgMonth                  = Integer.parseInt(argInDBMap.getOrDefault("CIR_AVG_MONTH", "36").toString());
//			cirPrjYear                   = Integer.parseInt(argInDBMap.getOrDefault("CIR_PROJECTION_YEAR", "30").toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Check Input Setting in [{}] or [{}] table", Process.toPhysicalName(CoJobList.class.getSimpleName()), Process.toPhysicalName(CoEsgMeta.class.getSimpleName()));
			System.exit(0);
		}
		
		jobList.clear();
//		jobList.add("120");
//		jobList.add("130");		
//		jobList.add("150");
//		
//		jobList.add("210");
//		jobList.add("220");
//		jobList.add("230");
//		jobList.add("240");
//		jobList.add("250");
//		jobList.add("260");
		jobList.add("270");
		jobList.add("280");
//		jobList.add("310");
//		jobList.add("320");
//		jobList.add("330");
//		jobList.add("340");   //
//		jobList.add("350");
//		jobList.add("360");
//		jobList.add("370");
		
//		jobList.add("710");
//		jobList.add("720");
//		jobList.add("730"); 
//		jobList.add("740");
//		
		
	}		
	
	//TODO: Start from E_IR_PARAM_SW_USR
	private static void job110() {
//		if(jobList.contains("110")) {
		if(true) {	
			session.beginTransaction();		
			CoJobInfo jobLog = startJogLog(EJob.ESG110);
			try {
				
				// 2023.04.10 add 통째로 들고 다니기 
				irCurveList   = IrCurveDao.getIrCurveList();
				List<String> irCurveNmList  = irCurveList.stream().map(IrCurve::getIrCurveNm).collect(Collectors.toList());
				
				if(irCurveList.isEmpty()) {
					log.error("No Active Interest Rate Curve in [{}] Table", Process.toPhysicalName(IrCurve.class.getSimpleName()));
					throw new Exception();
				}
				else {  
					log.info("Active Interest Rate Curve: [{}]", irCurveNmList);
				}			
				
				int delNum = session.createQuery("delete IrParamSw a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrParamSw.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);				

				List<IrParamSwUsr> paramSwUsrList = IrParamSwDao.getIrParamSwUsrList(bssd, irCurveNmList);				
//				paramSwUsrList.forEach(s -> log.info("paramSwUsrList: {}", s));
				log.info("Active PARAM_SW_USR SIZE in [{}]: [{}]", bssd, paramSwUsrList.size());
				
				
				// 23.04.10 irCurve처럼 static 으로 정의하기 
				paramSwList  = IrParamSwDao.getIrParamSwList(bssd);
				
				
				// 작업 전 기존 설정과 건수가 다른지 check 
				if(paramSwList.size() != paramSwUsrList.size()) {
					log.warn("Check Smith-Wilson Attribute in [{}] Table for [{}]", Process.toPhysicalName(IrParamSwUsr.class.getSimpleName()), bssd);
				}

				// save
				paramSwList.stream().forEach(s->s.setModifiedBy("GESG"+"job110"));
				paramSwList.stream().forEach(s->s.setUpdateDate(LocalDateTime.now())); 
				paramSwList.stream().forEach(s -> session.save(s));
				log.info("[{}] has been Created from [{}] in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrParamSw.class.getSimpleName()), Process.toPhysicalName(IrParamSwUsr.class.getSimpleName()), jobLog.getJobId(), bssd, paramSwList.size());

				// irCurve 별 시나리오번호(1) swMap : biz구분없이 공통적으로 할일은 irCurve 단위로 반복처리 
				commIrParamSw  = paramSwList.stream()
											.filter(s->s.getIrCurveSceNo().equals(EDetSce.SCE01))
											.collect(Collectors
											.toMap(IrParamSw::getIrCurveNm, Function.identity(), (a,b)->a ));

				// 비었을 때 => 종료 
				if(commIrParamSw.isEmpty()) {
					log.error("Check Smith-Wilson Attribute in [{}] Table for [{}]", Process.toPhysicalName(IrParamSw.class.getSimpleName()), bssd);
					throw new Exception();
				}

				
				// biz 구분 단위로, irCurve 단위로 (시나리오별 paramSw)을 그룹핑. 
				bizIrParamSw = paramSwList.stream()
				        .collect(Collectors.groupingBy(IrParamSw::getApplBizDv,
				                 Collectors.groupingBy(IrParamSw::getIrCurve,
				                        TreeMap::new,
				                        Collectors.toMap(IrParamSw::getIrCurveSceNo, Function.identity(), (k, v) -> k, TreeMap::new))));
				
				log.info("KICS {}", bizIrParamSw.get(EApplBizDv.KICS)) ;
				log.info("IFRS {}", bizIrParamSw.get(EApplBizDv.IFRS)) ;
				log.info("SAAS {}", bizIrParamSw.get(EApplBizDv.SAAS)) ;
				log.info("SAAS {}", bizIrParamSw.get(EApplBizDv.IBIZ)) ;

				// TODO : model 단위로 처리할 일이 있을까 model 단위로 프로세스를 구분하는것이 의미가 있을까?
//				modelMstList = IrParamModelDao.getParamModelList();
//				modelMstMap  = modelMstList.stream().collect(Collectors.toMap(IrParamModel::getIrCurveNm, Function.identity()));
				
								
				session.flush();
				session.clear();				
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
				
				session.saveOrUpdate(jobLog);
				session.getTransaction().commit();	
				System.exit(0);
			}
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}
	}
	
	
	private static void job120() {
		if(jobList.contains("120")) {
			session.beginTransaction();		
			CoJobInfo jobLog = startJogLog(EJob.ESG120);
			
			try {				
				for (IrCurve irCurve :irCurveList) {
						String irCurveNm = irCurve.getIrCurveNm();
						
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);						
						continue;
					}

					// delete 
					int delNum = session.createQuery("delete IrVolSwpn a where a.baseYymm = :param1 and a.irCurveNm = :param2")
										.setParameter("param1", bssd)				
										.setParameter("param2", irCurveNm).executeUpdate();
		
					log.info("[{}] has been Deleted in Job:[{}] [COUNT: {}]", Process.toPhysicalName(IrVolSwpn.class.getSimpleName()), jobLog.getJobId(), delNum);
					
					// biz : 컬럼으로 구분된 tenor별 변동성을 unpivot  
					List<IrVolSwpn> swpnVol = Esg120_SetVolSwpn.createVolSwpnFromUsr(bssd, irCurve);
					
					//save
					swpnVol.stream().forEach(s -> session.save(s));
				}
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
	
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	/** <p> insert into IR_CURVE_YTM 
	 * <p> - 엔진에서 사용할 ytm정보를 저장함. ( 금리모형에 따라 달리질 이유가 없음. 금리코드에 따라 looping )</br> 
	 * - 동일한 월의 ytm이 from 테이블에 둘 다 있을 경우 dup발생. </br> 
	 * from 01 : IR_CURVE_YTM_USR_HIS : KOFIA BOND 와 동일한 layout, 만기를 컬럼으로 구분함. 입력단위 (% ; toReal 0.01)  </br>
	 * from 02 : IR_CURVE_YTM_USR : 만기구분을 코드값으로 구분함. 입력단위 (number ; toReal 1) </br>
	 * */
//	TODO : 사전 setting check / delete / BIZ / insert 분리 필요 
	private static void job130() {
		if(jobList.contains("130")) {
			session.beginTransaction();		
			CoJobInfo jobLog = startJogLog(EJob.ESG130);
			
			try {				
			    for (IrCurve irCurve :irCurveList) {
			    	
					if(!commIrParamSw.containsKey(irCurve.getIrCurveNm())) { 
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurve.getIrCurveNm(), bssd);						
						continue;
					}

					int delNum = session.createQuery("delete IrCurveYtm a where a.baseDate like :param1 and a.irCurveNm = :param2")
										.setParameter("param1", bssd+"%")				
										.setParameter("param2", irCurve.getIrCurveNm()).executeUpdate();
		
					log.info("[{}] has been Deleted in Job:[{}] [COUNT: {}]", Process.toPhysicalName(IrCurveYtm.class.getSimpleName()), jobLog.getJobId(), delNum);
					
					List<IRateInput> ytmUsrHis = Esg130_SetYtm.createYtmFromUsrHis(bssd, irCurve);
					ytmUsrHis.stream().forEach(s -> session.save(s));
					
					Stream<IRateInput> ytmUsr    = Esg130_SetYtm.createYtmFromUsr(bssd, irCurve);
					ytmUsr.forEach(s -> session.save(s)); 
			    	
			    }
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
	
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	/** <p> insert into IR_CURVE_SPOT  
	 * <p> - YTM to SPOT by Smith-Wilson 방법론</br>
	 * - SW 방법론을 적용하기 때문에 ir_Curve 단위로 IR_PARAM_SW 에 설정여부를 체크함.  </br> 
	 * from : IR_CURVE_YTM  </br>
	 * @See SmithWilsonKicsBts
	 * */
	private static void job150() {
		if(jobList.contains("150")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG150);			
			
			try {
			    for (IrCurve irCurve :irCurveList) {
			    	
			    	String irCurveNm = irCurve.getIrCurveNm() ;
			    	IrParamSw irparamSw = commIrParamSw.get(irCurveNm) ;
			    	
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);						
						continue;
					}					

					// 기준일자의 이전 작업 결과 delete 
					IrCurveSpotDao.deleteIrCurveSpotMonth(bssd, irCurveNm);
					
					// YTM 가져오기 
					List<IrCurveYtm> ytmRstList = IrCurveYtmDao.getIrCurveYtmMonth(bssd, irCurveNm);
					
					// input ytm 적재여부 확인 
					if(ytmRstList.size()==0) {
						log.warn("No Historical YTM Data exist for [{}, {}]", bssd, irCurveNm);
						continue;
					}					
					
					TreeMap<String, List<IRateInput>> ytmRstMap = new TreeMap<String, List<IRateInput>>();
					ytmRstMap = ytmRstList.stream().collect(Collectors.groupingBy(s -> s.getBaseDate(), TreeMap::new, Collectors.toList()));					
					
					// 생성한 ytm 트리맵 기준일자별로 루프 
					for(Map.Entry<String, List<IRateInput>> ytmRst : ytmRstMap.entrySet()) {						
						
//						log.info("ytmRst: {}, {}, {}, {}, {}, {}", ytmRst.getKey(), irCrv.getKey(), irCurveSwMap.get(irCrv.getKey()).getSwAlphaYtm(), irCurveSwMap.get(irCrv.getKey()).getFreq(), ytmRst.getValue(), ytmRst);
						
						List<IrCurveSpot> rst = new ArrayList<IrCurveSpot>();
						
						// biz로직 :ytm -> spot 
						rst = Esg150_YtmToSpotSw.createIrCurveSpot(	ytmRst.getValue(), irparamSw ) ;
						
						// ir curve에 대한 정보 추가 setter (fk)
						rst.forEach(s -> s.setIrCurve(irCurve));
					
						if(rst.isEmpty()) throw new Exception();
						
						// 저장 
						rst.forEach(s -> session.save(s));
					}
					
					// 
					session.flush();
					session.clear();
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();		
		}
	}

	
	/** <p> insert into IR_CURVE_SPOT_WEEK  </br> 
	 * <p>- Spot rate 에서 영업일 구분 후 week 테이블에 적재. 모수추정용으로 충분성 확보 필요.  </br> 
	 * from : IR_CURVE_SPOT  </br>
	 * @See convertToWeek
	 * */
	private static void job210() {
		if(jobList.contains("210")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG210);			

			try {
				for (IrCurve irCurve :irCurveList) {
				    	
				    String irCurveNm = irCurve.getIrCurveNm() ;
				    IrParamSw irparamSw = commIrParamSw.get(irCurveNm) ;
//				    Integer llp  = Math.min(irparamSw.getLlp(), 20) ;
				    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);						
						continue;
					}

					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm , Math.min(irparamSw.getLlp(), 20));
					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);				

					if(tenorList.isEmpty()) {
						log.warn("No Ir Curve Data [{}] at [{}] in Table [{}]", irCurveNm, bssd, Process.toPhysicalName(IrCurveSpot.class.getSimpleName()));						
						continue;
					}					
					
					int delNum = session.createQuery("delete IrCurveSpotWeek a where a.baseDate >= :param1 and a.baseDate <= :param2 and a.irCurveNm = :param3")
										.setParameter("param1", iRateHisStBaseDate)
										.setParameter("param2", bssd+31)
							            .setParameter("param3", irCurveNm).executeUpdate();				
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrCurveSpotWeek.class.getSimpleName()), jobLog.getJobId(), irCurveNm, delNum);

					List<IrCurveSpotWeek> spotWeek = Esg210_SpotWeek.setupIrCurveSpotWeek(bssd, iRateHisStBaseDate, irCurve, tenorList);
					spotWeek.stream().forEach(s -> s.setUpdateDate(LocalDateTime.now()));
				    spotWeek.stream().forEach(s -> s.setModifiedBy("ESG210")) ;
					spotWeek.stream().forEach(s -> session.save(s));
					
					session.flush();
					session.clear();					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}		
	}		

	/** <p> insert into IR_SPRD_AFNS_CALC  </br> 
	 * <p>- 자체 데이터를 기반으로 금리충격시나리오 생성   </br> 
	 * from : IR_CURVE_SPOT_WEEK (충분해야 함)  </br>
	 * @See convertToWeek
	 * */
	private static void job220() {
		if(jobList.contains("220")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG220);			

			EIrModel irModelNm     = EIrModel.AFNS;						
			int    weekDay         = Integer.valueOf((String) argInDBMap.getOrDefault("AFNS_WEEK_DAY"        , "5")); //금욜 
			
			List<IrParamModel> modelMstList = IrParamModelDao.getParamModelList(irModelNm);
			Map<String, IrParamModel> irModelMstMap = modelMstList.stream()
													.collect(Collectors.toMap(IrParamModel::getIrCurveNm, Function.identity()));
			
			log.info("IrParamModel: {}", irModelMstMap.toString());			
			
			try {
				for (IrCurve irCurve :irCurveList) {
				    	
					    String        irCurveNm  = irCurve.getIrCurveNm() ;
					    IrParamSw     irparamSw  = commIrParamSw.get(irCurveNm) ;
//					    Integer       llp        = Math.min(irparamSw.getLlp(), 20) ; // 이거 왜 NPE 발생 ?
					    IrParamModel  irModelMst = irModelMstMap.get(irCurveNm) ;
					
					    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					if(!irModelMstMap.containsKey(irCurveNm)) {
						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
						continue;
					}
					
					log.info("AFNS Shock Spread (Cont) for [{}({}, {})]", irCurveNm, irCurve.getIrCurveNm(), irCurve.getCurCd());
					
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));
					
					// 엑셀과 모수 추정에 사용하는 테너만 남기기
					tenorList.remove("M0003");tenorList.remove("M0006");tenorList.remove("M0009");tenorList.remove("M0018");tenorList.remove("M0030");tenorList.remove("M0048"); tenorList.remove("M0084"); tenorList.remove("M0180");  //FOR CHECK w/ FSS

					
					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;
					}

					int delNum1 = session.createQuery("delete IrParamAfnsCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
		                     			 .setParameter("param1", bssd) 
		                     			 .setParameter("param2", irModelNm)
		                     			 .setParameter("param3", irCurveNm)
		                     			 .executeUpdate();
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamAfnsCalc.class.getSimpleName()), jobLog.getJobId(), irModelNm, irCurveNm, delNum1);
					
					int delNum2 = session.createQuery("delete IrSprdAfnsCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
					                     .setParameter("param1", bssd) 
								  		 .setParameter("param2", irModelNm)
										 .setParameter("param3", irCurveNm)
										 .executeUpdate();					

					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()), jobLog.getJobId(), irModelNm, irCurveNm, delNum2);
					
					List<IrCurveSpotWeek> weekHisList    = IrCurveSpotWeekDao.getIrCurveSpotWeekHis(bssd, iRateHisStBaseDate, irCurve, tenorList, weekDay, false);
					List<IrCurveSpotWeek> weekHisBizList = IrCurveSpotWeekDao.getIrCurveSpotWeekHis(bssd, iRateHisStBaseDate, irCurve, tenorList, weekDay, true);
					log.info("weekHisList: [{}], [TOTAL: {}, BIZDAY: {}], [from {} to {}, weekDay:{}]", irCurveNm, weekHisList.size(), weekHisBizList.size(), iRateHisStBaseDate.substring(0,6), bssd, weekDay);			

					//for ensuring enough input size
					if(weekHisList.size() < 1000) {
						log.warn("Weekly SpotRate Data is not Enough [ID: {}, SIZE: {}] for [{}]", irCurveNm, weekHisList.size(), bssd);
						continue;
					}					

					List<IRateInput> curveHisList = weekHisList.stream().map(s->s.convertToHis()).collect(toList());
//					log.info("{}", curveHisList);
					
					//Any curveBaseList result in same parameters and spreads.
					List<IRateInput> curveBaseList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm, tenorList);					
					
					if(curveBaseList.size()==0) {
						log.warn("No IR Curve Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					
					Map<String, List<?>> irShockSenario = new TreeMap<String, List<?>>();
					irShockSenario = Esg220_ShkSprdAfns.createAfnsShockScenario(FinUtils.toEndOfMonth(bssd)
																			  , curveHisList
																			  , curveBaseList
																			  , irModelMst  // add 
																			  , irparamSw   // add 
																			  , argInDBMap  // add 
																			  );	
											
					for(Map.Entry<String, List<?>> rslt : irShockSenario.entrySet()) {						
						rslt.getValue().forEach(s -> session.save(s));

						session.flush();
						session.clear();
					}					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	/** <p> insert into IR_SPRD_AFNS_BIZ  </br> 
		 * <p>- 금감원 제공 금리충격시나리오 적용   </br> 
		 * - 자체 산출데이터보다 우선 적용   </br> 
		 * from : IR_SPRD_AFNS_USR  </br>
		 * @See createBizAfnsShockScenario
		 * */
		private static void job230() {
			if(jobList.contains("230")) {
				session.beginTransaction();
				CoJobInfo jobLog = startJogLog(EJob.ESG230);			
		
//				String irModelNm = argInDBMap.getOrDefault("AFNS_MODE", "AFNS").trim().toUpperCase();						
				EIrModel irModelNm = EIrModel.AFNS;
				
				try {
					for (IrCurve irCurve :irCurveList) {						
					    String        irCurveNm  = irCurve.getIrCurveNm() ;
					    
						if(!commIrParamSw.containsKey(irCurveNm)) {
							log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
							continue;
						}
						
						int delNum = session.createQuery("delete IrSprdAfnsBiz a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
			                     			.setParameter("param1", bssd) 
			                     			.setParameter("param2", irModelNm)
			                     			.setParameter("param3", irCurveNm)
			                     			.executeUpdate();					
		
						log.info("[{}] has been Deleted in Job:[{}] [IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrSprdAfnsBiz.class.getSimpleName()), jobLog.getJobId(), irCurveNm, delNum);
						
						List<IrSprdAfnsBiz> afnsBizList = Esg230_BizSprdAfns.createBizAfnsShockScenario(bssd, irModelNm, irCurveNm);
						afnsBizList.stream().forEach(s -> session.save(s));					
					}
					completeJob("SUCCESS", jobLog);
					
				} catch (Exception e) {
					log.error("ERROR: {}", e);
					completeJob("ERROR", jobLog);
				}			
				session.saveOrUpdate(jobLog);
				session.getTransaction().commit();
			}
		}


	/** <p> insert into IR_SPRD_LP  </br> 
	 * <p>- 적용할 유동성 프리미엄 가져오기   </br> 
	 * - 적용 요건 및 방식에 따라 3가지 방식으로 가져옴 (회사별 적용 요건 확인!) </br> 
	 * from : IR_PARAM_SW / IR_SPRD_CURVE / IR_SPRD_LP_USR </br>
	 * */
	private static void job240() {
		if(jobList.contains("240")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG240);
			
			try {
				
				int delNum = session.createQuery("delete IrSprdLp a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrSprdLp.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);
				
				String lpCurveId = argInDBMap.getOrDefault("LP_CURVE_ID", "5010110");
				
				for(EApplBizDv biz : EApplBizDv.getUseBizList()) {
					
					List<IrSprdLp> bizSpread1 = Esg240_LpSprd.setLpFromSwMap(bssd, biz, bizIrParamSw.get(biz)) ;
					bizSpread1.stream().forEach(s -> session.save(s));
					
					List<IrSprdLp> bizSpread2 = Esg240_LpSprd.setLpFromCrdSprd(bssd, biz, bizIrParamSw.get(biz), lpCurveId);
					bizSpread2.stream().forEach(s -> session.save(s));
					
					List<IrSprdLp> bizSpread3 = Esg240_LpSprd.setLpFromUsr(bssd, biz, bizIrParamSw.get(biz));
					bizSpread3.stream().forEach(s -> session.save(s));
				}
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);					
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}			
	}	
		

	/** <p> insert into IR_SPRD_LP_BIZ  </br> 
	 * <p>- 적용할 유동성 프리미엄 적재    </br> 
	 * - 최종 적용 기준 : IR_PARAM_SW 의 DCNT_APPL_MODEL_CD (할인율적용모형코드) 설정에 따라 결정함 </br> 
	 * from : IR_SPRD_LP </br>
	 * */
	private static void job250() {
		if(jobList.contains("250")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG250);
			
			try {
				int delNum = session.createQuery("delete IrSprdLpBiz a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrSprdLpBiz.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);
				
				for(EApplBizDv biz : EApplBizDv.getUseBizList()) {
					
					List<IrSprdLpBiz> bizSpread = Esg250_BizLpSprd.setLpSprdBiz(bssd, biz, bizIrParamSw.get(biz));
					bizSpread.stream().forEach(s -> session.save(s));
				}
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);					
			}				
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}			
	}	


	private static void job260() {
		if(jobList.contains("260")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG260);
			
			try {
				int delNum = session.createQuery("delete IrDcntRateBu a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();				
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrDcntRateBu.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);
				
				EIrModel irModelNm = EIrModel.AFNS;

				for(EApplBizDv biz : EApplBizDv.getUseBizList()) {
					List<IrDcntRateBu> bizDcntRateBu = Esg261_IrDcntRateBu_Ytm.setIrDcntRateBu(bssd, irModelNm,  biz, bizIrParamSw.get(biz));				
					bizDcntRateBu.stream().forEach(s -> session.save(s));
				}
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);					
			}				
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}			
	}	


	private static void job270() {
		if(jobList.contains("270")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG270);
			
			try {
				int delNum = session.createQuery("delete IrDcntRate a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();				
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrDcntRate.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);				
				
				
				for(EApplBizDv biz : EApplBizDv.getUseBizList()) {
					
					List<IrDcntRate> bizDcntRate = Esg270_IrDcntRate.createIrDcntRate(bssd,  biz, bizIrParamSw.get(biz), projectionYear);
					bizDcntRate.stream().forEach(s -> session.save(s));
				}
				
				session.flush();
				session.clear();				
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
//				log.error("Check User Defined Dcnt Rate Data in [{}] Table", Process.toPhysicalName(IrDcntRateUsr.class.getSimpleName()));
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);				
			}				
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}		
	}	
	

	private static void job280() {
		if(jobList.contains("280")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG280);
			
			try {
				int delNum = session.createQuery("delete IrDcntRateBiz a where a.baseYymm=:param").setParameter("param", bssd).executeUpdate();				
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(IrDcntRateBiz.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);								

				for (EApplBizDv applBiz : EApplBizDv.getUseBizList()) {
					IrDcntRateDao.getIrDcntRateBizAdjSpotList (bssd, applBiz).forEach(s -> session.save(s));
					IrDcntRateDao.getIrDcntRateBizBaseSpotList(bssd, applBiz).forEach(s -> session.save(s));
				}
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);					
			}				
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();			
		}		
	}	
	
 
	private static void job310() {
		if(jobList.contains("310")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG310);
			
			EIrModel irModelNm    = EIrModel.HW1F ;		
			EIrModel irModelNmNsp = EIrModel.valueOf( irModelNm + "_NSP");
			
			EIrModel irModelNmSp  = EIrModel.valueOf( irModelNm + "_SP");
			
//          1개의 job에서느 여러개의 금리모델을 사용하는 경우가 있을까? 모델정보는 단일객체로 받기 때문에 리스트로 정의하지 않아도 됨.  
//			List<IrParamModel> modelMst = IrParamModelDao.getParamModelList(irModelNm);
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());
			
			try {
				
				for (IrCurve irCurve :irCurveList) {
				    	
				    String irCurveNm = irCurve.getIrCurveNm() ;
				    IrParamSw irparamSw = commIrParamSw.get(irCurveNm) ;
//				    Integer llp  = Math.min(irparamSw.getLlp(), 20) ;
				
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);					
						continue;
					}
					
//					if(!modelMstMap.containsKey(irCurveNm)) {
//						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
//						continue;
//					}
					
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));
					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);					
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;
					}
					
					int delNum1 = session.createQuery("delete IrParamHwCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm =:param3")
								 		 .setParameter("param1", bssd) 
		                     			 .setParameter("param2", irModelNmNsp)
		                     			 .setParameter("param3", irCurveNm)
		                     			 .executeUpdate();
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamHwCalc.class.getSimpleName()), jobLog.getJobId(), irModelNmNsp, irCurveNm, delNum1);

					
					int delNum2 = session.createQuery("delete IrParamHwCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
								 		 .setParameter("param1", bssd) 
			                			 .setParameter("param2", irModelNmSp)
			                			 .setParameter("param3", irCurveNm)
			                			 .executeUpdate();		
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamHwCalc.class.getSimpleName()), jobLog.getJobId(), irModelNmSp , irCurveNm, delNum2);					

					
					int delNum3 = session.createQuery("delete IrValidParamHw a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
										 .setParameter("param1", bssd) 
										 .setParameter("param2", irModelNmNsp)
										 .setParameter("param3", irCurveNm)
										 .executeUpdate();
		
					log.info("[{}] has been Deleted in Job:[{}] [IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrValidParamHw.class.getSimpleName()), jobLog.getJobId(), irCurveNm, delNum3);
					
					List<IRateInput> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm, tenorList);
					
					log.info("SPOT RATE: [ID: {}], [SIZE: {}]", irCurveNm, spotList.size());					
					if(spotList.size()==0) {
						log.warn("No IR Curve Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}

					List<IrVolSwpn> swpnVolList = IrVolSwpnDao.getSwpnVol(bssd, irCurveNm);
					
					log.info("SWAPNTION VOL: [ID: {}], [SIZE: {}]", irCurveNm, swpnVolList.size());
					if(swpnVolList.size()==0 || swpnVolList.size() != 36) {
						log.warn("Check SWAPTION VOL Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}

					Integer freq = commIrParamSw.get(irCurveNm).getFreq();				
//					double errTol = modelMst.getItrTol();
//					log.info("freq: {}, errTol: {}", freq, errTol);
					log.info("freq: {}", freq);
					
					double[] hwInitParam  = new double[] {hw1fInitAlpha, hw1fInitAlpha, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma};
//					double[] hwInitParam  = new double[] {0.03, 0.06, 0.007, 0.006, 0.005, 0.004, 0.005, 0.006};
					
					Map<String, List<?>> irParamHw1fNonSplitMap = new TreeMap<String, List<?>>();
					irParamHw1fNonSplitMap = Esg310_ParamHw1f.createParamHw1fNonSplitMap(bssd, irModelNmNsp, modelMst, spotList, swpnVolList, hwInitParam,freq, hwAlphaPieceNonSplit, hwSigmaPiece);
				
					for(Map.Entry<String, List<?>> rslt : irParamHw1fNonSplitMap.entrySet()) {												
//						if(rslt.getKey().equals("VALID")) rslt.getValue().forEach(s -> session.save(s));
						rslt.getValue().forEach(s -> session.save(s));
						session.flush();
						session.clear();
					}					
					
					Map<String, List<?>> irParamHw1fSplitMap = new TreeMap<String, List<?>>();
					irParamHw1fSplitMap = Esg310_ParamHw1f.createParamHw1fSplitMap(bssd, irModelNmSp, modelMst, spotList, swpnVolList, hwInitParam, freq, hwAlphaPieceSplit, hwSigmaPiece);
				
					for(Map.Entry<String, List<?>> rslt : irParamHw1fSplitMap.entrySet()) {
						rslt.getValue().forEach(s -> session.save(s));
						session.flush();
						session.clear();
					}					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);				
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}			
	}	
	
	
	private static void job320() {		
		if(jobList.contains("320")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG320);
			
			EIrModel irModelNm   = EIrModel.HW1F ;
			EIrModel irModelNmNsp = EIrModel.valueOf(irModelNm + "_NSP");
			
//			List<IrParamModel> modelMst = IrParamModelDao.getParamModelList(irModelNm);
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());
			
			try {
				for (IrCurve irCurve :irCurveList) {
			    	
				    String irCurveNm = irCurve.getIrCurveNm() ;
				    IrParamSw irparamSw = commIrParamSw.get(irCurveNm) ;
//				    Integer llp  = Math.min(irparamSw.getLlp(), 20) ;
				    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);					
						continue;
					}				
					
//					if(!modelMstMap.containsKey(irCurveNm)) {
//						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
//						continue;
//					}					
										
//					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCrv.getKey(), irCurveSwMap.get(irCrv.getKey()).getLlp());
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));
					
					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);					
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;
					}
					
//					int delNum = session.createQuery("delete IrParamHwCalc a where baseYymm=:param1 and a.irModelNm = :param2 and a.irCurveNm =:param3 and a.modifiedBy=:param4")
					int delNum = session.createQuery("delete IrParamHwCalc a where baseYymm=:param1 and a.irCurveNm =:param3 and a.modifiedBy=:param4")
								 		.setParameter("param1", bssd) 
//		                     			.setParameter("param2", irModelNmNsp)
		                     			.setParameter("param3", irCurveNm)
		                     			.setParameter("param4", jobLog.getJobId())
		                     			.executeUpdate();
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamHwCalc.class.getSimpleName()), jobLog.getJobId(), irModelNmNsp, irCurveNm, delNum);

					List<IRateInput> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm, tenorList);
					
					log.info("SPOT RATE: [ID: {}], [SIZE: {}]", irCurveNm, spotList.size());					
					if(spotList.size()==0) {
						log.warn("No IR Curve Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}

					List<IrVolSwpn> swpnVolList = IrVolSwpnDao.getSwpnVol(bssd, irCurveNm);
					
					log.info("SWAPNTION VOL: [ID: {}], [SIZE: {}]", irCurveNm, swpnVolList.size());
					if(swpnVolList.size()==0) {
						log.warn("No SWAPTION VOL Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}

					// 작업마다 default 를 다르게 정의하기도 할까 ?? 아님 다른 것과 동일하게 entity에서 읽어올 때 디폴트 처리를 하는것이 맞을까 ?
//					Integer freq = StringUtil.objectToPrimitive(irCurveSwMap.get(irCurveNm).getFreq(), 2);				
					Integer freq = commIrParamSw.get(irCurveNm).getFreq();
					double errTol = modelMst.getItrTol();
					log.info("freq: {}, errTol: {}", freq, errTol);
			
					//TODO: Initial Parameter(Sigma) Stability Test for SIGMA: [0.001, 0.010, 0.020, 0.030, 0.040, 0.050]
					double validSigma = 0.0;
					for(int i=0; i<6; i++) {
						
						if(i==0) validSigma = 0.001;
						if(i==1) validSigma = 0.010;
						if(i==2) validSigma = 0.020;
						if(i==3) validSigma = 0.030;
						if(i==4) validSigma = 0.040;
						if(i==5) validSigma = 0.050;
						
						double[] hwInitParamSigma  = new double[] {hw1fInitAlpha, hw1fInitAlpha, validSigma, validSigma, validSigma, validSigma, validSigma, validSigma};
						
						List<IrParamHwCalc> hwParamCalcValid = Esg320_ParamHw1fStressTest.createParamHw1fNonSplitMapValid(
								  bssd
//								, EIrModel.valueOf( irModelNmNsp + "_INIT_" + String.valueOf(validSigma))
								, EIrModel.valueOf( irModelNmNsp + "_INIT_" + String.valueOf(i)) //enum에 소수점을 넣을수가 없음 
								, modelMst
								, spotList
								, swpnVolList
								, hwInitParamSigma
								, freq
								, errTol
								, hwAlphaPieceNonSplit
								, hwSigmaPiece
								);
						hwParamCalcValid.forEach(s -> session.save(s));
					}
					

					//TODO: Market Data(Spot and Swaption Vol Stability Test for [Spot +1bp, Spot -1bp, Swaption Vol +1bp, Swaption Vol -1bp]
					double[] hwInitParamMkt = new double[] {hw1fInitAlpha, hw1fInitAlpha, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma, hw1fInitSigma};				
					
					//IrCurveSpot으로 가져와서 rate조정(setter) 후에 IRateInput로 형변환함 IRateInput는 getter밖에 없어서 값을 조정하지 못함ㅠ 
					List<IrCurveSpot> tmpUp = IrCurveSpotDao.getIrCurveSpot2(bssd, irCurveNm, tenorList);
					tmpUp.stream().forEach(s -> s.setSpotRate(s.getSpotRate() + 0.0001));
					List<IRateInput> spotListUp = tmpUp.stream().map(x -> (IRateInput)x).collect(Collectors.toList());
					
					List<IrParamHwCalc> hwParamCalcSpotUp = Esg320_ParamHw1fStressTest.createParamHw1fNonSplitMapValid(
							  bssd
							, EIrModel.valueOf( irModelNmNsp + "_SPOT_UP")
							, modelMst
//							, irCurveNm
							, spotListUp
							, swpnVolList
							, hwInitParamMkt
							, freq
							, errTol
							, hwAlphaPieceNonSplit
							, hwSigmaPiece)
							;
					hwParamCalcSpotUp.forEach(s -> session.save(s));
										
					
					//IrCurveSpot으로 가져와서 rate조정(setter) 후에 IRateInput로 형변환함 IRateInput는 getter밖에 없어서 값을 조정하지 못함ㅠ 
					List<IrCurveSpot> tmpDn = IrCurveSpotDao.getIrCurveSpot2(bssd, irCurveNm, tenorList);					
					tmpDn.stream().forEach(s -> s.setSpotRate(s.getSpotRate() - 0.0001));
					List<IRateInput> spotListDn = tmpDn.stream().map(x -> (IRateInput)x).collect(Collectors.toList());
					
					List<IrParamHwCalc> hwParamCalcSpotDn = Esg320_ParamHw1fStressTest.createParamHw1fNonSplitMapValid(
							  bssd
							, EIrModel.valueOf( irModelNmNsp + "_SPOT_DN")
							, modelMst
							, spotListDn
							, swpnVolList
							, hwInitParamMkt
							, freq
							, errTol
							, hwAlphaPieceNonSplit
							, hwSigmaPiece
							);
					hwParamCalcSpotDn.forEach(s -> session.save(s));
					
					
					List<IrVolSwpn> swpnVolListUp = IrVolSwpnDao.getSwpnVol(bssd, irCurveNm);
					swpnVolListUp.stream().forEach(s -> s.setVol(s.getVol() + 0.0001));
					List<IrParamHwCalc> hwParamCalcSwpnUp = Esg320_ParamHw1fStressTest.createParamHw1fNonSplitMapValid(
							  bssd
							, EIrModel.valueOf( irModelNmNsp + "_SWPN_UP")
							, modelMst
							, spotList
							, swpnVolListUp
							, hwInitParamMkt
							, freq
							, errTol
							, hwAlphaPieceNonSplit
							, hwSigmaPiece
							);
					hwParamCalcSwpnUp.forEach(s -> session.save(s));
					
					
					List<IrVolSwpn> swpnVolListDn = IrVolSwpnDao.getSwpnVol(bssd, irCurveNm);
					swpnVolListDn.stream().forEach(s -> s.setVol(s.getVol() - 0.0001));
					List<IrParamHwCalc> hwParamCalcSwpnDn = Esg320_ParamHw1fStressTest.createParamHw1fNonSplitMapValid(
							  bssd
							, EIrModel.valueOf( irModelNmNsp + "_SWPN_DN")
							, modelMst
							, spotList
							, swpnVolListDn
							, hwInitParamMkt
							, freq
							, errTol
							, hwAlphaPieceNonSplit
							, hwSigmaPiece
							);
					hwParamCalcSwpnDn.forEach(s -> session.save(s));
					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);				
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}		
	}
	
	
	private static void job330() {
		if(jobList.contains("330")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG330);
			
			EIrModel irModelNm   = EIrModel.HW1F ;
			
//			List<IrParamModel> modelMst = IrParamModelDao.getParamModelList(irModelNm);
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());
			
			try {
				for (IrCurve irCurve :irCurveList) {
						String irCurveNm = irCurve.getIrCurveNm() ;
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
						continue;
					}				
					
					int delNum = session.createQuery("delete IrParamHwBiz a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
										.setParameter("param1", bssd) 
		                     			.setParameter("param2", irModelNm)
		                     			.setParameter("param3", irCurveNm)
		                     			.executeUpdate();
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamHwBiz.class.getSimpleName()), jobLog.getJobId(), irCurveNm, delNum);	
					
					int hwAlphaAvgNum = -1 * Integer.parseInt(argInDBMap.getOrDefault("HW_ALPHA_AVG_NUM", "120").toString());
					int hwSigmaAvgNum = -1 * Integer.parseInt(argInDBMap.getOrDefault("HW_SIGMA_AVG_NUM", "120").toString());
					
					String hwAlphaAvgMatCd = argInDBMap.getOrDefault("HW_ALPHA_AVG_MAT_CD", "M0240").trim().toUpperCase();
					String hwSigmaAvgMatCd = argInDBMap.getOrDefault("HW_SIGMA_AVG_MAT_CD", "M0120").trim().toUpperCase();					
					
					for(EApplBizDv biz : EApplBizDv.getUseBizList()) {
						Esg330_BizParamHw1f.createBizHw1fParam(bssd, biz, modelMst, hwAlphaAvgNum, hwAlphaAvgMatCd, hwSigmaAvgNum, hwSigmaAvgMatCd).forEach(s -> session.save(s));					
					}
					session.flush();
					session.clear();					
				}				
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}				
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}	
	}	
	

	private static void job340() {
		if(jobList.contains("340")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG340);			
			
			EIrModel irModelNm   = EIrModel.HW1F ;
			
//			List<IrParamModel> modelMst = IrParamModelDao.getParamModelList(irModelNm);
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());			
						
			Map<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> totalSwMap = new LinkedHashMap<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>>();
			totalSwMap = bizIrParamSw ;

//			String query = " delete " + schema + ".E_IR_DCNT_SCE_STO_BIZ partition (PT_E" + bssd + ") " 
//						 + "  where BASE_YYMM=:param1 and IR_MODEL_NM=:param2 ";
//
//			String query2 = " delete " + schema + ".E_IR_PARAM_HW_RND partition (PT_E" + bssd + ") " 
//					  + "  where BASE_YYMM=:param1 and IR_MODEL_NM=:param2 ";
//
//			int delNum = session.createNativeQuery(query)
//								.setParameter("param1", bssd) 
//								.setParameter("param2", irModelNm)
//								.executeUpdate();			
			
			try {				
				int delNum1 = session.createQuery("delete IrDcntSceStoBiz a where a.baseYymm=:param1 and a.irModelNm=:param2")						
							 		 .setParameter("param1", bssd) 
									 .setParameter("param2", irModelNm)
									 .executeUpdate();
				
				log.info("[{}] has been Deleted in Job:[{}] [COUNT: {}]", Process.toPhysicalName(IrDcntSceStoBiz.class.getSimpleName()), jobLog.getJobId(), delNum1);

				int delNum2 = session.createQuery("delete IrParamHwRnd a where baseYymm=:param1 and a.irModelNm=:param2")						
									 .setParameter("param1", bssd) 
									 .setParameter("param2", irModelNm)
									 .executeUpdate();

				log.info("[{}] has been Deleted in Job:[{}] [COUNT: {}]", Process.toPhysicalName(IrParamHwRnd.class.getSimpleName()), jobLog.getJobId(), delNum2);
				
				int delNum3 = session.createQuery("delete IrValidSceSto a where baseYymm=:param1 and a.irModelNm=:param2 and a.modifiedBy=:param3")
									 .setParameter("param1", bssd) 
									 .setParameter("param2", irModelNm)
									 .setParameter("param3", jobLog.getJobId())
									 .executeUpdate();				

				log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, COUNT: {}]", Process.toPhysicalName(IrValidSceSto.class.getSimpleName()), jobLog.getJobId(), irModelNm, delNum3);
				

				for(Map.Entry<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> biz : totalSwMap.entrySet()) {
					// biz별 작업구분해서 아래 작업을 반복 
					EApplBizDv bizDv = biz.getKey();
					if (bizDv ==EApplBizDv.IFRS) {
						log.info(bizDv.name());
					}
					
					for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : biz.getValue().entrySet()) {
						// 금리커브 단위로 작업함. 
						String irCurveNm = curveSwMap.getKey().getIrCurveNm();
						for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
							// 금리커브에 정의된 결정론 시나리오 단위로 반복 
							EDetSce detSceNo = swSce.getKey() ;
							Integer irCurveSceNo = detSceNo.getSceNo();
							
//							if(!biz.getKey().equals("KICS") || !irCurveSceNo.equals(1)) continue;
							log.info("[{}] BIZ: [{}], IR_CURVE_NM: [{}], IR_CURVE_SCE_NO: [{}]", jobLog.getJobId(), bizDv, irCurveNm, irCurveSceNo);
							Map<String, List<?>> hw1fResult = Esg340_BizScenHw1f.createScenHw1f
									        ( bssd
											, bizDv //KICS
											, modelMst
											, irCurveSceNo
											, biz.getValue() //curveSwMap
											, projectionYear);
						
							@SuppressWarnings("unchecked")
							List<IrDcntSceStoBiz> stoSceList = (List<IrDcntSceStoBiz>) hw1fResult.get("SCE");				
							@SuppressWarnings("unchecked")
							List<IrParamHwRnd>    randHwList = (List<IrParamHwRnd>) hw1fResult.get("RND");							
							
							TreeMap<Integer, TreeMap<Integer, Double>> stoSceMap = new TreeMap<Integer, TreeMap<Integer, Double>>();							
							stoSceMap = stoSceList.stream().collect(Collectors.groupingBy(s -> Integer.valueOf(s.getMatCd().substring(1))
													               , TreeMap::new, Collectors.toMap(s -> Integer.valueOf(s.getSceNo()), IrDcntSceStoBiz::getFwdRate, (k, v) -> k, TreeMap::new)));
							
							Esg340_BizScenHw1f.createQuantileValue
										( bssd
										, bizDv
										, modelMst
										, irCurveSceNo
										, stoSceMap
										).forEach(s -> session.save(s));							
							
							int sceCnt = 1;
							for (IrDcntSceStoBiz sce : stoSceList) {						
								session.save(sce);
								if (sceCnt % 50 == 0) {
									session.flush();
									session.clear();
								}
								if (sceCnt % logSize == 0) {
									log.info("Stochastic TermStructure of [{}] [BIZ: {}, ID: {}, SCE: {}] is processed {}/{} in Job:[{}]", irModelNm, bizDv, irCurveNm, irCurveSceNo, sceCnt, stoSceList.size(), jobLog.getJobId());
								}
								sceCnt++;
							}					
							
							if(bizDv.equals(EApplBizDv.KICS)) {
								int rndCnt = 1;
								for (IrParamHwRnd rnd : randHwList) {
									session.save(rnd);
									if (rndCnt % 50 == 0) {
										session.flush();
										session.clear();
									}
									if (rndCnt % logSize == 0) {
										log.info("Stochastic Random Number of [{}] [BIZ: {}, ID: {}, SCE: {}] is processed {}/{} in Job:[{}]", irModelNm, bizDv, irCurveSceNo, irCurveSceNo, rndCnt, randHwList.size(), jobLog.getJobId());
									}
									rndCnt++;
								}
							}
							
							session.flush();
							session.clear();								
						}
					}
				}
				completeJob("SUCCESS", jobLog);	
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);					
			session.getTransaction().commit();
		}	
	}		
	

	private static void job350() {
		if(jobList.contains("350")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG350);			
			
			EIrModel irModelNm   = EIrModel.HW1F ;
//			List<IrParamModel> modelMst = IrParamModelDao.getParamModelList(irModelNm);
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());
			
			Map<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> totalSwMap = new LinkedHashMap<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>>();
			totalSwMap = bizIrParamSw ;

			try {				
				int delNum = session.createQuery("delete StdAsstIrSceSto a where a.baseYymm=:param1")						
									.setParameter("param1", bssd)
									.executeUpdate();

				log.info("[{}] has been Deleted in Job:[{}] [COUNT: {}]", Process.toPhysicalName(StdAsstIrSceSto.class.getSimpleName()), jobLog.getJobId(), delNum);

				for(Entry<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> biz : totalSwMap.entrySet()) {
					EApplBizDv bizDv = biz.getKey();
					
					for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : biz.getValue().entrySet()) {
						for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
							EDetSce detSceNo = swSce.getKey() ;
							log.info("[{}] BIZ: [{}], IR_CURVE_NM: [{}], IR_CURVE_SCE_NO: [{}]", jobLog.getJobId(), bizDv, curveSwMap.getKey().getIrCurveNm(), detSceNo);
							List<StdAsstIrSceSto> bondYieldList = Esg350_BizBondYieldHw1f.createBondYieldHw1f
									( bssd
									, bizDv
									, modelMst
									, detSceNo
									, biz.getValue()
									, projectionYear
									, targetDuration);									
						
							int sceCnt = 1;
							for (StdAsstIrSceSto sce : bondYieldList) {						
								session.save(sce);
								if (sceCnt % 50 == 0) {
									session.flush();
									session.clear();
								}
								if (sceCnt % logSize == 0) {
									log.info("Stochastic Bond Yield of [{}] [BIZ: {}, ASST: {}, SCE: {}] is processed {}/{} in Job:[{}]", irModelNm, bizDv, curveSwMap.getKey(), detSceNo, sceCnt, bondYieldList.size(), jobLog.getJobId());
								}
								sceCnt++;
							}							
							
							session.flush();
							session.clear();								
						}
					}
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}
			
			session.saveOrUpdate(jobLog);					
			session.getTransaction().commit();
		}	
	}	
	
	
	private static void job360() {
		if(jobList.contains("360")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG360);
			
			EIrModel irModelNm   = EIrModel.HW1F ;
			
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());	
			
			Map<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> totalSwMap = new LinkedHashMap<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>>();
			totalSwMap = bizIrParamSw; // 왜 kics 만 담았는지 확인하기 !!
			
//			totalSwMap.put(EApplBizDv.KICS,  kicsSwMap);
////			totalSwMap.put(EApplBizDv.IFRS,  ifrsSwMap);
////			totalSwMap.put(EApplBizDv.IBIZ,  ibizSwMap);
////			totalSwMap.put(EApplBizDv.SAAS,  saasSwMap);
										
			try {				
				int delNum = session.createQuery("delete IrValidRnd a where baseYymm=:param1 and a.irModelNm=:param2")
									.setParameter("param1", bssd) 
									.setParameter("param2", irModelNm)
									.executeUpdate();				

				log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, COUNT: {}]", Process.toPhysicalName(IrValidRnd.class.getSimpleName()), jobLog.getJobId(), irModelNm, delNum);				
				
				for(Map.Entry<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> biz : totalSwMap.entrySet()) {
					
					EApplBizDv bizDv = biz.getKey();
					
					for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : biz.getValue().entrySet()) {
						for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
							
							EDetSce detSceNo = swSce.getKey() ;
							
							if(!bizDv.equals(EApplBizDv.KICS) || !detSceNo.equals(1)) continue;
//							if(!curveSwMap.getKey().equals("1010000")) continue;
							
							log.info("[{}] BIZ: [{}], IR_CURVE_NM: [{}], IR_CURVE_SCE_NO: [{}]", jobLog.getJobId(), bizDv, curveSwMap.getKey(), detSceNo);
							List<IrParamHwRnd> randHwList = Esg360_ValidRandHw1f.createValidInputHw1f
									        ( bssd
											, bizDv
											, modelMst
											, detSceNo.getSceNo()
											, biz.getValue()
											, projectionYear
											, targetDuration
											);
							
							TreeMap<Integer, TreeMap<Integer, Double>> randNumMap = new TreeMap<Integer, TreeMap<Integer, Double>>();							
							randNumMap = randHwList.stream().collect(Collectors.groupingBy(s -> Integer.valueOf(s.getMatCd().substring(1))
													               , TreeMap::new, Collectors.toMap(s -> Integer.valueOf(s.getSceNo()), IrParamHwRnd::getRndNum, (k, v) -> k, TreeMap::new)));
							
//							log.info("rand: {}", randNumMap.firstEntry().getValue());							
							
							Esg360_ValidRandHw1f.testRandNormality    (bssd, modelMst, randNumMap, significanceLevel).forEach(s -> session.save(s));							
							Esg360_ValidRandHw1f.testRandIndependency (bssd, modelMst, randNumMap, significanceLevel).forEach(s -> session.save(s));														
							
							session.flush();
							session.clear();								
						}
					}
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);				
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}		
	}	
	
	
	private static void job370() {
		if(jobList.contains("370")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG370);
			
			EIrModel irModelNm   = EIrModel.HW1F ;
			
			IrParamModel modelMst = IrParamModelDao.getParamModelList(irModelNm).get(0);
			log.info("IrParamModel: {}", modelMst.toString());	
					
			Map<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> totalSwMap = new LinkedHashMap<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>>();
			totalSwMap = bizIrParamSw; // 왜 kics 만 담았는지 확인하기 !!
//			totalSwMap.put(EApplBizDv.KICS,  kicsSwMap);
////			totalSwMap.put(EApplBizDv.IFRS,  ifrsSwMap);
////			totalSwMap.put(EApplBizDv.IBIZ,  ibizSwMap);
////			totalSwMap.put(EApplBizDv.SAAS,  saasSwMap);
										
			try {				
				int delNum = session.createQuery("delete IrValidSceSto a where baseYymm=:param1 and a.irModelNm=:param2 and a.modifiedBy=:param3")
									.setParameter("param1", bssd) 
									.setParameter("param2", irModelNm)
									.setParameter("param3", jobLog.getJobId())
									.executeUpdate();				

				log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, COUNT: {}]", Process.toPhysicalName(IrValidSceSto.class.getSimpleName()), jobLog.getJobId(), irModelNm, delNum);
				
				for(Entry<EApplBizDv, Map<IrCurve, Map<EDetSce, IrParamSw>>> biz : totalSwMap.entrySet()) {
					EApplBizDv bizDv = biz.getKey();
					for(Map.Entry<IrCurve, Map<EDetSce, IrParamSw>> curveSwMap : biz.getValue().entrySet()) {
						for(Map.Entry<EDetSce, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {				
							
							EDetSce detSceNo = swSce.getKey() ;
							
//							if(!bizDv.equals("KICS") || !detSceNo.equals(1)) continue;
//							if(!curveSwMap.getKey().equals("1010000")) continue;
							
							log.info("[{}] BIZ: [{}], IR_CURVE_NM: [{}], IR_CURVE_SCE_NO: [{}]", jobLog.getJobId(), bizDv, curveSwMap.getKey().getIrCurveNm() , detSceNo);
							Map<String, List<?>> hw1fResult = Esg370_ValidScenHw1f.createValidInputHw1f(
									bssd
								  , bizDv
//								  , irModelNm
//								  , curveSwMap.getKey().getIrCurveNm()
								  , modelMst
								  , detSceNo.getSceNo()
								  , biz.getValue()
//								  , modelMstMap
								  , projectionYear
								  , targetDuration);
						
							@SuppressWarnings("unchecked")
							List<IrDcntSceStoBiz> stoSceList = (List<IrDcntSceStoBiz>) hw1fResult.get("SCE");				
							@SuppressWarnings("unchecked")
							List<StdAsstIrSceSto> stoYldList = (List<StdAsstIrSceSto>) hw1fResult.get("YLD");							

							TreeMap<Integer, TreeMap<Integer, Double>> stoSceMap = new TreeMap<Integer, TreeMap<Integer, Double>>();							
							stoSceMap = stoSceList.stream().collect(Collectors.groupingBy(s -> Integer.valueOf(s.getMatCd().substring(1))
													               , TreeMap::new, Collectors.toMap(s -> Integer.valueOf(s.getSceNo()), IrDcntSceStoBiz::getFwdRate, (k, v) -> k, TreeMap::new)));

							TreeMap<Integer, TreeMap<Integer, Double>> stoYldMap = new TreeMap<Integer, TreeMap<Integer, Double>>();							
							stoYldMap = stoYldList.stream().collect(Collectors.groupingBy(s -> Integer.valueOf(s.getMatCd().substring(1))
													               , TreeMap::new, Collectors.toMap(s -> Integer.valueOf(s.getSceNo()), StdAsstIrSceSto::getAsstYield, (k, v) -> k, TreeMap::new)));							

//							log.info("dcnt  : {}", stoBizMap.get(2).entrySet());
//							log.info("yield : {}", stoYldMap.get(2).entrySet());							
							
							Esg370_ValidScenHw1f.testMarketConsistency(
									bssd
								  , bizDv
//								  , irModelNm
//								  , curveSwMap.getKey().getIrCurveNm()
								  , modelMst
								  , detSceNo.getSceNo()
								  , stoSceMap
								  , stoYldMap
								  , significanceLevel)
							.forEach(s -> session.save(s));
							
							session.flush();
							session.clear();								
						}
					}
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);				
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}		
	}

	
	

	private static void job710() {
		if(jobList.contains("710")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG710);			

			EIrModel irModelNm     = EIrModel.AFNS_IM;						
			int    weekDay         = Integer.valueOf((String) argInDBMap.getOrDefault("AFNS_WEEK_DAY"        , "5")); //금욜 
			
			List<IrParamModel> modelMstList = IrParamModelDao.getParamModelList(irModelNm);
			Map<String, IrParamModel> irModelMstMap = modelMstList.stream()
													.collect(Collectors.toMap(IrParamModel::getIrCurveNm, Function.identity()));
			
			log.info("IrParamModel: {}", irModelMstMap.toString());			
			
			try {
				for (IrCurve irCurve :irCurveList) {
				    	
					    String        irCurveNm  = irCurve.getIrCurveNm() ;
					    IrParamSw     irparamSw  = commIrParamSw.get(irCurveNm) ;
					    IrParamModel  irModelMst = irModelMstMap.get(irCurveNm) ;
					
					    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					if(!irModelMstMap.containsKey(irCurveNm)) {
						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
						continue;
					}
					
					log.info("AFNS Shock Spread (Cont) for [{}({}, {})]", irCurveNm, irCurve.getIrCurveNm(), irCurve.getCurCd());
					
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));

					// 엑셀과 모수 추정에 사용하는 테너만 남기기
					tenorList.remove("M0003");tenorList.remove("M0006");tenorList.remove("M0009");tenorList.remove("M0018");tenorList.remove("M0030");tenorList.remove("M0048"); tenorList.remove("M0084"); tenorList.remove("M0180");  //FOR CHECK w/ FSS
					
					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;
					}

					// 이전에 산출한 초기모수 삭제 
					int delNum1 = session.createQuery("delete IrParamAfnsCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3 ") // and modifiedBy =:param4")
		                     			 .setParameter("param1", bssd) 
		                     			 .setParameter("param2", irModelNm)
		                     			 .setParameter("param3", irCurveNm)
//		                     			 .setParameter("param4", "ESG710")
		                     			 .executeUpdate();
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamAfnsCalc.class.getSimpleName()), jobLog.getJobId(), irModelNm, irCurveNm, delNum1);
					
					
					List<IrCurveSpotWeek> weekHisList    = IrCurveSpotWeekDao.getIrCurveSpotWeekHis(bssd, iRateHisStBaseDate, irCurve, tenorList, weekDay, false);
					List<IrCurveSpotWeek> weekHisBizList = IrCurveSpotWeekDao.getIrCurveSpotWeekHis(bssd, iRateHisStBaseDate, irCurve, tenorList, weekDay, true);
					log.info("weekHisList: [{}], [TOTAL: {}, BIZDAY: {}], [from {} to {}, weekDay:{}]", irCurveNm, weekHisList.size(), weekHisBizList.size(), iRateHisStBaseDate.substring(0,6), bssd, weekDay);			

					//for ensuring enough input size
					if(weekHisList.size() < 1000) {
						log.warn("Weekly SpotRate Data is not Enough [ID: {}, SIZE: {}] for [{}]", irCurveNm, weekHisList.size(), bssd);
						continue;
					}					

					List<IRateInput> curveHisList = weekHisList.stream().map(s->s.convertToHis()).collect(toList());
//					log.info("{}", curveHisList);
					
					//Any curveBaseList result in same parameters and spreads.
					List<IRateInput> curveBaseList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm, tenorList);					
					
					if(curveBaseList.size()==0) {
						log.warn("No IR Curve Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					
					Map<String, List<?>> resultMap = new TreeMap<String, List<?>>();
					resultMap = Esg710_SetAfnsInitParam.setAfnsInitParam(FinUtils.toEndOfMonth(bssd)
																			  , curveHisList
																			  , curveBaseList
																			  , irModelMst  // add 
																			  , irparamSw   // add 
																			  , argInDBMap  // add 
																			  );	
											
					for(Map.Entry<String, List<?>> rslt : resultMap.entrySet()) {						
						rslt.getValue().forEach(s -> session.save(s));

						session.flush();
						session.clear();
					}		
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	private static void job720() {
		if(jobList.contains("720")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG720);			

			EIrModel irModelNm     = EIrModel.AFNS_IM;						
			int    weekDay         = Integer.valueOf((String) argInDBMap.getOrDefault("AFNS_WEEK_DAY"        , "5")); //금욜 
			
			List<IrParamModel> modelMstList = IrParamModelDao.getParamModelList(irModelNm);
			Map<String, IrParamModel> irModelMstMap = modelMstList.stream()
													.collect(Collectors.toMap(IrParamModel::getIrCurveNm, Function.identity()));
			
			log.info("IrParamModel: {}", irModelMstMap.toString());			
			
			try {
				for (IrCurve irCurve :irCurveList) {
				    	
					    String        irCurveNm  = irCurve.getIrCurveNm() ;
					    IrParamSw     irparamSw  = commIrParamSw.get(irCurveNm) ;
					    IrParamModel  irModelMst = irModelMstMap.get(irCurveNm) ;
					
					    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					if(!irModelMstMap.containsKey(irCurveNm)) {
						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
						continue;
					}
					
					log.info("AFNS Shock Spread (Cont) for [{}({}, {})]", irCurveNm, irCurve.getIrCurveNm(), irCurve.getCurCd());
					
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));

					// 엑셀과 모수 추정에 사용하는 테너만 남기기
					tenorList.remove("M0003");tenorList.remove("M0006");tenorList.remove("M0009");tenorList.remove("M0018");tenorList.remove("M0030");tenorList.remove("M0048"); tenorList.remove("M0084"); tenorList.remove("M0180");  //FOR CHECK w/ FSS
					
					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;

					}
//					 double[] genTenor = EBaseTenor.getTenorArray(tenorList) ;
					// 기본적으로 인풋에 사용되는 금리 정보를 바탕으로 그에 대응되는 테너를 사용하지만, 금리 모델에서 생성하고자 하는 경우 테너가 추가될수도, 제거될 수 있는 것임. 
					// 필요한 경우에 목적에 맞는 tenor 를 irModel에 전해주기 


					int delNum1 = session.createQuery("delete IrParamAfnsCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3 and modifiedBy =:param4")
		                     			 .setParameter("param1", bssd) 
		                     			 .setParameter("param2", irModelNm)
		                     			 .setParameter("param3", irCurveNm)
		                     			 .setParameter("param4", "ESG720")
		                     			 .executeUpdate();
					
					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrParamAfnsCalc.class.getSimpleName()), jobLog.getJobId(), irModelNm, irCurveNm, delNum1);
										
					List<IrCurveSpotWeek> weekHisList    = IrCurveSpotWeekDao.getIrCurveSpotWeekHis(bssd, iRateHisStBaseDate, irCurve, tenorList, weekDay, false);

					List<IRateInput> curveHisList = weekHisList.stream().map(s->s.convertToHis()).collect(toList());
					
					//Any curveBaseList result in same parameters and spreads.
					List<IRateInput> curveBaseList = IrCurveSpotDao.getIrCurveSpot(bssd, irCurveNm, tenorList);					
					
					if(curveBaseList.size()==0) {
						log.warn("No IR Curve Data [IR_CURVE_NM: {}] for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					// enum에 정의된 순서로 정렬해서 받음 =>  모수는 enum에 정의한 순서대로 다른 의미를 갖는 값이기 때문에 순서가 중요함.  
					List<IrParamAfnsCalc> initParam = IrParamAfnsDao.getIrParamAfnsCalcInitList(bssd, EIrModel.AFNS_IM, irCurveNm).stream()
						                                            .sorted(Comparator.comparingInt(p -> p.getParamTypCd().ordinal()))
						                                            .collect(Collectors.toList());
					
					Map<String, List<?>> resultMap = new TreeMap<String, List<?>>();
					resultMap = Esg720_optAfnsParam.optimizationParas(FinUtils.toEndOfMonth(bssd)
																			  , curveHisList
																			  , curveBaseList
																			  , irModelMst  
																			  , irparamSw   
																			  , argInDBMap 
																			  , initParam // add
																			  );	
											
					for(Map.Entry<String, List<?>> rslt : resultMap.entrySet()) {						
						rslt.getValue().forEach(s -> session.save(s));

						session.flush();
						session.clear();
					}					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	

	private static void job730() {
		if(jobList.contains("730")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG730);			

			EIrModel irModelNm     = EIrModel.AFNS_IM;					
			
			List<IrParamModel> modelMstList = IrParamModelDao.getParamModelList(irModelNm);
			Map<String, IrParamModel> irModelMstMap = modelMstList.stream()
													.collect(Collectors.toMap(IrParamModel::getIrCurveNm, Function.identity()));
			
			log.info("IrParamModel: {}", irModelMstMap.toString());			
			
			try {
				for (IrCurve irCurve :irCurveList) {
				    	
					    String        irCurveNm  = irCurve.getIrCurveNm() ;
					    IrParamSw     irparamSw  = commIrParamSw.get(irCurveNm) ;
					    IrParamModel  irModelMst = irModelMstMap.get(irCurveNm) ;
					
					    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					if(!irModelMstMap.containsKey(irCurveNm)) {
						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
						continue;
					}
					
					log.info("AFNS Shock Spread (Cont) for [{}({}, {})]", irCurveNm, irCurve.getIrCurveNm(), irCurve.getCurCd());

					// 시나리오를 생성할 기본 테너 
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));

					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;
					}
					 double[] genTenor = EBaseTenor.getTenorArray(tenorList) ;
					// 기본적으로 인풋에 사용되는 금리 정보를 바탕으로 그에 대응되는 테너를 사용하지만, 금리 모델에서 생성하고자 하는 경우 테너가 추가될수도, 제거될 수 있는 것임. 
					// 필요한 경우에 목적에 맞는 tenor 를 irModel에 전해주기 

					
					int delNum2 = session.createQuery("delete IrSprdAfnsCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3")
					                     .setParameter("param1", bssd) 
								  		 .setParameter("param2", irModelNm)
										 .setParameter("param3", irCurveNm)
										 .executeUpdate();					

					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()), jobLog.getJobId(), irModelNm, irCurveNm, delNum2);
					
					
					List<IrParamAfnsCalc> optParam = IrParamAfnsDao.getIrParamAfnsCalcList(bssd, EIrModel.AFNS_IM, irCurveNm).stream()
                            .sorted(Comparator.comparingInt(p -> p.getParamTypCd().ordinal()))
                            .collect(Collectors.toList());
					
					Map<String, List<?>> irShockSenario = new TreeMap<String, List<?>>();
					irShockSenario = Esg730_ShkSprdAfns.createAfnsShockScenario(FinUtils.toEndOfMonth(bssd)
																			  , genTenor // add
																			  , irModelMst 
																			  , irparamSw   
																			  , argInDBMap 
																			  , optParam // add 
																			  );	
											
					for(Map.Entry<String, List<?>> rslt : irShockSenario.entrySet()) {						
						rslt.getValue().forEach(s -> session.save(s));

						session.flush();
						session.clear();
					}					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	private static void job740() {
		if(jobList.contains("740")) {
			session.beginTransaction();
			CoJobInfo jobLog = startJogLog(EJob.ESG740);			

			EIrModel irModelNm      = EIrModel.AFNS_STO;	  // IR_SPRD_AFNS_calc에 이 결과도 적재하기 위해 모델 이름 추가 (동일한 AFNS 모수를 사용함.) 				
			EIrModel upperIrModelNm = irModelNm.getIrModel();	// AFNS_IM 				
			
			List<IrParamModel> modelMstList = IrParamModelDao.getParamModelList(upperIrModelNm);
			Map<String, IrParamModel> irModelMstMap = modelMstList.stream()
													.collect(Collectors.toMap(IrParamModel::getIrCurveNm, Function.identity()));
			
			log.info("IrParamModel: {}", irModelMstMap.toString());			
			
			try {
				for (IrCurve irCurve :irCurveList) {
				    	
					    String        irCurveNm  = irCurve.getIrCurveNm() ;
					    IrParamSw     irparamSw  = commIrParamSw.get(irCurveNm) ;
					    IrParamModel  irModelMst = irModelMstMap.get(irCurveNm) ;
					
					    
					if(!commIrParamSw.containsKey(irCurveNm)) {
						log.warn("No Ir Curve Data [{}] in Smith-Wilson Map for [{}]", irCurveNm, bssd);
						continue;
					}					
					
					if(!irModelMstMap.containsKey(irCurveNm)) {
						log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", upperIrModelNm, irCurveNm, Process.toPhysicalName(IrParamModel.class.getSimpleName()));
						continue;
					}
					
					log.info("AFNS Shock Spread (Cont) for [{}({}, {})]", irCurveNm, irCurve.getIrCurveNm(), irCurve.getCurCd());

					// 시나리오를 생성할 기본 테너 
					List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, irCurveNm, Math.min(irparamSw.getLlp(), 20));

					log.info("TenorList in [{}]: ID: [{}], llp: [{}], matCd: {}", jobLog.getJobId(), irCurveNm, Math.min(irparamSw.getLlp(), 20), tenorList);
					if(tenorList.isEmpty()) {
						log.warn("No Spot Rate Data [ID: {}] for [{}]", irCurveNm, bssd);
						continue;
					}
					 double[] genTenor = EBaseTenor.getTenorArray(tenorList) ;
					// 기본적으로 인풋에 사용되는 금리 정보를 바탕으로 그에 대응되는 테너를 사용하지만, 금리 모델에서 생성하고자 하는 경우 테너가 추가될수도, 제거될 수 있는 것임. 
					// 필요한 경우에 목적에 맞는 tenor 를 irModel에 전해주기 

					// 적재할 테이블 조건 추가하기 740에 해당하는 것만 지워야 함. AFNS_STO
					int delNum2 = session.createQuery("delete IrSprdAfnsCalc a where baseYymm=:param1 and a.irModelNm=:param2 and a.irCurveNm=:param3 ")
					                     .setParameter("param1", bssd) 
								  		 .setParameter("param2", irModelNm) // AFNS_STO
										 .setParameter("param3", irCurveNm)
										 .executeUpdate();					

					log.info("[{}] has been Deleted in Job:[{}] [IR_MODEL_NM: {}, IR_CURVE_NM: {}, COUNT: {}]", Process.toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()), jobLog.getJobId(), irModelNm, irCurveNm, delNum2);

					// pk 중복을 피하기 위해 모델명을 구분하였으나 (AFNS_STO) 모수는 동일하게 (AFNS) 모수를 사용함.  
					List<IrParamAfnsCalc> optParam = IrParamAfnsDao.getIrParamAfnsCalcList(bssd, upperIrModelNm, irCurveNm).stream()
                            .sorted(Comparator.comparingInt(p -> p.getParamTypCd().ordinal()))
                            .collect(Collectors.toList());
					
					Map<String, List<?>> irShockSenario = new TreeMap<String, List<?>>();
					irShockSenario = Esg740_ShkSprdAfnsSto.createAfnsShockScenario(FinUtils.toEndOfMonth(bssd)
																			  , genTenor // add
																			  , irModelMst 
																			  , irparamSw   
																			  , argInDBMap 
																			  , optParam // add 
																			  , irModelNm
																			  );	
											
					for(Map.Entry<String, List<?>> rslt : irShockSenario.entrySet()) {						
						rslt.getValue().forEach(s -> session.save(s));

						session.flush();
						session.clear();
					}					
				}
				completeJob("SUCCESS", jobLog);
				
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}			
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}
	
	private static void job810() {
		if(jobList.contains("810")) {
			session.beginTransaction();		
			CoJobInfo jobLog = startJogLog(EJob.ESG810);
			
			try {
				int delNum = session.createQuery("delete RcCorpTm a where a.baseYymm = :param").setParameter("param", bssd).executeUpdate();	
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(RcCorpTm.class.getSimpleName()), jobLog.getJobId(), bssd, delNum);				
				
				List<String> agencyCd = RcCorpPdDao.getAgencyCdUsr(bssd);
				log.info("Credit Rating Agency: {}", agencyCd);
				
				for(String agency : agencyCd) {
					List<RcCorpTm> rcCorpTmList = Esg810_SetTransitionMatrix.createCorpTmFromUsr(bssd, agency);				
					rcCorpTmList.stream().forEach(s -> session.save(s));			
				}				
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
	
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	
	private static void job820() {
		if(jobList.contains("820")) {		
			session.beginTransaction();		
			CoJobInfo jobLog = startJogLog(EJob.ESG820);
			
			try {
				int delNum1 = session.createQuery("delete RcCorpPd a where a.baseYymm = :param").setParameter("param", bssd).executeUpdate();	
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(RcCorpPd.class.getSimpleName()), jobLog.getJobId(), bssd, delNum1);
				
				int delNum2 = session.createQuery("delete RcCorpPdBiz a where a.baseYymm = :param").setParameter("param", bssd).executeUpdate();	
				log.info("[{}] has been Deleted in Job:[{}] [BASE_YYMM: {}, COUNT: {}]", Process.toPhysicalName(RcCorpPdBiz.class.getSimpleName()), jobLog.getJobId(), bssd, delNum2);				

				List<String> agencyCd = RcCorpPdDao.getAgencyCd(bssd);
				log.info("Credit Rating Agency: {}", agencyCd);
				
				for(String agency : agencyCd) {
					List<RcCorpPd> rcCorpPdList = Esg820_RcCorpPd.createRcCorpPd(bssd, agency, projectionYear);				
					rcCorpPdList.stream().forEach(s -> session.save(s));
					
					if(!agency.equals("NICE")) continue;
					
					List<RcCorpPdBiz> rcCorpPdBizKicsList = Esg820_RcCorpPd.createRcCorpPdBiz(bssd, EApplBizDv.KICS, agency, rcCorpPdList);
					rcCorpPdBizKicsList.stream().forEach(s -> session.save(s));
					
					List<RcCorpPdBiz> rcCorpPdBizIfrsList = Esg820_RcCorpPd.createRcCorpPdBiz(bssd, EApplBizDv.IFRS, agency, rcCorpPdList);
					rcCorpPdBizIfrsList.stream().forEach(s -> session.save(s));					
				}				
				
				session.flush();
				session.clear();
				completeJob("SUCCESS", jobLog);
	
			} catch (Exception e) {
				log.error("ERROR: {}", e);
				completeJob("ERROR", jobLog);
			}
			session.saveOrUpdate(jobLog);
			session.getTransaction().commit();
		}
	}	
	
	
	private static CoJobInfo startJogLog(EJob job) {
		CoJobInfo jobLog = new CoJobInfo();
		jobLog.setJobStart(LocalDateTime.now());
		
		jobLog.setJobId(job.name());
		jobLog.setCalcStart(LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")));
		jobLog.setBaseYymm(bssd);
		jobLog.setCalcDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd")));
		jobLog.setJobNm(job.getJobName());
		
		log.info("{}({}): Job Start !!! " , job.name(), job.getJobName());
		
		return jobLog;
	}
	
	private static void completeJob(String successDiv, CoJobInfo jobLog) {		

		long timeElapse = Duration.between(jobLog.getJobStart(), LocalDateTime.now()).getSeconds();
		
//		log.info("timeElapse: {}, jobStart: {}", timeElapse, jobLog.getJobStart());
		jobLog.setCalcEnd(LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")));
		jobLog.setCalcScd(successDiv);
		jobLog.setCalcElps(String.valueOf(timeElapse));
		jobLog.setModifiedBy(jobLog.getJobId());
		jobLog.setUpdateDate(LocalDateTime.now());
		
		log.info("{}({}): Job Completed with {} !!!", jobLog.getJobId(), jobLog.getJobNm(), successDiv);
	}		
	
	
	private static void hold() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error("ERROR: {}", e);
		}		
	}
		
	
	protected static void saveOrUpdate(Object item) {
		session.saveOrUpdate(item);

		if(cnt % flushSize ==0) {
			session.flush();
			session.clear();
			log.info("in the flush : {}", cnt);
		}
		cnt = cnt+1;
	}
	

	protected static void save(Object item) {
		session.save(item);
			
		if(cnt % flushSize ==0) {
			session.flush();
			session.clear();
			log.info("in the flush : {}", cnt);
		}
		cnt = cnt+1;
	}

}
