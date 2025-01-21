package avl;

// Author: Ben Fry-Holman
//Date: 7/18/2024
//Purpose: AVL and BST trees

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Unique {

    /**
     * Main program: prints the number of unique lines in a given file by one of two methods
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Requires 2 arguments: naive or avl and a filename.");
            return;
        }
        File f = new File(args[1]);
        try {
            Scanner sc = new Scanner(f);
            System.out.println("Finding unique lines in " + args[1]);
            if (args[0].equals("naive")) {
                System.out.println("Naive:");
                System.out.println(naiveUnique(sc));
            } else {
                System.out.println(args[1]);
                System.out.println("AVL:");
                System.out.println(avlUnique(sc));
            }
        } catch (FileNotFoundException exc) {
            System.out.println("Could not find file " + args[1] + ":" + f.getAbsolutePath());
        }
    }

    /**
     * Return the number of unique lines available to be read by sc
     */
    private static int naiveUnique(Scanner sc) {
        // unique lines seen so far
        ArrayList<String> seen = new ArrayList<String>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            // check if we've seen it:
            int i = 0;
            while (i < seen.size() && !line.equals(seen.get(i))) {
                i++;
            }
            if (i == seen.size()) {
                seen.add(line);
            }
        }
        return seen.size();
    }

    /**
     * Return the number of unique lines available to be read by sc
     */
    private static int avlUnique(Scanner sc) {
        // create new tree
        AVL avlTree = new AVL();
        
        // scan
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (avlTree.search(line) == null) {
                avlTree.avlInsert(line);
            }
        }
        // return size
        return avlTree.getSize();
    }
}
