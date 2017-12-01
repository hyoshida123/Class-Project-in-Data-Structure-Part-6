package xmlvalidator;

import java.util.*;

public class BasicStringStack implements StringStack {

	private int count = 0;
	private ArrayList<String> stack = new ArrayList<String>();

	@Override
	public void push(String item) {
		stack.add(item);
		count++;
	}

	@Override
	public String pop() {
		if (count == 0)
			return null;
		else {
			String item = stack.get(count - 1);
			stack.remove(count - 1);
			count--;
			return item;
		}
	}

	@Override
	public String peek(int position) {
		// Out of bounds.
		if ((position > count - 1) || (position < 0))
			return null;
		else
			// When you pass position = 0, you get the top.
			return stack.get(count - position - 1);
	}

	@Override
	public int getCount() {
		return count;
	}

}
