/*
 *
 * Contributor(s):
 *  Felipe Moraes felipemoraes{a}dcc.ufmg.br (original author) 
 *  
 */

package br.ufmg.dcc.latin.utils;

import java.util.stream.IntStream;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;


/**
 * Implementation of Tau AP rank correlation</a>.
 * Tau AP defined as:
 * <pre>
 * tau_ap = (2)/(N-1) * \sum_(2)^(N)(C(i)/(i-1) -1)
 * </pre>
 * <p>
 * where:
 * <ul>
 *     <li>C(i)<sub>0</sub> = number of items above rank i and correctely ranked with respect to the item at rank in the first list</li>
 * </ul>
 * <p>
 *
 * @see A new rank correlation coefficient for information retrieval. Emine Yilmaz, Javed A. Aslam, and Stephen Robertson. SIGIR 2008.
 *
 */


public class TauApCorrelation {
	
	
	 /** correlation matrix */
    private final RealMatrix correlationMatrix;
	
    /**
     * Create a TauApCorrelation instance without data.
     */
    public TauApCorrelation() {
        correlationMatrix = null;
    }
	
	/**
     * Create a TauApCorrelation from a rectangular array
     * whose columns represent values of variables to be correlated.
     *
     * @param data rectangular array with columns representing variables
     * @throws IllegalArgumentException if the input data array is not
     * rectangular with at least two rows and two columns.
     */
	public TauApCorrelation(double[][] data){
		this(MatrixUtils.createRealMatrix(data));
	}
	
    /**
     * Create a TauApCorrelation from a RealMatrix whose columns
     * represent variables to be correlated.
     *
     * @param matrix matrix with columns representing variables to correlate
     */
    public TauApCorrelation(RealMatrix matrix) {
        correlationMatrix = computeCorrelationMatrix(matrix);
    }
    
	/**
	* Returns the correlation matrix.
	*
	* @return correlation matrix
	*/
    public RealMatrix getCorrelationMatrix() {
        return correlationMatrix;
    }
    
	
    /**
     * Computes Tau AP rank correlation matrix for the columns of
     * the input matrix.
     *
     * @param matrix matrix with columns representing variables to correlate
     * @return correlation matrix
     */
    public RealMatrix computeCorrelationMatrix(final RealMatrix matrix) {
        int nVars = matrix.getColumnDimension();
        RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);
        for (int i = 0; i < nVars; i++) {
            for (int j = 0; j < i; j++) {
                double corr = correlation(matrix.getColumn(i), matrix.getColumn(j));
                outMatrix.setEntry(i, j, corr);
                outMatrix.setEntry(j, i, corr);
            }
            outMatrix.setEntry(i, i, 1d);
        }
        return outMatrix;
    }
    
    /**
     * Computes the Tau AP rank correlation matrix for the columns of
     * the input rectangular array.  The columns of the array represent values
     * of variables to be correlated.
     *
     * @param matrix matrix with columns representing variables to correlate
     * @return correlation matrix
     */
    public RealMatrix computeCorrelationMatrix(final double[][] matrix) {
       return computeCorrelationMatrix(new BlockRealMatrix(matrix));
    }
    
	
    /**
     * Computes the Tau AP rank correlation coefficient between the two arrays.
     *
     * @param xArray first data array
     * @param yArray second data array
     * @return Returns Kendall's Tau rank correlation coefficient for the two arrays
     * @throws DimensionMismatchException if the arrays lengths do not match
     */
    public double correlation(final double[] xArray, final double[] yArray)
            throws DimensionMismatchException {

        if (xArray.length != yArray.length) {
            throw new DimensionMismatchException(xArray.length, yArray.length);
        }

        final int n = xArray.length;


        int[] xArrayIndices = IntStream.range(0, xArray.length)
                .boxed().sorted((i, j) -> (new Double(xArray[i])).compareTo(xArray[j])*-1 )
                .mapToInt(ele -> ele).toArray();
        int[] yArrayIndices = IntStream.range(0, yArray.length)
                .boxed().sorted((i, j) -> (new Double(yArray[i])).compareTo(yArray[j])*-1 )
                .mapToInt(ele -> ele).toArray();
        
        double sum = 0.0;
        double previous = 0;
        for (int i = 1; i < xArrayIndices.length; i++) {
        	double value = (xArrayIndices[i-1] == yArrayIndices[i-1]) ? 1 : 0;
			sum += (value + previous) / i;
			previous += value;
		}
        
        return (2d/(n-1)) * sum-1;
    }
    
  
    
    public static void main(String[] args){
    	TauApCorrelation tauApCorrelation = new TauApCorrelation();
    	double[] list1 = {5,4,3,2,1,1,1,1,1,1};
    	double[] list2 = {6,5,4,2,1,1,1,1,1,3};
    	System.out.println(tauApCorrelation.correlation(list1,list2));
    	double[] list3 = {5,4,3,2,1};
    	double[] list4 = {5,4,2,3,0};
    	System.out.println(tauApCorrelation.correlation(list3,list4));
    	System.out.println(tauApCorrelation.correlation(list4,list3));
    	double[] list5 = {5,4,3,2,1};
    	double[] list6 = {5,3,4,2,1};
    	System.out.println(tauApCorrelation.correlation(list5,list6));
    	double[] list7 = {5,4,3,2,1,1,1,1,1,1,1,1,1,1,1};
    	double[] list8 = {4,10,3,2,1,1,1,1,1,1,1,1,1,1,1};
    	System.out.println(tauApCorrelation.correlation(list7,list8));
    }
}
