package jsonobject;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class User{
	private int uid;
	private List<List<String>> checkins;

	public int getNumOfRecords(){
		return checkins.size();
	}

	public int getUid(){
		return uid;
	}

	public void setUid(String uid){
		this.uid = Integer.valueOf(uid);
	}

	public List<List<String>> getCheckins(){
		return checkins;
	}

	public void setCheckins(List<List<String>> checkins){
		this.checkins = checkins;
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value){
		System.out.println("handleUnknown in " + User.class.toString());
	}

	@Override
	public String toString(){
		String string = "{";
		string += "check-ins: [";
		for (int i = 0; i < checkins.size(); i++)
			string += "[" + checkins.get(i).get(0) + ", " + checkins.get(i).get(1) + "],";
		string += "]," + "uid: " + uid + "}";

		return string;
	}
}
