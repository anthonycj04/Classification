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
		HashMap<Integer, UserInfo> users = new HashMap<Integer, UserInfo>();  // used to store the checkins and friendlist of a user
		HashMap<String, LocationInfo> locations = new HashMap<String, LocationInfo>();

		startTime = System.currentTimeMillis();
		dataSet = readData(Config.dataFilename);
		System.out.println("reading time: " + (double)(System.currentTimeMillis() - startTime) / 1000);

		startTime = System.currentTimeMillis();
		convertDataSet(dataSet, locations, users);
		System.out.println("converting time: " + (double)(System.currentTimeMillis() - startTime) / 1000);

		startTime = System.currentTimeMillis();
		classify(Config.inputFilename, locations, users, Config.outputFilename);
		System.out.println("classification time: " + (double)(System.currentTimeMillis() - startTime) / 1000);

		checkAccuracy(Config.outputFilename, Config.answerFilename);
		// System.out.println("# of users: " + dataSet.getUsers().size());
		// System.out.println("# of records: " + dataSet.getTotalNumOfRecords());
		// System.out.println("# of freindships: " + dataSet.getNumOfFriendships());
		// System.out.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
		// System.out.println("Start time: " + dataSet.getStartTime());
		// System.out.println("End time: " + dataSet.getEndTime());
		System.out.println("Done");
	}

	// reads a json string from a given file and convert it into a java class
	private DataSet readData(String filename){
		if ((new File(filename).exists())){
			// file exists, start reading data
			try {
				String line;
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
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
	private void convertDataSet(DataSet dataSet, 
								HashMap<String, LocationInfo>  locations, 
								HashMap<Integer, UserInfo> users){
		String date;
		double xCoordinate, yCoordinate;
		String[] splittedCoordinates;
		// initialize the locations
		for (Entry<String, String> entry: dataSet.getLocations().getLocationMap().entrySet()){
			splittedCoordinates = entry.getValue().split(",");
			xCoordinate = Double.valueOf(splittedCoordinates[0].substring(1));
			yCoordinate = Double.valueOf(splittedCoordinates[1].substring(1, splittedCoordinates[1].length() - 1));
			locations.put(entry.getKey(), new LocationInfo(xCoordinate, yCoordinate));
		}
		// run through the data and add the user's checkin into the hashmap, and also count the number of checkins of a location
		for (User user: dataSet.getUsers()){
			// add the user if it doesn't exist
			if (!users.containsKey(user.getUid()))
				users.put(user.getUid(), new UserInfo());
			UserInfo tempUserInfo = users.get(user.getUid());
			for (List<String> checkin: user.getCheckins()){
				date = checkin.get(1).substring(0, 7);

				tempUserInfo.incNumOfCheckin(checkin.get(0));
				locations.get(checkin.get(0)).incNumOfVisits();
				tempUserInfo.addCheckin(checkin.get(0), date);
				
				locations.get(checkin.get(0)).incNumOfVisitsPerMonth(date);
			}
		}
		// run through the friendships and add the relationships
		for (List<Integer> friends: dataSet.getFriendships()){
			if (!users.containsKey(friends.get(0)))
				users.put(friends.get(0), new UserInfo());
			if (!users.containsKey(friends.get(1)))
				users.put(friends.get(1), new UserInfo());
			users.get(friends.get(0)).addFriend(friends.get(1));
			users.get(friends.get(1)).addFriend(friends.get(0));
		}
	}

	// try to classify the input file with the given data, and writes the result to the output file
	private void classify(String inputFilename, 
							HashMap<String, LocationInfo>  locations, 
							HashMap<Integer, UserInfo> users, 
							String outputFilename){
		if ((new File(inputFilename).exists())){
			// file exists, start reading data
			try {
				String line, location, result;
				String[] splittedLine;
				Integer uid;
				UserInfo tempUserInfo;
				BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilename));
				PrintWriter printWriter = new PrintWriter(new FileOutputStream(outputFilename, false), true);
				// DecimalFormat df = new DecimalFormat("##.#######");
				// String outFilename = "results/" + Config.filename + "_" + df.format(Config.minSup) + ".txt";
				// printWriter.println("\t\t\t\t\t\t\t#of visits\t#of friends\t#of friend's visits\t#of visits of location\t#of fof\t#of fof's visits");
				while ((line = bufferedReader.readLine()) != null){
					splittedLine = line.split(";");
					uid = Integer.valueOf(splittedLine[0]);
					location = splittedLine[1];
					tempUserInfo = users.get(uid);
					if (tempUserInfo.getNumOfCheckin(location) < Config.checkinThreshold && 
						tempUserInfo.getNumOfFriendsVisited(location, users) < Config.numOfFriendsVisitedThreshold && 
						tempUserInfo.getNumOfVisitsOfFriends(location, users) < Config.numOfVisitsOfFriendsThreshold && 
						locations.get(location).getNumOfVisits() < Config.locationThreshold && 
						tempUserInfo.getNearestDistanceOfVisited(location, locations) > Config.nearestDistanceOfVisitedThreshold)
						// tempUserInfo.getNearestDistanceOfVisitedLastMonth(location, locations, "2010-07") > Config.nearestDistanceOfVisitedLastMonthThreshold)
						result = ";No";
					else
						result = ";Yes";
					printWriter.println(line + result);
					// String temp = line.indexOf(result) == -1?"nooooooooooooo":"";
					// printWriter.println(line + result + "\t" + 
					// 					tempUserInfo.getNumOfCheckin(location) + "\t\t\t" + 
					// 					tempUserInfo.getNumOfFriendsVisited(location, users) + "\t\t\t" + 
					// 					tempUserInfo.getNumOfVisitsOfFriends(location, users) + "\t\t\t\t\t" + 
					// 					locations.get(location).getNumOfVisits() + "\t\t\t\t\t\t" + 
					// 					tempUserInfo.getNumOfFriendsOfFriendsVisited(location, users) + "\t\t" + 
					// 					tempUserInfo.getNumOfVisitsOfFriendsOfFriends(location, users) +  "\t" + 
					// 					df.format(tempUserInfo.getNearestDistanceOfVisited(location, locations)) +"\t" + 
					// 					df.format(tempUserInfo.getNearestDistanceOfVisitedLastMonth(location, locations, "2010-07")) + "\t" + 
					// 					temp);
					// for (Entry<String, Integer> numOfVisitsPerMonth: locations.get(location).getNumOfVisitsPerMonth().entrySet()){
					// 	printWriter.println(numOfVisitsPerMonth.getKey() + ": " + numOfVisitsPerMonth.getValue() + ", ");
					// }
					// if (uid == 188862)
					// 	printWriter.println();
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

	// check the accuracy by reading the output file and the answer file
	private void checkAccuracy(String outputFilename, String answerFilename){
		if ((new File(outputFilename).exists()) && (new File(answerFilename).exists())){
			// file exists, start reading data
			int totalTries = 0, correctTries = 0;
			try {
				String inLine, outLine;
				BufferedReader inBufferedReader = new BufferedReader(new FileReader(outputFilename));
				BufferedReader outBufferedReader = new BufferedReader(new FileReader(answerFilename));
				while ((inLine = inBufferedReader.readLine()) != null){
					outLine = outBufferedReader.readLine();
					totalTries++;
					if (outLine.equals(inLine))
						correctTries++;
				}
				inBufferedReader.close();
				outBufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Accuracy: " + (double) correctTries / totalTries);
		}
		else{
			// file doesn't exist
			System.err.println("file doesn't exist");
			System.exit(1);
		}
	}
}
