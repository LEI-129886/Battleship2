package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public record Move(int number, List<IPosition> shots, List<IGame.ShotResult> shotResults) implements IMove {

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public List<IPosition> getShots() {
		return shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {
		int repeatedShots = 0;
		int validShots = 0;
		int missedShots = 0;
		Map<String, Integer> hitsPerBoat = new HashMap<>();
		Map<String, Integer> sunkBoatsCount = new HashMap<>();

		for (IGame.ShotResult result : shotResults) {
			if (!result.valid())
				continue;
			if (result.repeated()) {
				repeatedShots++;
			} else {
				validShots++;
				if (result.ship() == null) {
					missedShots++;
				} else {
					String boatName = result.ship().getCategory();
					hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
					if (result.sunk())
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
				}
			}
		}

		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;
		if (verbose)
			printEnemyFireSummary(repeatedShots, validShots, missedShots, outsideShots, hitsPerBoat, sunkBoatsCount);

		Map<String, Object> response = new HashMap<>();
		response.put("validShots", validShots);
		response.put("outsideShots", outsideShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);

		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}
		response.put("sunkBoats", sunkBoats);

		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}
		response.put("hitsOnBoats", boatHits);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String jsonString = objectMapper.writeValueAsString(response);
			System.out.println(jsonString);
			System.out.println();
			return jsonString;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}
	}

	private void printEnemyFireSummary(int repeatedShots, int validShots, int missedShots, int outsideShots,
			Map<String, Integer> hitsPerBoat, Map<String, Integer> sunkBoatsCount) {
		StringBuilder output = new StringBuilder();
		if (validShots == 0 && repeatedShots > 0) {
			output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
		} else {
			if (validShots > 0)
				output.append(validShots).append(" tiro").append(validShots > 1 ? "s" : "").append(" válido").append(validShots > 1 ? "s" : "").append(": ");
			for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
				int count = entry.getValue();
				output.append(count).append(" ").append(entry.getKey()).append(count > 1 ? "s" : "").append(" ao fundo").append(" + ");
			}
			for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
				if (!sunkBoatsCount.containsKey(entry.getKey())) {
					int hits = entry.getValue();
					output.append(hits).append(" tiro").append(hits > 1 ? "s" : "").append(" num(a) ").append(entry.getKey()).append(" + ");
				}
			}
			if (missedShots > 0) {
				output.append(missedShots).append(" tiro").append(missedShots > 1 ? "s" : "").append(" na água");
			} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
				output.setLength(output.length() - 2);
			}
			if (repeatedShots > 0) {
				if (validShots > 0)
					output.append(", ");
				output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
			}
		}
		if (outsideShots > 0) {
			if (!output.isEmpty())
				output.append(", ");
			output.append(outsideShots).append(" tiro").append(outsideShots > 1 ? "s" : "").append(" exterior").append(outsideShots > 1 ? "es" : "");
		}
		System.out.println("Jogada nº" + number + " -> " + output);
	}
}