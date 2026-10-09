package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Game implements IGame
{
	/**
	 * Prints the game board by representing the positions of ships, adjacent tiles,
	 * shots, and other game elements onto the console. The method also optionally
	 * displays shot positions and a legend explaining the symbols used on the board.
	 *
	 * @param fleet       the fleet of ships to be displayed on the board. Ships are marked
	 *                    and their positions are shown according to their placement.
	 * @param moves       the list of moves containing shots. If shot positions are shown,
	 *                    they will be rendered based on their outcome (hit, miss, etc.).
	 * @param show_shots  if true, displays the shots taken during the game and marks
	 *                    their result (hit or miss) on the board.
	 * @param showLegend  if true, displays an explanatory legend of the symbols used
	 *                    to represent various elements such as ships, misses, hits, etc.
	 */
	public static void printBoard(IFleet fleet, List<IMove> moves, boolean show_shots, boolean showLegend) {

		assert fleet != null;
		assert moves != null;

		char[][] map = new char[BOARD_SIZE][BOARD_SIZE];

		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				map[r][c] = EMPTY_MARKER;

		printShips(fleet, map);

		if (show_shots)
			printBoardShots(moves, map);

		printBoardFrame(map);

		if (showLegend) {
			System.out.println("          LEGENDA");
			System.out.println("'" + SHIP_MARKER + "'->navio, '" + SHIP_ADJACENT_MARKER + "'->adjacente a navio, '" + EMPTY_MARKER + "'->água");
			System.out.println("'" + SHOT_SHIP_MARKER + "'->Tiro certeiro, '" + SHOT_WATER_MARKER + "'->Tiro na água");
		}
		System.out.println();
	}

	private static void printShips(IFleet fleet, char[][] map) {
		for (IShip ship : fleet.getShips()) {
			for (IPosition ship_pos : ship.getPositions())
				map[ship_pos.getRow()][ship_pos.getColumn()] = SHIP_MARKER;
			if (!ship.stillFloating())
				for (IPosition adjacent_pos : ship.getAdjacentPositions())
					map[adjacent_pos.getRow()][adjacent_pos.getColumn()] = SHIP_ADJACENT_MARKER;
		}
	}

	private static void printBoardShots(List<IMove> moves, char[][] map) {
		for (IMove move : moves)
			for (IPosition shot : move.getShots()) {
				if (shot.isInside()){
					int row = shot.getRow();
					int col = shot.getColumn();
					if (map[row][col] == SHIP_MARKER)
						map[row][col] = SHOT_SHIP_MARKER;
					if (map[row][col] == EMPTY_MARKER || map[row][col] == SHIP_ADJACENT_MARKER)
						map[row][col] = SHOT_WATER_MARKER;
				}
			}
	}

	private static void printBoardFrame(char[][] map) {
		System.out.println();
		System.out.print("    ");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();

		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");

		for (int row = 0; row < BOARD_SIZE; row++) {
			Position pos = new Position(row, 0);
			char rowLabel = pos.getClassicRow();
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++)
				System.out.print(" " + map[row][col]);
			System.out.println(" |");
		}

		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++)
			System.out.print("--");
		System.out.println("-+");
	}

	/**
	 * Serializes a list of shot positions into a JSON string. Each shot is represented
	 * with its classic row and column values. The method uses the Jackson library for
	 * JSON serialization.
	 *
	 * @param shots a list of shot positions to be serialized. Each position is represented
	 *              by an implementation of the {@code IPosition} interface. The list must
	 *              not be null.
	 * @return a formatted JSON string containing the shot positions. Each shot includes
	 *         its classic row and column.
	 * @throws RuntimeException if an error occurs during JSON serialization.
	 */
	public static String jsonShots(List<IPosition> shots) {

		assert shots != null;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// 1. Create a simplified list containing only the desired data
		List<Map<String, Object>> simplifiedShots = new ArrayList<>();
		for (IPosition shot : shots) {
			Map<String, Object> simplePos = new LinkedHashMap<>();
			// We use getClassicRow() and getClassicColumn() based on your current JSON output
			simplePos.put("row", String.valueOf(shot.getClassicRow()));
			simplePos.put("column", shot.getClassicColumn());
			simplifiedShots.add(simplePos);
		}

		String jsonString = null;
		try {
			// 2. Serialize the simplified list instead of the raw 'shots' list
			jsonString = objectMapper.writeValueAsString(simplifiedShots);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON", e);
		}

		System.out.println(jsonString);
		System.out.println();

		// Retornar o JSON
		return jsonString;
	}

	//------------------------------------------------------------------
	public static final int BOARD_SIZE = 10;
	public static final int NUMBER_SHOTS = 3;

	private static final char EMPTY_MARKER = '.';
	private static final char SHIP_MARKER = '#';
	private static final char SHOT_SHIP_MARKER = '*';
	private static final char SHOT_WATER_MARKER = 'o';
	private static final char SHIP_ADJACENT_MARKER = '-';
	private static final int SAVE_FORMAT_VERSION = 1;
	private static final ObjectMapper SAVE_MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT);

	private record PositionData(int row, int column) {}
	private record ShipData(String category, Compass bearing, PositionData position, List<PositionData> hits) {}
	private record ShotResultData(boolean valid, boolean repeated, String fleet, Integer shipIndex, boolean sunk) {}
	private record MoveData(int number, List<PositionData> shots, List<ShotResultData> results) {}
	private record SaveData(int version, List<ShipData> myFleet, List<ShipData> alienFleet,
						 List<MoveData> alienMoves, List<MoveData> myMoves,
						 int countInvalidShots, int countRepeatedShots, int countHits,
						 int countSinks, int moveNumber) {}

	//------------------------------------------------------------------
	private final IFleet myFleet;
	private final List<IMove> alienMoves;

	private final IFleet alienFleet;
	private final List<IMove> myMoves;

	private Integer countInvalidShots;
	private Integer countRepeatedShots;
	private Integer countHits;
	private Integer countSinks;
	private int moveNumber;

	//------------------------------------------------------------------
	public Game(IFleet myFleet)
	{
		this.moveNumber = 1;

		this.alienMoves = new ArrayList<IMove>();
		this.myMoves = new ArrayList<IMove>();

		this.alienFleet = new Fleet();
		this.myFleet = myFleet;

		this.countInvalidShots = 0;
		this.countRepeatedShots = 0;
		this.countHits = 0;
		this.countSinks = 0;
	}

	/** Saves the complete game state as formatted JSON at the given path. */
	public void save(Path file) throws IOException {
		Objects.requireNonNull(file, "Save path must not be null");
		SAVE_MAPPER.writeValue(file.toFile(), createSaveData());
	}

	/** Loads a game previously written by {@link #save(Path)}. */
	public static Game load(Path file) throws IOException {
		Objects.requireNonNull(file, "Save path must not be null");
		SaveData data = SAVE_MAPPER.readValue(file.toFile(), SaveData.class);
		return restoreSaveData(data);
	}

	private SaveData createSaveData() {
		return new SaveData(SAVE_FORMAT_VERSION, saveFleet(myFleet), saveFleet(alienFleet),
				saveMoves(alienMoves), saveMoves(myMoves), countInvalidShots, countRepeatedShots,
				countHits, countSinks, moveNumber);
	}

	private static List<ShipData> saveFleet(IFleet fleet) {
		List<ShipData> ships = new ArrayList<>();
		for (IShip ship : fleet.getShips()) {
			List<PositionData> hits = new ArrayList<>();
			for (IPosition position : ship.getPositions()) {
				if (position.isHit())
					hits.add(new PositionData(position.getRow(), position.getColumn()));
			}
			IPosition origin = ship.getPosition();
			ships.add(new ShipData(ship.getCategory(), ship.getBearing(),
					new PositionData(origin.getRow(), origin.getColumn()), hits));
		}
		return ships;
	}

	private List<MoveData> saveMoves(List<IMove> moves) {
		List<MoveData> savedMoves = new ArrayList<>();
		for (IMove move : moves) {
			List<PositionData> shots = new ArrayList<>();
			for (IPosition shot : move.getShots())
				shots.add(new PositionData(shot.getRow(), shot.getColumn()));

			List<ShotResultData> results = new ArrayList<>();
			for (ShotResult result : move.getShotResults()) {
				String fleetName = null;
				Integer shipIndex = null;
				if (result.ship() != null) {
					shipIndex = indexOfShip(myFleet, result.ship());
					if (shipIndex >= 0)
						fleetName = "my";
					else {
						shipIndex = indexOfShip(alienFleet, result.ship());
						if (shipIndex >= 0)
							fleetName = "alien";
					}
					if (fleetName == null)
						throw new IllegalStateException("Move result references a ship outside this game");
				}
				results.add(new ShotResultData(result.valid(), result.repeated(), fleetName, shipIndex, result.sunk()));
			}
			savedMoves.add(new MoveData(move.getNumber(), shots, results));
		}
		return savedMoves;
	}

	private static int indexOfShip(IFleet fleet, IShip target) {
		for (int index = 0; index < fleet.getShips().size(); index++)
			if (fleet.getShips().get(index) == target)
				return index;
		return -1;
	}

	private static Game restoreSaveData(SaveData data) throws IOException {
		if (data == null || data.version() != SAVE_FORMAT_VERSION)
			throw new IOException("Unsupported or missing game save format version");
		if (data.moveNumber() < 1 || data.countInvalidShots() < 0 || data.countRepeatedShots() < 0
				|| data.countHits() < 0 || data.countSinks() < 0)
			throw new IOException("Game save contains invalid counters");

		try {
			IFleet myFleet = restoreFleet(data.myFleet());
			IFleet alienFleet = restoreFleet(data.alienFleet());
			Game game = new Game(myFleet);
			game.alienFleet.getShips().addAll(alienFleet.getShips());
			game.alienMoves.addAll(restoreMoves(data.alienMoves(), myFleet, alienFleet));
			game.myMoves.addAll(restoreMoves(data.myMoves(), myFleet, alienFleet));
			game.countInvalidShots = data.countInvalidShots();
			game.countRepeatedShots = data.countRepeatedShots();
			game.countHits = data.countHits();
			game.countSinks = data.countSinks();
			game.moveNumber = data.moveNumber();
			return game;
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new IOException("Game save contains invalid state", exception);
		}
	}

	private static IFleet restoreFleet(List<ShipData> savedShips) throws IOException {
		if (savedShips == null)
			throw new IOException("Game save is missing a fleet");
		IFleet fleet = new Fleet();
		for (ShipData savedShip : savedShips) {
			if (savedShip == null || savedShip.category() == null || savedShip.bearing() == null
					|| savedShip.position() == null || savedShip.hits() == null)
				throw new IOException("Game save contains an incomplete ship");
			Ship ship = Ship.buildShip(savedShip.category().toLowerCase(Locale.ROOT), savedShip.bearing(),
					new Position(savedShip.position().row(), savedShip.position().column()));
			if (ship == null || !fleet.addShip(ship))
				throw new IOException("Game save contains an invalid ship");
			for (PositionData hit : savedShip.hits()) {
				if (hit == null || !ship.occupies(new Position(hit.row(), hit.column())))
					throw new IOException("Game save contains an invalid ship hit");
				ship.shoot(new Position(hit.row(), hit.column()));
			}
		}
		return fleet;
	}

	private static List<IMove> restoreMoves(List<MoveData> savedMoves, IFleet myFleet, IFleet alienFleet) throws IOException {
		if (savedMoves == null)
			throw new IOException("Game save is missing move history");
		List<IMove> moves = new ArrayList<>();
		for (MoveData savedMove : savedMoves) {
			if (savedMove == null || savedMove.shots() == null || savedMove.results() == null)
				throw new IOException("Game save contains an incomplete move");
			List<IPosition> shots = new ArrayList<>();
			for (PositionData shot : savedMove.shots()) {
				if (shot == null)
					throw new IOException("Game save contains an invalid shot");
				shots.add(new Position(shot.row(), shot.column()));
			}
			List<ShotResult> results = new ArrayList<>();
			for (ShotResultData result : savedMove.results()) {
				if (result == null)
					throw new IOException("Game save contains an invalid shot result");
				IShip ship = null;
				if (result.fleet() != null && result.shipIndex() != null) {
					IFleet fleet = switch (result.fleet()) {
						case "my" -> myFleet;
						case "alien" -> alienFleet;
						default -> throw new IOException("Game save references an unknown fleet");
					};
					if (result.shipIndex() < 0 || result.shipIndex() >= fleet.getShips().size())
						throw new IOException("Game save references an unknown ship");
					ship = fleet.getShips().get(result.shipIndex());
				} else if (result.fleet() != null || result.shipIndex() != null) {
					throw new IOException("Game save contains an incomplete ship reference");
				}
				results.add(new ShotResult(result.valid(), result.repeated(), ship, result.sunk()));
			}
			moves.add(new Move(savedMove.number(), shots, results));
		}
		return moves;
	}

	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	@Override
	public IFleet getAlienFleet()
	{
		return alienFleet;
	}

	@Override
	public List<IMove> getMyMoves()
	{
		return myMoves;
	}

	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	/**
	 * Simulates a random firing action by the enemy, generating a set of unique shot coordinates
	 * and serializing them into a JSON string. The method ensures that the random shots are valid
	 * and do not duplicate existing shots in the game or previous enemy moves. After generating
	 * the shots, it applies the firing logic and serializes the result for further processing.
	 *
	 * @return A JSON string representing the list of randomly generated enemy shots.
	 * @throws RuntimeException if there is an error during the JSON serialization of the shots.
	 */
	public String randomEnemyFire() {

		// Criar uma instância de Random com uma seed baseada no timestamp atual
		Random random = new Random(System.currentTimeMillis());

		Set<IPosition> usablePositions = new HashSet<IPosition>();
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				usablePositions.add(new Position(r, c));

		this.myFleet.getSunkShips().forEach(ship -> usablePositions.removeAll(ship.getAdjacentPositions()));
		this.alienMoves.forEach(move ->  usablePositions.removeAll(move.getShots()));

		List<IPosition> candidateShots = new ArrayList<>(usablePositions);

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<IPosition>();

		System.out.println();
		// Gerar coordenadas únicas até atingir o número definido por NUMBER_SHOTS

		IPosition newShot = null;
		if (candidateShots.size() >= Game.NUMBER_SHOTS)
			while (shots.size() < Game.NUMBER_SHOTS) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
		else {
			while (shots.size() < candidateShots.size()) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
			while (shots.size() < Game.NUMBER_SHOTS)
				shots.add(newShot);
		}

		System.out.print("rajada ");
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}


	/**
	 * Reads and processes the enemy fire input from the specified scanner.
	 * The method expects input describing positions for enemy shots. It verifies
	 * the format, ensures the correct number of positions are provided, and then fires
	 * on those positions.
	 *
	 * @param in the scanner object to read the enemy fire positions from, input must
	 *           be formatted either as a single token combining the column and row
	 *           (e.g., "A3") or as separate tokens (e.g., "A" followed by "3").
	 * @throws IllegalArgumentException if the provided positions are incomplete,
	 *                                  incorrectly formatted, or do not match the
	 *                                  required number of shots (NUMBER_SHOTS).
	 */
	public String readEnemyFire(Scanner in) {

		assert in != null;

		String input = in.nextLine().trim();

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<>();

		Scanner inputScanner = new Scanner(input);
		while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
			// Lê a próxima parte e constrói uma posição
			String token = inputScanner.next();

			if (token.matches("[A-Za-z]")) {
				// Caso seja somente uma coluna ("A", "B", etc.), esperar o próximo número
				if (inputScanner.hasNextInt()) {
					int row = inputScanner.nextInt();
					shots.add(new Position(token.toUpperCase().charAt(0), row));
				} else {
					throw new IllegalArgumentException("Posição incompleta! A coluna '" + token + "' não é seguida por uma linha.");
				}
			} else {
				// Caso o token já contenha a coluna e a linha juntas (ex.: "A3")
				Scanner singleScanner = new Scanner(token);
				shots.add(Tasks.readClassicPosition(singleScanner));
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Você deve inserir exatamente " + NUMBER_SHOTS + " posições!");
		}

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}

	/**
	 * Fires a set of shots during a player's move. Each shot is resolved and
	 * consolidated into a move, which is processed and added to the list of alien moves.
	 * The method ensures exactly {@code NUMBER_SHOTS} shots are fired, validates
	 * each shot's position, and increments the move counter after completing the operation.
	 *
	 * @param shots a list of positions representing the locations to fire shots at.
	 *              The positions should be unique and valid within the bounds of the game board.
	 *              The size of the list must be equal to {@code NUMBER_SHOTS}.
	 * @throws IllegalArgumentException if the list of shots is null, contains an invalid
	 *                                  number of positions, or includes duplicate positions.
	 */
	public void fireShots(List<IPosition> shots)
	{
		assert shots != null;

		List<ShotResult> shotResults = new ArrayList<ShotResult>();
		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Must fire exactly " + NUMBER_SHOTS + " shots per move.");
		}

		List<IPosition> alreadyShot = new ArrayList<IPosition>();
		for (IPosition pos : shots) {
			shotResults.add(fireSingleShot(pos, alreadyShot.contains(pos)));
			alreadyShot.add(pos);
		}

		Move move = new Move(moveNumber, shots, shotResults);

//		System.out.println(move);

		move.processEnemyFire(true);

		alienMoves.add(move);

		moveNumber++;
	}

	/**
	 * Fires a single shot at the specified position, handling scenarios such as invalid positions,
	 * repeated shots, hits, misses, and sinking a ship. The method updates the necessary counters
	 * for invalid shots, repeated shots, hits, and sunk ships.
	 *
	 * @param pos the position to fire the shot at; must be valid and within the game board boundaries.
	 * @param isRepeated true if the shot is marked as a repeat attempt, false otherwise.
	 * @return a ShotResult object containing the result of the shot, including whether the shot was
	 *         valid, repeated, a hit, and whether a ship was sunk.
	 */
	public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside()) {
			countInvalidShots++;
			return new ShotResult(false, false, null, false);
		}

		if (isRepeated || repeatedShot(pos)) {
			countRepeatedShots++;
			return new ShotResult(true, true, null, false);
		}

		IShip ship = myFleet.shipAt(pos);
		if (ship == null)
			return new ShotResult(true, false, null, false);
		else
		{
			ship.shoot(pos);
			countHits++;
			if (!ship.stillFloating()) {
				countSinks++;
			}
			return new ShotResult(true, false, ship, !ship.stillFloating());
		}
	}

	@Override
	public int getRepeatedShots()
	{
		return this.countRepeatedShots;
	}

	@Override
	public int getInvalidShots()
	{
		return this.countInvalidShots;
	}

	@Override
	public int getHits()
	{
		return this.countHits;
	}

	@Override
	public int getSunkShips()
	{
		return this.countSinks;
	}

	@Override
	public int getRemainingShips()
	{
		List<IShip> floatingShips = myFleet.getFloatingShips();
		return floatingShips.size();
	}

	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	public void printMyBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	public void printAlienBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.alienFleet, this.myMoves, show_shots, show_legend);
	}

	public void over() {
			System.out.println();
			System.out.println("+--------------------------------------------------------------+");
			System.out.println("| Maldito sejas, Java Sparrow, eu voltarei, glub glub glub ... |");
			System.out.println("+--------------------------------------------------------------+");
	}
}