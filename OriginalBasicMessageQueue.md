
```
public class BasicMessageQueue implements MessageQueue {
	private final LinkedList<Message> list;
	private volatile int size;
	private volatile long total;
	
	public BasicMessageQueue() {
		list = new LinkedList<Message>();
		size = 0;
		total = 0;
	}
	
	public void add(Message message) {
		if (message == null) throw new NullPointerException("Message must not be null");
		
		synchronized (list) {
			list.addLast(message);
		}
		size++;
		total++;
	}

	public Message poll() {
		if (isEmpty()) return null;
		
		synchronized (list) {
			Message m = list.poll();
			if (m != null) size--;
			return m;
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public long getTotal() {
		return total;
	}

	public int getSize() {
		return size;
	}

	@SuppressWarnings("all")
	public List<Message> clonedList() {
		return (List<Message>)list.clone();
	}
	
	public void remove(Message message) {
		synchronized(list) {
			if (list.remove(message)) {
				size--;
			}
		}
	}
}
```