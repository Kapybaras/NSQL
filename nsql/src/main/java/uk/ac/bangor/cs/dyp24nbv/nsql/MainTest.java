package uk.ac.bangor.cs.dyp24nbv.nsql;

import java.util.Scanner;

public class MainTest {
    public static void main(String[] args) {
        GeminiManager nsqlManager = new GeminiManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- NSQL Prototype: Dual-Layer Mode Active ---");
        System.out.println("Ready for natural language queries. Type 'exit' to quit.");

        while (true) {
            System.out.print("\nEnter your request: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) break;

            // This triggers Layer 1 (Generate) and Layer 2 (Validate)
            String response = nsqlManager.askAI(input);
            
            System.out.println("------------------------------");
            System.out.println("FINAL OUTPUT: " + response);
            System.out.println("------------------------------");
        }
        
        scanner.close();
        System.out.println("System offline.");
    }
}