package apriori;

public class Math {
	// used to get the hash table size, has a maximum value though
	public static int calculateCombination(long n, long r){
		if (n < r)
			return 0;
		long result = n;
		for (int i = 1; i < r; i++)
			result *= (n - i);
		for (int i = 1; i <= r; i++)
			result /= i;
		if (result > Config.maxHashTableSize)
			return Config.maxHashTableSize;
		else
			return (int) result;
	}

	// public static int hash(NonDuplicateArrayList<Integer> nonDuplicateArrayList, int hashTableSize){
	// 	int sum = 0;
	// 	for (Integer i: nonDuplicateArrayList)
	// 		sum += i;
	// 	return sum % hashTableSize;
	// }

	public static int hash(NonDuplicateArrayList<?> nonDuplicateArrayList, int hashTableSize){
		return java.lang.Math.abs(nonDuplicateArrayList.hashCode() % hashTableSize);
	}

	public static long binomial(int n, int k){
		if (k < 0 || k > n)
			return 0;
		if (k > n - k){
			k = n - k;
		}
		long c = 1;
		for (int i = 1; i < k + 1; i++){
			c *= (n - (k - i));
			c /= i;
		}
		return c;
	}
}
