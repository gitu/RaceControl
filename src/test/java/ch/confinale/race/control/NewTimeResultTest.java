package ch.confinale.race.control;

import ch.confinale.race.control.results.NewTimeResult;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class NewTimeResultTest  {

    @Test
    public void testSwap() {
        assertEquals(0x0A, NewTimeResult.swap((byte) 0x55));

    }

    @Test
    public void testTimeParsing() {
        // http://www.freeslotter.de/board25-tipps-erfahrungen-und-zubeh%F6r/board49-digitales-slotracing/board93-carrera-digital-132/64656-datenwort-rundenz%E4hler/#post668196
        assertEquals("expected hex: 0373EF", 226287, new NewTimeResult("?2003037?>1=".getBytes(StandardCharsets.US_ASCII)).getTime());
        assertEquals("expected hex: 039A12", 236050, new NewTimeResult("?20030:9211<".getBytes(StandardCharsets.US_ASCII)).getTime());
        assertEquals("expected hex: 03C16F", 246127, new NewTimeResult("?200301<?618".getBytes(StandardCharsets.US_ASCII)).getTime());
    }

    @Test
    public void testCarParser() throws Exception {
        assertEquals(2, new NewTimeResult("?20000=49>1;".getBytes(StandardCharsets.US_ASCII)).getCarNr());
    }

}