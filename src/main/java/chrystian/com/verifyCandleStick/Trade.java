package chrystian.com.verifyCandleStick;

import org.json.JSONObject;

public class Trade {

	
	/*
		p	number	Trade price
		q	number	Trade quantity
		s	string	Side ("buy" or "sell")
		d	number	Trade ID
		t	number	Trade timestamp
	 * */
	
	private double price;
	private double quantity;
	private String side;
	private long id;
	private long timestamp;
	
	private boolean valid = true;
	

	public Trade(JSONObject object) {
		try {
			price = object.getDouble("p");
			quantity = object.getDouble("q");
			side = object.getString("s");
			id = object.getLong("d");
			timestamp = object.getLong("t");
			
		}catch(Exception e) {
			valid = false;
		}
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isValid() {
		return valid;
	}
	
	
}
