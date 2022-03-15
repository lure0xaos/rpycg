package gargoyle.rpycg.fx;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FXUtilTest {

    @Test
    void testFormat() {
        assertEquals("Hello User", FXUtil.format("Hello #{user}", Map.of("user", "User")));
    }

    @Test
    void testFormat1() {
        assertEquals("Hello User", FXUtil.format("Hello #{user}", Map.of(
                "user", "User",
                "user2", "User"
        )));
    }
    @Test
    void testFormat2() {
        assertEquals("Hello User #{unknown}", FXUtil.format("Hello #{user} #{unknown}", Map.of(
                "user", "User",
                "user2", "User"
        )));
    }
}
