package Part3;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Mailer {

	private HashMap<Integer, List<Message>> map = new HashMap<>();
	public void send(int reciever, Message m) {
		synchronized (map) { // synchronizing the sending of messages => no sending to the same index at the
								// same time.
			map.get(reciever).add(m); // adding the message to the array list of the reciever.
		}
	}

	public Message readOne(int reciever) {

		if (this.map.get(reciever).size() == 0) { // returns null if the list of messages is empty.
			return null;
		}

		else {
			synchronized (map) { // synchronizing the reading of a message => no getting values to the same
									// message by more then one agent at a time.
				Message message = map.get(reciever).get(0); // getting the message => deleting it => returning it.
				map.get(reciever).remove(0);
				return message;
			}
		}
	}

	public HashMap<Integer, List<Message>> getMap() {
		return map;
	}
	

	

}
