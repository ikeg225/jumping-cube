package jump61;

import org.junit.Test;

import static jump61.Side.*;
import static org.junit.Assert.*;
import static jump61.AI.*;

/** Unit tests of Boards.
 *  @author Ethan Ikegami
 */

public class AITest {
    @Test
    public void evalTest() {
        Board C = new Board();
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 2);
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 4);
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 4);
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 5);
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 6);
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 6);
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 1, 5);
        C.addSpot(RED, 1, 1);
        System.out.println(C);


    }
}
