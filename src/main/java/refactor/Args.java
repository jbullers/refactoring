package refactor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class Args {

    private final String schema;
    private final String[] args;
    private final Map<Character, Boolean> booleanArgs = new HashMap<>();
    private final Map<Character, String> stringArgs = new HashMap<>();
    private final Map<Character, Integer> intArgs = new HashMap<>();
    private final Set<Character> argsFound = new HashSet<>();
    private int currentArgument;

    public Args(String schema, String[] args) throws ArgsException {
        this.schema = schema;
        this.args = args;
        parse();
    }

    private void parse() throws ArgsException {
        if (schema.isEmpty() && args.length == 0) return;
        parseSchema();
        parseArguments();
    }

    private void parseSchema() throws ArgsException {
        for (String element : schema.split(",")) {
            if (!element.isEmpty()) {
                String trimmedElement = element.trim();
                parseSchemaElement(trimmedElement);
            }
        }
    }

    private void parseSchemaElement(String element) throws ArgsException {
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if (isBooleanSchemaElement(elementTail))
            parseBooleanSchemaElement(elementId);
        else if (isStringSchemaElement(elementTail))
            parseStringSchemaElement(elementId);
        else if (isIntegerSchemaElement(elementTail)) {
            parseIntegerSchemaElement(elementId);
        } else {
            throw new ArgsException("Argument: %c has invalid format: %s.".formatted(elementId, elementTail));
        }
    }

    private void validateSchemaElementId(char elementId) throws ArgsException {
        if (!Character.isLetter(elementId)) {
            throw new ArgsException("Bad character: %c in Args format: %s".formatted(elementId, schema));
        }
    }

    private void parseBooleanSchemaElement(char elementId) {
        booleanArgs.put(elementId, false);
    }

    private void parseIntegerSchemaElement(char elementId) {
        intArgs.put(elementId, 0);
    }

    private void parseStringSchemaElement(char elementId) {
        stringArgs.put(elementId, "");
    }

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private boolean isBooleanSchemaElement(String elementTail) {
        return elementTail.isEmpty();
    }

    private boolean isIntegerSchemaElement(String elementTail) {
        return elementTail.equals("#");
    }

    private void parseArguments() throws ArgsException {
        for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
            String arg = args[currentArgument];
            parseArgument(arg);
        }
    }

    private void parseArgument(String arg) throws ArgsException {
        if (arg.startsWith("-")) {
            parseElements(arg);
        }
    }

    private void parseElements(String arg) throws ArgsException {
        for (int i = 1; i < arg.length(); i++) {
            parseElement(arg.charAt(i));
        }
    }

    private void parseElement(char argChar) throws ArgsException {
        if (setArgument(argChar)) {
            argsFound.add(argChar);
        } else {
            throw new ArgsException("Argument -%c unexpected".formatted(argChar));
        }
    }

    private boolean setArgument(char argChar) throws ArgsException {
        if (isBooleanArg(argChar))
            setBooleanArg(argChar, true);
        else if (isStringArg(argChar))
            setStringArg(argChar);
        else if (isIntArg(argChar))
            setIntArg(argChar);
        else
            return false;

        return true;
    }

    private boolean isIntArg(char argChar) {return intArgs.containsKey(argChar);}

    private void setIntArg(char argChar) throws ArgsException {
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

    private void setStringArg(char argChar) throws ArgsException {
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

    public String usage() {
        return !schema.isEmpty() ? "-[" + schema + "]" : "";
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