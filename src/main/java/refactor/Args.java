package refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

public class Args {

    private final Map<Character, Boolean> booleanArgs = new HashMap<>();
    private final Map<Character, String> stringArgs = new HashMap<>();
    private final Map<Character, Integer> intArgs = new HashMap<>();

    private final Set<Character> argsFound = new HashSet<>();

    public Args(String schema, String[] args) throws ArgsException {
        parseSchema(schema);
        parseArguments(args);
    }

    private void parseSchema(String schema) throws ArgsException {
        for (String element : schema.split(",")) {
            if (!element.isEmpty()) {
                String trimmedElement = element.trim();
                char elementId = trimmedElement.charAt(0);
                if (Character.isLetter(elementId)) {
                    String elementTail = trimmedElement.substring(1);
                    parseSchemaElement(elementId, elementTail);
                } else {
                    throw new ArgsException("Bad character: %c in Args format: %s".formatted(elementId, schema));
                }
            }
        }
    }

    private void parseSchemaElement(char elementId, String elementTail) throws ArgsException {
        if (isBooleanSchemaElement(elementTail))
            booleanArgs.put(elementId, false);
        else if (isStringSchemaElement(elementTail))
            stringArgs.put(elementId, "");
        else if (isIntegerSchemaElement(elementTail)) {
            intArgs.put(elementId, 0);
        } else {
            throw new ArgsException("Argument: %c has invalid format: %s.".formatted(elementId, elementTail));
        }
    }

    private boolean isBooleanSchemaElement(String elementTail) {
        return elementTail.isEmpty();
    }

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private boolean isIntegerSchemaElement(String elementTail) {
        return elementTail.equals("#");
    }

    private void parseArguments(String[] args) throws ArgsException {
        for (var argsIter = new ArrayList<>(asList(args)).iterator(); argsIter.hasNext(); ) {
            String arg = argsIter.next();
            if (arg.startsWith("-")) {
                for (char argChar : arg.substring(1).toCharArray()) {
                    if (booleanArgs.containsKey(argChar))
                        booleanArgs.put(argChar, true);
                    else if (stringArgs.containsKey(argChar)) {
                        try {
                            stringArgs.put(argChar, argsIter.next());
                        } catch (NoSuchElementException e) {
                            throw new ArgsException("Could not find string parameter for -%c.".formatted(argChar), e);
                        }
                    } else if (intArgs.containsKey(argChar)) {
                        String parameter;
                        try {
                            parameter = argsIter.next();
                        } catch (NoSuchElementException e) {
                            throw new ArgsException("Could not find integer parameter for -%c.".formatted(argChar), e);
                        }

                        try {
                            intArgs.put(argChar, parseInt(parameter));
                        } catch (NumberFormatException e) {
                            throw new ArgsException("Argument -%c expects an integer but was '%s'.".formatted(argChar, parameter), e);
                        }
                    } else {
                        throw new ArgsException("Argument -%c unexpected".formatted(argChar));
                    }

                    argsFound.add(argChar);
                }
            }
        }
    }

    public int cardinality() {
        return argsFound.size();
    }

    public String getString(char arg) {
        return stringArgs.getOrDefault(arg, "");
    }

    public int getInt(char arg) {
        return intArgs.getOrDefault(arg, 0);
    }

    public boolean getBoolean(char arg) {
        return booleanArgs.getOrDefault(arg, false);
    }

    public boolean has(char arg) {
        return argsFound.contains(arg);
    }
}