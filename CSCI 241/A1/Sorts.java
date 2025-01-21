package sort;
/* Author: Ben Fry-Holman   
 * Date: 7/9/2024
 * Description: Sorting Methods for int arrays.
 * */
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

public class Sorts {

   // maintains a count of comparisons performed by this Sorts object
  private int comparisonCount;

  public int getComparisonCount() {
    return comparisonCount;
  }

  public void resetComparisonCount() {
    comparisonCount = 0;
  }

  /** Sorts A[start..end] in place using insertion sort
    * Precondition: 0 <= start <= end <= A.length */
  public void insertionSort(int[] A, int start, int end) {
    // Invariant: A[start..i-1] is sorted
    for (int i = start + 1; i < end; i++) {
      int key = A[i];
      int j = i - 1;
      // Invariant: A[start..j] is sorted and all elements are <= key
      while (j >= start) {
        comparisonCount++;
        if (A[j] > key) {
          A[j + 1] = A[j];
          j = j - 1;
        } else {
          break;
        }
      }
      A[j + 1] = key;
    }
  }
  /** Partitions A[start..end] around the pivot A[pivIndex]; returns the
   *  pivot's new index.
   *  Precondition: start <= pivIndex < end
   *  Postcondition: If partition returns i, then
   *  A[start..i] <= A[i] <= A[i+1..end] 
   **/
  public int partition(int[] A, int start, int end, int pivIndex) {
    int pivot = A[pivIndex];
    swap(A, pivIndex, end - 1);
    int storeIndex = start;
    // Invariant: A[start..storeIndex-1] <= pivot and A[storeIndex..i-1] > pivot
    for (int i = start; i < end - 1; i++) {
      comparisonCount++;
      if (A[i] <= pivot) {
        swap(A, i, storeIndex);
        storeIndex++;
      }
    }
    swap(A, storeIndex, end - 1);
    return storeIndex;
  }

  /** use quicksort to sort the subarray A[start..end] */
  public void quickSort(int[] A, int start, int end) {
    if (start < end - 1) {
      int pivotIndex = (start + end) / 2;
      int newPivot = partition(A, start, end, pivotIndex);
      quickSort(A, start, newPivot);
      quickSort(A, newPivot + 1, end);
    }
  }

  /** merge the sorted subarrays A[start..mid] and A[mid..end] into
   *  a single sorted array in A. */
  public void merge(int[] A, int start, int mid, int end) {
    // Get copy of left and right
    int[] left = Arrays.copyOfRange(A, start, mid);
    int[] right = Arrays.copyOfRange(A, mid, end);
    int i = 0, j = 0, k = start;
    // Invariant: A[start..k-1] is sorted and 
    // contains the smallest elements from left and right
    while (i < left.length && j < right.length) {
      comparisonCount++;
      if (left[i] <= right[j]) {
        A[k++] = left[i++];
      } else {
        A[k++] = right[j++];
      }
    }
    // Invariant: A[start..k-1] is sorted and 
    // contains all elements from left[0..i-1] and right[0..j-1]
    // for each loop respectively 
    while (i < left.length) {
      A[k++] = left[i++];
    }
    while (j < right.length) {
      A[k++] = right[j++];
    }
  }

  /** use mergesort to sort the subarray A[start..end] */
  public void mergeSort(int[] A, int start, int end) {
    // Do recursion as specified in writeup
    if (end - start > 1) {
      int mid = (start + end) / 2;
      mergeSort(A, start, mid);
      mergeSort(A, mid, end);
      merge(A, start, mid, end);
    }
  }

  /** Sort A using LSD radix sort. */
  public void radixSort(int[] A) {
    // Get max and min from the array
    int max = Arrays.stream(A).max().getAsInt();
    int min = Arrays.stream(A).min().getAsInt();
    // Use max to get the difits
    int maxDigits = (int) Math.log10(max) + 1;
    if (min < 0) {
      for (int i = 0; i < A.length; i++) {
        A[i] -= min;
      }
      maxDigits = Math.max(maxDigits, (int) Math.log10(-min) + 1);
    }
    // D stands for digits
    // Iterate through each digit creating buckets along the way
    for (int d = 0; d < maxDigits; d++) {
      // Create 10 buckets for each digit (0-9)
      ArrayList<LinkedList<Integer>> buckets = new ArrayList<>(10);
      for (int i = 0; i < 10; i++) {
        buckets.add(new LinkedList<>());
      }
      // Place each number in the corresponding bucket based on the current digit
      for (int num : A) {
        int digit = getDigit(num, d);
        buckets.get(digit).add(num);
      }
      int fill = 0;
      // Collect the numbers from the buckets and put them back in the array
      for (Queue<Integer> bucket : buckets) {
        while (!bucket.isEmpty()) {
          A[fill++] = bucket.poll();
        }
      }
    }
    // Edge-case for negatives
    if (min < 0) {
      for (int i = 0; i < A.length; i++) {
        A[i] += min;
      }
    }
  }

  /* return the 10^d's place digit of n */
  private int getDigit(int n, int d) {
    return (n / ((int)Math.pow(10, d))) % 10;
  }

  /** swap a[i] and a[j]
   *  pre: 0 <= i, j < a.size
   *  post: values in a[i] and a[j] are swapped */
  public void swap(int[] a, int i, int j) {
    int tmp = a[i];
    a[i] = a[j];
    a[j] = tmp;
  }

}
