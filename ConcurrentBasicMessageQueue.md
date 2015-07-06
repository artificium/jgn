
```
public class BasicMessageQueue implements MessageQueue {
	private Queue<Message> list;
	private volatile long total;
	private List<Message> clone;
	
	public BasicMessageQueue() {
		list = new ConcurrentLinkedQueue<Message>();
		clone = new ArrayList<Message>();
		total = 0;
	}
	
	public void add(Message message) {
		if (message == null) throw new NullPointerException("Message must not be null");
		
		list.add(message);

		total++;
	}

	public Message poll() {
		return list.poll();
	}

	public boolean isEmpty() {
		return list.size() == 0;
	}

	public long getTotal() {
		return total;
	}

	public int getSize() {
		return list.size();
	}
	
	public void remove(Message message) {
		list.remove(message);
	}
	
	public List<Message> clonedList() {
		clone.clear();
		for (Message m : list) {
			clone.add(m);
		}
		return clone;
	}
}
```