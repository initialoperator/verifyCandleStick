package chrystian.com.verifyCandleStick;

import org.json.JSONObject;

//to receive candle stick data from API
public class CandleStick {

	/*
	t	long	End time of candlestick (Unix timestamp)
	o	number	Open
	h	number	High
	l	number	Low
	c	number	Close
	v	number	Volume
	 */
	
	private long timestamp;
	private double open;
	private double high;
	private double low;
	private double close;
	private double volume;
	
	private boolean valid = true;
	
	public CandleStick() {
		
	}
	
	public CandleStick(JSONObject data){
		try {
			timestamp = data.getLong("t");
			open = data.getDouble("o");
			high= data.getDouble("h");
			low = data.getDouble("l");
			close = data.getDouble("c");
			volume = data.getDouble("v");
		}catch (Exception e) {
			valid = false;
		}
	}
	
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}


	public boolean isValid() {
		return valid;
	}
	
	public boolean identical(CandleStick other) {
		return this.getClose() == other.getClose()
				&& this.getHigh() == other.getHigh()
				&& this.getLow() == other.getLow()
				&& this.getOpen() == other.getOpen();
	}
	
	
}
