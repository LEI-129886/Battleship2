package restserver;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class GameReportPdfGenerator {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault());

	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
	private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
	private static final float MARGIN = 42;
	private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);

	private static final Color NAVY = new Color(24, 43, 65);
	private static final Color TEAL = new Color(24, 145, 145);
	private static final Color SKY = new Color(226, 242, 243);
	private static final Color INK = new Color(38, 50, 56);
	private static final Color MUTED = new Color(100, 116, 125);
	private static final Color LINE = new Color(220, 228, 231);
	private static final Color ROW = new Color(247, 250, 250);

	private GameReportPdfGenerator() {}

	public static byte[] generate(GameSession session) throws IOException {
		List<GameSession.ReportEntry> entries = session.getReportEntries();
		int water = count(entries, "Agua");
		int hits = count(entries, "Tiro");
		int sunk = count(entries, "Afundou");

		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			PDPageContentStream content = new PDPageContentStream(document, page);
			drawHeader(content, session, 1);
			drawSummary(content, session, water, hits, sunk);
			drawTableHeader(content, 302);

			float y = 278;
			int pageNumber = 1;
			for (int index = 0; index < entries.size(); index++) {
				if (y < 62) {
					drawFooter(content, pageNumber);
					content.close();
					pageNumber++;
					page = new PDPage(PDRectangle.A4);
					document.addPage(page);
					content = new PDPageContentStream(document, page);
					drawHeader(content, session, pageNumber);
					drawTableHeader(content, 700);
					y = 676;
				}
				drawTableRow(content, entries.get(index), y, index % 2 == 0);
				y -= 25;
			}
			if (entries.isEmpty()) {
				drawText(content, "Ainda nao existem jogadas registadas.", 10, MARGIN, 270, MUTED);
			}
			drawFooter(content, pageNumber);
			content.close();

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			document.save(output);
			return output.toByteArray();
		}
	}

	private static void drawHeader(PDPageContentStream content, GameSession session, int pageNumber)
			throws IOException {
		content.setNonStrokingColor(NAVY);
		content.addRect(0, PAGE_HEIGHT - 96, PAGE_WIDTH, 96);
		content.fill();
		drawText(content, "BATTLESHIP", 10, MARGIN, PAGE_HEIGHT - 31, SKY);
		drawText(content, "Relatorio da partida", 23, MARGIN, PAGE_HEIGHT - 62, Color.WHITE);
		drawText(content, "Pagina " + pageNumber, 9, PAGE_WIDTH - 88, PAGE_HEIGHT - 31, SKY);
	}

	private static void drawSummary(PDPageContentStream content, GameSession session,
			int water, int hits, int sunk) throws IOException {
		drawText(content, "RESUMO DA PARTIDA", 9, MARGIN, 713, TEAL);
		drawText(content, "Identificador", 8, MARGIN, 688, MUTED);
		drawText(content, session.getGameId(), 10, MARGIN, 672, INK);
		drawText(content, "Jogador", 8, 330, 688, MUTED);
		drawText(content, session.getPlayerName(), 10, 330, 672, INK);
		drawText(content, "Adversario", 8, MARGIN, 650, MUTED);
		drawText(content, "IA", 10, MARGIN, 634, INK);
		drawText(content, "Data e hora", 8, 330, 650, MUTED);
		drawText(content, DATE_FORMAT.format(session.getStartedAt()), 10, 330, 634, INK);

		drawCard(content, MARGIN, 550, 160, 53, "RESULTADO", result(session), resultColor(session));
		drawCard(content, 217, 550, 107, 53, "JOGADAS", String.valueOf(session.getReportEntries().size()), TEAL);
		drawCard(content, 341, 550, 99, 53, "TURNOS", String.valueOf(turns(session.getReportEntries())), TEAL);
		drawCard(content, 457, 550, 96, 53, "TIROS", String.valueOf(water + hits + sunk), TEAL);

		drawText(content, "DISTRIBUICAO DOS TIROS", 9, MARGIN, 515, TEAL);
		int maximum = Math.max(1, Math.max(water, Math.max(hits, sunk)));
		drawBar(content, "Agua", water, maximum, 470, new Color(104, 169, 184));
		drawBar(content, "Tiro", hits, maximum, 441, new Color(24, 145, 145));
		drawBar(content, "Afundou", sunk, maximum, 412, new Color(235, 150, 69));
	}

	private static void drawCard(PDPageContentStream content, float x, float y, float width,
			float height, String label, String value, Color accent) throws IOException {
		content.setNonStrokingColor(new Color(247, 250, 250));
		content.addRect(x, y, width, height);
		content.fill();
		content.setNonStrokingColor(accent);
		content.addRect(x, y, 4, height);
		content.fill();
		drawText(content, label, 7, x + 12, y + 37, MUTED);
		drawText(content, value, 13, x + 12, y + 16, accent);
	}

	private static void drawBar(PDPageContentStream content, String label, int value, int maximum,
			float y, Color color) throws IOException {
		content.setNonStrokingColor(LINE);
		content.addRect(105, y, 350, 12);
		content.fill();
		if (value > 0) {
			content.setNonStrokingColor(color);
			content.addRect(105, y, Math.max(8, Math.min(350, value * 350f / maximum)), 12);
			content.fill();
		}
		drawText(content, label, 9, MARGIN, y + 2, INK);
		drawText(content, String.valueOf(value), 9, 465, y + 2, INK);
	}

	private static void drawTableHeader(PDPageContentStream content, float y) throws IOException {
		content.setNonStrokingColor(NAVY);
		content.addRect(MARGIN, y - 5, CONTENT_WIDTH, 23);
		content.fill();
		drawText(content, "TURNO", 8, 54, y + 3, Color.WHITE);
		drawText(content, "JOGADOR", 8, 116, y + 3, Color.WHITE);
		drawText(content, "COORDENADA", 8, 330, y + 3, Color.WHITE);
		drawText(content, "RESULTADO", 8, 440, y + 3, Color.WHITE);
	}

	private static void drawTableRow(PDPageContentStream content, GameSession.ReportEntry entry,
			float y, boolean alternate) throws IOException {
		if (alternate) {
			content.setNonStrokingColor(ROW);
			content.addRect(MARGIN, y - 8, CONTENT_WIDTH, 25);
			content.fill();
		}
		drawText(content, String.valueOf(entry.turn()), 9, 54, y, INK);
		drawText(content, entry.player(), 9, 116, y, INK);
		drawText(content, entry.coordinate(), 9, 330, y, INK);
		Color outcomeColor = "Afundou".equals(entry.outcome()) ? new Color(190, 112, 30) : TEAL;
		drawText(content, entry.outcome(), 9, 440, y, outcomeColor);
	}

	private static void drawFooter(PDPageContentStream content, int pageNumber) throws IOException {
		content.setStrokingColor(LINE);
		content.setLineWidth(0.6f);
		content.moveTo(MARGIN, 38);
		content.lineTo(PAGE_WIDTH - MARGIN, 38);
		content.stroke();
		drawText(content, "Battleship 2.0 - historico de jogo", 8, MARGIN, 24, MUTED);
		drawText(content, "" + pageNumber, 8, PAGE_WIDTH - MARGIN - 8, 24, MUTED);
	}

	private static int count(List<GameSession.ReportEntry> entries, String outcome) {
		return (int) entries.stream().filter(entry -> outcome.equals(entry.outcome())).count();
	}

	private static int turns(List<GameSession.ReportEntry> entries) {
		return entries.stream().mapToInt(GameSession.ReportEntry::turn).max().orElse(0);
	}

	private static String result(GameSession session) {
		if ("STUDENT_WINS".equals(session.getWinner())) return "Vitoria";
		if ("AI_WINS".equals(session.getWinner())) return "Derrota";
		return "Em curso";
	}

	private static Color resultColor(GameSession session) {
		if ("STUDENT_WINS".equals(session.getWinner())) return new Color(35, 143, 91);
		if ("AI_WINS".equals(session.getWinner())) return new Color(196, 80, 73);
		return TEAL;
	}

	private static void drawText(PDPageContentStream content, String text, float size, float x,
			float y, Color color) throws IOException {
		content.beginText();
		content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
		content.setNonStrokingColor(color);
		content.newLineAtOffset(x, y);
		content.showText(text);
		content.endText();
	}
}