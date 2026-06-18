package test;

/**
 * Minimal assertion library for the Finanz Core test suite.
 *
 * <p>Provides {@code assertEquals} and {@code assertTrue} overloads that throw
 * {@link AssertionError} with a descriptive message on failure. This demonstrates
 * how assertion frameworks work internally — no JUnit or external libraries required.</p>
 */
public final class TestAssertions {

    private TestAssertions() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " | esperado=" + expected + ", actual=" + actual);
        }
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(message + " | esperado=" + expected + ", actual=" + actual);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(message + " | esperado=" + expected + ", actual=" + actual);
    }
}
