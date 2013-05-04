package classification;

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

public class Classification {
	public static void main(String[] args){
		Classification classification = new Classification();
		classification.start();
	}
	
	public Classification(){
	}

	public void start(){
		int iteration, numOfFrequentItemSets = 0, numOfFreqItems = 0;
		DataSet dataSet = readData(Config.filename);
		System.out.println(dataSet.toString());
		System.exit(0);
		// ArrayList<NonDuplicateArrayList<String>> transactions = getTransactions(dataSet);
		long startTime = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("##.#####");
		PrintWriter printWriter = null;
		String outFilename = "results/" + Config.filename + "_" + df.format(Config.minSup) + ".txt";

		try {
			printWriter = new PrintWriter(new FileOutputStream(outFilename, false), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
/*
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
		System.out.println("Done");*/
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
				line = line.replace("\"check-ins\":", "\"checkins\":");
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
}
