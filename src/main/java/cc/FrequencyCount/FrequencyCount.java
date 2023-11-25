package cc.FrequencyCount;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class FrequencyCount {
    public static final double SIMILARITY_THRESHOLD = 0.5;
    public static final int MAX_SIMILAR_WORDS_COUNT = 5;

    private static int searchInFile(File file, String targetWord) {
        int count = 0;

        Pattern splitPattern = Pattern.compile("[\\s\\p{Punct}]+");

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (word.equalsIgnoreCase(targetWord)) {
                    count++;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return  count;
    }

    private static Map<String, Integer> getFrequencyCount (String[] filenames, String searchString) {
        Map<String, Integer> result = new HashMap<>();

        for (String filename : filenames) {
            File file = new File(filename);

            if (file.exists() && file.isFile()) {
                int count = searchInFile(file, searchString);
                result.put(filename, count);
            }
        }

        return result;
    }

    public static List<WordFrequency> getMultipleWordsFrequencyCount(String[] filenames, String[] searchWords) {
        List<WordFrequency> fileWordFrequencies = new ArrayList<>();

        for (String filename : filenames) {
            File file = new File(filename);

            if (file.exists() && file.isFile()) {
                Map<String, Integer> wordMap = createWordFrequencyMap(file, searchWords);
                fileWordFrequencies.add(new WordFrequency(filename, wordMap));
            }
        }

        return fileWordFrequencies;

    }

    public static Map<String, Integer> createWordFrequencyMap(File file, String[] searchWords) {
        Map<String, Integer> wordMap = new HashMap<>();

        // Define a pattern to split the file content based on punctuations, spaces and tabs
        Pattern splitPattern = Pattern.compile("[\\s\\p{Punct}]+");

        try (BufferedReader bufferReader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = bufferReader.readLine()) != null) {
                // Convert the entire line to lower case for accurate frequency count
                String lowerCaseLine = currentLine.toLowerCase();
                String[] words = splitPattern.split(lowerCaseLine);

                for (String word : words) {
                    word = word.trim();
                    if (!word.isEmpty()) {
                        for (String searchWord : searchWords) {
                            if (word.equalsIgnoreCase(searchWord)) {
                                wordMap.put(word, wordMap.getOrDefault(word, 0) + 1);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return wordMap;

    }

    private static FrequentWord countFrequency(File file, String targetWord) {
        // Define a pattern to split the file content based on punctuations, spaces and tabs
        Pattern splitPattern = Pattern.compile("[\\s\\p{Punct}]+");

        List<SimilarWord> similarWords = new ArrayList<>();

        int count = 0;

        try {
            if (file.exists()) {
                try (BufferedReader bufferReader = new BufferedReader(new FileReader(file))) {
                    String currentLine;
                    while ((currentLine = bufferReader.readLine()) != null) {
                        // Convert the entire line to lower case for accurate frequency count
                        String lowerCaseLine = currentLine.toLowerCase();
                        String[] words = splitPattern.split(lowerCaseLine);

                        for (String word : words) {
                            word = word.trim();
                            if (!word.isEmpty()) {
                                getUpdatedSimilarWordsList(similarWords, word, targetWord);
                                if (word.equalsIgnoreCase(targetWord)) {
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        similarWords.sort((sw1, sw2) -> Double.compare(sw2.similarity, sw1.similarity));

        return new FrequentWord(count, similarWords);
    }

    private static void getUpdatedSimilarWordsList (List<SimilarWord> similarWords, String currentWord, String targetWOrd) {
        // Check if the word already exists in the list
        boolean wordExists = false;

        if (currentWord.equalsIgnoreCase(targetWOrd)) {
            return;
        }

        double similarity = EditDistance.getSimilarity(targetWOrd, currentWord);

        if (similarity < SIMILARITY_THRESHOLD) {
            return;
        }

        for (SimilarWord sw : similarWords) {
            if (sw.word.equals(currentWord)) {
                // If the word exists, increment the count
                sw.count++;
                wordExists = true;
                break; // No need to continue iterating
            }
        }

        // If the word doesn't exist, add it to the list
        if (!wordExists) {
            similarWords.add(new SimilarWord(currentWord, 1, similarity));
        }

    }

    private static Map<String, Integer> getWordFrequencyMap (File file, List<String> stopWordsList) {
        // Create a HashMap to store word frequencies
        Map<String, Integer> hMap = new HashMap<>();
        // Define a pattern to split the file content based on punctuations, spaces and tabs
        Pattern splitPattern = Pattern.compile("[\\s\\p{Punct}]+");

        // Check if files in the file exist or not
        if (file.exists()) {
            try (BufferedReader bufferReader = new BufferedReader(new FileReader(file))) {
                String currentLine;
                while ((currentLine = bufferReader.readLine()) != null) {
                    // Convert the entire line to lower case for accurate frequency count
                    String lowerCaseLine = currentLine.toLowerCase();
                    // Split the line into words
                    String[] words = splitPattern.split(lowerCaseLine);
                    // Update word frequencies in the HashMap
                    for (String word : words) {
                        word = word.trim();
                        if (!word.isEmpty() && !stopWordsList.contains(word)) {
                            // If the word already exists then increment its value by 1, if not then add 1 to the default value which is set to 0
                            hMap.put(word, hMap.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return hMap;
    }

    private static List<Map.Entry<String, Integer>> getWordFrequencySortedList(File file, List<String> stopWordsList) {

        Map<String, Integer> hMap = getWordFrequencyMap(file, stopWordsList);

        // Pass the hash map to ArrayList to convert the set of entries to a list of entries so that it can sorted later
        List<Map.Entry<String, Integer>> sortedFrequencyList = new ArrayList<>(hMap.entrySet());

        // Sort the array based on Value (frequency count) of each key value pair
        sortedFrequencyList.sort((first, second) -> second.getValue().compareTo(first.getValue()));

        return sortedFrequencyList;
    }

    private static List<Map.Entry<String, Integer>> getKMostFrequentWords(File file, List<String> stopWordsList, int k) {

        List<Map.Entry<String, Integer>> sortedFrequencyList = getWordFrequencySortedList(file, stopWordsList);

        List<Map.Entry<String, Integer>> kMostFrequentWOrdsList = new ArrayList<>();

        // Iterate over the sortedList to get the first n key value pairs (highest frequency)
        for (int n = 0; n < k && n < sortedFrequencyList.size(); n++) {
            kMostFrequentWOrdsList.add(sortedFrequencyList.get(n));
        }

        return  kMostFrequentWOrdsList;
    }

    private static void printWordFrequencies (Map<String, Integer> hMap) {
        // Print word frequencies to the console
        System.out.println("Word\t:\tFrequency");
        for (Map.Entry<String, Integer> entry : hMap.entrySet()) {
            System.out.println(entry.getKey() + "\t:\t" + entry.getValue());
        }
    }

    public static void main (String[] args) {
        // Testing getFrequencyCount method
//        String[] filenames = {"parsed_rental.ca/page_1_listing_1.txt", "parsed_rental.ca/page_1_listing_2.txt", "parsed_rental.ca/page_1_listing_3.txt", "parsed_rental.ca/page_1_listing_7.txt"};
//        String[] searchWords = {"windsor", "furnished", "apartment"};

        String[] filenames = {"storage/txt/common/doc1.txt", "storage/txt/common/doc2.txt", "storage/txt/common/doc3.txt"};
        String[] searchWords = {"document", "sample"};

        List<WordFrequency> wordFrequencies = getMultipleWordsFrequencyCount(filenames, searchWords );

        for (WordFrequency wordFrequency : wordFrequencies) {

            System.out.println(wordFrequency.filename + " -> " + wordFrequency.wordsCount);
        }

    }
}



