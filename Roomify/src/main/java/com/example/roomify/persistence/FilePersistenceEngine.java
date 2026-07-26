package com.example.roomify.persistence;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic File Persistence Engine
 * Handles saving and loading serialized objects.
 */
public class FilePersistenceEngine {

    /**
     * Saves a list of objects into a binary file.
     */
    public static <T> void saveObjects(List<T> objects, String fileName) {

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(fileName))) {

            out.writeObject(objects);

            System.out.println("Data saved successfully to " + fileName);

        } catch (IOException e) {

            System.out.println("Error saving data: " + e.getMessage());

        }

    }

    /**
     * Loads objects from a binary file.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> loadObjects(String fileName) {

        File file = new File(fileName);

        if (!file.exists()) {

            return new ArrayList<>();

        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(fileName))) {

            return (List<T>) in.readObject();

        } catch (IOException | ClassNotFoundException e) {

            System.out.println("Error loading data: " + e.getMessage());

        }

        return new ArrayList<>();

    }

    /**
     * Saves text into a normal text file.
     */
    public static void writeText(String fileName, String text) {

        try (FileWriter writer = new FileWriter(fileName, true)) {

            writer.write(text);
            writer.write(System.lineSeparator());

        } catch (IOException e) {

            System.out.println("Error writing text file.");

        }

    }

    /**
     * Reads a complete text file.
     */
    public static List<String> readText(String fileName) {

        List<String> lines = new ArrayList<>();

        File file = new File(fileName);

        if (!file.exists()) {

            return lines;

        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                lines.add(line);

            }

        } catch (IOException e) {

            System.out.println("Error reading text file.");

        }

        return lines;

    }

}