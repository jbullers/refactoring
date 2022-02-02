package refactor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class Args {

    private final Map<Character, Boolean> booleanArgs = new HashMap<>();
    private final Map<Character, String> stringArgs = new HashMap<>();
    private final Map<Character, Integer> intArgs = new HashMap<>();

    private final Set<Character> argsFound = new HashSet<>();

    private int currentArgument;

    public Args(String schema, String[] args) throws ArgsException {
        parseSchema(schema);
        parseArguments(args);
    }

    private void parseSchema(String schema) throws ArgsException {
        for (String element : schema.split(",")) {
            if (!element.isEmpty()) {
                String trimmedElement = element.trim();
                char elementId = trimmedElement.charAt(0);
                String elementTail = trimmedElement.substring(1);
                parseSchemaElement(validateSchemaElementId(elementId, schema), elementTail);
            }
        }
    }

    private static char validateSchemaElementId(char elementId, String schema) throws ArgsException {
        if (Character.isLetter(elementId)) {
            return elementId;
        } else {
            throw new ArgsException("Bad character: %c in Args format: %s".formatted(elementId, schema));
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
        for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
            String arg = args[currentArgument];
            parseArgument(arg, args);
        }
    }

    private void parseArgument(String arg, String[] args) throws ArgsException {
        if (arg.startsWith("-")) {
            parseElements(arg, args);
        }
    }

    private void parseElements(String arg, String[] args) throws ArgsException {
        for (int i = 1; i < arg.length(); i++) {
            parseElement(arg.charAt(i), args);
        }
    }

    private void parseElement(char argChar, String[] args) throws ArgsException {
        if (setArgument(argChar, args)) {
            argsFound.add(argChar);
        } else {
            throw new ArgsException("Argument -%c unexpected".formatted(argChar));
        }
    }

    private boolean setArgument(char argChar, String[] args) throws ArgsException {
        if (isBooleanArg(argChar))
            setBooleanArg(argChar, true);
        else if (isStringArg(argChar))
            setStringArg(argChar, args);
        else if (isIntArg(argChar))
            setIntArg(argChar, args);
        else
            return false;

        return true;
    }

    private boolean isIntArg(char argChar) {return intArgs.containsKey(argChar);}

    private void setIntArg(char argChar, String[] args) throws ArgsException {
        currentArgument++;
        String parameter = null;
        try {
            parameter = args[currentArgument];
            intArgs.put(argChar, parseInt(parameter));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArgsException("Could not find integer parameter for -%c.".formatted(argChar), e);
        } catch (NumberFormatException e) {
            throw new ArgsException("Argument -%c expects an integer but was '%s'.".formatted(argChar, parameter), e);
        }
    }

    private void setStringArg(char argChar, String[] args) throws ArgsException {
        currentArgument++;
        try {
            stringArgs.put(argChar, args[currentArgument]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArgsException("Could not find string parameter for -%c.".formatted(argChar), e);
        }
    }

    private boolean isStringArg(char argChar) {
        return stringArgs.containsKey(argChar);
    }

    private void setBooleanArg(char argChar, boolean value) {
        booleanArgs.put(argChar, value);
    }

    private boolean isBooleanArg(char argChar) {
        return booleanArgs.containsKey(argChar);
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