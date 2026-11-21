package restserver;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;

public record CardParameters(PDPageContentStream content, float x, float y, float width, float height, String label,
                             String value, Color accent) {
}