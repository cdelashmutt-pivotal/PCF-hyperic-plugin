package com.pivotal.cloudfoundry.monitoring.hyperic;

import javax.management.AttributeNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.MeasurementPlugin;
import org.hyperic.hq.product.Metric;
import org.hyperic.hq.product.MetricNotFoundException;
import org.hyperic.hq.product.MetricUnreachableException;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.product.PluginException;

/**
 * This class is called by the Hyperic framework whenever a metric needs to
 * be collected. 
 * 
 * @since 1.4.X
 *
 */
public class Measurement extends MeasurementPlugin {
	/**
	 * Logger
	 */
    private static Log log = LogFactory.getLog(Measurement.class.getName());
    
    /**
     * This method retrieves the metric value for the received metric. Its queries
     * the JMX endpoint to get the value of the metric.
     */
    @Override
    public MetricValue getValue(Metric metric) throws PluginException, MetricNotFoundException, MetricUnreachableException {
        log.debug("[getValue]metric" + metric);
        MetricValue metricValue = null;
        
        if (metric.isAvail()){
        	if (metric.toString().equals("CF:Availability")){
        		if (JMXClient.getInstance().isConnected())
        			return new MetricValue(Metric.AVAIL_UP);
        		else return new MetricValue(Metric.AVAIL_DOWN);
        	}
        	String property = metric.toString().replaceAll("Availability", "healthy");
        	try {
				double up = JMXClient.getInstance().getPropertyValue(property);
				if (up>0) return new MetricValue(Metric.AVAIL_UP);
				return new MetricValue(Metric.AVAIL_DOWN);
				
			} catch (AttributeNotFoundException e) {
				log.warn("Attribute HEALTHY not found for " + property+". Assuming its available");
				return new MetricValue(Metric.AVAIL_UP);
				//e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
        	
        	return new MetricValue(Metric.AVAIL_UP); 
        }
        
        if (!metric.toString().startsWith("org.cloudfoundry")) return new MetricValue(1d);
        else{
	        try{
	        	log.info(">>>>WILL GET VALUE VALUE OF METRIC: " + metric);
	        	double value = JMXClient.getInstance().getPropertyValue(metric.toString());
	        	log.info(">>>>VALUE IS: " + value);
	        	
	        	metricValue = new MetricValue(value); 
	        }
	        catch(Exception e){
	        	e.printStackTrace();
	        	metricValue=new MetricValue(0);
	        }
        }
        return metricValue;
    }
}