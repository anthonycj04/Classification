package classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jsonobject.DataSet;
import jsonobject.User;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Classification {
	public static void main(String[] args){
		Classification classification = new Classification();
		classification.start();
	}
	
	public Classification(){
	}

	public void start(){
		long startTime;
		DataSet dataSet;
		HashMap<String, Integer> locations = new HashMap<String, Integer>(); // used to store the number of checkins of a location
		HashMap<Integer, UserInfo> users = new HashMap<Integer, UserInfo>();  // used to store the checkins and friendlist of a user

		startTime = System.currentTimeMillis();
		dataSet = readData(Config.filename);
		System.out.println("reading time: " + (double)(System.currentTimeMillis() - startTime) / 1000);

		startTime = System.currentTimeMillis();
		convertDataSet(dataSet, locations, users);
		System.out.println("converting time: " + (double)(System.currentTimeMillis() - startTime) / 1000);

		startTime = System.currentTimeMillis();
		classify("dm2013_dataset_3_100result.dat", locations, users, "dm2013_dataset_3_100_myresult.dat");
		System.out.println("classification time: " + (double)(System.currentTimeMillis() - startTime) / 1000);
		// ArrayList<NonDuplicateArrayList<String>> transactions = getTransactions(dataSet);
		// DecimalFormat df = new DecimalFormat("##.#####");
		// PrintWriter printWriter = null;
		// String outFilename = "results/" + Config.filename + "_" + df.format(Config.minSup) + ".txt";

		// try {
		// 	printWriter = new PrintWriter(new FileOutputStream(outFilename, false), true);
		// } catch (FileNotFoundException e) {
		// 	e.printStackTrace();
		// }

		// print out the results
		/*
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
		System.out.println("# of users: " + dataSet.getUsers().size());
		System.out.println("# of records: " + dataSet.getTotalNumOfRecords());
		System.out.println("# of freindships: " + dataSet.getNumOfFriendships());
		System.out.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
		System.out.println("Start time: " + dataSet.getStartTime());
		System.out.println("End time: " + dataSet.getEndTime());
		System.out.println("Done");
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

	// read thourgh the dataset and convert it to more accessable data structure
	private void convertDataSet(DataSet dataSet, HashMap<String, Integer> locations, HashMap<Integer, UserInfo> users){
		// initialize the locations
		for (Entry<String, String> entry: dataSet.getLocations().getLocationMap().entrySet())
			locations.put(entry.getKey(), 0);
		// run through the data and add the user's checkin into the hashmap, and also count the number of checkins of a location
		for (User user: dataSet.getUsers()){
			// add the user if it doesn't exist
			if (!users.containsKey(user.getUid()))
				users.put(user.getUid(), new UserInfo());
			UserInfo tempUserInfo = users.get(user.getUid());
			for (List<String> checkin: user.getCheckins()){
				tempUserInfo.addCheckin(checkin.get(0));
				locations.put(checkin.get(0), locations.get(checkin.get(0)) + 1);
			}
		}
		// run through the friendships and add the relationships
		// TODO: assuming the user here all exists, not sure if this is right
		for (List<Integer> friends: dataSet.getFriendships()){
			if (!users.containsKey(friends.get(0)))
				users.put(friends.get(0), new UserInfo());
			if (!users.containsKey(friends.get(1)))
				users.put(friends.get(1), new UserInfo());
			users.get(friends.get(0)).addFriend(friends.get(1));
			users.get(friends.get(1)).addFriend(friends.get(0));
		}
	}

	private void classify(String filename, HashMap<String, Integer> locations, HashMap<Integer, UserInfo> users, String outFilename){
			if ((new File(filename).exists())){
			// file exists, start reading data
			try {
				String line, location;
				String[] splittedLine;
				Integer uid;
				UserInfo tempUserInfo;
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
				PrintWriter printWriter = new PrintWriter(new FileOutputStream(outFilename, false), true);
				// DecimalFormat df = new DecimalFormat("##.#####");
				// String outFilename = "results/" + Config.filename + "_" + df.format(Config.minSup) + ".txt";
				printWriter.println("\t\t\t\t\t\t\t#of visits\t#of friends\t#of friend's visits\t#of visits of location");
				while ((line = bufferedReader.readLine()) != null){
					splittedLine = line.split(";");
					uid = Integer.valueOf(splittedLine[0]);
					location = splittedLine[1];
					tempUserInfo = users.get(uid);
					printWriter.println(line + "\t\t" + 
										tempUserInfo.getCheckin(location) + "\t\t\t" + 
										tempUserInfo.getNumOfFriendsVisited(location, users) + "\t\t\t" + 
										tempUserInfo.getNumOfVisitsOfFriends(location, users) + "\t\t\t\t\t" + 
										locations.get(location));
				}
				bufferedReader.close();
				printWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			// file doesn't exist
			System.err.println("file doesn't exist");
			System.exit(1);
		}
	}
}
