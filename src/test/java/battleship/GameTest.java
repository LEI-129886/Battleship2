package battleship;

import java.util.List;
import java.nio.file.Path;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	@Test
	void saveAndLoadPreservesGameAndAllowsPlayToContinue(@org.junit.jupiter.api.io.TempDir Path tempDir) throws Exception {
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		game = new Game(fleet);
		game.fireShots(List.of(new Position(1, 1), new Position(2, 2), new Position(3, 3)));
		Path saveFile = tempDir.resolve("game.json");

		game.save(saveFile);
		Game loaded = Game.load(saveFile);

		assertEquals(1, loaded.getAlienMoves().size());
		assertEquals(1, loaded.getHits());
		assertEquals(1, loaded.getSunkShips());
		assertEquals(0, loaded.getRemainingShips());
		assertTrue(loaded.repeatedShot(new Position(1, 1)));
		assertEquals("Barca", loaded.getAlienMoves().get(0).getShotResults().get(0).ship().getCategory());

		loaded.fireShots(List.of(new Position(4, 4), new Position(5, 5), new Position(6, 6)));
		assertEquals(2, loaded.getAlienMoves().size());
		assertEquals(2, loaded.getAlienMoves().get(1).getNumber());
	}
	@Test
	void getMyFleet() {
		assertNotNull(game.getMyFleet());
	}

	@Test
	void getAlienFleet() {
		assertNotNull(game.getAlienFleet());
		assertTrue(game.getAlienFleet().getShips().isEmpty());
	}

	@Test
	void getMyMoves() {
		assertNotNull(game.getMyMoves());
		assertTrue(game.getMyMoves().isEmpty());
	}

	@Test
	void getAlienMovesInitiallyEmpty() {
		assertNotNull(game.getAlienMoves());
		assertTrue(game.getAlienMoves().isEmpty());
	}

	@Test
	void fireShotsWithInvalidNumberOfShots() {
		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(List.of(
						new Position(1, 1),
						new Position(2, 2)
				)));

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(List.of(
						new Position(1, 1),
						new Position(2, 2),
						new Position(3, 3),
						new Position(4, 4)
				)));
	}

	@Test
	void fireShotsWithRepeatedPositionInSameMove() {
		List<IPosition> positions = List.of(
				new Position(2, 3),
				new Position(2, 3),
				new Position(4, 5)
		);

		game.fireShots(positions);

		assertEquals(1, game.getRepeatedShots());
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	void fireSingleShotOnWater() {
		IGame.ShotResult result = game.fireSingleShot(
				new Position(9, 9), false);

		assertTrue(result.valid());
		assertFalse(result.repeated());
		assertNull(result.ship());
		assertFalse(result.sunk());

		assertEquals(0, game.getHits());
		assertEquals(0, game.getSunkShips());
	}

	@Test
	void fireSingleShotOnShip() {
		Fleet fleet = new Fleet();
		Ship ship = new Barge(
				Compass.NORTH, new Position(1, 1));

		fleet.addShip(ship);
		game = new Game(fleet);

		IGame.ShotResult result = game.fireSingleShot(
				new Position(1, 1), false);

		assertTrue(result.valid());
		assertFalse(result.repeated());
		assertNotNull(result.ship());

		assertEquals(1, game.getHits());
	}

	@Test
	void fireSingleShotSinksShip() {
		Fleet fleet = new Fleet();
		Ship ship = new Barge(
				Compass.NORTH, new Position(1, 1));

		fleet.addShip(ship);
		game = new Game(fleet);

		IGame.ShotResult result = game.fireSingleShot(
				new Position(1, 1), false);

		assertTrue(result.sunk());
		assertEquals(1, game.getHits());
		assertEquals(1, game.getSunkShips());
		assertEquals(0, game.getRemainingShips());
	}

	@Test
	void fireSingleShotRepeatedByParameter() {
		IGame.ShotResult result = game.fireSingleShot(
				new Position(2, 3), true);

		assertTrue(result.valid());
		assertTrue(result.repeated());
		assertNull(result.ship());
		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	void fireSingleShotRepeatedAfterPreviousMove() {
		game.fireShots(List.of(
				new Position(2, 3),
				new Position(4, 5),
				new Position(6, 7)
		));

		IGame.ShotResult result = game.fireSingleShot(
				new Position(2, 3), false);

		assertTrue(result.repeated());
		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	void fireShotsWithInvalidPositions() {
		game.fireShots(List.of(
				new Position(-1, 3),
				new Position(2, 3),
				new Position(4, 5)
		));

		assertEquals(1, game.getInvalidShots());
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	void printBoardWithoutShotsAndLegend() {
		Game.printBoard(
				new Fleet(),
				List.of(),
				false,
				false
		);
	}

	@Test
	void printBoardWithShotsAndLegend() {
		Game.printBoard(
				new Fleet(),
				List.of(),
				true,
				true
		);
	}

	@Test
	void printMyBoardWithAllOptions() {
		game.printMyBoard(true, true);
	}

	@Test
	void printMyBoardWithoutOptions() {
		game.printMyBoard(false, false);
	}

	@Test
	void printAlienBoardWithAllOptions() {
		game.printAlienBoard(true, true);
	}

	@Test
	void printAlienBoardWithoutOptions() {
		game.printAlienBoard(false, false);
	}

	@Test
	void printBoardWithShip() {
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(
				Compass.NORTH, new Position(1, 1)));

		Game.printBoard(fleet, List.of(), false, true);
	}

	@Test
	void printBoardWithSunkShip() {
		Fleet fleet = new Fleet();
		Ship ship = new Barge(
				Compass.NORTH, new Position(1, 1));

		fleet.addShip(ship);
		ship.sink();

		Game.printBoard(fleet, List.of(), false, true);
	}

	@Test
	void jsonShotsWithMultiplePositions() {
		List<IPosition> positions = List.of(
				new Position(1, 1),
				new Position(2, 3),
				new Position(4, 5)
		);

		String json = Game.jsonShots(positions);

		assertNotNull(json);
		assertTrue(json.contains("row"));
		assertTrue(json.contains("column"));
	}

	@Test
	void jsonShotsWithEmptyList() {
		String json = Game.jsonShots(List.of());

		assertNotNull(json);
		assertEquals("[]", json.trim());
	}

	@Test
	void readEnemyFireWithCombinedPositions() {
		String input = "A1 B2 C3";

		String result = game.readEnemyFire(
				new java.util.Scanner(input));

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	void readEnemyFireWithSeparatePositions() {
		String input = "A 1 B 2 C 3";

		String result = game.readEnemyFire(
				new java.util.Scanner(input));

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	void readEnemyFireWithIncompletePosition() {
		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(
						new java.util.Scanner("A B2 C3")));
	}

	@Test
	void readEnemyFireWithLessThanThreePositions() {
		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(
						new java.util.Scanner("A1 B2")));
	}

	@Test
	void readEnemyFireWithInvalidPosition() {
		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(
						new java.util.Scanner("Z99 B2 C3")));
	}

	@Test
	void saveWithNullPath() {
		assertThrows(NullPointerException.class,
				() -> game.save(null));
	}

	@Test
	void loadWithNullPath() {
		assertThrows(NullPointerException.class,
				() -> Game.load(null));
	}

	@Test
	void loadWithInvalidJson(@org.junit.jupiter.api.io.TempDir Path tempDir)
			throws Exception {

		Path file = tempDir.resolve("invalid.json");

		java.nio.file.Files.writeString(file, "{ invalid json }");

		assertThrows(java.io.IOException.class,
				() -> Game.load(file));
	}

	@Test
	void loadWithUnsupportedVersion(@org.junit.jupiter.api.io.TempDir Path tempDir)
			throws Exception {

		Path file = tempDir.resolve("version.json");

		String json = """
                {
                  "version": 99,
                  "myFleet": [],
                  "alienFleet": [],
                  "alienMoves": [],
                  "myMoves": [],
                  "countInvalidShots": 0,
                  "countRepeatedShots": 0,
                  "countHits": 0,
                  "countSinks": 0,
                  "moveNumber": 1
                }
                """;

		java.nio.file.Files.writeString(file, json);

		assertThrows(java.io.IOException.class,
				() -> Game.load(file));
	}

	@Test
	void loadWithInvalidCounters(@org.junit.jupiter.api.io.TempDir Path tempDir)
			throws Exception {

		Path file = tempDir.resolve("counters.json");

		String json = """
                {
                  "version": 1,
                  "myFleet": [],
                  "alienFleet": [],
                  "alienMoves": [],
                  "myMoves": [],
                  "countInvalidShots": -1,
                  "countRepeatedShots": 0,
                  "countHits": 0,
                  "countSinks": 0,
                  "moveNumber": 1
                }
                """;

		java.nio.file.Files.writeString(file, json);

		assertThrows(java.io.IOException.class,
				() -> Game.load(file));
	}

	@Test
	void loadWithInvalidMoveNumber(@org.junit.jupiter.api.io.TempDir Path tempDir)
			throws Exception {

		Path file = tempDir.resolve("moveNumber.json");

		String json = """
                {
                  "version": 1,
                  "myFleet": [],
                  "alienFleet": [],
                  "alienMoves": [],
                  "myMoves": [],
                  "countInvalidShots": 0,
                  "countRepeatedShots": 0,
                  "countHits": 0,
                  "countSinks": 0,
                  "moveNumber": 0
                }
                """;

		java.nio.file.Files.writeString(file, json);

		assertThrows(java.io.IOException.class,
				() -> Game.load(file));
	}

	@Test
	void loadWithMissingFleet(@org.junit.jupiter.api.io.TempDir Path tempDir)
			throws Exception {

		Path file = tempDir.resolve("missingFleet.json");

		String json = """
                {
                  "version": 1,
                  "myFleet": null,
                  "alienFleet": [],
                  "alienMoves": [],
                  "myMoves": [],
                  "countInvalidShots": 0,
                  "countRepeatedShots": 0,
                  "countHits": 0,
                  "countSinks": 0,
                  "moveNumber": 1
                }
                """;

		java.nio.file.Files.writeString(file, json);

		assertThrows(java.io.IOException.class,
				() -> Game.load(file));
	}

	@Test
	void loadWithMissingMoveHistory(@org.junit.jupiter.api.io.TempDir Path tempDir)
			throws Exception {

		Path file = tempDir.resolve("missingMoves.json");

		String json = """
                {
                  "version": 1,
                  "myFleet": [],
                  "alienFleet": [],
                  "alienMoves": null,
                  "myMoves": [],
                  "countInvalidShots": 0,
                  "countRepeatedShots": 0,
                  "countHits": 0,
                  "countSinks": 0,
                  "moveNumber": 1
                }
                """;

		java.nio.file.Files.writeString(file, json);

		assertThrows(java.io.IOException.class,
				() -> Game.load(file));
	}

	@Test
	void overPrintsGameOverMessage() {
		game.over();
	}



	@Test
	void printBoardWithHitsAndMisses() {
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(
				Compass.NORTH, new Position(1, 1)));

		game = new Game(fleet);

		game.fireShots(List.of(
				new Position(1, 1),
				new Position(5, 5),
				new Position(7, 7)
		));

		game.printMyBoard(true, true);
	}


	@Test
	void randomEnemyFireWithLessThanThreeAvailablePositions() {
		List<IPosition> positions = new java.util.ArrayList<>();

		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				positions.add(new Position(row, col));
			}
		}

		// Utilizar 99 posições diferentes.
		for (int i = 0; i < 99; i += 3) {
			game.fireShots(List.of(
					positions.get(i),
					positions.get(i + 1),
					positions.get(i + 2)
			));
		}

		String result = game.randomEnemyFire();

		assertNotNull(result);
		assertEquals(34, game.getAlienMoves().size());

		IMove lastMove = game.getAlienMoves().get(33);

		assertEquals(3, lastMove.getShots().size());
	}
}