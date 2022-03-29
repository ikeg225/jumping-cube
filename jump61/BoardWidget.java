package jump61;

import ucb.gui2.Pad;

import java.awt.*;
import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static jump61.Side.*;

/** A GUI component that displays a Jump61 board, and converts mouse clicks
 *  on that board to commands that are sent to the current Game.
 *  @author Ethan Ikegami
 */
class BoardWidget extends Pad {

    /** Length of the side of one square in pixels. */
    private static final int SQUARE_SIZE = 50;
    /** Width and height of a spot. */
    private static final int SPOT_DIM = 8;
    /** Minimum separation of center of a spot from a side of a square. */
    private static final int SPOT_MARGIN = 10;
    /** Width of the bars separating squares in pixels. */
    private static final int SEPARATOR_SIZE = 3;
    /** Width of square plus one separator. */
    private static final int SQUARE_SEP = SQUARE_SIZE + SEPARATOR_SIZE;

    /** Colors of various parts of the displayed board. */
    private static final Color
        NEUTRAL = Color.WHITE,
        SEPARATOR_COLOR = Color.BLACK,
        SPOT_COLOR = Color.BLACK,
        RED_TINT = new Color(255, 200, 200),
        BLUE_TINT = new Color(200, 200, 255);

    /** A new BoardWidget that monitors and displays a game Board, and
     *  converts mouse clicks to commands to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        _side = 6 * SQUARE_SEP + SEPARATOR_SIZE;
        setMouseHandler("click", this::doClick);
    }

    /* .update and .paintComponent are synchronized because they are called
     *  by three different threads (the main thread, the thread that
     *  responds to events, and the display thread).  We don't want the
     *  saved copy of our Board to change while it is being displayed. */

    /** Update my display to show BOARD.  Here, we save a copy of
     *  BOARD (so that we can deal with changes to it only when we are ready
     *  for them), and recompute the size of the displayed board. */
    synchronized void update(Board board) {
        if (board.equals(_board)) {
            return;
        }
        if (_board != null && _board.size() != board.size()) {
            invalidate();
        }
        _board = new Board(board);
        _side = _board.size() * SQUARE_SEP + SEPARATOR_SIZE;
        _widthHeight = _board.size();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        if (_board == null) {
            return;
        }
        drawCells(g);
        drawGrid(g);
        drawSpots(g);
    }

    /** Return pixel coordinates of the top of row ROW relative to window. */
    private int cy(int row) {
        return 1 + row * SQUARE_SEP;
    }

    /** Return pixel coordinates of the left side of column COL relative
     *  to window. */
    private int cx(int col) {
        return 1 + col * SQUARE_SEP;
    }

    /** Set the cell colors on G from the model. */
    private void drawCells(Graphics2D g) {
        for (int row = 0; row < _widthHeight; row += 1) {
            for (int col = 0; col < _widthHeight; col += 1) {
                if (_board.get(row + 1, col + 1).getSide().equals(RED)) {
                    g.setColor(RED_TINT);
                } else if (_board.get(row + 1, col + 1).getSide().equals(BLUE)) {
                    g.setColor(BLUE_TINT);
                } else {
                    g.setColor(NEUTRAL);
                }
                g.fillRect(cx(row), cy(col), SQUARE_SEP, SQUARE_SEP);
            }
        }
    }

    /** Draw the spots on G. */
    private void drawSpots(Graphics2D g) {
        g.setColor(SEPARATOR_COLOR);
        g.setStroke(GRIDLINE_STROKE);
        g.drawLine(cx(0), cy(0), cx(_widthHeight), cy(0));
        g.drawLine(cx(0), cy(0), cx(0), cy(_widthHeight));
        g.drawLine(cx(0), cy(_widthHeight), cx(_widthHeight), cy(_widthHeight));
        g.drawLine(cx(_widthHeight), cy(0), cx(_widthHeight), cy(_widthHeight));

        for (int row = 0; row < _widthHeight; row += 1) {
            for (int col = 0; col < _widthHeight; col += 1) {
                if (col + 1 < _widthHeight) {
                    g.drawLine(cx(col + 1), cy(row), cx(col + 1), cy(row + 1));
                }
                if (row + 1 < _widthHeight) {
                    g.drawLine(cx(col), cy(row + 1), cx(col + 1), cy(row + 1));
                }
            }
        }
    }

    /** Draw the grid lines on G. */
    private void drawGrid(Graphics2D g) {
        g.setColor(SPOT_COLOR);
        for (int i = 0; i < _board.numSquares(); i++) {
            switch (_board.getBoard()[i].getSpots()) {
            case 1:
                displaySpots(g, _board.row(i), _board.col(i), 1);
                break;
            case 2:
                displaySpots(g, _board.row(i), _board.col(i), 2);
                break;
            case 3:
                displaySpots(g, _board.row(i), _board.col(i), 3);
                break;
            case 4:
                displaySpots(g, _board.row(i), _board.col(i), 4);
                break;
            case 5:
                displaySpots(g, _board.row(i), _board.col(i), 5);
                break;
            }
        }
    }

    /** Color and display the spots on the square at row R and column C
     *  on G.  (Used by paintComponent). */
    private void displaySpots(Graphics2D g, int r, int c, int numSpots) {
        switch (numSpots) {
        case 1:
            spot(g, cy(r) - SQUARE_SEP / 2, cx(c) - SQUARE_SEP / 2);
            break;
        case 2:
            spot(g, cy(r) - (SQUARE_SEP / 3) * 2,
                    cx(c) - (SQUARE_SEP / 3) * 2);
            spot(g, cy(r) - SQUARE_SEP / 3, cx(c) - SQUARE_SEP / 3);
            break;
        case 3:
            spot(g, cy(r) - SQUARE_SEP / 2, cx(c) - SQUARE_SEP / 2);
            spot(g, cy(r) - (SQUARE_SEP / 3) * 2,
                    cx(c) - (SQUARE_SEP / 3) * 2);
            spot(g, cy(r) - SQUARE_SEP / 3, cx(c) - SQUARE_SEP / 3);
            break;
        case 4:
            spot(g, cy(r) - (SQUARE_SEP / 3) * 2,
                    cx(c) - (SQUARE_SEP / 3) * 2);
            spot(g, cy(r) - SQUARE_SEP / 3, cx(c) - SQUARE_SEP / 3);
            spot(g, cy(r) - SQUARE_SEP / 3,
                    cx(c) - (SQUARE_SEP / 3) * 2);
            spot(g, cy(r) - (SQUARE_SEP / 3) * 2, cx(c) - SQUARE_SEP / 3);
            break;
        case 5:
            spot(g, cy(r) - SQUARE_SEP / 2, cx(c) - SQUARE_SEP / 2);
            spot(g, cy(r) - (SQUARE_SEP / 3) * 2,
                    cx(c) - (SQUARE_SEP / 3) * 2);
            spot(g, cy(r) - SQUARE_SEP / 3, cx(c) - SQUARE_SEP / 3);
            spot(g, cy(r) - SQUARE_SEP / 3,
                    cx(c) - (SQUARE_SEP / 3) * 2);
            spot(g, cy(r) - (SQUARE_SEP / 3) * 2, cx(c) - SQUARE_SEP / 3);
            break;
        }
    }

    /** Draw one spot centered at position (X, Y) on G. */
    private void spot(Graphics2D g, int x, int y) {
        g.setColor(SPOT_COLOR);
        g.fillOval(x - SPOT_DIM / 2, y - SPOT_DIM / 2, SPOT_DIM, SPOT_DIM);
    }

    /** Respond to the mouse click depicted by EVENT. */
    public void doClick(String dummy, MouseEvent event) {
        int x = event.getX(),
            y = event.getY();
        int r = x / SQUARE_SEP;
        int c = y / SQUARE_SEP;
        _commandQueue.offer(String.format("%d %d", r + 1, c + 1));
    }

    /** The Board I am displaying. */
    private Board _board;
    /** Dimension in pixels of one side of the board. */
    private int _side;
    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;

    /** Creates a stroke from separator size. */
    static final BasicStroke
            GRIDLINE_STROKE = new BasicStroke(SEPARATOR_SIZE);

    /** Number of Rows and Columns of the board. */
    private int _widthHeight;
}
