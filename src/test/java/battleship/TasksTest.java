package battleship;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TasksTest
 *
 * @author Your Name
 * Date: 07/01/2026
 * Time: 00:53
 */
class TasksTest {

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void taskA() {
	}

	@Test
	void taskB() {
	}

	@Test
	void taskC() {
	}

	@Test
	void taskD() {
	}

	@Test
	void buildFleet() {
	}

	@Test
	void readShip() {
	}

	@Test
	void readPosition() {
	}

	@Test
	void firingRound() {
	}

	@Test
	void savesAndLoadsGameThroughMenuCommands(@TempDir Path tempDir) throws Exception {
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
		InputStream originalInput = System.in;
		try {
			System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
			Tasks.menu();
		} finally {
			System.setIn(originalInput);
		}

		Game restored = Game.load(copiedSave);
		assertEquals(1, restored.getHits());
		assertEquals(1, restored.getAlienMoves().size());
	}
}