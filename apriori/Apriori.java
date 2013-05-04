package apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import jsonobject.DataSet;
import jsonobject.User;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Implements the Apriori Algorithm(with Direct Hashing and Pruning)
 * Reference: 	An Effective Hash_Based Algorithm for Mining Association Rules
 * 				Jong Soo Park, Ming-Syan Chen, Philip S. Yu
 */

public class Apriori {
	public static void main(String[] args){
		Apriori apriori = new Apriori();
		apriori.start();
	}
	
	public Apriori(){
	}

	public void start(){
		int iteration, numOfFrequentItemSets = 0, numOfFreqItems = 0;
		DataSet dataSet = readData(Config.filename);
		ArrayList<NonDuplicateArrayList<String>> transactions = getTransactions(dataSet);
		long startTime = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("##.#####");
		PrintWriter printWriter = null;
		String outFilename = "results/" + Config.filename + "_" + df.format(Config.minSup) + ".txt";

		try {
			printWriter = new PrintWriter(new FileOutputStream(outFilename, false), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// the first pass
		Config.minSupCount = (int) java.lang.Math.ceil(transactions.size() * Config.minSup);
		int[] hashTable = new int[Math.calculateCombination(dataSet.getLocations().getLocationMap().size(), 2)];
		HashMap<NonDuplicateArrayList<String>, Integer> candidates = new HashMap<NonDuplicateArrayList<String>, Integer>();
		ArrayList<NonDuplicateArrayList<String>> frequentItemSet = new ArrayList<NonDuplicateArrayList<String>>();
		for (NonDuplicateArrayList<String> transaction: transactions){
			for (NonDuplicateArrayList<String> subset: getKSubset(transaction, 1)){
				if (candidates.containsKey(subset))
					candidates.put(subset, candidates.get(subset) + 1);
				else
					candidates.put(subset, 1);
			}
			for (NonDuplicateArrayList<String> subset: getKSubset(transaction, 2))
				hashTable[Math.hash(subset, hashTable.length)]++;
		}
		frequentItemSet.clear();
		for (Entry<NonDuplicateArrayList<String>, Integer> entry: candidates.entrySet()){
			if (entry.getValue() >= Config.minSupCount)
				frequentItemSet.add(entry.getKey());
		}
		printFrequentItemSet(frequentItemSet, printWriter);
		numOfFreqItems += frequentItemSet.size();
		numOfFrequentItemSets += frequentItemSet.size();

		// Direct hashing and pruning
		iteration = 2;
		while (frequentItemSet.size() > 0 && iteration < Config.DHPThreshold){
			System.out.println("generating candidates in iteration: " + iteration);
			generateCandidates(frequentItemSet, hashTable, candidates, iteration);
			int[] newHashTable = new int[Math.calculateCombination(dataSet.getLocations().getLocationMap().size(), iteration + 1)];
			ArrayList<NonDuplicateArrayList<String>> newTransactions = new ArrayList<NonDuplicateArrayList<String>>();
			for (NonDuplicateArrayList<String> transaction: transactions){
				NonDuplicateArrayList<String> newTransaction = new NonDuplicateArrayList<String>();
				countSupport(transaction, candidates, iteration, newTransaction);
				if (newTransaction.size() > iteration){
					NonDuplicateArrayList<String> newnewTransaction = new NonDuplicateArrayList<String>();
					makeHashTable(newTransaction, hashTable, iteration, newHashTable, newnewTransaction);
					if (newnewTransaction.size() > iteration)
						newTransactions.add(newnewTransaction);
				}
			}
			hashTable = newHashTable;
			transactions = newTransactions;
			frequentItemSet.clear();
			System.out.println("generating frequent itemsets in iteration: " + iteration);
			for (Entry<NonDuplicateArrayList<String>, Integer> entry: candidates.entrySet()){
				if (entry.getValue() >= Config.minSupCount)
					frequentItemSet.add(entry.getKey());
			}
			printFrequentItemSet(frequentItemSet, printWriter);
			numOfFrequentItemSets += frequentItemSet.size();
			numOfFreqItems += frequentItemSet.size() * iteration;
			iteration++;
		}

		// original Apriori
		generateCandidates(frequentItemSet, hashTable, candidates, iteration);
		while (frequentItemSet.size() > 0){
			ArrayList<NonDuplicateArrayList<String>> newTransactions = new ArrayList<NonDuplicateArrayList<String>>();
			for (NonDuplicateArrayList<String> transaction: transactions){
				NonDuplicateArrayList<String> newTransaction = new NonDuplicateArrayList<String>();
				countSupport(transaction, candidates, iteration, newTransaction);
				if (newTransaction.size() > iteration)
					newTransactions.add(newTransaction);
			}
			frequentItemSet.clear();
			System.out.println("generating frequent itemsets in iteration: " + iteration);
			for (Entry<NonDuplicateArrayList<String>, Integer> entry: candidates.entrySet()){
				if (entry.getValue() >= Config.minSupCount)
					frequentItemSet.add(entry.getKey());
			}
			printFrequentItemSet(frequentItemSet, printWriter);
			numOfFrequentItemSets += frequentItemSet.size();
			numOfFreqItems += frequentItemSet.size() * iteration;
			if (newTransactions.size() == 0)
				break;
			System.out.println("generating candidates in iteration: " + iteration);
			aprioriGen(frequentItemSet, candidates, iteration);
			iteration++;
		}

		// print out the results
		long endTime = System.currentTimeMillis();
		printWriter.println();
		printWriter.println("------------min support count: " + Config.minSupCount + " ----------------------------------------");
		printWriter.println();
		printWriter.println("Running time: " + (double)(endTime - startTime) / 1000);
		printWriter.println("Number of frequent itemsets: " + numOfFrequentItemSets);
		printWriter.println("Average items per frequent itemsets: " + df.format((double) numOfFreqItems / numOfFrequentItemSets));
		printWriter.println("---------------------------------------------------------------------------");
		printWriter.println("# of users: " + dataSet.getUsers().size());
		printWriter.println("# of trajectories: " + dataSet.getTotalNumOfTrajectories());
		printWriter.println("# of records: " + dataSet.getTotalNumOfRecords());
		printWriter.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
		printWriter.close();
		System.out.println("Done");
	}

	// private void aprioriGen(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet, 
	// 						HashMap<NonDuplicateArrayList<Integer>, Integer> candidates,
	// 						int iteration){
	private void aprioriGen(ArrayList<NonDuplicateArrayList<String>> frequentItemSet, 
							HashMap<NonDuplicateArrayList<String>, Integer> candidates,
							int iteration){
		candidates.clear();
		for (int i = 0; i < frequentItemSet.size(); i++){
			for (int j = i + 1; j < frequentItemSet.size(); j++){
				NonDuplicateArrayList<String> tempSet = new NonDuplicateArrayList<String>(frequentItemSet.get(i));
				tempSet.retainAll(frequentItemSet.get(j));
				if (tempSet.size() == iteration - 1){
					tempSet = new NonDuplicateArrayList<String>(frequentItemSet.get(i));
					tempSet.addAll(frequentItemSet.get(j));
					candidates.put(tempSet, 0);
				}
			}
		}
		HashSet<NonDuplicateArrayList<String>> toRemoveSet = new HashSet<NonDuplicateArrayList<String>>();
		for (Entry<NonDuplicateArrayList<String>, Integer> entry: candidates.entrySet()){
			for (NonDuplicateArrayList<String> set: getKSubset(entry.getKey(), iteration)){
				if (!frequentItemSet.contains(set)){
					toRemoveSet.add(set);
					break;
				}
			}
		}
		for (NonDuplicateArrayList<String> set: toRemoveSet)
			candidates.remove(set);
	}

	// private void generateCandidates(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet, 
	// 								int[] hashTable, 
	// 								HashMap<NonDuplicateArrayList<Integer>, Integer> candidates, int iteration){
	private void generateCandidates(ArrayList<NonDuplicateArrayList<String>> frequentItemSet, 
									int[] hashTable, 
									HashMap<NonDuplicateArrayList<String>, Integer> candidates, int iteration){
		candidates.clear();
		for(int i = 0; i < frequentItemSet.size(); i++){
			for (int j = i + 1; j < frequentItemSet.size(); j++){
				NonDuplicateArrayList<String> tempSet = new NonDuplicateArrayList<String>(frequentItemSet.get(i));
				tempSet.retainAll(frequentItemSet.get(j));
				if (tempSet.size() == iteration - 2){
					tempSet = new NonDuplicateArrayList<String>(frequentItemSet.get(i));
					tempSet.addAll(frequentItemSet.get(j));
					if (hashTable[Math.hash(tempSet, hashTable.length)] >= Config.minSupCount)
						candidates.put(tempSet, 0);
				}
			}
		}
	}

	// private void countSupport(NonDuplicateArrayList<Integer> transaction, 
	// 							HashMap<NonDuplicateArrayList<Integer>, Integer> candidates, 
	// 							int iteration, 
	// 							NonDuplicateArrayList<Integer> newTransaction){
	private void countSupport(NonDuplicateArrayList<String> transaction, 
								HashMap<NonDuplicateArrayList<String>, Integer> candidates, 
								int iteration, 
								NonDuplicateArrayList<String> newTransaction){
		int[] occurrenceCount = new int[transaction.size()];
		for (Entry<NonDuplicateArrayList<String>, Integer> entry: candidates.entrySet()){
			if (transaction.containsAll(entry.getKey())){
				candidates.put(entry.getKey(), entry.getValue() + 1);
				for (String i: entry.getKey())
					occurrenceCount[transaction.indexOf(i)]++;
			}
		}
		for (int i = 0; i < transaction.size(); i++){
			if (occurrenceCount[i] >= iteration)
				newTransaction.add(transaction.get(i));
		}
	}

	// private void makeHashTable(NonDuplicateArrayList<Integer> newTransaction, 
	// 							int[] hashTable, 
	// 							int iteration, 
	// 							int[] newHashTable, 
	// 							NonDuplicateArrayList<Integer> newnewTransaction){
	private void makeHashTable(NonDuplicateArrayList<String> newTransaction, 
								int[] hashTable, 
								int iteration, 
								int[] newHashTable, 
								NonDuplicateArrayList<String> newnewTransaction){
		int[] occurrenceCount = new int[newTransaction.size()];
		for (NonDuplicateArrayList<String> subset: getKSubset(newTransaction, iteration + 1)){
			boolean flag = true;
			for (NonDuplicateArrayList<String> subsubset: getKSubset(subset, iteration)){
				if (hashTable[Math.hash(subsubset, hashTable.length)] < Config.minSupCount){
					flag = false;
					break;
				}
			}
			if (flag){
				newHashTable[Math.hash(subset, newHashTable.length)]++;
				for (String i: subset)
					occurrenceCount[newTransaction.indexOf(i)]++;
			}
		}
		for (int i = 0; i < newTransaction.size(); i++){
			if (occurrenceCount[i] > 0)
				newnewTransaction.add(newTransaction.get(i));
		}
	}

	// reads a json string from a given file and convert it into a java class
	private DataSet readData(String filename){
		if ((new File(filename).exists())){
			// file exists, start reading data
			try {
				String line;
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
				// while ((line = bufferedReader.readLine()) != null);
				line = bufferedReader.readLine();
				bufferedReader.close();
				ObjectMapper mapper = new ObjectMapper();
				DataSet dataSet = mapper.readValue(line, DataSet.class);
				return dataSet;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			// file doesn't exist
			System.err.println("file doesn't exist");
			System.exit(1);
		}
		return null;
	}

	// retrieve the transactions from a given dataset
	// private ArrayList<NonDuplicateArrayList<Integer>> getTransactions(DataSet dataSet){
	private ArrayList<NonDuplicateArrayList<String>> getTransactions(DataSet dataSet){
		ArrayList<NonDuplicateArrayList<String>> transactions = new ArrayList<NonDuplicateArrayList<String>>();
		List<User> users = dataSet.getUsers();
		// many users
		for (User user: users){
			List<List<List<String>>> trajectories = user.getTrajectories();
			// each user has one or more trajectories
			for (List<List<String>> trajectory: trajectories){
				// each trajectory contains one or more records
				NonDuplicateArrayList<String> transaction = new NonDuplicateArrayList<String>();
				for (List<String> record: trajectory)
					transaction.add(record.get(0));
				transactions.add(transaction);
			}
		}
		return transactions;
	}

	// private ArrayList<NonDuplicateArrayList<Integer>> getKSubset(NonDuplicateArrayList<Integer> set, int k){
	private ArrayList<NonDuplicateArrayList<String>> getKSubset(NonDuplicateArrayList<String> set, int k){
		/*
		// recursive approach
		// ref: http://stackoverflow.com/questions/4504974/how-to-iteratively-generate-k-elements-subsets-from-a-set-of-size-n-in-java
		ArrayList<NonDuplicateArrayList<Integer>> subsets = new ArrayList<NonDuplicateArrayList<Integer>>();
		Integer[] subset = new Integer[k];
		processLargerSubsets(set, subset, 0, 0, subsets);
		return subsets;
		*/
		// iterative approach
		// ref: http://stackoverflow.com/questions/4504974/how-to-iteratively-generate-k-elements-subsets-from-a-set-of-size-n-in-java
		int c = (int) Math.binomial(set.size(), k);
		ArrayList<NonDuplicateArrayList<String>> subsets = new ArrayList<NonDuplicateArrayList<String>>();
		for (int i = 0; i < c; i++)
			subsets.add(new NonDuplicateArrayList<String>());
		int[] ind = k < 0?null:new int[k];
		for (int i = 0; i < k; i++)
			ind[i] = i;
		for (int i = 0; i < c; i++){
			for (int j = 0; j < k; j++)
				subsets.get(i).add(set.get(ind[j]));
			int x = ind.length - 1;
			boolean loop;
			do{
				loop = false;
				ind[x] = ind[x] + 1;
				if (ind[x] > set.size() - (k - x)){
					x--;
					loop = x >= 0;
				}
				else{
					for (int x1 = x + 1; x1 < ind.length; x1++)
						ind[x1] = ind[x1 - 1] + 1;
				}
			} while (loop);
		}
		return subsets;
	}

	/*private void processLargerSubsets(NonDuplicateArrayList<Integer> set, Integer[] subset, int subsetSize, int nextIndex, ArrayList<NonDuplicateArrayList<Integer>> subsets){
		if (subsetSize == subset.length){
			subsets.add(new NonDuplicateArrayList<Integer>(subset));
		}
		else{
			for (int j = nextIndex; j < set.size(); j++){
				subset[subsetSize] = set.get(j);
				processLargerSubsets(set, subset, subsetSize + 1, j + 1, subsets);
			}
		}
	}*/

	// private void printFrequentItemSet(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet, PrintWriter printWriter){
	private void printFrequentItemSet(ArrayList<NonDuplicateArrayList<String>> frequentItemSet, PrintWriter printWriter){
		ArrayList<String> result = new ArrayList<String>();
		for (NonDuplicateArrayList<String> set: frequentItemSet){
			ArrayList<String> tempArray = new ArrayList<String>();
			for (String i: set)
				tempArray.add(i);
			Collections.sort(tempArray);
			String tempString = "";
			for (String string: tempArray)
				tempString += string + ";";
			tempString = tempString.substring(0, tempString.length() - 1);
			result.add(tempString);
		}
		Collections.sort(result);
		for (String string: result)
			printWriter.println(string);
	}
}
