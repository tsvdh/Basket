package core;

import java.util.Arrays;
import java.util.PriorityQueue;

public class StringQueue extends PriorityQueue<String> {

    @Override
    public String toString() {
        return super.toString();
    }

    public static StringQueue parse(String stringValue) {
        StringQueue queue = new StringQueue();

        if (stringValue.equals("[]")) {
            return queue;
        }

        String[] elements = stringValue.substring(1, stringValue.length() - 1).split(", ");

        queue.addAll(Arrays.asList(elements));

        return queue;
    }
}
