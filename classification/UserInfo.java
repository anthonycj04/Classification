package classification;

import java.util.HashMap;
import java.util.HashSet;

public class UserInfo {
	private HashSet<Integer> friends;
	private HashMap<String, Integer> checkins;

	public UserInfo(){
		friends = new HashSet<Integer>();
		checkins = new HashMap<String, Integer>();
	}

	void addFriend(Integer uid){
		friends.add(uid);
	}

	void addCheckin(String location){
		if (checkins.containsKey(location))
			checkins.put(location, checkins.get(location) + 1);
		else
			checkins.put(location, 1);
	}

	int getCheckin(String location){
		if (checkins.containsKey(location))
			return checkins.get(location);
		else
			return 0;
	}

	HashSet<Integer> getFriends(){
		return friends;
	}

	// caculates the number of friends that visited the location
	int getNumOfFriendsVisited(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		for (Integer friend: friends){
			if (users.get(friend).getCheckin(location) > 0)
				sum++;
		}
		return sum;
	}

	// calculates the number of visits to the location according to friends
	int getNumOfVisitsOfFriends(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		for (Integer friend: friends)
			sum += users.get(friend).getCheckin(location);
		return sum;
	}

	// caculates the number of friends and friends of friends that visited the location
	int getNumOfFriendsOfFriendsVisited(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		HashSet<Integer> checked = new HashSet<Integer>(); // used to record whether already counted a person or not
		for (Integer friend: friends){
			if (!checked.contains(friend) && users.get(friend).getCheckin(location) > 0){
				checked.add(friend);
				sum++;
			}
			for (Integer friendsOfFriends: users.get(friend).getFriends()){
				if (!checked.contains(friendsOfFriends) && users.get(friendsOfFriends).getCheckin(location) > 0){
					checked.add(friendsOfFriends);
					sum++;
				}
			}
		}
		return sum;
	}

	// calculates the number of visits to the location according to friends and friends of friends
	int getNumOfVisitsOfFriendsOfFriends(String location, HashMap<Integer, UserInfo> users){
		int sum = 0;
		HashSet<Integer> checked = new HashSet<Integer>(); // used to record whether already counted a person or not
		for (Integer friend: friends){
			if (!checked.contains(friend)){
				checked.add(friend);
				sum += users.get(friend).getCheckin(location);
			}
			for (Integer friendsOfFriends: users.get(friend).getFriends()){
				if (!checked.contains(friendsOfFriends)){
					checked.add(friendsOfFriends);
					sum += users.get(friendsOfFriends).getCheckin(location);
				}
			}
		}
		return sum;
	}
}
