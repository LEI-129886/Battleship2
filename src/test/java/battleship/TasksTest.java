package battleship;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

	@Test
	@DisplayName("Constructs the Tasks utility")
	void constructsTasks() {
		assertNotNull(new Tasks());
	}

	@Test
	@DisplayName("Rejects null scanners when assertions are enabled")
	void scannerArgumentsMustNotBeNull() {
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null));
		assertThrows(AssertionError.class, () -> Tasks.readShip(null));
		assertThrows(AssertionError.class, () -> Tasks.readPosition(null));
	}

	@Test
	@DisplayName("Builds a fleet after skipping unknown and overlapping ships")
	void buildFleetRetriesInvalidShips() {
		StringBuilder input = new StringBuilder()
				.append("unknown 0 0 n ")
				.append("barca 0 0 n ")
				.append("barca 0 0 n ");
		int[][] positions = {
				{0, 2}, {0, 4}, {0, 6}, {0, 8},
				{2, 0}, {2, 2}, {2, 4}, {2, 6}, {2, 8}, {4, 0}
		};
		for (int[] position : positions) {
			input.append("barca ").append(position[0]).append(' ')
					.append(position[1]).append(" n ");
		}

		Fleet fleet = Tasks.buildFleet(new Scanner(input.toString()));

		assertEquals(IFleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	@DisplayName("Reads a supported ship and returns null for an unknown type")
	void readShipBuildsSupportedShip() {
		Ship ship = Tasks.readShip(new Scanner("caravela 4 5 e"));

		assertInstanceOf(Caravel.class, ship);
		assertEquals(new Position(4, 5), ship.getPosition());
		assertEquals(Compass.EAST, ship.getBearing());
		assertNull(Tasks.readShip(new Scanner("unknown 1 2 n")));
	}

	@Test
	@DisplayName("Reads numeric row and column coordinates")
	void readPositionReadsCoordinates() {
		Position position = Tasks.readPosition(new Scanner("3 7"));

		assertEquals(3, position.getRow());
		assertEquals(7, position.getColumn());
	}

	@Test
	@DisplayName("Reads compact and spaced classic positions and rejects malformed input")
	void readClassicPositionParsesInputs() {
		IPosition compact = Tasks.readClassicPosition(new Scanner("b4"));
		IPosition spaced = Tasks.readClassicPosition(new Scanner("C 7"));

		assertEquals(new Position(1, 3), compact);
		assertEquals(new Position(2, 6), spaced);
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("")));
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("A nope")));
	}

	@Test
	@DisplayName("Prints help and safely ignores game commands before a game starts")
	void menuHandlesCommandsWithoutGame() {
		String output = runMenu(String.join(System.lineSeparator(),
				"estado", "mapa", "rajada", "simula", "relatorio",
				"tiros", "guardar ignored.json", "ajuda", "comando-inexistente", "desisto"));

		assertTrue(output.contains("AJUDA DO MENU"));
		assertTrue(output.contains("Que comando é esse???"));
		assertTrue(output.contains("Não existe um jogo para guardar."));
		assertTrue(output.contains("Bons ventos!"));
	}

	@Test
	@DisplayName("Prompts for an empty save path and rejects a path containing a null character")
	void menuRejectsInvalidSavePaths() {
		String commands = "guardar" + System.lineSeparator()
				+ "guardar invalid\u0000path" + System.lineSeparator()
				+ "carregar" + System.lineSeparator()
				+ "carregar invalid\u0000path" + System.lineSeparator()
				+ "desisto";

		String output = runMenu(commands);

		assertTrue(output.contains("Indique o caminho do ficheiro JSON."));
		assertTrue(output.contains("O caminho indicado não é válido."));
	}

	@Test
	@DisplayName("Starts a game with a randomly generated fleet")
	void menuGeneratesRandomFleet() {
		String output = runMenu("gerafrota" + System.lineSeparator() + "desisto");

		assertTrue(output.contains("O seu jogo começa agora."));
		assertTrue(output.contains("Bons ventos!"));
	}

	@Test
	@DisplayName("Runs simulation immediately for a loaded game with no remaining ships")
	void menuSimulatesFinishedGame(@TempDir Path tempDir) throws Exception {
		Path saveFile = tempDir.resolve("empty-fleet.json");
		new Game(new Fleet()).save(saveFile);

		String output = runMenu("carregar " + saveFile + System.lineSeparator()
				+ "simula" + System.lineSeparator()
				+ "desisto");

		assertTrue(output.contains("Maldito sejas, Java Sparrow"));
	}

	@Test
	@DisplayName("Restarts the stopwatch when loading over an active game")
	void menuLoadsGameOverActiveGame(@TempDir Path tempDir) throws Exception {
		Path saveFile = tempDir.resolve("replacement-game.json");
		new Game(new Fleet()).save(saveFile);
		String commands = "lefrota" + System.lineSeparator()
				+ fleetInput()
				+ "carregar " + saveFile + System.lineSeparator()
				+ "desisto";

		String output = runMenu(commands);

		assertTrue(output.contains("O seu jogo começa agora."));
		assertTrue(output.contains("Jogo carregado de "));
	}

	@Test
	@DisplayName("Simulates turns until the fleet is sunk")
	void simulateGameRunsUntilFinished() {
		ScriptedGame game = new ScriptedGame(2);

		Tasks.simulateGame(game, 0);

		assertEquals(2, game.getTurns());
		assertTrue(game.isOver());
	}

	@Test
	@DisplayName("Restores the interrupted status when a simulation wait is interrupted")
	void simulateGameRestoresInterruptStatus() {
		ScriptedGame game = new ScriptedGame(1);
		Thread.currentThread().interrupt();

		try {
			Tasks.simulateGame(game, 0);

			assertEquals(1, game.getTurns());
			assertTrue(game.isOver());
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
		}
	}

	@Test
	@DisplayName("Returns from the menu after the last ship is sunk")
	void menuReturnsWhenGameIsOver(@TempDir Path tempDir) throws Exception {
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		Path saveFile = tempDir.resolve("last-ship.json");
		new Game(fleet).save(saveFile);

		String output = runMenu("carregar " + saveFile + System.lineSeparator()
				+ "rajada A1 B2 C3" + System.lineSeparator());

		assertTrue(output.contains("Maldito sejas, Java Sparrow"));
	}

	@Test
	@DisplayName("Exports a report with invalid, repeated, water, hit, and sunk shots")
	void menuExportsGameReport(@TempDir Path tempDir) throws Exception {
		Path reportFile = Path.of("relatorio-battleship.pdf");
		Assumptions.assumeFalse(Files.exists(reportFile), "Preserve an existing report file");

		try {
			Fleet fleet = new Fleet();
			fleet.addShip(new Frigate(Compass.EAST, new Position(4, 4)));
			Game game = new Game(fleet);
			game.fireShots(java.util.List.of(
					new Position(-1, 0), new Position(2, 2), new Position(2, 2)));
			game.fireShots(java.util.List.of(
					new Position(4, 4), new Position(4, 5), new Position(6, 6)));
			game.fireShots(java.util.List.of(
					new Position(4, 6), new Position(4, 7), new Position(7, 7)));
			Path saveFile = tempDir.resolve("report-game.json");
			game.save(saveFile);

			String output = runMenu("carregar " + saveFile + System.lineSeparator()
					+ "relatorio" + System.lineSeparator()
					+ "desisto");

			assertTrue(output.contains("Relatorio exportado para relatorio-battleship.pdf"));
			assertTrue(Files.size(reportFile) > 0);

			Files.delete(reportFile);
			Fleet unfinishedFleet = new Fleet();
			unfinishedFleet.addShip(new Frigate(Compass.EAST, new Position(4, 4)));
			Path unfinishedSave = tempDir.resolve("unfinished-game.json");
			new Game(unfinishedFleet).save(unfinishedSave);

			String unfinishedOutput = runMenu("carregar " + unfinishedSave + System.lineSeparator()
					+ "relatorio" + System.lineSeparator()
					+ "desisto");
			assertTrue(unfinishedOutput.contains("Relatorio exportado para relatorio-battleship.pdf"));

			Files.delete(reportFile);
			Files.createDirectory(reportFile);
			String failedOutput = runMenu("carregar " + unfinishedSave + System.lineSeparator()
					+ "relatorio" + System.lineSeparator()
					+ "desisto");
			assertTrue(failedOutput.contains("Nao foi possivel exportar o relatorio:"));
		} finally {
			Files.deleteIfExists(reportFile);
		}
	}

	@Test
	@DisplayName("Creates and plays a game from a fleet entered through the menu")
	void menuBuildsFleetAndPlaysFiringRound() {
		String commands = "lefrota" + System.lineSeparator()
				+ fleetInput()
				+ "rajada A1 B2 C3" + System.lineSeparator()
				+ "estado" + System.lineSeparator()
				+ "mapa" + System.lineSeparator()
				+ "tiros" + System.lineSeparator()
				+ "desisto";

		String output = runMenu(commands);

		assertTrue(output.contains("O seu jogo começa agora."));
		assertTrue(output.contains("Demoraste "));
		assertTrue(output.contains("Estado da Frota:"));
		assertTrue(output.contains("Bons ventos!"));
	}

	@Test
	@DisplayName("Saves and loads a game through menu commands")
	void savesAndLoadsGameThroughMenu(@TempDir Path tempDir) throws Exception {
		Path initialSave = tempDir.resolve("initial.json");
		Path copiedSave = tempDir.resolve("copied save.json");
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		Game game = new Game(fleet);
		game.fireShots(java.util.List.of(new Position(1, 1), new Position(2, 2), new Position(3, 3)));
		game.save(initialSave);

		String commands = "carregar " + initialSave + System.lineSeparator()
				+ "guardar " + copiedSave + System.lineSeparator()
				+ "desisto" + System.lineSeparator();
		String output = runMenu(commands);

		Game restored = Game.load(copiedSave);
		assertTrue(output.contains("Jogo carregado de "));
		assertTrue(output.contains("Jogo guardado em "));
		assertEquals(1, restored.getHits());
		assertEquals(1, restored.getAlienMoves().size());
	}

	@Test
	@DisplayName("Reports I/O errors when saving to a directory or loading a missing save")
	void menuReportsSaveAndLoadFailures(@TempDir Path tempDir) throws Exception {
		Path initialSave = tempDir.resolve("initial.json");
		Path missingSave = tempDir.resolve("missing.json");
		new Game(new Fleet()).save(initialSave);

		String commands = "carregar " + initialSave + System.lineSeparator()
				+ "guardar " + tempDir + System.lineSeparator()
				+ "carregar " + missingSave + System.lineSeparator()
				+ "desisto";

		String output = runMenu(commands);

		assertTrue(output.contains("Não foi possível guardar o jogo."));
		assertTrue(output.contains("Não foi possível carregar o jogo."));
	}

	private static String fleetInput() {
		StringBuilder input = new StringBuilder();
		int[][] positions = {
				{0, 0}, {0, 2}, {0, 4}, {0, 6}, {0, 8},
				{2, 0}, {2, 2}, {2, 4}, {2, 6}, {2, 8}, {4, 0}
		};
		for (int[] position : positions) {
			input.append("barca ").append(position[0]).append(' ')
					.append(position[1]).append(" n ")
					.append(System.lineSeparator());
		}
		return input.toString();
	}

	private static final class ScriptedGame extends Game {
		private int remainingShips;
		private int turns;
		private boolean over;

		private ScriptedGame(int remainingShips) {
			super(new Fleet());
			this.remainingShips = remainingShips;
		}

		@Override
		public String randomEnemyFire() {
			remainingShips--;
			turns++;
			return "";
		}

		@Override
		public int getRemainingShips() {
			return remainingShips;
		}

		@Override
		public void printMyBoard(boolean showShots, boolean showLegend) {
		}

		@Override
		public void over() {
			over = true;
		}

		private int getTurns() {
			return turns;
		}

		private boolean isOver() {
			return over;
		}
	}

	private static String runMenu(String commands) {
		InputStream originalInput = System.in;
		PrintStream originalOutput = System.out;
		ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
		try (PrintStream output = new PrintStream(capturedOutput, true, StandardCharsets.UTF_8)) {
			System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
			System.setOut(output);
			Tasks.MENU();
		} finally {
			System.setIn(originalInput);
			System.setOut(originalOutput);
		}
		return capturedOutput.toString(StandardCharsets.UTF_8);
	}
}