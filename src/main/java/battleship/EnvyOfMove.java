package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvyOfMove {
    public EnvyOfMove(){}
    public static String moveString(int number, int shotSize, int resSize){
        return "Move{" +
                "number=" + number +
                ", shots=" + shotSize +
                ", results=" + resSize +
                '}';
    }
    public static int getNumMove(int number){return number;}
    public static List<IPosition> getShots(List<IPosition> shots) {
        return shots;
    }
    public static List<IGame.ShotResult> getShotResults(List<IGame.ShotResult> shotResults) {
        return shotResults;
    }
    public static String prcessShot(int number,List<IGame.ShotResult> shotResults, int repeatedShots, int validShots, int missedShots, Map<String, Integer> hitsPerBoat,Map<String, Integer> sunkBoatsCount,boolean verbose){
        // Processar cada resultado de tiro
        for (IGame.ShotResult result : shotResults) {
            if (!result.valid()) {
                // Tiro inválido - apenas ignorar
                continue;
            }

            if (result.repeated())
                repeatedShots++; // tiro repetido
            else {
                // Tiro válido
                validShots++;
                if (result.ship() == null)
                    missedShots++; // Tiro na água
                else{
                    String boatName = result.ship().getCategory();
                    hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
                    if (result.sunk())
                        sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1); // Contar barcos do mesmo tipo afundados
                }
            }
        }
        // Determinar número de tiros fora do tabuleiro
        int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

        if (verbose) {
            // Construção da mensagem de saída
            StringBuilder output = new StringBuilder();

            if (validShots == 0 && repeatedShots > 0) {
                output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
            } else {
                if (validShots > 0) {
                    output.append(validShots).append(" tiro").append(validShots > 1 ? "s" : "").append(" válido").append(validShots > 1 ? "s" : "").append(": ");
                }

                // Atualizar lógica para contar múltiplos barcos afundados do mesmo tipo
                if (!sunkBoatsCount.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
                        String boatName = entry.getKey();
                        int count = entry.getValue();
                        output.append(count).append(" ").append(boatName).append(count > 1 ? "s" : "").append(" ao fundo").append(" + ");
                    }
                }

                if (!hitsPerBoat.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
                        String boatName = entry.getKey();
                        int hits = entry.getValue();
                        if (!sunkBoatsCount.containsKey(boatName)) {
                            output.append(hits).append(" tiro").append(hits > 1 ? "s" : "").append(" num(a) ").append(boatName).append(" + ");
                        }
                    }
                }

                if (missedShots > 0) {
                    output.append(missedShots).append(" tiro").append(missedShots > 1 ? "s" : "").append(" na água");
                } else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
                    output.setLength(output.length() - 2); // Remover o "+" final
                }

                if (repeatedShots > 0) {
                    if (validShots > 0) {
                        output.append(", ");
                    }
                    output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
                }
            }

            // Adicionar contagem de tiros fora do tabuleiro
            if (outsideShots > 0) {
                if (!output.isEmpty()) {
                    output.append(", ");
                }
                output.append(outsideShots).append(" tiro").append(outsideShots > 1 ? "s" : "").append(" exterior").append(outsideShots > 1 ? "es" : "");
            }

            // Imprimir na consola se verbose for true
            System.out.println("Jogada nº" + number + " -> " + output);
        }

        // Criar o mapa para o JSON
        Map<String, Object> response = new HashMap<>();
        response.put("validShots", validShots);
        response.put("outsideShots", outsideShots);
        response.put("repeatedShots", repeatedShots);
        response.put("missedShots", missedShots);

        // Criar a lista de barcos afundados
        List<Map<String, Object>> sunkBoats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
            Map<String, Object> boat = new HashMap<>();
            boat.put("type", entry.getKey());
            boat.put("count", entry.getValue());
            sunkBoats.add(boat);
        }
        response.put("sunkBoats", sunkBoats);

        // Criar a lista de acertos em barcos que não foram afundados
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

        // Serializar o JSON utilizando Jackson
        String jsonString;

        // Serializar os tiros gerados em JSON usando a biblioteca Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            jsonString = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
        }

        System.out.println(jsonString);
        System.out.println();

        // Retornar o JSON
        return jsonString;

    }
}
