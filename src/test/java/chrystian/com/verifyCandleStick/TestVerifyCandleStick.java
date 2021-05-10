package chrystian.com.verifyCandleStick;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestVerifyCandleStick {

	
	@Test
	public void testGetInconsistentCandleSticks() {
		CandleStick c1 = new CandleStick();
		CandleStick c2 = new CandleStick();
		CandleStick c3 = new CandleStick();

		c1.setLow(111.1);
		c1.setClose(111.2);
		c1.setClose(111.3);
		c1.setHigh(111.4);
		c1.setTimestamp(1596944700000L);
		
		c2.setLow(112.1);
		c2.setClose(112.2);
		c2.setClose(112.3);
		c2.setHigh(112.4);
		c2.setTimestamp(1596945000000L);
		
//		c3.setLow(113.1);
//		c3.setClose(113.2);
//		c3.setClose(113.3);
//		c3.setHigh(113.4);
		List<CandleStick> data = new ArrayList<>();
		data.add(c1);
		data.add(c2);
		
		CandleStick c4 = new CandleStick();
		CandleStick c5 = new CandleStick();
		CandleStick c6 = new CandleStick();
		
		c4.setLow(111.1);
		c4.setClose(111.2);
		c4.setClose(111.3);
		c4.setHigh(111.4);
		c4.setTimestamp(1596944700000L);
		
		c5.setLow(112.2);
		c5.setClose(112.2);
		c5.setClose(112.3);
		c5.setHigh(112.4);
		c5.setTimestamp(1596945000000L);
		
		c6.setLow(113.1);
		c6.setClose(113.2);
		c6.setClose(113.3);
		c6.setHigh(113.4);
		c6.setTimestamp(1596945300000L);
		List<CandleStick> standard = new ArrayList<>();
		standard.add(c4);
		standard.add(c5);
		standard.add(c6);
		
		List<CandleStick> result = VerifyCandleStick.getInconsistentCandleSticks(data, standard);
		
		Assertions.assertEquals(result.size(), 2);
		Assertions.assertEquals(result.get(0).getTimestamp(), 1596945000000L);
		Assertions.assertEquals(result.get(1).isValid(), false);
	}
	
	@Test
	public void testGetCandleSticksFromTrades() {
		Trade t1 = new Trade(null);
		Trade t2 = new Trade(null);
		Trade t3 = new Trade(null);
		
		t1.setTimestamp(1591710781946L);
		t1.setPrice(2.96);
		t2.setTimestamp(1591710783298L);
		t2.setPrice(2.9016);
		t3.setTimestamp(1591710786154L);
		t3.setPrice(25.676);
		
		List<Trade> trades = new ArrayList<>();
		trades.add(t1);
		trades.add(t2);
		trades.add(t3);
		
		List<CandleStick> css = VerifyCandleStick.getCandleSticksFromTrades(trades, 1);
		
		Assertions.assertEquals(css.size(), 1);
		CandleStick cs = css.get(0);
		Assertions.assertEquals(cs.getTimestamp(),1591710840000L);
		Assertions.assertEquals(cs.getClose(), 25.676);
		Assertions.assertEquals(cs.getLow(), 2.9016);
		Assertions.assertEquals(cs.getOpen(), 2.96);
		Assertions.assertEquals(cs.getHigh(), 25.676);
		
		
		
		
	}
}
