package fi.hut.soberit.agilefant.web;


import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
//import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.DeliverableDAO;
import fi.hut.soberit.agilefant.db.IterationDAO;
import fi.hut.soberit.agilefant.db.PerformedWorkDAO;
import fi.hut.soberit.agilefant.db.TaskDAO;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.PerformedWork;



public class ChartAction extends ActionSupport {
	private static final Log log = LogFactory.getLog(ChartAction.class);
	
	private byte[] result;
	private PerformedWork performedWork;
	private int taskId;
	private int backlogItemId;
	private int iterationId;
	private int deliverableId;
	private TaskDAO taskDAO;
	private BacklogItemDAO backlogItemDAO;
	private IterationDAO iterationDAO;
	private DeliverableDAO deliverableDAO;
	private PerformedWorkDAO performedWorkDAO;
	private Collection<PerformedWork> works;
	private int workDone;
	private double effortDone;
	private double effortLeft;


	public String execute(){
//		 Create a time series chart
		
		if (taskId > 0){
			works = performedWorkDAO.getPerformedWork(taskDAO.get(taskId));
		} else if (backlogItemId > 0){
			works = performedWorkDAO.getPerformedWork(backlogItemDAO.get(backlogItemId));
		} else if (iterationId > 0){
			works = performedWorkDAO.getPerformedWork(iterationDAO.get(iterationId));
		} else if (deliverableId > 0){
			works = performedWorkDAO.getPerformedWork(deliverableDAO.get(deliverableId));
		}
		
		log.info(taskId);
		
		/*-------------------------------------------------------------*/
		// The code for dataset: actual workhours
		
		TimeSeries workSeries = new TimeSeries("Workhours", Day.class);
			
		int day_last=0;
		int month_last=0;
		int year_last=0;
		int worksum=0;
		int count=0;
		int worktime=0;
		
		// The date for the first work effort
		int day_f=0;
		int month_f=0;
		int year_f=0;
		double totalWorkDone=0;		
		
		for(PerformedWork performedWork : works){
			
			AFTime effort = performedWork.getEffort();
			if(effort!=null){
				long time = effort.getTime();
				long days = time / AFTime.WORKDAY_IN_MILLIS;
				time %= AFTime.WORKDAY_IN_MILLIS;
				
				long hours = time / AFTime.HOUR_IN_MILLIS;
				time %= AFTime.HOUR_IN_MILLIS;
				
				long minutes = time / AFTime.MINUTE_IN_MILLIS;
				time %= AFTime.MINUTE_IN_MILLIS;
				
				worktime = Math.round(days * 24 + hours + (minutes/60));
				Date date = performedWork.getCreated();
				//String dateStr = date.toString(); // for debugging purposes
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH) + 1; // January == 0
				int year = calendar.get(Calendar.YEAR);
				count++;
				
//				 day changed, time to pop the previous days hours
				if ((day!=day_last || month != month_last || year!=year_last) && (count > 1)){
					workSeries.add(new Day(day_last, month_last, year_last), worksum);
					//worksum=0; Can be used if the effort is wanted per day
				}else if(count==1){
					day_f=day;
					month_f=month;
					year_f=year;
				}
				
				worksum=worksum+worktime;
				day_last=day;
				month_last=month;
				year_last=year;
			}		
			
		}
		if(worksum > 0){ // pop the last days hours
			workSeries.add(new Day(day_last, month_last, year_last), worksum);
			totalWorkDone=worksum;
		}
		
		Date d1 = new GregorianCalendar(year_f, month_f, day_f, 00, 00).getTime();
		Date d2 = new GregorianCalendar(year_last, month_last, day_last, 00, 00).getTime();
		long diff = d2.getTime() - d1.getTime();
		double usedCalendarDays = (double)(diff / (1000 * 60 * 60 * 24));

		
		double averageDailyProgress = totalWorkDone/usedCalendarDays;
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		/*-- Shows the done workhours
		//dataset.addSeries(workSeries); 
		
		/*-------------------------------------------------------------*/
		// The code for dataset: effort estimates
		// we only want to keep the last estimate for the day
		
		TimeSeries estimateSeries = new TimeSeries("Effort estimates", Day.class);
		TimeSeries trendSeries = new TimeSeries("Estimated progress", Day.class);
		
		day_last=0;
		month_last=0;
		year_last=0;
		worksum=0;
		count=0;	
		worktime=0;
		
		
		for(PerformedWork performedWork : works){
			
			AFTime effort = performedWork.getNewEstimate();
			
			if(effort!=null){
				
				long time = effort.getTime();
				long days = time / AFTime.WORKDAY_IN_MILLIS;
				time %= AFTime.WORKDAY_IN_MILLIS;
				
				long hours = time / AFTime.HOUR_IN_MILLIS;
				time %= AFTime.HOUR_IN_MILLIS;
				
				long minutes = time / AFTime.MINUTE_IN_MILLIS;
				time %= AFTime.MINUTE_IN_MILLIS;
				
				worktime = Math.round(days * 24 + hours + (minutes/60));
				Date date = performedWork.getCreated();
				//String dateStr = date.toString(); // for debugging purposes
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH) + 1; // January == 0
				int year = calendar.get(Calendar.YEAR);
				count++;
				
				// day changed, time to pop the previous days hours
				if ((day!=day_last || month != month_last || year!=year_last) && (count > 1)){
					estimateSeries.add(new Day(day_last, month_last, year_last), worksum);
					worksum=0;
				}
				
				worksum=worktime; // No summing up
				day_last=day;
				month_last=month;
				year_last=year;		
				
			}	
		}
		if(worksum > 0){ // pop the last days hours
			estimateSeries.add(new Day(day_last, month_last, year_last), worksum);		
		}
		
		dataset.addSeries(estimateSeries);
		
		// Variables that are used to account date setting to the trend line
		double workRemaining = worksum;
		int day_tr = day_last;
		int month_tr = month_last;
		int year_tr = year_last;
		
		if(workRemaining>0){ // To avoid a gap in the graph
			trendSeries.add(new Day(day_tr, month_tr, year_tr), workRemaining);
		}
		
		while(workRemaining>0){
			workRemaining = workRemaining - averageDailyProgress;
			if(workRemaining<0){
				workRemaining=0;
			}
			
			if(month_tr == 2 && day_tr==29){
				month_tr=3;
				day_tr=1;
			}else if(month_tr==12 && day_tr==31){
				day_tr=1;
				month_tr=1;
				year_tr++;
			}else if((month_tr==4 || month_tr==6 || month_tr==9 || month_tr==11) && (day_tr==30)){
				day_tr=1;
				month_tr++;
			}else if((month_tr==1 || month_tr==3 || month_tr==5 || month_tr==7 || month_tr==8 || month_tr==10) && (day_tr==31)){
				day_tr=1;
				month_tr++;
			}else {
				day_tr++;
			}
			trendSeries.add(new Day(day_tr, month_tr, year_tr), workRemaining);
		}
		dataset.addSeries(trendSeries);
		
		/*-------------------------------------------------------------*/
		
		JFreeChart chart1 = ChartFactory.createTimeSeriesChart(
		"Agilefant07 workhours per day",
		"Date",
		"Workhours",
		dataset,
		true,
		true,
		false);
		XYPlot plot = chart1.getXYPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
		
		XYItemRenderer rend = plot.getRenderer();
		XYLineAndShapeRenderer rr = (XYLineAndShapeRenderer)rend;
		rr.setShapesVisible(true);
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(out, chart1, 500, 300);
			result = out.toByteArray();		
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
		return Action.SUCCESS;
	}
	
	
	// The code for bar chart that will check the work done automatically 
	/*
	public String barChart(){
		
		
				
		if (taskId > 0){
			works = performedWorkDAO.getPerformedWork(taskDAO.get(taskId));
		} 
		
		int worksum=0;
		int worktime=0;
		
		// Get work done total
		for(PerformedWork performedWork : works){
			
			AFTime effort = performedWork.getEffort();
			if(effort!=null){
				long time = effort.getTime();
				long days = time / AFTime.WORKDAY_IN_MILLIS;
				time %= AFTime.WORKDAY_IN_MILLIS;
				
				long hours = time / AFTime.HOUR_IN_MILLIS;
				time %= AFTime.HOUR_IN_MILLIS;
				
				long minutes = time / AFTime.MINUTE_IN_MILLIS;
				time %= AFTime.MINUTE_IN_MILLIS;
				
				worktime = Math.round(days * 24 + hours + (minutes/60));
				
				worksum=worksum+worktime;
			}
		}
		// Total sum for done hours
		int done = worksum; 
		
		int effortLeft = 0;
//		 Get effort estimate left
		for(PerformedWork performedWork : works){
			
			AFTime effort = performedWork.getNewEstimate();
			
			if(effort!=null){
				
				long time = effort.getTime();
				long days = time / AFTime.WORKDAY_IN_MILLIS;
				time %= AFTime.WORKDAY_IN_MILLIS;
				
				long hours = time / AFTime.HOUR_IN_MILLIS;
				time %= AFTime.HOUR_IN_MILLIS;
				
				long minutes = time / AFTime.MINUTE_IN_MILLIS;
				time %= AFTime.MINUTE_IN_MILLIS;
				
				effortLeft = Math.round(days * 24 + hours + (minutes/60));			
			}	
		}
		// How many hours work left
		int left = effortLeft;
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		JFreeChart chart2 = null;
		
		if(done > 0 || effortLeft > 0){
			double donePros = ((double)done / (double)(done+left))*100;
			dataset.setValue(donePros, "done", "");
			dataset.setValue((100 - donePros), "left", "");
			double stringPros = Math.round(donePros); // We want the output neat!
			chart2 = ChartFactory.createStackedBarChart("",
					"", ""+ stringPros +" % done", dataset, PlotOrientation.HORIZONTAL,
					true, true, false);
		} else {
			dataset.setValue(workDone, "done", "");
			dataset.setValue((100 - workDone), "left", "");
			chart2 = ChartFactory.createStackedBarChart("",
					"", ""+ workDone +" % done", dataset, PlotOrientation.HORIZONTAL,
					true, true, false);
		}
		
		
		
		CategoryPlot plot = chart2.getCategoryPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesItemLabelsVisible(0, false);
		renderer.setSeriesPaint(0, Color.green);
		renderer.setSeriesPaint(1, Color.red);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(out, chart2, 200, 100);
			result = out.toByteArray();		
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
		return Action.SUCCESS;
	}
	*/
	//-----------------
	
	
	
	
	/**
	 * Bar chart takes two parameters, effort done and effort left. 
	 * The procentage of work compleated is calculated based on theses two numbers.
	 * Finally the bar chart is returned back as a png-image.
	 * 
	 * @param effortDone
	 * @param effortLeft
	 * @return
	 */
	public String barChart(){
		
		// Intitializing variables
		double done = 0;
		double left = 0;
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		JFreeChart chart2 = null;
		
		
		if(effortDone > 0){
			done = effortDone;
		}
		
		if(effortLeft > 0){
			left = effortLeft;
		}

		// We want to avoid division by zero
		if(done > 0 || left > 0){ // Two parameters chart
			double donePros = ((double)done / (double)(done+left))*100;
			dataset.setValue(donePros, "done", "");
			dataset.setValue((100 - donePros), "left", "");
			double stringPros = Math.round(donePros); // We want the output neat!
			chart2 = ChartFactory.createStackedBarChart("",
					"", ""+ stringPros +" % done", dataset, PlotOrientation.HORIZONTAL,
					true, true, false);
		} else { // Chart based on one "workDone" parameter
			dataset.setValue(workDone, "done", "");
			dataset.setValue((100 - workDone), "left", "");
			chart2 = ChartFactory.createStackedBarChart("",
					"", ""+ workDone +" % done", dataset, PlotOrientation.HORIZONTAL,
					true, true, false);
		}
		
		CategoryPlot plot = chart2.getCategoryPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesItemLabelsVisible(0, false);
		renderer.setSeriesPaint(0, Color.green);
		renderer.setSeriesPaint(1, Color.red);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(out, chart2, 200, 100);
			result = out.toByteArray();		
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
		return Action.SUCCESS;
	}	
	
	// The code under this line creates custom chart for demo purposes
	/* 
	public String demoChart(){
		TimeSeries pop = new TimeSeries("Workhours", Day.class);
		pop.add(new Day(11, 12, 2006), 11);
		pop.add(new Day(12, 12, 2006), 21);
		pop.add(new Day(13, 12, 2006), 36);
		pop.add(new Day(14, 12, 2006), 53);
		pop.add(new Day(15, 12, 2006), 53);
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		//dataset.addSeries(pop);
		
		TimeSeries hip = new TimeSeries("Effort estimates", Day.class);
		
		hip.add(new Day(11, 12, 2006), 95);
		hip.add(new Day(12, 12, 2006), 75);
		hip.add(new Day(13, 12, 2006), 50);
		hip.add(new Day(14, 12, 2006), 40);
		hip.add(new Day(15, 12, 2006), 0);
		
		dataset.addSeries(hip);
		
		JFreeChart chart1 = ChartFactory.createTimeSeriesChart(
				"Agilefant07 workhours per day",
				"Date",
				"Workhours",
				dataset,
				true,
				true,
				false);
				XYPlot plot = chart1.getXYPlot();
				DateAxis axis = (DateAxis) plot.getDomainAxis();
				axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
				
				XYItemRenderer rend = plot.getRenderer();
				XYLineAndShapeRenderer rr = (XYLineAndShapeRenderer)rend;
				rr.setShapesVisible(true);
				
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ChartUtilities.writeChartAsPNG(out, chart1, 500, 300);
					result = out.toByteArray();		
				} catch (IOException e) {
					System.err.println("Problem occurred creating chart.");
				}
				return Action.SUCCESS;
	}
	*/
	
	public InputStream getInputStream(){
		return new ByteArrayInputStream(result);
	}

	public BacklogItemDAO getBacklogItemDAO() {
		return backlogItemDAO;
	}

	public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
		this.backlogItemDAO = backlogItemDAO;
	}

	public int getBacklogItemId() {
		return backlogItemId;
	}

	public void setBacklogItemId(int backlogItemId) {
		this.backlogItemId = backlogItemId;
	}

	public DeliverableDAO getDeliverableDAO() {
		return deliverableDAO;
	}

	public void setDeliverableDAO(DeliverableDAO deliverableDAO) {
		this.deliverableDAO = deliverableDAO;
	}

	public int getDeliverableId() {
		return deliverableId;
	}

	public void setDeliverableId(int deliverableId) {
		this.deliverableId = deliverableId;
	}

	public IterationDAO getIterationDAO() {
		return iterationDAO;
	}

	public void setIterationDAO(IterationDAO iterationDAO) {
		this.iterationDAO = iterationDAO;
	}

	public int getIterationId() {
		return iterationId;
	}

	public void setIterationId(int iterationId) {
		this.iterationId = iterationId;
	}

	public PerformedWork getPerformedWork() {
		return performedWork;
	}

	public void setPerformedWork(PerformedWork performedWork) {
		this.performedWork = performedWork;
	}

	public PerformedWorkDAO getPerformedWorkDAO() {
		return performedWorkDAO;
	}

	public void setPerformedWorkDAO(PerformedWorkDAO performedWorkDAO) {
		this.performedWorkDAO = performedWorkDAO;
	}

	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] result) {
		this.result = result;
	}

	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public Collection<PerformedWork> getWorks() {
		return works;
	}

	public void setWorks(Collection<PerformedWork> works) {
		this.works = works;
	}
	
	public int getWorkDone() {
		return workDone;
	}

	public void setWorkDone(int workDone) {
		this.workDone = workDone;
	}


	public double getEffortDone() {
		return effortDone;
	}


	public void setEffortDone(double effortDone) {
		this.effortDone = effortDone;
	}


	public double getEffortLeft() {
		return effortLeft;
	}


	public void setEffortLeft(double effortLeft) {
		this.effortLeft = effortLeft;
	}


}
