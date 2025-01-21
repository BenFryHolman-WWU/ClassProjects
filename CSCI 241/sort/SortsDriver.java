package sort;
/* Author: Ben Fry-Holman 
 * Date: 7/9/2024
 * Description: Individual sorting options.
 * */

import java.util.Random;
import java.util.Scanner;

public class SortsDriver {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Random random = new Random();

    System.out.print("Enter sort (i[nsertion], q[uick], m[erge], r[adix], a[ll]): ");
    char sortType = scanner.next().charAt(0);

    System.out.print("Enter n (size of array to sort): ");
    int n = scanner.nextInt();

    int[] array = new int[n];
    for (int i = 0; i < n; i++) {
        array[i] = random.nextInt(2 * n + 1) - n;
    }

    Sorts sorts = new Sorts();
    int[] arrayCopy = array.clone();

    // Not pretty but works.
    switch (sortType) {
      case 'i':
        System.out.println("Unsorted: " + arrayToString(array));
        sorts.resetComparisonCount();
        sorts.insertionSort(array, 0, n);
        System.out.println("Sorted: " + arrayToString(array));
        System.out.println("Comparisons: " + sorts.getComparisonCount());
      case 'q':
        System.out.println("Unsorted: " + arrayToString(array));
        sorts.resetComparisonCount();
        sorts.quickSort(array, 0, n);
        System.out.println("Sorted: " + arrayToString(array));
        System.out.println("Comparisons: " + sorts.getComparisonCount());
      case 'm':
        System.out.println("Unsorted: " + arrayToString(array));
        sorts.resetComparisonCount();
        sorts.mergeSort(array, 0, n);
        System.out.println("Sorted: " + arrayToString(array));
        System.out.println("Comparisons: " + sorts.getComparisonCount());
      case 'r':
        System.out.println("Unsorted: " + arrayToString(array));
        sorts.resetComparisonCount();
        sorts.radixSort(array);
        System.out.println("Sorted: " + arrayToString(array));
        System.out.println("Comparisons: " + sorts.getComparisonCount());
      case 'a':
        System.out.println("Unsorted: " + arrayToString(array));
        sorts.resetComparisonCount();
        sorts.insertionSort(arrayCopy = array.clone(), 0, n);
        System.out.println("insertion: " + sorts.getComparisonCount());
        System.out.println("Sorted: " + arrayToString(arrayCopy));

        sorts.resetComparisonCount();
        sorts.quickSort(arrayCopy = array.clone(), 0, n);
        System.out.println("quick: " + sorts.getComparisonCount());
        System.out.println("Sorted: " + arrayToString(arrayCopy));

        sorts.resetComparisonCount();
        sorts.mergeSort(arrayCopy = array.clone(), 0, n);
        System.out.println("merge: " + sorts.getComparisonCount());
        System.out.println("Sorted: " + arrayToString(arrayCopy));

        sorts.resetComparisonCount();
        sorts.radixSort(arrayCopy = array.clone());
        System.out.println("radix: " + sorts.getComparisonCount());
        System.out.println("Sorted: " + arrayToString(arrayCopy));
      default:
        System.out.println("Invalid sort type.");
    }
    scanner.close();
  }
  //we need to get the aray to a string so make a basic array to String method
  private static String arrayToString(int[] array) {
    if (array.length <= 20) {
      StringBuilder string = new StringBuilder("[");
      for (int num : array) {
          string.append(num).append(" ");
      }
      string.setLength(string.length() - 1); 
      string.append("]");
      return string.toString();
    } else {
      return "\n";
    }
  }
}
