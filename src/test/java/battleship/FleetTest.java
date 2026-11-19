package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Test class for Fleet.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - addShip: 3
 * - getShips: 1
 * - getShipsLike: 2
 * - getFloatingShips: 2
 * - shipAt: 2
 * - isInsideBoard: 3
 * - colisionRisk: 2
 */
	public class FleetTest {

		private Fleet fleet;

		@BeforeAll
		static void enableFleetAssertions() {
			ClassLoader classLoader = Fleet.class.getClassLoader();
			classLoader.setClassAssertionStatus(Fleet.class.getName(), true);
		}

		@BeforeEach
		void setUp() {
			fleet = new Fleet();
		}

		@AfterEach
		void tearDown() {
			fleet = null;
		}

		/**
		 * Test for the Fleet constructor.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testConstructor() {
			assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
		}

		@Test
		void testCreateRandom(){
			IFleet randomFleet = Fleet.createRandom();
			assertNotNull(randomFleet, "Error: createRandom should return a non-null fleet instance.");
			assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
					"Error: createRandom should create a fleet with the correct number of ships.");
		}

		/**
		 * Test for the addShip method (all conditions true).
		 * Cyclomatic Complexity: 3
		 */
		@Test
		void testAddShip1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
		}

		/**
		 * Test for the addShip method (fleet size limit reached).
		 */
		@Test
		void testAddShip2() {
			for (int row = 0; row < 6; row += 2) {
				for (int column = 0; column < 8; column += 2) {
					assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(row, column))));
				}
			}
			assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size());
			assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9))),
					"Error: Should not add a ship after the fleet limit is reached.");
		}

		/**
		 * Test for the addShip method (ship outside the board).
		 */
		@Test
		void testAddShip3() {
			IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
			assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
		}

		/**
		 * Test for the addShip method (collision risk).
		 */
		@Test
		void testAddShip4() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);
			assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
		}

		/**
		 * Test for the getShips method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testGetShips() {
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
			assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
		}

		/**
		 * Test for the getShipsLike method (ships of specific category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetShipsLike() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> barges = fleet.getShipsLike("Barca");
			assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
			assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
		}

		@Test
		void testGetShipsLikeOnEmptyFleet() {
			assertTrue(fleet.getShipsLike("Barca").isEmpty());
		}

		/**
		 * Test for the getFloatingShips method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetFloatingShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> floatingShips = fleet.getFloatingShips();
			assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

			ship1.getPositions().get(0).shoot();  // Sink ship1
			floatingShips = fleet.getFloatingShips();
			assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
			assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
		}

		@Test
		void testGetSunkShips() {
			IShip sunkShip = new Barge(Compass.NORTH, new Position(1, 1));
			IShip floatingShip = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(sunkShip);
			fleet.addShip(floatingShip);

			assertTrue(fleet.getSunkShips().isEmpty());
			sunkShip.sink();
			assertEquals(List.of(sunkShip), fleet.getSunkShips());
		}

		/**
		 * Test for the shipAt method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testShipAt() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
			assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
		}


		/**
		 * Test for private method colisionRisk.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testColisionRisk() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);

			assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
			assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
					"Error: Ships at non-overlapping positions should not have a collision risk.");
		}

		/**
		 * Test for the printStatus method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testPrintStatus() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			ship.sink();

			String output = captureOutput(fleet::printStatus);
			assertTrue(output.contains("0 a flutuar, 1 afundados!"));
		}

		@Test
		void testPrintShips() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			String emptyOutput = captureOutput(() -> fleet.printShips(List.of()));
			String shipOutput = captureOutput(() -> fleet.printShips(List.of(ship)));

			assertEquals("", emptyOutput);
			assertEquals(ship + System.lineSeparator(), shipOutput);
		}

		@Test
		void testPrintShipsByCategory() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			String output = captureOutput(() -> fleet.printShipsByCategory("Barca"));
			assertEquals(ship + System.lineSeparator(), output);
		}

		@Test
		void testPrintFloatingShips() {
			IShip floatingShip = new Barge(Compass.NORTH, new Position(1, 1));
			IShip sunkShip = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(floatingShip);
			fleet.addShip(sunkShip);
			sunkShip.sink();

			String output = captureOutput(fleet::printFloatingShips);
			assertEquals(floatingShip + System.lineSeparator(), output);
		}

		@Test
		void testPrintAllShips() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			String output = captureOutput(fleet::printAllShips);
			assertEquals(ship + System.lineSeparator(), output);
		}

		@Test
		void testNullArguments() throws Exception {
			assertThrows(AssertionError.class, () -> fleet.addShip(null));
			assertThrows(AssertionError.class, () -> fleet.getShipsLike(null));
			assertThrows(AssertionError.class, () -> fleet.shipAt(null));
			assertThrows(AssertionError.class, () -> fleet.printShips(null));
			assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null));

			var collisionRisk = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			collisionRisk.setAccessible(true);
			assertThrows(java.lang.reflect.InvocationTargetException.class,
					() -> collisionRisk.invoke(fleet, new Object[] {null}));
		}

		private String captureOutput(Runnable action) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PrintStream originalOut = System.out;
			try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
				System.setOut(capturedOut);
				action.run();
			} finally {
				System.setOut(originalOut);
			}
			return output.toString(StandardCharsets.UTF_8);
		}
	}