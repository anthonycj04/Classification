package classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class UserInfo {
	private HashSet<Integer> friends; // friendlist
	private HashMap<String, Integer> numOfCheckins; // number of checkins of an location, excluding checkin month
	private HashMap<String, ArrayList<String>> checkins; //number of checkins of an location, including checkin month

	public UserInfo(){
		friends = new HashSet<Integer>();
		numOfCheckins = new HashMap<String, Integer>();
		checkins = new HashMap<String, ArrayList<String>>();
	}

	public void addCheckin(String location, String date){
		if (!checkins.containsKey(location))
			checkins.put(location, new ArrayList<String>());
		checkins.get(location).add(date);
	}

	public void addFriend(Integer uid){
		friends.add(uid);
	}

	public void incNumOfCheckin(String location){
		if (numOfCheckins.containsKey(location))
			numOfCheckins.put(location, numOfCheckins.get(location) + 1);
		else
			numOfCheckins.put(location, 1);
	}

	public int getNumOfCheckin(String location){
		if (numOfCheckins.containsKey(location))
			return numOfCheckins.get(location);
		else
			return 0;
	}

	public HashSet<Integer> getFriends(){
		return friends;
	}

	// caculates the number of friends that visited the location
	public int getNumOfFriendsVisited(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		for (Integer friend: friends){
			if (users.get(friend).getNumOfCheckin(location) > 0)
				sum++;
		}
		return sum;
	}

	// calculates the number of visits to the location according to friends
	public int getNumOfVisitsOfFriends(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		for (Integer friend: friends)
			sum += users.get(friend).getNumOfCheckin(location);
		return sum;
	}

	// caculates the number of friends and friends of friends that visited the location
	public int getNumOfFriendsOfFriendsVisited(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		HashSet<Integer> checked = new HashSet<Integer>(); // used to record whether already counted a person or not
		for (Integer friend: friends){
			if (!checked.contains(friend) && users.get(friend).getNumOfCheckin(location) > 0){
				checked.add(friend);
				sum++;
			}
			for (Integer friendsOfFriends: users.get(friend).getFriends()){
				if (!checked.contains(friendsOfFriends) && users.get(friendsOfFriends).getNumOfCheckin(location) > 0){
					checked.add(friendsOfFriends);
					sum++;
				}
			}
		}
		return sum;
	}

	// calculates the number of visits to the location according to friends and friends of friends
	public int getNumOfVisitsOfFriendsOfFriends(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		HashSet<Integer> checked = new HashSet<Integer>(); // used to record whether already counted a person or not
		for (Integer friend: friends){
			if (!checked.contains(friend)){
				checked.add(friend);
				sum += users.get(friend).getNumOfCheckin(location);
			}
			for (Integer friendsOfFriends: users.get(friend).getFriends()){
				if (!checked.contains(friendsOfFriends)){
					checked.add(friendsOfFriends);
					sum += users.get(friendsOfFriends).getNumOfCheckin(location);
				}
			}
		}
		return sum;
	}

	// calculates the nearest distance of visited places and the target location
	public double getNearestDistanceOfVisited(String location, HashMap<String, LocationInfo> locations){
		double min = 999999999, distance;
		for (Entry<String, Integer> entry: numOfCheckins.entrySet()){
			distance = Math.calculateDistance(locations.get(location), locations.get(entry.getKey()));
			min = distance < min?distance:min;
		}
		return min;
	}

	// calculates the nearest distance of visited places in the given month and the target location
	public double getNearestDistanceOfVisitedLastMonth(String location, HashMap<String, LocationInfo> locations, String date){
		double min = 999999999, distance;
		for (Entry<String, ArrayList<String>> entry: checkins.entrySet()){
			for (String visitedData: entry.getValue()){
				if (visitedData.equals(date)){
					distance = Math.calculateDistance(locations.get(location), locations.get(entry.getKey()));
					min = distance < min?distance:min;
					break;
				}
			}
		}
		return min;
	}
}
