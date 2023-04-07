package com.gof.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

//import com.gof.entity.IrCurveSpot;
//import com.gof.entity.IrCurveYtm;
import com.gof.interfaces.IRateInput;
import com.gof.model.entity.SmithWilsonRslt;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmithWilsonKicsBts extends IrModel {	
	
	private int                           freq              = 2;
	private double                        alphaApplied      = 0.1;
	private double                        lastLiquidPoint   = 20;
	private double                        ltfr;	
	private double                        ltfrCont;
	private int                           ltfrT;		
	private double                        liqPrem           = 0.481;
	
	private double[]                      cfCol;
	private double[][]                    cfMatrix;		
	private RealMatrix                    zetaHat;
	

	/**
	 * 자산평가용 
	 * @param IrCurveYtm 
	 * @param ltfr = iRateBase[this.iRateBase.length-1] ; 마지막값  
	 * @param alphaApplied 0.1 
	 * @param freq 이자지급주기 2  */
	
	@Builder(builderClassName="of", builderMethodName="of")
	public SmithWilsonKicsBts(LocalDate baseDate
							, List<IRateInput> ytmCurveHisList
							, Double alphaApplied
							, Boolean isRealNumber
							, Integer freq
							, Double liqPrem) 
	{				
		super();		
		this.baseDate = baseDate;
//		this.setTermStructureYtm(ytmCurveHisList);
		this.setTermStructure(ytmCurveHisList);
		this.setLastLiquidPoint(this.tenor[this.tenor.length-1]);
		this.isRealNumber = (isRealNumber == null ? true : isRealNumber);		
		this.alphaApplied = (alphaApplied == null ? 0.1  : alphaApplied);		
		this.freq         = (freq         == null ? 2    : freq        );
		this.liqPrem      = (liqPrem      == null ? 0.0  : liqPrem     );
		
		double toRealScale = this.isRealNumber ? 1 : 0.01;
		for(int i=0; i<this.iRateBase.length; i++) this.iRateBase[i] = toRealScale * this.iRateBase[i];		
				
		this.ltfr = this.iRateBase[this.iRateBase.length-1];
		this.ltfrT = (int) this.lastLiquidPoint;
		this.ltfrCont = irDiscToCont(this.ltfr);
		this.liqPrem = toRealScale * this.liqPrem;
		
//		log.info("baseDate: {}, tenor:{}, iRate:{}, ltfrT:{}, ltfr:{}, ltfrCont:{}", this.baseDate, this.tenor, this.iRateBase, this.ltfrT, this.ltfr, this.ltfrCont);
	}
	
//	23.04.03 주석처리 타입변환은 여기에서 안함. 
/* // (타입변환) SmithWilsonResultList (SmithWilsonRslt 타입)를 IrCurveSpot 타입의 list로 리턴함
//	public List<IrCurveSpot> getSpotBtsRslt() {		
//		
//		List<IrCurveSpot> curveList = new ArrayList<IrCurveSpot>();
//				
//		for(SmithWilsonRslt sw : this.getSmithWilsonResultList()) { 
//			// 만기코드별로 한줄씩 결과값이 출력되기 때문에 한줄 씩 타입변환해서 담아주기 
//			IrCurveSpot crv = new IrCurveSpot();
//			crv.setBaseDate(sw.getBaseDate());
//			crv.setMatCd(sw.getMatCd());
////			crv.setIntRate(sw.getSpotCont());
//			crv.setSpotRate(sw.getSpotDisc());
//			curveList.add(crv);
//		}		
//		return curveList;
//	}	
*/

	
	// 테너가 다를 때(보간 보외), SmithWilsonResult
	public List<SmithWilsonRslt> getSmithWilsonResultList(double[] prjTenor) {

		List<SmithWilsonRslt> resultList = new ArrayList<SmithWilsonRslt>();		
		resultList.addAll(this.swProjectionList(this.alphaApplied, prjTenor));
		
		return resultList;		
	}

	
    // 기본 테너를 기준으로 반환(ytm2spot)할 때, SmithWilsonResult 
	public List<SmithWilsonRslt> getSmithWilsonResultList() {
		
		List<SmithWilsonRslt> resultList = new ArrayList<SmithWilsonRslt>();		
		resultList.addAll(this.swProjectionList(this.alphaApplied, this.tenor));
		
		return resultList;
	}	

	/**
	 * m = Cp = Cu + (CWC')ζ </br>
	 * m : YTM Price </br>
	 * C : YTM Cashflow </br>
	 * p : ZCB Price(i.e. SPOT) </br>
	 * 
	 * @param alpha
	 */	
	private void smithWilsonZeta(double alpha) {		
		
		if(this.freq > 0) {		
			Set<Double> cfColSet = new TreeSet<Double>();	
			
			for(int i=0; i<this.tenor.length; i++) {
				int jMax = (int) Math.ceil(this.tenor[i] * this.freq);
				
				for(int j=0; j<jMax; j++) {
					cfColSet.add(this.tenor[i] - 1.0 * j / this.freq);
				}
			}
			this.cfCol = cfColSet.stream().mapToDouble(Double::doubleValue).toArray(); 
			
			this.cfMatrix = new double[this.tenor.length][this.cfCol.length]; 
			
			for(int i=0; i<cfMatrix.length; i++) {
				for(int j=0; j<cfMatrix[i].length; j++) {
					if(Math.abs(this.cfCol[j] - this.tenor[i]) < ZERO_DOUBLE) {					
						this.cfMatrix[i][j] = 1 + this.iRateBase[i] / this.freq;
					}
					else if(this.cfCol[j] < this.tenor[i]) {
						int tmp = (int) ((this.tenor[i] - this.cfCol[j]) * MONTH_IN_YEAR) % (MONTH_IN_YEAR / this.freq);
						if(tmp == 0) this.cfMatrix[i][j] = this.iRateBase[i] / this.freq;
						else  this.cfMatrix[i][j] = 0.0;
					}
					else this.cfMatrix[i][j] = 0.0;				
				}
			}
		}
		//for this.freq == 0
		else {
			this.cfCol = this.tenor;
			this.cfMatrix = new double[this.tenor.length][this.cfCol.length];
			
			for(int i=0; i<cfMatrix.length; i++) {
				for(int j=0; j<cfMatrix[i].length; j++) {
					if(i == j) {
						this.cfMatrix[i][j] = 1.0;
					}
					else {
						this.cfMatrix[i][j] = 0.0;
					}									
				}
			}
		}
//		log.info("cfCol: {}, cfMatx: {}", this.cfCol, this.cfMatrix);
		
//		Constructing m, mu, m - C * mu
		double[] mean = new double[this.tenor.length];
		for(int i=0; i<mean.length; i++) mean[i] = this.ytmPrice(this.tenor[i], this.iRateBase[i], this.freq);
		
		double[] muCol = new double[this.cfCol.length];
		for(int i=0; i<muCol.length; i++) muCol[i] = this.zeroBondUnitPrice(this.ltfrCont, this.cfCol[i]);		
		
//		log.info("{},{}", this.cfCol, alpha);		
		RealMatrix weight  = MatrixUtils.createRealMatrix(smithWilsonWeight(this.cfCol, this.cfCol, alpha, this.ltfrCont));
		RealMatrix cfMatx  = MatrixUtils.createRealMatrix(this.cfMatrix);
		RealMatrix cwctInv = MatrixUtils.inverse(cfMatx.multiply(weight).multiply(cfMatx.transpose())); // (CWCt)^(-1)
		RealMatrix cDotMu  = cfMatx.multiply(MatrixUtils.createColumnRealMatrix(muCol)); // C * Mu
		RealMatrix mSubCU  = MatrixUtils.createColumnRealMatrix(mean).subtract(cDotMu); // m - C * mu
		RealMatrix zetaCol = cwctInv.multiply(mSubCU); // zeta = (CWCt)^(-1) * ( m - C * Mu) 
		
		this.zetaHat       = cfMatx.transpose().multiply(zetaCol); //C^T * zeta 
	}
	
// 23.04.03 주석처리 굳이 이 단계를 나눈 이유는 뭘까? 	
//	private List<SmithWilsonRslt> swProjectionList(double alpha) {
//		return swProjectionList(alpha, this.tenor);
//	}
	
	//TODO:
	/**
	 * @param alpha, prjTenor
	 * @return swResultlList
	 * @see smithWilsonZeta
*/
	private List<SmithWilsonRslt> swProjectionList(double alpha, double[] prjTenor) {
		
		List<SmithWilsonRslt> swResultlList = new ArrayList<SmithWilsonRslt>();			
		smithWilsonZeta(alpha);	// zetaHat

		RealMatrix weightPrj = MatrixUtils.createRealMatrix(smithWilsonWeight(prjTenor, this.cfCol, alpha, this.ltfrCont));

		double[] muPrj = new double[prjTenor.length];
		for(int i=0; i<muPrj.length; i++) muPrj[i] = this.zeroBondUnitPrice(this.ltfrCont, prjTenor[i]);				
		
		RealMatrix priceCol  = weightPrj.multiply(this.zetaHat).add(MatrixUtils.createColumnRealMatrix(muPrj));
		//p(t) = e^(-ufr*t) + sigma (zetaHat * W) s.t. W = wilson-function 
		
		//log.info("{}", priceCol.getEntry(1,0)) : column index = 1
		double[] priceZcb = new double[prjTenor.length];
		double[] spotCont = new double[prjTenor.length];
		double[] fwdCont  = new double[prjTenor.length];

		for(int i=0; i<prjTenor.length; i++) {
			priceZcb[i] = priceCol.getEntry(i,0);		// zero coupon bond price	
			spotCont[i] = -1.0 / prjTenor[i] * Math.log(priceZcb[i]); // spot(cont)
			spotCont[i] = Math.log(Math.exp(spotCont[i]) + this.liqPrem);	// dist2Cont(spot(dist) => 유동성반영 후 spot(cont) 		
			fwdCont[i]  = (i > 0) ? (spotCont[i] * prjTenor[i] - spotCont[i-1] * prjTenor[i-1]) / (prjTenor[i] - prjTenor[i-1]) : spotCont[i];	// 직전 tenor와 당기 tenor를 이용해서 fwd rate산출 		
			
			SmithWilsonRslt swResult = new SmithWilsonRslt();
			
			swResult.setBaseDate(baseDate.toString());
			swResult.setResultType("SW SPOT");
			swResult.setScenType("1");			
			swResult.setMatCd(String.format("%s%04d", TIME_UNIT_MONTH, (int) (prjTenor[i] * MONTH_IN_YEAR)));			
			swResult.setDcntFactor(priceZcb[i]);
			swResult.setSpotCont(round(spotCont[i]));
			swResult.setFwdCont(round(fwdCont[i]));
			swResult.setSpotDisc(round(irContToDisc(spotCont[i])));			
			swResult.setFwdDisc(round(irContToDisc(fwdCont[i])));
			
			swResultlList.add(swResult);
		}
		return swResultlList;
	}	
	
	/**
	 * symmetric wilson W(i,j) function 
	 * @param prjYearFrac, tenorYearFrac, alpha, ltfrCont
	 * @return weight matrix
	 * @see swProjectionList
*/
	private double[][] smithWilsonWeight(double[] prjYearFrac, double[] tenorYearFrac, double alpha, double ltfrCont) {
		
		double[][] weight = new double[prjYearFrac.length][tenorYearFrac.length];
		double min, max;
		
		for(int i=0; i<prjYearFrac.length; i++) {
			for(int j=0; j<tenorYearFrac.length; j++) {
				
				min = Math.min(prjYearFrac[i], tenorYearFrac[j]);
				max = Math.max(prjYearFrac[i], tenorYearFrac[j]);
				weight[i][j] = Math.exp(-ltfrCont * (prjYearFrac[i] + tenorYearFrac[j])) * (alpha * min - Math.exp(-alpha*max) * Math.sinh(alpha*min));				
			}
		}
		return weight;
	}
	
	
	private double ytmPrice(double tenor, double ytm, int freq) {
		
		if(freq < 1) return 1 / Math.pow(1 + ytm, tenor);
		
		double T  = tenor;
		double P  = 0.0;
		double Cf = 0.0;
		double Df = 0.0;		
		
		while(T > 0) {
			if(Math.abs(T - tenor) < ZERO_DOUBLE) Cf = 1 + ytm / freq;
			else Cf = ytm / freq;
			
			if(Math.abs(T * freq - (int) (T * freq)) < ZERO_DOUBLE) Df = Math.pow(1 + ytm / freq, -T * freq);
			else Df = 1 / (1 + ytm * T);
			
			P += Cf * Df;
			T -= 1.0 / freq;
		}		
		return P;
	}
		
}
