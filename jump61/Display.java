package jump61;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.concurrent.ArrayBlockingQueue;

import static jump61.Side.*;

/** The GUI controller for jump61.  To require minimal change to textual
 *  interface, we adopt the strategy of converting GUI input (mouse clicks)
 *  into textual commands that are sent to the Game object through a
 *  a Writer.  The Game object need never know where its input is coming from.
 *  A Display is an Observer of Games and Boards so that it is notified when
 *  either changes.
 *  @author Ethan Ikegami
 */
class Display extends TopLevel implements View, CommandSource, Reporter {

    /** A new window with given TITLE displaying GAME, and using COMMANDWRITER
     *  to send commands to the current game. */
    Display(String title) {
        super(title, true);

        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Game->New Game", this::newGame);
        addMenuRadioButton("Options->Red Manual","Red",
                true, this::manualRed);
        addMenuRadioButton("Options->Red AI", "Red",
                false, this::autoRed);
        addMenuRadioButton("Options->Blue Manual", "Blue",
                false, this::manualBlue);
        addMenuRadioButton("Options->Blue AI", "Blue",
                true, this::autoBlue);
        addMenuButton("Options->Set Seed...", this::setSeed);
        addMenuButton("Options->Board Size...", this::boardSize);

        _boardWidget = new BoardWidget(_commandQueue);
        add(_boardWidget, new LayoutSpec("y", 1, "width", 2));
        display(true);
    }

    /** Response to "Quit" button click. */
    void quit(String dummy) {
        System.exit(0);
    }

    /** Response to "New Game" button click. */
    void newGame(String dummy) {
        _commandQueue.offer("new");
    }

    /** Response to "Manual Red" button click. */
    void manualRed(String dummy) {
        _commandQueue.offer("manual red");
    }

    /** Response to "Auto Red" button click. */
    void autoRed(String dummy) {
        _commandQueue.offer("auto red");
    }

    /** Response to "Manual Blue" button click. */
    void manualBlue(String dummy) {
        _commandQueue.offer("manual blue");
    }

    /** Response to "Auto Blue" button click. */
    void autoBlue(String dummy) {
        _commandQueue.offer("auto blue");
    }

    /** Response to "Set Seed" button click. */
    void setSeed(String dummy) {
        String seed = getTextInput("Enter Seed Value", "Seed", "String", "");
        _commandQueue.offer("seed " + seed);
    }

    /** Response to "Board Size" button click. */
    void boardSize(String dummy) {
        String size = getTextInput("Enter Number of Rows and Columns (2-10)",
                "Size", "String", "");
        _commandQueue.offer("size " + size);
    }

    @Override
    public void update(Board board) {
        // FIXME
        _boardWidget.update(board);
        pack();
        _boardWidget.repaint();
    }

    @Override
    public String getCommand(String ignored) {
        try {
            return _commandQueue.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void announceWin(Side side) {
        showMessage(String.format("%s wins!", side.toCapitalizedString()),
                    "Game Over", "information");
    }

    @Override
    public void announceMove(int row, int col) {
    }

    @Override
    public void msg(String format, Object... args) {
        showMessage(String.format(format, args), "", "information");
    }

    @Override
    public void err(String format, Object... args) {
        showMessage(String.format(format, args), "Error", "error");
    }

    /** Time interval in msec to wait after a board update. */
    static final long BOARD_UPDATE_INTERVAL = 50;

    /** The widget that displays the actual playing board. */
    private BoardWidget _boardWidget;

    /** Queue for commands going to the controlling Game. */
    private final ArrayBlockingQueue<String> _commandQueue =
        new ArrayBlockingQueue<>(5);
}
