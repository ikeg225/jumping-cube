package jump61;

import java.util.ArrayList;
import java.util.Formatter;

import java.util.function.Consumer;

import static jump61.Side.*;
import static jump61.Square.square;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Ethan Ikegami
 */
class Board {

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        boardIntialize(6);
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        if (N <= 1) {
            throw new GameException("Size must be greater than 1!");
        } else {
            boardIntialize(N);
        }
    }

    /** Initialize a new Board of size N. */
    private void boardIntialize(int N) {
        _size = N;
        _notifier = NOP;
        _board = new Square[numSquares()];
        for (int i = 0; i < numSquares(); i++) {
            _board[i] = square(WHITE, 0);
        }
        _white = numSquares();
        _spots = numSquares();
        _blue = 0;
        _red = 0;
        _history = new ArrayList<>();
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        this(board0.size());
        copy(board0);
        _white = board0.getWhite();
        _red = board0.getRed();
        _blue = board0.getBlue();
        _size = board0.size();
        _spots = board0.getSpots();
        _notifier = NOP;
        _readonlyBoard = new ConstantBoard(this);
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        boardIntialize(N);
        _history.clear();
        announce();
    }

    /** Copy the contents of BOARD into me. Resets undo
     * history and number of moves. */
    void copy(Board board) {
        _board = new Square[board.numSquares()];
        Square[] copyBoard = board.getBoard();
        for (int i = 0; i < board.numSquares(); i++) {
            _board[i] = square(parseSide(copyBoard[i].getSide().toString()),
                    copyBoard[i].getSpots());
        }
        _white = board.getWhite();
        _red = board.getRed();
        _blue = board.getBlue();
        _spots = board.getSpots();
        _size = board.size();
        _history.clear();
        announce();
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size. */
    public void internalCopy(Board board) {
        assert size() == board.size();
        Square[] copyBoard = board.getBoard();
        for (int i = 0; i < numSquares(); i++) {
            _board[i] = square(parseSide(copyBoard[i].getSide().toString()),
                    copyBoard[i].getSpots());
        }
        _white = board.getWhite();
        _red = board.getRed();
        _blue = board.getBlue();
        _spots = board.getSpots();
        announce();
    }

    /** Copy the contents of STATE and returns a new one. */
    public Square[] boardCopyState(Square[] state) {
        Square[] copyBoard = new Square[state.length];
        for (int i = 0; i < numSquares(); i++) {
            copyBoard[i] = square(parseSide(state[i].getSide().toString()),
                    state[i].getSpots());
        }
        return copyBoard;
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _size;
    }

    /** Return the number of squares in the Board. */
    int numSquares() {
        return _size * _size;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        return _board[n];
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((getSpots() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        if (!exists(n)) {
            throw new GameException("Not possible row for given index.");
        } else {
            return n / size() + 1;
        }
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        if (!exists(n)) {
            throw new GameException("Not possible column for given index.");
        } else {
            return n % size() + 1;
        }
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        if (!exists(r, c)) {
            throw new GameException("Not possible index "
                    + "for given row and column.");
        } else {
            return (c - 1) + (r - 1) * size();
        }
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        return (isLegal(player)
                && (_board[n].getSide().toString().equals(player.toString())
                || _board[n].getSide().equals(WHITE)));
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        return (whoseMove().toString().equals(player.toString())
                && getWinner() == null);
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        if (getBlue() == numSquares()) {
            return BLUE;
        } else if (getRed() == numSquares()) {
            return RED;
        } else {
            return null;
        }
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        switch (side.toString()) {
        case "white":
            return getWhite();
        case "red":
            return getRed();
        case "blue":
            return getBlue();
        default:
            throw new GameException("Not side color.");
        }
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        set(r, c, get(r, c).getSpots() + 1, player);
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        addSpot(player, row(n), col(n));
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        if (getWinner() == null) {
            increaseSpots(sqNum(r, c), num);
            colorCount(player, _board[sqNum(r, c)].getSide().toString());
            markUndo();
            internalSet(r, c, num, player);
            announce();
        }
    }

    /** Sets the squares given a jump using row R column C
     * with a number NUM and side PLAYER. */
    void jumpSet(int r, int c, int num, Side player) {
        if (getWinner() == null) {
            colorCount(player, _board[sqNum(r, c)].getSide().toString());
            internalSet(r, c, num, player);
            announce();
        }
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        _board[n] = square(player, num);
        if (num > neighbors(n)) {
            jump(n);
        }
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        if (_history.isEmpty()) {
            throw new GameException("No history to undo");
        } else {
            GameState prev = _history.get(_history.size() - 1);
            _board = prev.getBoardGame();
            _white = prev.getWhiteGame();
            _red = prev.getRedGame();
            _blue = prev.getBlueGame();
            _spots = prev.getSpotsGame();
            _history.remove(_history.size() - 1);
        }
        announce();
    }

    /** Record the beginning of a move in the undo history. */
    void markUndo() {
        _history.add(new GameState());
    }

    /** Represents enough of the state of a game to allow undoing of moves. */
    private class GameState {
        /** A holder for the _cells and _active instance variables of this
         *  Model. */
        GameState() {
            _boardGame = boardCopyState(_board);
            _whiteGame = getWhite();
            _redGame = getRed();
            _blueGame = getBlue();
            _spotsGame = getSpots();
        }

        /** Contents of board. */
        private Square[] _boardGame;

        /** Get the contents of board.
         * @return Square[]
         * */
        public Square[] getBoardGame() {
            return _boardGame;
        }

        /** Count of the number of white squares on a board. */
        private int _whiteGame;

        /** Get the number of white squares on a board.
         * @return int
         * */
        public int getWhiteGame() {
            return _whiteGame;
        }

        /** Count of the number of red squares on a board. */
        private int _redGame;

        /** Get the number of red squares on a board.
         * @return int
         * */
        public int getRedGame() {
            return _redGame;
        }

        /** Count of the number of blue squares on a board. */
        private int _blueGame;

        /** Get the number of blue squares on a board.
         * @return int
         * */
        public int getBlueGame() {
            return _blueGame;
        }

        /** Count of the number of spots on a board. */
        private int _spotsGame;

        /** Get the number of spots on a board.
         * @return int
         * */
        public int getSpotsGame() {
            return _spotsGame;
        }
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /** Do all jumping on this board, assuming that initially, S is the only
     *  square that might be over-full. */
    private void jump(int S) {
        for (int i : neighborJumpIndex(S)) {
            jumpSet(row(i), col(i), get(i).getSpots() + 1,
                    get(S).getSide());
        }
    }

    /** Return a List of the neighbors of a given index and
     * given index CURR. */
    public int[] neighborJumpIndex(int curr) {
        int neighborCount = neighbors(curr);
        int[] neighbors = new int[neighborCount];
        switch (neighborCount) {
        case 4:
            neighbors[0] = curr - 1;
            neighbors[1] = curr + 1;
            neighbors[2] = curr - size();
            neighbors[3] = curr + size();
            _board[curr] = square(get(curr).getSide(),
                    get(curr).getSpots() - 4);
            break;
        case 3:
            if (row(curr) == 1) {
                neighbors[0] = curr - 1;
                neighbors[1] = curr + 1;
                neighbors[2] = curr + size();
            } else if (row(curr) == size()) {
                neighbors[0] = curr - 1;
                neighbors[1] = curr + 1;
                neighbors[2] = curr - size();
            } else if (col(curr) == 1) {
                neighbors[0] = curr - size();
                neighbors[1] = curr + size();
                neighbors[2] = curr + 1;
            } else if (col(curr) == size()) {
                neighbors[0] = curr - size();
                neighbors[1] = curr + size();
                neighbors[2] = curr - 1;
            } else {
                throw new GameException("Invalid Neighbor Count.");
            }
            _board[curr] = square(get(curr).getSide(),
                    get(curr).getSpots() - 3);
            break;
        case 2:
            if (row(curr) == 1 && col(curr) == 1) {
                neighbors[0] = curr + 1;
                neighbors[1] = curr + size();
            } else if (row(curr) == size() && col(curr) == size()) {
                neighbors[0] = curr - 1;
                neighbors[1] = curr - size();
            } else if (row(curr) == 1 && col(curr) == size()) {
                neighbors[0] = curr - 1;
                neighbors[1] = curr + size();
            } else if (row(curr) == size() && col(curr) == 1) {
                neighbors[0] = curr + 1;
                neighbors[1] = curr - size();
            } else {
                throw new GameException("Invalid Neighbor Count.");
            }
            _board[curr] = square(get(curr).getSide(),
                    get(curr).getSpots() - 2);
            break;
        default:
            throw new GameException("Invalid Neighbor Count.");
        }
        return neighbors;
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        StringBuilder total = new StringBuilder("===");
        int newRow = size();
        for (Square i : getBoard()) {
            if (newRow == size()) {
                total.append("\n   ");
                newRow = 0;
            }
            total.append(" " + i.getSpots() + toSingle(i.getSide()));
            newRow++;
        }
        total.append("\n===");
        return total.toString();
    }

    /** Converts SIDE to a single String value.
     * @return String
     * */
    public String toSingle(Side side) {
        switch (side.toString()) {
        case "white":
            return "-";
        case "red":
            return "r";
        case "blue":
            return "b";
        default:
            throw new GameException("Not Possible Color.");
        }
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            if (this.numSquares() != B.numSquares()) {
                return false;
            } else if (this == B) {
                return true;
            } else {
                return deepCheck(B);
            }
        }
    }

    /** Deep copy checker for another Board object B.
     * @return boolean
     * */
    private boolean deepCheck(Board B) {
        for (int i = 0; i < this.numSquares(); i++) {
            Square thisSquare = this.getBoard()[i];
            Square objSquare = B.getBoard()[i];
            if (thisSquare.getSpots() != objSquare.getSpots()
                    || !thisSquare.getSide().toString().equals(
                    objSquare.getSide().toString())) {
                return false;
            }
        }
        return true;
    }

    /** Increases or decreases the color count given
     * the side NEWSIDE and string OLDCOLOR.
     * @return int*/
    private int colorCount(Side newSide, String oldColor) {
        switch (newSide.toString()) {
        case "white":
            if (oldColor.equals("red")) {
                _red -= 1;
                _white += 1;
            } else if (oldColor.equals("blue")) {
                _blue -= 1;
                _white += 1;
            }
            return _white;
        case "red":
            if (oldColor.equals("white")) {
                _white -= 1;
                _red += 1;
            } else if (oldColor.equals("blue")) {
                _blue -= 1;
                _red += 1;
            }
            return _red;
        case "blue":
            if (oldColor.equals("red")) {
                _red -= 1;
                _blue += 1;
            } else if (oldColor.equals("white")) {
                _white -= 1;
                _blue += 1;
            }
            return _blue;
        default:
            throw new GameException("Not Possible Color.");
        }
    }

    @Override
    public int hashCode() {
        return getSpots();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** Gets Board Array of squares.
     * @return Square
     * */
    public Square[] getBoard() {
        return _board;
    }

    /** Get the number of white squares on a board.
     * @return int
     * */
    public int getWhite() {
        return _white;
    }

    /** Get the number of red squares on a board.
     * @return int
     * */
    public int getRed() {
        return _red;
    }

    /** Get the number of blue squares on a board.
     * @return int
     * */
    public int getBlue() {
        return _blue;
    }

    /** Get the number of spots on a board.
     * @return int
     * */
    public int getSpots() {
        return _spots;
    }

    /** Get the number of spots on a board.
     * @return int
     * */
    int numPieces() {
        return _spots;
    }

    /** Increases the number of all spots on the board using
     * given BOARDINDEX anf NEWSPOTS. */
    private void increaseSpots(int boardIndex, int newSpots) {
        if (newSpots <= 0) {
            throw new GameException("Not possible set value");
        } else {
            _spots += (newSpots - _board[boardIndex].getSpots());
        }
    }

    /** Gets the ArrayList of Square[] states for the board.
     * @return ArrayList
     * */
    public ArrayList<GameState> getHistory() {
        return _history;
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;

    /** Size of the board. */
    private int _size;

    /** The board containing squares of given size. */
    private Square[] _board;

    /** Count of the number of white squares on a board. */
    private int _white;

    /** Count of the number of red squares on a board. */
    private int _red;

    /** Count of the number of blue squares on a board. */
    private int _blue;

    /** Count of the number of spots on a board. */
    private int _spots;

    /** ArrayList of Square[] states for the board. */
    private ArrayList<GameState> _history;
}
