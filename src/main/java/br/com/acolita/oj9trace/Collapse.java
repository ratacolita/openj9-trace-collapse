package br.com.acolita.oj9trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Collapse {

    private static final Predicate<String> BEGIN = Pattern.compile("Time \\(UTC\\)").asPredicate();
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern BRACES = Pattern.compile("\\(");
    private static final Pattern TIME = Pattern.compile(":|\\.");

    private static class Frame {
        private final long start;
        private final String frame;
        private Frame(long start, String frame) {
            this.start = start;
            this.frame = frame;
        }
    }

    private static final Map<String, Stack<Frame>> STACK_MAP = new HashMap<>();
    private static final Map<String, Long> COUNT_MAP = new HashMap<>();

    public static void main(String[] args) throws IOException {
        final Iterator<String> iterator = gotoBegin(args[0]);
        while(iterator.hasNext()) {
            final String line = iterator.next();
            final String[] tokens = Stream.of(SPACE.split(line)).filter(s -> !"".equals(s)).collect(Collectors.toList()).toArray(new String[0]);
            final String thread = '*' == tokens[1].charAt(0) ? tokens[1].substring(1) : tokens[1];
            final Stack<Frame> stack = STACK_MAP.computeIfAbsent(thread, t -> new Stack<>());
            if("Entry".equals(tokens[3])) {
                stack.push(new Frame(getMiliseconds(tokens[0]), BRACES.split(tokens[4])[0].substring(1)));
            }
            else if("Exit".equals(tokens[3])) {
                final String currentStack = stack.stream().map(f -> f.frame).collect(Collectors.joining(";"));
                final Long count = COUNT_MAP.getOrDefault(currentStack, 0L);
                final Frame pop = stack.pop();
                COUNT_MAP.put(currentStack, count + (getMiliseconds(tokens[0]) - pop.start));
            }
        }
        for (final String key : COUNT_MAP.keySet()) {
            System.out.printf("%s %d\n", key, COUNT_MAP.get(key));
        }
    }

    private static long getMiliseconds(final String value) {
        final String[] tokens = TIME.split(value);
        return Long.valueOf(tokens[0]) * 3600 * 1000 + Long.valueOf(tokens[1]) * 60 * 1000 + Long.valueOf(tokens[2]) * 1000 + Long.valueOf(tokens[3]) / 1000000;
    }

    private static Iterator<String> gotoBegin(String arg) throws IOException {
        final Iterator<String> iterator = Files.readAllLines(Paths.get(arg)).iterator();
        if(iterator.hasNext()) {
            String next = iterator.next();
            while (iterator.hasNext() && BEGIN.test(next) == false) {
                    next = iterator.next();
            }
            if(BEGIN.test(next) == false) {
                System.exit(-1);
            }
        }
        else {
            System.exit(-1);
        }
        return iterator;
    }
}
