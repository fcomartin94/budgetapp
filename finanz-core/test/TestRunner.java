package test;

/**
 * Entry point for the Finanz Core test suite.
 *
 * <p>Runs all test classes sequentially and prints a final {@code OK / FAIL} count.
 * Exits with code {@code 1} if any suite fails, so the result can be checked in CI
 * or shell scripts.</p>
 *
 * <p>Run with: {@code java -cp out test.TestRunner}</p>
 */
public class TestRunner {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        try {
            BudgetServiceTest.runAll();
            System.out.println("[OK] BudgetServiceTest");
            passed++;
        } catch (Throwable t) {
            System.out.println("[FAIL] BudgetServiceTest: " + t.getMessage());
            failed++;
        }

        try {
            TransaccionRepositoryTest.runAll();
            System.out.println("[OK] TransaccionRepositoryTest");
            passed++;
        } catch (Throwable t) {
            System.out.println("[FAIL] TransaccionRepositoryTest: " + t.getMessage());
            failed++;
        }

        System.out.println("\nResultado tests -> OK: " + passed + ", FAIL: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
