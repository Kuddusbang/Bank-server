import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.text.DecimalFormat;

class BetterServer {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static String output = "";
    private static String eor = "[EOR]"; // a code for end-of-response
    private static int balance = 0;    // in cents
    private static ArrayList<Integer> transactions = new ArrayList<Integer>();
    private static DecimalFormat df = new DecimalFormat("#0.00"); 
    
    // establishing a connection
    private static void setup() throws IOException {
        
        serverSocket = new ServerSocket(0);
        toConsole("Server port is " + serverSocket.getLocalPort());
        
        clientSocket = serverSocket.accept();

        // get the input stream and attach to a buffered reader
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        // get the output stream and attach to a printwriter
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        toConsole("Accepted connection from "
                 + clientSocket.getInetAddress() + " at port "
                 + clientSocket.getPort());
            
        sendGreeting();
    }
    
    // the initial message sent from server to client
    private static void sendGreeting()
    {
        appendOutput("Greetings from PigNet!\n");
        appendOutput("Please enter your username : \n");
        sendOutput();
    }
    
    // what happens while client and server are connected
    private static void talk() throws IOException {
        /* placing echo functionality into a separate private method allows it to be easily swapped for a different behaviour */
        echoClient();
        disconnect();
    }
    
    // repeatedly take input from client and send back in upper case
    private static void echoClient() throws IOException
    {
        String inputLine;
        int count = 0;
        while (count != 5) {
            inputLine = in.readLine();
            if (inputLine.equals("Peppa")){
                appendOutput("Please enter the password: ");
                sendOutput();
                inputLine = in.readLine();
                if(inputLine.equals("OINK")){
                    clientWindow();
                }
                else {
                    appendOutput("Password incorrect.\n");
                    appendOutput("Please enter your username : ");
                    sendOutput();
                    count++;
                }
            }
            else {
                appendOutput("Username incorrect.\n");
                appendOutput("Please enter your username : ");
                sendOutput();
                count++;
            }
            toConsole(inputLine);
        }
    }
    
    private static void disconnect() throws IOException {
        out.close();
        toConsole("Disconnected.");
        System.exit(0);
    }
    
    // add a line to the next message to be sent to the client
    private static void appendOutput(String line) {
        output += line + "\r";
    }
    
    // send next message to client
    private static void sendOutput() {
        out.println( output + "[EOR]");
        out.flush();
        output = "";
    }
    
    // because it makes life easier!
    private static void toConsole(String message) {
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        try {
            setup();
            talk();
            clientWindow();
        }
        catch( IOException ioex ) {
            toConsole("Error: " + ioex );
        }
    }
    public static void clientWindow()
    {    
        appendOutput("Welcome to PigNet!");
        int selection = 0;
        while (selection != 4)
        {
            appendOutput("Your current balance is:\t" + inDollars(balance));
            selection = menu();
            switch ( selection )
            {
                case 1: deposit(); break;
                case 2: withdraw(); break;
                case 3: viewTransactions(); break;
                case 4: exit(); break;
                default: tryAgain();
            }
        }
    }
    
    private static int menu()
    {
        appendOutput("\nPlease select an option:");
        appendOutput("1. Make a deposit");
        appendOutput("2. Make a withdrawal");
        appendOutput("3. View a list of all transactions");
        appendOutput("4. Exit");
        appendOutput("\nYour selection:\t");
        sendOutput();

        int selection = 0;
        try
        {
            selection = Integer.parseInt(in.readLine()); 
            System.out.println("Option selected");
        }
        catch ( Exception ex )
        {
            // can ignore; main() will handle since selection = 0;
        }
        return selection;
    }
    
    private static void deposit()
    {
        appendOutput("\nEnter an amount to deposit in $$.cc: ");
        sendOutput();
        try
        {
            double amount = Double.parseDouble(in.readLine());
            System.out.println("Deposit made");
            int inCents = Math.abs((int) (amount * 100));
            balance += inCents;
            transactions.add(inCents);
        }
        catch ( Exception ex )
        {
            wrongFormat();
        }
    }
    
    private static void withdraw()
    {
        appendOutput("\nEnter an amount to withdraw in $$.cc: ");
        sendOutput();
        try
        {
            double amount = Double.parseDouble(in.readLine());
            System.out.println("Withdrawal made");
            int inCents = Math.abs((int) (amount * 100));
            balance -= inCents;
            transactions.add(0 - inCents);
        }
        catch ( Exception ex )
        {
            wrongFormat();
        }
    }
    
    private static void viewTransactions()
    {
        appendOutput("\nTransactions Performed:");
        for (Integer item : transactions)
        {
            if (item > 0)
            {
                appendOutput("deposit:\t" + inDollars(item));
            }
            else
            {
                item = 0 - item;
                appendOutput("withdrawal:\t" + inDollars(item)); 
            }
        }
        System.out.println("transaction shown");
    }
    
    private static void exit()
    {
        appendOutput("Thank you for using PigNet.  Have a nice day!\n");
        sendOutput();
        System.out.println("exit called");
        System.exit(0);
    }
    
    private static void tryAgain()
    {
        appendOutput("\nPlease make a valid selection!\n");
        System.out.println("tryagain called");
        sendOutput();
    }
    
    private static void wrongFormat()
    {
        appendOutput("\nNot a valid amount in $$.cc\n");
        System.out.println("wrong format");
        sendOutput();
    }
    
    private static String inDollars(int inCents)
    {
        return "$" + df.format(inCents/100.00);
    }
}
