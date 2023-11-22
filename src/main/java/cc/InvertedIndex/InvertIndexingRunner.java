package cc.InvertedIndex;

import cc.utils.FileReader;
import cc.utils.config;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class InvertIndexingRunner {
    private static InverseIndexing indexer;
    public static final String VALID_WORD_REGEX = "[A-Za-z0-9 ]+";

    public static void init(List<String> folders) {
        indexer = new InverseIndexing();

        addStopWordsTask();
        addWordsTask(folders);
    }

    private static void addStopWordsTask() {
        FileReader.readFile(config.stopwordsFilePath, FileReader.TYPE.WORDS)
                .forEach(indexer::addStopWords);
    }

    // may need to work on removing symbols
    private static void addWordsTask(List<String> folders) {
        for (String parentPath : folders) {
            String[] fileNames = FileReader.listAllFiles(parentPath);
            for (String fileName : fileNames) {
                indexer.addDocument(parentPath, fileName);
                System.out.printf("DONE - %s/%s\n", parentPath, fileName);
            }
        }
    }

    public static void main(String[] args) {
        List<String> folders = List.of(config.txtFolderPathLiv, config.txtFolderPathRental, config.txtFolderPathRentSeeker);
        init(folders);

        run(folders);
    }

    public static void run(List<String> folders) {
        boolean sessionFlag = true;
        int menuUserInput;
        while (sessionFlag) {
            menuUserInput = menuTakeUserInput();

            switch (menuUserInput) {
                case 1 -> searchDocument();
                case 2 -> init(folders);
                default -> sessionFlag = false;
            }
        }
    }

    // take menu item input from user as integer
    private static int menuTakeUserInput() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter 1 : To search for words in the crawled documents");
        System.out.println("Enter 2 : To Reload the indexing");
        System.out.println("Enter 3 : To Return to main menu");

        String input = sc.next();

        while (!input.matches("[0-9]")) {
            System.out.println("Please enter a valid input");
            input = sc.next();
        }

        return Integer.parseInt(input);
    }

    // Search for documents containing a word
    private static void searchDocument() {
        String query = queryTakeUserInput();
        Set<String> result = indexer.search(query);

        if (result.isEmpty()) {
            System.out.printf("No documents found containing the query \"%s\"\n", query);
            System.out.println("Try again with another search query");
        } else {
            System.out.printf("Documents containing the query \"%s\" : ", query);
            System.out.printf("%s \n", String.join(" , ", result));
        }
        System.out.println();
    }

    // Take valid word input from user
    private static String queryTakeUserInput() {
        // Function InvertIndexing
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the word you want to search among the documents : ");
        String query = sc.nextLine().trim();

        while (!query.matches(VALID_WORD_REGEX)) {
            System.out.println("Please enter a valid word!");
            query = sc.nextLine();
        }

        return query;
    }
}
