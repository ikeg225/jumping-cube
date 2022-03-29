package jump61;

import static jump61.Side.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of Boards.
 *  @author Ethan Ikegami
 */

public class BoardTest {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testSize() {
        Board B = new Board(5);
        assertEquals("bad length", 5, B.size());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        Board D = new Board(C);
        assertEquals("bad length", 5, D.size());
    }

    @Test
    public void testSet() {
        Board B = new Board(5);
        B.set(2, 2, 1, RED);
        assertEquals("wrong number of spots", 1, B.get(2, 2).getSpots());
        assertEquals("wrong color", RED, B.get(2, 2).getSide());
        assertEquals("wrong count", 1, B.numOfSide(RED));
        assertEquals("wrong count", 0, B.numOfSide(BLUE));
        assertEquals("wrong count", 24, B.numOfSide(WHITE));
    }

    @Test
    public void testMove() {
        Board B = new Board(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        B.undo();
        checkBoard("#2U", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.undo();
        checkBoard("#1U", B, 1, 1, 2, RED);
        B.undo();
        checkBoard("#0U", B);
    }

    @Test
    public void boardCreation() {
        Board B = new Board();
        assertEquals(36, B.numSquares());
        assertEquals(6, B.size());

        Board C = new Board(9);
        assertEquals(81, C.numSquares());
        assertEquals(9, C.size());
    }

    @Test
    public void setTest() {
        Board B = new Board();
        B.set(1, 1, 2, RED);
        B.set(3, 6, 2, BLUE);

        Board C = new Board(9);
        C.set(1, 1, 2, RED);
        C.set(2, 5, 3, BLUE);
        String a = "===\n"
                +
                "    2r 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 3b 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "===";
        assertEquals(a, C.toString());
    }

    @Test (expected = GameException.class)
    public void outofBoundsSmallRow() {
        Board C = new Board(9);
        C.set(0, 1, 2, RED);
    }

    @Test (expected = GameException.class)
    public void outofBoundsLargeCol() {
        Board C = new Board(9);
        C.set(1, 20, 2, RED);
    }

    @Test (expected = GameException.class)
    public void outofBoundsSmallCol() {
        Board C = new Board(9);
        C.set(1, -4, 2, RED);
    }

    @Test (expected = GameException.class)
    public void outofBoundsLargeRow() {
        Board C = new Board(9);
        C.set(23, 1, 2, RED);
    }

    @Test
    public void internalCopy() {
        Board C = new Board(9);
        String cPrint = C.toString();

        Board B = new Board(9);
        B.internalCopy(C);
        assertEquals(true, B.equals(C));
        B.set(3, 3, 3, RED);
        assertEquals(cPrint, C.toString());
    }

    @Test
    public void equalsTest() {
        Board C = new Board(9);
        C.set(2, 3, 2, RED);
        C.set(4, 5, 3, BLUE);

        Board B = new Board(9);
        B.set(2, 3, 2, RED);
        B.set(4, 5, 3, BLUE);

        Board A = new Board(10);
        A.set(2, 3, 2, RED);
        A.set(4, 5, 3, BLUE);

        Board D = new Board(8);
        D.set(2, 3, 2, RED);
        D.set(4, 5, 3, BLUE);

        assertEquals(true, C.equals(B));
        assertEquals(false, C.equals(A));
        assertEquals(false, C.equals(D));
    }

    @Test
    public void addSpotTest() {
        Board C = new Board(9);
        C.addSpot(RED, 2, 3);
        C.addSpot(BLUE, 3);
        String a = "===\n"
                +
                "    1- 1- 1- 2b 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 2r 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1- 1- 1- 1-\n"
                +
                "===";
        assertEquals(a, C.toString());
    }

    @Test
    public void numSideTest() {
        Board C = new Board(9);
        C.addSpot(RED, 2, 3);
        C.addSpot(BLUE, 9);
        C.addSpot(RED, 2, 4);
        C.addSpot(BLUE, 23);
        C.addSpot(RED, 8, 9);
        assertEquals(76, C.numOfSide(WHITE));
        assertEquals(3, C.numOfSide(RED));
        assertEquals(2, C.numOfSide(BLUE));
    }

    @Test
    public void historyAndUndo() {
        Board C = new Board(9);

        C.addSpot(RED, 21);
        C.addSpot(BLUE, 24);
        String a = C.toString();
        C.addSpot(RED, 54);
        C.addSpot(BLUE, 56);
        C.addSpot(RED, 42);
        C.undo();
        C.undo();
        C.undo();
        String b = C.toString();
        assertEquals(true, a.equals(b));
    }

    @Test
    public void clearTest() {
        Board C = new Board(9);

        C.addSpot(RED, 21);
        C.addSpot(BLUE, 24);
        C.markUndo();
        C.addSpot(RED, 54);
        C.markUndo();
        C.addSpot(BLUE, 56);
        C.markUndo();
        C.addSpot(RED, 42);
        C.undo();
        C.clear(8);
        Board B = new Board(8);
        assertEquals(true, B.equals(C));
        assertEquals(true, C.getHistory().isEmpty());
    }

    @Test
    public void neighborJumpIndexTest() {
        Board C = new Board(4);
        int[] a = C.neighborJumpIndex(0);
        int[] b = C.neighborJumpIndex(3);
        int[] c = C.neighborJumpIndex(12);
        int[] d = C.neighborJumpIndex(15);

        assertArrayEquals(a, new int[]{1, 4});
        assertArrayEquals(b, new int[]{2, 7});
        assertArrayEquals(c, new int[]{13, 8});
        assertArrayEquals(d, new int[]{14, 11});

        int[] a2 = C.neighborJumpIndex(1);
        int[] b2 = C.neighborJumpIndex(14);
        int[] c2 = C.neighborJumpIndex(8);
        int[] d2 = C.neighborJumpIndex(7);

        assertArrayEquals(a2, new int[]{0, 2, 5});
        assertArrayEquals(b2, new int[]{13, 15, 10});
        assertArrayEquals(c2, new int[]{4, 12, 9});
        assertArrayEquals(d2, new int[]{3, 11, 6});

        int[] e = C.neighborJumpIndex(5);
        assertArrayEquals(e, new int[]{4, 6, 1, 9});
    }

    @Test
    public void jumpTest() {
        Board C = new Board();
        C.addSpot(RED, 1, 1);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 1, 2);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 1, 2);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 2, 1);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 2, 1);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 2, 2);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 2, 2);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 2, 2);
        C.addSpot(BLUE, 6, 6);
        C.addSpot(RED, 2, 2);

        String a = "===\n"
                +
                "    2r 2r 2r 1- 1- 1-\n"
                +
                "    2r 3r 2r 1- 1- 1-\n"
                +
                "    2r 2r 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 2b\n"
                +
                "    1- 1- 1- 1- 3b 3b\n"
                +
                "    1- 1- 1- 2b 3b 1b\n"
                +
                "===";
        assertEquals(a, C.toString());
    }

    @Test
    public void fullGame() {
        try {
            Board D = new Board(3);
            D.addSpot(RED, 1, 1);
            D.addSpot(BLUE, 3, 3);
            D.addSpot(RED, 1, 2);
            D.addSpot(BLUE, 3, 3);
            D.addSpot(RED, 1, 3);
            D.addSpot(BLUE, 3, 1);
            D.addSpot(RED, 2, 1);
            D.addSpot(BLUE, 2, 2);
            D.addSpot(RED, 2, 1);
            D.addSpot(BLUE, 3, 1);
            D.addSpot(RED, 1, 3);
            D.addSpot(BLUE, 3, 1);
        } catch (GameException re) {
            String a = "===\n"
                    +
                    "    1b 3b 1b\n"
                    +
                    "    1b 2b 3b\n"
                    +
                    "    2b 3b 1b\n"
                    +
                    "===";
            assertEquals(a, re.getMessage());
        }
    }

    @Test
    public void basic2() {
        Board A = new Board();
        A.set(1, 1, 2, RED);
        A.set(6, 1, 1, RED);
        A.set(1, 6, 2, BLUE);
        A.set(6, 6, 1, BLUE);
        String a = "===\n"
                +
                "    2r 1- 1- 1- 1- 2b\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1r 1- 1- 1- 1- 1b\n"
                +
                "===";
        assertEquals(a, A.toString());
    }

    @Test
    public void basic3() {
        Board A = new Board();
        A.set(1, 2, 1, BLUE);
        A.set(1, 1, 2, RED);
        String a = "===\n"
                +
                "    2r 1b 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "===";
        assertEquals(a, A.toString());
        A.clear(A.size());
        String b = "===\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "    1- 1- 1- 1- 1- 1-\n"
                +
                "===";
        assertEquals(b, A.toString());
    }

    @Test
    public void random() {
        Board a = new Board(2);
        a.addSpot(RED, 0);
        a.addSpot(BLUE, 2);
        System.out.println(a);
        System.out.println(a.getBlue());
        System.out.println(a.getRed());
    }

    /** Checks that B conforms to the description given by CONTENTS.
     *  CONTENTS should be a sequence of groups of 4 items:
     *  r, c, n, s, where r and c are row and column number of a square of B,
     *  n is the number of spots that are supposed to be there and s is the
     *  color (RED or BLUE) of the square.  All squares not listed must
     *  be WHITE with one spot.  Raises an exception signaling a unit-test
     *  failure if B does not conform. */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                                     contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                       (B.get(i).getSide() != WHITE)
                       || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }
}
