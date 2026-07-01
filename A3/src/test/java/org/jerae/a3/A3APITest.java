package org.jerae.a3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class A3APITest {

    @Test
    public void testMaterialTitleCase() {
        String rawName = "STONE_SWORD";
        String[] parts = rawName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        assertEquals("Stone Sword", sb.toString().trim());
    }
}
