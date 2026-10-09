package restserver;

import battleship.Fleet;
import battleship.Game;
import battleship.IGame;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds all state for one active game between the AI opponent and a student player.
 *
 * One GameSession is created per registration (m0) and stored in the GameRegistry
 * keyed by its gameId. It wraps the existing Game class without modifying it.
 */
public class GameSession {

	/** Unique identifier for this game, returned to the student on registration. */
	private final String gameId;

	/** The student's name, for logging / display purposes. */
	private final String playerName;

	/**
	 * The URL of the student's server where the AI will POST its shots (m2a).
	 * Example: "http://student-host:9090"
	 * The AI will call POST {callbackUrl}/game/{gameId}/shots
	 */
	private final String callbackUrl;

	/**
	 * The core game object from the existing codebase.
	 * myFleet = AI's fleet  (receives student's shots)
	 * alienFleet = tracked knowledge of student's fleet
	 */
	private final IGame game;

	/** Number of shots fired per turn (matches Game.NUMBER_SHOTS = 3). */
	private final int shotsPerTurn;

	/** True once one fleet is completely sunk. */
	private boolean gameOver;

	/** "AI_WINS" or "STUDENT_WINS" — set when gameOver becomes true. */
	private String winner;
	private final Instant startedAt;
	private final List<ReportEntry> reportEntries;
	private int reportTurn;

	// -------------------------------------------------------------------------

	public GameSession(String gameId, String playerName, String callbackUrl) {
		this(gameId, playerName, callbackUrl, new Game(Fleet.createRandom()));
	}

	public GameSession(String gameId, String playerName, String callbackUrl, IGame game) {
		this.gameId      = gameId;
		this.playerName  = playerName;
		this.callbackUrl = callbackUrl;
		this.game        = game;
		this.shotsPerTurn = Game.NUMBER_SHOTS;
		this.gameOver    = false;
		this.winner      = null;
		this.startedAt   = Instant.now();
		this.reportEntries = new ArrayList<>();
		this.reportTurn  = 0;
	}

	// ── Getters ──────────────────────────────────────────────────────────────

	public String getGameId()      { return gameId; }
	public String getPlayerName()  { return playerName; }
	public String getCallbackUrl() { return callbackUrl; }
	public IGame  getGame()        { return game; }
	public int    getShotsPerTurn(){ return shotsPerTurn; }
	public boolean isGameOver()    { return gameOver; }
	public String getWinner()      { return winner; }
	public Instant getStartedAt()  { return startedAt; }

	public synchronized void recordMove(String player, List<battleship.IPosition> shots,
			List<String> outcomes) {
		reportTurn++;
		for (int i = 0; i < shots.size() && i < outcomes.size(); i++) {
			battleship.IPosition shot = shots.get(i);
			reportEntries.add(new ReportEntry(reportTurn, player,
					String.valueOf(shot.getClassicRow()) + shot.getClassicColumn(), outcomes.get(i)));
		}
	}

	public synchronized List<ReportEntry> getReportEntries() {
		return Collections.unmodifiableList(new ArrayList<>(reportEntries));
	}

	public record ReportEntry(int turn, String player, String coordinate, String outcome) {}

	// ── State transitions ────────────────────────────────────────────────────

	public void markAiWins() {
		this.gameOver = true;
		this.winner   = "AI_WINS";
	}

	public void markStudentWins() {
		this.gameOver = true;
		this.winner   = "STUDENT_WINS";
	}
}