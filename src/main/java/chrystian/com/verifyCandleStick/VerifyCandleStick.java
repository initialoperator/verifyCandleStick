package chrystian.com.verifyCandleStick;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class VerifyCandleStick {

	@Autowired
	static
	RestTemplate restTemplate;
	
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		
		//scanner read to get instrument and period 
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Please enter the instrument name: ");
		String instrument = scanner.nextLine();
		System.out.println("Please enter the time period (number in minute): ");
		int period = scanner.nextInt();
		
		scanner.close();
		
		//Alternative hardcoded way to provided instrument name and period
//		String instrument = "ICX_CRO";
//		int period = "1";
		

		/* get candlesticks and trades from local mocked data */
//		List<CandleStick> candleSticks = getCandleSticks(instrument, period);
//		List<Trade> trades = getTrades(instrument);
		
		/* get candlesticks and trades from remote webservices */
		List<CandleStick> candleSticks = getCandleSticksFromRemote(instrument, period);
		List<Trade> trades = getTradesFromRemote(instrument);
		
		/* 
		 * parse the trades and conclude candle sticks from the trades
		 * 	-first determine the time stamp trunks
		 * 	-group trades within a trunk.
		 *  -for each trunk of trades, get the O,C,H and L -> produce a candle stick
		 *  -compare the candle stick with the queries ones
		 *  -..... anything extra
		 *  */
		
		List<CandleStick> cssDerived = getCandleSticksFromTrades(trades, period);
		
		List<CandleStick> inconsistences = getInconsistentCandleSticks(candleSticks, cssDerived);
		
		//printing out result:
		System.out.println("Inconsistent candle sticks: ");
		inconsistences.stream().forEach(c -> {
			System.out.println("Timestamp: " + c.getTimestamp()
								+ " Open: " + c.getOpen()
								+ " Close: "+ c.getClose()
								+ " High: " + c.getHigh()
								+ " Low: " + c.getLow() +"\r\n");
		});
		
	}
	
	protected static List<CandleStick> getInconsistentCandleSticks(List<CandleStick> data, List<CandleStick> standard){
		Map<Long, CandleStick> dict = data.stream().collect(Collectors.toMap(CandleStick::getTimestamp, e->e));
		return standard.stream().map(s -> {
				long key = s.getTimestamp();
				if(dict.get(key) == null) {
					CandleStick missing = new CandleStick(null);//to return a candlesitck with isValid == false;	
					missing.setClose(s.getClose());
					missing.setHigh(s.getHigh());
					missing.setLow(s.getLow());
					missing.setOpen(s.getOpen());
					missing.setTimestamp(s.getTimestamp());
					return missing;
				}else {
					CandleStick cs = dict.get(key);
					if(!cs.identical(s))
						return cs;
					else
						//to return a candlesitck with isValid == false and timestamp == 0L: to be filtered out
						return new CandleStick(null);		
				}
			}).filter(c -> c.getTimestamp() > 0L).sorted((c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()))
				.collect(Collectors.toList());
			
	}
	protected static List<CandleStick> getCandleSticksFromTrades(List<Trade> trades, int timeFrame){
		//60000 milliseconds in a minute
		final long minute = 60000L;
		Map<Long, List<Trade>> tradesByMin = new HashMap<>();
		trades.stream().forEach(t -> {
							long timestamp = t.getTimestamp();
							long key = (timestamp/minute+timeFrame) * minute;
							if(key < System.currentTimeMillis()) { //ignoring the current (incomplete) minute
								if(tradesByMin.get(key) == null) {
									List<Trade> l = new ArrayList<>();
									l.add(t);
									tradesByMin.put(key, l);
								}else
									tradesByMin.get(key).add(t);	
							}
						});
		
		List<CandleStick> result = tradesByMin.entrySet().stream().sorted((e1, e2) -> Long.compare(e1.getKey(), e2.getKey()))
				.map(e -> {
					List<Trade> tradesSorted = e.getValue().stream()
							.sorted((t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp())).collect(Collectors.toList());
					double high = tradesSorted.stream().map(Trade::getPrice).max((d1, d2) -> Double.compare(d1, d2)).get();
					double low= tradesSorted.stream().map(Trade::getPrice).min((d1, d2) -> Double.compare(d1, d2)).get();
					double open = tradesSorted.get(0).getPrice();
					double close = tradesSorted.get(tradesSorted.size()-1).getPrice();
										
					CandleStick cs = new CandleStick();
					cs.setClose(close);
					cs.setHigh(high);
					cs.setLow(low);
					cs.setOpen(open);
					cs.setTimestamp(e.getKey());
					//TODO: volume is omitting for now
					
					return cs;
				}).collect(Collectors.toList());
								
		return result;
	}
	
	protected static List<CandleStick> getCandleSticks(String instrument, int period) {
		String data = "{result:[\n"
				+ "      {\"t\":1596944700000,\"o\":11752.38,\"h\":11754.77,\"l\":11746.65,\"c\":11753.64,\"v\":3.694583},\n"
				+ "      {\"t\":1596945000000,\"o\":11753.63,\"h\":11754.77,\"l\":11739.83,\"c\":11746.17,\"v\":2.073019},\n"
				+ "      {\"t\":1596945300000,\"o\":11746.16,\"h\":11753.24,\"l\":11738.1,\"c\":11740.65,\"v\":0.867247}\n"
				+ "    ]}";

		return convertJSONToCandleSticks(data);
	}
	
	protected static List<Trade> getTrades(String instrument) {
		
		String data = "{result: [\n"
				+ "    {\"dataTime\":1591710781947,\"d\":465533583799589409,\"s\":\"BUY\",\"p\":2.96,\"q\":16.0,\"t\":1591710781946,\"i\":\"ICX_CRO\"},\n"
				+ "    {\"dataTime\":1591707701899,\"d\":465430234542863152,\"s\":\"BUY\",\"p\":0.007749,\"q\":115.0,\"t\":1591707701898,\"i\":\"VET_USDT\"},\n"
				+ "    {\"dataTime\":1591710786155,\"d\":465533724976458209,\"s\":\"SELL\",\"p\":25.676,\"q\":0.55,\"t\":1591710786154,\"i\":\"XTZ_CRO\"},\n"
				+ "    {\"dataTime\":1591710783300,\"d\":465533629172286576,\"s\":\"SELL\",\"p\":2.9016,\"q\":0.6,\"t\":1591710783298,\"i\":\"XTZ_USDT\"},\n"
				+ "    {\"dataTime\":1591710784499,\"d\":465533669425626384,\"s\":\"SELL\",\"p\":2.7662,\"q\":0.58,\"t\":1591710784498,\"i\":\"EOS_USDT\"},\n"
				+ "    {\"dataTime\":1591710784700,\"d\":465533676120104336,\"s\":\"SELL\",\"p\":243.21,\"q\":0.01647,\"t\":1591710784698,\"i\":\"ETH_USDT\"},\n"
				+ "    {\"dataTime\":1591710786600,\"d\":465533739878620208,\"s\":\"SELL\",\"p\":253.06,\"q\":0.00516,\"t\":1591710786598,\"i\":\"BCH_USDT\"},\n"
				+ "    {\"dataTime\":1591710786900,\"d\":465533749959572464,\"s\":\"BUY\",\"p\":0.9999,\"q\":0.2,\"t\":1591710786898,\"i\":\"USDC_USDT\"},\n"
				+ "    {\"dataTime\":1591710787500,\"d\":465533770081010000,\"s\":\"BUY\",\"p\":3.159,\"q\":1.65,\"t\":1591710787498,\"i\":\"ATOM_USDT\"}\n"
				+ "  ]}";

		return convertJSONToTrades(data);
	}
	
	protected static List<CandleStick> convertJSONToCandleSticks(String data){
		JSONObject json = new JSONObject(data);
		JSONArray array = json.getJSONArray("result");
		List<CandleStick> result = IntStream.range(0, array.length()).mapToObj(array::getJSONObject)
																		.map(CandleStick::new)
																		.filter(CandleStick::isValid)
																		.collect(Collectors.toList());
		return result;
	}
	
	protected static List<Trade> convertJSONToTrades(String data){
		JSONObject json = new JSONObject(data);
		JSONArray array = json.getJSONArray("result");
		List<Trade> result = IntStream.range(0, array.length()).mapToObj(array::getJSONObject)
																		.map(Trade::new)
																		.filter(Trade::isValid)
																		.collect(Collectors.toList());
		return result;
	}
	
	protected static List<CandleStick> getCandleSticksFromRemote(String instrument, int timeFrame) throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI("http://localhost:8080/public/get-candlestick?instrumentName="+instrument+"&timeFrame="+timeFrame))
				  .timeout(Duration.ofSeconds(10))
				  .GET()
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder().build()
				  .send(request, BodyHandlers.ofString());
		if(response.statusCode() != 200)
			return new ArrayList<CandleStick>();
		String res = response.body();
		return convertJSONToCandleSticks(res);
	}
	
	protected static List<Trade> getTradesFromRemote(String instrument) throws URISyntaxException, IOException, InterruptedException  {
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI("http://localhost:8080/public/get-trades?instrumentName="+instrument))
				  .timeout(Duration.ofSeconds(10))
				  .GET()
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder().build()
				  .send(request, BodyHandlers.ofString());
		if(response.statusCode() != 200)
			return new ArrayList<Trade>();
		String res = response.body();
		return convertJSONToTrades(res);
	}
	

}
