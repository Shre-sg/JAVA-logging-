import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import java.util.concurrent.*;
import java.util.logging.*;

class Calculator implements ActionListener {

    JFrame frame;
    JTextArea textArea;
    JButton[] numberButtons = new JButton[10];
    JButton[] functionButtons = new JButton[9];
    JButton addButton, subButton, mulButton, divButton;
    JButton decButton, equButton, delButton, clrButton, negButton;
    JPanel panel;
    Font myFont = new Font("Ink Free", Font.BOLD, 30);
    double num1 = 0;
    double num2 = 0;
    char operator;
    StringBuilder inputExpression = new StringBuilder();
    Stack<Character> operatorStack = new Stack<>();
    Stack<Double> operandStack = new Stack<>();
    Logger logger = Logger.getLogger(Calculator.class.getName());

    Calculator() {
        frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 550);
        frame.setLayout(null);

        textArea = new JTextArea();
        textArea.setBounds(50, 25, 300, 100);
        textArea.setFont(myFont);
        textArea.setEditable(false);

        addButton = new JButton("+");
        subButton = new JButton("-");
        mulButton = new JButton("*");
        divButton = new JButton("/");
        decButton = new JButton(".");
        equButton = new JButton("=");
        delButton = new JButton("Del");
        clrButton = new JButton("Clr");
        negButton = new JButton("(-)");

        functionButtons[0] = addButton;
        functionButtons[1] = subButton;
        functionButtons[2] = mulButton;
        functionButtons[3] = divButton;
        functionButtons[4] = decButton;
        functionButtons[5] = equButton;
        functionButtons[6] = delButton;
        functionButtons[7] = clrButton;
        functionButtons[8] = negButton;

        for (int i = 0; i < 9; i++) {
            functionButtons[i].addActionListener(this);
            functionButtons[i].setFont(myFont);
            functionButtons[i].setFocusable(false);
        }

        for (int i = 0; i < 10; i++) {
            numberButtons[i] = new JButton(String.valueOf(i));
            numberButtons[i].addActionListener(this);
            numberButtons[i].setFont(myFont);
            numberButtons[i].setFocusable(false);
        }

        negButton.setBounds(50, 430, 100, 50);
        delButton.setBounds(150, 430, 100, 50);
        clrButton.setBounds(250, 430, 100, 50);

        panel = new JPanel();
        panel.setBounds(50, 150, 300, 250);
        panel.setLayout(new GridLayout(4, 4, 10, 10));

        panel.add(numberButtons[1]);
        panel.add(numberButtons[2]);
        panel.add(numberButtons[3]);
        panel.add(addButton);
        panel.add(numberButtons[4]);
        panel.add(numberButtons[5]);
        panel.add(numberButtons[6]);
        panel.add(subButton);
        panel.add(numberButtons[7]);
        panel.add(numberButtons[8]);
        panel.add(numberButtons[9]);
        panel.add(mulButton);
        panel.add(decButton);
        panel.add(numberButtons[0]);
        panel.add(equButton);
        panel.add(divButton);

        frame.add(panel);
        frame.add(negButton);
        frame.add(delButton);
        frame.add(clrButton);
        frame.add(textArea);
        frame.setVisible(true);

        setupLogging();
    }

    private void setupLogging() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler("mycalci.log");
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logger.info("<---calculator program opened --->.");

        for (int i = 0; i < 10; i++) {
            if (e.getSource() == numberButtons[i]) {
                inputExpression.append(i);
                textArea.setText(inputExpression.toString());
            }
        }

        if (e.getSource() == decButton) {
            inputExpression.append(".");
            textArea.setText(inputExpression.toString());
        }

        if (e.getSource() == addButton || e.getSource() == subButton ||
                e.getSource() == mulButton || e.getSource() == divButton) {
            char newOperator = e.getActionCommand().charAt(0);
            handleOperator(newOperator);
        }

        if (e.getSource() == equButton) {
            try {
                evaluateExpression();
            } catch (Exception ex) {
                textArea.setText("Error");
                logger.warning("An error occurred during calculation: " + ex.getMessage());
            }
        }

        if (e.getSource() == clrButton) {
            clearInput();
        }

        if (e.getSource() == delButton) {
            deleteLastCharacter();
        }

        if (e.getSource() == negButton) {
            inputExpression.insert(0, "-");
            textArea.setText(inputExpression.toString());
        }
    }

    private void handleOperator(char newOperator) {
        while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(newOperator)) {
            inputExpression.append(operatorStack.pop());
        }
        operatorStack.push(newOperator);
        textArea.setText(inputExpression.toString());
    }

    private int precedence(char operator) {
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/') {
            return 2;
        }
        return 0;
    }

    private void evaluateExpression() {
        while (!operatorStack.isEmpty()) {
            inputExpression.append(operatorStack.pop());
        }
        String postfixExpression = inputExpression.toString();
        double result = evaluatePostfix(postfixExpression);
        textArea.setText(postfixExpression + " = " + result);
        inputExpression.setLength(0);
        inputExpression.append(result);
    }

    private double evaluatePostfix(String postfixExpression) {
        Stack<Double> stack = new Stack<>();
        for (char c : postfixExpression.toCharArray()) {
            if (Character.isDigit(c)) {
                stack.push((double) (c - '0'));
            } else {
                double operand2 = stack.pop();
                double operand1 = stack.pop();
                double result = performCalculation(operand1, operand2, c, logger);
                stack.push(result);
            }
        }
        return stack.pop();
    }

    private void clearInput() {
        inputExpression.setLength(0);
        textArea.setText("");
    }

    private void deleteLastCharacter() {
        if (inputExpression.length() > 0) {
            inputExpression.deleteCharAt(inputExpression.length() - 1);
            textArea.setText(inputExpression.toString());
        }
    }

    private double performCalculation(double num1, double num2, char operator, Logger logger) {
        switch (operator) {
            case '+':
                if (logger != null) {
                    logger.info("<--NUM1 = " + num1 + " & NUM2 = " + num2 + " --->");
                    logger.info("<----ADD was chosen as Operation--->>");
                }
                return num1 + num2;

            case '-':
                if (logger != null) {
                    logger.info("<--NUM1 = " + num1 + "& NUM2 = " + num2 + " --->");
                    logger.info("<----SUB was chosen as Operation--->>");
                }
                return num1 - num2;

            case '*':
                if (logger != null) {
                    logger.info("<--NUM1 = " + num1 + " & NUM2 = " + num2 + " --->");
                    logger.info("<----MULTIPLY was chosen as Operation--->>");
                }
                return num1 * num2;

            case '/':
                if (num2 == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                if (logger != null) {
                    logger.info("<--NUM1 = " + num1 + " & NUM2 = " + num2 + " --->");
                    logger.info("<----DIV was chosen as Operation--->>");
                }
                return num1 / num2;

            default:
                throw new IllegalArgumentException("Invalid operator");
        }
    }
}